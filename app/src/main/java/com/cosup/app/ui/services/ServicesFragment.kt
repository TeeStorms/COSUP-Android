package com.cosup.app.ui.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cosup.app.MainActivity
import com.cosup.app.R
import com.cosup.app.data.ServiceDto
import com.cosup.app.databinding.FragmentServicesBinding
import com.cosup.app.databinding.ItemServiceBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.SimpleListAdapter
import com.cosup.app.ui.common.bindService
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.visible

class ServicesFragment : Fragment() {
    private var _b: FragmentServicesBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentServicesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = requireActivity() as MainActivity
        val adapter = SimpleListAdapter<ServiceDto, ItemServiceBinding>(
            ItemServiceBinding::inflate,
            { ib, s -> bindService(ib, s) { main.goTab(R.id.sitesFragment) } },
            { x, y -> x.serviceId == y.serviceId }
        )
        b.list.layoutManager = LinearLayoutManager(requireContext())
        b.list.adapter = adapter
        b.btnRetry.setOnClickListener { vm.refresh() }

        collectFlow(vm.content) { c ->
            b.loading.visible(c.loading)
            b.errorBox.visible(!c.loading && c.error != null)
            b.errorText.text = c.error
            b.list.visible(!c.loading && c.error == null)
            adapter.submitList(c.services)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
