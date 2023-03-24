package com.example.arkanoid0_1

import android.content.Context
import android.graphics.Rect
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

//private lateinit var ballImageView: ImageView
private lateinit var handler: Handler
private const val updateInterval = 16L // Update every 16 milliseconds (approx. 60 FPS)

private var standardSpeedY = 6
private var standardSpeedX = 6

val ballSpeed = Speed(standardSpeedX, standardSpeedY)



class MainActivity : AppCompatActivity() {
    lateinit var paddle: ImageView
    lateinit var ballImageView: ImageView

    var screenHeight = 0
    var screenWidth = 0

    lateinit var rect: MutableList<ImageView>

    var rectIsLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        paddle = findViewById<ImageView>(R.id.paddleImage)
        ballImageView = findViewById<ImageView>(R.id.ballImage)


        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val navBarHeight = resources.getDimensionPixelSize(resourceId)
            // Use navBarHeight as needed
            screenWidth = screenWidth + navBarHeight
        }





        // TODO:  I need to eliminate those magic numbers
        var rect = generateTiles(9, 4, "rule1", screenWidth, 250,
            160, 0.6f)





        var touchX0 = 0f
        var isBottomHalfTouched = false
        val parentLayout = findViewById<View>(android.R.id.content)
        // Attach a touch listener to the parent layout
        parentLayout.setOnTouchListener { _, motionEvent ->

            Log.d("testing touch", "ocorreu um toque")
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    //this condition could be some padding around the paddle,
                    // maybe it will be necessary when there are more things in the screen, like items
                    if (motionEvent.y > 4*screenHeight / 5) {
                        touchX0 = motionEvent.rawX
                        // Touch is in the bottom half of the screen
                        isBottomHalfTouched = true
                        Log.d("testing touch", "tocou debaixo da tela")
                        true // Consume the event
                    } else {
                        false // Don't consume the event
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isBottomHalfTouched) {
                        //Log.d("testing touch", "tocou debaixo da tela e moveu")
                        // Move the paddle based on the amount of displacement
                        var displacement = motionEvent.rawX - touchX0
                        paddle.x = paddle.x + displacement
                        touchX0 = motionEvent.rawX
                        Log.d("testing touch", "tocou debaixo da tela e moveu")
                        true // Consume the event
                    } else {
                        false // Don't consume the event
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d("testing touch", "soltou o dedo")
                    // Reset the touch state when the finger is lifted or canceled
                    isBottomHalfTouched = false
                    true // Consume the event
                }
                else -> false // Don't consume other touch events
            }
        }




        handler = Handler(Looper.getMainLooper())


        // Start the update loop
        handler.post(object : Runnable {
            override fun run() {
                // Update the position of the ball
                if(rectIsLoaded) {
                    updateBallPosition(ballImageView, screenHeight, screenWidth, paddle, rect)
                }

                //preciso tirar o rect do argumento do updateBallPosition()
                // - será que é possível?
                //fazer o updateBallPosition acontecer só depois de carregar os rects


                // Schedule the next update
                handler.postDelayed(this, updateInterval)
            }
        })



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


    override fun onPause() {
        super.onPause()

        // Save the current state of the app to SharedPreferences
        val stageState = getSharedPreferences("stageState", Context.MODE_PRIVATE)
        val stageStateEditor = stageState.edit()


        //stageStateEditor.putFloat("ballX", ballImageView.x)
        //stageStateEditor.putFloat("ballY", ballImageView.y)

        stageStateEditor.putInt("ballSpeedX", ballSpeed.x)
        stageStateEditor.putInt("ballSpeedY", ballSpeed.y)

        ballSpeed.x = 0
        ballSpeed.y = 0


        //stageStateEditor.putFloat("paddleX", findViewById<ImageView>(R.id.paddleImage).x) //nao consigo usar a variável paddle...
        //stageStateEditor.putFloat("paddleY", paddle.y)






        // ... save other state variables ...



        stageStateEditor.apply()
    }


    override fun onResume() {
        super.onResume()
        // TODO: implementar o que está nos comentários abaixo 
        //tem que cuidar que essa função carrega logo depois da onCreate, ou seja, antes de pausar alguma vez
        //é importante setar os padrões aqui na primeira vez que carrega
        //if(controle) {carregar padrões com stageStateEditor.putFloat("ballX", ballImageView.x), entre outros}


        // Re-apply immersive flags when the activity resumes
        window.decorView.systemUiVisibility = immersiveFlags


        // Retrieve the saved state from SharedPreferences and restore the app
        val stageState = getSharedPreferences("stageState", Context.MODE_PRIVATE)

        //ballImageView.x = stageState.getFloat("ballX", 0f) // TODO: trocar esse default value para metade do tamanho da tela, mas o escopo dificulta um pouco
        //ballImageView.y = stageState.getFloat("ballY", 0f)

        ballSpeed.x = stageState.getInt("ballSpeedX", standardSpeedX)
        ballSpeed.y = stageState.getInt("ballSpeedY", standardSpeedY)

        //paddle.y = paddle.y
        //findViewById<ImageView>(R.id.paddleImage).x = stageState.getFloat("paddleX", 0f)

        //ballSpeed.x = stageState.getInt("ballSpeedX", 0)

        // ... retrieve other state variables ...

        // ... restore other state variables ...


    }

    fun collision(ball: ImageView): Int {
        var ballLocation = mutableListOf<Float>(ball.x, ball.y)
        var ballEdges = RectEdges(ball, ballLocation)

        for (i in 0 until obstacleRectEdges.size) {
            //the +1 or -1 in (ballSpeed.y / 2 - 1) are been used because it is better the ball going deeper into the obstacle than bouncing earlier
            //maybe it could be removed


            if(ballSpeed.y > 0) { //going down
                    //ballSpeed.y / 2 - 1
                    if(ballEdges.bottom + (ballSpeed.y / 2 - 1) >= obstacleRectEdges[i].top && ballEdges.bottom <= obstacleRectEdges[i].top || ballEdges.bottom >= obstacleRectEdges[i].top && ballEdges.bottom - ballSpeed.y <= obstacleRectEdges[i].top) {
                        if(ballEdges.right + ballSpeed.x >= obstacleRectEdges[i].left && ballEdges.left + ballSpeed.x <= obstacleRectEdges[i].right) {
                            ballSpeed.y = -ballSpeed.y
                            return i
                        }
                    }
            } else { //ballSpeed.y < 0 //going up
                //ballSpeed.y / 2 + 1
                //trocar os top/bottom, >/<, +1/-1
                if(ballEdges.top + (ballSpeed.y / 2 + 1) <= obstacleRectEdges[i].bottom && ballEdges.top >= obstacleRectEdges[i].bottom || ballEdges.top <= obstacleRectEdges[i].bottom && ballEdges.top - ballSpeed.y >= obstacleRectEdges[i].bottom) {
                    if(ballEdges.right + ballSpeed.x >= obstacleRectEdges[i].left && ballEdges.left + ballSpeed.x <= obstacleRectEdges[i].right) {
                        ballSpeed.y = -ballSpeed.y
                        return i
                    }
                }
            }


            //change all X/Y
            //bottom -> right
            //top -> left
            //right -> bottom
            //left -> top
            if(ballSpeed.x > 0) { //going right
                //ballSpeed.x / 2 - 1
                if(ballEdges.right + (ballSpeed.x / 2 - 1) >= obstacleRectEdges[i].left && ballEdges.right <= obstacleRectEdges[i].left || ballEdges.right >= obstacleRectEdges[i].left && ballEdges.right - ballSpeed.x <= obstacleRectEdges[i].left) {
                    if(ballEdges.bottom + ballSpeed.y >= obstacleRectEdges[i].top && ballEdges.top + ballSpeed.y <= obstacleRectEdges[i].bottom) {
                        ballSpeed.x = -ballSpeed.x
                        return i
                    }
                }
            } else { //ballSpeed.y < 0 //going left
                //ballSpeed.x / 2 + 1
                if(ballEdges.left + (ballSpeed.x / 2 + 1) <= obstacleRectEdges[i].right && ballEdges.left >= obstacleRectEdges[i].right || ballEdges.left <= obstacleRectEdges[i].right && ballEdges.left - ballSpeed.x >= obstacleRectEdges[i].right) {
                    if(ballEdges.bottom + ballSpeed.y >= obstacleRectEdges[i].top && ballEdges.top + ballSpeed.y <= obstacleRectEdges[i].bottom) {
                        ballSpeed.x = -ballSpeed.x
                        return i
                    }
                }
            }


        }
        //verify if there is a collision with the obstacles(rectangles)
        //which color is the obstacle? (then, change() the color or remove obstacle)
        return -1
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // Re-apply immersive flags when the window focus changes
        if (hasFocus) {
            window.decorView.systemUiVisibility = immersiveFlags
        }


        //moved to onCreate
        //setContentView(R.layout.activity_main)

        /* moved to onCreate
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels
        var screenWidth = displayMetrics.widthPixels
        */


        /* moved to onCreate
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            val navBarHeight = resources.getDimensionPixelSize(resourceId)
            // Use navBarHeight as needed
            screenWidth = screenWidth + navBarHeight
        }
        */

        //moved to onCreate
        //val paddle = findViewById<ImageView>(R.id.paddleImage)


        /* moved to onCreate
        // TODO:  I need to eliminate those magic numbers
        var rect = generateTiles(9, 4, "rule1", screenWidth, 250,
            160, 0.6f)
        */

        /* moved to onCreate
        var touchX0 = 0f
        var isBottomHalfTouched = false
        val parentLayout = findViewById<View>(android.R.id.content)
        // Attach a touch listener to the parent layout
        parentLayout.setOnTouchListener { _, motionEvent ->

            Log.d("testing touch", "ocorreu um toque")
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    //this condition could be some padding around the paddle,
                    // maybe it will be necessary when there are more things in the screen, like items
                    if (motionEvent.y > 4*screenHeight / 5) {
                        touchX0 = motionEvent.rawX
                        // Touch is in the bottom half of the screen
                        isBottomHalfTouched = true
                        Log.d("testing touch", "tocou debaixo da tela")
                        true // Consume the event
                    } else {
                        false // Don't consume the event
                    }
               }
                MotionEvent.ACTION_MOVE -> {
                    if (isBottomHalfTouched) {
                        //Log.d("testing touch", "tocou debaixo da tela e moveu")
                        // Move the paddle based on the amount of displacement
                        var displacement = motionEvent.rawX - touchX0
                        paddle.x = paddle.x + displacement
                        touchX0 = motionEvent.rawX
                        Log.d("testing touch", "tocou debaixo da tela e moveu")
                        true // Consume the event
                    } else {
                        false // Don't consume the event
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Log.d("testing touch", "soltou o dedo")
                    // Reset the touch state when the finger is lifted or canceled
                    isBottomHalfTouched = false
                    true // Consume the event
                }
                else -> false // Don't consume other touch events
            }
        }
        */



        //moved to onCreate
        //val ballImageView = findViewById<ImageView>(R.id.ballImage)

        /*
        //moved to onCreate
        handler = Handler(Looper.getMainLooper())

        // Start the update loop
        handler.post(object : Runnable {
            override fun run() {
                // Update the position of the ball
                updateBallPosition(ballImageView, screenHeight, screenWidth, paddle, rect)


                // Schedule the next update
                handler.postDelayed(this, updateInterval)
            }
        })

        */

    }

    //class used to take the edges of any imageView
    class RectEdges(imageView: ImageView, location: MutableList<Float>, id: Int = -1) {
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
        var id = id
    }

    var obstacleRectEdges = mutableListOf<RectEdges>()
    var tempObstacleLocationX = mutableListOf<Float>()
    var tempObstacleLocationY = mutableListOf<Float>()

    fun generateTiles(amountPerLine: Int, lines: Int, rule: String, screenWidth: Int,
                      marginX: Int, marginY:Int, scale: Float): MutableList<ImageView> {
        //rule will be used to make different stages, with holes, "pictures", etc.
        //I have to check and decrease the scale if the screen cant handle the amount of tiles


        //creating the first ImageView
        var firstImageView = ImageView(this)
        firstImageView.setImageResource(R.drawable.element_blue_rectangle)

        // Generate a unique ID for the ImageView
        var id = View.generateViewId();

        // Set the ID for the ImageView
        firstImageView.id = id;

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



        rect = mutableListOf<ImageView>()

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


                    //var rect = mutableListOf<ImageView>()
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

                            // Generate a unique ID for the ImageView
                            var id = View.generateViewId();

                            // Set the ID for the ImageView
                            rect[k].id = id;


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
                                    obstacleRectEdges[k].id = rect[k].id
                                }
                                var idToRemove = firstImageView.id
                                val viewToRemove = findViewById<ImageView>(idToRemove)
                                val parentView = findViewById<ConstraintLayout>(R.id.parent_layout)
                                parentView.removeView(viewToRemove)
                            }
                        }
                    )


                }
            }
        )
        //Log.d("Trying to get width", "firstImageView.width = ${firstImageView.width}")


        rectIsLoaded = true
        return rect
    }

    fun eliminateObstacle(index: Int, obstacles: MutableList<ImageView>) {
        var removedIndexes: MutableList<Int> = mutableListOf()
        val parentView = findViewById<ConstraintLayout>(R.id.parent_layout)

        //parentView.removeView(obstacles[index])
        //obstacles.removeAt(index)



        var idToRemove = obstacleRectEdges[index].id
        val viewToRemove = findViewById<ImageView>(idToRemove)
        parentView.removeView(viewToRemove)

        obstacleRectEdges.removeAt(index)
        //parentView.removeView(obstacles[index])



    }



    fun updateBallPosition(ball: ImageView, screenHeight: Int, screenWidth: Int, paddle: ImageView, obstacles: MutableList<ImageView>) {


        var collisionIndex = -1 //-1 means there was no collision
        bounceInPaddle(ball, paddle)
        collisionIndex = collision(ball)
        bounceInScreenSides(ball, screenHeight, screenWidth)

        moveBall(ball, ballSpeed)

        if(collisionIndex > -1) {
            eliminateObstacle(collisionIndex, obstacles)
        }



    }

}



