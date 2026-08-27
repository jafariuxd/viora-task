package com.example.viewmodel
import com.example.network.viora.VioraNetworkModule
import com.example.model.viora.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CalendarEvent
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.model.WeatherInfo
import com.example.model.User
import com.example.model.Team
import com.example.model.TaskList
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VioraTaskViewModel(application: Application) : AndroidViewModel(application) {

    private val _scannedUser = MutableStateFlow<com.example.model.viora.UserResponseDto?>(null)
    val scannedUser: StateFlow<com.example.model.viora.UserResponseDto?> = _scannedUser.asStateFlow()
    
    private val _isScannedUserLoading = MutableStateFlow(false)
    val isScannedUserLoading: StateFlow<Boolean> = _isScannedUserLoading.asStateFlow()
    
    val fullTeamsList: List<com.example.model.Team>
        get() = mockTeams.values.toList()



    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // Current navigation tab: "home", "new_task", "teams"
    private val _messages = kotlinx.coroutines.flow.MutableSharedFlow<com.example.model.MessageEvent>()
    val messages = _messages.asSharedFlow()

    fun showMessage(message: String, isError: Boolean = false) {
        viewModelScope.launch {
            _messages.emit(com.example.model.MessageEvent(message = message, isError = isError))
        }
    }

    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _quickAddSignal = MutableStateFlow(false)
    val quickAddSignal: StateFlow<Boolean> = _quickAddSignal.asStateFlow()

    fun triggerQuickAdd() {
        _quickAddSignal.value = true
    }

    fun consumeQuickAdd() {
        _quickAddSignal.value = false
    }

    private val _navigateToBriefSignal = MutableStateFlow(false)
    val navigateToBriefSignal: StateFlow<Boolean> = _navigateToBriefSignal.asStateFlow()

    fun triggerDailyBrief() {
        _navigateToBriefSignal.value = true
    }

    fun consumeDailyBrief() {
        _navigateToBriefSignal.value = false
    }

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userHandle = MutableStateFlow("user")
    val userHandle: StateFlow<String> = _userHandle.asStateFlow()

    private val _userAvatarUri = MutableStateFlow<String?>(null)
    val userAvatarUri: StateFlow<String?> = _userAvatarUri.asStateFlow()

    // Real-time time display (e.g., "22:13")
    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    // Real-time date display (e.g., "Wednesday, 29 Aug")
    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Teams state
    private val _teams = MutableStateFlow(listOf("Personal"))
    val teams: StateFlow<List<String>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow("All Lists")
    val selectedTeam: StateFlow<String> = _selectedTeam.asStateFlow()

    private val _lists = MutableStateFlow<List<TaskList>>(emptyList())
    private val _activeLists = MutableStateFlow<List<TaskList>>(emptyList())
    val lists: StateFlow<List<TaskList>> = _activeLists.asStateFlow()

    private val _archivedTeams = MutableStateFlow<List<Team>>(emptyList())
    val archivedTeams: StateFlow<List<Team>> = _archivedTeams.asStateFlow()

    private val _archivedLists = MutableStateFlow<List<TaskList>>(emptyList())
    val archivedLists: StateFlow<List<TaskList>> = _archivedLists.asStateFlow()

    private val _archivedTasks = MutableStateFlow<List<Task>>(emptyList())
    val archivedTasks: StateFlow<List<Task>> = _archivedTasks.asStateFlow()

    private val _viewingList = MutableStateFlow<String?>(null)
    val viewingList: StateFlow<String?> = _viewingList.asStateFlow()

    fun viewListDetail(list: String?) {
        _viewingList.value = list
    }

    private val _viewingTeam = MutableStateFlow<String?>(null)
    val viewingTeam: StateFlow<String?> = _viewingTeam.asStateFlow()

    private val _wizardCompleted = MutableStateFlow(false)
    val wizardCompleted: StateFlow<Boolean> = _wizardCompleted.asStateFlow()

    fun markWizardCompleted() {
        if (!_wizardCompleted.value) {
            _wizardCompleted.value = true
            val prefs = getApplication<Application>().getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("wizard_completed", true).apply()
        }
    }

    fun viewTeamDetail(team: String?) {
        _viewingTeam.value = team
    }

    fun selectTeam(team: String) {
        _selectedTeam.value = team
    }

    
    fun addTeam(team: String, defaultDeadlineDays: Int? = null) {
        viewModelScope.launch {
            try {
                val req = CreateTeamDto(name = team, deadlineDays = defaultDeadlineDays)
                val res = VioraNetworkModule.api.createTeam(req)
                if (res.success) {
                    showMessage("Team created successfully")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun updateTeamMembers(teamName: String, addUsernames: List<String>? = null, removeUsernames: List<String>? = null) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == teamName } ?: return@launch
                val req = UpdateTeamDto(addUsernames = addUsernames, removeUsernames = removeUsernames)
                val res = VioraNetworkModule.api.updateTeam(t.id, req)
                if (res.success) {
                    showMessage("Team members updated")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun updateTeam(oldTeamName: String, newTeamName: String, defaultDeadlineDays: Int?) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == oldTeamName } ?: return@launch
                val req = UpdateTeamDto(name = newTeamName, deadlineDays = defaultDeadlineDays)
                val res = VioraNetworkModule.api.updateTeam(t.id, req)
                if (res.success) {
                    showMessage("Team updated successfully")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun deleteTeam(teamName: String) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == teamName } ?: return@launch
                val res = VioraNetworkModule.api.deleteTeam(t.id)
                if (res.success) {
                    showMessage("Team deleted")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun addListToTeam(teamName: String, listName: String, defaultDeadlineDays: Int? = null) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == teamName } ?: return@launch
                val req = CreateListDto(name = listName, teamId = t.id, deadlineDays = defaultDeadlineDays)
                val res = VioraNetworkModule.api.createList(req)
                if (res.success) {
                    showMessage("List created")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun deleteList(listName: String) {
        viewModelScope.launch {
            try {
                val l = mockLists.values.find { it.name == listName } ?: return@launch
                val res = VioraNetworkModule.api.deleteList(l.id)
                if (res.success) {
                    showMessage("List deleted")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }
fun updateActiveAndArchivedStates() {
        // Ensure personal team entry exists
        if (!mockTeams.containsKey("personal_space")) {
            mockTeams["personal_space"] = Team(
                id = "personal_space",
                name = "Personal",
                ownerId = "user1",
                isArchived = false,
                members = emptyList()
            )
        }

        // Ensure default sub-lists for Personal team exist
        if (!mockLists.containsKey("unplanned_tasks")) {
            mockLists["unplanned_tasks"] = TaskList(
                id = "unplanned_tasks",
                name = "Unplanned Tasks",
                teamId = "personal_space",
                isArchived = false
            )
        }
        if (!mockLists.containsKey("personal_tasks") && mockLists.values.none { (it.teamId == "personal_space" || it.teamId.isEmpty()) && it.id != "unplanned_tasks" && !it.isArchived }) {
            mockLists["personal_tasks"] = TaskList(
                id = "personal_tasks",
                name = "My Tasks",
                teamId = "personal_space",
                isArchived = false
            )
        }

        _lists.value = mockLists.values.toList()

        // Update active lists
        _activeLists.value = _lists.value.filter { list ->
            !list.isArchived && (list.teamId.isEmpty() || list.teamId == "personal_space" || mockTeams[list.teamId] == null || mockTeams[list.teamId]?.isArchived != true)
        }

        // Update active teams based on mockTeams isArchived status
        val activeNames = mutableListOf<String>()
        if (_teams.value.contains("All Lists")) {
            activeNames.add("All Lists")
        }
        if (!activeNames.contains("Personal")) {
            activeNames.add("Personal")
        }
        mockTeams.values.sortedBy { it.name }.forEach { team ->
            if (!team.isArchived) {
                if (team.name != "All Lists" && team.name != "Personal" && team.name != "Personal Space" && !activeNames.contains(team.name)) {
                    activeNames.add(team.name)
                }
            }
        }
        _teams.value = activeNames

        // Update archived lists/teams/tasks
        _archivedTeams.value = mockTeams.values.filter { it.isArchived }
        _archivedLists.value = _lists.value.filter { it.isArchived }
        _archivedTasks.value = fullTaskPool.filter { it.isArchived }

        loadCurrentTasks()
        updateNextTask()
        updateCompletedCount()
    }

    
    fun archiveTeam(teamName: String) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == teamName } ?: return@launch
                val res = VioraNetworkModule.api.archiveTeam(t.id)
                if (res.success) {
                    showMessage("Team archived")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun restoreTeam(teamId: String) {
        viewModelScope.launch {
            try {
                val res = VioraNetworkModule.api.unarchiveTeam(teamId)
                if (res.success) {
                    showMessage("Team restored")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun archiveList(listId: String) {
        viewModelScope.launch {
            try {
                val res = VioraNetworkModule.api.archiveList(listId)
                if (res.success) {
                    showMessage("List archived")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun restoreList(listId: String) {
        // No unarchive endpoint available for lists based on JSON
        showMessage("Restoring lists is not supported currently.", true)
    }

    fun archiveTask(taskId: String) {
        viewModelScope.launch {
            try {
                val res = VioraNetworkModule.api.archiveTask(taskId)
                if (res.success) {
                    showMessage("Task archived")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun restoreTask(taskId: String) {
        // No unarchive endpoint available for tasks based on JSON
        showMessage("Restoring tasks is not supported currently.", true)
    }
fun getTeamMembers(teamId: String?): List<String> {
        if (teamId == null) return emptyList()
        val team = mockTeams[teamId] ?: return emptyList()
        return team.members.map { it.username }
    }

    fun getTeamMemberUsers(teamId: String?): List<com.example.model.User> {
        if (teamId == null) return emptyList()
        val team = mockTeams[teamId] ?: return emptyList()
        return team.members
    }

    fun getTeamIdByName(teamName: String): String? {
        return mockTeams.values.find { it.name == teamName }?.id
    }

    fun getListIdByName(listName: String): String? {
        return mockLists.values.find { it.name == listName }?.id
    }

    fun getTeamNameById(teamId: String): String {
        if (teamId.isBlank()) return "Personal"
        return mockTeams[teamId]?.name ?: mockTeams.values.find { it.id == teamId || it.name == teamId }?.name ?: "Personal"
    }

    fun getTeamDefaultDeadline(teamIdOrName: String): Int? {
        val teamId = teamIdOrName.lowercase().replace(" ", "_")
        return mockTeams[teamId]?.defaultDeadlineDays ?: mockTeams.values.find { it.name == teamIdOrName || it.id == teamIdOrName }?.defaultDeadlineDays
    }

    fun getUserDefaultDeadline(): Int {
        return mockUsers["user1"]?.defaultDeadlineDays ?: 5
    }

    private val _isDashboardLoading = MutableStateFlow(true)
    val isDashboardLoading: StateFlow<Boolean> = _isDashboardLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshDashboard() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchLocationAndWeather()
            loadCurrentTasks()
            updateNextTask()
            updateCompletedCount()
            delay(1000)
            _isRefreshing.value = false
        }
    }

    init {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            _isDashboardLoading.value = false
        }
    }

    // Weather Info state
    private val _weatherInfo = MutableStateFlow(
        WeatherInfo(
            location = "Tehran",
            temperature = "9°",
            condition = "Sunny",
            uvIndex = "Low",
            dateText = "Wednesday, 29 Aug",
            lastRefreshText = "Last refresh: Just now"
        )
    )
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    // Weather location options
    private val _weatherAutoDetect = MutableStateFlow(true)
    val weatherAutoDetect: StateFlow<Boolean> = _weatherAutoDetect.asStateFlow()

    private val _manualCity = MutableStateFlow("Tehran")
    val manualCity: StateFlow<String> = _manualCity.asStateFlow()

    private val _manualLatitude = MutableStateFlow(35.6944)
    val manualLatitude: StateFlow<Double> = _manualLatitude.asStateFlow()

    private val _manualLongitude = MutableStateFlow(51.4215)
    val manualLongitude: StateFlow<Double> = _manualLongitude.asStateFlow()

    // Upcoming event state
    private val _upcomingEvent = MutableStateFlow(
        CalendarEvent(
            day = "23",
            month = "Aug",
            title = "Meeting with Mamad",
            time = "At 08:15 am"
        )
    )
    val upcomingEvent: StateFlow<CalendarEvent> = _upcomingEvent.asStateFlow()

    // Task list data state
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _allTasks = MutableStateFlow<List<Task>>(emptyList())
    val allTasks: StateFlow<List<Task>> = _allTasks.asStateFlow()

    // Lazy load state
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreToLoad = MutableStateFlow(true)
    val hasMoreToLoad: StateFlow<Boolean> = _hasMoreToLoad.asStateFlow()

    // Total number of tasks shown (increases by 5)
    private val _visibleCount = MutableStateFlow(5)
    val visibleCount: StateFlow<Int> = _visibleCount.asStateFlow()

    // Next urgent task (sorted first by closest deadline and not done)
    private val _nextTask = MutableStateFlow<Task?>(null)
    val nextTask: StateFlow<Task?> = _nextTask.asStateFlow()

    // Count of unplanned tasks

    
    

    private val _completedCount = MutableStateFlow(0)
    val completedCount: StateFlow<Int> = _completedCount.asStateFlow()

    // Full task pool of mock data (40 tasks)
    private val fullTaskPool = mutableListOf<Task>()
    private val idMapping = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val pendingTaskCreations = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    // Mock entities for testing fallback logic
    private val mockUsers = mutableMapOf<String, User>()
    private val mockTeams = mutableMapOf<String, Team>()
    private val mockLists = mutableMapOf<String, TaskList>()
    private var weatherRefreshMinutes = 0

    init {
        loadState()
        updateActiveAndArchivedStates()
        
        val taskPrefs = getApplication<Application>().getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
        if (taskPrefs.getBoolean("wizard_completed", false) || fullTaskPool.isNotEmpty()) {
            _wizardCompleted.value = true
            if (!taskPrefs.getBoolean("wizard_completed", false)) {
                taskPrefs.edit().putBoolean("wizard_completed", true).apply()
            }
        }
        
        // Load weather settings and cached weather info
        val weatherPrefs = getApplication<Application>().getSharedPreferences("viora_weather_prefs", Context.MODE_PRIVATE)
        _weatherAutoDetect.value = weatherPrefs.getBoolean("weather_auto_detect", true)
        _manualCity.value = weatherPrefs.getString("weather_manual_city", "Tehran") ?: "Tehran"
        _manualLatitude.value = weatherPrefs.getFloat("weather_manual_lat", 35.6944f).toDouble()
        _manualLongitude.value = weatherPrefs.getFloat("weather_manual_lon", 51.4215f).toDouble()

        // Apply cached weather info if present
        val cachedLoc = weatherPrefs.getString("cached_weather_location", "Tehran") ?: "Tehran"
        val cachedTemp = weatherPrefs.getString("cached_weather_temp", "9°") ?: "9°"
        val cachedCond = weatherPrefs.getString("cached_weather_condition", "Sunny") ?: "Sunny"
        val cachedUv = weatherPrefs.getString("cached_weather_uv", "Low") ?: "Low"
        _weatherInfo.value = _weatherInfo.value.copy(
            location = cachedLoc,
            temperature = cachedTemp,
            condition = cachedCond,
            uvIndex = cachedUv
        )

        startClock()
        startWeatherTimer()
        loadUserInfo()
        viewModelScope.launch(Dispatchers.IO) {
            fetchLocationAndWeather()
        }
    }

    fun loadUserInfo() {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        _userName.value = authPrefs.getString("user_name", "User") ?: "User"
        _userHandle.value = authPrefs.getString("user_username", "user") ?: "user"
        _userAvatarUri.value = authPrefs.getString("user_avatar_uri", null)
    }

    fun getUserName(): String {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        return authPrefs.getString("user_name", "User") ?: "User"
    }

    fun getUserAvatarUri(): String? {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        return authPrefs.getString("user_avatar_uri", null)
    }

    private fun saveState() {
        val prefs = getApplication<Application>().getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        try {
            val tasksAdapter = moshi.adapter<List<Task>>(Types.newParameterizedType(List::class.java, Task::class.java))
            editor.putString("tasks", tasksAdapter.toJson(fullTaskPool))
        } catch (e: Exception) { e.printStackTrace() }
        
        try {
            val teamsAdapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
            editor.putString("teams", teamsAdapter.toJson(_teams.value))
        } catch (e: Exception) { e.printStackTrace() }

        try {
            val mockTeamsAdapter = moshi.adapter<Map<String, Team>>(Types.newParameterizedType(Map::class.java, String::class.java, Team::class.java))
            editor.putString("mock_teams", mockTeamsAdapter.toJson(mockTeams))
        } catch (e: Exception) { e.printStackTrace() }

        try {
            val mockListsAdapter = moshi.adapter<Map<String, TaskList>>(Types.newParameterizedType(Map::class.java, String::class.java, TaskList::class.java))
            editor.putString("mock_lists", mockListsAdapter.toJson(mockLists))
            
            val listsAdapter = moshi.adapter<List<TaskList>>(Types.newParameterizedType(List::class.java, TaskList::class.java))
            editor.putString("lists", listsAdapter.toJson(_lists.value))
        } catch (e: Exception) { e.printStackTrace() }

        editor.apply()
    }

    

    fun fetchScannedUser(userId: String) {
        viewModelScope.launch {
            _isScannedUserLoading.value = true
            try {
                val res = com.example.network.viora.VioraNetworkModule.api.getUserProfile(userId)
                if (res.success && res.data != null) {
                    _scannedUser.value = res.data
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            } finally {
                _isScannedUserLoading.value = false
            }
        }
    }

    fun addScannedUserToTeam(teamId: String, username: String) {
        viewModelScope.launch {
            try {
                val req = com.example.model.viora.UpdateTeamDto(addUsernames = listOf(username))
                val res = com.example.network.viora.VioraNetworkModule.api.updateTeam(teamId, req)
                if (res.success) {
                    showMessage("Member recruited successfully")
                    _scannedUser.value = null
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun clearScannedUser() {
        _scannedUser.value = null
    }

    fun loadState() {
        loadUserInfo()
        val tm = VioraNetworkModule.getTokenManager()
        if (tm == null || tm.getAccessToken().isNullOrEmpty()) {
            return
        }
        viewModelScope.launch {
            try {
                // Fetch user report
                val reportRes = VioraNetworkModule.api.getCurrentUserReport()
                if (reportRes.success && reportRes.data != null) {
                    val report = reportRes.data
                    val user = User(id = report.userId, name = report.fullName, username = report.username, defaultDeadlineDays = report.remainingDays)
                    mockUsers.clear()
                    mockUsers[user.id] = user
                    _userName.value = report.fullName
                    _userHandle.value = report.username
                    // avatar can be set if needed
                }
                
                // Fetch Teams
                val teamsRes = VioraNetworkModule.api.getTeams(perPage = 100)
                if (teamsRes.success && teamsRes.data != null) {
                    mockTeams.clear()
                    mockTeams["personal_space"] = Team(
                        id = "personal_space",
                        name = "Personal",
                        ownerId = "user1",
                        isArchived = false,
                        members = emptyList()
                    )
                    teamsRes.data.items.forEach { t ->
                        mockTeams[t.id] = Team(
                            id = t.id, 
                            name = t.name, 
                            ownerId = t.ownerId, 
                            isArchived = t.isArchived,
                            members = t.members?.map { m -> User(id = m.id, name = m.fullName, username = m.username, defaultDeadlineDays = 0) } ?: emptyList()
                        )
                    }
                    val activeTeamList = mutableListOf("Personal")
                    mockTeams.values.filter { !it.isArchived && it.name != "Personal" && it.name != "Personal Space" }.map { it.name }.forEach { name ->
                        if (!activeTeamList.contains(name)) activeTeamList.add(name)
                    }
                    _teams.value = activeTeamList
                    _archivedTeams.value = mockTeams.values.filter { it.isArchived }.toList()
                }

                // Fetch Lists
                val listsRes = VioraNetworkModule.api.getLists()
                if (listsRes.success && listsRes.data != null) {
                    mockLists.clear()
                    listsRes.data.forEach { l ->
                        mockLists[l.id] = TaskList(
                            id = l.id, 
                            name = l.name, 
                            teamId = l.teamId ?: "", 
                            isArchived = l.isArchived
                            // members = l.members is not in ListSummaryDto, so we skip here
                        )
                    }
                    if (!mockLists.containsKey("unplanned_tasks")) {
                        mockLists["unplanned_tasks"] = TaskList(
                            id = "unplanned_tasks",
                            name = "Unplanned Tasks",
                            teamId = "personal_space",
                            isArchived = false
                        )
                    }
                    if (!mockLists.containsKey("personal_tasks") && mockLists.values.none { (it.teamId == "personal_space" || it.teamId.isEmpty()) && it.id != "unplanned_tasks" && !it.isArchived }) {
                        mockLists["personal_tasks"] = TaskList(
                            id = "personal_tasks",
                            name = "My Tasks",
                            teamId = "personal_space",
                            isArchived = false
                        )
                    }
                    _lists.value = mockLists.values.toList()
                }

                // Fetch Tasks
                val tasksRes = VioraNetworkModule.api.getTasks()
                if (tasksRes.success && tasksRes.data != null) {
                    fullTaskPool.clear()
                    tasksRes.data.forEach { t ->
                        val localStatus = when(t.status) {
                            "todo" -> TaskStatus.TODO
                            "in-progress" -> TaskStatus.IN_PROGRESS
                            "done" -> TaskStatus.DONE
                            else -> TaskStatus.TODO
                        }
                        
                        var computedMillis: Long? = null
                        var dueDateTextVal = ""
                        
                        try {
                            if (t.deadlineInfo?.actualDeadline?.isNotEmpty() == true) {
                                // e.g. "2025-04-11T12:00:00.000Z" (ISO-8601)
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                val date = t.deadlineInfo?.actualDeadline?.let { sdf.parse(it) }
                                if (date != null) {
                                    computedMillis = date.time
                                    val outFormat = java.text.SimpleDateFormat("yyyy MMM dd", java.util.Locale.getDefault())
                                    dueDateTextVal = outFormat.format(date)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        
                        fullTaskPool.add(Task(
                            id = t.id,
                            title = t.name,
                            client = t.teamName ?: "Personal",
                            listId = t.listId,
                            teamId = t.teamId,
                            status = localStatus,
                            description = t.description ?: "",
                            tags = t.tagNames ?: emptyList(),
                            daysLeft = t.deadlineInfo?.remainingDays ?: 0,
                            computedDeadlineMillis = computedMillis,
                            dueDateText = dueDateTextVal,
                            isArchived = t.isArchived,
                            folder = t.listName ?: "Unplanned Tasks"
                        ))
                    }
                }
                updateActiveAndArchivedStates()
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage("Failed to load: ${com.example.util.ErrorUtil.getErrorMessage(e)}")
            }
        }
    }
private fun computeTaskDeadline(task: Task): Task {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val deadlineDate = Calendar.getInstance()
        
        var source = com.example.model.DeadlineSource.SPECIFIC
        if (task.selectedDeadlineMillis != null) {
            deadlineDate.timeInMillis = task.selectedDeadlineMillis
        } else if (task.computedDeadlineMillis != null) {
            deadlineDate.timeInMillis = task.computedDeadlineMillis
        } else {
            var deadlineDays = 5
            source = com.example.model.DeadlineSource.USER

            val myUser = mockUsers[task.userId] ?: mockUsers["user1"] ?: mockUsers.values.firstOrNull()
            if (myUser != null) {
                deadlineDays = myUser.defaultDeadlineDays
                source = com.example.model.DeadlineSource.USER
            }

            val listObj = mockLists[task.listId] ?: mockLists.values.find { it.name == task.listId }
            val teamIdResolved = task.teamId ?: listObj?.teamId
            val teamObj = if (teamIdResolved != null) (mockTeams[teamIdResolved] ?: mockTeams.values.find { it.name == teamIdResolved || it.id == teamIdResolved }) else null
            if (teamObj != null && teamObj.defaultDeadlineDays != null) {
                deadlineDays = teamObj.defaultDeadlineDays
                source = com.example.model.DeadlineSource.TEAM
            }

            if (listObj != null && listObj.defaultDeadlineDays != null) {
                deadlineDays = listObj.defaultDeadlineDays
                source = com.example.model.DeadlineSource.LIST
            }
                
            deadlineDate.timeInMillis = task.createdAtMillis
            deadlineDate.add(Calendar.DAY_OF_YEAR, deadlineDays)
            deadlineDate.set(Calendar.HOUR_OF_DAY, 23)
            deadlineDate.set(Calendar.MINUTE, 59)
            deadlineDate.set(Calendar.SECOND, 59)
            deadlineDate.set(Calendar.MILLISECOND, 999)
        }
        
        val dayStartCal = (deadlineDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = dayStartCal.timeInMillis - today.timeInMillis
        val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
        
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        val dueDateText = if (task.dueDateText.isNotEmpty()) task.dueDateText else dateFormat.format(deadlineDate.time)
        
        return task.copy(
            computedDeadlineMillis = deadlineDate.timeInMillis,
            daysLeft = daysLeft,
            dueDateText = dueDateText,
            deadlineSource = source
        )
    }

    private fun generateMockTasks() {
        /* Mock Tasks Backup:
        val nextTaskItem = Task(
            id = "task_0",
            title = "Design wireframe and final user flow in homepage of Haj Behzad",
            client = "Iranicard",
            specificDeadlineDays = 2,
            userId = "user1",
            status = TaskStatus.IN_PROGRESS,
            assigneePhotos = listOf("mohammad", "sara", "sara", "mohammad") // represent multiple avatars +4
        )
        fullTaskPool.add(computeTaskDeadline(nextTaskItem))
        // Add more mock tasks if needed
        */
        loadCurrentTasks()
    }

    private fun loadCurrentTasks() {
        val count = _visibleCount.value
        fullTaskPool.sortBy { it.computedDeadlineMillis ?: Long.MAX_VALUE }
        val activeTasks = fullTaskPool.filter { task ->
            !task.isArchived && 
            (_lists.value.find { it.id == task.listId }?.isArchived != true) &&
            (task.teamId == null || mockTeams[task.teamId]?.isArchived != true)
        }
        _allTasks.value = activeTasks
        
        val feedTasks = activeTasks.filter { it.status != TaskStatus.DONE }
        val subList = feedTasks.take(count)
        _tasks.value = subList
        _hasMoreToLoad.value = count < feedTasks.size
    }

    fun loadMoreTasks() {
        if (_isLoadingMore.value || !_hasMoreToLoad.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            // Simulate 1.5 second loading delay
            delay(1500)
            val newCount = _visibleCount.value + 5
            _visibleCount.value = newCount
            loadCurrentTasks()
            _isLoadingMore.value = false
        }
    }

    private fun updateNextTask() {
        fullTaskPool.sortBy { it.computedDeadlineMillis ?: Long.MAX_VALUE }
        // Find the next task that is In Progress or To Do, with the lowest daysLeft / closest deadline time
        _nextTask.value = fullTaskPool.firstOrNull { task ->
            task.status != TaskStatus.DONE &&
            !task.isArchived &&
            (_lists.value.find { it.id == task.listId }?.isArchived != true) &&
            (task.teamId == null || mockTeams[task.teamId]?.isArchived != true)
        }
    }

    
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                val realId = idMapping[taskId] ?: taskId
                val statusStr = when(newStatus) {
                    TaskStatus.TODO -> "todo"
                    TaskStatus.IN_PROGRESS -> "in-progress"
                    TaskStatus.DONE -> "done"
                }
                val req = UpdateTaskDto(status = statusStr)
                val res = VioraNetworkModule.api.updateTask(realId, req)
                if (res.success) {
                    // Update local pool immediately for UI speed
                    val idx = fullTaskPool.indexOfFirst { it.id == realId || it.id == taskId }
                    if (idx != -1) {
                        fullTaskPool[idx] = fullTaskPool[idx].copy(status = newStatus)
                        loadCurrentTasks()
                        updateNextTask()
                        updateCompletedCount()
                    }
                    // Sync fully
                    loadState()
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }

    fun addTask(task: Task) {
        markWizardCompleted()
        upsertTask(task)
    }

    private fun updateCompletedCount() {
        _completedCount.value = fullTaskPool.count { task ->
            task.status == TaskStatus.DONE &&
            !task.isArchived &&
            (task.teamId == null || mockTeams[task.teamId]?.isArchived != true)
        }
    }

    fun upsertTask(task: Task) {
        markWizardCompleted()
        viewModelScope.launch {
            try {
                var realId = idMapping[task.id] ?: task.id

                // Wait if task creation is in flight for this local ID
                while (pendingTaskCreations.contains(task.id) || pendingTaskCreations.contains(realId)) {
                    kotlinx.coroutines.delay(50)
                    realId = idMapping[task.id] ?: task.id
                }

                val isExisting = fullTaskPool.any { it.id == realId } || idMapping.containsKey(realId)
                val taskToSave = task.copy(id = realId)

                var listId = mockLists.values.find { it.name == taskToSave.folder || it.id == taskToSave.listId }?.id
                if (listId == "unplanned_tasks" || listId == "personal_tasks" || listId == null) {
                    var team = mockTeams.values.find { (it.name == "Personal" || it.name == "Personal Space") && it.id != "personal_space" }
                    if (team == null) {
                        val tReq = CreateTeamDto(name = "Personal Space", deadlineDays = 5)
                        val tRes = VioraNetworkModule.api.createTeam(tReq)
                        if (tRes.success && tRes.data != null) {
                            team = Team(id = tRes.data.id, name = tRes.data.name, ownerId = tRes.data.ownerId, isArchived = false)
                            mockTeams[tRes.data.id] = team
                        }
                    }
                    
                    val listName = if (taskToSave.folder.isNotEmpty() && taskToSave.folder != "Unplanned Tasks" && taskToSave.folder != "My Tasks" && taskToSave.folder != "unplanned_tasks" && taskToSave.folder != "personal_tasks") taskToSave.folder else "My Tasks"
                    var list = mockLists.values.find { it.name == listName && it.teamId == team?.id && it.id != "unplanned_tasks" && it.id != "personal_tasks" }
                    if (list == null && team != null) {
                        val lReq = CreateListDto(name = listName, teamId = team.id, deadlineDays = 5)
                        val lRes = VioraNetworkModule.api.createList(lReq)
                        if (lRes.success && lRes.data != null) {
                            list = TaskList(id = lRes.data.list.id, name = lRes.data.list.name, teamId = team.id, isArchived = false)
                            mockLists[lRes.data.list.id] = list
                        }
                    }
                    listId = list?.id
                }
                val calculatedDeadlineDays = taskToSave.computedDeadlineMillis?.let {
                    val current = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    val target = Calendar.getInstance().apply {
                        timeInMillis = it
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    ((target.timeInMillis - current.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                }

                val statusStr = when(taskToSave.status) {
                    TaskStatus.TODO -> "todo"
                    TaskStatus.IN_PROGRESS -> "in-progress"
                    TaskStatus.DONE -> "done"
                }

                if (!isExisting) {
                    pendingTaskCreations.add(task.id)
                    try {
                        val req = CreateTaskDto(
                            name = taskToSave.title,
                            description = taskToSave.description.ifEmpty { null },
                            deadlineDays = calculatedDeadlineDays,
                            listId = listId,
                            tags = taskToSave.tags.ifEmpty { null },
                            status = statusStr,
                            usernames = taskToSave.assigneePhotos.ifEmpty { null }
                        )
                        val res = VioraNetworkModule.api.createTask(req)
                        if (res.success && res.data != null) {
                            val serverId = res.data.task.id
                            idMapping[task.id] = serverId
                            idMapping[serverId] = serverId
                            showMessage("Task added")
                            loadState()
                        } else {
                            showMessage(res.message ?: "Failed to add task", true)
                        }
                    } finally {
                        pendingTaskCreations.remove(task.id)
                    }
                    return@launch
                }

                // Existing task update
                val req = UpdateTaskDto(
                    name = taskToSave.title,
                    description = taskToSave.description,
                    deadlineDays = calculatedDeadlineDays,
                    status = statusStr,
                    listId = listId,
                    tagsToAdd = taskToSave.tags
                )
                val res = VioraNetworkModule.api.updateTask(taskToSave.id, req)
                if (res.success) {
                    showMessage("Task updated")
                    loadState()
                } else {
                    showMessage(res.message ?: "Failed to update task", true)
                }
            } catch(e: Exception) {
                e.printStackTrace()
                showMessage("Error saving task: ${com.example.util.ErrorUtil.getErrorMessage(e)}", true)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val realId = idMapping[taskId] ?: taskId
                val res = VioraNetworkModule.api.deleteTask(realId)
                if (res.success) {
                    showMessage("Task deleted")
                    idMapping.remove(taskId)
                    idMapping.remove(realId)
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
            }
        }
    }
fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    private fun startClock() {
        viewModelScope.launch {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEEE, d MMM", Locale.getDefault())
            while (true) {
                val calendar = Calendar.getInstance()
                _currentTime.value = timeFormat.format(calendar.time)
                _currentDate.value = dateFormat.format(calendar.time)
                delay(1000) // Update every second
            }
        }
    }

    private fun startWeatherTimer() {
        viewModelScope.launch {
            while (true) {
                _weatherInfo.value = _weatherInfo.value.copy(
                    lastRefreshText = if (weatherRefreshMinutes == 0) "Last refresh: Just now" else "Last refresh: $weatherRefreshMinutes min ago"
                )
                delay(60000) // update refresh statement every minute
                weatherRefreshMinutes++
            }
        }
    }

    private fun fetchLocationAndWeather() {
        var city = "Tehran"
        var lat = 35.6944
        var lon = 51.4215
        
        val isAuto = _weatherAutoDetect.value
        if (!isAuto) {
            city = _manualCity.value
            lat = _manualLatitude.value
            lon = _manualLongitude.value
            android.util.Log.d("VioraTaskViewModel", "Using manual weather city: $city ($lat, $lon)")
        } else {
            // Fetch IP location with User-Agent set to avoid blocking
            try {
                val url = java.net.URL("https://freeipapi.com/api/json")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = org.json.JSONObject(response)
                if (json.has("cityName") && !json.isNull("cityName")) {
                    val foundCity = json.getString("cityName")
                    if (foundCity.isNotBlank()) {
                        city = foundCity
                        lat = json.optDouble("latitude", 35.6944)
                        lon = json.optDouble("longitude", 51.4215)
                        android.util.Log.d("VioraTaskViewModel", "freeipapi city found: $city")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VioraTaskViewModel", "freeipapi failed: ${e.message}")
                try {
                    val url = java.net.URL("https://ipapi.co/json/")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(response)
                    if (json.has("city") && !json.isNull("city")) {
                        val foundCity = json.getString("city")
                        if (foundCity.isNotBlank()) {
                            city = foundCity
                            lat = json.optDouble("latitude", 35.6944)
                            lon = json.optDouble("longitude", 51.4215)
                            android.util.Log.d("VioraTaskViewModel", "ipapi city found: $city")
                        }
                    }
                } catch (e2: Exception) {
                    android.util.Log.e("VioraTaskViewModel", "ipapi failed: ${e2.message}")
                    try {
                        val url = java.net.URL("https://ipinfo.io/json")
                        val conn = url.openConnection() as java.net.HttpURLConnection
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        conn.connectTimeout = 4000
                        conn.readTimeout = 4000
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(response)
                        if (json.has("city") && !json.isNull("city")) {
                            val foundCity = json.getString("city")
                            if (foundCity.isNotBlank()) {
                                city = foundCity
                                val loc = json.optString("loc", "35.6944,51.4215")
                                val parts = loc.split(",")
                                if (parts.size == 2) {
                                    lat = parts[0].toDoubleOrNull() ?: 35.6944
                                    lon = parts[1].toDoubleOrNull() ?: 51.4215
                                }
                                android.util.Log.d("VioraTaskViewModel", "ipinfo city found: $city")
                            }
                        }
                    } catch (e3: Exception) {
                        android.util.Log.e("VioraTaskViewModel", "ipinfo failed: ${e3.message}")
                    }
                }
            }
        }

        // 2. Fetch current weather and max daily UV index
        var tempStr = "9°"
        var conditionStr = "Sunny"
        var uvIndexStr = "Low"
        try {
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&daily=uv_index_max&timezone=auto"
            val conn = java.net.URL(weatherUrl).openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val json = org.json.JSONObject(response)
            if (json.has("current_weather")) {
                val current = json.getJSONObject("current_weather")
                val temp = current.getDouble("temperature")
                tempStr = "${Math.round(temp)}°"
                val code = current.optInt("weathercode", 0)
                conditionStr = when (code) {
                    0, 1 -> "Sunny"
                    2, 3 -> "Cloudy"
                    45, 48 -> "Foggy"
                    51, 53, 55, 61, 63, 65, 80, 81, 82 -> "Rainy"
                    71, 73, 75 -> "Snowy"
                    95 -> "Thunderstorm"
                    else -> "Sunny"
                }
            }
            if (json.has("daily")) {
                val daily = json.getJSONObject("daily")
                if (daily.has("uv_index_max")) {
                    val uvArray = daily.getJSONArray("uv_index_max")
                    if (uvArray.length() > 0) {
                        val uvVal = uvArray.getDouble(0)
                        uvIndexStr = when {
                            uvVal >= 6.0 -> "High"
                            uvVal >= 3.0 -> "Moderate"
                            else -> "Low"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("VioraTaskViewModel", "Weather fetch failed: ${e.message}")
        }

        // 3. Update weather state and cache it
        viewModelScope.launch {
            weatherRefreshMinutes = 0
            _weatherInfo.value = _weatherInfo.value.copy(
                location = city,
                temperature = tempStr,
                condition = conditionStr,
                uvIndex = uvIndexStr,
                lastRefreshText = "Last refresh: Just now"
            )

            // Cache it in shared preferences for offline resilience
            val weatherPrefs = getApplication<Application>().getSharedPreferences("viora_weather_prefs", Context.MODE_PRIVATE)
            weatherPrefs.edit().apply {
                putString("cached_weather_location", city)
                putString("cached_weather_temp", tempStr)
                putString("cached_weather_condition", conditionStr)
                putString("cached_weather_uv", uvIndexStr)
                apply()
            }
        }
    }

    fun setWeatherSettings(autoDetect: Boolean, city: String, lat: Double, lon: Double) {
        _weatherAutoDetect.value = autoDetect
        _manualCity.value = city
        _manualLatitude.value = lat
        _manualLongitude.value = lon
        
        val weatherPrefs = getApplication<Application>().getSharedPreferences("viora_weather_prefs", Context.MODE_PRIVATE)
        weatherPrefs.edit().apply {
            putBoolean("weather_auto_detect", autoDetect)
            putString("weather_manual_city", city)
            putFloat("weather_manual_lat", lat.toFloat())
            putFloat("weather_manual_lon", lon.toFloat())
            apply()
        }
        
        // Refresh weather data immediately in the background
        viewModelScope.launch(Dispatchers.IO) {
            fetchLocationAndWeather()
        }
    }
}
