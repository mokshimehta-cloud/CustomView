package com.customview

import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.common.MapBuilder

class VisibilityViewManager : ViewGroupManager<VisibilityView>() {

    override fun getName() = "VisibilityView"

    override fun createViewInstance(reactContext: ThemedReactContext): VisibilityView {
        return VisibilityView(reactContext)
    }

    @ReactProp(name = "threshold", defaultFloat = 0.5f)
    fun setThreshold(view: VisibilityView, threshold: Float) {
        view.setThreshold(threshold)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        return MapBuilder.builder<String, Any>()
            .put("topVisibilityChange", MapBuilder.of("registrationName", "onVisibilityChange"))
            .build()
    }
}