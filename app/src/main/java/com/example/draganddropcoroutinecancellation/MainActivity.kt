package com.example.draganddropcoroutinecancellation


import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

// callbackFlow creates code flow
suspend fun View.dragAndDrop(): Flow<MotionEvent> = callbackFlow {

    this@dragAndDrop.setOnTouchListener { _, event ->
        offer(event)
    }

    // called when cancelled or closed
    awaitClose { this@dragAndDrop.setOnTouchListener(null) }
}


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageView = findViewById<ImageView>(R.id.image)

        // GlobalScope running pon application lifecycle
        // launch creates non blocking coroutine
        GlobalScope.launch {

            val touch = imageView.dragAndDrop()

            val touchDownGesture =
                touch.filter { motionEvent -> motionEvent.action == MotionEvent.ACTION_DOWN }
            val moveGesture =
                touch.filter { motionEvent -> motionEvent.action == MotionEvent.ACTION_MOVE }
            val touchUpGesture =
                touch.filter { motionEvent -> motionEvent.action == MotionEvent.ACTION_UP }


            touchDownGesture.flatMapConcat {

                val originalPosition = Pair(imageView.x , imageView.y)

                moveGesture.map { moveCoordinate ->
                    val x = moveCoordinate.x - originalPosition.first
                    val y = moveCoordinate.y - originalPosition.second

                    Pair(x, y)
                }
            }
                .flatMapLatest{
                    touchUpGesture
                    flow { emit(it) }
                }
//                .takeUntil(touchUpGesture)
                .collect {
                    imageView.x += it.first
                    imageView.y += it.second
                }
        }
    }
}

