package com.example.twittertest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.twitter_fragment.*
import kotlinx.android.synthetic.main.twitter_item_video.*

class TwitterFragment : Fragment() {

    val listItems by lazy { mutableListOf(
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4", 0L),
        Pair("https://i.gifer.com/YHD8.gif", 0L)
    ) }
    val adapter by lazy { TwitterAdapter(listItems, context!!) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 777 && resultCode == Activity.RESULT_OK){
            val link = data?.getStringExtra("link")
            val position = data?.getIntExtra("position", 0)
            val videoPosition = data?.getLongExtra("video_position", 0)
            if (position != null && !link.isNullOrEmpty() && videoPosition != null) {
                listItems[position] = Pair(link, videoPosition)
                adapter.notifyItemChanged(position)
            }
        }
    }
}