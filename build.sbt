
name := "spark-cassandra-analyzer_scala"
version := "0.0.1"
organization := "ch.unisg"
scalaVersion := "2.11.1"
ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

//libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0", // % "provided",
  "org.apache.spark" %% "spark-sql" % "1.6.0",
  "org.apache.spark" %% "spark-streaming" % "1.6.0",
  "org.apache.spark" %% "spark-mllib" % "1.6.0",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.6.0",
  "com.datastax.spark" %% "spark-cassandra-connector-java" % "1.6.0-M1",
  // "com.cloudera.sparkts" % "sparktimeseries" % "0.3.0",
//  "org.apache.spark" %% "spark-hive" % "1.6.0",
  // "org.datastax.spark" %% "spark-streaming-kafka" % "1.6.0",
//  "org.apache.spark" %% "spark-streaming-flume" % "1.6.0",
//  "org.apache.spark" %% "spark-mllib" % "1.6.0",
  "net.liftweb" %% "lift-json" % "2.6.3",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "org.scala-lang" % "jline" % "2.11.0-M3",
  //excludeAll (
  //ExclusionRule(organization = "org.scala-lang"),
  //ExclusionRule("jline", "jline"),
  //ExclusionRule("org.slf4j", "slf4j-api")
  //)
  "com.github.tototoshi" %% "scala-csv" % "1.3.3"
)

//ivyXML := <dependency org="com.cloudera.sparkts" name="sparktimeseries" rev="0.1.0" />