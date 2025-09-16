package com.example.gamebox2d.helicopter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaPlayer
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.gamebox2d.GameState
import com.example.gamebox2d.R
import kotlinx.coroutines.Runnable
import kotlin.random.Random

@SuppressLint("ViewConstructor")
class GameView(context: Context, play: Boolean, private var sharedPreferences: SharedPreferences) : SurfaceView(context), SurfaceHolder.Callback{

    private var gameThread: GameThread
    private var helicopter: Helicopter
    private var buildings: ArrayList<Building>
    private var coins: ArrayList<Coin>
    private var drone: ArrayList<Drone>
    private var background1: Background
    private var background2: Background
    private var paint: Paint
    private var textPaint: Paint
    private var coinPaint: Paint
    private var score: Int = 0
    private var gameOver: Boolean = false
    private var gameStarted: Boolean = false
    private var buildingSpeed: Int = 15
    private var coinSpeed: Int = 15
    private var lastBuildingTime: Long = 0
    private var lastCoinTime: Long = 0
    private var buildingInterval: Long = 2000
    private var coinInterval: Long = 2000
    private var droneInterval: Long = 10000
    private var lastDroneTime: Long = 0


    // Speed control
    private val baseBuildingSpeed = 15
    private val baseCoinSpeed = 15
    private var speedIncreaseFactor = 1.0f
    private var lastSpeedIncreaseScore = 0
    private var baseDroneSpeed: Long = 20

    // Building dimensions
    private val smallBuildingWidth = 300
    private val smallBuildingHeight = 400
    private val mediumBuildingWidth = 350
    private val mediumBuildingHeight = 550
    private val largeBuildingWidth = 400
    private val largeBuildingHeight = 700

    // Bitmaps
    private lateinit var helicopterBitmap: Bitmap
    private lateinit var smallBuildingBitmap: Bitmap
    private lateinit var mediumBuildingBitmap: Bitmap
    private lateinit var largeBuildingBitmap: Bitmap
    private lateinit var coinBitmap: Bitmap
    private lateinit var explosionBitmap: Bitmap
    private lateinit var backgroundBitmap: Bitmap
    private var healthBitmap: Bitmap
    private var scoreCount: Bitmap
    private var droneBitmap: Bitmap

    // Game Sounds
    private var helicopterSound: MediaPlayer
    private var backgroundMusic: MediaPlayer
    private var coinSound: MediaPlayer
    private var explosion: MediaPlayer
    private var playMusic: Boolean = false

    // Buttons (game over screen)
    private val exitRect = RectF()

    // Add this at the top of the class
    private var releaseUsed = false
    private val releaseRect = RectF()

    // Pause Button & Text
    private val pauseRect = RectF()
    private val resume = RectF()
    private var isPaused = false
    private var pauseBitmap: Bitmap
    private lateinit var resumeBitmap: Bitmap


    private var money: Int = 0
    private var coinsFromScore: Int = 0

    private var playerHealth: Int = 3
    private var healthSize: Int = 64


