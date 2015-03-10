package io.github.morgaroth.navigator_import.android.utils

import android.os.{Parcel, Parcelable}
import io.github.morgaroth.navigator_import.core.global

object parcelableWrappers {

  case class WaypointParcelable(
                                 name: Option[String],
                                 latitude: Double,
                                 longitude: Double) extends Parcelable {
    override def toString: String = s"Wpt(name=$name,latitude=$latitude,longitude=$longitude)"

    override def describeContents(): Int = 0

    override def writeToParcel(dest: Parcel, flags: Int): Unit = {
      dest.writeString(name.getOrElse(RouteParcelable.none))
      dest.writeDouble(latitude)
      dest.writeDouble(longitude)
    }

    def toWaypoint = global.Waypoint(name, latitude, longitude)
  }

  object WaypointParcelable {

    def apply(wrapped: global.Waypoint): WaypointParcelable = WaypointParcelable(wrapped.name, wrapped.latitude, wrapped.longitude)

    final val CREATOR: Parcelable.Creator[WaypointParcelable] = new Parcelable.Creator[WaypointParcelable]() {
      override def createFromParcel(source: Parcel): WaypointParcelable =
        WaypointParcelable(source.readString() match {
          case RouteParcelable.none => None
          case another => Some(another)
        }, source.readDouble(), source.readDouble())

      override def newArray(size: Int): Array[WaypointParcelable] = new Array[WaypointParcelable](size)
    }
  }

  case class RouteParcelable(
                              name: Option[String],
                              departure: Option[WaypointParcelable],
                              waypoints: List[WaypointParcelable],
                              destination: Option[WaypointParcelable]) extends android.os.Parcelable {
    override def toString = s"""GPXGet(waypoints=${waypoints.map(_.toString).mkString(",")})"""

    override def describeContents(): Int = 0

    override def writeToParcel(dest: Parcel, flags: Int): Unit = {
      dest.writeString(name.getOrElse(RouteParcelable.none))
      dest.writeParcelable(departure.getOrElse(RouteParcelable.NoneWpt), 0)
      dest.writeParcelableArray(waypoints.toArray, 0)
      dest.writeParcelable(destination.getOrElse(RouteParcelable.NoneWpt), 0)
    }

    def toGlobalRoute = global.Route(
      name,
      departure.map(_.toWaypoint),
      waypoints.map(_.toWaypoint),
      destination.map(_.toWaypoint)
    )
  }

  object RouteParcelable {
    def apply(wrapped: global.Route): RouteParcelable = RouteParcelable(
      wrapped.name,
      wrapped.departure.map(WaypointParcelable.apply),
      wrapped.waypoints.map(WaypointParcelable.apply),
      wrapped.destination.map(WaypointParcelable.apply)
    )

    val none: String = "4scjfbdscjfythfgrdefghjuhgf97654wsdfghjkmnbvcserghj87654edf"
    val NoneWpt = WaypointParcelable(Some(none), 0.0d, 0.0d)

    final val CREATOR: Parcelable.Creator[RouteParcelable] = new Parcelable.Creator[RouteParcelable]() {
      override def createFromParcel(source: Parcel): RouteParcelable = {
        val name = source.readString() match {
          case RouteParcelable.none => None
          case another => Some(another)
        }
        val departure = source.readParcelable[WaypointParcelable](null) match {
          case RouteParcelable.NoneWpt => None
          case another => Some(another)
        }
        val betwn = source.readParcelableArray(null).asInstanceOf[Array[WaypointParcelable]].toList
        val destination = source.readParcelable[WaypointParcelable](null) match {
          case RouteParcelable.NoneWpt => None
          case another => Some(another)
        }
        RouteParcelable(name, departure, betwn, destination)
      }

      override def newArray(size: Int): Array[RouteParcelable] = new Array[RouteParcelable](size)
    }
  }

}
