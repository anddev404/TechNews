package com.anddev404.technews.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity

class AndroidBars {
    companion object {

        fun changeColors(activity: AppCompatActivity, color: Int) {

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val window = activity.window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = color
                    window.navigationBarColor = color
                }
            } catch (e: Exception) {
            }

            activity.supportActionBar?.setBackgroundDrawable(
                ColorDrawable(
                    adjustAlpha(
                        color,
                        0.90f
                    )
                )
            )

        }

        @ColorInt
        private fun adjustAlpha(@ColorInt color: Int, factor: Float): Int {
            val alpha = Math.round(Color.alpha(color) * factor)
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }
    }


}