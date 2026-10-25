package com.example.appgrupo3.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

object Compartir {
    fun texto(context: Context, asunto: String, cuerpo: String): Boolean {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, asunto)
            putExtra(Intent.EXTRA_TEXT, cuerpo)
        }
        return try {
            context.startActivity(Intent.createChooser(intent, asunto))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
