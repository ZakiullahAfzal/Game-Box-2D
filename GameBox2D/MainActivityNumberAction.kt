package com.example.gamebox2d

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gamebox2d.databinding.ActivityMainNumberActionBinding
import kotlin.random.Random
import androidx.core.content.edit

class MainActivityNumberAction : AppCompatActivity() {

    private lateinit var binding: ActivityMainNumberActionBinding
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var buttonUp: Array<TextView>
    private lateinit var buttonDown: Array<TextView>

    private lateinit var menuView: Array<View?>

    private var countDownTimer: CountDownTimer? = null
    private var botTimer: CountDownTimer? = null

    private var expression: Int = 0
    private var bot: Boolean = false
    private var gameRunning: Boolean = true
    private var isClick : Boolean = false

    private var difficulty: String = "Easy"

    private var playerOne: Int = 0
    private var playerTwo: Int = 0

    private fun setViews() {
        Log.i("msg", "View")
        buttonUp = arrayOf(binding.textViewUp1, binding.textViewUp2, binding.textViewUp3)
        buttonDown = arrayOf(binding.textViewDown1, binding.textViewDown2, binding.textViewDown3)

        menuView = arrayOf(
            binding.textViewNumberActionBack,
            binding.textViewNumberActionLogo,
            binding.constraintLayoutButton,
            binding.imageViewMode
        )

    }

    @SuppressLint("SetTextI18n")
    private fun setAction() {
        binding.textViewPlay?.setOnClickListener {
            setVisibility()
            timer()
        }

        binding.textViewBot.setOnClickListener {
            bot = !bot
            when(binding.textViewBot.text.toString()){
                "Back" -> binding.textViewBot.text = "Bot"
                "Bot" -> binding.textViewBot.text = "Back"
            }
        }

        binding.textViewExit.setOnClickListener {
            finish()
        }

        binding.textViewNumberActionBack.setOnClickListener {
            finish()
        }

        binding.textViewMode.setOnClickListener {
            difficulty =  when(binding.textViewMode.text.toString()){
                "Easy" -> {
                    binding.textViewMode.text = "Medium"
                    binding.imageViewMode.setImageResource(R.drawable.medium)
                    "Medium"
                }

                "Medium" -> {
                    binding.textViewMode.text = "Hard"
                    binding.imageViewMode.setImageResource(R.drawable.hard)
                    "Hard"
                }

                else -> {
                    binding.textViewMode.text = "Easy"
                    binding.imageViewMode.setImageResource(R.drawable.easy)
                    "Easy"
                }

            }
        }

        if(!bot){
            buttonUp.forEach { textView ->
                textView.setOnClickListener {
                    if (isClick) {
                        isClick = false
                        if (textView.text == expression.toString()) {
                            playerOne++
                            binding.textViewScore1.text = playerOne.toString()
                            binding.textViewUp.text = "$playerOne for player One"
                        } else {
                            playerTwo++
                            binding.textViewScore2.text = playerTwo.toString()
                            binding.textViewDown.text = "$playerTwo for player Two"
                        }
                        checkWinner()
                    }
                }
            }

        }


        buttonDown.forEach { textView ->
            textView.setOnClickListener {
                if (isClick) {
                    isClick = false
                    if (bot) botTimer?.cancel()
                    if (textView.text == expression.toString()) {
                        playerTwo++
                        binding.textViewDown.text = "$playerTwo for player Two"
                        binding.textViewScore2.text = playerTwo.toString()
                    } else {
                        playerOne++
                        binding.textViewUp.text = "$playerOne for player One"
                        binding.textViewScore1.text = playerOne.toString()
                    }
                    checkWinner()
                }
            }
        }


    }

