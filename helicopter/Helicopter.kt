
package com.example.gamebox2d.helicopter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect


class Helicopter(private val image: Bitmap, var x: Int, var y: Int) {
    private var goingUp: Boolean = false
    private val gravity: Int = 1
    private var velocity: Int = 0
    private var isBelowScreen: Boolean = false

    fun update(screenHeight: Int): Boolean {
        if (goingUp) {
            velocity = -15
        } else {
            velocity += gravity
        }

        y += velocity

        if (y < 0) y = 0


        isBelowScreen = y > screenHeight - image.height
        return isBelowScreen
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(image, x.toFloat(), y.toFloat(), null)
    }

    fun goUp() {
        goingUp = true
    }

    fun stopGoingUp() {
        goingUp = false
    }

    fun getCollisionShape(): Rect {
        return Rect(x, y, x + image.width, y + image.height)
    }

    fun reset() {
        y = image.height
        velocity = 0
        goingUp = false
    }

    // New method - reset to middle of screen
    fun resetPosition(screenHeight: Int) {
        y = screenHeight / 2
        velocity = 0
        goingUp = false
    }
}
