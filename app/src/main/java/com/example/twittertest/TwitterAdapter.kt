package com.example.twittertest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.exo_controls.view.*
import kotlinx.android.synthetic.main.twitter_item_video.view.*
import java.io.File


class TwitterAdapter(private val listUrls: MutableList<Pair<String, Long>>, private val context: Context) :
    RecyclerView.Adapter<TwitterViewHolder>() {
    private val bandwidthMeter by lazy { DefaultBandwidthMeter.Builder(context).build() }
    private val agent by lazy { Util.getUserAgent(context, "streamlayer") }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwitterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.twitter_item_video,
            parent,
            false
        )
        return TwitterViewHolder(view)
    }

    override fun onBindViewHolder(holder: TwitterViewHolder, position: Int) {

        val player = SimpleExoPlayer.Builder(context).build()
        holder.player.player = player
        val streamUri = MediaItem.Builder().setUri(Uri.parse(listUrls[position].first))
        streamUri.setMimeType(MimeTypes.APPLICATION_MP4)
        val mediaSource = ProgressiveMediaSource.Factory(
            defaultDataSourceFactory()
        ).createMediaSource(streamUri.build())
        player.setMediaSource(mediaSource)
        player.prepare()

        if (listUrls[position].second > 0){
            player.setMediaSource(mediaSource)
            player.prepare()
            player.seekTo(listUrls[position].second)
            player.playWhenReady = true
        }

        holder.resizeButton.setOnClickListener {
            context as MainActivity
            val intent = Intent(context, FullScreenVideoActivity::class.java)
            intent.putExtra("link", listUrls[position].first)
            intent.putExtra("position", position)
            intent.putExtra("video_position", player?.currentPosition)
            context.supportFragmentManager.findFragmentByTag("twitter_fragment")?.startActivityForResult(intent, 777)
            player.pause()
        }
    }

    override fun getItemCount(): Int = listUrls.size

    private fun defaultDataSourceFactory(): DefaultDataSourceFactory =
        DefaultDataSourceFactory(context, agent, bandwidthMeter)
}

class TwitterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val player = itemView.findViewById<PlayerView>(R.id.player)
    val resizeButton = itemView.findViewById<ImageView>(R.id.resize_button)
}