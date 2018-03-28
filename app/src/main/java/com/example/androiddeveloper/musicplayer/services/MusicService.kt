package com.example.androiddeveloper.musicplayer.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.widget.RemoteViews
import com.example.androiddeveloper.musicplayer.*
import com.example.androiddeveloper.musicplayer.models.*
import java.io.File


@Suppress("UNREACHABLE_CODE")
class MusicService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    var onStateChangedListener: OnStateChangedListener? = null
    var binder = StartBinder()

    var music: MusicData? = null
    var mp = MediaPlayer()
    fun isPlaying() = mp.isPlaying

    var nm: NotificationManagerCompat? = null
    var notification: NotificationCompat? = null
    var notificationLayout: RemoteViews? = null
    var stop: PendingIntent? = null
    var start: PendingIntent? = null
    var play: PendingIntent? = null
    var pause: PendingIntent? = null
    var next: PendingIntent? = null
    var prev: PendingIntent? = null


    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    inner class StartBinder : Binder() {
        val service: MusicService
            get() = this@MusicService
    }

    interface OnStateChangedListener {
        fun onStateChanged(state: Int)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("ServiceCast")
    override fun onCreate() {
        super.onCreate()
        mp.setOnCompletionListener(this)
        mp.setOnErrorListener(this)
        mp.setOnPreparedListener(this)

        updateNotification()

//        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManagerCompat?


//        val notificationIntent = Intent(this, MainActivity::class.java)
////        notificationIntent.putExtra("NOTIFICATION", "AAAAAAAAAAAAAAAAAAAAA")
//
//        val pendingIntent = PendingIntent.getService(this, 0,
//                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//
//        notificationLayout = RemoteViews(this.packageName, R.layout.notification_main)
//
//
//        val notification = Notification.Builder(applicationContext)
//                .setSmallIcon(R.mipmap.music_player_default_cover)
//                .setContentTitle("My Awesome App")
//                .setContentText("Doing some work...")
//                .setContentIntent(pendingIntent)
//                .setPriority(Notification.PRIORITY_MAX)
//                .build()
//
//        notification.contentView = notificationLayout
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            notification.bigContentView = notificationLayout
//        }
//
//        startForeground(101, notification)

        Log.d("TAG", "OnCreate")
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null && intent.action != null) {
            if (intent.action == START) {
                music = intent.getSerializableExtra("DATA") as MusicData
                Log.d("music", music.toString())
                mp.reset()
                mp.setDataSource(music?.data)
                mp.prepare()
                mp.start()
                play()
//                updateNotification()
            } else if (intent.action == PLAY) {
                mp.start()
            } else if (intent.action == PAUSE) {
                mp.pause()
            }

        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mp.stop()
        music?.isPlaying = false
        stop()
    }


    override fun onPrepared(mp: MediaPlayer?) {

    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        stop()
        return false
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stop()
    }


    fun play() {
        onStateChangedListener?.onStateChanged(1)
    }

    fun pause() {
        onStateChangedListener?.onStateChanged(0)
    }

    fun stop() {
        onStateChangedListener?.onStateChanged(-1)
    }

    fun updateNotification() {
        if (notificationLayout == null) {
//            val openIntent = Intent(this, MainActivity::class.java)
//            val open = PendingIntent.getActivity(this, 0, openIntent, 0)

            val closeIntent = Intent(this, MainActivity::class.java)
            closeIntent.action = CLOSE
            val close = PendingIntent.getService(this, 0, closeIntent, 0)

            val startIntent = Intent(this, MainActivity::class.java)
            startIntent.action = START
            start = PendingIntent.getService(this, 0, startIntent, 0)

            val stopIntent = Intent(this, MainActivity::class.java)
            stopIntent.action = STOP
            stop = PendingIntent.getService(this, 0, stopIntent, 0)

            notificationLayout = RemoteViews(packageName, R.layout.notification_main)
//            notificationLayout!!.setOnClickPendingIntent(R.id.nRoot, open)
            notificationLayout!!.setOnClickPendingIntent(R.id.nClose, close)
        }

        notificationLayout!!.setTextViewText(R.id.nTitle, music?.title)
        notificationLayout!!.setTextViewText(R.id.nArtist, music?.artist)
//        notificationLayout!!.setImageViewUri(R.id.nImage, Uri.fromFile(File(music?.albumCoverPath)))


        if (mp.isPlaying) {
//            val platIntent = Intent(this, MainActivity::class.java)
//            platIntent.action = PAUSE
//            notificationLayout!!.setOnClickPendingIntent(R.id.nPlay, play)
//            notificationLayout!!.setImageViewUri(R.id.nPlay)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.putExtra("NOTIFICATION", "AAAAAAAAAAAAAAAAAAAAA")

        val pendingIntent = PendingIntent.getService(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
//

        val notification = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.mipmap.icon)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_MAX)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setCustomBigContentView(notificationLayout)
                .build()

        startForeground(101, notification)

    }

}
