package io.github.morgaroth.navigator_import.android

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.WindowManager
import com.droelf.gpxparser.gpxtype.GPX
import io.github.morgaroth.navigator_import.android.build.BuildInfo
import io.github.morgaroth.navigator_import.android.utils.{GPXGet, WithDelayed}
import org.scaloid.common._

import scala.language.postfixOps

object RoutesImporterActivity {
  val BACKEND_URL = "http://navigator-import-api.herokuapp.com"
}

class RoutesImporterActivity extends FragmentActivity with SContext with WithDelayed
with ScanQRCodeFragment.ScanQRCodeTrait
with StartFragment.StartFragmentTrait
with FetchingDataFragment.FetchingDataTrait {

  implicit val thisActivity: Activity = this
  lazy val container = getUniqueId

  lazy val startFragment: StartFragment = new StartFragment
  lazy val scanQRFragment: ScanQRCodeFragment = new ScanQRCodeFragment

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    val h = getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager].getDefaultDisplay.getHeight
    info(s"height $h")
    setContentView(new SVerticalLayout {
      this += {
        val containerL = new SFrameLayout {}.<<(FILL_PARENT, (h - 30 - 50) dip).>>
        containerL.setId(container)
        containerL
      }
      STextView(s"BuildNumber: ${BuildInfo.buildinfoBuildnumber}").<<(FILL_PARENT, 30 dip).>>.gravity(CENTER_HORIZONTAL)
    })
    startScreen()
    info("Created!")
  }

  def startScreen() = {
    getSupportFragmentManager.beginTransaction().replace(container, startFragment).commit()
  }

  def scanQR() = {
    getSupportFragmentManager.beginTransaction().replace(container, scanQRFragment).commit()
  }

  def fetchGPX(id: String) = {
    getSupportFragmentManager.beginTransaction().replace(container, FetchingDataFragment(id)).commit()
  }

  override def userWants: Unit = {
    info("user wants to scan QR code")
    scanQR()
  }

  override def qrScanningAborted(): Unit = {
    toast("scanning aborted")
    startScreen()
  }

  override def qrScanned(id: String): Unit = {
    toast(id.substring(0, 10))
    delayed(fetchGPX(id), 50)
  }

  override def gpxFetchingFailNoInternet(): Unit = {
    toast("NoInternet")
    delayed(startScreen(), 100)
  }

  override def gpxFetched(gpx: GPXGet): Unit = {
    toast("fetched")
    info(gpx.toString())
    delayed(startScreen(), 100)
  }

  override def gpxFetchingFailSomeError(): Unit = {
    toast("NoInternet")
    delayed(startScreen(), 100)
  }
}
