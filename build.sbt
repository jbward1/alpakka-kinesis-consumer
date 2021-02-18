ThisBuild / name := "alpakka-kinesis-consumer"
ThisBuild / version := "0.0.1"
ThisBuild / scalaVersion := "2.13.4"

val AkkaStreamAlpakka      = "2.0.2"
val AmazonKinesisClient    = "2.3.3"
val AmazonSDK              = "2.15.82"
val ScalaTest              = "3.2.2"
val ScalaMock              = "5.1.0"
val TestContainers         = "1.15.2"
val Logback                = "1.2.3"
val ScalaLogging           = "3.9.2"


Compile / mainClass := Some("com.ward.stream.Server")

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "software.amazon.kinesis"     % "amazon-kinesis-client"       % AmazonKinesisClient,
      "software.amazon.awssdk"      % "kinesis"                     % AmazonSDK,
      "software.amazon.awssdk"      % "dynamodb"                    % AmazonSDK,
      "software.amazon.awssdk"      % "cloudwatch"                  % AmazonSDK,
      "ch.qos.logback"              % "logback-classic"             % Logback,
      "com.typesafe.scala-logging" %% "scala-logging"               % ScalaLogging,
      "com.lightbend.akka"         %% "akka-stream-alpakka-kinesis" % AkkaStreamAlpakka,
      "org.scalactic"     %% "scalactic"         % ScalaTest,
      "org.scalamock"     %% "scalamock"         % ScalaMock          % "test,it",
      "com.amazonaws"      % "aws-java-sdk-core" % "1.11.956"         % "test,it",
      "org.scalatest"     %% "scalatest"         % ScalaTest          % "it,test",
      "org.testcontainers" % "testcontainers"    % TestContainers     % "it,test",
      "org.testcontainers" % "localstack"        % TestContainers     % "it,test"
    ).map(
      _.excludeAll(
        ExclusionRule("software.amazon.awssdk", "apache-client"),
        ExclusionRule("software.amazon.awssdk", "netty-nio-client")
      )
    )
  )
