package com.example.twittertest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.twitter_fragment.*

class TwitterFragment : Fragment() {

    val listItems by lazy { mutableListOf(
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L)
    ) }
    val adapter by lazy { TwitterAdapter(listItems, context!!) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.twitter_fragment, null, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycle.layoutManager = LinearLayoutManager(context)
        recycle.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val videoPosition = arguments?.getLong("video_position")
        if (videoPosition != null && videoPosition > 0) {
            val url = arguments?.getString("link")
            val position = arguments?.getInt("position")
            if (!url.isNullOrEmpty()) {
                position?.let { listItems.set(it, Pair(url, videoPosition)) }
                position?.let { adapter.notifyItemChanged(it) }
            }
        }
    }
}