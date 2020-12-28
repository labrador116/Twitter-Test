package com.example.twittertest

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import java.io.File


data class VideoItem(val urlContent: String, val urlThumb: String, val videoPosition: Long = 0)

class TwitterAdapter(private val listUrls: MutableList<VideoItem>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val CONTENT_LINK = "link"
        const val THUMB_LINK = "thumb_link"
        const val ITEM_POSITION = "position"
        const val VIDEO_POSITION = "video_position"
    }

    private val bandwidthMeter by lazy { DefaultBandwidthMeter.Builder(context).build() }
    private val agent by lazy { Util.getUserAgent(context, "streamlayer") }

    override fun getItemViewType(position: Int): Int =
        if (listUrls[position].urlContent.endsWith(".gif")) {
            1
        } else 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.twitter_item_gif,
                parent,
                false
            )
            TwitterGifViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.twitter_item_video,
                parent,
                false
            )
            TwitterVideoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TwitterVideoViewHolder -> {
                val player = SimpleExoPlayer.Builder(context).build()
                val streamUri = MediaItem.Builder().setUri(Uri.parse(listUrls[position].urlContent))
                streamUri.setMimeType(MimeTypes.APPLICATION_MP4)
                val cacheDir = File(context.cacheDir.absolutePath + "/" + position)
                val cache = SimpleCache(
                    cacheDir,
                    LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
                    ExoDatabaseProvider(context)
                )
                val mediaSource = when {
                    listUrls[position].urlContent.endsWith(".m3u8") -> {
                        streamUri.setMimeType(MimeTypes.APPLICATION_M3U8)
                        HlsMediaSource.Factory(defaultDataSourceFactory())
                            .createMediaSource(streamUri.build())
                    }
                    else -> {
                        streamUri.setMimeType(MimeTypes.APPLICATION_MP4)
                        ProgressiveMediaSource.Factory(
                            CacheDataSource.Factory().setCache(cache)
                                .setUpstreamDataSourceFactory(defaultDataSourceFactory())
                                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                        ).createMediaSource(streamUri.build())
                    }
                }
                player.setMediaSource(mediaSource)
                player.prepare()

                if (listUrls[position].videoPosition > 0) {
                    holder.player.player = player
                    holder.startPlayerBtn.visibility = View.GONE
                    player.seekTo(listUrls[position].videoPosition)
                    player.playWhenReady = true
                } else {
                    Glide.with(context).load(listUrls[position].urlThumb)
                        .into(object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                holder.thumbImage.setImageDrawable(resource)
                                holder.thumbImage.visibility = View.VISIBLE
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                        })
                }

                holder.itemView.viewTreeObserver.addOnScrollChangedListener {
                    if (!holder.itemView.isShown) {
                        player.pause()
                    }
                }

                holder.startPlayerBtn.setOnClickListener {
                    holder.startPlayerBtn.visibility = View.GONE
                    holder.player.player = player
                    player.playWhenReady = true
                }

                holder.resizeButton.setOnClickListener {
                    context as MainActivity
                    val intent = Intent(context, FullScreenVideoActivity::class.java)
                    intent.putExtra(CONTENT_LINK, listUrls[position].urlContent)
                    intent.putExtra(THUMB_LINK, listUrls[position].urlThumb)
                    intent.putExtra(ITEM_POSITION, position)
                    intent.putExtra(VIDEO_POSITION, player?.currentPosition)
                    context.supportFragmentManager.findFragmentByTag("twitter_fragment")
                        ?.startActivityForResult(intent, 777)
                    player.pause()
                    cache.release()
                }
            }
            is TwitterGifViewHolder -> {
                Glide.with(context).load(listUrls[position].urlContent).into(holder.gifView)
                holder.gifView.setOnClickListener {
                    holder.gifView.visibility = View.INVISIBLE
                    holder.playGifBtn.visibility = View.VISIBLE
                }
                holder.playGifBtn.setOnClickListener {
                    holder.playGifBtn.visibility = View.GONE
                    holder.gifView.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun getItemCount(): Int = listUrls.size

    private fun defaultDataSourceFactory(): DefaultDataSourceFactory =
        DefaultDataSourceFactory(context, agent, bandwidthMeter)
}

class TwitterVideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val player = itemView.findViewById<PlayerView>(R.id.player)
    val resizeButton = itemView.findViewById<ImageView>(R.id.resize_button)
    val thumbImage = itemView.findViewById<ImageView>(R.id.exo_artwork)
    val startPlayerBtn = itemView.findViewById<ImageView>(R.id.start_play)
}

class TwitterGifViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val gifView = itemView.findViewById<ImageView>(R.id.gif_view)
    val playGifBtn = itemView.findViewById<ImageView>(R.id.play_gif)
}