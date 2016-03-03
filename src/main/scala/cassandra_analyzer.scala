package ch.unisg

import org.apache.spark.{SparkConf, SparkContext}
import com.datastax.spark.connector._


object cassandra_analyzer {

  val cassandraHost = "meepcar.ch"
  val sparkMasterHost = "localhost"
  val keyspace = "dbv1"
  val conf = new SparkConf(true).set("spark.cassandra.connection.host", cassandraHost)
  val sc = new SparkContext("local", "test", conf)
  case class Measurement(id:String, timestamp:String, userid:String, value:Double)

  def main(args: Array[String]) {

    val rdd = sc.cassandraTable(keyspace,"users")
    rdd.collect().foreach(println)

  }
}


