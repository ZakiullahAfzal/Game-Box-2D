package com.example.gamebox2d

import android.annotation.SuppressLint
import android.app.GameState
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gamebox2d.databinding.ActivityTicTocToeBinding

class TicTocToe : AppCompatActivity() {

    private lateinit var binding: ActivityTicTocToeBinding
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var game: TicTacToeGame
    private var botState: Boolean = false
    private var userState: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private lateinit var viewsMenu: Array<View?>
    private val baseColor = arrayOf(Color.rgb(30, 102, 157), Color.rgb(131, 54, 79))
    private lateinit var image: Array<Array<ImageView?>>
    private val crossCircle = arrayOf(R.drawable.cross, R.drawable.circle)
    private lateinit var victor: Array<View?>
    private lateinit var currentPlayer: Enum<Player>
    private lateinit var mainView: Array<View?>

    private fun setViews() {
        mainView = arrayOf(
            binding.linearLayout,
            binding.linearLayout2,
            binding.linearLayout3,
            binding.divider13,
            binding.divider14,
        )
        image = arrayOf(
            arrayOf(binding.image1, binding.image2, binding.image3),
            arrayOf(binding.image4, binding.image5, binding.image6),
            arrayOf(binding.image7, binding.image8, binding.image9)
        )

        victor = arrayOf(
            binding.dividerWin1,
            binding.dividerWin2,
            binding.dividerWin3,
            binding.dividerWin4,
            binding.dividerWin5,
            binding.dividerWin6,
            binding.dividerWin7,
            binding.dividerWin8
        )

        viewsMenu = arrayOf(binding.constraintLayoutTicTocToe,
            binding.textViewLogo, binding.textViewBack,binding.imageViewMode)
        initializeBotTimer()
    }

    @SuppressLint("SetTextI18n")
    private fun setActions() {
        binding.btnPlay.setOnClickListener {
            for (i in viewsMenu) {
                i?.visibility = View.INVISIBLE
            }
            binding.exit.visibility = View.VISIBLE
            startGame()
            setVisibility()
        }

        binding.textViewBack.setOnClickListener {
            finish()
        }

        binding.btnMode.setOnClickListener {
            when (binding.btnMode.text) {
                "Easy" -> {
                    binding.btnMode.text = "Medium"
                    binding.imageViewMode.setImageResource(R.drawable.medium)
                }
                "Medium" -> {
                    binding.btnMode.text = "Hard"
                    binding.imageViewMode.setImageResource(R.drawable.hard)
                }
                "Hard" -> {
                    binding.btnMode.text = "Easy"
                    binding.imageViewMode.setImageResource(R.drawable.easy)
                }
            }
        }

        binding.exit.setOnClickListener {
            finish()
        }

        binding.btnBot.setOnClickListener {
            botState = !botState
            if (botState) {
                binding.btnBot.text = "Back"
                binding.imageViewMode.visibility = View.VISIBLE
                binding.btnMode.visibility = View.VISIBLE
            } else {
                binding.btnBot.text = "Bot"
                binding.imageViewMode.visibility = View.INVISIBLE
                binding.btnMode.visibility = View.INVISIBLE
            }
        }

        binding.textViewRestart.setOnClickListener {
            restartGame()
        }

        for (row in 0..2) {
            for (col in 0..2) {
                image[row][col]?.setOnClickListener {
                    onCellClickedUsers(row, col)
                }
            }
        }


    }

