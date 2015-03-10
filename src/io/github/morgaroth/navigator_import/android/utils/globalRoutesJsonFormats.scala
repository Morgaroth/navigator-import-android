package io.github.morgaroth.navigator_import.android.utils

import io.github.morgaroth.navigator_import.core.global.{Route, Waypoint}
import spray.json.JsonParser.ParsingException
import spray.json._

import scala.language.implicitConversions


trait WptProtocol extends DefaultJsonProtocol {

  implicit object WaypointGPXJsonProtocol extends RootJsonFormat[Waypoint] {
    val raw = jsonFormat(Waypoint.apply,"name","latitude","longitude")

    override def read(json: JsValue) = {
//      json match {
//        case JsObject(fields) if fields.contains("longitude") && fields.contains("latitude") =>
//          try {
//            val name = fields.get("name").map {
//              case JsString(value) => value
//            }
//            val long = fields("longitude") match {
//              case JsNumber(bidDecimal) if bidDecimal.isDecimalDouble => bidDecimal.doubleValue()
//            }
//            val lat = fields("latitude") match {
//              case JsNumber(bidDecimal) if bidDecimal.isDecimalDouble => bidDecimal.doubleValue()
//            }
//            Waypoint(name, lat, long)
//          } catch {
//            case _: Throwable => raw.read(json)
//          }
//        case _ =>
//          raw.read(json)
//      }
      raw.read(json)
    }

    override def write(obj: Waypoint): JsValue = raw.write(obj)
  }

}


trait RouteProtocol extends DefaultJsonProtocol with WptProtocol {
//  implicit val RouteRootJsonProtocol = jsonFormat4(Route.apply)
  implicit val RouteRootJsonProtocol = jsonFormat(Route.apply, "name", "departure", "waypoints", "destination")

  case class ParsableRoute(json:String){
    def parseRoute =
      try {
        val json1: JsValue = json.parseJson
        Right(RouteRootJsonProtocol.read(json1))
      }
      catch {
        case d: DeserializationException => Left(d)
        case d: SerializationException => Left(d)
        case d: ParsingException => Left(d)
      }
  }

  implicit def wrapToParsableRoute(json: String): RouteProtocol.this.type#ParsableRoute = new ParsableRoute(json)
}