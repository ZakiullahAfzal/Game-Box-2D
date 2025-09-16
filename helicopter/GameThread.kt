package com.example.gamebox2d.helicopter

import android.graphics.Canvas
import android.view.SurfaceHolder
import com.example.gamebox2d.GameState


class GameThread(private val holder: SurfaceHolder, private val gameView: GameView) : Thread() {
    var running: Boolean = false

    // private val targetFPS = 60
    private val targetTime = 17
    var gameState = GameState.PLAYING

    override fun run() {
        var startTime: Long
        var timeMillis: Long
        var waitTime: Long

        while (running) {
            startTime = System.nanoTime()
            var canvas: Canvas? = null

            try {
                canvas = holder.lockCanvas()
                synchronized(holder) {
                    if(gameState == GameState.PAUSE){
                        gameView.drawPause(canvas)
                    }else if (gameState == GameState.PLAYING){
                        gameView.update()
                        gameView.draw(canvas)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            timeMillis = (System.nanoTime() - startTime) / 1000000
            waitTime = targetTime - timeMillis

            try {
                if (waitTime > 0) {
                    sleep(waitTime)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }



        }
    }
}
