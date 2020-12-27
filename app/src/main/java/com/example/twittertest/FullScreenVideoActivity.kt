package com.example.twittertest

import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.full_video_activity.*
import java.io.File

class FullScreenVideoActivity : AppCompatActivity() {

    private val bandwidthMeter by lazy { DefaultBandwidthMeter.Builder(this).build() }
    private val agent by lazy { Util.getUserAgent(this, "streamlayer") }
    val player by lazy { SimpleExoPlayer.Builder(this).build() }
    val cache by lazy {
        SimpleCache(
            File(cacheDir?.absolutePath + "/full"),
            LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024),
            ExoDatabaseProvider(this)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.full_video_activity)
    }

    override fun onStart() {
        super.onStart()
        val url = intent.getStringExtra("link")
        val position = intent.getIntExtra("position", 0)
        val videoPosition = intent.getLongExtra("video_position", 0)
        val streamUri = MediaItem.Builder().setUri(Uri.parse(url))
        player_view.player = player
        streamUri.setMimeType(MimeTypes.APPLICATION_MP4)

        val mediaSource = ProgressiveMediaSource.Factory(
            CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(defaultDataSourceFactory())
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        ).createMediaSource(streamUri.build())
        player.setMediaSource(mediaSource)
        player.prepare()
        videoPosition.let { player.seekTo(it) }
        player.playWhenReady = true

        resize_button.setOnClickListener {
            intent.putExtra("video_position", player.currentPosition)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
        cache.release()
    }

    private fun defaultDataSourceFactory(): DefaultDataSourceFactory =
        DefaultDataSourceFactory(this, agent, bandwidthMeter)
}