package com.ward.stream

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import com.typesafe.scalalogging.LazyLogging
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global

object Server extends App with LazyLogging {
  implicit val system: ActorSystem = ActorSystem("KCLConsumerServer")

  private val localStackDocker = URI.create(Config.localStackHostname)
  private val akkaHttpClient   = AkkaHttpClient.builder().withActorSystem(system).build()

  private lazy val awsCredentials =
    StaticCredentialsProvider.create(
      AwsBasicCredentials.create(
        "foobar",
        "foobar",
      )
    )

  private val kinesisClient: KinesisAsyncClient =
    KinesisAsyncClient
      .builder()
      .endpointOverride(localStackDocker)
      .credentialsProvider(awsCredentials)
      .region(Config.region)
      .httpClient(akkaHttpClient)
      .build()

  private val dynamoClient: DynamoDbAsyncClient =
    DynamoDbAsyncClient
      .builder()
      .endpointOverride(localStackDocker)
      .credentialsProvider(awsCredentials)
      .region(Config.region)
      .httpClient(akkaHttpClient)
      .build()

  private val cloudWatchClient: CloudWatchAsyncClient =
    CloudWatchAsyncClient
      .builder()
      .endpointOverride(localStackDocker)
      .credentialsProvider(awsCredentials)
      .region(Config.region)
      .httpClient(akkaHttpClient)
      .build()

  private val kinesisStreamClients =
    KinesisStreamClients(kinesisClient, dynamoClient, cloudWatchClient)

  logger.info("Server started")

  (for {
    _ <- StreamInfrastructure.init(kinesisStreamClients)
    _ = logger.info("Lease table and stream created!")
    committableRecord <- KinesisStreamSource(StreamInfrastructure.testStream, kinesisStreamClients)
      .runWith(Sink.last)
  } yield {
    logger.info(s"Message received from kinesis source! ${committableRecord.sequenceNumber}")
  }).recover { case e: Throwable =>
    logger.error("error", e)
  }
}
