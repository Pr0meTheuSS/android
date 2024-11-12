package com.example.lab2

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private val tracks: List<Track>,
    private val onPlayPauseClicked: (Track, Button) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        Log.println(Log.DEBUG, "TrackAdapter", "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        Log.println(Log.DEBUG, "TrackAdapter", "onBindViewHolder")
        val track = tracks[position]
        holder.trackTitle.text = track.title
        holder.artist.text = track.artist
        holder.album.text = track.album

        val bitmap = track.art?.let { BitmapFactory.decodeByteArray(track.art, 0, it.size) }
        holder.art.setImageBitmap(bitmap)

        holder.playPauseButton.setOnClickListener {
            onPlayPauseClicked(track, holder.playPauseButton)
        }
    }

    override fun getItemCount() : Int {
        Log.println(Log.DEBUG, "TrackAdapter", "getItemCount")
        return tracks.size
    }

    inner class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val trackTitle: TextView = itemView.findViewById(R.id.trackTitle)
        val playPauseButton: Button = itemView.findViewById(R.id.playPauseButton)
        val artist: TextView = itemView.findViewById(R.id.trackArtist)
        val album: TextView = itemView.findViewById(R.id.trackAlbum)
        val art: ImageView = itemView.findViewById(R.id.albumArt)
    }
}