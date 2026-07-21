package com.example.adaptiveisland.ui.overlay

import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.WindowInsets
import android.os.Build
import android.graphics.Rect
import kotlin.math.abs

/**
 * Interface mapping layout updates up to the parent persistent layer.
 */
interface OnPositionChangeListener {
    fun onPositionChanged(x: Int, y: Int)
}

/**
 * Handles single-touch drag tracking loops for the Adaptive Island, delivering stable 60 FPS updates.
 * Features boundary metric caching, fallback layout measurement resolving, and race-safe updates.
 */
class IslandGestureHandler(
    private val windowManager: WindowManager,
    private val targetView: View,
    private val layoutParams: WindowManager.LayoutParams,
    private val changeListener: OnPositionChangeListener
) : View.OnTouchListener {

    private val touchSlop = ViewConfiguration.get(targetView.context).scaledTouchSlop
    private val safeBoundsCache = Rect()
    
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isDragging = false

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.pointerCount > 1) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = layoutParams.x
                initialY = layoutParams.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                
                // Cache boundaries exactly once during tap phase initialization to maximize performance
                calculateSafeScreenBounds(safeBoundsCache)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY

                // Filter out small accidental finger movements to maintain reliable click mechanics
                if (!isDragging && (abs(deltaX) > touchSlop || abs(deltaY) > touchSlop)) {
                    isDragging = true
                }

                if (isDragging) {
                    val targetX = initialX + deltaX.toInt()
                    val targetY = initialY + deltaY.toInt()

                    // Guard against unmeasured dimensions or WRAP_CONTENT/MATCH_PARENT layout configurations (-2 / -1)
                    val viewWidth = when {
                        targetView.width > 0 -> targetView.width
                        layoutParams.width > 0 -> layoutParams.width
                        else -> 0
                    }
                    val viewHeight = when {
                        targetView.height > 0 -> targetView.height
                        layoutParams.height > 0 -> layoutParams.height
                        else -> 0
                    }

                    val maxX = safeBoundsCache.right - viewWidth
                    val maxY = safeBoundsCache.bottom - viewHeight

                    layoutParams.x = targetX.coerceIn(safeBoundsCache.left, maxX)
                    layoutParams.y = targetY.coerceIn(safeBoundsCache.top, maxY)

                    // Safely patch window transformations to protect against parallel teardown operations
                    if (targetView.isAttachedToWindow) {
                        try {
                            windowManager.updateViewLayout(targetView, layoutParams)
                        } catch (e: IllegalArgumentException) {
                            // Recover gracefully if window was removed exactly during transformation execution
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    changeListener.onPositionChanged(layoutParams.x, layoutParams.y)
                    isDragging = false
                    return true
                } else if (event.action == MotionEvent.ACTION_UP) {
                    view.performClick()
                    return true
                }
            }
        }
        return false
    }

    /**
     * Resolves visible boundary dimensions utilizing API 30+ capabilities.
     * Computes the system surface area avoiding notches, control frames, or cutout partitions.
     */
    private fun calculateSafeScreenBounds(outRect: Rect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val windowBounds = windowMetrics.bounds
            
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout()
            )
            
            outRect.set(
                windowBounds.left + insets.left,
                windowBounds.top + insets.top,
                windowBounds.right - insets.right,
                windowBounds.bottom - insets.bottom
            )
        } else {
            val display = windowManager.defaultDisplay
            val displayMetrics = android.util.DisplayMetrics()
            display.getRealMetrics(displayMetrics)
            outRect.set(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }
}