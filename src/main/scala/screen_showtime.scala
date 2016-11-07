import java.io.{File, PrintWriter}
import java.util.Date

import com.datastax.spark.connector._
import org.apache.commons.io.FileUtils
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.Map

// import com.cloudera.sparkts


/**
  * Created by gahrb on 21.06.16.
  */
class screen_showtime(sc_conf: SparkConf, keyspace: String) extends java.io.Serializable{

  val sc = new SparkContext("local[*]", "test", sc_conf)
  //sc.parallelize(0 to 4)
  val kmodel = new StreamingKMeans()
  //val l = new PrintWriter(new File("/home/gahrb/matlab/data/cassandraToCsv.log"))
  var screen_Hash = Map[String,Double]()


  def getScreentime( userId: String , start_time: String, stop_time: String): Unit = {

    val rdd_trip = sc.cassandraTable(keyspace, "trips").select("userid","start_time","end_time").where("userid="+userId.toString+" AND start_time>'"+start_time+"' AND start_time<'"+stop_time+"'")

    val CassArr = rdd_trip.collect()

    for (row <- CassArr){
      eachTrip(row)
    }
    println(screen_Hash.toString())
  }

  def eachTrip (row: CassandraRow): Unit={
    val start_trip = row.getDate("start_time")
    val start_tripSTR = row.getString("start_time")
    val stop_tripSTR = row.getString("end_time")
    val rdd = sc.cassandraTable(keyspace, "selected_screen_type").select("time","value").where("user="+row.getString("userid")+" AND time>'"+start_tripSTR+"' AND time<'"+stop_tripSTR+"'").collect()


    var token = start_trip
    var ts = new Date
    var old_ts = 0.0
    var screen = "START_SCREEN"
    for (trip_row <- rdd) {
      screen = trip_row.getString("value")

      ts = trip_row.getDate("time")
      if (screen_Hash.contains(screen)) {
        screen_Hash(screen) += old_ts
      }else{
        screen_Hash += (screen -> old_ts)
      }
      old_ts = (ts.getTime - token.getTime) / 1000
      token = ts
    }
    // Add also the time until the trip ended
    val stop_trip = row.getDate("end_time")
    old_ts = (stop_trip.getTime - token.getTime) / 1000
    if (screen_Hash.contains(screen)) {
      screen_Hash(screen) += old_ts
    }else{
      screen_Hash += (screen -> old_ts)
    }
    println(screen_Hash.toString())
  }

  def valtoFile(): Unit={
    val f = new File("/home/gahrb/Desktop/CassandraExtract/screenTime.csv")
    f.getParentFile().mkdirs()
    if(f.exists()){ FileUtils.deleteQuietly(f)}
    val writer = new PrintWriter(f)
    println("----------------------------")
    println(screen_Hash)
    for (line <- screen_Hash){
      println(line.toString())
      writer.write(line._1 + ", "+line._2.toString)
      writer.append("\n")
    }
    writer.close()
    println("----------------------------")
  }

}


