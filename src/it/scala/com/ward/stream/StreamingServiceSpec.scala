package com.ward.stream

import akka.stream.scaladsl.Sink
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest

import scala.jdk.FutureConverters._

class StreamingServiceSpec extends AsyncFlatSpec with Matchers with KCLTest {
  protected val testStream: String   = StreamInfrastructure.testStream
  protected val testStreamShards     = 1
  protected val partitionKey: String = "foobar"
  private val testRecord             = SdkBytes.fromUtf8String("""{"id": "1"}""")

  private def putKinesisRecord = {
    val putRecordRequest =
      PutRecordRequest
        .builder()
        .streamName(testStream)
        .partitionKey(partitionKey)
        .data(testRecord)
        .build()

    kinesisClient.putRecord(putRecordRequest).asScala
  }

  "kinesis consumer" should "successfully consume message from a kinesis stream" in {
    (for {
      _           <- putKinesisRecord
      firstRecord <- KinesisStreamSource(testStream, kinesisStreamClients).runWith(Sink.head)
    } yield {
      firstRecord.record.partitionKey() shouldEqual partitionKey
    }).recover { e: Throwable =>
      fail("something broke", e)
    }
  }
}
