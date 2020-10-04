package com.example.simplemusicplayer

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), Runnable, SeekBar.OnSeekBarChangeListener {

    private lateinit var mp: MediaPlayer
    private val handler = Handler()
    private var totalTime: Int = 0
    private lateinit var  audioManager:AudioManager
    private val openRequestCode=1
    private lateinit var uri:Uri

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioManager=getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeBar.max=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeBar.progress=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        if (savedInstanceState == null) {
            initPlayer(null)
        }
    }

    override fun run() {

        updateUI()

        handler.postDelayed(this, 1000)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mp.release()
        handler.removeCallbacks(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initPlayer(uri: Uri?){
        mp = if(uri==null)
            MediaPlayer.create(this, R.raw.music)
        else
            MediaPlayer.create(this, uri)
        mp.isLooping = true
        mp.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        )
        mp.setVolume(1f, 1f)
        totalTime = mp.duration
        updateUI()
        playBar.max = totalTime
        volumeBar.setOnSeekBarChangeListener(this)
        playBar.setOnSeekBarChangeListener(this)
    }

    private fun updateUI() {
        val currentPosition = mp.currentPosition
        TimeGoneLabel.text = createTimeLabel(currentPosition)
        TimeLeftLabel.text = createTimeLabel(totalTime - currentPosition)
    }

    private fun createTimeLabel(time: Int): String {
        var timeLabel = ""
        var min = time / 1000 / 60
        var sec = time / 1000 % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }


    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            if (seekBar == volumeBar) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0)
            } else {
                mp.seekTo(progress)
                updateUI()
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    fun playMusic(v: View) {
        playMusic()
    }

    fun playMusic(){
        if (mp.isPlaying) {
            // Stop
            mp.pause()
            handler.removeCallbacks(this)
            playButton.setBackgroundResource(R.drawable.play)
        } else {
            // Start
            mp.start()
            handler.removeCallbacks(this)
            handler.post(this)
            playButton.setBackgroundResource(R.drawable.stop)
        }
    }

    fun openFile(view: View) {
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type="audio/*"

        startActivityForResult(intent, openRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==openRequestCode){
            if (data != null) {
                uri= data.data!!
                mp.release()
                handler.removeCallbacks(this)
                playMusic()
            }
        }
    }

}
