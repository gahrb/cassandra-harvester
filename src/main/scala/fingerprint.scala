import com.datastax.spark.connector._
import com.datastax.spark.connector.streaming._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by gahrb on 21.06.16.
  */
class fingerprint(sc_conf: SparkConf, keyspace: String) {

  val sc = new SparkContext("local[*]", "test", sc_conf)
  //val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  // create a new configuration

  def analyze_measurement(measurement: String): Unit = {

    val ssc = new StreamingContext(sc, Seconds(20))
    val rdd = ssc.cassandraTable[Double](keyspace, measurement).select("value")
    //val rd = sc.cassandraTable[Double](keyspace, measurement).select("value").limit(500)
    //val df = cSQL.cassandraSql("SELECT * FROM "+measurement+" LIMIT 500")

    //val angles = rdd.map{x => Math.round(x)}
    //val numdist = rd.map{case value => Math.round(value)}.distinct().count()
    val nummdist = rdd.map { case value => Math.round(value) }.distinct().count()
    //ssc.start()
    println("Number of distinct " + measurement + ": " + nummdist)
    //sc.stop()
    //ssc.stop()
  }
}
