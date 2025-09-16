package com.example.gamebox2d

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gamebox2d.databinding.ActivityMainMissingNumberBinding

class MainActivityMissingNumber : AppCompatActivity() {

    private lateinit var binding: ActivityMainMissingNumberBinding
    private var count: Int = 0
    private var level: Int = 0
    private var state: Int = 0
    private var getCoin : Boolean = true

    @SuppressLint("SetTextI18n")
    private fun setView() {


        mainView = arrayOf(
            binding.imageViewProblem,
            binding.textViewLevel,
            binding.imageViewHelp,
            binding.textViewInput,
            binding.linearLayout4,
            binding.gridLayout
        )

        levels = arrayOf(
            binding.textView1,
            binding.textView2,
            binding.textView3,
            binding.textView4,
            binding.textView5,
            binding.textView6,
            binding.textView7,
            binding.textView8,
            binding.textView9,
            binding.textView10,
            binding.textView11,
            binding.textView12,
            binding.textView13,
            binding.textView14,
            binding.textView15,
            binding.textView16,
            binding.textView17,
            binding.textView18,
            binding.textView19,
            binding.textView20,
            binding.textView21,
            binding.textView22,
            binding.textView23,
            binding.textView24,
            binding.textView25,
            binding.textView26,
            binding.textView27,
            binding.textView28,
            binding.textView29,
            binding.textView30,
            binding.textView31,
            binding.textView32,
            binding.textView33,
            binding.textView34,
            binding.textView35,
            binding.textView36,
            binding.textView37,
            binding.textView38,
            binding.textView39,
            binding.textView40,
            binding.textView41,
            binding.textView42,
            binding.textView43,
            binding.textView44,
            binding.textView45,
            binding.textView46,
            binding.textView47,
            binding.textView48,
            binding.textView49,
            binding.textView50
        )

        images = arrayOf(
            R.drawable.missingnumber1,
            R.drawable.missingnumber2,
            R.drawable.missingnumber3,
            R.drawable.missingnumber4,
            R.drawable.missingnumber5,
            R.drawable.missingnumber6,
            R.drawable.missingnumber7,
            R.drawable.missingnumber8,
            R.drawable.missingnumber9,
            R.drawable.missingnumber10,
            R.drawable.missingnumber11,
            R.drawable.missingnumber12,
            R.drawable.missingnumber13,
            R.drawable.missingnumber14,
            R.drawable.missingnumber15,
            R.drawable.missingnumber16,
            R.drawable.missingnumber17,
            R.drawable.missingnumber18,
            R.drawable.missingnumber19,
            R.drawable.missingnumber20,
            R.drawable.missingnumber21,
            R.drawable.missingnumber22,
            R.drawable.missingnumber23,
            R.drawable.missingnumber24,
            R.drawable.missingnumber25,
            R.drawable.missingnumber26,
            R.drawable.missingnumber27,
            R.drawable.missingnumber28,
            R.drawable.missingnumber29,
            R.drawable.missingnumber30,
            R.drawable.missingnumber31,
            R.drawable.missingnumber32,
            R.drawable.missingnumber33,
            R.drawable.missingnumber34,
            R.drawable.missingnumber35,
            R.drawable.missingnumber36,
            R.drawable.missingnumber37,
            R.drawable.missingnumber38,
            R.drawable.missingnumber39,
            R.drawable.missingnumber40,
            R.drawable.missingnumber41,
            R.drawable.missingnumber42,
            R.drawable.missingnumber43,
            R.drawable.missingnumber44,
            R.drawable.missingnumber45,
            R.drawable.missingnumber46,
            R.drawable.missingnumber47,
            R.drawable.missingnumber48,
            R.drawable.missingnumber49,
            R.drawable.missingnumber50
            )

        buttons = arrayOf(
            binding.grTextView1,
            binding.grTextView2,
            binding.grTextView3,
            binding.grTextView4,
            binding.grTextView5,
            binding.grTextView6,
            binding.grTextView7,
            binding.grTextView8,
            binding.grTextView9
        )

        binding.grTextView0?.setOnClickListener {
            val state = binding.grTextView0?.text.toString()
            binding.textViewInput.text = binding.textViewInput.text.toString() + state
        }


    }

