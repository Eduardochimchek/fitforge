package app.fitforge.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prefs")

enum class ThemeMode(val label: String) { ESCURO("Escuro"), CLARO("Claro"), SISTEMA("Seguir o sistema") }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.ESCURO,
    val homeEquipment: Set<String> = setOf("nenhum"),
)

@Singleton
class UserPrefsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private object Keys {
        val SEED_VERSION = intPreferencesKey("seed_version")
        val THEME = stringPreferencesKey("theme")
        val HOME_EQUIPMENT = stringSetPreferencesKey("home_equipment")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.ESCURO,
            homeEquipment = p[Keys.HOME_EQUIPMENT] ?: setOf("nenhum"),
        )
    }

    suspend fun seedVersion(): Int = context.dataStore.data.first()[Keys.SEED_VERSION] ?: 0

    suspend fun setSeedVersion(v: Int) {
        context.dataStore.edit { it[Keys.SEED_VERSION] = v }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setHomeEquipment(codes: Set<String>) {
        context.dataStore.edit { it[Keys.HOME_EQUIPMENT] = codes + "nenhum" }
    }
}
