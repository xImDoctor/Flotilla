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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.imdoctor.flotilla.di.AppContainer
import com.imdoctor.flotilla.presentation.StartupViewModel
import com.imdoctor.flotilla.presentation.ViewModelFactory
import com.imdoctor.flotilla.presentation.navigation.FlotillaNavGraph
import com.imdoctor.flotilla.presentation.navigation.Screen
import com.imdoctor.flotilla.presentation.theme.FlotillaColors
import com.imdoctor.flotilla.presentation.theme.FlotillaTheme
import com.imdoctor.flotilla.utils.LocaleManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var currentLanguage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Слушаем изменения языка для пересоздания Activity
        observeLanguageChanges()

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

    /**
     * Наблюдаем за изменением языка и пересоздаем Activity
     */
    private fun observeLanguageChanges() {
        lifecycleScope.launch {
            AppContainer.settingsRepository.languageFlow
                .distinctUntilChanged()
                .collect { newLanguage ->
                    if (currentLanguage == null) {
                        // Первая загрузка - просто сохраняем текущий язык
                        currentLanguage = newLanguage
                        android.util.Log.d("MainActivity", "Initial language set: $newLanguage")
                    } else if (currentLanguage != newLanguage) {
                        // Язык изменился - применяем и пересоздаем Activity
                        android.util.Log.d("MainActivity", "Language changed from $currentLanguage to $newLanguage")

                        // Применяем новую локаль
                        LocaleManager.applyLocale(this@MainActivity, newLanguage)

                        // Обновляем текущий язык
                        currentLanguage = newLanguage

                        // Пересоздаем Activity для применения изменений
                        recreate()
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        // Приостанавливаем фоновую музыку когда приложение сворачивается
        AppContainer.audioManager.onAppPause()
    }

    override fun onResume() {
        super.onResume()
        // Возобновляем фоновую музыку когда приложение возвращается
        AppContainer.audioManager.onAppResume()
    }
}