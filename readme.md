
# Alpakka Kinesis Consumer Testing
This project attempts to use `akka-stream-alpakka-kinesis` along with 
the `amazon-kinesis-client` to connect to a `locakstack` KCL consumer.

## Install AWS Local CLI
aws-cli version 2
```bash
pip install awscli-local
```

## Running
This is a multi-step process, so apologies.

1. Startup localstack via docker `docker-compose.yaml`
```bash
docker-compose up -d
```
2. Start the `Server`
```bash
sbt run
```

3. Put some test messages in kinesis stream
```bash
./scripts/put-record.sh
```

4. Monitor the terminal, notice no messages are received
from the `KinesisStreamSource`
   
5. Shutdown and cleanup 
```bash
docker rm -f stream-localstack
rm -rf .localstack
```

## Testing

Run integration tests
```bash
sbt it:test
```