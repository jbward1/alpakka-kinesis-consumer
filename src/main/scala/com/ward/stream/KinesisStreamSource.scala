package com.ward.stream

import akka.actor.ActorSystem
import akka.stream.alpakka.kinesis.scaladsl.KinesisSchedulerSource
import akka.stream.alpakka.kinesis.{CommittableRecord, KinesisSchedulerSourceSettings}
import akka.stream.scaladsl.Source
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient
import software.amazon.kinesis.common.ConfigsBuilder
import software.amazon.kinesis.coordinator.Scheduler
import software.amazon.kinesis.processor.ShardRecordProcessorFactory

import java.net.URI
import java.util.UUID
import scala.concurrent.Future

object KinesisStreamSource {
  private def builder(
      streamName: String,
      KinesisStreamClients: KinesisStreamClients
  )(recordProcessorFactory: ShardRecordProcessorFactory): Scheduler = {
    import KinesisStreamClients._

    val configsBuilder =
      new ConfigsBuilder(
        streamName,
        streamName,
        kinesisClient,
        dynamoClient,
        cloudWatchClient,
        s"${
          import scala.sys.process._
          "hostname".!!.trim()
        }:${UUID.randomUUID()}",
        recordProcessorFactory
      )

    new Scheduler(
      configsBuilder.checkpointConfig,
      configsBuilder.coordinatorConfig,
      configsBuilder.leaseManagementConfig,
      configsBuilder.lifecycleConfig,
      configsBuilder.metricsConfig,
      configsBuilder.processorConfig,
      configsBuilder.retrievalConfig
    )
  }

  def apply(
      kinesisStream: String,
      kinesisClients: KinesisStreamClients
  )(implicit actorSystem: ActorSystem): Source[CommittableRecord, Future[Scheduler]] =
    KinesisSchedulerSource(
      builder(kinesisStream, kinesisClients),
      KinesisSchedulerSourceSettings.defaults
    )
}
