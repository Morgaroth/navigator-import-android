package io.github.morgaroth.navigator_import.android.utils

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import org.scaloid.common._

abstract class FragmentWithAttached extends Fragment with TagUtil {

  type Interface

  implicit def activityContext: Context = getActivity

  var attached: Option[Interface] = _

  override def onAttach(activity: Activity): Unit = {
    super.onAttach(activity)
    attached = tryAttach(activity)
  }

  def tryAttach(activity: Activity) = {
    try Some(activity.asInstanceOf[Interface])
    catch {
      case cc: ClassCastException =>
        error(s"activity $activity must implement FetchGPXFromServerFragmentTrait trait from companion", cc)
        None

    }
  }

  override def onDetach(): Unit = {
    super.onDetach()
    attached = None
  }
}