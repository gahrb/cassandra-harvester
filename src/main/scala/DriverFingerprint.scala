import java.io.File

import com.datastax.spark.connector._
import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source


/**
  * Created by gahrb on 14.06.16.
  *
  * Includes functionality to identify drivers from CAN-Bus data
  */
object DriverFingerprint {

  val cassandraHost = "136.243.156.9"
  val sparkMasterHost = "localhost"
  val keyspace = "dbv1"
  val authFile = new File("/home/gahrb/Desktop/Bosch IoT/cassandra-harvester/credentials.txt")
  val populatedSignals = Array(
    //"brake_pressure_detected",
    //"breaking_pressure",
    "brake_pressure")//,
    //"brake_pressure_detected_rpm",
    //"brake_position")//,
    //"lights",
    //"gear",
    //"cruise_control_active",
    //"cruise_control_enabled",
    //"steering_wheel_angle",
    //"trips",
    //"throttle_percent",
    //"throttle_position",
    //"throttle_pressure")
  var humanSignals = Array(
    "ignition_status",
    "throttle_pressure",
    "brake_pressure_detected_2",
    "steerine_position",
    "brake_pressure_detected",
    "twheel_angle",
    "cruise_control_enabled",
    "brake_pedal_status",
    "brake_pressure_detected7",
    "parking_brake_status",
    "wipers",
    "gear",
    "breaking_pressure",
    "brake_pressure_detected1",
    "cruise_control_active",
    "head_lights",
    "windshield_wiper_status",
    "steering_wheel_angle",
    "high_beam_status",
    "brake_pressure",
    "throtion",
    "thrngle",
    "steerintion",
    "vehicle_le",
    "trips",
    "engine_spe",
    "bearing",
    "abs_failed",
    "wheel_speed_back_rightal_acceleration",
    "brake_pressure_detected_rpm",
    "brake_positiote",
    "brake_position",
    "throttle_percent",
    "lights",
    "turn_indicator",
    "throttle_position",
    "accelerator_pedal_position",
    "obd_throttle_position",
    "headlamp_status",
    "steering_wheelrate"
  )

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
      .set("input.fetch.size_in_rows","1000")
      .setMaster("local[*]")
    val sc = new SparkContext("local[*]", "test", sc_conf)
    //case class Measurement(id: String, timestamp: String, userid: String, value: Double)
    //val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    val user_rdd = get_users(sc)
    sc.stop()


    val start_date = "2016-06-21"
    val stop_date = "2016-07-21"

    //val fp = new fingerprint(sc_conf, keyspace)
    val ss = new screen_showtime(sc_conf, keyspace)
    for (user_data <- user_rdd.par) {
      if (!user_data.isNullAt("userid") && !user_data.isNullAt("email")) {
        var email = user_data.getString("email")
        var userId = user_data.getUUID("userid")
        println(email + ": " + userId.toString())

        // for (feature <- populatedSignals) {
        //   fp.analyze_measurement(feature,userId)
        //   fp.finalize()
        // }
        //ss.getScreentime(userId.toString,start_date,stop_date)
      }
    }
    ss.valtoFile()

    // var features = ListBuffer.empty[String]
    // for (signal <- humanSignals){
    //   //var size = sqlContext.sql("SELECT value FROM "+features+" LIMIT 5000").count()
    //      val rdd = ssc.cassandraTable(keyspace,signal).limit(5000).cassandraCount()
    //   var size = sc.cassandraTable(keyspace,signal).select("value").limit(5000).cassandraCount()
    //   if (size >= 5000){
    //     features += signal
    //     println(signal)
    //   }
    // }
    //sc.stop()
    println(populatedSignals.toString)


  }

  def get_users(sc: SparkContext): Array[CassandraRow] = {
    sc.cassandraTable(keyspace, "users").select("userid", "email").collect()
  }
}
