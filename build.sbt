
name := "spark-cassandra-analyzer_scala"
version := "0.0.1"
organization := "ch.unisg"
scalaVersion := "2.10.5"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

//libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0", // % "provided",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
//  "org.apache.spark" %% "spark-hive" % "1.6.0",
//  "org.apache.spark" %% "spark-streaming" % "1.6.0",
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0",
//  "org.apache.spark" %% "spark-streaming-flume" % "1.6.0",
//  "org.apache.spark" %% "spark-mllib" % "1.6.0",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.6.0-M1",
  "com.datastax.spark" %% "spark-cassandra-connector-java" % "1.6.0-M1"
)
