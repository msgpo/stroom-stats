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
import stroom.stats.shared.EventStoreTimeIntervalEnum;
import stroom.stats.streams.aggregation.StatAggregate;
import stroom.stats.util.logging.LambdaLogger;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@NotThreadSafe
class StatAggregator {

    private static final LambdaLogger LOGGER = LambdaLogger.getLogger(StatAggregator.class);

    private Map<StatKey, StatAggregate> buffer;
    private final int maxEventIds;
    private final EventStoreTimeIntervalEnum aggregationInterval;

    private Supplier<Map<StatKey, StatAggregate>> bufferSupplier;

    public StatAggregator(final int expectedSize, final int maxEventIds, final EventStoreTimeIntervalEnum aggregationInterval) {
        //initial size to avoid it rehashing
        this.bufferSupplier = () -> new HashMap<>((int)Math.ceil(expectedSize / 0.75));
        this.buffer = bufferSupplier.get();
        this.maxEventIds = maxEventIds;
        this.aggregationInterval = aggregationInterval;
    }

    /**
     * Add a single key/aggregate pair into the aggregator. The aggregate will be aggregated
     * with any existing aggregates for that {@link StatKey}
     */
    public void add(final StatKey statKey, final StatAggregate statAggregate){

        Preconditions.checkNotNull(statKey);
        Preconditions.checkNotNull(statAggregate);
        Preconditions.checkArgument(statKey.getInterval().equals(aggregationInterval),
                "statKey %s doesn't match aggregator interval %s", statKey, aggregationInterval);

        LOGGER.trace("Adding statKey {} and statAggregate {} to aggregator {}", statKey, statAggregate, aggregationInterval);

        //The passed StatKey will already have its time truncated to the interval of this aggregator
        //so we don't need to do anything to it.

        //aggregate the passed aggregate and key into the existing aggregates
        buffer.merge(
                statKey,
                statAggregate,
                (existingAgg, newAgg) -> existingAgg.aggregate(newAgg, maxEventIds));
    }

    public int size() {
        return buffer.size();
    }

    public EventStoreTimeIntervalEnum getAggregationInterval() {
        return aggregationInterval;
    }

    public Map<StatKey, StatAggregate> drain() {
        LOGGER.trace(() -> String.format("drain called, return %s events", buffer.size()));

        //Grab the reference to the map and point buffer at a new map ready for new data
        Map<StatKey, StatAggregate> aggregatedEvents = buffer;
        buffer = bufferSupplier.get();
        return aggregatedEvents;
    }
}
