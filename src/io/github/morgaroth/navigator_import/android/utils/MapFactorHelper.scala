package io.github.morgaroth.navigator_import.android.utils

import java.io.File

object MapFactorHelper {

  val path = "/Android/data/com.mapfactor.navigator/files/navigator/routing_points.xml"

  def findMapFactorHomes: List[File] = {
    val available: List[String] = External.readAll.toList
    available.map(new File(_, path)) map { x =>
      println(s"generated path $x"); x
//    } filter { file =>
//      file.exists() && file.canWrite && file.isFile && file.canRead
    }
  }

}
