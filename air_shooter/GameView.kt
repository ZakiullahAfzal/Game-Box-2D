package com.example.gamebox2d.air_shooter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.MotionEvent
import com.example.gamebox2d.R
import android.media.SoundPool
import android.media.AudioAttributes
import android.media.MediaPlayer
import java.util.*
import kotlin.math.min
import androidx.core.content.edit
import androidx.core.graphics.toRect

@SuppressLint("ViewConstructor")
class GameView(context: Context, play : Boolean, private val sharedPreferences: SharedPreferences) : SurfaceView(context), Runnable {

    private val surfaceHolder: SurfaceHolder = holder
    private var gameThread: Thread? = null
    private var isPlaying = false

    // Game state
    private enum class GameState { PLAYING, GAME_OVER, GAME_WON, START_SCREEN, PAUSED}
    private var gameState = GameState.START_SCREEN

    // Bitmaps (lazy load to avoid early decoding)
    private val background by lazy { BitmapFactory.decodeResource(resources, R.drawable.drawing) }
    private val healthBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.health) }
    private val scoreBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.score) }
    private val timerBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.timer) }
    private val bossHealth by lazy { BitmapFactory.decodeResource(resources, R.drawable.bosshealth) }

    // Game objects (thread-safe collections)
    private val player = Player(context)
    private val bullets = Collections.synchronizedList(mutableListOf<Bullet>())
    private val enemies = Collections.synchronizedList(mutableListOf<Enemy>())
    private val enemyBullets = Collections.synchronizedList(mutableListOf<Bullet>())
    private val explosions = Collections.synchronizedList(mutableListOf<Explosion>())
    private val rewards = Collections.synchronizedList(mutableListOf<Reward>())

    // Boss state
    private var isBossLevel = false
    private var boss: Boss? = null
    private var bossIntroShown = false
    private var bossStartTime: Long = 0L


    // Timers
    private var lastBulletTime = System.currentTimeMillis()
    private var lastEnemySpawnTime = System.currentTimeMillis()
    private var lastEnemyShootTime = System.currentTimeMillis()
    private var lastRewardSpawnTime = System.currentTimeMillis()
    private var lastBossBulletTime = System.currentTimeMillis()
    private var gameStartTime = System.currentTimeMillis()

    // UI metrics
    private val healthSize = 64
    private val scoreSize = 74
    private val timerSize = 124

    // Audio
    private val soundPool: SoundPool
    private val explosionSoundId: Int
    private var backgroundMusic: MediaPlayer? = null
    private var playMusic : Boolean = false

    // Stats
    private var score = 0
    private var playerHealth = 5

    // Player Shoot style
    private var doubleShoot : Boolean = false
    private var counter : Int = 5
    private var shoot : Boolean = true

    private var money : Int = 0
    private var coinFrame : Int = 0

    // UI Paint
    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
        isAntiAlias = true
    }

    // Buttons (game over screen)
    private val restartRect = RectF()
    private val exitRect = RectF()
    // Add this at the top of the class
    private var releaseUsed = false
    private val releaseRect = RectF()

    private val pauseBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.pause_button) }
   // private val resumeBitmap by lazy { BitmapFactory.decodeResource(resources, R.drawable.resume_button) }
    private val pauseRect = RectF()


    init {
        // Sound setup
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        explosionSoundId = soundPool.load(context, R.raw.explosion, 1)
        backgroundMusic = MediaPlayer.create(context, R.raw.background_music)
        playMusic = play
        if (playMusic){
            backgroundMusic?.isLooping = true
            backgroundMusic?.setVolume(0.5f, 0.5f)
            backgroundMusic?.start()
        }
        Reward.loadBitmaps(context)

        money = sharedPreferences.getInt("coin", 0)
    }

    // Game Handler
    override fun run() {
        var lastFrameTime = System.currentTimeMillis()
        while (isPlaying) {
            val currentTime = System.currentTimeMillis()
            val deltaTime = currentTime - lastFrameTime
            lastFrameTime = deltaTime
            if(gameState == GameState.PLAYING) {
                update()
                draw()
                val sleepTime = 17L - (System.currentTimeMillis() - currentTime)
                if (sleepTime > 0) Thread.sleep(sleepTime)
            }else {
                draw()
                Thread.sleep(100)
            }
        }
    }

    fun update() {
        if (gameState != GameState.PLAYING) return

        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = (currentTime - gameStartTime) / 1000

        // Boss level transition
        if (elapsedSeconds >= 20 && !isBossLevel) {
            startBossLevel(currentTime)
        }


        // Update game objects
        bullets.forEach { it.update() }
        enemies.forEach { it.update() }
        enemyBullets.forEach { it.update() }
        explosions.forEach { it.update() }
        rewards.forEach { it.update() }

        // Cleanup off-screen objects
        bullets.removeAll { it.y < 170 || it.y > height }
        enemyBullets.removeAll { it.y > height }
        enemies.removeAll { it.y > height }
        explosions.removeAll { it.isFinished }
        rewards.removeAll { it.isOffScreen() }

        // Handle collisions
        handleCollisions()

        // Spawn logic
        if (!isBossLevel) {
            handlePlayerShooting(currentTime)
            handleEnemySpawning(currentTime)
            handleRewardSpawning(currentTime)
        } else {
            if (shoot)
                handlePlayerShooting(currentTime)
            handleBossLogic(currentTime)

        }
    }

    private fun startBossLevel(currentTime: Long) {
        isBossLevel = true
        bossStartTime = currentTime
        bossIntroShown = false
        bullets.clear()
        enemies.clear()
        enemyBullets.clear()
    }

    private fun handlePlayerShooting(currentTime: Long) {
        if (currentTime - lastBulletTime > 400) {
            if(doubleShoot){
                counter --
                bullets.add(Bullet(context, (player.x + player.width / 2) - 16f, player.y, true))
                bullets.add(Bullet(context, (player.x + player.width / 2) + 16f, player.y, true))
                if (counter == 0)
                    doubleShoot = false
            }else{
                bullets.add(Bullet(context, player.x + player.width / 2, player.y, true))
            }
            lastBulletTime = currentTime
        }
    }

    private fun handleEnemySpawning(currentTime: Long) {
        if (currentTime - lastEnemySpawnTime > 1500) {
            enemies.add(Enemy(context, width))
            lastEnemySpawnTime = currentTime
        }

        if (currentTime - lastEnemyShootTime > 1000) {
            enemies.forEach {
                enemyBullets.add(Bullet(context, it.x + it.getWidth() / 2, it.y + it.getHeight(), false))
            }
            lastEnemyShootTime = currentTime
        }
    }

    private fun handleRewardSpawning(currentTime: Long) {
        if (rewards.size < 3 && currentTime - lastRewardSpawnTime > 10000) {
            rewards.add(Reward(context, width, height))
            lastRewardSpawnTime = currentTime
        }
    }

    private fun handleBossLogic(currentTime: Long) {
        val bossElapsed = currentTime - bossStartTime

        if (!bossIntroShown && bossElapsed >= 3000) {
            bossIntroShown = true
            boss = Boss(context, width)
        }

        boss?.let { boss ->
            boss.update(width)

            // Boss shooting pattern
            if (currentTime - lastBossBulletTime > 700 && bossElapsed % 6000 < 5000) {
                lastBossBulletTime = currentTime
                enemyBullets.addAll(boss.shoot())
            }

            // Check if boss is defeated
            if (boss.health <= 0) {
                gameState = GameState.GAME_WON
                isPlaying = false
            }
        }
    }

    private fun handleCollisions() {
        // Player bullets vs enemies
        bullets.removeAll { bullet ->
            enemies.removeAll { enemy ->
                if (RectF.intersects(bullet.getRect(), enemy.getRect())) {
                    explosions.add(Explosion(context, enemy.x, enemy.y, enemy.getWidth(), enemy.getHeight()))
                    score += 10
                    soundPool.play(explosionSoundId, 1f, 1f, 0, 0, 1f)
                    if(score % 10 == 0){
                        coinFrame++
                        money++
                    }
                    true
                } else false
            }
            false
        }

        // Enemy bullets vs player
        enemyBullets.removeAll { bullet ->
            if (RectF.intersects(bullet.getRect(), player.getRect())) {
                playerHealth--
                if (playerHealth <= 0) {
                    gameState = GameState.GAME_OVER
                    isPlaying = false
                }
                true
            } else false
        }

        // Rewards vs player
        rewards.removeAll { reward ->
            if (RectF.intersects(reward.getRect(), player.getRect())) {
                when (reward.type) {
                    RewardType.HEALTH -> playerHealth = min(playerHealth + 1, 5)
                    RewardType.SCORE -> score += 50
                    RewardType.ENERGY -> {
                        doubleShoot = true
                        counter = 10
                    }
                }
                true
            } else false
        }

        // Boss vs player bullets
        boss?.let { boss ->
            bullets.removeAll { bullet ->
                if (RectF.intersects(bullet.getRect(), boss.getRect())) {
                    boss.health--
                    // explosions.add(Explosion(context, boss.x, boss.y, boss.width, boss.height))
                    score += 10
                    //  soundPool.play(explosionSoundId, 1f, 1f, 0, 0, 1f)
                    true
                } else false
            }
        }
    }

    private fun draw() {
        val canvas = surfaceHolder.lockCanvas() ?: return

        try {
            // Draw background
            canvas.drawBitmap(background, null, Rect(0, 0, width, height), null)

            when (gameState) {
                GameState.START_SCREEN, GameState.PAUSED -> drawStartScreen(canvas)
                GameState.PLAYING -> drawGame(canvas)
                GameState.GAME_OVER, GameState.GAME_WON -> drawGameOver(canvas)
            }
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawGame(canvas: Canvas) {
        // Draw player and entities
        player.draw(canvas)
        bullets.forEach { it.draw(canvas) }
        enemies.forEach { it.draw(canvas) }
        enemyBullets.forEach { it.draw(canvas) }
        explosions.forEach { it.draw(canvas) }
        rewards.forEach { it.draw(canvas) }

        // Draw boss intro or boss
        if (isBossLevel) {
            if (!bossIntroShown) {
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Boss Level", width / 2f, height / 2f, paint)
                shoot = false
            } else {
                shoot = true
                boss?.draw(canvas)
            }
        }

        // Draw HUD
        drawHUD(canvas)
        pauseRect.set(
            width - 130f,
            height - 250f,         // <- dynamically position to bottom
            width - 30f,
            height - 150f
        )
        canvas.drawBitmap(pauseBitmap, null ,pauseRect.toRect(), null)

    }

    private fun drawHUD(canvas: Canvas) {
        // Health
        for (i in 0 until playerHealth) {
            canvas.drawBitmap(
                healthBitmap,
                null,
                Rect(20 + i * (healthSize + 10), 30, 20 + i * (healthSize + 10) + healthSize, 20 + healthSize),
                null
            )
        }

        // Score
        paint.color = Color.WHITE
        paint.textSize = 50f
        canvas.drawBitmap(scoreBitmap, null, Rect(30, 100, 30 + scoreSize, 100 + scoreSize), null)
        canvas.drawText(" $score", (30 + scoreSize + 30).toFloat(), 110f + scoreSize / 1.5f, paint)

        // Timer (only in normal level)
        if (!isBossLevel) {
            val timeLeft = 60 - ((System.currentTimeMillis() - gameStartTime) / 1000).toInt()
            canvas.drawText(": $timeLeft", (width - 30 - timerSize - 90).toFloat(), 100f + timerSize / 1.5f, paint)
            canvas.drawBitmap(timerBitmap, null, Rect(width - 30 - timerSize, 100, width - 10, 100 + timerSize), null)
        }else{
            canvas.drawText("${boss?.health} : ", (width - 30 - timerSize - 90).toFloat(), 100f + timerSize / 1.5f, paint)
            canvas.drawBitmap(bossHealth, null, Rect(width - 30 - timerSize, 100, width - 10, 100 + timerSize), null)
        }
    }

    private fun drawStartScreen(canvas: Canvas) {
        paint.textAlign = Paint.Align.CENTER
        val centerX = width / 2f
        val centerY = height / 2f
        paint.color = Color.WHITE
        paint.textSize = 100f

        if (gameState == GameState.PAUSED){
            canvas.drawText("Game Paused", centerX, centerY-300, paint)
            val resumeButtonRect = RectF(centerX-300, centerY, centerX+300, centerY+100)
            paint.color = Color.LTGRAY
            canvas.drawRoundRect(resumeButtonRect, 20f, 20f, paint)
            paint.color = Color.BLACK
            paint.textSize = 70f
            canvas.drawText("Resume", centerX, centerY+ 70, paint)
            return
        }else{
            // Draw game title

            canvas.drawText("Space Shooter", centerX, centerY - 250, paint)
            // Draw buttons
            paint.color = Color.LTGRAY
            restartRect.set(centerX - 300, centerY, centerX + 300, centerY + 100)
            exitRect.set(centerX - 300, centerY + 150, centerX + 300, centerY + 250)

            canvas.drawRoundRect(restartRect, 20f, 20f, paint)
            canvas.drawRoundRect(exitRect, 20f, 20f, paint)

            paint.color = Color.BLACK
            paint.textSize = 70f
            canvas.drawText("START GAME", centerX, centerY + 70, paint)
            canvas.drawText("EXIT", centerX, centerY + 220, paint)
        }

    }

    private fun drawGameOver(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 60f
        paint.color = Color.WHITE
        canvas.drawText(
            if (gameState == GameState.GAME_WON) "You Win! Coin : $coinFrame"
            else "Game Over Coin : $coinFrame",
            centerX, centerY - 300f, paint
        )

        // Draw Restart Button
        restartRect.set(centerX - 300, centerY - 50, centerX + 300, centerY + 50)
        paint.color = Color.LTGRAY
        canvas.drawRect(restartRect, paint)
        paint.color = Color.BLACK
        canvas.drawText("Restart", centerX, centerY + 15f, paint) // +15f to vertically center the text in the rect

        // Draw Exit Button
        exitRect.set(centerX - 300, centerY + 100, centerX + 300, centerY + 200)
        paint.color = Color.LTGRAY
        canvas.drawRect(exitRect, paint)
        paint.color = Color.BLACK
        canvas.drawText("Exit", centerX, centerY + 150f, paint)

        // Draw Release Button (if available)
        if (!releaseUsed && gameState == GameState.GAME_OVER && money >= 50) {
            releaseRect.set(centerX - 300, centerY + 250, centerX + 300, centerY + 350)
            paint.color = Color.LTGRAY
            canvas.drawRect(releaseRect, paint)
            paint.color = Color.BLACK
            canvas.drawText("RELEASE", centerX, centerY + 300f + 15f, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (gameState) {
                    GameState.PLAYING ->{
                        if(pauseRect.contains(event.x, event.y)){
                            gameState = GameState.PAUSED
                            return true
                        }
                    }
                    GameState.PAUSED ->{
                        val centerX = width / 2f
                        val centerY = height / 2f
                        val resumeButtonRect = RectF(centerX - 300, centerY - 50, centerX + 300, centerY + 50)
                        if (resumeButtonRect.contains(event.x, event.y)){
                            gameState = GameState.PLAYING
                            if (!isPlaying) resume()
                            return true
                        }
                    }
                    GameState.GAME_OVER, GameState.GAME_WON, GameState.START_SCREEN -> {

                        if (restartRect.contains(event.x, event.y)) {
                            sharedPreferences.edit { putInt("coin", money).apply() }
                            restartGame()
                            return true
                        } else if (exitRect.contains(event.x, event.y)) {
                            sharedPreferences.edit { putInt("coin", money).apply() }
                            (context as? Activity)?.finish()
                            return true
                        } else if (!releaseUsed && gameState == GameState.GAME_OVER && releaseRect.contains(event.x, event.y)) {
                            money -= 50
                            sharedPreferences.edit { putInt("coin", money).apply() }
                            releaseUsed = true
                            gameState = GameState.PLAYING
                            playerHealth = 3 // Give some health back
                            player.x = (width / 2 - player.width / 2).toFloat()
                            player.y = (height - player.height - 50).toFloat()
                            if (!isPlaying) resume()
                            return true
                        }
                    }

                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (gameState == GameState.PLAYING) {
                    player.x = event.x - player.width / 2
                    player.y = event.y - player.height / 2

                    if (player.x < 0) player.x = 0f
                    if (player.x + player.width > width) player.x = (width - player.width).toFloat()
                    if (player.y < 0) player.y = 0f
                    if (player.y + player.height > height) player.y = (height - player.height).toFloat()
                }
            }
        }
        return true
    }

    fun pause() {
        isPlaying = false
        gameThread?.join()
        if(playMusic)
            backgroundMusic?.pause()
    }

    fun resume() {
        if (!isPlaying) {
            isPlaying = true
            enemyBullets.clear()
            gameThread = Thread(this).also { it.start() }
            if (playMusic)
                backgroundMusic?.start()
        }
    }

    fun destroy() {
        isPlaying = false
        gameThread?.join()
        soundPool.release()
        backgroundMusic?.stop()
        backgroundMusic?.release()
        backgroundMusic = null
        listOf(background, healthBitmap, scoreBitmap, timerBitmap).forEach { it?.recycle() }
    }

    private fun restartGame() {
        score = 0
        playerHealth = 5
        bullets.clear()
        enemies.clear()
        enemyBullets.clear()
        explosions.clear()
        rewards.clear()
        isBossLevel = false
        boss = null
        bossIntroShown = false
        releaseUsed = false // reset here
        gameStartTime = System.currentTimeMillis()
        lastBulletTime = gameStartTime
        lastEnemySpawnTime = gameStartTime
        lastEnemyShootTime = gameStartTime
        lastRewardSpawnTime = gameStartTime

        // Reset player position
        player.x = (width / 2 - player.width / 2).toFloat()
        player.y = (height - player.height - 50).toFloat()


        gameState = GameState.PLAYING
        if (!isPlaying) {
            resume()
        }
    }
    
    private fun cleanupBitmap(){
        timerBitmap?.recycle()
        scoreBitmap?.recycle()
        healthBitmap?.recycle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cleanupBitmap()
        destroy()
    }

}
