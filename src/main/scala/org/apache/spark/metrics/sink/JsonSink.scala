package org.apache.spark.metrics.sink

import com.codahale.metrics.{JsonReporter, MetricRegistry}
import org.apache.spark.SecurityManager
import org.apache.spark.metrics.MetricsSystem
import org.slf4j.{Logger, LoggerFactory}

import java.util.concurrent.TimeUnit
import java.util.{Locale, Properties}

private[spark] abstract class JsonSink(val property: Properties,
                                       val registry: MetricRegistry,
                                       val securityManager: SecurityManager) extends Sink {

  lazy val reporter: JsonReporter = {
    // TO BE IMPLEMENT by concrete class
    ???
  }
  val log: Logger = LoggerFactory.getLogger(getClass)
  val KEY_PERIOD = "period"
  val KEY_UNIT = "unit"
  val DEFAULT_PERIOD = 10
  val DEFAULT_UNIT = "SECONDS"
  val pollPeriod: Int = Option(property.getProperty(KEY_PERIOD)) match {
    case Some(s) => s.toInt
    case None => DEFAULT_PERIOD
  }
  val pollUnit: TimeUnit = Option(property.getProperty(KEY_UNIT)) match {
    case Some(s) => TimeUnit.valueOf(s.toUpperCase(Locale.ROOT))
    case None => TimeUnit.valueOf(DEFAULT_UNIT)
  }

  MetricsSystem.checkMinimalPollingPeriod(pollUnit, pollPeriod)
  val jobId: String = {
    val name = registry.getNames.first()
    name.substring(0, name.indexOf("."))
  }

  def start(): Unit = {
    log.debug("JsonReporter Start")
    reporter.start(pollPeriod, pollUnit)
  }

  def stop(): Unit = {
    log.debug("JsonReporter Stop")
    reporter.stop()
  }

  def report(): Unit = {
    log.debug("JsonReporter Reporter")
    reporter.report()
  }
}