fun moveBall(ball: ImageView, ballSpeed: Speed) {

    // Move the paddle to the x position of the touch event
    ball.x = ball.x + ballSpeed.x
    ball.y = ball.y + ballSpeed.y
}

fun bounceInScreenSides(ball: ImageView, screenHeight: Int, screenWidth: Int) {
    var ballLocation = mutableListOf<Float>(ball.x, ball.y)
    var ballEdges = MainActivity.RectEdges(ball, ballLocation)

    if(ballEdges.bottom + (ballSpeed.y/2 + 1) >= screenHeight) {
        ballSpeed.y = -ballSpeed.y
    } else if(ballEdges.top + (ballSpeed.y/2 - 1) <= 0) {
        ballSpeed.y = -ballSpeed.y
    }

    if(ballEdges.right + (ballSpeed.x/2 + 1) >= screenWidth) {
        ballSpeed.x = -ballSpeed.x
    } else if(ballEdges.left + (ballSpeed.x/2 - 1) <= 0) {
        ballSpeed.x = -ballSpeed.x
    }



}

//calculate the speed in order to apply() an effect in the ball
fun bounceInPaddle(ball: ImageView, paddle: ImageView) {

    var ballLocation = mutableListOf<Float>(ball.x, ball.y)
    var ballEdges = MainActivity.RectEdges(ball, ballLocation)

    var paddleLocation = mutableListOf<Float>(paddle.x, paddle.y)
    var paddleEdges = MainActivity.RectEdges(paddle, paddleLocation)

    if(ballEdges.bottom + (ballSpeed.y/2 + 1) >= paddleEdges.top) {
        if(ballEdges.right >= paddleEdges.left && ballEdges.left <= paddleEdges.right) {
            ballSpeed.y = -ballSpeed.y
        }
    }
}

class Speed(standardSpeedX: Int, standardSpeedY: Int) {
    var x: Int
    var y: Int
    init {
        x = standardSpeedX
        y = standardSpeedY
    }
}



// TODO: change some orders in the code
// TODO: substitute some code for functions and classes

// TODO: create the rectCollision function ??
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

// TODO: Criar um tile que quando bate acumula velocidade (ou fazer isso em todos, a cada x batidas?)
//  - criar uma barrinha que mostre quanto falta para aumentar a velocidade

// TODO: Adicionar mecânica que cria um contorno para o paddle e se o paddle ficar tempo ali dentro ganha alguma coisa:
//  item, pontos, destrói peça, dá golpe em chefão, etc


