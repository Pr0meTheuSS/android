package com.example.lab2

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi

data class TrackMetadata(
    val artist: String = "Unknown Artist",
    val title: String = "Unknown Title",
    val album: String = "Unknown Album",
    val albumArt: ByteArray? = null
)

class Mp3MetadataParser(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.P)
    fun parseFile(uri: Uri): TrackMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            TrackMetadata(
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist",
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "Unknown Title",
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album",
                albumArt = retriever.embeddedPicture
            )
        } catch (e: Exception) {
            e.printStackTrace()
            TrackMetadata()
        } finally {
            retriever.release()
        }
    }
}
