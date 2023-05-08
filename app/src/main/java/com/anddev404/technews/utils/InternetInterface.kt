package com.anddev404.technews.utils

import android.content.Context

interface InternetInterface {
    fun isOnline(context: Context): Boolean
}