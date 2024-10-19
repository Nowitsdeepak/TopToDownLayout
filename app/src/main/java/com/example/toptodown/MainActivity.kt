package com.example.toptodown

import android.os.Bundle
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var clMain: View
    private lateinit var mainLayout: View
    private var prevY: Float = 0f
    private var startY: Float = 0f
    private var isExpanded = false
    private val threshold = 200f  // Threshold to snap up/down
    private lateinit var velocityTracker: VelocityTracker
    private var isDragging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clMain = findViewById(R.id.clMain)
        mainLayout = findViewById(R.id.main)
        velocityTracker = VelocityTracker.obtain()

        setupDragBehavior()
    }

    private fun setupDragBehavior() {
        // Handle touch events on the entire main layout
        mainLayout.setOnTouchListener { _, event ->
            if (isExpanded && isTouchInsideView(event, clMain)) {
                // If expanded and touch is inside clMain, don't handle drag
                return@setOnTouchListener false
            }

            // Otherwise, handle drag events
            velocityTracker.addMovement(event)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    prevY = event.rawY
                    startY = clMain.translationY
                    isDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - prevY

                    if (abs(dy) > 10) {
                        isDragging = true
                        val newTranslationY = (startY + dy).coerceIn(-clMain.height.toFloat(), 0f)
                        clMain.translationY = newTranslationY
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        handleDragEnd()
                    }
                    velocityTracker.clear()
                }
            }
            true
        }
    }

    private fun handleDragEnd() {
        velocityTracker.computeCurrentVelocity(1000)  // Compute velocity in pixels/second
        val yVelocity = velocityTracker.yVelocity

        if (yVelocity > 1000 || clMain.translationY > -threshold) {
            // Expand (snap to fully visible)
            animateTranslation(0f)
            isExpanded = true
        } else if (yVelocity < -1000 || clMain.translationY <= -threshold) {
            // Collapse (snap to fully hidden)
            animateTranslation(-clMain.height.toFloat())
            isExpanded = false
        }
    }

    private fun animateTranslation(targetY: Float) {
        clMain.animate()
            .translationY(targetY)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(300)
            .start()
    }

    private fun isTouchInsideView(event: MotionEvent, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()

        val viewLeft = location[0]
        val viewRight = viewLeft + view.width
        val viewTop = location[1]
        val viewBottom = viewTop + view.height

        return x in viewLeft..viewRight && y in viewTop..viewBottom
    }

    override fun onDestroy() {
        super.onDestroy()
        velocityTracker.recycle()
    }
}
