package io.github.morgaroth.navigator_import.android

import android.app.Activity.{RESULT_CANCELED, RESULT_OK}
import android.content.Intent
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.{LayoutInflater, View, ViewGroup}
import com.droelf.gpxparser.gpxtype.GPX
import io.github.morgaroth.navigator_import.android.FetchingDataFragment.FetchingDataTrait
import io.github.morgaroth.navigator_import.android.R.string.Please_wait_fetching_is_in_progress
import io.github.morgaroth.navigator_import.android.RoutesImporterActivity.BACKEND_URL
import io.github.morgaroth.navigator_import.android.ScanQRCodeFragment.{SCAN_BARCODE_REQUEST_CODE, ScanQRCodeTrait}
import io.github.morgaroth.navigator_import.android.utils.FragmentWithAttached
import io.github.morgaroth.navigator_import.core.Core
import org.apache.http.client.methods.{HttpUriRequest, HttpGet}
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.scaloid.common._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FetchingDataFragment {

  trait FetchingDataTrait {
    def gpxFetched(gpx: GPX): Unit

    def gpxFetchingFailNoInternet(): Unit

    def gpxFetchingFailSomeError(): Unit
  }

  def apply(gpxId: String) = {
    val f = new FetchingDataFragment
    f.setArguments(args(gpxId))
    f
  }

  val ID_KEY = "id_key_FetchingDataFragment"

  def args(gpxId: String) = {
    val args = new Bundle
    args.putString(ID_KEY, gpxId)
    args
  }

}

trait HTTPUtils {
  val httpClient = new DefaultHttpClient()

  def execute: (HttpUriRequest) => (Int, String) = (r: HttpUriRequest) => {
    val response = httpClient.execute(r)
    (response.getStatusLine.getStatusCode, EntityUtils.toString(response.getEntity))
  }

}

class FetchingDataFragment extends FragmentWithAttached with TagUtil with HTTPUtils {

  import FetchingDataFragment.ID_KEY

  type Interface = FetchingDataTrait

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    info("starting fetching data")
    Future {
      try {
        val getGPXRequest = new HttpGet(s"$BACKEND_URL/api/mobile/gpx/${getArguments.getString(ID_KEY)}")
        val (resultCode, entity) = execute(getGPXRequest)
        info(s"request to backend about GPX file end with status $resultCode")
        debug(s"request to backend about GPX file end with status $resultCode and entity ${entity.substring(0, if (entity.length > 30) 30 else entity.length - 1)}")
        (resultCode, Core.loadGpx(entity)) match {
          case (200, gpx) =>
            info("not fetched")
            attached.map(_.gpxFetched(gpx)).getOrElse(warn("gpx fetched, but no activity attached"))
          case _ =>
            warn("not fetched")
            attached.map(_.gpxFetchingFailSomeError()).getOrElse(warn("gpx not fetched, and no activity attached"))
        }
      } catch {
        case t: Throwable =>
          error("error during fetching data from serwer", t)
          attached.map(_.gpxFetchingFailSomeError()).getOrElse(warn("gpx not fetched, and no activity attached"))
      }
    }
    new SFrameLayout {
      STextView(Please_wait_fetching_is_in_progress).<<.wrap.Gravity(CENTER).>>
    }
  }
}
