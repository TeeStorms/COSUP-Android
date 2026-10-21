package com.cosup.app.ui.common

import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.cosup.app.BuildConfig
import com.cosup.app.util.imageUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

fun View.visible(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

fun View.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

val TextView.str: String get() = text?.toString().orEmpty()

/** Loads an image that lives in the website's /images folder; hides the view when there is none. */
fun ImageView.loadRemote(path: String?) {
    val url = imageUrl(BuildConfig.API_BASE_URL, path)
    if (url == null) {
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
        load(url)
    }
}

/** Collects a flow only while the fragment's view is on screen. */
fun <T> Fragment.collectFlow(flow: Flow<T>, block: (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect { block(it) }
        }
    }
}

/** Fills an exposed-dropdown field. options = value to display text. */
fun AutoCompleteTextView.bindOptions(options: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, options.map { it.second }))
    setText(options.firstOrNull { it.first == selected }?.second.orEmpty(), false)
    setOnItemClickListener { _, _, position, _ -> onSelect(options[position].first) }
}
