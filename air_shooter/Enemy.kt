package com.example.gamebox2d.air_shooter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import kotlin.random.Random
import androidx.core.graphics.scale
import com.example.gamebox2d.R

class Enemy(context: Context, screenWidth: Int) {
    private val bitmap: Bitmap
    var x: Float
    var y: Float
    private val speed: Int
    private var width : Int = 0
    private var height : Int = 0

    init {
        val original = BitmapFactory.decodeResource(context.resources, R.mipmap.enemy)

        val scaleFactor = 0.17f // 17% of screen width
        val scaledWidth = (screenWidth * scaleFactor).toInt()
        val scaledHeight = (original.height * scaledWidth) / original.width
        bitmap = original.scale(scaledWidth, scaledHeight)

        x = Random.nextInt(0, screenWidth - bitmap.width).toFloat()
        y = -bitmap.height.toFloat()
        speed = Random.nextInt(5, 12)
        width = bitmap.width;
        height = bitmap.height
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

    fun getWidth(): Int{
        return this.width;
    }

    fun getHeight(): Int{
        return this.height;
    }
}

