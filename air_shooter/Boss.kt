
package com.example.gamebox2d.air_shooter

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import com.example.gamebox2d.R

class Boss(private val context: Context, screenWidth: Int) {
    var x = 0f//(screenWidth / 2).toFloat()
    var y = -200f
    private var direction = 8f
    var health = 30
    private val bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.boss)

    val width = bitmap.width
    val height = bitmap.height



   fun update(screenWidth: Int){
        if (y < 100) {
            y += 6 // Entering from top
        } else {
            x += direction
            if (x <= 0 || x + width >= screenWidth) {
                direction *= -1
            }
        }
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, x, y, null)
    }

    fun getRect(): RectF {
        return RectF(x, y, x + width, y + height)
    }

    fun shoot(): List<Bullet> {
        val bullets = mutableListOf<Bullet>()
        val spacing = this.width / 5
        for (i in 1..4) {
            bullets.add(Bullet(context, x + i * spacing, y + height, false))
        }
        return bullets
    }

}