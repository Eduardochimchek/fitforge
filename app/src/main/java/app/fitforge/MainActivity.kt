package app.fitforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.fitforge.data.images.ExerciseImageStore
import app.fitforge.data.prefs.AppSettings
import app.fitforge.data.prefs.UserPrefsRepository
import app.fitforge.data.seed.ExerciseSeeder
import app.fitforge.ui.components.LocalImageStore
import app.fitforge.ui.navigation.AppShell
import app.fitforge.ui.theme.FitForgeTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    prefs: UserPrefsRepository,
    seeder: ExerciseSeeder,
) : ViewModel() {
    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    val settings: StateFlow<AppSettings?> = prefs.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            runCatching { seeder.seedIfNeeded() }
            _ready.value = true
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    @Inject lateinit var imageStore: ExerciseImageStore

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !vm.ready.value || vm.settings.value == null }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            val s = settings ?: AppSettings()
            FitForgeTheme(mode = s.themeMode) {
                CompositionLocalProvider(LocalImageStore provides imageStore) {
                    AppShell()
                }
            }
        }
    }
}
