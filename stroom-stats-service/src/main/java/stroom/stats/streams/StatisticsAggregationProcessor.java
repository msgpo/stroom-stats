/*
 * Copyright 2017 Crown Copyright
 *
 * This file is part of Stroom-Stats.
 *
 * Stroom-Stats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Stroom-Stats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Stroom-Stats.  If not, see <http://www.gnu.org/licenses/>.
 */

package stroom.stats.streams;

import com.google.common.base.Preconditions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import stroom.stats.StatisticsProcessor;
import stroom.stats.api.StatisticType;
import stroom.stats.api.StatisticsService;
import stroom.stats.hbase.EventStoreTimeIntervalHelper;
import stroom.stats.mixins.Startable;
import stroom.stats.mixins.Stoppable;
import stroom.stats.properties.StroomPropertyService;
import stroom.stats.shared.EventStoreTimeIntervalEnum;
import stroom.stats.streams.aggregation.StatAggregate;
import stroom.stats.streams.serde.StatAggregateSerde;
import stroom.stats.streams.serde.StatKeySerde;
import stroom.stats.util.logging.LambdaLogger;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


/**
 * The following shows how the aggregation processing works for a single stat type
 * e.g. COUNT.  Events come in on one topic per aggregationInterval. Each aggregationInterval topic is
 * consumed and the events are aggregated together by StatKey (which all have their
 * time truncated to the aggregationInterval of the topic they came from.
 * <p>
 * Periodic flushes of the aggregated events are then forked to
 * the stat service for persistence and to the next biggest aggregationInterval topic for another
 * iteration. This waterfall approach imposes increasing latency as the intervals get bigger
 * but this should be fine as a query on the current DAY bucket will yield partial results as
 * the day is not yet over.
 * <p>
 * -------> consumer/producer SEC  -------->    statisticsService.putAggregatedEvents
 * __________________________|
 * V
 * -------> consumer/producer MIN  -------->    statisticsService.putAggregatedEvents
 * __________________________|
 * V
 * -------> consumer/producer HOUR -------->    statisticsService.putAggregatedEvents
 * __________________________|
 * V
 * -------> consumer/producer DAY  -------->    statisticsService.putAggregatedEvents
 * <p>
 * If the system goes down unexpectedly then events that have been read off a topic but not yet committed
 * may be re-processed to some extent depending on when the shutdown happened, e.g duplicate events may go to
 * the next topic and/or to the stat service. The size of the StatAggregator is a trade off between in memory aggregation
 * benefits and the risk of more duplicate data in the stat store
 */
public class StatisticsAggregationProcessor implements StatisticsProcessor, Startable, Stoppable {

    private static final LambdaLogger LOGGER = LambdaLogger.getLogger(StatisticsAggregationProcessor.class);

    public static final String PROP_KEY_AGGREGATOR_MIN_BATCH_SIZE = "stroom.stats.aggregation.minBatchSize";
    public static final String PROP_KEY_AGGREGATOR_MAX_FLUSH_INTERVAL_MS = "stroom.stats.aggregation.maxFlushIntervalMs";
    public static final String PROP_KEY_AGGREGATOR_POLL_TIMEOUT_MS = "stroom.stats.aggregation.pollTimeoutMs";

    public static final String GROUP_ID_PREFIX = "AggregationProcessor-";

    private final StatisticsService statisticsService;
    private final StroomPropertyService stroomPropertyService;
    private final StatisticType statisticType;
    private final EventStoreTimeIntervalEnum aggregationInterval;
    private final int instanceId;
    private final int maxEventIds;
    private final AtomicReference<String> consumerThreadName = new AtomicReference<>();

    private RunState runState = RunState.STOPPED;

    //used for thread synchronization
    private final Object startStopMonitor = new Object();

    private final KafkaConsumer<StatKey, StatAggregate> kafkaConsumer;
    private final KafkaProducer<StatKey, StatAggregate> kafkaProducer;
    private final String inputTopic;
    private final String groupId;
    private final Optional<EventStoreTimeIntervalEnum> optNextInterval;
    private final Optional<String> optNextIntervalTopic;
    private StatAggregator statAggregator;


    private Serde<StatKey> statKeySerde;
    private Serde<StatAggregate> statAggregateSerde;

    private enum RunState {
        STOPPED,
        STOPPING,
        RUNNING
    }

