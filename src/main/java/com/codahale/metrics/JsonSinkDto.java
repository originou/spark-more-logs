package com.codahale.metrics;

import java.util.SortedMap;

import lombok.Builder;
import lombok.Data;

/**
 * DTO of metrics reported by {@link JsonReporter} to be serialize as json string.
 * 
 */
@Data
@Builder
public class JsonSinkDto {
  /**
   * Job Spark ID
   */
  private String jobId;

  /**
   * Instance: "driver", "executor"...
   */
  private String instance;

  private long generated_at;

  private SortedMap<String, Gauge> gauges;
  private SortedMap<String, Counter> counters;
  private SortedMap<String, Histogram> histograms;
  private SortedMap<String, Meter> meters;
  private SortedMap<String, Timer> timers;
}