    private fun initializeBotTimer() {
        countDownTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                val move = game.aiMove(binding.btnMode.text.toString())
                move?.let { (row, col) ->
                    if (game.board[row][col] == Player.NONE && game.checkWinner() == Player.NONE) {
                        onCellClickedUsers(row, col)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTicTocToeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferences = getSharedPreferences("gameBox2d", MODE_PRIVATE)


        game = TicTacToeGame()
        setViews()
        setActions()
    }

    private fun onCellClickedUsers(row: Int, col: Int) {
        if (game.board[row][col] != Player.NONE || game.checkWinner() != Player.NONE) return

        if (userState) {
            game.board[row][col] = Player.O
            image[row][col]?.setImageResource(crossCircle[1])
            binding.main.setBackgroundColor(baseColor[1])
        } else {
            game.board[row][col] = Player.X
            image[row][col]?.setImageResource(crossCircle[0])
            binding.main.setBackgroundColor(baseColor[0])
        }

        checkGameState()

        // If bot is active and now it's bot's turn, trigger timer
        if (botState && !userState && game.checkWinner() == Player.NONE) {
            countDownTimer?.start()
        }
    }

    private fun checkGameState(): Player {
        val winner = game.checkWinner()
        currentPlayer = winner
        userState = !userState
        when (winner) {
            Player.X, Player.O -> {
                val winPattern = getWinPatternFromBoard(game.board)
                if (winPattern > 0) {
                    showWinLine(winPattern)
                    Toast.makeText(this, "Player ${winner.name} wins!", Toast.LENGTH_SHORT).show()
                    binding.textViewRestart.visibility = View.VISIBLE
                    countDownTimer?.cancel()
                }
                if (winner == Player.X){
                    sharedPreferences.edit { putBoolean("p2", true).apply()}
                    sharedPreferences.edit { putBoolean("p1", false).apply()}
                }else{
                    sharedPreferences.edit { putBoolean("p1", true).apply()}
                    sharedPreferences.edit { putBoolean("p2", false).apply()}
                }
            }
            Player.NONE -> {
                if (game.board.flatten().none { it == Player.NONE }) {
                    Toast.makeText(this, "Draw", Toast.LENGTH_SHORT).show()
                    binding.textViewRestart.visibility = View.VISIBLE
                    countDownTimer?.cancel()
                }
            }
        }
        return winner
    }

    private fun showWinLine(winPattern: Int) {
        victor.forEach { it?.visibility = View.INVISIBLE }
        when (winPattern) {
            1 -> binding.dividerWin4.visibility = View.VISIBLE
            2 -> binding.dividerWin5.visibility = View.VISIBLE
            3 -> binding.dividerWin6.visibility = View.VISIBLE
            4 -> binding.dividerWin1.visibility = View.VISIBLE
            5 -> binding.dividerWin2.visibility = View.VISIBLE
            6 -> binding.dividerWin3.visibility = View.VISIBLE
            7 -> binding.dividerWin8.visibility = View.VISIBLE
            8 -> binding.dividerWin7.visibility = View.VISIBLE
        }
    }

    private fun getWinPatternFromBoard(board: Array<Array<Player>>): Int {
        return when {
            board[0][0] != Player.NONE && board[0][0] == board[0][1] && board[0][1] == board[0][2] -> 1
            board[1][0] != Player.NONE && board[1][0] == board[1][1] && board[1][1] == board[1][2] -> 2
            board[2][0] != Player.NONE && board[2][0] == board[2][1] && board[2][1] == board[2][2] -> 3
            board[0][0] != Player.NONE && board[0][0] == board[1][0] && board[1][0] == board[2][0] -> 4
            board[0][1] != Player.NONE && board[0][1] == board[1][1] && board[1][1] == board[2][1] -> 5
            board[0][2] != Player.NONE && board[0][2] == board[1][2] && board[1][2] == board[2][2] -> 6
            board[0][0] != Player.NONE && board[0][0] == board[1][1] && board[1][1] == board[2][2] -> 7
            board[0][2] != Player.NONE && board[0][2] == board[1][1] && board[1][1] == board[2][0] -> 8
            else -> 0
        }
    }

    private fun setVisibility() {

            val color = baseColor.random()
            userState = (baseColor[0] == color)
            binding.main.setBackgroundColor(color)
            mainView.forEach { it?.visibility = View.VISIBLE }

            // If bot plays first
            if (botState && !userState) {
                countDownTimer?.start()
            }
      /*  } else {
            binding.main.setBackgroundColor(Color.rgb(47, 61, 67))
            viewsMenu.forEach { it?.visibility = View.INVISIBLE }
        } */
    }

    private fun startGame(){
        game.resetBoard()
        for (row in 0..2) {
            for (col in 0..2) {
                image[row][col]?.setImageResource(0)
            }
        }
        victor.forEach { it?.visibility = View.INVISIBLE }
        userState = false
        binding.main.setBackgroundColor(baseColor[1])
        binding.textViewRestart.visibility = View.INVISIBLE
    }

    private fun restartGame() {
        game.resetBoard()
        for (row in 0..2) {
            for (col in 0..2) {
                image[row][col]?.setImageResource(0)
            }
        }
        victor.forEach { it?.visibility = View.INVISIBLE }
        userState = false
        binding.main.setBackgroundColor(baseColor[1])
        binding.textViewRestart.visibility = View.INVISIBLE
        if (botState && !userState){
            countDownTimer?.start()
        }
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}
