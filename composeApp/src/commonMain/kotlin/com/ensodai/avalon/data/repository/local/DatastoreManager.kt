package com.ensodai.avalon.data.repository.local


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ensodai.avalon.di.ApplicationScope
import com.ensodai.avalon.utils.IODispatcher
import com.ensodai.avalon.utils.L
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile
import kotlin.time.Instant

class DatastoreManager(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope
) {

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    @Volatile
    private var cachedUserId: String? = null

    @Volatile
    private var cachedServerUrl: String? = null

    @Volatile
    private var cachedLastSyncedTimestamp: Instant? = null

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        scope.launch(IODispatcher) {
            dataStore.data.collect { preferences ->
                cachedAccessToken = preferences[PreferencesKeys.ACCESS_TOKEN_KEY]
                cachedRefreshToken = preferences[PreferencesKeys.REFRESH_TOKEN_KEY]
                cachedUserId = preferences[PreferencesKeys.USER_ID_KEY]
                val savedUrl = preferences[PreferencesKeys.SERVER_URL_KEY]
                cachedServerUrl = savedUrl
                if (!savedUrl.isNullOrBlank()) {
                    com.ensodai.avalon.di.updateServerBaseUrl(savedUrl)
                }
                cachedLastSyncedTimestamp = preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP]?.let {
                    Instant.fromEpochMilliseconds(it)
                }
                _isInitialized.value = true
                L.d(
                    "DatastoreManager",
                    "Cache updated: AccessToken present=${cachedAccessToken != null}, ServerUrl=$savedUrl"
                )
            }
        }
    }

    private object PreferencesKeys {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val SERVER_URL_KEY = stringPreferencesKey("custom_server_url")
        val LAST_VERSION_CODE = intPreferencesKey("last_version_code")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }

    fun getServerUrl(): String? = cachedServerUrl

    suspend fun saveServerUrl(url: String) {
        val cleanUrl = url.trim().trimEnd('/')
        cachedServerUrl = cleanUrl
        com.ensodai.avalon.di.updateServerBaseUrl(cleanUrl)
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL_KEY] = cleanUrl
        }
    }

    suspend fun getLastVersionCode(): Int? {
        return dataStore.data.firstOrNull()?.get(PreferencesKeys.LAST_VERSION_CODE)
    }

    suspend fun saveVersionCode(versionCode: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_VERSION_CODE] = versionCode
        }
    }

    val isUserAuthorizedFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            val accessToken = preferences[PreferencesKeys.ACCESS_TOKEN_KEY]
            !accessToken.isNullOrEmpty()
        }


    fun getAccessToken(): String? = cachedAccessToken

    fun getRefreshToken(): String? = cachedRefreshToken

    fun getUserId(): String? = cachedUserId
    
    fun getUserIdFlow(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID_KEY]
    }

    suspend fun saveAuthData(accessToken: String, refreshToken: String, userId: String) {
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
        cachedUserId = userId
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN_KEY] = accessToken
            preferences[PreferencesKeys.REFRESH_TOKEN_KEY] = refreshToken
            preferences[PreferencesKeys.USER_ID_KEY] = userId
        }
    }

    suspend fun clearUserSession() {
        cachedAccessToken = null
        cachedRefreshToken = null
        cachedUserId = null
        cachedLastSyncedTimestamp = null
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCESS_TOKEN_KEY)
            preferences.remove(PreferencesKeys.REFRESH_TOKEN_KEY)
            preferences.remove(PreferencesKeys.USER_ID_KEY)
            preferences.remove(PreferencesKeys.LAST_SYNC_TIMESTAMP)
        }
    }

    suspend fun saveLastSyncTimestamp(timestamp: Instant) {
        cachedLastSyncedTimestamp = timestamp
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP] = timestamp.toEpochMilliseconds()
        }
    }

    fun getLastSyncTimestampFlow(): Flow<Instant?> {
        return dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIMESTAMP]?.let {
                Instant.fromEpochMilliseconds(it)
            }
        }
    }
}
