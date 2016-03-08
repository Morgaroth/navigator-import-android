package io.github.morgaroth.navigator_import.android

import io.github.morgaroth.navigator_import.android.StartFragment.StartFragmentTrait
import io.github.morgaroth.navigator_import.android.utils.FragmentWithAttached

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


