package io.dma.client

import io.dma.client.payload.{EventType, StreamingPayload}
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.codehaus.jackson.map.ObjectMapper

import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.annotation.tailrec
import scala.collection.JavaConverters.seqAsJavaList
import scala.collection.mutable
import scala.concurrent.duration.{FiniteDuration, _}

case class HttpStreamingClient[T](httpClient: HttpClient = new DefaultHttpClient(),
                                  appName: String,
                                  jobId: String,
                                  eventType: EventType,
                                  apiUrl: String,
                                  maxQueueSize: Int = 50,
                                  pollingIntervalInSec: FiniteDuration = 5.seconds,
                                  mapper: ObjectMapper = new ObjectMapper()
                                 ) extends StreamingClient[T] {

  private val pendingEvents: mutable.Queue[T] = mutable.Queue[T]()


  private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
  private val publishTask: Runnable = () => publishIfPendingOnQueue()

  def publish(payloads: Seq[T]): Unit = {
    val streamingPayload = new StreamingPayload(appName, jobId, eventType, seqAsJavaList(payloads))
    val asString = mapper.writeValueAsString(streamingPayload)
    val httpPost: HttpPost = new HttpPost(apiUrl)
    httpPost.setEntity(new StringEntity(asString))

    val response: HttpResponse = httpClient.execute(httpPost)

    val statusCode = response.getStatusLine.getStatusCode
    EntityUtils.consume(response.getEntity)

    if (statusCode != 200) {
      throw new RuntimeException(
        "Problem during execute request api url with status error: " + statusCode);
    }
  }

  def publishToQueue(payload: T): Unit = pendingEvents.enqueue(payload)

  def start(): Unit = scheduler.scheduleAtFixedRate(publishTask,
    pollingIntervalInSec.toSeconds,
    pollingIntervalInSec.toSeconds,
    TimeUnit.SECONDS)

  def stop(): Unit = {
    scheduler.shutdown()
    publishIfPendingOnQueue()
  }

  @tailrec
  private def publishIfPendingOnQueue(): Unit = pendingEvents.synchronized(pendingEvents.size) match {
    case 0 =>
    case nbPending => {
      val messages: Seq[T] = pendingEvents.synchronized(
        for (_ <- 0 until math.min(nbPending, maxQueueSize)) yield pendingEvents.dequeue()
      )
      publish(messages)
      publishIfPendingOnQueue()
    }
  }

}
