package ru.networkignav.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.networkignav.model.AuthModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<AuthModel?>(null)
    val state = _state.asStateFlow()

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getInt(ID_KEY, 0)

        if (!prefs.contains(TOKEN_KEY) || token == null) {
            prefs.edit { clear() }
        } else {
            _state.value = AuthModel(id, token)
        }
    }

    @Synchronized
    fun setAuth(id: Int, token: String) {
        prefs.edit {
            putString(TOKEN_KEY, token)
            putInt(ID_KEY, id)
        }

        _state.value = AuthModel(id, token)

    }

    @Synchronized
    fun clearAuth() {
        prefs.edit { clear() }
        _state.value = null
    }

    fun isUserValid() = state.value != null

    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

    }
}