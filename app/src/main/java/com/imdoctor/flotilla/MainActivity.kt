package com.imdoctor.flotilla

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.imdoctor.flotilla.presentation.StartupViewModel
import com.imdoctor.flotilla.presentation.ViewModelFactory
import com.imdoctor.flotilla.presentation.navigation.FlotillaNavGraph
import com.imdoctor.flotilla.presentation.navigation.Screen
import com.imdoctor.flotilla.presentation.theme.FlotillaColors
import com.imdoctor.flotilla.presentation.theme.FlotillaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FlotillaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val startupViewModel: StartupViewModel = viewModel(factory = ViewModelFactory())
                    val startDestination by startupViewModel.startDestination.collectAsStateWithLifecycle()

                    if (startDestination != null) {
                        val navController = rememberNavController()
                        FlotillaNavGraph(
                            navController = navController,
                            startDestination = startDestination ?: Screen.UserRegistration.route
                        )
                    } else {
                        // Показываем индикатор загрузки пока определяем стартовый экран
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = FlotillaColors.Primary
                            )
                        }
                    }
                }
            }
        }
    }
}