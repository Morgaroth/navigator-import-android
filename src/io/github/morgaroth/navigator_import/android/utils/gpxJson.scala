package io.github.morgaroth.navigator_import.android.utils

import android.os.{Parcelable, Parcel}
import spray.json.{DefaultJsonProtocol, _}

import scala.language.implicitConversions
import scala.math.BigDecimal

case class WaypointGPX(name: String, lat: Double, lon: Double) extends Parcelable {
  override def toString: String = s"Wpt(name=$name,latitude=$lat,longitude=$lon)"

  override def describeContents(): Int = 0

  override def writeToParcel(dest: Parcel, flags: Int): Unit = {
    dest.writeString(name)
    dest.writeDouble(lat)
    dest.writeDouble(lon)
  }
}

object WaypointGPX {
  final val CREATOR: Parcelable.Creator[WaypointGPX] = new Parcelable.Creator[WaypointGPX]() {
    override def createFromParcel(source: Parcel): WaypointGPX =
      WaypointGPX(source.readString(), source.readDouble(), source.readDouble())

    override def newArray(size: Int): Array[WaypointGPX] = new Array[WaypointGPX](size)
  }
}

trait WaypointGPXProtocol extends DefaultJsonProtocol {

  implicit object WaypointGPXJsonProtocol extends RootJsonFormat[WaypointGPX] {
    val raw = jsonFormat3(WaypointGPX.apply)

    override def read(json: JsValue): WaypointGPX = {
      json match {
        case JsObject(fields) if fields.contains("name") && fields.contains("lon") && fields.contains("lat") =>
          try {
            var empty = WaypointGPX("unnamed", 0D, 0D)
            fields("name") match {
              case JsString(value) => empty = empty.copy(name = value)
            }
            fields("lon") match {
              case JsNumber(bidDecimal) if bidDecimal.isDecimalDouble =>
                empty = empty.copy(lon = bidDecimal.doubleValue())
            }
            fields("lat") match {
              case JsNumber(bidDecimal) if bidDecimal.isDecimalDouble =>
                empty = empty.copy(lat = bidDecimal.doubleValue())
            }
            empty
          } catch {
            case _: Throwable => raw.read(json)
          }
        case _ => raw.read(json)
      }
    }

    override def write(obj: WaypointGPX): JsValue = raw.write(obj)
  }

}

case class GPXGet(waypoints: List[WaypointGPX]) extends android.os.Parcelable {
  override def toString = s"""GPXGet(waypoints=${waypoints.map(_.toString).mkString(",")})"""

  override def describeContents(): Int = 0

  override def writeToParcel(dest: Parcel, flags: Int): Unit = {
    dest.writeParcelableArray(waypoints.toArray, 0)
  }

}

object GPXGet {
  final val CREATOR: Parcelable.Creator[GPXGet] = new Parcelable.Creator[GPXGet]() {
    override def createFromParcel(source: Parcel): GPXGet =
      GPXGet(source.readParcelableArray(null).asInstanceOf[Array[WaypointGPX]].toList)

    override def newArray(size: Int): Array[GPXGet] = new Array[GPXGet](size)
  }

}

trait GPXGetProtocol extends DefaultJsonProtocol with WaypointGPXProtocol {
  implicit val GPXGetJsonProtocol = jsonFormat1(GPXGet.apply)

  implicit def wrapToParsableGPXGet(json: String): Object {def parseMyGPX: Either[RuntimeException, GPXGet]} = new {
    def parseMyGPX =
      try {
        val json1: JsValue = json.parseJson
        Right(GPXGetJsonProtocol.read(json1))
      }
      catch {
        case d: DeserializationException => Left(d)
        case d: SerializationException => Left(d)
      }

  }
}