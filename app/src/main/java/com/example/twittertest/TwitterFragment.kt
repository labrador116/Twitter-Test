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

class TwitterFragment : Fragment() {

    val listItems by lazy {
        mutableListOf(
            VideoItem(
                "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
                "https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg"
            ),
            VideoItem(
                "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
                "https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg"
            ),
            VideoItem(
                "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
                "https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg"
            ),
            VideoItem(
                "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4",
                "https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg"
            ),
            VideoItem(
                "https://i.gifer.com/YHD8.gif",
                "https://file-examples-com.github.io/uploads/2017/10/file_example_JPG_100kB.jpg"
            )
        )
    }
    private val adapter by lazy { context?.let { TwitterAdapter(listItems, it) } }

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
        val layoutManager = LinearLayoutManager(context)
        recycle.layoutManager = layoutManager
        recycle.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 777 && resultCode == Activity.RESULT_OK) {
            val link = data?.getStringExtra(TwitterAdapter.CONTENT_LINK)
            val thumbLink = data?.getStringExtra(TwitterAdapter.THUMB_LINK)
            val position = data?.getIntExtra(TwitterAdapter.ITEM_POSITION, 0)
            val videoPosition = data?.getLongExtra(TwitterAdapter.VIDEO_POSITION, 0)
            if (position != null && !link.isNullOrEmpty() && !thumbLink.isNullOrEmpty() && videoPosition != null) {
                listItems[position] = VideoItem(link, thumbLink, videoPosition)
                adapter?.notifyItemChanged(position)
            }
        }
    }
}