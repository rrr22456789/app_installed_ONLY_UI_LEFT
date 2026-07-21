package com.example.adaptiveisland.ui.overlay

import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.TextView
import com.example.adaptiveisland.R
import com.example.adaptiveisland.data.settings.AdaptiveIslandPreferences
import com.example.adaptiveisland.tracker.ActiveSessionState

/**
 * Passive overlay view responsible only for rendering UI.
 */
class IslandOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val tvAppName: TextView
    private val tvTimer: TextView

    private var cornerRadiusPx: Float = 0f

    private var positionChangeListener: ((Int, Int) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_island_overlay, this, true)

        tvAppName = findViewById(R.id.tvAppName)
        tvTimer = findViewById(R.id.tvSessionTime)

        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val radius =
                    if (cornerRadiusPx > 0f) cornerRadiusPx else view.height / 2f

                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    view.height,
                    radius
                )
            }
        }

        clipToOutline = true
    }

    override fun onSizeChanged(
        w: Int,
        h: Int,
        oldw: Int,
        oldh: Int
    ) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidateOutline()
    }

    /**
     * Updates UI from immutable session state.
     */
    fun updateSessionState(state: ActiveSessionState) {
        tvAppName.text = state.appName
        tvTimer.text = state.formattedTime
    }

    /**
     * Applies visual preferences.
     */
    fun applyPreferences(preferences: AdaptiveIslandPreferences) {
        val expectedRadius = preferences.capsuleHeight / 2f

        if (cornerRadiusPx != expectedRadius) {
            cornerRadiusPx = expectedRadius
            invalidateOutline()
        }
    }

    /**
     * Compatibility API used by OverlayForegroundService.
     */
    fun renderSessionMetrics(
        appName: String,
        formattedTime: String
    ) {
        tvAppName.text = appName
        tvTimer.text = formattedTime
    }

    /**
     * Compatibility API used by OverlayForegroundService.
     */
    fun updateLayoutDimensions(
        width: Int,
        height: Int
    ) {
        layoutParams = LayoutParams(width, height)
    }

    /**
     * Compatibility API used by OverlayForegroundService.
     */
    fun updatePositionCoordinates(
        x: Int,
        y: Int
    ) {
        translationX = x.toFloat()
        translationY = y.toFloat()
    }

    /**
     * Compatibility API used by OverlayForegroundService.
     */
    fun setOnPositionChangeListener(
        listener: (Int, Int) -> Unit
    ) {
        positionChangeListener = listener
    }

    /**
     * Temporary stub.
     * Real WindowManager attachment will be implemented later.
     */
    fun attachToWindow() {
        // no-op
    }

    /**
     * Temporary stub.
     * Real WindowManager removal will be implemented later.
     */
    fun removeFromWindow() {
        // no-op
    }
}