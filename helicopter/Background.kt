package com.example.gamebox2d.helicopter

import android.graphics.Bitmap
import android.graphics.Canvas

class Background(private val bitmap: Bitmap, var x: Int) {
    private val width = bitmap.width
    fun update(speed: Int) {
        x -= speed
        if (x <= -width) {
            x = width - (speed - (width + x))
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
        canvas.drawBitmap(bitmap, (x + width).toFloat(), 0f, null)
    }
}