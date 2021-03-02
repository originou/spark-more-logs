package org.apache.spark.metrics.sink

import com.codahale.metrics._
import org.apache.spark.sql.execution.streaming.CommitMetadata.format
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.streaming.{DataStreamWriter, Trigger}
import org.apache.spark.sql.{Row, SparkSession}
import org.json4s.jackson._
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{atLeastOnce, mock, spy, verify}

import java.util
import java.util.Properties
import java.util.concurrent.TimeUnit

class JsonSinkTest extends org.scalatest.funsuite.AnyFunSuite {


  test("should test something") {

    new Spark {
      init()

      val ac: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val captor: Captor = TestJsonSink.captor
      verify(captor, atLeastOnce()).capture(ac.capture())


      import scala.collection.JavaConversions._

      val results: Seq[JsonMockDto] = ac.getAllValues.map {
        parseJson(_).extract[JsonMockDto]
      }


      results.foreach(dto => {
        assert(dto.isInstanceOf[JsonMockDto])
        assert(dto.instance equals "driver")
        // Should check keys
      })

    }
  }

  trait Spark {

    val spark: SparkSession = SparkSession.builder()
      .appName("Test")
      .master("local[*]")
      .config("spark.metrics.conf.*.sink.json.class", "org.apache.spark.metrics.sink.TestJsonSink")
      //      .config("spark.metrics.conf.*.sink.restapi.apiUrl", "http://localhost:8080/spark/")
      .getOrCreate()

    def init(): Unit = {

      import spark.implicits._
      val inputStream = new MemoryStream[(Int, String)](1, spark.sqlContext)
      inputStream.addData((1, "abc"), (2, "def"), (3, "ghi"), (4, "jkf"))

      val writeQuery: DataStreamWriter[Row] = inputStream.toDS()
        .toDF("nr", "letter")
        .writeStream
        .trigger(Trigger.Once())
        .format("json")
        .partitionBy("nr")
        .option("path", "./output")
        .option("checkpointLocation", "/tmp/file_sink_checkpoint")
      writeQuery.start().awaitTermination()

      spark.stop()
    }
  }


}

trait Captor {
  def capture(any: Object)
}

trait MockCaptor {
  val captor: Captor
}

class MockJsonReporter(registry: MetricRegistry, name: String, jobId: String, captor: Captor)
  extends JsonReporter(registry, name, MetricFilter.ALL, TimeUnit.MILLISECONDS, TimeUnit.SECONDS, Clock
    .defaultClock(), jobId) {
  def report(gauges: util.SortedMap[String, Gauge[_]],
             counters: util.SortedMap[String, Counter],
             histograms: util.SortedMap[String, Histogram],
             meters: util.SortedMap[String, Meter],
             timers: util.SortedMap[String, Timer]): Unit = {

    captor.capture(reportAsJson(gauges, counters, histograms, meters, timers))
  }
}

class TestJsonSink(override val property: Properties,
                   override val registry: MetricRegistry,
                   override val securityManager: org.apache.spark.SecurityManager)
  extends JsonSink(property, registry, securityManager) with MockCaptor {
  override lazy val reporter: JsonReporter = spy(new MockJsonReporter(registry, "test-reporter", jobId, captor))
  override val captor: Captor = TestJsonSink.captor

}

object TestJsonSink {
  val captor: Captor = mock(classOf[Captor])
}

case class JsonMockDto(jobId: String,
                       instance: String,
                       generated_at: Long,
                       gauges: Map[String, Object],
                       counters: Map[String, Object],
                       histograms: Map[String, Object],
                       meters: Map[String, Object],
                       timers: Map[String, Object],
                      ) {
}

