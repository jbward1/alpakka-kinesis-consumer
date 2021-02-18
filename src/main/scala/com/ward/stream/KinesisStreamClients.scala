package com.ward.stream

import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient

case class KinesisStreamClients(
    kinesisClient: KinesisAsyncClient,
    dynamoClient: DynamoDbAsyncClient,
    cloudWatchClient: CloudWatchAsyncClient
)
