package com.example.draganddropcoroutinecancellation


import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*


// callbackFlow creates code flow
suspend fun View.dragIt(): Flow<MotionEvent> = callbackFlow {


    this@dragIt.setOnTouchListener { _, event ->
        offer(event)
    }

    // called when canceled or closed
    awaitClose { this@dragIt.setOnTouchListener(null) }

}






class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageView = findViewById<ImageView>(R.id.image)





        // GlobalScope running pon application lifecycle
        // launch creates non blocking coroutine

        GlobalScope.launch {


            val touch = imageView.dragIt()

            val touchDownGesture =
                touch.filter { motionEvent -> motionEvent.action == MotionEvent.ACTION_DOWN }
            val moveGesture =
                touch.filter { motionEvent -> motionEvent.action == MotionEvent.ACTION_MOVE }
            val touchUpGesture =
                touch.filter { motionEvent -> motionEvent.action == MotionEvent.ACTION_UP }



            touchDownGesture.flatMapConcat { initialTouch ->

                val firstCoordinate = Pair(initialTouch.x, initialTouch.y)
                Log.i("tagg", "${initialTouch}")



                moveGesture.map { moveCoordinate ->


                    val x = moveCoordinate.x - firstCoordinate.first
                    val y = moveCoordinate.y - firstCoordinate.second
                    Pair(x, y)

                }


                }
                .collect {

                Log.i("tagg", "${it.first} ${it.second}")

                imageView.x = imageView.x + it.first
                imageView.y = imageView.y + it.second
            }
            }
        }
    }

