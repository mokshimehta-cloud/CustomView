package com.customview

import android.graphics.Rect
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter

class VisibilityView(
    private val reactContext: ThemedReactContext
) : FrameLayout(reactContext) {

    private var isFocused = false
    private var threshold = 0.5f

    fun setThreshold(value: Float) {
        threshold = value
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        viewTreeObserver.addOnScrollChangedListener {
            checkVisibility()
        }

        post { checkVisibility() } // initial check
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnScrollChangedListener {
            checkVisibility()
        }
    }

    private fun checkVisibility() {
        val rect = Rect()
        val isVisibleNow = getGlobalVisibleRect(rect)

        if (!isVisibleNow) {
            updateFocus(false)
            return
        }

        val visibleHeight = rect.height().toFloat()
        val totalHeight = height.toFloat()

        if (totalHeight == 0f) return

        val ratio = visibleHeight / totalHeight
        updateFocus(ratio >= threshold)
    }

    private fun updateFocus(visible: Boolean) {
        if (visible == isFocused) return
        isFocused = visible

        val event = Arguments.createMap()
        event.putBoolean("focused", visible)

        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, "topVisibilityChange", event) // ✅ MUST BE topVisibilityChange
    }
}