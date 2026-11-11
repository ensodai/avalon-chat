package com.ensodai.avalon.domain.paymentManager

import android.app.Activity
import java.lang.ref.WeakReference

class CurrentActivityProvider {
    private var _currentActivity: WeakReference<Activity>? = null

    fun setCurrentActivity(activity: Activity) {
        _currentActivity = WeakReference(activity)
    }

    fun getCurrentActivity(): Activity? {
        return _currentActivity?.get()
    }
}
