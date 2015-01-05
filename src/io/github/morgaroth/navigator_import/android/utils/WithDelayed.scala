package io.github.morgaroth.navigator_import.android.utils

import android.os.Handler

trait WithDelayed {
  type Millis = Long
  val handler = new Handler()

  def delayed[S](f: => S, delay: Millis): Unit = {
    handler.postDelayed(new Runnable {
      override def run(): Unit = f
    }, delay)
  }
}
