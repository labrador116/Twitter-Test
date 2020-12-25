package com.example.twittertest

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
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
        val streamUri = MediaItem.Builder().setUri(Uri.parse(listUrls[position].first))
        val player = SimpleExoPlayer.Builder(context).build()
        holder.player.player = player
        streamUri.setMimeType(MimeTypes.APPLICATION_MP4)
        val cacheDir = File(context.cacheDir.absolutePath+"/"+position)
        val cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExoDatabaseProvider(context)
        )
        val mediaSource = ProgressiveMediaSource.Factory(
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(defaultDataSourceFactory())
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        ).createMediaSource(streamUri.build())
        player.setMediaSource(mediaSource)
        player.prepare()
        if (listUrls[position].second > 0){
            player.seekTo(listUrls[position].second)
            player.playWhenReady = true
        }

        holder.resize.setOnClickListener {
            context as MainActivity
            val fragment = FullScreenVideoFragment()
            val bundle = Bundle()
            bundle.putString("link", listUrls[position].first)
            bundle.putInt("position", position)
            bundle.putLong("video_position", player.currentPosition)
            fragment.arguments = bundle
            context.supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).addToBackStack(null).commit()
            player.pause()
            cache.release()
        }
    }

    override fun getItemCount(): Int = listUrls.size

    private fun defaultDataSourceFactory(): DefaultDataSourceFactory =
        DefaultDataSourceFactory(context, agent, bandwidthMeter)
}

class TwitterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val player = itemView.findViewById<PlayerView>(R.id.player)
    val resize = itemView.findViewById<ImageView>(R.id.resize_button)
}