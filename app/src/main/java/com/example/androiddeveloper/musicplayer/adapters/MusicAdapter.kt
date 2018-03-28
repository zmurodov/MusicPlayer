package com.example.androiddeveloper.musicplayer.adapters

import android.annotation.SuppressLint
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.androiddeveloper.musicplayer.R
import com.example.androiddeveloper.musicplayer.models.MusicData
import java.io.File

@Suppress("DEPRECATION")
/**
 * Created by Android Developer on 22.03.2018.
 */
class MusicAdapter(val musicDates: MutableList<MusicData>) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    var callBack: CallBack? = null

    interface CallBack {
        fun onItemClick(music: MusicData)
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.bindMusic(musicDates[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent?.context).inflate(R.layout.list_item, parent, false))


    override fun getItemCount(): Int {
        return musicDates.size
    }

    inner class ViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var title: TextView = root.findViewById<TextView>(R.id.title)
        var artist: TextView = root.findViewById<TextView>(R.id.artist)
        var cover: ImageView = root.findViewById(R.id.img)


        @SuppressLint("ResourceAsColor")
        fun bindMusic(music: MusicData) {
            title.text = music.title
            artist.text = music.artist
            Log.d("TAG", "$music.isPlaying  $music.albumCoverPath")

            music.setColor()
            title.setTextColor(music.color)
            artist.setTextColor(music.color)

            itemView.setOnClickListener {
                callBack?.onItemClick(music)
            }

            if (music.albumCoverPath != null)
                cover.setImageURI(Uri.fromFile(File(music.albumCoverPath)))
            else
                cover.setImageResource(R.mipmap.music_player_default_cover)

        }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}