    public StatisticsAggregationProcessor(final StatisticsService statisticsService,
                                          final StroomPropertyService stroomPropertyService,
                                          final StatisticType statisticType,
                                          final EventStoreTimeIntervalEnum aggregationInterval,
                                          final KafkaProducer<StatKey, StatAggregate> kafkaProducer,
                                          final int instanceId) {

        this.statisticsService = statisticsService;
        this.stroomPropertyService = stroomPropertyService;
        this.statisticType = statisticType;
        this.aggregationInterval = aggregationInterval;
        this.instanceId = instanceId;
        this.kafkaProducer = kafkaProducer;

        LOGGER.info("Building aggregation processor for type {}, aggregationInterval {}, and instance id {}",
                statisticType, aggregationInterval, instanceId);

        maxEventIds = getMaxEventIds();
        statKeySerde = StatKeySerde.instance();
        statAggregateSerde = StatAggregateSerde.instance();

        String topicPrefix = stroomPropertyService.getPropertyOrThrow(
                StatisticsIngestService.PROP_KEY_STATISTIC_ROLLUP_PERMS_TOPIC_PREFIX);

        inputTopic = TopicNameFactory.getIntervalTopicName(topicPrefix, statisticType, aggregationInterval);
        groupId = GROUP_ID_PREFIX + inputTopic;
        optNextInterval = EventStoreTimeIntervalHelper.getNextBiggest(aggregationInterval);
        optNextIntervalTopic = optNextInterval.map(newInterval ->
                TopicNameFactory.getIntervalTopicName(topicPrefix, statisticType, newInterval));

        int maxEventIds = getMaxEventIds();
        long minBatchSize = getMinBatchSize();


        kafkaConsumer = buildConsumer();

        //start a processor for a stat type and aggregationInterval pair
        //This will improve aggregation as it will only handle data for the same stat types and aggregationInterval sizes
    }


    private KafkaConsumer<StatKey, StatAggregate> buildConsumer() {

        KafkaConsumer<StatKey, StatAggregate> kafkaConsumer = new KafkaConsumer<>(
                getConsumerProps(),
                statKeySerde.deserializer(),
                statAggregateSerde.deserializer());
        kafkaConsumer.subscribe(Collections.singletonList(inputTopic));

        return kafkaConsumer;
    }

