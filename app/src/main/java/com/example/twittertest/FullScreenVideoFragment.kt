package com.example.twittertest

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.full_video_fragment.*
import java.io.File

class FullScreenVideoFragment : Fragment() {
    private val bandwidthMeter by lazy {
        context?.let {
            DefaultBandwidthMeter.Builder(it).build()
        }
    }

    private val agent by lazy { context?.let { Util.getUserAgent(it, "streamlayer") } }
    val player by lazy { context?.let { SimpleExoPlayer.Builder(it).build() } }
    val cache by lazy {
        SimpleCache(
            File(context?.cacheDir?.absolutePath + "/full"),
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExoDatabaseProvider(context!!) )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.full_video_fragment, null, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString("link")
        val position = arguments?.getInt("position")
        val videoPosition = arguments?.getLong("video_position")
        val streamUri = MediaItem.Builder().setUri(Uri.parse(url))
        player_view.player = player
        streamUri.setMimeType(MimeTypes.APPLICATION_MP4)

        val mediaSource = ProgressiveMediaSource.Factory(
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(defaultDataSourceFactory())
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        ).createMediaSource(streamUri.build())
        player?.setMediaSource(mediaSource)
        player?.prepare()
        videoPosition?.let { player?.seekTo(it) }
        player?.playWhenReady = true

        resize_button.setOnClickListener {
            player?.let { it1 -> arguments?.putLong("video_position", it1.currentPosition) }
            activity?.supportFragmentManager?.findFragmentByTag("twitter_fragment")?.arguments = arguments
            activity?.supportFragmentManager?.findFragmentByTag("twitter_fragment")?.onResume()
            activity?.supportFragmentManager?.popBackStackImmediate()
        }
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        cache.release()
    }

    private fun defaultDataSourceFactory(): DefaultDataSourceFactory =
        DefaultDataSourceFactory(context!!, agent!!, bandwidthMeter)
}