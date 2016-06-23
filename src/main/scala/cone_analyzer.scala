package cone_analyzer


import net.liftweb.json.JsonAST.JValue
import net.liftweb.json._

import scalaj.http.Http

/**
  * Created by gahrb on 11.05.16.
  */
case class hotspot(ID: JString, Lat: JString, Lon: JString, Type: JString, Text: JString)

class cone_analyzer {

  implicit val formats = DefaultFormats

  def load_hotspots(server: String, port: Int, path: String): JValue = {
    val page = ("http://" + server + ":" + port + path)
    val request = Http(page).asString.body
    JsonParser.parse(request.replaceAll("'", "")) \ "Hotspots"
  }


  def load_cone(server: String, port: Int, path: String): JValue = {
    val page = ("http://" + server + ":" + port + path)
    val request = Http(page).asString.body
    JsonParser.parse(request.replaceAll("'", "")) \ "cone"
  }

  def hs_approach(hs: JValue, lat: Double, lng: Double): String = {
    for (it <- hs.children.indices) {
      val hs_i = hs.apply(it)
      if (gps_dist(pretty(render(hs_i \ "Lat")).stripPrefix("\"").stripSuffix("\"").toDouble, pretty(render(hs_i \ "Lon")).stripPrefix("\"").stripSuffix("\"").toDouble, lat, lng) < 300) {
        return pretty(render(hs_i \ "ID")).stripPrefix("\"").stripSuffix("\"")
      }
      print(hs_i \ "ID")
    }
    "-1"
    //if (hs \ "latitude" - location[0])<500){
    //  true
    //}
  }

  def gps_dist(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double = {
    lat1 - lat2 + lng1 - lng2
  }

  def cone_calc(hotspot: JObject, cone_conf: JObject): Boolean = {
    false
  }
}
