package com.cosup.app.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cosup.app.data.NewsDto
import com.cosup.app.databinding.FragmentNewsBinding
import com.cosup.app.databinding.ItemNewsBinding
import com.cosup.app.ui.AppViewModel
import com.cosup.app.ui.common.SimpleListAdapter
import com.cosup.app.ui.common.bindNews
import com.cosup.app.ui.common.collectFlow
import com.cosup.app.ui.common.visible

class NewsFragment : Fragment() {
    private var _b: FragmentNewsBinding? = null
    private val b get() = _b!!
    private val vm: AppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentNewsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SimpleListAdapter<NewsDto, ItemNewsBinding>(
            ItemNewsBinding::inflate,
            { ib, n -> bindNews(ib, n) },
            { x, y -> x.newsId == y.newsId }
        )
        b.list.layoutManager = LinearLayoutManager(requireContext())
        b.list.adapter = adapter
        b.btnRetry.setOnClickListener { vm.refresh() }

        collectFlow(vm.content) { c ->
            b.loading.visible(c.loading)
            b.errorBox.visible(!c.loading && c.error != null)
            b.errorText.text = c.error
            b.empty.visible(!c.loading && c.error == null && c.news.isEmpty())
            b.list.visible(!c.loading && c.error == null)
            adapter.submitList(c.news)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
