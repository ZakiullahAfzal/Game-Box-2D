package com.example.gamebox2d

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gamebox2d.databinding.ActivityMainGamePlayListBinding
import kotlin.system.exitProcess

class MainActivityGamePlayList : AppCompatActivity() {

    private lateinit var binding: ActivityMainGamePlayListBinding
    private lateinit var sharedPreferences: SharedPreferences

    private var backgroundMusic : MediaPlayer? = null
    private var isPlay : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainGamePlayListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("gameBox2d", MODE_PRIVATE)

        backgroundMusic = MediaPlayer.create(this, R.raw.backgroundmusich)
        backgroundMusic?.isLooping = true
        backgroundMusic?.setVolume(0.5f, 0.5f)

        val music = sharedPreferences.getInt("music", 0)
        if (music <= 1) {
           // sharedPreferences.edit { putInt("music", 2).apply() }
            binding.imageViewSetting.setImageResource(R.drawable.music)
            backgroundMusic?.start()
            isPlay = true
        } else {
           // sharedPreferences.edit { putInt("music", 1).apply() }
            binding.imageViewSetting.setImageResource(R.drawable.mutemusic)
            backgroundMusic?.pause()
            isPlay = false
        }


        binding.imageViewSetting.setOnClickListener {
            if (isPlay) {
                sharedPreferences.edit { putInt("music", 2).apply() }
                binding.imageViewSetting.setImageResource(R.drawable.mutemusic)
                backgroundMusic?.pause()
                isPlay = false
            } else {
                sharedPreferences.edit { putInt("music", 1).apply() }
                binding.imageViewSetting.setImageResource(R.drawable.music)
                backgroundMusic?.seekTo(0)
                backgroundMusic?.start()
                isPlay = true
            }
        }


        binding.textViewExit.setOnClickListener {
            sharedPreferences.edit{putBoolean("p1", false).apply()}
            sharedPreferences.edit{putBoolean("p2", false).apply()}
            exitProcess(0)
        }

        val activities = arrayOf(MainActivityNumberAction::class.java,
            MainActivityHelicopter::class.java,
            MainActivityMissingNumber::class.java,
            MainActivitySpaceShooter::class.java,
            TicTocToe::class.java)

        val imageArray = arrayOf(binding.imageViewNumberAction,
            binding.imageViewHelicopter,
            binding.imageViewMissingNumber,
            binding.imageViewAirPlane,
            binding.imageViewTicTocToe)

        activities.indices.forEach { i ->
            imageArray[i].setOnClickListener {
                backgroundMusic?.pause()
                startActivity(Intent(this, activities[i]))
            }
        }

    }

    override fun onResume() {
        super.onResume()
        updateChange()
        if (isPlay) {
            backgroundMusic?.start()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateChange(){

        val coin = sharedPreferences.getInt("coin", 0)
        if(coin == 0)  binding.textViewCoin.text = "0"
        else binding.textViewCoin.text = coin.toString()

        val stateOne = sharedPreferences.getBoolean("p1", false)
        val stateTow =  sharedPreferences.getBoolean("p2",false)
        if (stateOne){
           val int = binding.textViewPlayerOneScore.text.toString().toInt()
           binding.textViewPlayerOneScore.text = (int + 1).toString()
            sharedPreferences.edit{putBoolean("p1", false).apply()}
        }else
        if (stateTow){
            val int = binding.textViewPlayerTwoScore.text.toString().toInt()
            binding.textViewPlayerTwoScore.text = (int + 1).toString()
            sharedPreferences.edit{putBoolean("p2", false).apply()}
        }

    }

    override fun onPause() {
        super.onPause()
        backgroundMusic?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundMusic?.release()
        backgroundMusic = null
    }


}