package com.cosup.app.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cosup.app.databinding.FragmentAboutBinding

/** Static page: all the text lives in fragment_about.xml. */
class AboutFragment : Fragment() {
    private var _b: FragmentAboutBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentAboutBinding.inflate(inflater, container, false)
        return _b!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
