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

    private val _showOAuthConfigModal = MutableStateFlow(false)
    val showOAuthConfigModal: StateFlow<Boolean> = _showOAuthConfigModal.asStateFlow()

    private val _needsCalendarPermission = MutableStateFlow(false)
    val needsCalendarPermission: StateFlow<Boolean> = _needsCalendarPermission.asStateFlow()

    fun dismissOAuthConfigModal() {
        _showOAuthConfigModal.value = false
    }

    fun openOAuthConfigModal() {
        _showOAuthConfigModal.value = true
    }

    fun clearCalendarPermissionPrompt() {
        _needsCalendarPermission.value = false
    }

    fun clearPaginationError() { _paginationError.value = null }

    private val _authIntent = MutableStateFlow<Intent?>(null)
    val authIntent: StateFlow<Intent?> = _authIntent.asStateFlow()

    var hasAutoPromptedCalendar: Boolean = false
        private set

    fun markCalendarPrompted() {
        hasAutoPromptedCalendar = true
    }

    fun resetCalendarPromptState() {
        hasAutoPromptedCalendar = false
    }

    private var currentAccessToken: String? = null
    private var pendingAccountName: String? = null
    private var nextPageToken: String? = null
    private var cachedTimeMin: String? = null
    private var cachedTimeMax: String? = null
    val hasMore: Boolean get() = nextPageToken != null

    private var isAuthInProgress = false

    fun clearAuthIntent() { 
        _authIntent.value = null 
    }

    fun onAuthCancelled() {
        isAuthInProgress = false
        _isLoading.value = false
        _error.value = null
    }

    fun setError(msg: String) { _error.value = msg }

    private var hasFetchedFromNetwork = false

    fun authorizeAndFetch(activity: Activity, silent: Boolean = true, forceRefresh: Boolean = false) {
        if (!forceRefresh && (_isAuthorized.value || hasFetchedFromNetwork || _events.value.isNotEmpty())) {
            _isAuthorized.value = true
            return
        }

        val token = currentAccessToken
        if (token != null) {
            _isAuthorized.value = true
            fetchEvents(activity, token)
            return
        }

        if (isAuthInProgress) {
            return
        }

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/calendar.readonly"))
                .build()
            val client = GoogleSignIn.getClient(activity, gso)
            if (silent) {
                client.silentSignIn().addOnCompleteListener(activity) { task ->
                    try {
                        val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                        val accountName = account?.account?.name ?: account?.email
                        if (!accountName.isNullOrBlank()) {
                            viewModelScope.launch(Dispatchers.IO) {
                                try {
                                    val token = if (account.account != null) {
                                        GoogleAuthUtil.getToken(activity, account.account!!, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                                    } else {
                                        GoogleAuthUtil.getToken(activity, accountName, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                                    }
                                    currentAccessToken = token
                                    _isAuthorized.value = true
                                    fetchEvents(activity, token)
                                } catch (e: Exception) {
                                    Log.d("AgendaViewModel", "Silent token acquisition skipped", e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Silent sign-in failed; do NOT open interactive prompt automatically
                    }
                }
            } else {
                isAuthInProgress = true
                _authIntent.value = client.signInIntent
            }
        } catch (e: Exception) {
            isAuthInProgress = false
            _error.value = "Error starting Google Sign-In: ${e.localizedMessage ?: e.message}"
        }
    }

    fun handleAuthorizationResult(context: android.content.Context, intent: Intent?, resultCode: Int = android.app.Activity.RESULT_OK) {
        if (resultCode == android.app.Activity.RESULT_CANCELED) {
            if (intent != null) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                    if (account == null) {
                        onAuthCancelled()
                        return
                    }
                } catch (e: com.google.android.gms.common.api.ApiException) {
                    if (e.statusCode == 12501 || e.statusCode == 12502 || e.statusCode == com.google.android.gms.common.api.CommonStatusCodes.CANCELED) {
                        Log.d("AgendaViewModel", "Google sign-in cancelled or dismissed by user (statusCode=${e.statusCode})")
                        onAuthCancelled()
                        return
                    }
                } catch (e: Exception) {
                    Log.d("AgendaViewModel", "Google sign-in dismissed: ${e.message}")
                    onAuthCancelled()
                    return
                }
            } else {
                onAuthCancelled()
                return
            }
        }

        if (resultCode != android.app.Activity.RESULT_OK && intent == null) {
            onAuthCancelled()
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            var tokenToFetch: String? = null

            val pending = pendingAccountName
            if (resultCode == android.app.Activity.RESULT_OK && pending != null && (intent == null || intent.extras?.containsKey("googleSignInAccount") != true)) {
                try {
                    val token = withContext(Dispatchers.IO) {
                        GoogleAuthUtil.getToken(context, pending, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                    }
                    tokenToFetch = token
                    _isAuthorized.value = true
                    pendingAccountName = null
                } catch (e: Exception) {
                    Log.w("AgendaViewModel", "Error fetching token after consent: ${e.message}", e)
                    _error.value = "Error fetching token after consent: ${e.localizedMessage ?: e.message}"
                }
            } else if (intent != null) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
                    val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                    val accountName = account?.account?.name ?: account?.email

                    if (!accountName.isNullOrBlank()) {
                        pendingAccountName = accountName
                        val token = withContext(Dispatchers.IO) {
                            if (account.account != null) {
                                GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                            } else {
                                GoogleAuthUtil.getToken(context, accountName, "oauth2:https://www.googleapis.com/auth/calendar.readonly")
                            }
                        }
                        tokenToFetch = token
                        _isAuthorized.value = true
                        pendingAccountName = null
                    } else {
                        _error.value = "Selected account has no valid account name or email."
                    }
                } catch (e: com.google.android.gms.auth.UserRecoverableAuthException) {
                    Log.w("AgendaViewModel", "UserRecoverableAuthException: launching consent intent", e)
                    _authIntent.value = e.intent
                    // Wait for consent activity result without stopping loading
                    return@launch
                } catch (e: com.google.android.gms.common.api.ApiException) {
                    when (e.statusCode) {
                        12501, 12502, com.google.android.gms.common.api.CommonStatusCodes.CANCELED -> {
                            // User cancelled account picker; dismiss quietly without logging error
                            Log.d("AgendaViewModel", "Google sign-in cancelled or dismissed: statusCode=${e.statusCode}")
                            onAuthCancelled()
                        }
                        com.google.android.gms.common.api.CommonStatusCodes.DEVELOPER_ERROR -> {
                            Log.w("AgendaViewModel", "Google sign-in Developer Error: statusCode=${e.statusCode}")
                            _error.value = "Developer Error (Code 10): SHA-1 fingerprint (21:90:96:94:A1:EE:91:C2:B7:C0:E5:56:9F:2B:DB:1A:42:11:87:19) or package name (com.viora.task) mismatch in Google Cloud."
                        }
                        12500 -> {
                            Log.w("AgendaViewModel", "Google sign-in failed (Code 12500)")
                            _error.value = "Google Sign-In failed (Code 12500). Please check Google Play Services & OAuth configuration."
                        }
                        com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> {
                            Log.w("AgendaViewModel", "Google sign-in network error (statusCode=${e.statusCode})")
                            _error.value = "Network error connecting to Google. Please check your internet connection."
                        }
                        else -> {
                            Log.w("AgendaViewModel", "Google sign-in ApiException: statusCode=${e.statusCode} ${e.message}")
                            _error.value = "Google Sign-In error (Code ${e.statusCode}): ${e.message ?: "Please try again."}"
                        }
                    }
                } catch (e: Exception) {
                    Log.w("AgendaViewModel", "Auth error during Google sign in: ${e.message}", e)
                    _error.value = "Error during Google authorization: ${e.localizedMessage ?: e.message}"
                }
            } else {
                onAuthCancelled()
            }

            isAuthInProgress = false
            if (tokenToFetch != null) {
                currentAccessToken = tokenToFetch
                fetchEvents(context, tokenToFetch)
            } else {
                _isLoading.value = false
            }
        }
    }

    fun fetchDeviceCalendarEvents(context: android.content.Context) {
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CALENDAR
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            _needsCalendarPermission.value = true
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val calMin = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
                val calMax = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }.timeInMillis

                val uri = android.provider.CalendarContract.Events.CONTENT_URI
                val projection = arrayOf(
                    android.provider.CalendarContract.Events._ID,
                    android.provider.CalendarContract.Events.TITLE,
                    android.provider.CalendarContract.Events.DTSTART,
                    android.provider.CalendarContract.Events.DTEND,
                    android.provider.CalendarContract.Events.ALL_DAY,
                    android.provider.CalendarContract.Events.EVENT_LOCATION,
                    android.provider.CalendarContract.Events.STATUS
                )
                val selection = "(${android.provider.CalendarContract.Events.DTSTART} >= ? AND ${android.provider.CalendarContract.Events.DTSTART} <= ?) AND (${android.provider.CalendarContract.Events.STATUS} IS NULL OR ${android.provider.CalendarContract.Events.STATUS} != ?)"
                val selectionArgs = arrayOf(calMin.toString(), calMax.toString(), android.provider.CalendarContract.Events.STATUS_CANCELED.toString())
                val sortOrder = "${android.provider.CalendarContract.Events.DTSTART} ASC"

                val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
                val parsedList = mutableListOf<AgendaItemData>()
                cursor?.use { c ->
                    val idCol = c.getColumnIndex(android.provider.CalendarContract.Events._ID)
                    val titleCol = c.getColumnIndex(android.provider.CalendarContract.Events.TITLE)
                    val dtStartCol = c.getColumnIndex(android.provider.CalendarContract.Events.DTSTART)
                    val dtEndCol = c.getColumnIndex(android.provider.CalendarContract.Events.DTEND)
                    val allDayCol = c.getColumnIndex(android.provider.CalendarContract.Events.ALL_DAY)
                    val locationCol = c.getColumnIndex(android.provider.CalendarContract.Events.EVENT_LOCATION)

                    val dayFormat = SimpleDateFormat("dd", Locale.US)
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                    val now = System.currentTimeMillis()

                    while (c.moveToNext()) {
                        val id = if (idCol >= 0) c.getLong(idCol).toString() else java.util.UUID.randomUUID().toString()
                        val title = if (titleCol >= 0) c.getString(titleCol) ?: "Untitled Event" else "Untitled Event"
                        val dtStart = if (dtStartCol >= 0) c.getLong(dtStartCol) else 0L
                        val dtEnd = if (dtEndCol >= 0) c.getLong(dtEndCol) else dtStart
                        val allDay = if (allDayCol >= 0) c.getInt(allDayCol) == 1 else false
                        val location = if (locationCol >= 0) c.getString(locationCol) ?: "" else ""

                        val startDate = java.util.Date(dtStart)
                        val day = dayFormat.format(startDate)
                        val startTime = if (allDay) "All Day" else timeFormat.format(startDate)
                        val endTime = if (allDay || dtEnd <= dtStart) "" else timeFormat.format(java.util.Date(dtEnd))
                        val timeString = if (startTime == "All Day" || endTime.isEmpty()) startTime else "$startTime - $endTime"

                        val isOnline = location.contains("zoom", ignoreCase = true) || location.contains("meet", ignoreCase = true) || location.contains("teams", ignoreCase = true) || location.contains("http", ignoreCase = true)
                        val type = if (isOnline) "Online meeting" else "Calendar event"
                        val isPast = if (dtEnd > 0) dtEnd < now else dtStart < now

                        parsedList.add(
                            AgendaItemData(
                                id = id,
                                day = day,
                                type = type,
                                isOnline = isOnline,
                                time = timeString,
                                title = title,
                                originalDateTime = isoFormat.format(startDate),
                                htmlLink = "",
                                isPast = isPast
                            )
                        )
                    }
                }

                withContext(Dispatchers.Main) {
                    if (parsedList.isNotEmpty()) {
                        _events.value = parsedList
                        repository.clearAndInsert(parsedList)
                    }
                    _isAuthorized.value = true
                    _error.value = null
                }
            } catch (e: Exception) {
                Log.e("AgendaViewModel", "Error fetching device calendar events", e)
            } finally {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                }
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
