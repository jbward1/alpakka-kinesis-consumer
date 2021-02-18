package com.ward.stream

import akka.actor.ActorSystem
import com.github.matsluni.akkahttpspi.AkkaHttpClient
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import scala.concurrent.duration._

import scala.concurrent.Await
import scala.util.Try

trait KCLTest extends BeforeAndAfterAll { this: Suite =>
  implicit val system: ActorSystem = ActorSystem("KCLTest")

  private val LocalStackImageVersion = "localstack/localstack:0.12.6"
  private val localStackImage        = DockerImageName.parse(LocalStackImageVersion)
  private val akkaHttpClient         = AkkaHttpClient.builder().withActorSystem(system).build()

  private val localstack = new LocalStackContainer(localStackImage).withServices(
    Service.KINESIS,
    Service.DYNAMODB,
    Service.SNS
  )

  localstack.start()

  override def beforeAll(): Unit = {
    Try(Await.result(StreamInfrastructure.init(kinesisStreamClients), 5.seconds))
      .map(x => {
        println(s"Kinesis stream status: ${x._1.sdkHttpResponse().statusText().get()}")
        println(s"Dynamodb lease table status: ${x._2.sdkHttpResponse().statusText().get()}")
        println("Stream infrastructure created!")
      })
      .recover { case e: Throwable =>
        this.fail("failed to init stream infrastructure", e)
      }
  }

  protected lazy val kinesisStreamClients: KinesisStreamClients =
    KinesisStreamClients(kinesisClient, dynamoClient, cloudWatchClient)

  private lazy val region = Region.of(localstack.getRegion)
  private lazy val awsCredentials =
    StaticCredentialsProvider.create(
      AwsBasicCredentials.create(
        localstack.getAccessKey,
        localstack.getSecretKey
      )
    )

  protected lazy val kinesisClient: KinesisAsyncClient =
    KinesisAsyncClient
      .builder()
      .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.KINESIS))
      .region(region)
      .credentialsProvider(awsCredentials)
      .httpClient(akkaHttpClient)
      .build()

  protected lazy val dynamoClient: DynamoDbAsyncClient =
    DynamoDbAsyncClient
      .builder()
      .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.DYNAMODB))
      .region(region)
      .credentialsProvider(awsCredentials)
      .httpClient(akkaHttpClient)
      .build()

  protected lazy val cloudWatchClient: CloudWatchAsyncClient =
    CloudWatchAsyncClient
      .builder()
      .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.CLOUDWATCH))
      .region(region)
      .credentialsProvider(awsCredentials)
      .httpClient(akkaHttpClient)
      .build()
}
