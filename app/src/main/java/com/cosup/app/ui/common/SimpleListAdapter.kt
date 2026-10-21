package com.cosup.app.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/** One small RecyclerView adapter reused by the Services, News and Sites lists. */
class SimpleListAdapter<T : Any, B : ViewBinding>(
    private val inflate: (LayoutInflater, ViewGroup, Boolean) -> B,
    private val bind: (B, T) -> Unit,
    sameItem: (T, T) -> Boolean
) : ListAdapter<T, SimpleListAdapter.VH<B>>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = sameItem(oldItem, newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
}) {
    class VH<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH<B> =
        VH(inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH<B>, position: Int) = bind(holder.binding, getItem(position))
}