    @SuppressLint("SetTextI18n")
    private fun setAction() {

        binding.imageViewHelp.setOnClickListener {
            setHelpView(true)
            state = 3
        }

        binding.textViewExit.setOnClickListener {
            when (state) {
                //0 -> Exit from level view
                0,1,2 -> finish()
                3 -> {
                    getCoin = true
                    setHelpView(false)
                }
            }
        }

        binding.textViewPruches.setOnClickListener {
            val coin = sharedPreferences.getInt("coin", 0)
            if (coin < 50) Toast.makeText(this, "No Coin left", Toast.LENGTH_SHORT).show()
            else {
                if(getCoin) {
                    getCoin = false
                    val state = coin - 50
                    binding.textViewAnswer.text = solution[level]
                    binding.textViewAnswer.visibility = View.VISIBLE
                    sharedPreferences.edit().putInt("coin", state).apply()
                }
            }
        }

        for (i in buttons.indices) {
            buttons[i].setOnClickListener {
                val state = binding.textViewInput.text.toString()
                binding.textViewInput.text = (state + (i + 1))
            }
        }


        for (i in 0 .. count) {
            levels[i].setTextColor(Color.WHITE)
            levels[i].setOnClickListener {
                    setResume()
                    level = i
                    state = 1
                    if (level < images.size) {
                        binding.textViewLevel.text = "${level + 1}/50"
                        setImage(images[level])
                    }
            }
        }

        binding.textViewEnter?.setOnClickListener {
            if (binding.textViewInput.text.toString() == ans[level]) {
                if (level == count && count < 50) {
                    count++
                    level = count
                    edit()
                } else if(level < 50)
                    level++
                binding.textViewLevel.text = "${level + 1}/50"
                nextLevel()
            } else {
                Toast.makeText(this, "Wrong Answer!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewNextLevel.setOnClickListener {
            if (level < images.size) {
                reMain()
                setImage(images[level])
            } else {
                binding.textViewNextLevel.text = "Level Complete"
            }
        }


        binding.textViewClear.setOnClickListener {
            binding.textViewInput.text = ""
        }

        binding.textViewBackward.setOnClickListener {
            val state = binding.textViewInput.text
            if (!state.isEmpty()) binding.textViewInput.text = state.substring(0, state.length - 1)
        }

    }

    private fun setImage(resourceId: Int) {
        binding.imageViewProblem.setImageResource(resourceId)
    }

    private fun setResume() {
        binding.scrollviewMS.visibility = View.INVISIBLE
        binding.textViewName.visibility = View.INVISIBLE
        mainView.forEach { it.visibility = View.VISIBLE }
    }


    private fun nextLevel() {
        mainView.forEach { it.visibility = View.INVISIBLE }
        binding.imageViewNextLevel.visibility = View.VISIBLE
        binding.textViewNextLevel.visibility = View.VISIBLE
        binding.textViewInput.text = ""
        state = 2
    }

    private fun reMain() {
        binding.imageViewNextLevel.visibility = View.INVISIBLE
        binding.textViewNextLevel.visibility = View.INVISIBLE
        mainView.forEach { it.visibility = View.VISIBLE }
        binding.textViewInput.text = ""
        state = 1
    }

    private fun edit() {
        sharedPreferencesMissingNumber.edit().putInt("count", count).apply()
    }

    private fun setHelpView(state: Boolean) {
        if (state) {
            mainView.forEach { it -> it.visibility = View.INVISIBLE }
            binding.textViewPruches.visibility = View.VISIBLE
            binding.imageViewPruchesCoin.visibility = View.VISIBLE
            binding.textViewMessage.visibility = View.VISIBLE
            binding.textViewAnswer.visibility = View.VISIBLE
            this.state = 3
        } else {
            mainView.forEach { it -> it.visibility = View.VISIBLE }
            binding.textViewPruches.visibility = View.INVISIBLE
            binding.imageViewPruchesCoin.visibility = View.INVISIBLE
            binding.textViewMessage.visibility = View.INVISIBLE
            binding.textViewAnswer.visibility = View.INVISIBLE
            this.state = 1
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        binding = ActivityMainMissingNumberBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        sharedPreferences = getSharedPreferences("gameBox2d", MODE_PRIVATE)
        sharedPreferencesMissingNumber = getSharedPreferences("missingNumber", MODE_PRIVATE)
        count = sharedPreferencesMissingNumber.getInt("count", 0)
        if(count < 50)
           level = count
        setView()
        setAction()
    }


    companion object {
        private lateinit var mainView: Array<View>
        private lateinit var levels: Array<TextView>
        private lateinit var images: Array<Int>
        private lateinit var buttons: Array<TextView>
        private lateinit var sharedPreferencesMissingNumber: SharedPreferences
        private lateinit var sharedPreferences : SharedPreferences

        private var solution = arrayOf(
           "(1*2) (2*2) (4*2).. = 16",//1
            "3",//2
            "3",//3
            "n * 5 = 10",//4
            "n + 8 = 31",//5
            "14",//6
            "[] = 10",//7
            "A : 60 B : 20 = 3",//8
            "n + m = 78",//9
            "2",//10
            "96004",//11
            "n+5+6+7 = 31",//12
            "n*m = 56",//13
            "10",//14
            "529",//15
            "n * (n+1) = 110",//16
            "n*m (m = 7)",//17
            "n+10 + 20 + 30 = 69",//18
            "1(power) 2(/) 3(+) 4(-) = 1",//19
            "n+10 - 3 = 17",//20
            "3186",//21
            "n*2 +1 = 61",//22
            "8 * 8 = 64",//23
            "n*m+3 = 84",//24
            "(n-2) / 2 = 5",//25
            "13 + 5 = 18",//26
            "6+3 = 9",//27
            "(n+m)^2 = 64",//28
            "7 * 8 = 56",//29
            "n - 60 = 203",//30
            "(n^2)+m = 17",//31
            "1+2 = 3",//32
            "(n*n) + (m*m) = 26",//33
            "n*3 : 3",//34
            "(n+m)/2 = 4",//35
            "1+2 = 3",//36
            "n*m + (n+m) = 23",//37
            "47 <-> 74 = 81",//38
            "n+10 - 5 = 16",//39
            "n+5 = 2",//40
            "5^5 7^7 : 7+5 = 12",//41
            "(20 + 7) / 3 = 9",//42
            "(n+1) * (m +1) = 48",//43
            "(2-1) (7-1) (1-1) = 160",//44
            "11 * 5 = 55",//45
            "(4*4) - (3*3) = 7",//46
            "7-2 5-2 3-2 = 1",//47
            "4+1 4-1 = 53",//48
            "2*7 = 14",//49
            "2*(2-1) 3*(3-1)..6*(6-1)=30"//50

        )

        private var ans = arrayOf(
           "16", //1
            "3", //2
            "3", //3
            "10",//4
            "31",//5
            "14",//6
            "10",//7
            "3",//8
            "78",//9
            "2",//10
            "96004",//11
            "31",//12
            "56",//13
            "10",//14
            "529",//15
            "110",//16
            "7",//17
            "69",//18
            "1",//19
            "17",//20
            "3186", //21
            "61", //22
            "64",//23
            "84",//24
            "5",//25
            "18",//26
            "9",//27
            "64",//28
            "56",//29
            "203",//30
            "17",//31
            "3",//32
            "26",//33
            "3",//34
            "4",//35
            "1",//36
            "23",//37
            "81",//38
            "16",//39
            "2",//40
            "12",//41
            "9",//42
            "48",//43
            "160",//44
            "55",//45
            "7",//46
            "1",//47
            "53",//48
            "2",//49
            "30"//50
        )
    }

}
