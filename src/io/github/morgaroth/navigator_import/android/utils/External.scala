package io.github.morgaroth.navigator_import.android.utils

import java.io.File

import android.os.Environment
import io.github.morgaroth.navigator_import.android.utils.OptionHelpers.OptIf

import scala.io.Source
import scala.util.Try

object OptionHelpers {
  def OptIf[T](value: T)(f: T => Boolean): Option[T] = Option(value).flatMap(v => if (f(v)) Some(v) else None)
}

object External {

  def readAll: Set[String] = `readFrom /proc/mounts` ++ `readFrom /system/etc/vold.fstab` ++ `try read /storage directly` ++
    `read external from env`

  def `read external from env` = Set(Environment.getExternalStorageDirectory.getPath, Environment.getDataDirectory.getPath)

  def `readFrom /proc/mounts`: Set[String] = {
    try {
      val mountFile: File = new File("/proc/mounts")
      if (mountFile.exists) {
        Source.fromFile(mountFile).getLines().map {
          case line if line.startsWith("/dev/block/vold/") =>
            val lineElements = line.split(" ")
            Some(lineElements(1))
          case _ => None
        }.flatten.map(new File(_)).map(_.getAbsolutePath).toSet
      } else Set.empty
    } catch {
      case e: Exception =>
        e.printStackTrace()
        Set.empty
    }
  }

  def `readFrom /system/etc/vold.fstab`: Set[String] = try {
    val voldFile: File = new File("/system/etc/vold.fstab")
    if (voldFile.exists) {
      Source.fromFile(voldFile).getLines().toList.map {
        case line if line.startsWith("dev_mount") =>
          val lineElements: Array[String] = line.split(" ")
          var element: String = lineElements(2)
          if (element.contains(":")) element = element.substring(0, element.indexOf(":"))
          if (!(element == "/mnt/sdcard")) Some(element) else None
        case _ => None
      }.flatten.map(new File(_)).map(_.getAbsolutePath).toSet
    } else Set.empty
  }
  catch {
    case e: Exception =>
      e.printStackTrace()
      Set.empty
  }

  def `try read /storage directly`: Set[String] =
    OptIf(new File("/storage"))(f => f.exists) map {
      folder => folder.listFiles()
    } map {
      files => files.map(_.getAbsolutePath).toSet
    } getOrElse Set.empty
}
