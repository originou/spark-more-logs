import org.apache.spark.sql.execution.streaming.MemoryStream
import org.apache.spark.sql.streaming.{DataStreamWriter, Trigger}
import org.apache.spark.sql.{Row, SparkSession}

/**
 * TODO: move me on scala test
 */
object ExampleApp {

  def main(args: Array[String]): Unit = {
    val spark: SparkSession = SparkSession.builder().appName("Random Example")
      .master("local[4]")
      .config("spark.metrics.conf.*.sink.restapi.class", "org.apache.spark.metrics.sink.RestApiSink")
      .config("spark.metrics.conf.*.sink.restapi.apiUrl", "http://localhost:8080/spark/")
//      .config("spark.metrics.conf.*.sink.json.class", "org.apache.spark.metrics.sink.JsonSink")
      .getOrCreate()

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

  }
}
