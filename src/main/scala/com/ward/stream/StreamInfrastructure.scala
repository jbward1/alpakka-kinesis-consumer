package com.ward.stream

import software.amazon.awssdk.services.dynamodb.model.{
  AttributeDefinition,
  CreateTableRequest,
  CreateTableResponse,
  KeySchemaElement,
  KeyType,
  ProvisionedThroughput,
  ScalarAttributeType
}
import software.amazon.awssdk.services.kinesis.model.{CreateStreamRequest, CreateStreamResponse}

import scala.concurrent.Future
import scala.jdk.FutureConverters._
import scala.concurrent.ExecutionContext.Implicits.global

object StreamInfrastructure {
  val testStream       = "testing-stream"
  val testStreamShards = 1
  val rcu = 5
  val wcu = 5

  def init(
      kinesisStreamClients: KinesisStreamClients
  ): Future[(CreateStreamResponse, CreateTableResponse)] = {
    val stream     = createStream(kinesisStreamClients)
    val leaseTable = createLeaseTable(kinesisStreamClients)

    for {
      leaseResponse <- leaseTable
      streamRes     <- stream
    } yield (streamRes, leaseResponse)
  }

  private def createLeaseTable(
      kinesisStreamClients: KinesisStreamClients
  ): Future[CreateTableResponse] = {
    val LeaseKey = "leaseKey"
    val attributeDefinition = AttributeDefinition
      .builder()
      .attributeName(LeaseKey)
      .attributeType(ScalarAttributeType.S)
      .build()
    val keySchemaElement =
      KeySchemaElement.builder().attributeName(LeaseKey).keyType(KeyType.HASH).build()

    val throughput =
      ProvisionedThroughput.builder().readCapacityUnits(rcu).writeCapacityUnits(wcu).build()

    val createTableRequest: CreateTableRequest =
      CreateTableRequest
        .builder()
        .tableName(testStream)
        .attributeDefinitions(attributeDefinition)
        .keySchema(keySchemaElement)
        .provisionedThroughput(throughput)
        .build()

    kinesisStreamClients.dynamoClient.createTable(createTableRequest).asScala
  }

  private def createStream(
      kinesisStreamClients: KinesisStreamClients
  ): Future[CreateStreamResponse] = {
    val createStreamRequest =
      CreateStreamRequest.builder().streamName(testStream).shardCount(testStreamShards).build()

    kinesisStreamClients.kinesisClient.createStream(createStreamRequest).asScala
  }
}
