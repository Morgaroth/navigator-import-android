package io.github.morgaroth.navigator_import.android

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat

import android.os.{Environment, Bundle}
import android.view.Gravity.CENTER
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.navigator_import.android.R.string.Please_wait_merging_is_in_progress
import io.github.morgaroth.navigator_import.android.RouteMergingFragment.LoadingMapFactorRouteFileTrait
import io.github.morgaroth.navigator_import.android.utils.{MapFactorHelper, External}
import io.github.morgaroth.navigator_import.android.utils.parcelableWrappers.RouteParcelable
import io.github.morgaroth.navigator_import.core.Core
import io.github.morgaroth.navigator_import.core.global.Route
import io.github.morgaroth.navigator_import.core.models.mapfactor.routeFile.RoutingPoints
import org.scaloid.common._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.Source
import scala.language.{implicitConversions, reflectiveCalls}
import scala.xml.XML
import scala.collection.JavaConverters._

object RouteMergingFragment {

  trait LoadingMapFactorRouteFileTrait {
    def done: Unit
  }

  def apply(route: Route) = {
    val f = new RouteMergingFragment
    f.setArguments(args(route))
    f
  }

  val ID_KEY = "id_key_RouteMergingFragment"

  def args(route: Route) = {
    val args = new Bundle
    args.putParcelable(ID_KEY, RouteParcelable(route))
    args
  }

}



class RouteMergingFragment extends utils.FragmentWithAttached with TagUtil with io.github.morgaroth.navigator_import.core.modelsConversions.toNavigator {

  type Interface = LoadingMapFactorRouteFileTrait

  import io.github.morgaroth.navigator_import.android.RouteMergingFragment._

  var input: SEditText = _

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    info("starting merging data")
    input = new SEditText()

    val f = new SimpleDateFormat()

    val newRoute = getArguments.getParcelable[RouteParcelable](ID_KEY).toGlobalRoute.toNaviRoute

    val readingFileAsync: Future[Option[File]] = Future {
      MapFactorHelper.findMapFactorHomes.filter(_.isFile) match {
        case Nil =>
          info(s"could not find routing points file")
          None
        case one :: Nil =>
          info(s"used ${one.getAbsolutePath} as source for merging")
          Some(one)
        case _ =>
          info("more places :/")
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
        info(s"new route to append $newRoute")
        rp.copy(rest = newRoute :: rp.rest)
      }, file)
    }).map(x => x.map { FileAndRP =>
      info(s"writing to file $FileAndRP")
      val (maybeRoutingPoints: Either[Throwable, RoutingPoints], file: File) = FileAndRP
      maybeRoutingPoints.right.map { rp =>
        val xml = Core.toXML(rp)
        try {
          println(s"generated xml: ${xml.mkString}")
          val writer = new PrintWriter(file)
          println(s"writer has errros ${writer.checkError()}")
          println(s"generated xml: ${xml.mkString}")
          XML.write(writer, xml, enc = "UTF-8", xmlDecl = true, doctype = null)
          println(s"generated xml: ${xml.mkString}")
          writer.flush()
          println(s"generated xml: ${xml.mkString}")
          writer.close()
          println(s"saved routing_points file")
          xml
        }catch{
          case t:Throwable =>
            println(s"$t fsgfdsgfdsfdsfds")
        } finally {
          println("fdsafdsgfdsgdf")
        }
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
