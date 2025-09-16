package com.example.gamebox2d.air_shooter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import com.example.gamebox2d.R
import java.util.Random
import androidx.core.graphics.scale

enum class RewardType {
    HEALTH, SCORE, ENERGY
}

class Reward(val context: Context, screenWidth: Int, private val screenHeight: Int) {
    var x: Float
    var y: Float = -100f
    val speed = 10f
    val bitmap: Bitmap
    val type: RewardType = RewardType.entries.random()

    init {
        loadBitmaps(context)

        bitmap = when (type) {
            RewardType.HEALTH -> healthBitmap!!
            RewardType.SCORE -> scoreBitmap!!
            RewardType.ENERGY -> energyBitmap!!
        }

        x = Random().nextInt(screenWidth - bitmap.width).toFloat()
    }

    fun update() {
        y += speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }

    fun getRect(): RectF {
        return RectF(x, y, x + bitmap.width, y + bitmap.height)
    }

    fun isOffScreen(): Boolean {
        return y > screenHeight
    }

    companion object {
        private var healthBitmap: Bitmap? = null
        private var scoreBitmap: Bitmap? = null
        private var energyBitmap: Bitmap? = null

        fun loadBitmaps(context: Context) {
            if (healthBitmap == null) {
                val size = 100
                healthBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.health)
                    .scale(size, size)
                scoreBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.score)
                    .scale(size, size)
                energyBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.energy)
                    .scale(size, size)
            }
        }
    }
}
