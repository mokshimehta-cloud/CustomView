package com.customview

import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.facebook.react.bridge.Arguments
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.facebook.react.views.scroll.ReactScrollView
import com.facebook.react.views.scroll.ReactHorizontalScrollView

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
        console("🛑 Scroll STOPPED - checking visibility")
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
        console("✅ ATTACHED to window")
        handler.post(frameCheckRunnable)
        handler.postDelayed({ checkVisibility() }, 100)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        console("❌ DETACHED from window")
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
            
            // Detect if scroll position changed
            if (currentScrollY != lastScrollY || currentScrollX != lastScrollX) {
                onScrollDetected()
                lastScrollY = currentScrollY
                lastScrollX = currentScrollX
            }
        }
        
        // Only check visibility when not scrolling
        if (!isScrolling) {
            checkVisibility()
        }
    }

    private fun onScrollDetected() {
        if (!isScrolling) {
            console("🔄 Scroll STARTED - pausing videos")
            isScrolling = true
            updateFocus(false) // Immediately blur when scrolling starts
        }
        
        // Reset scroll stop timer
        handler.removeCallbacks(scrollStopRunnable)
        handler.postDelayed(scrollStopRunnable, scrollStopDelay)
    }

    private fun checkVisibility() {
        // Don't update focus while scrolling
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
        console("🎯 FOCUS: ${if (visible) "PLAYING ✅" else "PAUSED ❌"}")

        val event = Arguments.createMap()
        event.putBoolean("focused", visible)

        reactContext
            .getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(id, "topVisibilityChange", event)
    }
    
    private fun console(message: String) {
        // Log to JS console via event
        val event = Arguments.createMap()
        event.putString("message", message)
        
        try {
            reactContext
                .getJSModule(RCTEventEmitter::class.java)
                .receiveEvent(id, "topLog", event)
        } catch (e: Exception) {
            // Fallback to Android log
            android.util.Log.d("VisibilityView", message)
        }
    }
}