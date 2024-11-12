package com.example.lab2

import android.graphics.Bitmap

data class Track(
    var title: String,
    val filePath: String,
    var artist: String = "Unknown Artist",
    var album: String = "Unknown Album",
    val art: ByteArray? = null
)
