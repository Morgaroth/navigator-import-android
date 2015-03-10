package io.github.morgaroth.navigator_import.android

import java.io.FileOutputStream

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.navigator_import.android.DebugFragment._
import io.github.morgaroth.navigator_import.android.utils.{External, FragmentWithAttached, MapFactorHelper, RouteProtocol}
import org.scaloid.common._

import scala.language.reflectiveCalls

object DebugFragment {

  val READ_REQUEST_CODE = 5423
  val WRITE_REQ = 5424

  trait DebugFragmentTrait {
    def ready(): Unit
  }

  def apply() = {
    val f = new DebugFragment
    //    f.setArguments(args(routeId))
    f
  }

  val ID_KEY = "id_key_FetchingDataFragment"

  def args(routeId: String) = {
    val args = new Bundle
    args.putString(ID_KEY, routeId)
    args
  }

}

class DebugFragment extends FragmentWithAttached with TagUtil with HTTPUtils with RouteProtocol {

  type Interface = DebugFragmentTrait

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    info("starting debug fragment")
    new SVerticalLayout {
      SButton("print readFrom /proc/mounts", info(External.`readFrom /proc/mounts`.mkString("readFrom /proc/mounts\n\t", "\n\t", "\n\tend of readFrom /proc/mounts")))
      SButton("print readFrom /system/etc/vold.fstab", info(External.`readFrom /system/etc/vold.fstab`.mkString("readFrom /system/etc/vold.fstab\n\t", "\n\t", "\n\tend of readFrom /system/etc/vold.fstab")))
      SButton("print readFrom /storage directly", info(External.`try read /storage directly`.mkString("readFrom /storage directly\n\t", "\n\t", "\n\tend of readFrom /storage directly")))
      SButton("print possibly routing files", info(MapFactorHelper.findMapFactorHomes.map(f => (f.canRead, f.canWrite, f.isFile, f.exists(), f.getAbsolutePath)).mkString("possibly files\n\t", "\n\t", "\n\tend of possibilities")))
      SButton("perform search", (v: View) => performFileSearch)
      SButton("Done", attached.foreach(_.ready()))
    }
  }

  def performFileSearch = {

    // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
    // browser.
    val intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
      .addCategory(Intent.CATEGORY_OPENABLE)
      .setType("*/*")

    // Filter to only show results that can be "opened", such as a
    // file (as opposed to a list of contacts or timezones)
    //    intent.addCategory(Intent.CATEGORY_OPENABLE)

    // Filter to show only images, using the image MIME data type.
    // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
    // To search for all documents available via installed storage providers,
    // it would be "*/*".
    //    intent.setType("image/*")

    startActivityForResult(intent, WRITE_REQ)
  }

  override def onActivityResult(requestCode: Int, resultCode: Int,
                                resultData: Intent) {

    // The ACTION_OPEN_DOCUMENT intent was sent with the request code
    // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
    // response to some other intent, and the code below shouldn't run at all.

    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == READ_REQUEST_CODE) {
        var uri: Uri = null
        if (resultData != null) {
          uri = resultData.getData
          info("Uri: " + uri.toString)
        }
      } else if (requestCode == WRITE_REQ) {
        if (resultData != null) {
          val currentUri: Uri = resultData.getData
          info("Uri: " + currentUri.toString)
//          try {
//            val pfd = activityContext.getContentResolver.openFileDescriptor(currentUri, "w")
//            val fileOutputStream = new FileOutputStream(pfd.getFileDescriptor)
//            val textContent = "<name>dupa cycki</name>"
//            fileOutputStream.write(textContent.getBytes)
//            fileOutputStream.close()
//            pfd.close()
//          } catch {
//            case e: Throwable => println(s"sucks $e")
//          }
        }
      }
    }
  }
}
