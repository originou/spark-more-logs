package io.dma.spark

import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.streaming.{DataStreamWriter, Trigger}

trait SparkTest {

  val spark: SparkSession

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