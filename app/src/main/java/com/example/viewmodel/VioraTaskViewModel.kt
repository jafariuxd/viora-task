package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CalendarEvent
import com.example.model.Task
import com.example.model.TaskStatus
import com.example.model.WeatherInfo
import com.example.model.User
import com.example.model.Team
import com.example.model.TaskList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VioraTaskViewModel : ViewModel() {

    // Current navigation tab: "home", "new_task", "teams"
    private val _currentTab = MutableStateFlow("home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    // Real-time time display (e.g., "22:13")
    private val _currentTime = MutableStateFlow("")
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    // Real-time date display (e.g., "Wednesday, 29 Aug")
    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Teams state
    private val _teams = MutableStateFlow(listOf("All Lists", "Chamedoon", "GymShow", "Iranicard", "Rahbord", "Viora Design"))
    val teams: StateFlow<List<String>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow("All Lists")
    val selectedTeam: StateFlow<String> = _selectedTeam.asStateFlow()

    private val _viewingList = MutableStateFlow<String?>(null)
    val viewingList: StateFlow<String?> = _viewingList.asStateFlow()

    fun viewListDetail(list: String?) {
        _viewingList.value = list
    }

    private val _viewingTeam = MutableStateFlow<String?>(null)
    val viewingTeam: StateFlow<String?> = _viewingTeam.asStateFlow()

    fun viewTeamDetail(team: String?) {
        _viewingTeam.value = team
    }

    fun selectTeam(team: String) {
        _selectedTeam.value = team
    }

    fun addTeam(team: String) {
        if (team.isNotBlank() && !_teams.value.contains(team)) {
            _teams.value = _teams.value + team
            _selectedTeam.value = team
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
    private val _unplannedCount = MutableStateFlow(14)
    val unplannedCount: StateFlow<Int> = _unplannedCount.asStateFlow()

    // Full task pool of mock data (40 tasks)
    private val fullTaskPool = mutableListOf<Task>()
    // Mock entities for testing fallback logic
    private val mockUsers = mutableMapOf<String, User>()
    private val mockTeams = mutableMapOf<String, Team>()
    private val mockLists = mutableMapOf<String, TaskList>()

    init {
        setupMockHierarchy()
        // generateMockTasks() // Mock tasks removed as requested
        updateNextTask()
        startClock()
        startWeatherTimer()
    }

    private fun setupMockHierarchy() {
        val myUser = User(id = "user1", name = "Mehran", username = "mehran", defaultDeadlineDays = 5)
        val myTeam = Team(id = "team1", name = "My Team", ownerId = "user1", defaultDeadlineDays = 3)
        val myList = TaskList(id = "list1", name = "My List", teamId = "team1", defaultDeadlineDays = 2)

        mockUsers[myUser.id] = myUser
        mockTeams[myTeam.id] = myTeam
        mockLists[myList.id] = myList
    }

    private fun computeTaskDeadline(task: Task): Task {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val deadlineDays = task.specificDeadlineDays 
            ?: task.listId?.let { mockLists[it]?.defaultDeadlineDays }
            ?: task.teamId?.let { mockTeams[it]?.defaultDeadlineDays }
            ?: task.userId?.let { mockUsers[it]?.defaultDeadlineDays }
            ?: 0 

        val deadlineDate = today.clone() as Calendar
        deadlineDate.add(Calendar.DAY_OF_YEAR, deadlineDays)
        
        val diff = deadlineDate.timeInMillis - today.timeInMillis
        val daysLeft = (diff / (1000 * 60 * 60 * 24)).toInt()
        
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        val dueDateText = dateFormat.format(deadlineDate.time)
        
        return task.copy(
            computedDeadlineMillis = deadlineDate.timeInMillis,
            daysLeft = daysLeft,
            dueDateText = dueDateText
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
        val subList = fullTaskPool.take(count)
        _tasks.value = subList
        _hasMoreToLoad.value = count < fullTaskPool.size
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
        // Find the next task that is In Progress or To Do, with the lowest daysLeft
        _nextTask.value = fullTaskPool.firstOrNull { it.status != TaskStatus.DONE }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        // Update in pool
        val index = fullTaskPool.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val updatedTask = fullTaskPool[index].copy(status = newStatus)
            fullTaskPool[index] = updatedTask
            // Update the display list
            loadCurrentTasks()
            updateNextTask()
        }
    }

    fun addTask(task: Task) {
        val updatedTask = computeTaskDeadline(task)
        fullTaskPool.add(updatedTask)
        fullTaskPool.sortBy { it.daysLeft }
        loadCurrentTasks()
        updateNextTask()
    }

    fun upsertTask(task: Task) {
        val updatedTask = computeTaskDeadline(task)
        val index = fullTaskPool.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            fullTaskPool[index] = updatedTask
        } else {
            fullTaskPool.add(updatedTask)
        }
        fullTaskPool.sortBy { it.daysLeft }
        loadCurrentTasks()
        updateNextTask()
    }

    fun deleteTask(taskId: String) {
        val index = fullTaskPool.indexOfFirst { it.id == taskId }
        if (index != -1) {
            fullTaskPool.removeAt(index)
            loadCurrentTasks()
            updateNextTask()
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
            var minutes = 0
            while (true) {
                _weatherInfo.value = _weatherInfo.value.copy(
                    lastRefreshText = if (minutes == 0) "Last refresh: Just now" else "Last refresh: $minutes min ago"
                )
                delay(60000) // update refresh statement every minute
                minutes++
            }
        }
    }
}
