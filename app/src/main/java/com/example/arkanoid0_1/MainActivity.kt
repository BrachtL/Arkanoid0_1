package com.example.arkanoid0_1

import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout

private var immersiveFlags: Int = 0

private lateinit var ballImageView: ImageView
private lateinit var handler: Handler
private const val updateInterval = 16L // Update every 16 milliseconds (approx. 60 FPS)

private var standardSpeedY = 6
private var standardSpeedX = 6


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

    // TODO: Implement this collision function
    // TODO: gravei um vídeo mostrando uma colisão com standardSpeedX negativo e standardSpeedY positivo que não aconteceu, verificar
    fun collision(ball: ImageView): Boolean{
        var ballLocation = mutableListOf<Float>(ball.x, ball.y)
        var ballEdges = RectEdges(ball, ballLocation)

        for (i in 0 until obstacleRectEdges.size) {
            //the +1 or -1 in (standardSpeedY / 2 - 1) are been used because it is better the ball going deeper into the obstacle than bouncing earlier
            //maybe it could be removed


            if(standardSpeedY > 0) { //going down
                    //standardSpeedY / 2 - 1
                    if(ballEdges.bottom + (standardSpeedY / 2 - 1) >= obstacleRectEdges[i].top && ballEdges.bottom <= obstacleRectEdges[i].top || ballEdges.bottom >= obstacleRectEdges[i].top && ballEdges.bottom - standardSpeedY <= obstacleRectEdges[i].top) {
                        if(ballEdges.right + standardSpeedX >= obstacleRectEdges[i].left && ballEdges.left + standardSpeedX <= obstacleRectEdges[i].right) {
                            standardSpeedY = -standardSpeedY
                            return true
                        }
                    }
            } else { //standardSpeedY < 0 //going up
                //standardSpeedY / 2 + 1
                //trocar os top/bottom, >/<, +1/-1
                if(ballEdges.top + (standardSpeedY / 2 + 1) <= obstacleRectEdges[i].bottom && ballEdges.top >= obstacleRectEdges[i].bottom || ballEdges.top <= obstacleRectEdges[i].bottom && ballEdges.top - standardSpeedY >= obstacleRectEdges[i].bottom) {
                    if(ballEdges.right + standardSpeedX >= obstacleRectEdges[i].left && ballEdges.left + standardSpeedX <= obstacleRectEdges[i].right) {
                        standardSpeedY = -standardSpeedY
                        return true
                    }
                }
            }


            //change all X/Y
            //bottom -> right
            //top -> left
            //right -> bottom
            //left -> top
            if(standardSpeedX > 0) { //going down
                //standardSpeedX / 2 - 1
                if(ballEdges.right + (standardSpeedX / 2 - 1) >= obstacleRectEdges[i].left && ballEdges.right <= obstacleRectEdges[i].left || ballEdges.right >= obstacleRectEdges[i].left && ballEdges.right - standardSpeedX <= obstacleRectEdges[i].left) {
                    if(ballEdges.bottom + standardSpeedY >= obstacleRectEdges[i].top && ballEdges.top + standardSpeedY <= obstacleRectEdges[i].bottom) {
                        standardSpeedX = -standardSpeedX
                        return true
                    }
                }
            } else { //standardSpeedY < 0 //going up
                //standardSpeedX / 2 + 1
                if(ballEdges.left + (standardSpeedX / 2 + 1) <= obstacleRectEdges[i].right && ballEdges.left >= obstacleRectEdges[i].right || ballEdges.left <= obstacleRectEdges[i].right && ballEdges.left - standardSpeedX >= obstacleRectEdges[i].right) {
                    if(ballEdges.bottom + standardSpeedY >= obstacleRectEdges[i].top && ballEdges.top + standardSpeedY <= obstacleRectEdges[i].bottom) {
                        standardSpeedX = -standardSpeedX
                        return true
                    }
                }
            }


        }
        //verify if there is a collision with the obstacles(rectangles)
        //which color is the obstacle? (then, change() the color or remove obstacle)
        return false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // Re-apply immersive flags when the window focus changes
        if (hasFocus) {
            window.decorView.systemUiVisibility = immersiveFlags
        }



        setContentView(R.layout.activity_main)


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



        // TODO:  I need to eliminate those magic numbers
        generateTiles(9, 4, "rule1", screenWidth, 250,
            160, 0.6f)

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

    var obstacleRectEdges = mutableListOf<RectEdges>()
    var tempObstacleLocationX = mutableListOf<Float>()
    var tempObstacleLocationY = mutableListOf<Float>()

    fun generateTiles(amountPerLine: Int, lines: Int, rule: String, screenWidth: Int,
                      marginX: Int, marginY:Int, scale: Float) {
        //rule will be used to make different stages, with holes, "pictures", etc.
        //I have to check and decrease the scale if the screen cant handle the amount of tiles


        //creating the first ImageView
        var firstImageView = ImageView(this)
        firstImageView.setImageResource(R.drawable.element_blue_rectangle)

        firstImageView.scaleX = scale
        firstImageView.scaleY = scale


        // set other attributes such as layout params, scale type, etc.
        // for example, to set layout params to match parent:

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        //params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        //params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID


        firstImageView.layoutParams = params

        // add the ImageView to a parent view
        val parentView =
            findViewById<ConstraintLayout>(R.id.parent_layout)
        parentView.addView(firstImageView)


        params.topMargin = marginY
        params.leftMargin = marginX




        //I need to create this firstImageView in order to get some values that only exists after
        //the imageView is created, like width and height attributes
        firstImageView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                //I need this to wait the imageView is created, in order to get its properties
                override fun onGlobalLayout() {
                    // Remove the listener to avoid multiple callbacks
                    firstImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Get the width of the ImageView
                    //Log.d("Trying to get width", "firstImageView.width = ${firstImageView.width}")

                    var distanceBetweenTopLeftCornerX = (screenWidth - 2 * marginX - firstImageView.width)/(amountPerLine-1)

                    var gapX = distanceBetweenTopLeftCornerX - firstImageView.width
                    var gapY = gapX


                    /*
                    //for debug purposes: gapX is negative in older/smaller smartphones
                    Log.d("testing rects", "firstImageView.width: ${firstImageView.width}\n" +
                            "marginY = $marginY\n" +
                            "firstImageView.height = ${firstImageView.height}")
                    println("distanceBetweenTopLeftCornerX: ${distanceBetweenTopLeftCornerX}\n" +
                            "firstImageView.width = ${firstImageView.width}\n" +
                            "firstImageView.height = ${firstImageView.height}")

                    */

                    if(gapX <= 0) {
                        //impossible scenario
                        //I have to change the scale
                    }


                    var rect = mutableListOf<ImageView>()
                    var k = 0
                    var j = 0

                    //var obstacleRectEdges = mutableListOf<RectEdges>()
                    //var tempObstacleLocation = mutableListOf<Float>(0f,0f)


                    for(i in 0 until lines) {
                        while(j < amountPerLine) {

                            rect.add(ImageView(this@MainActivity))
                            //rect[k] = ImageView(this)
                            rect[k].setImageResource(R.drawable.element_blue_rectangle)
                            // set other attributes such as layout params, scale type, etc.
                            // for example, to set layout params to match parent:

                            rect[k].scaleX = scale
                            rect[k].scaleY = scale

                            val params = ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                            )

                            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID


                            rect[k].layoutParams = params


                            val parentView = findViewById<ConstraintLayout>(R.id.parent_layout)
                            parentView.addView(rect[k])


                            params.topMargin = marginY + gapY*i + firstImageView.height*i
                            params.leftMargin = marginX + distanceBetweenTopLeftCornerX*j



                            tempObstacleLocationY.add(params.topMargin.toFloat())
                            tempObstacleLocationX.add(params.leftMargin.toFloat())



                            k++
                            j++


                        }
                        j = 0
                    }

                    rect[rect.size-1].viewTreeObserver.addOnGlobalLayoutListener(
                        object : ViewTreeObserver.OnGlobalLayoutListener {
                            //I need this to wait the imageView is created, in order to get its properties
                            override fun onGlobalLayout() {

                                var height: Float
                                var width: Float

                                for (k in 0 until rect.size) {

                                    // Remove the listener to avoid multiple callbacks
                                    rect[k].viewTreeObserver.removeOnGlobalLayoutListener(this)


                                    obstacleRectEdges.add(RectEdges(rect[k], mutableListOf<Float>(rect[k].x, rect[k].y)))

                                    height = obstacleRectEdges[k].bottom - obstacleRectEdges[k].top
                                    width = obstacleRectEdges[k].right - obstacleRectEdges[k].left

                                    obstacleRectEdges[k].top+= (1-scale)*height/2
                                    obstacleRectEdges[k].bottom-= (1-scale)*height/2
                                    obstacleRectEdges[k].right-= (1-scale)*width/2
                                    obstacleRectEdges[k].left+= (1-scale)*width/2
                                }

                            }
                        }
                    )


                }
            }
        )
        //Log.d("Trying to get width", "firstImageView.width = ${firstImageView.width}")


    }



    fun updateBallPosition(ball: ImageView, screenHeight: Int, screenWidth: Int, paddle: ImageView) {


        bounceInPaddle(ball, paddle)
        collision(ball)



        // TODO: I have to create an independent function for move..
        // TODO: ..after check if there is an obstacle (that will change the speed sign)
        bounceInScreenSides(ball, screenHeight, screenWidth)


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

//calculate the speed in order to apply() an effect in the ball
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





// TODO: change some orders in the code
// TODO: substitute some code for functions and classes

// TODO: make the touch in the bottom half side of the screen works as if touches in the paddle

// TODO: create the rectCollision function
// TODO: create the effect of speed when the paddle touches the ball

// TODO: change the bounce function not to bounce bottom
// TODO: create defeat function and a system of lives

// TODO: allow multiple balls and create the scenario when more balls come

// TODO: put music
// TODO: put sound effects

// TODO: create more rules for stages

// TODO: create an online mode, when the players throw things each other

// TODO: create visual effects when the ball hits the rects

// TODO: resolver bug dos tiles colados em celular com telas menores
// TODO: reoslver bug dos celular que tem nav buttons fixos (tirar a função que desconsidera eles)
//é possível fazer uma função para desativar isso no menu, mas não é o ideal

// TODO: golpes "especiais" (sinais na tela, multitouch, clicar em algo, itens especiais)

// TODO: construir um BallEdges que faça o contorno de figuras circulares
// TODO: criar variáveis para armazenar os quadrantes
// TODO: na função collision, considerar apenas o quadrante que importa, com relação à velocidade

