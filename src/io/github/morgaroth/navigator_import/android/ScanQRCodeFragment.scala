package io.github.morgaroth.navigator_import.android

import android.app.Activity.{RESULT_CANCELED, RESULT_OK}
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.Gravity.CENTER
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.navigator_import.android.R.string.Please_wait_scanning_is_turning_on
import io.github.morgaroth.navigator_import.android.ScanQRCodeFragment.{SCAN_BARCODE_REQUEST_CODE, ScanQRCodeTrait}
import io.github.morgaroth.navigator_import.android.utils.FragmentWithAttached
import org.scaloid.common._


object ScanQRCodeFragment {

  val SCAN_BARCODE_REQUEST_CODE = 876

  trait ScanQRCodeTrait {
    this: FragmentActivity =>
    def qrScanningAborted(): Unit

    def qrScanned(id: String): Unit
  }

}

class ScanQRCodeFragment extends FragmentWithAttached with TagUtil {

  type Interface = ScanQRCodeTrait

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    attached.map { x =>
      val intent = new Intent("com.google.zxing.client.android.SCAN")
      intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
      startActivityForResult(intent, SCAN_BARCODE_REQUEST_CODE)
    }
    new SFrameLayout {
      STextView(Please_wait_scanning_is_turning_on).<<.wrap.Gravity(CENTER).>>
    }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    super.onActivityResult(requestCode, resultCode, data)
    debug(s"scannig end with requestCode $requestCode resultCode $resultCode data $data")
    if (requestCode == SCAN_BARCODE_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        val contents = data.getStringExtra("SCAN_RESULT")
        val format = data.getStringExtra("SCAN_RESULT_FORMAT")
        attached.map(_.qrScanned(contents)).getOrElse(error("qr scanned but no activity attached"))
      } else if (resultCode == RESULT_CANCELED) {
        attached.map(_.qrScanningAborted()).getOrElse(error("qr scanned aborted and no activity attached"))
      }
    }
  }
}
