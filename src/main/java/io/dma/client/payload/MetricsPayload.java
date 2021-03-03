package io.dma.client.payload;

import java.util.SortedMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.PayloadReporter;
import com.codahale.metrics.Timer;

import lombok.Builder;
import lombok.Data;

/**
 * DTO of metrics reported by {@link PayloadReporter} to be serialize as json string.
 * 
 */
@Data
@Builder
public class MetricsPayload
{
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
