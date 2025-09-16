package com.example.gamebox2d

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gamebox2d.air_shooter.GameView

class MainActivitySpaceShooter : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gameView: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("gameBox2d",MODE_PRIVATE)
        val state = sharedPreferences.getInt("music", 0) <= 1
        gameView = GameView(this, state, sharedPreferences)

        setContentView(gameView)
    }

    override fun onResume() {
        super.onResume()
        gameView.resume()
    }

    override fun onPause() {
        super.onPause()
        gameView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameView.destroy()
    }
}