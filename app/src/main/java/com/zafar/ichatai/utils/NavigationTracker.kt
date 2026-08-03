package com.zafar.ichatai.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

object NavigationTracker {
    private val trail = mutableListOf<String>()

    fun track(route: String) {
        if (trail.lastOrNull() != route) {
            trail.add(route)
            if (trail.size > 10) {
                trail.removeAt(0)
            }
            // Update Crashlytics trail
            FirebaseCrashlytics.getInstance().setCustomKey("navigation_trail", getTrail())
        }
    }

    fun getTrail(): String {
        return trail.joinToString(" -> ")
    }
}
