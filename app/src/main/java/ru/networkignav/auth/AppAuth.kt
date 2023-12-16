package ru.networkignav.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.networkignav.api.PostApiService
import ru.networkignav.model.AuthModel
import ru.networkignav.model.PushToken
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
    var pushToken: PushToken? = null


    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getLong(ID_KEY, 0)

        if (!prefs.contains(TOKEN_KEY) || token == null) {
            prefs.edit { clear() }
        } else {
            _state.value = AuthModel(id, token)
        }
        //sendPushToken()
    }

    @Synchronized
    fun setAuth(id: Long, token: String) {
        prefs.edit {
            putString(TOKEN_KEY, token)
            putLong(ID_KEY, id)
        }

        _state.value = AuthModel(id, token)
        //sendPushToken()
    }


    @Synchronized
    fun removeAuth() {
        _state.value = AuthModel()
        with(prefs.edit()) {
            clear()
            commit()
        }
        //sendPushToken()
    }

    @Synchronized
    fun clearAuth() {
        prefs.edit { clear() }
        _state.value = null
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getApiService(): PostApiService
    }




    fun isUserValid() = state.value != null

    companion object {

        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"


    }
}