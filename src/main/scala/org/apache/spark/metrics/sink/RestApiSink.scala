package org.apache.spark.metrics.sink

import com.codahale.metrics.{MetricFilter, MetricRegistry, RestApiReporter}
import io.dma.client.HttpStreamingClient
import io.dma.client.payload.{EventType, MetricsPayload}
import org.apache.spark.SecurityManager

import java.util.Properties
import java.util.concurrent.TimeUnit

private[spark] class RestApiSink(override val property: Properties,
                                 override val registry: MetricRegistry,
                                 override val securityManager: SecurityManager)
  extends PayloadSink(property, registry, securityManager) {

  override lazy val reporter: RestApiReporter = RestApiReporter.forRegistry()
    .registry(registry)
    .jobId(jobId)
    .name("json-sink")
    .filter(MetricFilter.ALL)
    .durationUnit(TimeUnit.SECONDS)
    .rateUnit(TimeUnit.MILLISECONDS)
    .streamingClient(streamingClient)
    .build()

  val KEY_APP_NAME = "appName"
  val KEY_API_URL = "apiUrl"

  val appName: String = Option(property.getProperty(KEY_APP_NAME)) match {
    case Some(s) => s
    case None => throw new Exception("RestApiSink No App Name is defined on spark configuration: key: " + KEY_APP_NAME)
  }

  val apiUrl: String = Option(property.getProperty(KEY_API_URL)) match {
    case Some(s) => s
    case None => throw new Exception("RestApiSink No Api URL is defined on spark configuration: key: " + KEY_API_URL)
  }

  val streamingClient: HttpStreamingClient[MetricsPayload] = {
    HttpStreamingClient[MetricsPayload](
      apiUrl = apiUrl,
      appName = appName,
      jobId = jobId,
      eventType = EventType.SPARK_SINK
    )
  }

  override def start(): Unit = {
    super.start()
    streamingClient.start()
  }

  override def stop(): Unit = {
    super.stop()
    streamingClient.stop()
  }
}
