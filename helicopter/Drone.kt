package com.example.gamebox2d.helicopter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect

class Drone(private val bitmap: Bitmap, var x : Int, var y : Int) {
    val width = bitmap.width
    val height = bitmap.height

    fun update(speed : Int){
        x -= speed
    }

    fun draw(canvas: Canvas){
        canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), null)
    }

    fun getCollisionShape(): Rect{
        return Rect(x, y, x+width, y+height)
    }

}