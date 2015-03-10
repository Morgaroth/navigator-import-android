package io.github.morgaroth.navigator_import.android

import android.app.Activity
import android.content.Context
import android.os.{Environment, Bundle}
import android.support.v4.app.FragmentActivity
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.{View, WindowManager}
import io.github.morgaroth.navigator_import.android.build.BuildInfo
import io.github.morgaroth.navigator_import.android.utils.{External, WithDelayed}
import io.github.morgaroth.navigator_import.core.global.Route
import org.scaloid.common._

import scala.language.postfixOps

object RoutesImporterActivity {
  val BACKEND_URL = "http://navigator-import-api.herokuapp.com"
}

class RoutesImporterActivity extends FragmentActivity with SContext with WithDelayed
with ScanQRCodeFragment.ScanQRCodeTrait
with StartFragment.StartFragmentTrait
with FetchingDataFragment.FetchingDataTrait
with RouteMergingFragment.LoadingMapFactorRouteFileTrait
with DebugFragment.DebugFragmentTrait {

  implicit val thisActivity: Activity = this
  lazy val container = getUniqueId

  lazy val startFragment: StartFragment = new StartFragment
  lazy val scanQRFragment: ScanQRCodeFragment = new ScanQRCodeFragment

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    val h = getSystemService(Context.WINDOW_SERVICE).asInstanceOf[WindowManager].getDefaultDisplay.getHeight
    setContentView(new SVerticalLayout {
      this += {
        val containerL = new SFrameLayout {}.<<(FILL_PARENT, (h - 30 - 50) dip).>>
        containerL.setId(container)
        containerL
      }
      STextView(s"BuildNumber: ${BuildInfo.buildinfoBuildnumber}", (v: View) => startDebug()).<<(FILL_PARENT, 30 dip).>>.gravity(CENTER_HORIZONTAL)
    })
    info("Created!")
    startScreen()
  }

  def startScreen() = {
    getSupportFragmentManager.beginTransaction().replace(container, startFragment).commitAllowingStateLoss()
  }

  def startDebug() = {
    getSupportFragmentManager.beginTransaction().replace(container, DebugFragment()).commitAllowingStateLoss()
  }

  def scanQR() = {
    getSupportFragmentManager.beginTransaction().replace(container, scanQRFragment).commitAllowingStateLoss()
  }

  def fetchRoute(id: String) = {
    getSupportFragmentManager.beginTransaction().replace(container, FetchingDataFragment(id)).commitAllowingStateLoss()
  }

  def merge(get: Route): Unit = {
    getSupportFragmentManager.beginTransaction().replace(container, RouteMergingFragment(get)).commitAllowingStateLoss()
  }

  override def userWants: Unit = {
    info("user wants to scan QR code")
    scanQR()
  }

  override def qrScanningAborted(): Unit = {
    toast("scanning aborted")
    delayed(startScreen(), 50)
  }

  override def qrScanned(id: String): Unit = {
    toast(id.substring(0, 10))
    delayed(fetchRoute(id), 50)
  }

  override def routeFetchingFailNoInternet(): Unit = {
    toast("NoInternet")
    delayed(startScreen(), 100)
  }

  override def routeFetched(route: Route): Unit = {
    toast("fetched")
    info(s"routeFetched(${route.toString})")
    merge(route)
  }

  override def routeFetchingFailSomeError(): Unit = {
    toast("NoInternet")
    delayed(startScreen(), 100)
  }

  override def done: Unit = {
    toast("merged")
  }

  override def ready(): Unit = {
    toast("come back to normal")
    delayed(startScreen(), 100)
  }
}
