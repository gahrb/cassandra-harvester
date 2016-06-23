/**
  * Created by gahrb on 10.06.16.
  */

import java.io.File

import com.datastax.spark.connector._
import cone_analyzer.cone_analyzer
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

object weather_analyzer {

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

    val ca = new cone_analyzer
    val hotspots = ca.load_hotspots(cassandraHost, 8080, "/hotspots")
    val cone_conf = ca.load_cone(cassandraHost, 8080, "/cone")

    for (user_data <- user_rdd) {
      if (!user_data.isNullAt("userid") && !user_data.isNullAt("email")) {
        var email = user_data.getString("email")
        var userId = user_data.getUUID("userid")
        println(email + ": " + userId.toString())
        val trips = sc.cassandraTable(keyspace, "trips").where("userid = " + userId.toString).collect()
        for (trip <- trips) {
          if (trip.getDouble("length") > 20) {
            val temp = sc.cassandraTable(keyspace, "outside_temperature").where("user = " + userId.toString + " AND time >= '" + trip.getDateTime("start_time") + "' AND time <= '" + trip.getDateTime("end_time") + "'").collect()
            if (temp.length > 0) {
              println(temp)
            }
          }
        }
      }
    }
    sc.stop()
  }


  def get_users(sc: SparkContext): Array[CassandraRow] = {
    sc.cassandraTable(keyspace, "users").select("userid", "email").collect()
  }

}


