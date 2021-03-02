package com.codahale.metrics;

import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestApiReporter extends JsonReporter {
  @Nullable
  private final HttpClient httpClient;

  private final String apiUrl;

  /**
   * Creates a new {@link ScheduledReporter} instance.
   *
   * @param registry the {@link MetricRegistry} containing the metrics this reporter will report
   * @param name the reporter's name
   * @param filter the filter for which metrics to report
   * @param rateUnit a unit of time
   * @param durationUnit a unit of time
   * @param clock clock can be easyly mockable for T.U
   * @param jobId job spark ID
   */
  @Builder(builderMethodName = "forRegistry")
  protected RestApiReporter(MetricRegistry registry, String name, MetricFilter filter,
      TimeUnit rateUnit, TimeUnit durationUnit, @Nullable Clock clock, String jobId,
      @Nullable HttpClient httpClient, String apiUrl) {
    super(registry, name, filter, rateUnit, durationUnit, clock, jobId);
    this.httpClient = getOrCreateDefaultHttpClient(httpClient);
    this.apiUrl = apiUrl;
  }

  /**
   * Called periodically by the polling thread. Subclasses should report all the given metrics.
   *
   * @param gauges all of the gauges in the registry
   * @param counters all of the counters in the registry
   * @param histograms all of the histograms in the registry
   * @param meters all of the meters in the registry
   * @param timers all of the timers in the registry
   */
  @Override
  public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
      SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
      SortedMap<String, Timer> timers) {

    try {
      String jsonAsString = reportAsJson(gauges, counters, histograms, meters, timers);
      StringEntity stringEntity = new StringEntity(jsonAsString);
      HttpPost httpPost = new HttpPost(apiUrl);
      httpPost.setEntity(stringEntity);

      HttpResponse response = httpClient.execute(httpPost);

      int statusCode = response.getStatusLine().getStatusCode();
      EntityUtils.consume(response.getEntity());

      if (statusCode != 200) {
        throw new RuntimeException(
            "Problem during execute request api url with status error: " + statusCode);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private HttpClient getOrCreateDefaultHttpClient(HttpClient httpClient) {
    return httpClient != null ? httpClient : new DefaultHttpClient();
  }
}
