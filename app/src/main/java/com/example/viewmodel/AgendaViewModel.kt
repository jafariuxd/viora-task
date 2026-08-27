package com.example.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.CalendarEventItem
import com.example.network.NetworkModule
import com.example.ui.screens.AgendaItemData
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.AppDatabase
import com.example.data.AgendaRepository
import kotlinx.coroutines.flow.firstOrNull

class AgendaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgendaRepository(AppDatabase.getDatabase(application).agendaDao())
    private val _events = MutableStateFlow<List<AgendaItemData>>(emptyList())
    val events: StateFlow<List<AgendaItemData>> = _events.asStateFlow()

    init {
        loadOfflineData()
    }

    private fun loadOfflineData() {
        viewModelScope.launch {
            repository.allItems.collect { offlineItems ->
                if (offlineItems.isNotEmpty()) {
                    if (!hasFetchedFromNetwork) {
                        _events.value = offlineItems
                    }
                    _isAuthorized.value = true
                }
            }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFetchingMore = MutableStateFlow(false)
    val isFetchingMore: StateFlow<Boolean> = _isFetchingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _paginationError = MutableStateFlow<String?>(null)
    val paginationError: StateFlow<String?> = _paginationError.asStateFlow()
    
    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    fun clearPaginationError() { _paginationError.value = null }

    private val _authIntent = MutableStateFlow<Intent?>(null)
    val authIntent: StateFlow<Intent?> = _authIntent.asStateFlow()

    private var currentAccessToken: String? = null
    private var nextPageToken: String? = null
    private var cachedTimeMin: String? = null
    private var cachedTimeMax: String? = null
    val hasMore: Boolean get() = nextPageToken != null

    fun clearAuthIntent() { _authIntent.value = null }
    fun setError(msg: String) { _error.value = msg }

    private var hasFetchedFromNetwork = false

    fun authorizeAndFetch(activity: Activity, silent: Boolean = false, forceRefresh: Boolean = false) {
        if (!forceRefresh && hasFetchedFromNetwork) {
            _isAuthorized.value = true
            return
        }

        if (currentAccessToken != null) {
            _isAuthorized.value = true
            fetchEvents(activity, currentAccessToken!!)
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/calendar.readonly"))
            .build()
        val client = GoogleSignIn.getClient(activity, gso)
        if (silent) {
            client.silentSignIn().addOnCompleteListener(activity) { task ->
                try {
                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                    if (account?.account != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val token = GoogleAuthUtil.getToken(activity, account.account!!, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                                currentAccessToken = token
                                _isAuthorized.value = true
                                fetchEvents(activity, token)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    if (!silent) _authIntent.value = client.signInIntent
                }
            }
        } else {
            _authIntent.value = client.signInIntent
        }
    }

    fun handleAuthorizationResult(context: android.content.Context, intent: Intent?) {
        viewModelScope.launch {
            var tokenToFetch: String? = null
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                
                if (account?.account != null) {
                    _isLoading.value = true
                    val token = withContext(Dispatchers.IO) {
                        GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                    }
                    tokenToFetch = token
                    _isAuthorized.value = true
                } else {
                    _error.value = "Account is null"
                }
            } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                _error.value = "Please grant Calendar permissions in the Google Sign-in flow. You may need to check the box for Calendar access."
            } catch (e: Exception) {
                _error.value = "Auth error: ${e.message ?: e.javaClass.simpleName}"
            }
            
            if (tokenToFetch != null) {
                currentAccessToken = tokenToFetch
                fetchEvents(context, tokenToFetch)
            } else {
                _isLoading.value = false
            }
        }
    }

    fun loadMore(context: android.content.Context) {
        if (!hasMore || _isFetchingMore.value || _isLoading.value) return
        currentAccessToken?.let {
            fetchEvents(context, it, loadMore = true)
        }
    }

    private fun fetchEvents(context: android.content.Context, token: String, loadMore: Boolean = false) {
        viewModelScope.launch {
            if (loadMore) {
                _isFetchingMore.value = true
            } else {
                _isLoading.value = true
                nextPageToken = null
            }
            _error.value = null

            try {
                val timeMin: String
                val timeMax: String

                if (loadMore && cachedTimeMin != null && cachedTimeMax != null) {
                    timeMin = cachedTimeMin!!
                    timeMax = cachedTimeMax!!
                } else {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                    sdf.timeZone = TimeZone.getDefault()
                    
                    val calMin = Calendar.getInstance()
                    calMin.add(Calendar.MONTH, -1)
                    timeMin = sdf.format(calMin.time)

                    val calMax = Calendar.getInstance()
                    calMax.add(Calendar.MONTH, 3)
                    timeMax = sdf.format(calMax.time)
                    
                    cachedTimeMin = timeMin
                    cachedTimeMax = timeMax
                }

                val response = NetworkModule.calendarApi.getEvents(
                    authHeader = "Bearer $token",
                    timeMin = timeMin,
                    timeMax = timeMax,
                    maxResults = 50,
                    pageToken = nextPageToken
                )

                nextPageToken = response.nextPageToken
                val items = response.items ?: emptyList()

                // Filter out cancelled events
                val activeItems = items.filter { it.status != "cancelled" }
                val parsedItems = activeItems.map { parseEvent(it) }

                if (loadMore) {
                    _events.value = _events.value + parsedItems
                    repository.insertAll(parsedItems)
                } else {
                    _events.value = parsedItems
                    repository.clearAndInsert(parsedItems)
                    hasFetchedFromNetwork = true
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("AgendaViewModel", "HTTP Error fetching events", e)
                val msg = if (e.code() == 401) {
                    withContext(Dispatchers.IO) {
                        try {
                            GoogleAuthUtil.clearToken(context, token)
                        } catch (ex: Exception) {}
                    }
                    currentAccessToken = null
                    _isAuthorized.value = false
                    "Session expired. Please close and try again."
                } else if (e.code() == 403) {
                    "API Error 403: Please enable Google Calendar API in Google Cloud Console."
                } else {
                    "HTTP Error: ${e.code()}"
                }
                
                if (loadMore) {
                    _paginationError.value = msg
                } else {
                    _error.value = msg
                }
            } catch (e: Exception) {
                Log.e("AgendaViewModel", "Error fetching events", e)
                if (_events.value.isNotEmpty()) {
                    _isAuthorized.value = true
                }
                if (loadMore) {
                    _paginationError.value = "Failed to fetch more events: ${e.message}"
                } else {
                    _error.value = "Failed to fetch events: ${e.message}"
                }
            } finally {
                if (loadMore) {
                    _isFetchingMore.value = false
                } else {
                    _isLoading.value = false
                }
            }
        }
    }

    fun disconnectCalendar(context: android.content.Context) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener {
            _isAuthorized.value = false
            currentAccessToken = null
            _events.value = emptyList()
            viewModelScope.launch(Dispatchers.IO) {
                repository.clearEvents()
            }
        }
    }

    private fun parseEvent(item: CalendarEventItem): AgendaItemData {
        val dtStart = item.start?.dateTime ?: item.start?.date ?: ""
        val dtEnd = item.end?.dateTime ?: item.end?.date ?: ""
        
        // simple parsing
        val day = try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dtStart.take(10))
            SimpleDateFormat("dd", Locale.US).format(date!!)
        } catch (e: Exception) { "01" }

        val startTime = try {
            if (dtStart.length > 10) {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(dtStart)
                SimpleDateFormat("hh:mm a", Locale.US).format(date!!)
            } else "All Day"
        } catch (e: Exception) { "All Day" }
        
        val endTime = try {
            if (dtEnd.length > 10) {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(dtEnd)
                SimpleDateFormat("hh:mm a", Locale.US).format(date!!)
            } else ""
        } catch (e: Exception) { "" }

        val timeString = if (startTime == "All Day") "All Day" else "$startTime - $endTime"
        
        val isOnline = item.location?.contains("zoom", ignoreCase = true) == true ||
                       item.location?.contains("meet", ignoreCase = true) == true
        
        val type = if (isOnline) "Online meeting" else "In-person meeting"

        
        val isPast = try {
            val dt = dtEnd.ifEmpty { dtStart }
            val date = if (dt.length > 10) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(dt)
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dt)
            }
            if (dt.length <= 10 && date != null) {
                // For all-day events, check against start of today
                val today = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time
                date.before(today)
            } else {
                date?.before(java.util.Date()) == true
            }
        } catch (e: Exception) {
            false
        }

        return AgendaItemData(
            id = item.id ?: java.util.UUID.randomUUID().toString(),
            day = day,
            type = type,
            isOnline = isOnline,
            time = timeString,
            title = item.summary ?: "No Title",
            originalDateTime = dtStart,
            htmlLink = item.htmlLink ?: "",
            isPast = isPast
        )
    }
}
