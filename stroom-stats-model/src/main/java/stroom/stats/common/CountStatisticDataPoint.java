

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

package stroom.stats.common;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import stroom.dashboard.expression.v1.Val;
import stroom.dashboard.expression.v1.ValLong;
import stroom.stats.api.StatisticTag;
import stroom.stats.api.StatisticType;
import stroom.stats.configuration.StatisticConfiguration;
import stroom.stats.shared.EventStoreTimeIntervalEnum;

import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * Value object to hold a statistic data point for a COUNT statistic as
 * retrieved from a statistic store.
 * This represents an aggregated value of 1-many statistic events
 */
public class CountStatisticDataPoint implements StatisticDataPoint {

    private static final StatisticType STATISTIC_TYPE = StatisticType.COUNT;

    private final BasicStatisticDataPoint delegate;
    private final long count;

    private static final Map<String, Function<CountStatisticDataPoint, Val>> FIELD_VALUE_FUNCTION_MAP;

    static {
        //hold a map of field names to functions that we get a value for that named field, converted to a string
        FIELD_VALUE_FUNCTION_MAP = ImmutableMap.<String, Function<CountStatisticDataPoint, Val>>builder()
                .put(StatisticConfiguration.FIELD_NAME_COUNT, dataPoint ->
                        ValLong.create(dataPoint.getCount()))
                .build();
    }

    public CountStatisticDataPoint(final StatisticConfiguration statisticConfiguration,
                                   final EventStoreTimeIntervalEnum precision,
                                   final long timeMs,
                                   final List<StatisticTag> tags,
                                   final Long count) {

        Preconditions.checkArgument(StatisticType.COUNT.equals(statisticConfiguration.getStatisticType()));

        this.delegate = new BasicStatisticDataPoint(statisticConfiguration, precision, timeMs, tags);
        this.count = count;
    }

    @Override
    public StatisticConfiguration getStatisticConfiguration() {
        return delegate.getStatisticConfiguration();
    }

    @Override
    public EventStoreTimeIntervalEnum getTimeInterval() {
        return delegate.getTimeInterval();
    }

    @Override
    public long getTimeMs() {
        return delegate.getTimeMs();
    }

    @Override
    public List<StatisticTag> getTags() {
        return delegate.getTags();
    }

    @Override
    public Map<String, String> getTagsAsMap() {
        return delegate.getTagsAsMap();
    }

    public long getCount() {
        return count;
    }

    @Override
    public StatisticType getStatisticType() {
        return STATISTIC_TYPE;
    }

    @Override
    public Val getFieldValue(final String fieldName) {
        Function<CountStatisticDataPoint, Val> fieldValueFunction = FIELD_VALUE_FUNCTION_MAP.get(fieldName);

        if (fieldValueFunction == null) {
            //we don't know what it is so see if the delegate does
            return delegate.getFieldValue(fieldName);
        } else {
            return fieldValueFunction.apply(this);
        }
    }

    @Override
    public String toString() {
        return "CountStatisticDataPoint{" +
                "delegate=" + delegate +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final CountStatisticDataPoint that = (CountStatisticDataPoint) o;

        if (count != that.count) return false;
        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + (int) (count ^ (count >>> 32));
        return result;
    }
}
