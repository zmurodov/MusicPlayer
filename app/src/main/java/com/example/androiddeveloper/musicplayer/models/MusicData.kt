package com.example.androiddeveloper.musicplayer.models

import android.graphics.Color
import java.io.Serializable

/**
 * Created by Android Developer on 22.03.2018.
 */
data class MusicData(val title: String,
                     val artist: String,
                     val data: String,
                     val albumId: Long,
                     var albumCoverPath: String? = null,
                     var isPlaying: Boolean = false) : Serializable {

    var color: Int = Color.rgb(255, 255, 255)

    fun setColor() {
        if (isPlaying) {
            color = Color.rgb(98, 140, 254)
        } else {
            color = Color.rgb(255, 255, 255)
        }
    }

}