    private fun bot() {
        botTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (isClick)
                    makeBotMove()
            }
        }
    }

    private fun sleep() {
        countDownTimer = object : CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                if (gameRunning) {
                    setMathProblem()
                    if (bot)
                        botTimer?.start()
                }
                else finish()
            }
        }
    }

    private fun timer() {
        var isFirst = 3
        object : CountDownTimer(4000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.textViewCounter.text = if (isFirst == 0) "Go" else "$isFirst"
                isFirst--
            }

            override fun onFinish() {
                binding.textViewCounter.text = ""
                setMathProblem()
                binding.linearLayoutUp.visibility = View.VISIBLE
                binding.linearLayoutDown.visibility = View.VISIBLE
                binding.textViewUp.visibility = View.VISIBLE
                binding.textViewDown.visibility = View.VISIBLE
                if (bot) botTimer?.start()

            }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainNumberActionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("gameBox2d", MODE_PRIVATE)

        bot()
        sleep()
        setViews()
        setAction()



    }

    private fun setVisibility() {
        menuView.forEach { it ->
            it?.visibility = View.INVISIBLE
        }

        binding.playView.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun makeBotMove() {
        if (!gameRunning || !isClick) return

        val correctOptions = buttonUp.filter { it.text == expression.toString() }
        val incorrectOptions = buttonUp.filter { it.text != expression.toString() }

        val (delay, accuracy) = when (difficulty) {
            "Easy" -> Pair(800L, 0.6f)
            "Medium" -> Pair(500L, 0.8f)
            else -> Pair(300L, 0.95f) // Hard
        }

        buttonDown[0].postDelayed({
            if (gameRunning && isClick) {
                val selectedView = if (correctOptions.isNotEmpty() && Random.nextFloat() < accuracy) {
                    correctOptions.random()
                } else {
                    incorrectOptions.random()
                }
                selectedView.performClick()
            }
        }, delay)
    }

    @SuppressLint("SetTextI18n")
    private fun checkWinner() {
        if (playerOne == 10) {
            binding.textViewUp.text = "You Win!"
            gameRunning = false
            if (bot) botTimer?.cancel()
            sharedPreferences.edit { putBoolean("p1", true).apply() }
            sharedPreferences.edit { putBoolean("p2", false).apply() }
            countDownTimer?.start()
        } else if (playerTwo == 10) {
            binding.textViewDown.text = "You Win!"
            gameRunning = false
            sharedPreferences.edit { putBoolean("p2", true).apply() }
            sharedPreferences.edit{putBoolean("p1", false).apply()}
            if (bot) botTimer?.cancel()
            countDownTimer?.start()
        } else countDownTimer?.start()
    }

    @SuppressLint("SetTextI18n")
    private fun setMathProblem() {

        val (problemText, correctAnswer) = generateOperator()
        val possibleAnswers = generatePossibleAnswers(correctAnswer)

        expression = correctAnswer

        binding.textViewUp.text = problemText
        binding.textViewDown.text = problemText

        for (i in 0..2) {
            buttonUp[i].text = possibleAnswers[i].toString()
            buttonDown[i].text = possibleAnswers[i].toString()
        }
        isClick = true

    }

    fun generateOperator(): Pair<String, Int> {
        val operators = listOf("+", "-", "*", "/")
        val operator = operators.random()

        return when (operator) {
            "+" -> generateAdditionProblem()
            "-" -> generateSubtractionProblem()
            "*" -> generateMultiplicationProblem()
            "/" -> generateDivisionProblem()
            else -> throw IllegalArgumentException("Invalid operator")
        }
    }

    fun generateAdditionProblem(): Pair<String, Int> {
        val (minA, maxA, minB, maxB) = when (difficulty) {
            "Easy" -> listOf(1, 10, 1, 10)
            "Medium" -> listOf(10, 50, 10, 50)
            else -> listOf(50, 100, 50, 100)
        }
        val a = Random.nextInt(minA, maxA)
        val b = Random.nextInt(minB, maxB)
        return "$a + $b" to (a + b)
    }

    fun generateSubtractionProblem(): Pair<String, Int> {
        val (minA, maxA) = when (difficulty) {
            "Easy" -> listOf(10, 30)
            "Medium" -> listOf(30, 70)
            else -> listOf(70, 120)
        }
        val a = Random.nextInt(minA, maxA)
        val b = Random.nextInt(1, a)
        return "$a - $b" to (a - b)
    }

    fun generateMultiplicationProblem(): Pair<String, Int> {
        val (min, max) = when (difficulty) {
            "Easy" -> listOf(5, 10)
            "Medium" -> listOf(10, 20)
            else -> listOf(20, 30)
        }
        val a = Random.nextInt(min, max)
        val b = Random.nextInt(min, max)
        return "$a * $b" to (a * b)
    }

    fun generateDivisionProblem(): Pair<String, Int> {
        val (minDivisor, maxDivisor, minQuotient, maxQuotient) = when (difficulty) {
            "Easy" -> listOf(1, 5, 1, 5)
            "Medium" -> listOf(2, 10, 2, 10)
            else -> listOf(5, 15, 5, 15)
        }
        val b = Random.nextInt(minDivisor, maxDivisor)
        val answer = Random.nextInt(minQuotient, maxQuotient)
        val a = b * answer
        return "$a / $b" to answer
    }


    fun generatePossibleAnswers(correctAnswer: Int): List<Int> {
        val answers = mutableSetOf(correctAnswer)
        while (answers.size < 3) {
            val randomOffset = Random.nextInt(-5, 6)
            val wrongAnswer = correctAnswer + randomOffset
            if (wrongAnswer != correctAnswer && wrongAnswer > 0) {
                answers.add(wrongAnswer)
            }
        }
        return answers.toList().shuffled()
    }


}