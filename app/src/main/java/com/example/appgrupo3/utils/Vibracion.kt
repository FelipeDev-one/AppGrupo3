package com.example.appgrupo3.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


object Vibracion {
    fun avisar(context: Context) {
        val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        if (vibrador?.hasVibrator() == true) {
            vibrador.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 180, 120, 180), -1))
        }
    }
}
