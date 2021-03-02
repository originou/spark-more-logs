package org.apache.spark.metrics.sink

import com.codahale.metrics.MetricRegistry
import org.apache.spark.SecurityManager

import java.util.Properties

private[spark] class JsonSink(val property: Properties,
                              val registry: MetricRegistry,
                              val securityManager: SecurityManager) extends Sink {
  def start(): Unit = {
    println("START")
  }

  def stop(): Unit = {
    println("STOP")
  }

  def report(): Unit = {
    println("REPORT")
  }
}
