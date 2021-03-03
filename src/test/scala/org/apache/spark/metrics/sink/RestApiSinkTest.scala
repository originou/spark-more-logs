package org.apache.spark.metrics.sink

import io.dma.spark.SparkTest
import org.apache.spark.sql.SparkSession
import org.scalatest.Ignore


class RestApiSinkTest extends org.scalatest.funsuite.AnyFunSuite {


  @Ignore // find a way to mock a server with scala
  test("should test something else") {

    new SparkTest {
      val spark: SparkSession = SparkSession.builder()
        .appName("Test")
        .master("local[*]")
        .config("spark.metrics.conf.*.sink.rest.class", "org.apache.spark.metrics.sink.RestApiSink")
        .config("spark.metrics.conf.*.sink.rest.apiUrl", "http://localhost:8080/spark/")
        .config("spark.metrics.conf.*.sink.rest.appName", "Test")
        .getOrCreate()

      init()

    }
  }

}
