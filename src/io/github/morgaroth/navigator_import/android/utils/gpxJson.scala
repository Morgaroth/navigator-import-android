package io.github.morgaroth.navigator_import.android.utils

import spray.json.{DefaultJsonProtocol, _}

import scala.language.implicitConversions

case class WaypointGPX(name: String, lat: Double, lon: Double) {
  override def toString: String = s"Wpt(name=$name,latitude=$lat,longitude=$lon)"
}

trait WaypointGPXProtocol extends DefaultJsonProtocol {
  implicit val WaypointGPXJsonProtocol = jsonFormat3(WaypointGPX)
}

case class GPXGet(waypoints: List[WaypointGPX]) {
  override def toString = s"""GPXGet(waypoints=${waypoints.map(_.toString).mkString(",")})"""
}

trait GPXGetProtocol extends DefaultJsonProtocol with WaypointGPXProtocol {
  implicit val GPXGetJsonProtocol = jsonFormat1(GPXGet)

  implicit def wrapToParsableGPXGet(json: String): Object {def parseMyGPX: Either[DeserializationException, GPXGet] with Product with Serializable} = new {
    def parseMyGPX = try Right(GPXGetJsonProtocol.read(json.parseJson))
    catch {
      case d: DeserializationException => Left(d)
    }

  }
}