import java.io.File
import java.util.UUID

import com.datastax.spark.connector._
import org.apache.commons.io.FileUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.DateTime

import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
// import com.cloudera.sparkts
import com.github.tototoshi.csv.CSVWriter

/**
  * Created by gahrb on 21.09.16.
  */
class extractTableperUser(sc_conf: SparkConf, keyspace: String) extends java.io.Serializable {

  val sc = new SparkContext("local[*]", "test", sc_conf)
  sc.parallelize(0 to 4)
  // val kmodel = new StreamingKMeans()
  //val l = new PrintWriter(new File("/home/gahrb/matlab/data/cassandraToCsv.log"))


  def analyze_measurement(measurement: String, userId: UUID): Unit = {

    println("---------------------------------\nReading the following from the database:")
    println(measurement)
    println(userId)
    //val ssc = new StreamingContext(sc, Seconds(20))
    //val rdd = ssc.cassandraTable(keyspace, measurement).select("user","time","value")
    //val rdd = sc.cassandraTable(keyspace, measurement).select("user","time","value").where("user="+userId.toString)
    val rdd = sc.cassandraTable(keyspace, measurement).where("user="+userId.toString)
    try{
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
      if (valuetype==0 || valuetype==2){
         //val value_map = rdd.map{case row => row.get[UUID]("user")->(row.get[DateTime]("time"),row.get[Double]("value"))}.groupByKey()//Grouped by the drivers
         //analyzeValues(value_map,measurement,userId.toString)
        //val value_map = rdd.map{case row => Seq(row.getDateTime("time"),row.getDouble("value"))}
        valtoCSV(rdd,measurement,userId.toString)

      }else if(valuetype==1){
        rdd.map { case value => value.get[Boolean]("value") }
      }else{
        rdd.map { case value => value.get[String]("value") }
      }
      //println("Number of distinct " + measurement + ": " + numdist.toString)
      //dist_map += measurement->numdist
    }catch{
      case typeErr: UnsupportedOperationException => println("Could not load first row. User: "+userId.toString+"\n tying again!")
         valtoCSV(rdd,measurement,userId.toString)
    }
  }

  def analyzeValues(valueList: RDD[(UUID,Iterable[(UUID,DateTime,Double,Double,Double)])],measurement: String, user: String): Unit={

    val valueArr = valueList.toArray()
    // kmodel.setK(valueArr.length)
    //   .setDecayFactor(1.0)
    //   .setRandomCenters(2, 0.0)
    var userHash = new HashMap[UUID,Any].empty
    for (user <- valueArr){
      //println("--------------------\n--------------------\n"+user._1.toString+"\n--------------------")
      var brakeHash = new HashMap[Int,(DateTime, DateTime, Double, Double)].empty
      var brakeArray = new ArrayBuffer[Double]()
      var start = true
      var I = 0
      var max = 0.0
      val f = new File("/home/gahrb/matlab/data/"+measurement+"/"+measurement+"_"+user._1.toString+".csv")
      f.getParentFile().mkdirs()
      val writer = CSVWriter.open(f)
      for (dt <- user._2) {
        writer.writeRow(Seq(dt._1,dt._2.getMillis,dt._3,dt._4,dt._5))

      }
      //writer.writeAll(List(brakeHash.toSeq))
      userHash += user._1 -> brakeHash.toSeq
    }

  }

  def valtoCSV(valueList: RDD[CassandraRow], measurement: String, user: String): Unit={
    val f = new File("/home/gahrb/matlab/data/"+measurement+"/"+measurement+"_"+user)
    f.getParentFile().mkdirs()
    try {
      //l.info("Writing user:" + user)
      //if (!(new File("/home/gahrb/matlab/data/"+measurement+"/"+measurement+"_"+user+"/_SUCCESS").exists())){
      if(f.exists()){ FileUtils.deleteQuietly(f)}
      valueList.map(a => a.getDateTime("time").getMillis.toString + "," + a.getString("latitude")+ "," + a.getString("longitude")+ "," + a.getString("value")).saveAsTextFile(f.getAbsoluteFile.toString)
      println("Finished writing for user: "+user)
      //}else{
        //println("Data already existed for user: "+user)
        //println("Skipping...")
      //}
    }catch{
      case typeErr: NumberFormatException => try{
        println("Error while writing:" + typeErr)
        valueList.map(a => a.getDateTime("time").getMillis.toString + "," + a.getString("value").replaceAll("\"","").toDouble).saveAsTextFile(f.getAbsoluteFile.toString)
      }catch {
        case typeErr: UnsupportedOperationException => valueList.map(a => a.getDateTime("time").getMillis.toString + "," + a.getString("value").replaceAll("\"", "")).saveAsTextFile(f.getAbsoluteFile.toString)
          println("Error while writing:" + typeErr)
      }
      case typeErr: UnsupportedOperationException => try{
        println("Error while writing:" + typeErr)
        valueList.map(a => a.getDateTime("time").getMillis.toString + "," + a.getString("value").replaceAll("\"","")).saveAsTextFile(f.getAbsoluteFile.toString)
        }finally{
          valueList.map(a => a.getDateTime("time").getMillis.toString + "," + a.getString("value")).saveAsTextFile(f.getAbsoluteFile.toString)
        }
    }finally{
      if (f.exists() && !(new File("/home/gahrb/matlab/data/"+measurement+"/"+measurement+"_"+user+"/_SUCCESS").exists())){
        FileUtils.deleteQuietly(f)
        valueList.map(a => a.getDateTime("time").getMillis.toString + "," + a.getString("value")).saveAsTextFile(f.getAbsoluteFile.toString)
        println("Finished writing for user: "+user)
      }else{
        println("Data already existed for user: "+user)
        println("Skipping...")
      }
    }
    //l.info("Finished writing for user: "+user)
  }

  override def finalize(): Unit ={
    //println("--------------------\nCurrent dist_map:")
    //dist_map.foreach(println)
  }
}
