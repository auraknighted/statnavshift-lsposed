package com.statnav.burnin

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.WeakHashMap

class BurnInHooks : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != SYSTEM_UI_PACKAGE) return

        hookViewAttach(
            lpparam,
            "com.android.systemui.statusbar.phone.PhoneStatusBarView",
            maxOffsetPx = STATUS_BAR_MAX_SHIFT_PX
        )

        hookViewAttach(
            lpparam,
            "com.android.systemui.navigationbar.NavigationBarView",
            maxOffsetPx = NAV_BAR_MAX_SHIFT_PX
        )

        hookViewAttach(
            lpparam,
            "com.android.systemui.navigationbar.NavigationBarFrame",
            maxOffsetPx = NAV_BAR_MAX_SHIFT_PX
        )

        XposedBridge.log("$TAG Loaded in ${lpparam.packageName}")
    }

    private fun hookViewAttach(
        lpparam: XC_LoadPackage.LoadPackageParam,
        className: String,
        maxOffsetPx: Int
    ) {
        runCatching {
            XposedHelpers.findAndHookMethod(
                className,
                lpparam.classLoader,
                "onAttachedToWindow",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val view = param.thisObject as? View ?: return
                        startPeriodicShift(view, maxOffsetPx)
                    }
                }
            )
        }.onFailure {
            XposedBridge.log("$TAG Unable to hook $className: ${it.message}")
        }
    }

    private fun startPeriodicShift(targetView: View, maxOffsetPx: Int) {
        if (scheduledViews.containsKey(targetView)) return

        val base = BaseState(
            left = targetView.paddingLeft,
            top = targetView.paddingTop,
            right = targetView.paddingRight,
            bottom = targetView.paddingBottom
        )

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (!targetView.isAttachedToWindow) return

                val shiftX = offsetForMinute(maxOffsetPx)
                val shiftY = offsetForMinute(maxOffsetPx)

                targetView.setPadding(
                    base.left + shiftX,
                    base.top + shiftY,
                    base.right - shiftX,
                    base.bottom - shiftY
                )

                targetView.translationX = shiftX * TRANSLATION_FACTOR
                targetView.translationY = shiftY * TRANSLATION_FACTOR

                handler.postDelayed(this, SHIFT_INTERVAL_MS)
            }
        }

        val listener = object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) = Unit

            override fun onViewDetachedFromWindow(v: View) {
                handler.removeCallbacks(runnable)
                targetView.setPadding(base.left, base.top, base.right, base.bottom)
                targetView.translationX = 0f
                targetView.translationY = 0f
                targetView.removeOnAttachStateChangeListener(this)
                scheduledViews.remove(targetView)
            }
        }

        targetView.addOnAttachStateChangeListener(listener)
        scheduledViews[targetView] = ScheduledState(handler, runnable)

        handler.post(runnable)
    }

    private fun offsetForMinute(maxOffsetPx: Int): Int {
        val minuteBucket = ((SystemClock.elapsedRealtime() / SHIFT_INTERVAL_MS) % OFFSET_PATTERN.size).toInt()
        val normalized = OFFSET_PATTERN[minuteBucket]
        return (normalized * maxOffsetPx).toInt().coerceIn(-maxOffsetPx, maxOffsetPx)
    }

    private data class BaseState(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int
    )

    private data class ScheduledState(
        val handler: Handler,
        val runnable: Runnable
    )

    private companion object {
        const val TAG = "[StatNavBurnIn]"
        const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        const val STATUS_BAR_MAX_SHIFT_PX = 2
        const val NAV_BAR_MAX_SHIFT_PX = 3

        const val SHIFT_INTERVAL_MS = 60_000L
        const val TRANSLATION_FACTOR = 0.25f

        val OFFSET_PATTERN = intArrayOf(0, 1, 0, -1, 0, 1, 0, -1)
        val scheduledViews = WeakHashMap<View, ScheduledState>()
    }
}
