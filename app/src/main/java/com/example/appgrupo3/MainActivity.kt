package com.example.appgrupo3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.appgrupo3.navigation.AppNavegacion
import com.example.appgrupo3.ui.theme.AppGrupo3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppGrupo3Theme {
                AppNavegacion()
            }
        }
    }
}
