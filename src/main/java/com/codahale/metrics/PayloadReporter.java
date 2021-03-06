package com.codahale.metrics;

import java.util.Collection;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;

import io.dma.client.payload.MetricsPayload;

public abstract class PayloadReporter extends ScheduledReporter {
  private final Clock clock;

  private final String jobId;

  /**
   * Creates a new {@link ScheduledReporter} instance.
   *
   * @param registry the {@link MetricRegistry} containing the metrics this reporter will report
   * @param name the reporter's name
   * @param filter the filter for which metrics to report
   * @param rateUnit a unit of time
   * @param durationUnit a unit of time
   */
  protected PayloadReporter(final MetricRegistry registry, final String name,
      final MetricFilter filter, final TimeUnit rateUnit, final TimeUnit durationUnit,
      @Nullable final Clock clock, final String jobId) {
    super(registry, name, filter, rateUnit, durationUnit);
    this.clock = getOrCreateDefaultClock(clock);
    this.jobId = jobId;
  }

  protected MetricsPayload reportAsDto(SortedMap<String, Gauge> gauges,
      SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms,
      SortedMap<String, Meter> meters, SortedMap<String, Timer> timers)
      throws JsonProcessingException {

    final long timestamp = TimeUnit.MILLISECONDS.toSeconds(clock.getTime());


    Pair<String, Integer> instancePair = getInstanceNameAndIndexOffset(
        ImmutableList.of(gauges, counters, histograms, meters, timers));

    String instance = instancePair.getLeft();
    int offset = instancePair.getRight() + 1;

    SortedMap<String, Gauge> _gauges = truncateKey(gauges, offset);
    SortedMap<String, Counter> _counters = truncateKey(counters, offset);
    SortedMap<String, Histogram> _histograms = truncateKey(histograms, offset);
    SortedMap<String, Meter> _meters = truncateKey(meters, offset);
    SortedMap<String, Timer> _timers = truncateKey(timers, offset);

    return MetricsPayload.builder()
        .jobId(jobId)
        .instance(instance)
        .generated_at(timestamp)
        .gauges(_gauges)
        .counters(_counters)
        .histograms(_histograms)
        .meters(_meters)
        .timers(_timers)
        .build();

  }

  private <T extends Metric> SortedMap<String, T> truncateKey(SortedMap<String, T> metrics,
      int offset) {

    SortedMap<String, T> newMap = new TreeMap<>();

    metrics.forEach((key, value) -> newMap.put(key.substring(offset), value));
    return newMap;
  }

  private Pair<String, Integer> getInstanceNameAndIndexOffset(
      Collection<SortedMap<String, ? extends Metric>> collection) {
    Optional<Pair<String, Integer>> instanceName =
        collection.stream().filter(map -> map.size() > 0).map(SortedMap::firstKey).map(key -> {
          int index1 = key.indexOf(".") + 1;
          int index2 = key.indexOf(".", index1);
          return Pair.of(key.substring(index1, index2), index2);
        }).findFirst();
    if (instanceName.isPresent()) {
      return instanceName.get();
    } else {
      throw new RuntimeException("Can't find instance name");
    }
  }

  private Clock getOrCreateDefaultClock(@Nullable Clock clock) {
    return clock != null ? clock : Clock.defaultClock();
  }
}
