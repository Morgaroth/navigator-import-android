package io.github.morgaroth.navigator_import.android

import java.io.{PrintWriter, File}
import java.text.SimpleDateFormat

import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.navigator_import.android.FetchingDataFragment.FetchingDataTrait
import io.github.morgaroth.navigator_import.android.R.string.Please_wait_merging_is_in_progress
import io.github.morgaroth.navigator_import.android.RouteMergingFragment.LoadingMapFactorRouteFileTrait
import io.github.morgaroth.navigator_import.android.RoutesImporterActivity.BACKEND_URL
import io.github.morgaroth.navigator_import.android.utils.{WaypointGPX, GPXGet, FragmentWithAttached, GPXGetProtocol}
import io.github.morgaroth.navigator_import.core.Core
import io.github.morgaroth.navigator_import.core.models.mapfactor.routeFile.{Waypoint, Route, RoutingPoints}
import org.apache.http.client.methods.HttpGet
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.language.{implicitConversions, reflectiveCalls}
import scala.xml.XML


object RouteMergingFragment {

  trait LoadingMapFactorRouteFileTrait {
    def done: Unit
  }

  def apply(gpx: GPXGet) = {
    val f = new RouteMergingFragment
    f.setArguments(args(gpx))
    f
  }

  val ID_KEY = "id_key_RouteMergingFragment"

  def args(gpx: GPXGet) = {
    val args = new Bundle
    args.putParcelable(ID_KEY, gpx)
    args
  }

}

class RouteMergingFragment extends FragmentWithAttached with TagUtil {

  type Interface = LoadingMapFactorRouteFileTrait

  import RouteMergingFragment._

  var input: SEditText = _

  def itudeToLong(itude: Double): Long = (itude * 60 * 60 * 1000).toLong

  implicit def convertItude(d: Double): Long = itudeToLong(d)

  def fromGPXWpt(x: WaypointGPX): Waypoint = Waypoint(x.name, x.lat, x.lon)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    info("starting merging data")
    input = new SEditText()

    val f = new SimpleDateFormat()

    val newGPX = getArguments.getParcelable[GPXGet](ID_KEY)

    val readingFileAsync: Future[Option[File]] = Future {
      val path = "/Android/data/com.mapfactor.navigator/files/navigator/routing_points.xml"
      val externalLocations = ExternalStorage.getAllStorageLocations
      val sdCard = externalLocations.get(ExternalStorage.SD_CARD)
      val externalSdCard = externalLocations.get(ExternalStorage.EXTERNAL_SD_CARD)
      val availablePlaces = List(sdCard, externalSdCard).map(new File(_, path)).filter(file => file.exists() && file.canWrite && file.isFile && file.canRead)
      availablePlaces match {
        case Nil =>
          info(s"could not find routing points file")
          None
        case one :: Nil =>
          Some(one)
        case _ =>
          info("more files :/")
          None
      }
    }
    readingFileAsync.map(x => x.map { file =>
      val rp = RoutingPoints.readFromXML(Source.fromFile(file).mkString)
      info(s"currently routes in routing file: ${rp.left.map(_.getMessage).right.map(_.rest.map(_.name))}")
      (rp, file)
    }).map(x => x.map { FileAndRP =>
      val (maybeRoutingPoints, file) = FileAndRP
      (maybeRoutingPoints.right.map { rp =>
        val name = Some(f.format(System.currentTimeMillis()))
        val departure: Some[Waypoint] = Some(fromGPXWpt(newGPX.waypoints.head))
        val destination: Some[Waypoint] = Some(fromGPXWpt(newGPX.waypoints.last))
        val wpts: List[Waypoint] = newGPX.waypoints.tail.init.map(fromGPXWpt)
        val newRoute = Route(name, departure, wpts, destination)
        info(s"new route to append $newRoute")
        rp.copy(rest = newRoute :: rp.rest)
      }, file)
    }).map(x => x.map { FileAndRP =>
      info(s"writing to file $FileAndRP")
      val (maybeRoutingPoints: Either[Throwable, RoutingPoints], file: File) = FileAndRP
      maybeRoutingPoints.right.map { rp =>
        val xml = Core.toXML(rp)
        val writer = new PrintWriter(file)
        debug(s"generated xml: ${xml.mkString}")
        XML.write(writer, xml, enc = "UTF-8", xmlDecl = true, doctype = null)
        writer.flush()
        writer.close()
        info(s"saved routing_points file")
        xml
      }.left.map { throwable =>
        toast("cannot perform merging")
        error("cannot perform merging", throwable)
        throwable
      }
    })

    new SFrameLayout {
      STextView(Please_wait_merging_is_in_progress).<<.wrap.Gravity(CENTER).>>
    }
  }


  def doWork(): Unit = {
    toast(input.text.toString)
  }
}
