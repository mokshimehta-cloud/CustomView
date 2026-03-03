package com.customview

import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ThemedReactContext

class VisibilityViewManager : ViewGroupManager<VisibilityView>() {

    override fun getName(): String {
        return "VisibilityView"
    }

    override fun createViewInstance(
        reactContext: ThemedReactContext
    ): VisibilityView {
        return VisibilityView(reactContext)
    }

    // ✅ REGISTER EVENT FOR FABRIC
    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> {
        return mutableMapOf(
            "topVisibilityChange" to mutableMapOf(
                "registrationName" to "onVisibilityChange"
            )
        )
    }
}