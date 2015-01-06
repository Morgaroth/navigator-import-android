package io.github.morgaroth.navigator_import.android.utils

import android.os.{Parcelable, Parcel}
import spray.json.{DefaultJsonProtocol, _}

import scala.language.implicitConversions

case class WaypointGPX(name: String, lat: Double, lon: Double) extends Parcelable {
  override def toString: String = s"Wpt(name=$name,latitude=$lat,longitude=$lon)"

  override def describeContents(): Int = 0

  override def writeToParcel(dest: Parcel, flags: Int): Unit = {
    dest.writeString(name)
    dest.writeDouble(lat)
    dest.writeDouble(lon)
  }

  val CREATOR: Parcelable.Creator[WaypointGPX] = new Parcelable.Creator[WaypointGPX]() {
    override def createFromParcel(source: Parcel): WaypointGPX =
      WaypointGPX(source.readString(), source.readDouble(), source.readDouble())

    override def newArray(size: Int): Array[WaypointGPX] = new Array[WaypointGPX](size)
  }
}

trait WaypointGPXProtocol extends DefaultJsonProtocol {
  implicit val WaypointGPXJsonProtocol = jsonFormat3(WaypointGPX)
}

case class GPXGet(waypoints: List[WaypointGPX]) extends android.os.Parcelable {
  override def toString = s"""GPXGet(waypoints=${waypoints.map(_.toString).mkString(",")})"""

  override def describeContents(): Int = 0

  override def writeToParcel(dest: Parcel, flags: Int): Unit = {
    dest.writeParcelableArray(waypoints.toArray, 0)
  }

  val CREATOR: Parcelable.Creator[GPXGet] = new Parcelable.Creator[GPXGet]() {
    override def createFromParcel(source: Parcel): GPXGet =
      GPXGet(source.readParcelableArray(null).asInstanceOf[Array[WaypointGPX]].toList)

    override def newArray(size: Int): Array[GPXGet] = new Array[GPXGet](size)
  }

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