import java.io.File
import java.util.UUID

import com.datastax.spark.connector._
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTime

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
// import com.cloudera.sparkts
import com.github.tototoshi.csv.CSVWriter



/**
  * Created by gahrb on 21.06.16.
  */
class fingerprint(sc_conf: SparkConf, keyspace: String) extends java.io.Serializable{

  val sc = new SparkContext("local[*]", "test", sc_conf)
  sc.parallelize(0 to 100)
  val f = new File("break_events.csv")
  val writer = CSVWriter.open(f)
  val kmodel = new StreamingKMeans()




  def analyze_measurement(measurement: String): Unit = {

    println(measurement)
    //val ssc = new StreamingContext(sc, Seconds(20))
    //val rdd = ssc.cassandraTable(keyspace, measurement).select("user","time","value")
    val rdd = sc.cassandraTable(keyspace, measurement).select("user","time","value").limit(50000)
    val firstrow = rdd.first()
    var valuetype = 0
    try {
      firstrow.get[Double]("value")
    }catch{
      case typeErr: IllegalArgumentException => try{
          // E.g.: brake pressure detected
          firstrow.get[Boolean]("value")
          valuetype = 1
      }catch{
        case typeErr: IllegalArgumentException =>  firstrow.get[String]("value")
          // E.g.: gear
          valuetype = 2
      }
    }

    //var numdist = 0.0
    var i=0
    //var value_map = new HashMap[DateTime,Double].empty
    if (valuetype==0){
       val value_map = rdd.map{case row => row.get[UUID]("user")->(row.get[DateTime]("time"),row.get[Double]("value"))}.groupByKey()//Grouped by the drivers
      value_map.foreach{case user => user._2.groupBy(tupel => tupel._1.dayOfMonth() )}
       //if (!value_map.isEmpty && (value_map.last._2<5)){
       analyzeValues(value_map)

    }else if(valuetype==1){
      rdd.map { case value => value.get[Boolean]("value") }
    }else{
      rdd.map { case value => value.get[String]("value") }
    }
    //println("Number of distinct " + measurement + ": " + numdist.toString)
    //dist_map += measurement->numdist
  }

  def analyzeValues(valueList: RDD[(UUID,Iterable[(DateTime,Double)])]): Unit={

    val valueArr = valueList.toArray()
    kmodel.setK(valueArr.length)
      .setDecayFactor(1.0)
      .setRandomCenters(2, 0.0)
    var userHash = new HashMap[UUID,Any].empty
    for (user <- valueArr){
      println("--------------------\n--------------------\n"+user._1.toString+"\n--------------------")
      var brakeHash = new HashMap[Int,(DateTime, DateTime, Double, Double)].empty
      var brakeArray = new ArrayBuffer[Double]()
      var start = true
      var I = 0
      var max = 0.0
      for (dt <- user._2) {
        if (start) {
          if (dt._2 > 0) {
            brakeHash += I ->(dt._1, dt._1, 0,0)
            start = false
          }
        } else {
          if (dt._2 == 0) {
            val dTime = dt._1.getMillis - brakeHash(I)._1.getMillis
            if (dTime>180000 || (brakeArray.length<=1)){ // Avoid inserting brake events over two trips. not the best way (look at the frequency of the time values)
              brakeArray.drop(brakeArray.length-1)
              brakeHash.drop(I)
              start = true
            } else {
              brakeHash += I ->(brakeHash(I)._1, dt._1, dTime, max)
              val brakeRdd = sc.parallelize(brakeArray)
              //kmodel.trainOn(brakeRdd)
              println(brakeHash(I))
              I += 1
              max = 0
              start = true
            }
          }
          brakeArray += dt._2
          if (dt._2 > max){
            max = dt._2
          }
        }
      }
      writer.writeAll(List(brakeHash.toSeq))
      userHash += user._1 -> brakeHash.toSeq
    }

    //TODO: interpolate and bring to unified frequency
    //TODO: apply FFT
    //TODO: analyze the Length and amplitude
    //TODO: analyze the frequencies and amplitudes

  }

  override def finalize(): Unit ={
    println("--------------------\nCurrent dist_map:")
    //dist_map.foreach(println)
  }
}


