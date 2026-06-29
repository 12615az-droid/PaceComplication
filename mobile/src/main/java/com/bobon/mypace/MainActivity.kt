package com.bobon.mypace


import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.bobon.mypace.ui.main.PaceScreenRoute
import com.bobon.mypace.ui.theme.RunningAppTheme


class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RunningAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    PaceScreenRoute()

                }
            }
        }


    }


}