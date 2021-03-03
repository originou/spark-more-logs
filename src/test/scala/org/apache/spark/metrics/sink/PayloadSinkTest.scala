package org.apache.spark.metrics.sink

import com.codahale.metrics._
import io.dma.client.payload.MetricsPayload
import io.dma.spark.SparkTest
import org.apache.spark.sql.SparkSession
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{atLeastOnce, mock, spy, verify}

import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit

class PayloadSinkTest extends org.scalatest.funsuite.AnyFunSuite {

  test("should test something") {

    new SparkTest {
      val spark: SparkSession = SparkSession.builder()
        .appName("Test")
        .master("local[*]")
        .config("spark.metrics.conf.*.sink.json.class", "org.apache.spark.metrics.sink.TestPayloadSink")
        .getOrCreate()

      init()

      val ac: ArgumentCaptor[MetricsPayload] = ArgumentCaptor.forClass(classOf[MetricsPayload])
      val captor: Captor = TestPayloadSink.captor
      verify(captor, atLeastOnce()).capture(ac.capture())

      import scala.collection.JavaConversions._

      val results: Seq[MetricsPayload] = ac.getAllValues


      results.foreach(dto => {
        assert(dto.isInstanceOf[MetricsPayload])
        assert(dto.getInstance() equals "driver")
        // Should check keys
      })

    }
  }
}

trait Captor {
  def capture(any: Object)
}

trait MockCaptor {
  val captor: Captor
}

class MockPayloadReporter(registry: MetricRegistry, name: String, jobId: String, captor: Captor)
  extends PayloadReporter(registry, name, MetricFilter.ALL, TimeUnit.MILLISECONDS, TimeUnit.SECONDS, Clock
    .defaultClock(), jobId) {
  def report(gauges: util.SortedMap[String, Gauge[_]],
             counters: util.SortedMap[String, Counter],
             histograms: util.SortedMap[String, Histogram],
             meters: util.SortedMap[String, Meter],
             timers: util.SortedMap[String, Timer]): Unit = {

    captor.capture(reportAsDto(gauges, counters, histograms, meters, timers))
  }
}

class TestPayloadSink(override val property: Properties,
                      override val registry: MetricRegistry,
                      override val securityManager: org.apache.spark.SecurityManager)
  extends PayloadSink(property, registry, securityManager) with MockCaptor {
  override lazy val reporter: PayloadReporter = spy(new MockPayloadReporter(registry, "test-reporter", jobId, captor))
  override val captor: Captor = TestPayloadSink.captor

}

object TestPayloadSink {
  val captor: Captor = mock(classOf[Captor])
}

