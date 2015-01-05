package io.github.morgaroth.navigator_import.android

import android.os.Bundle
import android.view.{Gravity, LayoutInflater, View, ViewGroup}
import io.github.morgaroth.navigator_import.android.StartFragment.StartFragmentTrait
import io.github.morgaroth.navigator_import.android.utils.FragmentWithAttached
import org.scaloid.common.{SButton, SFrameLayout}

object StartFragment {

  trait StartFragmentTrait {
    def userWants: Unit
  }

}

class StartFragment extends FragmentWithAttached {
  override type Interface = StartFragmentTrait

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    new SFrameLayout {
      SButton("Scan QR Code").<<.Gravity(Gravity.CENTER).wrap.>>.onClick(attached.map(_.userWants))
    }
  }
}


