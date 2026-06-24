

package com.bobon.mypace.presentation

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.bobon.mypace.presentation.ui.WearUiMain

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.BODY_SENSORS] == true) {
                // Можно запускать сервис
                Log.d("SYNC_DEBUG", "Разрешение на пульс получено")
            }
        }

// Вызывай это перед стартом тренировки
        requestPermissionLauncher.launch(arrayOf(Manifest.permission.BODY_SENSORS))

        setContent {
            WearUiMain()
        }


    }


}



