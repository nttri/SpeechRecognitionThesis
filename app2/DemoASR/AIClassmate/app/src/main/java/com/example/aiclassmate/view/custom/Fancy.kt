package com.example.aiclassmate.view.custom

import android.content.res.Resources
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView

object Fancy {
    internal var handler = Handler()

    fun animateBtn(view: View) {
        view.scaleX = 0.00001f
        view.scaleY = 0.00001f
        view.animate().scaleX(1f).setDuration(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction { view.animate().scaleY(1f).withEndAction(null) }
    }

    fun animateBtnDisapear(view: View) {
        view.animate().scaleX(0.00001f).scaleY(0.7f).translationY(Resources.getSystem().displayMetrics.density * 16)
            .setDuration(150).setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.visibility = View.GONE
                view.translationY = 0f
            }
    }

    fun animateProgress(view: View) {
        view.scaleX = 0.00001f
        view.animate().scaleX(1f).setDuration(150).interpolator = AccelerateDecelerateInterpolator()
    }

    private fun internalFlashText(view: TextView, duration: Long) {
        view.alpha = 0f
        view.animate().alpha(1f).duration = duration
    }

    // Only one man can call flash text at a time, because i want to fast prototyping so not seriously in this case
    private val selfFlashHandler = Handler()
    private val uiHandler = Handler()
    private lateinit var flashRunnable : Runnable
    private var stopFlashing = false
    private var isFlashing = false

    @Synchronized
    fun flashText(view: TextView) {
        if (isFlashing) return
        stopFlashing = false

        isFlashing = true
        flashRunnable = Runnable {
            if (stopFlashing) {
                isFlashing = false
                return@Runnable
            }
            internalFlashText(view, 500L)
            selfFlashHandler.postDelayed(flashRunnable, 1000)
        }

        uiHandler.postDelayed({
            selfFlashHandler.postDelayed(flashRunnable, 30)
        }, 50)
    }

    fun stopFlashing() {
        stopFlashing = true
    }
}

