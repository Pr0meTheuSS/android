package com.example.lab2

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi

class MainActivity : Activity() {

    private var mediaPlayer: MediaPlayer? = null
    private var currentlyPlayingTrack: Track? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var metadataParser: Mp3MetadataParser
    private var prevPlayPauseButton: Button? = null
    private val handler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        metadataParser = Mp3MetadataParser(this)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            loadTracks()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun loadTracks() {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val tracks = mutableListOf<Track>()

        musicDir?.listFiles()?.forEach { file ->
            if (file.extension == "mp3") {
                val uri = Uri.fromFile(file)
                val metadata = metadataParser.parseFile(uri)

                val track = Track(
                    title = metadata.title,
                    filePath = file.path,
                    artist = metadata.artist,
                    album = metadata.album,
                    art = metadata.albumArt
                )
                tracks.add(track)
            }
        }
        recyclerView.adapter = TrackAdapter(tracks) { track, button ->
            playPauseTrack(track, button)
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(5)
    }

    private fun playPauseTrack(track: Track, button: Button) {
        if (mediaPlayer?.isPlaying == true && track == currentlyPlayingTrack) {
            mediaPlayer?.pause()
            button.text = "Play"
        } else {
            mediaPlayer?.release()
            prevPlayPauseButton?.text = "Play"
            handler.removeCallbacksAndMessages(null)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(track.filePath)
                prepare()
                start()
            }
            currentlyPlayingTrack = track
            button.text = "Pause"
            prevPlayPauseButton = button
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}
