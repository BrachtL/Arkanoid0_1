package com.example.arkanoid0_1

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.os.*
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import java.util.concurrent.Executors


import android.view.WindowManager

private var immersiveFlags: Int = 0

private lateinit var ballImageView: ImageView
private lateinit var handler: Handler
private const val updateInterval = 16L // Update every 16 milliseconds (approx. 60 FPS)

private var standardSpeedY = 8
private var standardSpeedX = 8


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hiding two bars and nav buttons
        // Set immersive flags to hide the navigation bar and status bar
        immersiveFlags = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
        window.decorView.systemUiVisibility = immersiveFlags
    }

    override fun onResume() {
        super.onResume()

        // Re-apply immersive flags when the activity resumes
        window.decorView.systemUiVisibility = immersiveFlags
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // Re-apply immersive flags when the window focus changes
        if (hasFocus) {
            window.decorView.systemUiVisibility = immersiveFlags
        }



        setContentView(R.layout.activity_main)

        /*
        val test: ImageView = findViewById(R.id.imageView)
        test.y = 100F
        test.x = -300F

        fun onTouch(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y

            val test: ImageView = findViewById(R.id.imageView)
            test.x = x
            test.y = y

            return true
        }
        */

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        var screenWidth = displayMetrics.widthPixels

        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val navBarHeight = resources.getDimensionPixelSize(resourceId)
            // Use navBarHeight as needed
            screenWidth = screenWidth + navBarHeight
        }



        val paddle = findViewById<ImageView>(R.id.paddleImage)

        paddle.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Handle touch down event
                    true // Return true to indicate that we have consumed the event
                }
                MotionEvent.ACTION_MOVE -> {
                    // Handle touch move event
                    val x = motionEvent.rawX
                    // Move the paddle to the x position of the touch event
                    paddle.x = x - paddle.width / 2
                    //paddle.x = motionEvent.x - paddle.left - paddle.width / 2
                    true // Return true to indicate that we have consumed the event
                }
                else -> false // Return false for other touch events
            }
        }


        //val location = IntArray(2)

        //read the top left location
        //paddle.getLocationOnScreen(location)

        //create the edges
        //val x = location[0] + paddle.width / 2
        //val y = location[1] + paddle.height / 2


        fun colision(ball: Any) {
            //verify if there is a colision with the paddle or the obstacles(rectangles)
            //which color is the obstacle? (then, change() the color or remove obstacle)
            //the object of colision is the paddle? (then calculate the speed in order to apply() an effect in the ball)

        }

        val ballImageView = findViewById<ImageView>(R.id.ballImage)


        handler = Handler(Looper.getMainLooper())

        // Start the update loop
        handler.post(object : Runnable {
            override fun run() {
                // Update the position of the ball
                updateBallPosition(ballImageView, screenHeight, screenWidth, paddle)


                // Schedule the next update
                handler.postDelayed(this, updateInterval)
            }
        })


    }

    //class used to take the edges of any imageView
    class RectEdges(imageView: ImageView, location: MutableList<Float>) {
        /*

        var topLeft = mutableListOf<Float>()
        var topRight = mutableListOf<Float>()
        var bottomLeft = mutableListOf<Float>()
        var bottomRight = mutableListOf<Float>()

        init {
            topLeft = mutableListOf(location[0], location[1])
            topRight = mutableListOf(location[0] + imageView.width, location[1])
            bottomLeft = mutableListOf(location[0], location[1] + imageView.height)
            bottomRight = mutableListOf(location[0] + imageView.width, location[1] + imageView.height)
        }
        */

        var top = location[1]
        var left = location[0]
        var bottom = location [1] + imageView.height
        var right = location[0] + imageView.width
    }





    fun updateBallPosition(ball: ImageView, screenHeight: Int, screenWidth: Int, paddle: ImageView) {

        bounceInScreenSides(ball, screenHeight, screenWidth)

        bounceInPaddle(ball, paddle)






    }

}

fun bounceInScreenSides(ball: ImageView, screenHeight: Int, screenWidth: Int) {
    var ballLocation = mutableListOf<Float>(ball.x, ball.y)
    var ballEdges = MainActivity.RectEdges(ball, ballLocation)

    if(ballEdges.bottom + (standardSpeedY/2 + 1) >= screenHeight) {
        standardSpeedY = -standardSpeedY
    } else if(ballEdges.top + (standardSpeedY/2 - 1) <= 0) {
        standardSpeedY = -standardSpeedY
    }

    if(ballEdges.right + (standardSpeedX/2 + 1) >= screenWidth) {
        standardSpeedX = -standardSpeedX
    } else if(ballEdges.left + (standardSpeedX/2 - 1) <= 0) {
        standardSpeedX = -standardSpeedX
    }

    // Move the paddle to the x position of the touch event
    ball.x = ball.x + standardSpeedX
    ball.y = ball.y + standardSpeedY
}

fun bounceInPaddle(ball: ImageView, paddle: ImageView) {
    var ballLocation = mutableListOf<Float>(ball.x, ball.y)
    var ballEdges = MainActivity.RectEdges(ball, ballLocation)

    var paddleLocation = mutableListOf<Float>(paddle.x, paddle.y)
    var paddleEdges = MainActivity.RectEdges(paddle, paddleLocation)

    if(ballEdges.bottom + (standardSpeedY/2 + 1) >= paddleEdges.top) {
        if(ballEdges.right >= paddleEdges.left && ballEdges.left <= paddleEdges.right) {
            standardSpeedY = -standardSpeedY
        }
    }
}