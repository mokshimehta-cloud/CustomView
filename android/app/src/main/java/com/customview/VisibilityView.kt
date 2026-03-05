package com.customview

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.UIManagerHelper
import com.facebook.react.uimanager.events.Event
import com.facebook.react.views.scroll.ReactScrollView
import com.facebook.react.views.scroll.ReactHorizontalScrollView

class VisibilityChangeEvent(
    surfaceId: Int,
    viewTag: Int,
    private val focused: Boolean
) : Event<VisibilityChangeEvent>(surfaceId, viewTag) {
    override fun getEventName() = "topVisibilityChange"
    override fun getEventData(): WritableMap = Arguments.createMap().also {
        it.putBoolean("focused", focused)
    }
}

class VisibilityView(
    private val reactContext: ThemedReactContext
) : FrameLayout(reactContext) {

    private var isFocused = false
    private var threshold = 0.5f
    private var isScrolling = false
    private var lastScrollY = 0
    private var lastScrollX = 0

    private val handler = Handler(Looper.getMainLooper())
    private val scrollStopDelay = 150L
    private val frameCheckDelay = 16L // ~60fps

    private val scrollStopRunnable = Runnable {
        isScrolling = false
        checkVisibility()
    }

    private val frameCheckRunnable = object : Runnable {
        override fun run() {
            checkScrollState()
            handler.postDelayed(this, frameCheckDelay)
        }
    }

    fun setThreshold(value: Float) {
        threshold = value
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(frameCheckRunnable)
        handler.postDelayed({ checkVisibility() }, 100)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(frameCheckRunnable)
        handler.removeCallbacks(scrollStopRunnable)
    }

    private fun findParentReactScrollView(): ViewGroup? {
        var current = parent
        while (current != null) {
            if (current is ReactScrollView || current is ReactHorizontalScrollView) {
                return current as ViewGroup
            }
            current = current.parent
        }
        return null
    }

    private fun checkScrollState() {
        val scrollView = findParentReactScrollView()

        if (scrollView != null) {
            val currentScrollY = scrollView.scrollY
            val currentScrollX = scrollView.scrollX

            if (currentScrollY != lastScrollY || currentScrollX != lastScrollX) {
                onScrollDetected()
                lastScrollY = currentScrollY
                lastScrollX = currentScrollX
            }
        }

        if (!isScrolling) {
            checkVisibility()
        }
    }

    private fun onScrollDetected() {
        if (!isScrolling) {
            isScrolling = true
            updateFocus(false)
        }

        handler.removeCallbacks(scrollStopRunnable)
        handler.postDelayed(scrollStopRunnable, scrollStopDelay)
    }

    private fun checkVisibility() {
        if (isScrolling) {
            return
        }

        if (!isAttachedToWindow || height == 0) {
            updateFocus(false)
            return
        }

        val viewRect = Rect()
        val isVisible = getGlobalVisibleRect(viewRect)

        if (!isVisible) {
            updateFocus(false)
            return
        }

        val parentScrollView = findParentReactScrollView()
        if (parentScrollView != null) {
            val scrollRect = Rect()
            parentScrollView.getGlobalVisibleRect(scrollRect)

            if (!viewRect.intersect(scrollRect)) {
                updateFocus(false)
                return
            }
        }

        val visibleHeight = viewRect.height().toFloat()
        val totalHeight = height.toFloat()
        val ratio = visibleHeight / totalHeight

        updateFocus(ratio >= threshold)
    }

    private fun updateFocus(visible: Boolean) {
        if (visible == isFocused) return

        isFocused = visible

        try {
            val dispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)
            val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
            dispatcher?.dispatchEvent(VisibilityChangeEvent(surfaceId, id, visible))
        } catch (_: Exception) {
        }
    }
}
