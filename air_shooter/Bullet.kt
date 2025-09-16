package com.example.gamebox2d.air_shooter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import androidx.core.graphics.scale
import com.example.gamebox2d.R


class Bullet(context: Context, var x: Float, var y: Float, var state : Boolean) {
    private val bitmap: Bitmap
    private val speed = 30f
    val width: Int
    val height: Int

    init {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val original = BitmapFactory.decodeResource(context.resources, R.drawable.bullet)


        val scaledWidth = (screenWidth * 0.03f).toInt() // 3% of screen width
        val scaledHeight = (original.height * scaledWidth) / original.width
        bitmap = original.scale(scaledWidth, scaledHeight)

        width = bitmap.width
        height = bitmap.height
    }

    fun update() {
        if (state)
            y -= speed
        else y += speed
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x - width / 2, y, null)
    }

    fun getRect(): RectF {
        if (state)
            return RectF(x - width / 2, y, x + width / 2, y + height)
        else return RectF(x - width / 2, y, x + width / 2, y - height)
    }
}
