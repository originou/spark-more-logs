package org.apache.spark.metrics.sink

import com.codahale.metrics.{MetricFilter, MetricRegistry, RestApiReporter}
import org.apache.spark.SecurityManager

import java.util.Properties
import java.util.concurrent.TimeUnit

private[spark] class RestApiSink(override val property: Properties,
                                 override val registry: MetricRegistry,
                                 override val securityManager: SecurityManager)
  extends JsonSink(property, registry, securityManager) {

  override lazy val reporter: RestApiReporter = RestApiReporter.forRegistry()
    .registry(registry)
    .jobId(jobId)
    .name("json-sink")
    .filter(MetricFilter.ALL)
    .durationUnit(TimeUnit.SECONDS)
    .rateUnit(TimeUnit.MILLISECONDS)
    .apiUrl(apiUrl)
    .build()

  val KEY_API_URL = "apiUrl"

  val apiUrl: String = Option(property.getProperty(KEY_API_URL)) match {
    case Some(s) => s
    case None => throw new Exception("RestApiSink No Api URL is defined on spark configuration: key: " + KEY_API_URL)
  }

}