    private Map<String, Object> getConsumerProps() {

        Map<String, Object> consumerProps = new HashMap<>();

        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                stroomPropertyService.getPropertyOrThrow(StatisticsIngestService.PROP_KEY_KAFKA_BOOTSTRAP_SERVERS));
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "Consumer-" + inputTopic);
        return consumerProps;
    }


    /**
     * Drain the aggregator and pass all aggregated events to the stat service to persist to the event store
     *
     * @param statisticType  The type of events being processed
     * @param statAggregator
     * @return The list of aggregates events drained from the aggregator and sent to the event store
     */
    private Map<StatKey, StatAggregate> flushToStatStore(final StatisticType statisticType, final StatAggregator statAggregator) {

        Map<StatKey, StatAggregate> aggregatedEvents = statAggregator.getAggregates();
        LOGGER.trace(() -> String.format("Flushing %s events of type %s, aggregationInterval %s to the StatisticsService",
                aggregatedEvents.size(), statisticType, statAggregator.getAggregationInterval()));

        statisticsService.putAggregatedEvents(statisticType, statAggregator.getAggregationInterval(), aggregatedEvents);
        return aggregatedEvents;
    }

    private void flushToTopic(final StatAggregator statAggregator,
                              final String topic,
                              final EventStoreTimeIntervalEnum newInterval,
                              final KafkaProducer<StatKey, StatAggregate> producer) {

        Preconditions.checkNotNull(statAggregator);
        Preconditions.checkNotNull(producer);

        LOGGER.trace(() -> String.format("Flushing %s records with new aggregationInterval %s to topic %s",
                statAggregator.size(), newInterval, topic));

        //Uplift the statkey to the new aggregationInterval and put it on the topic
        //We will not be trying to uplift the statKey if we are already at the highest aggregationInterval
        //so the RTE that cloneAndChangeInterval can throw should never happen
        statAggregator.getAggregates().entrySet().stream()
                .map(entry -> new ProducerRecord<>(
                        topic,
                        entry.getKey().cloneAndChangeInterval(newInterval),
                        entry.getValue()))
                .peek(producerRecord -> LOGGER.trace("Putting record {} on topic {}", producerRecord, topic))
                .forEach(producer::send);

        producer.flush();
    }


    private void startProcessor() {
        runState = RunState.RUNNING;

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

            //TODO need to share this between all aggregationInterval/statTypes as it is thread-safe and config should be consistent

            LOGGER.info("Starting consumer/producer for {}, {}, {} -> {}",
                    statisticType, aggregationInterval, inputTopic, optNextIntervalTopic.orElse("None"));

            consumerThreadName.set(Thread.currentThread().getName());

            final Instant lastCommitTime = Instant.now();

            try {
                while (runState.equals(RunState.RUNNING)) {
                    try {
                        ConsumerRecords<StatKey, StatAggregate> records = kafkaConsumer.poll(getPollTimeoutMs());

                        LOGGER.ifTraceIsEnabled(() -> {
                            int recCount = records.count();
                            if (recCount > 0) {
                                LOGGER.trace("Received {} records from topic {}", records.count(), inputTopic);
                            }
                        });

                        if (!records.isEmpty()) {
                            if (statAggregator == null) {
                                statAggregator = new StatAggregator(
                                        getMinBatchSize(),
                                        getMaxEventIds(),
                                        aggregationInterval,
                                        getFlushIntervalMs());
                            }
                            for (ConsumerRecord<StatKey, StatAggregate> record : records) {
                                statAggregator.add(record.key(), record.value());
                            }
                        }

                        boolean flushHappened = flushAggregatorIfReady();
                        if (flushHappened) {
                            kafkaConsumer.commitSync();
                        }
                    } catch (Exception e) {
                        LOGGER.error("Error while polling with stat type {}", statisticType, e);
                    }
                }
            } finally {
                //force a flush of anything in the aggregator
                flushAggregator(statAggregator);
                kafkaConsumer.commitSync();
                kafkaConsumer.close();
                runState = RunState.STOPPED;
            }
        });
    }

    private boolean flushAggregatorIfReady() {

        if (statAggregator != null && statAggregator.isReadyForFlush()) {
            if (statAggregator.isEmpty()) {
                //null the variable so we create a new one when we actually have records
                statAggregator = null;
                return false;
            } else {
                flushAggregator(statAggregator);
                return true;
            }
        }
        return false;
    }

    private void flushAggregator(StatAggregator statAggregator) {
        //flush all the aggregated stats down to the StatStore and onto the next biggest aggregationInterval topic
        //(if there is one) for coarser aggregation
        flushToStatStore(statisticType, statAggregator);

        optNextInterval.ifPresent(nextInterval ->
                flushToTopic(statAggregator, optNextIntervalTopic.get(), nextInterval, kafkaProducer));

    }


    @Override
    public void stop() {

        synchronized (startStopMonitor) {
            switch (runState) {
                case STOPPED:
                    LOGGER.info("Aggregation processor {} is already stopped", toString());
                    break;
                case STOPPING:
                    LOGGER.info("Aggregation processor {} is already stopping", toString());
                    break;
                case RUNNING:
                    LOGGER.info("Stopping Aggregation processor {}", toString());
                    runState = RunState.STOPPING;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected runState " + runState);
            }
        }
    }

    @Override
    public void start() {
        synchronized (startStopMonitor) {
            switch (runState) {
                case STOPPED:
                    LOGGER.info("Starting Aggregation processor {}", toString());
                    startProcessor();
                    break;
                case STOPPING:
                    throw new RuntimeException(String.format("Cannot start processor %s as it is currently stopping", toString()));
                case RUNNING:
                    LOGGER.info("Aggregation processor {} is already running", toString());
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected runState " + runState);
            }
        }
    }

    private int getPollTimeoutMs() {
        return stroomPropertyService.getIntProperty(PROP_KEY_AGGREGATOR_POLL_TIMEOUT_MS, 100);
    }

    private int getMinBatchSize() {
        return stroomPropertyService.getIntProperty(PROP_KEY_AGGREGATOR_MIN_BATCH_SIZE, 10_000);
    }

    private int getMaxEventIds() {
        return stroomPropertyService.getIntProperty(StatAggregate.PROP_KEY_MAX_AGGREGATED_EVENT_IDS, Integer.MAX_VALUE);
    }

    private int getFlushIntervalMs() {
        return stroomPropertyService.getIntProperty(PROP_KEY_AGGREGATOR_MAX_FLUSH_INTERVAL_MS, 60_000);
    }

    @Override
    public String toString() {
        return "StatisticsAggregationProcessor{" +
                "statisticType=" + statisticType +
                ", aggregationInterval=" + aggregationInterval +
                ", instanceId=" + instanceId +
                ", runState=" + runState +
                '}';
    }
}
