package com.example.androiddeveloper.musicplayer

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.androiddeveloper.musicplayer.adapters.MusicAdapter
import com.example.androiddeveloper.musicplayer.models.MusicData
import com.example.androiddeveloper.musicplayer.models.PAUSE
import com.example.androiddeveloper.musicplayer.models.PLAY
import com.example.androiddeveloper.musicplayer.models.START
import com.example.androiddeveloper.musicplayer.services.MusicService
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity(), MusicAdapter.CallBack, ServiceConnection,
        MusicService.OnStateChangedListener {

    private var data: MutableList<MusicData> = ArrayList()
    private var adapter: MusicAdapter? = null
    private var music: MusicData? = null
    private var lastPosition = -1

    private var serviceIntent: Intent? = null
    private var musicService: MusicService? = null

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        list.layoutManager = LinearLayoutManager(this)

        serviceIntent = Intent(this, MusicService::class.java)


        checkRequestPermission()

        adapter = MusicAdapter(data)
        list.adapter = adapter
        adapter?.callBack = this

        list.layoutManager.findViewByPosition(0)
    }


    override fun onStateChanged(state: Int) {
        Log.d("OnStatChanged", "$state")

        when (state) {
            1 -> {

            }
            0 -> {

            }
            -1 -> {
                ivPlay_music.setImageResource(R.mipmap.music_mini_player_ic_control_play)
            }

        }
    }

    override fun onStart() {
        super.onStart()
        bindService(serviceIntent, this, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService?.onStateChangedListener = null

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        musicService = (service as MusicService.StartBinder).service
        musicService?.onStateChangedListener = this
    }

    override fun onItemClick(music: MusicData) {
        serviceIntent!!.action = START
        if (lastPosition != -1) {
            val lastMusic = data[lastPosition]
            lastMusic.isPlaying = false
            adapter?.notifyItemChanged(lastPosition)
        }
        lastPosition = data.indexOf(music)
        music.isPlaying = true
        adapter?.notifyItemChanged(lastPosition)
        playService(music)
    }


    private fun checkRequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                showRequestPermissionRationale()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 101)
            }
        } else {
            loadData();
        }
    }

    private fun showRequestPermissionRationale() {
        Snackbar.make(root, "Storage permission isn't granted", Snackbar.LENGTH_LONG)
                .setAction("SETTINGS", {
                    openApplicationSettings()
                    Toast.makeText(applicationContext, "Open Permissions and grant the Storage permission", Toast.LENGTH_LONG).show()
                })
    }

    private fun openApplicationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName))
        startActivityForResult(intent, 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadData()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 101) {
            checkRequestPermission()
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun playClick(view: View) {
        if (view.id == R.id.ivPlay_music) {
            val img = view as ImageView
            if (musicService!!.isPlaying()) {
                img.setImageResource(R.mipmap.music_mini_player_ic_control_play)
                serviceIntent!!.action = PAUSE
                startService(music)
            } else {
                if (music == null) {
                    music = data[0]
                    serviceIntent!!.action = START
                    playService(music)
                } else {
                    serviceIntent!!.action = PLAY
                    startService(music)
                }
                img.setImageResource(R.mipmap.music_mini_player_ic_control_pause)
            }
        }
    }

    fun nextClick(view: View) {
        if (view.id == R.id.ivPrev_music) {

        }
    }

    fun prevClick(view: View) {
        if (view.id == R.id.next_music) {

        }
    }

    fun startService(music: MusicData?) {
        serviceIntent?.putExtra("DATA", music)
        startService(serviceIntent)
    }

    fun playService(music: MusicData?) {
        name_music.text = music?.title
        music_artist.text = music?.artist
        this.music = music

        if (music?.albumCoverPath != null)
            play_image.setImageURI(Uri.fromFile(File(music.albumCoverPath)))
        else
            play_image.setImageResource(R.mipmap.music_player_default_cover)

        ivPlay_music.setImageResource(R.mipmap.music_mini_player_ic_control_pause)
        startService(music)
    }

    fun loadData() {
        val cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                val musicPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))

                val albomCursor = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                        MediaStore.Audio.Albums._ID + "=?", arrayOf(album_id.toString()), null)
                var albomCoverPath: String? = null
                if (albomCursor.moveToFirst()) {
                    albomCoverPath = albomCursor.getString(albomCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                }
                albomCursor.close()
                data.add(MusicData(title, artist, musicPath, album_id, albomCoverPath))
            }
            cursor.close();
        }
    }
}
