package com.willard.campusgps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.willard.campusgps.ui.PantallaMapa
import com.willard.campusgps.ui.theme.CampusGPSTheme
import com.willard.campusgps.viewmodel.CampusViewModelFactory
import com.willard.campusgps.viewmodel.MapaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CampusGPSTheme {
                val vm: MapaViewModel = viewModel(
                    factory = CampusViewModelFactory(applicationContext)
                )
                PantallaMapa(vm)
            }
        }
    }
}
