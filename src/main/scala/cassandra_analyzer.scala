//package ch.unisg

import java.io.File
import java.util.UUID

import com.datastax.spark.connector._
import cone_analyzer.cone_analyzer
import net.liftweb.json.JsonAST
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source


object cassandra_analyzer {

  val cassandraHost = "136.243.156.9"
  val sparkMasterHost = "localhost"
  val keyspace = "dbv1"
  val authFile = new File("/home/gahrb/Desktop/Bosch IoT/cassandra-harvester/credentials.txt")

  def main(args: Array[String]) {
    var user = ""
    var password = ""
    if (authFile.exists()) {
      for (line <- Source.fromFile(authFile).getLines())
        if (line.contains("user:")) {
          user = line.split(":")(1).trim()
        } else if (line.contains("password: ")) {
          password = line.split(":")(1).trim()
        }
    } else sys.error("Authentication File not Found")
    val sc_conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", cassandraHost)
      .set("spark.cassandra.auth.username", user)
      .set("spark.cassandra.auth.password", password)
    val sc = new SparkContext("local", "test", sc_conf)
    //case class Measurement(id: String, timestamp: String, userid: String, value: Double)
    val user_rdd = get_users(sc)
    sc.stop()

    val ca = new cone_analyzer
    val hotspots = ca.load_hotspots(cassandraHost, 8080, "/hotspots")
    val cone_conf = ca.load_cone(cassandraHost, 8080, "/cone")

    for (user_data <- user_rdd) {
      if (!user_data.isNullAt("userid") && !user_data.isNullAt("email")) {
        var email = user_data.getString("email")
        var userId = user_data.getUUID("userid")
        println(email + ": " + userId.toString())
        val tmp = getlocs(sc_conf, userId, ca, hotspots)
      }
    }
  }

  def getlocs(sc_conf: SparkConf, userid: UUID, ca: cone_analyzer.cone_analyzer, hs: JsonAST.JValue) {
    val sc = new SparkContext("local", "cone_analyzer", sc_conf)
    val rdd = sc.cassandraTable(keyspace, "gps_speed").select("longitude", "latitude", "value").where("user = " + userid.toString).collect()
    //TODO: call cone_approach check, if yes, find end value and send the location set to cone_simulator
    for (elem <- rdd) {
      val lat = elem.getDouble("latitude")
      val lng = elem.getDouble("longitude")
      if (!ca.hs_approach(hs, lat, lng).isEmpty) {
        println("Found cone approach")
      }
    }
    // rdd.foreach(if (ca.hs_appraoch(hs,[lat,lng]){
    //
    //
    // })
    sc.stop()
    println(rdd)
  }

  def get_users(sc: SparkContext): Array[CassandraRow] = {
    sc.cassandraTable(keyspace, "users").select("userid", "email").collect()
  }

}