    init {
        holder.addCallback(this)
        gameThread = GameThread(holder, this)
        initializeBitmaps()

        helicopter = Helicopter(helicopterBitmap.scale(250, 180, true), 100, 0)

        healthBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.health)
        scoreCount = BitmapFactory.decodeResource(context.resources, R.drawable.score)
        pauseBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pause_button)
        droneBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.drone)


        buildings = ArrayList()
        coins = ArrayList()
        background1 = Background(createNicePlaceholderBitmap(1, 1, Color.BLUE, "BG"), 0)
        background2 = Background(createNicePlaceholderBitmap(1, 1, Color.BLUE, "BG"), 0)

        drone = ArrayList()


        paint = Paint()
        textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
        }

        coinPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
        }


        money = sharedPreferences.getInt("coin", 0)

        helicopterSound = MediaPlayer.create(context, R.raw.helicoptersound)
        helicopterSound.isLooping = true

        explosion = MediaPlayer.create(context, R.raw.explosion)

        backgroundMusic = MediaPlayer.create(context, R.raw.backgroundmusich)
        playMusic = play
        if (playMusic) {
            backgroundMusic.isLooping = true
            backgroundMusic.setVolume(0.5f, 0.5f)
        }

        coinSound = MediaPlayer.create(context, R.raw.coinsound)

    }

    private fun createBuildingBitmap(width: Int, height: Int, color: Int, windows: Int): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)

        val gradient = LinearGradient(
            0f, 0f, 0f, height.toFloat(), color, Color.rgb(
                (Color.red(color) * 0.7).toInt(),
                (Color.green(color) * 0.7).toInt(),
                (Color.blue(color) * 0.7).toInt()
            ), Shader.TileMode.CLAMP
        )

        val buildingPaint = Paint().apply {
            shader = gradient
            style = Paint.Style.FILL
        }

        val buildingRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(buildingRect, 30f, 30f, buildingPaint)

        val windowPaint = Paint().apply {
            this.color = Color.YELLOW
            style = Paint.Style.FILL
        }

        val windowWidth = width / (windows + 2)
        val windowHeight = height / 12
        val windowPadding = width / 20

        for (i in 1..windows) {
            for (j in 1..6) {
                val left = windowPadding + (i - 1) * (windowWidth + windowPadding)
                val top = windowPadding + (j - 1) * (windowHeight + windowPadding)
                canvas.drawRoundRect(
                    left.toFloat(),
                    top.toFloat(),
                    (left + windowWidth).toFloat(),
                    (top + windowHeight).toFloat(),
                    10f,
                    10f,
                    windowPaint
                )
            }
        }

        val roofPaint = Paint().apply {
            this.color = Color.rgb(
                (Color.red(color) * 0.5).toInt(),
                (Color.green(color) * 0.5).toInt(),
                (Color.blue(color) * 0.5).toInt()
            )
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height / 20f, roofPaint)

        return bitmap
    }

    private fun createNicePlaceholderBitmap(width: Int, height: Int, color: Int, text: String): Bitmap {
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val textPaint = Paint().apply {
            this.color = Color.BLACK
            textSize = minOf(width, height) / 5f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.drawText(text, width / 2f, height / 2f, textPaint)

        return bitmap
    }

    private fun initializeBitmaps() {
        try {
            helicopterBitmap = decodeSampledBitmapFromResource(R.drawable.helicopter, 200, 120)!!
            smallBuildingBitmap = decodeSampledBitmapFromResource(
                R.drawable.pos,
                smallBuildingWidth,
                smallBuildingHeight
            ) ?: createBuildingBitmap(smallBuildingWidth, smallBuildingHeight, Color.GREEN, 3)
            mediumBuildingBitmap = decodeSampledBitmapFromResource(
                R.drawable.p2m,
                mediumBuildingWidth,
                mediumBuildingHeight
            ) ?: createBuildingBitmap(mediumBuildingWidth, mediumBuildingHeight, Color.BLUE, 5)
            largeBuildingBitmap = decodeSampledBitmapFromResource(
                R.drawable.p3l,
                largeBuildingWidth,
                largeBuildingHeight
            ) ?: createBuildingBitmap(largeBuildingWidth, largeBuildingHeight, Color.MAGENTA, 7)
            coinBitmap = decodeSampledBitmapFromResource(R.drawable.score, 80, 80)
                ?: createNicePlaceholderBitmap(80, 80, Color.YELLOW, "Coin")
            // explosionBitmap = decodeSampledBitmapFromResource(R.drawable.explosion, 200, 200)
            //   ?: createNicePlaceholderBitmap(200, 200, Color.RED, "Boom!")
            backgroundBitmap = decodeSampledBitmapFromResource(R.drawable.background, 1200, 1200)
                ?: createNicePlaceholderBitmap(1200, 1200, Color.CYAN, "Sky")

           // pauseBitmap = decodeSampledBitmapFromResource(R.drawable.pause_button, 100, 100)
             //   ?: createNicePlaceholderBitmap(100, 100, Color.LTGRAY, "⏸")

            resumeBitmap = decodeSampledBitmapFromResource(R.drawable.resume_button, 100, 100)
                ?: createNicePlaceholderBitmap(100, 100, Color.LTGRAY, "▶")


        } catch (e: Exception) {
            e.printStackTrace()
            helicopterBitmap = createNicePlaceholderBitmap(200, 120, Color.RED, "Heli")
            smallBuildingBitmap =
                createBuildingBitmap(smallBuildingWidth, smallBuildingHeight, Color.GREEN, 3)
            mediumBuildingBitmap =
                createBuildingBitmap(mediumBuildingWidth, mediumBuildingHeight, Color.BLUE, 5)
            largeBuildingBitmap =
                createBuildingBitmap(largeBuildingWidth, largeBuildingHeight, Color.MAGENTA, 7)
            coinBitmap = createNicePlaceholderBitmap(80, 80, Color.YELLOW, "Coin")
            explosionBitmap = createNicePlaceholderBitmap(200, 200, Color.RED, "Boom!")
            backgroundBitmap = createNicePlaceholderBitmap(1200, 1200, Color.CYAN, "Sky")
        }
    }

    private fun decodeSampledBitmapFromResource(resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resources.openRawResource(resId).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            resources.openRawResource(resId).use { stream ->
                BitmapFactory.decodeStream(stream, null, options)?.let { originalBitmap ->
                    if (originalBitmap.width != reqWidth || originalBitmap.height != reqHeight) {
                        originalBitmap.scale(reqWidth, reqHeight, true)
                    } else {
                        originalBitmap
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val scaledBackground = backgroundBitmap.scale(width, height, true)
        background1 = Background(scaledBackground, 0)
        background2 = Background(scaledBackground, width)
        helicopter.y = height / 2
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!gameStarted) {
                    if (pauseRect.contains(event.x, event.y)) {
                        /*isPaused = !isPaused
                        Log.e("msg", "Presed")
                        if (isPaused) {
                            pause()
                        } else {
                            resume()
                        } */
                        gameThread.gameState = GameState.PAUSE
                        return true
                    }else if(resume.contains(event.x, event.y)){
                         gameThread.gameState = GameState.PLAYING
                    }else
                    if (exitRect.contains(event.x, event.y)) {
                        sharedPreferences.edit { putInt("coin", money).apply() }
                        (context as? Activity)?.finish()
                        return true
                    } else {
                        gameStarted = true
                    }
                } else if (gameOver) {
                    if (exitRect.contains(event.x, event.y)) {
                        sharedPreferences.edit { putInt("coin", money).apply() }
                        (context as? Activity)?.finish()
                        return true
                    } else if (!releaseUsed && releaseRect.contains(
                            event.x,
                            event.y
                        ) && money >= 50
                    ) {
                        // Release button pressed
                        money -= 50
                        sharedPreferences.edit { putInt("coin", money).apply() }
                        releaseUsed = true
                        gameOver = false
                        gameStarted = true
                        playerHealth = 1
                        buildings.clear()
                        coins.clear()
                        helicopter.resetPosition(height)
                        lastBuildingTime = System.currentTimeMillis()
                        lastCoinTime = System.currentTimeMillis()
                        resume()
                        return true
                    } else if (!releaseRect.contains(event.x, event.y)) {
                        sharedPreferences.edit { putInt("coin", money).apply() }
                        resetGame()
                    }
                    return true
                }
                helicopter.goUp()
                return true
            }

            MotionEvent.ACTION_UP -> {
                helicopter.stopGoingUp()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // Update Method ===============================================================//
    fun update() {
        if (!gameStarted || gameOver || isPaused) return

        updateGameSpeed()

        if (helicopter.update(height)) {
            handleCollision()
            return
        }

        background1.update(buildingSpeed)
        background2.update(buildingSpeed)

        if (System.currentTimeMillis() - lastBuildingTime > buildingInterval) {
            if (buildings.isEmpty() || buildings.last().x < width - 600) {
                addBuilding()
                lastBuildingTime = System.currentTimeMillis()
            }
        }

        if (System.currentTimeMillis() - lastCoinTime > coinInterval) {
            addSafeCoin()
            lastCoinTime = System.currentTimeMillis()
        }

        if (System.currentTimeMillis() - lastDroneTime > droneInterval + 10000) {
            drone.add(Drone(droneBitmap.scale(100, 50), width, Random.nextInt(10,100)))
            lastDroneTime = System.currentTimeMillis()
        }

        for (building in buildings) {
            building.update(buildingSpeed)
            if (Rect.intersects(helicopter.getCollisionShape(), building.getCollisionShape())) {
                handleCollision()
                explosion.start()
            }
        }

        buildings.removeAll { building -> building.x + building.width < 0 }

        val droneIterator = drone.iterator()
        while (droneIterator.hasNext()) {
            val drone = droneIterator.next()
            drone.update(15)
            if (Rect.intersects(helicopter.getCollisionShape(), drone.getCollisionShape())) {
                playerHealth--
                droneIterator.remove()
                explosion.start()
                if (playerHealth == 0) gameOver = true
            }

            if (drone.x + drone.width < 0) droneIterator.remove()
        }


        val iterator = coins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            coin.update(coinSpeed)

            if (Rect.intersects(helicopter.getCollisionShape(), coin.getCollisionShape())) {
                score += 10
                if (score % 10 == 0) {
                    coinsFromScore++
                    money++
                }
                iterator.remove()
                playCoinSound()
                continue
            }

            if (coin.x + coin.width < 0) {
                iterator.remove()
            }
        }
    }

    private fun updateGameSpeed() {
        val speedIncreaseInterval = 100
        if (score >= lastSpeedIncreaseScore + speedIncreaseInterval) {
            lastSpeedIncreaseScore = score
            speedIncreaseFactor += 0.15f // 15% speed of screen

            buildingSpeed = (baseBuildingSpeed * speedIncreaseFactor).toInt()
            coinSpeed = (baseCoinSpeed * speedIncreaseFactor).toInt()
            buildingInterval = (2000 / speedIncreaseFactor).toLong().coerceAtLeast(500)
            coinInterval = (2000 / speedIncreaseFactor).toLong().coerceAtLeast(500)
            droneInterval = (2000 / speedIncreaseFactor).toLong().coerceAtLeast(500)
        }
    }

    private fun addBuilding() {
        val randomHeight = Random.nextInt(3)
        val (buildingBitmap, buildingWidth, buildingHeight) = when (randomHeight) {
            0 -> Triple(smallBuildingBitmap, smallBuildingWidth, smallBuildingHeight)
            1 -> Triple(mediumBuildingBitmap, mediumBuildingWidth, mediumBuildingHeight)
            else -> Triple(largeBuildingBitmap, largeBuildingWidth, largeBuildingHeight)
        }

        val building = Building(
            buildingBitmap.scale(buildingWidth, buildingHeight, true),
            width,
            height - buildingHeight
        )
        buildings.add(building)
    }

    private fun addSafeCoin() {
        val attempts = 5
        for (i in 1..attempts) {
            val randomY = Random.nextInt(height / 4, height * 3 / 4)
            val coinX = width
            val coinWidth = coinBitmap.width

            val coinRect = Rect(coinX, randomY, coinX + coinWidth, randomY + coinBitmap.height)
            val isSafe = buildings.none { building ->
                Rect.intersects(coinRect, building.getCollisionShape())
            }

            if (isSafe) {
                val coin = Coin(
                    coinBitmap.scale(80, 80, true), coinX, randomY
                )
                coins.add(coin)
                return
            }
        }
    }

    private fun handleCollision() {
        playerHealth--
        if (playerHealth <= 0) {
            gameOver = true
            helicopterSound.pause()
        } else {
            helicopter.y -= 70
        }
    }

    private fun resetGame() {
        playerHealth = 3
        score = 0
        gameOver = false
        buildings.clear()
        coins.clear()
        helicopter.reset()
        helicopterSound.start()
        speedIncreaseFactor = 1.0f
        lastSpeedIncreaseScore = 0
        buildingSpeed = baseBuildingSpeed
        coinSpeed = baseCoinSpeed
        buildingInterval = 2000
        coinInterval = 2000
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)


        background1.draw(canvas)
        background2.draw(canvas)

        for (building in buildings) {
            building.draw(canvas)
        }

        for (coin in coins) {
            coin.draw(canvas)
        }

        for (drone in drone) {
            drone.draw(canvas)
        }

        if (!gameOver || (System.currentTimeMillis() / 100) % 2 == 0L) {
            helicopter.draw(canvas)
        }

        for (i in 0 until playerHealth) {
            canvas.drawBitmap(
                healthBitmap,
                null,
                Rect(
                    20 + i * (healthSize + 10),
                    40,
                    20 + i * (healthSize + 10) + healthSize,
                    40 + healthSize
                ),
                null
            )
        }

        val scoreSize = 74
        textPaint.color = Color.BLACK
        canvas.drawBitmap(scoreCount, null, Rect(30, 100, 30 + scoreSize, 100 + scoreSize), null)
        canvas.drawText(" $score", 120f, 150f, textPaint)


        if (gameStarted && !gameOver) {
            val buttonSize = 120
            pauseRect.set((width - buttonSize - 100).toFloat(), 20f,
                (width - 90).toFloat(), 20 + buttonSize.toFloat())

            val icon = if (isPaused) resumeBitmap else pauseBitmap
            canvas.drawBitmap(icon, null, pauseRect, null)
        }


        if (!gameStarted) {
            val message = "Tap to start"

            textPaint.color = Color.BLACK
            textPaint.textSize = 60f
            val textWidth = textPaint.measureText(message)
            canvas.drawText(message, (width - textWidth) / 2, height / 2f, textPaint)

            val centerX = width / 2f
            val centerY = height / 2f

            paint.color = Color.LTGRAY
            exitRect.set(centerX - 200, centerY + 100, centerX + 200, centerY + 200)
            canvas.drawRect(exitRect, paint)

            paint.color = Color.BLACK
            paint.textSize = 60f
            canvas.drawText("Exit", centerX - 50, centerY + 170, paint)
        }

        if (gameOver) {
            gameOver(canvas)
        }
    }

    fun drawPause(canvas: Canvas){
        val centerX = width / 2f
        val centerY = height / 2f

        paint.color = Color.BLACK
        paint.textSize = 60f

        resume.set(centerX, centerY, centerX, centerY)
        canvas.drawRoundRect(resume, 20f, 20f, paint)

        canvas.drawText("Resume", centerX+100, centerY+100, paint)
    }

    fun gameOver(canvas: Canvas){
            val gameOverText = "Game Over"
            val restartText = "Tap to restart"
            val scoreText = "Coin: $coinsFromScore"

            val centerX = width / 2f
            val centerY = height / 2f

            val gameOverWidth = textPaint.measureText(gameOverText)
            val restartWidth = textPaint.measureText(restartText)
            val scoreWidth = textPaint.measureText(scoreText)

            textPaint.color = Color.BLACK
            canvas.drawText(gameOverText, (width - gameOverWidth) / 2, centerY - 60, textPaint)
            canvas.drawText(restartText, (width - restartWidth) / 2, centerY, textPaint)
            canvas.drawText(scoreText, (width - scoreWidth) / 2, centerY + 60, textPaint)

            // Draw Exit Button
            paint.color = Color.LTGRAY
            exitRect.set(centerX - 200, centerY + 100, centerX + 200, centerY + 200)
            canvas.drawRoundRect(exitRect, 20f, 20f, paint)

            paint.color = Color.BLACK
            paint.textSize = 60f
            canvas.drawText("Exit", centerX - 60, centerY + 165, paint)

            // Draw Release Button
            if (!releaseUsed && money >= 50) {
                paint.color = Color.LTGRAY
                releaseRect.set(centerX - 200, centerY + 250, centerX + 200, centerY + 350)
                canvas.drawRoundRect(releaseRect, 20f, 20f, paint)

                paint.color = Color.BLACK
                paint.textSize = 60f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("RELEASE", centerX, centerY + 310, paint)
                paint.textAlign = Paint.Align.LEFT // Reset for other texts
            }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                gameThread.running = false
                gameThread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun playCoinSound() {
        try {
            coinSound.seekTo(0) // Rewind to start if already playing
            coinSound.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread.running = true
        gameThread.gameState = GameState.PLAYING
        gameThread.start()
    }

    fun pause() {
        gameThread.running = false
        helicopterSound.pause()
        if (playMusic) backgroundMusic.pause()
    }

    fun resume() {
        gameOver = false
        gameThread.running = true
        helicopterSound.start()
        if (playMusic) backgroundMusic.start()
    }

    fun onDestroy() {
        helicopterSound.release()
        if (playMusic) backgroundMusic.release()
        coinSound.release()
    }



}
