package com.example.viewmodel
import com.example.network.viora.VioraNetworkModule
import com.example.model.viora.*
import com.example.firebase.VioraAnalytics
import com.example.firebase.VioraCrashlytics
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
import com.example.model.TeamRole
import com.example.model.TaskList
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // Deep Link Navigation Target Event (CMP-ready)
    private val _deepLinkTarget = MutableStateFlow<com.example.util.VioraDeepLink?>(null)
    val deepLinkTarget: StateFlow<com.example.util.VioraDeepLink?> = _deepLinkTarget.asStateFlow()

    fun handleDeepLink(deepLink: com.example.util.VioraDeepLink) {
        _deepLinkTarget.value = deepLink
    }

    fun consumeDeepLink() {
        _deepLinkTarget.value = null
    }

    fun getTaskById(taskId: String): Task? {
        val realId = idMapping[taskId] ?: taskId
        return fullTaskPool.firstOrNull { it.id == realId || it.id == taskId }
    }

    fun getTeamById(teamId: String): Team? {
        return mockTeams[teamId] ?: mockTeams.values.firstOrNull { it.name.equals(teamId, ignoreCase = true) }
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
    private val _teams = MutableStateFlow(listOf("Personal Space"))
    val teams: StateFlow<List<String>> = _teams.asStateFlow()

    private val _selectedTeam = MutableStateFlow("All Lists")
    val selectedTeam: StateFlow<String> = _selectedTeam.asStateFlow()

    private val _lists = MutableStateFlow<List<TaskList>>(emptyList())
    private val _activeLists = MutableStateFlow<List<TaskList>>(emptyList())
    val lists: StateFlow<List<TaskList>> = _activeLists.asStateFlow()

    private val _personalListInfo = MutableStateFlow<PersonalListResponseDto?>(null)
    val personalListInfo: StateFlow<PersonalListResponseDto?> = _personalListInfo.asStateFlow()

    private val _personalListId = MutableStateFlow<String?>("personal_tasks")
    val personalListId: StateFlow<String?> = _personalListId.asStateFlow()

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

    // Team Invitations state
    private val _pendingTeamInvitations = MutableStateFlow<List<TeamInviteResponseDto>>(emptyList())
    val pendingTeamInvitations: StateFlow<List<TeamInviteResponseDto>> = _pendingTeamInvitations.asStateFlow()

    private val _isInvitationsLoading = MutableStateFlow(false)
    val isInvitationsLoading: StateFlow<Boolean> = _isInvitationsLoading.asStateFlow()

    fun loadTeamInvitations() {
        viewModelScope.launch {
            _isInvitationsLoading.value = true
            try {
                val res = VioraNetworkModule.api.getTeamInvitations()
                if (res.success && res.data != null) {
                    _pendingTeamInvitations.value = res.data
                }
            } catch (e: Exception) {
                // Non-fatal or offline
            } finally {
                _isInvitationsLoading.value = false
            }
        }
    }

    fun acceptTeamInvitation(inviteIdOrTeamId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                var realInviteId = inviteIdOrTeamId
                val pending = _pendingTeamInvitations.value.find { it.id == inviteIdOrTeamId || it.teamId == inviteIdOrTeamId }
                if (pending != null) {
                    realInviteId = pending.id
                } else {
                    val invRes = VioraNetworkModule.api.getTeamInvitations()
                    if (invRes.success && invRes.data != null) {
                        _pendingTeamInvitations.value = invRes.data
                        val match = invRes.data.find { it.id == inviteIdOrTeamId || it.teamId == inviteIdOrTeamId }
                        if (match != null) {
                            realInviteId = match.id
                        }
                    }
                }

                val res = VioraNetworkModule.api.acceptTeamInvitation(realInviteId)
                if (res.success) {
                    showMessage("Joined team successfully")
                    _pendingTeamInvitations.value = _pendingTeamInvitations.value.filter { it.id != realInviteId && it.teamId != realInviteId }
                    loadState()
                    onComplete?.invoke(true)
                } else {
                    showMessage(res.message, true)
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
                onComplete?.invoke(false)
            }
        }
    }

    fun declineTeamInvitation(inviteIdOrTeamId: String, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                var realInviteId = inviteIdOrTeamId
                val pending = _pendingTeamInvitations.value.find { it.id == inviteIdOrTeamId || it.teamId == inviteIdOrTeamId }
                if (pending != null) {
                    realInviteId = pending.id
                } else {
                    val invRes = VioraNetworkModule.api.getTeamInvitations()
                    if (invRes.success && invRes.data != null) {
                        _pendingTeamInvitations.value = invRes.data
                        val match = invRes.data.find { it.id == inviteIdOrTeamId || it.teamId == inviteIdOrTeamId }
                        if (match != null) {
                            realInviteId = match.id
                        }
                    }
                }

                val res = VioraNetworkModule.api.declineTeamInvitation(realInviteId)
                if (res.success) {
                    showMessage("Invitation declined")
                    _pendingTeamInvitations.value = _pendingTeamInvitations.value.filter { it.id != realInviteId && it.teamId != realInviteId }
                    onComplete?.invoke(true)
                } else {
                    showMessage(res.message, true)
                    onComplete?.invoke(false)
                }
            } catch (e: Exception) {
                showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)
                onComplete?.invoke(false)
            }
        }
    }

    suspend fun searchUsers(query: String): List<UserSearchResultDto> {
        if (query.trim().length < 3) return emptyList()
        return try {
            val res = VioraNetworkModule.api.searchUsers(query = query.trim(), limit = 20)
            if (res.success && res.data != null) {
                res.data.items
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun inviteUserToTeam(teamName: String, username: String, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == teamName }
                if (t == null) {
                    onResult?.invoke(false, "Team not found")
                    return@launch
                }
                val cleanUser = username.removePrefix("@").trim()
                val req = UpdateTeamDto(addUsernames = listOf(cleanUser))
                val res = VioraNetworkModule.api.updateTeam(t.id, req)
                if (res.success) {
                    // Track username in team's pending invitations locally
                    val updatedPending = (t.pendingInvitations + cleanUser).distinct()
                    mockTeams[t.id] = t.copy(pendingInvitations = updatedPending)
                    showMessage("Invitation sent to @$cleanUser")
                    loadState()
                    onResult?.invoke(true, null)
                } else {
                    showMessage(res.message, true)
                    onResult?.invoke(false, res.message)
                }
            } catch(e: Exception) {
                val msg = com.example.util.ErrorUtil.getErrorMessage(e)
                showMessage(msg, true)
                onResult?.invoke(false, msg)
            }
        }
    }

    fun updateTeamMembers(teamName: String, addUsernames: List<String>? = null, removeUsernames: List<String>? = null) {
        viewModelScope.launch {
            try {
                val t = mockTeams.values.find { it.name == teamName } ?: return@launch
                val cleanAdd = addUsernames?.map { it.removePrefix("@").trim() }?.filter { it.isNotBlank() }?.ifEmpty { null }
                val cleanRemove = removeUsernames?.map { it.removePrefix("@").trim() }?.filter { it.isNotBlank() }?.ifEmpty { null }
                val req = UpdateTeamDto(addUsernames = cleanAdd, removeUsernames = cleanRemove)
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

    fun getTeamRoleForUser(teamIdOrName: String, username: String): TeamRole {
        val team = mockTeams[teamIdOrName] ?: mockTeams.values.find {
            it.id.equals(teamIdOrName, ignoreCase = true) || it.name.equals(teamIdOrName, ignoreCase = true)
        }
        if (team == null) return TeamRole.MEMBER
        return team.getMemberRole(username)
    }

    fun getCurrentUserRole(teamIdOrName: String): TeamRole {
        val currentUser = getCurrentUser()
        return getTeamRoleForUser(teamIdOrName, currentUser.username)
    }

    fun updateMemberRole(
        teamName: String,
        targetUsername: String,
        newRole: TeamRole,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val team = mockTeams.values.find { it.name.equals(teamName, ignoreCase = true) }
                    ?: mockTeams[teamName]
                if (team == null) {
                    showMessage("Team not found", true)
                    onResult?.invoke(false, "Team not found")
                    return@launch
                }

                val currentUsername = getCurrentUser().username
                val currentRole = team.getMemberRole(currentUsername)

                // Permissions check: only Owner and Admin can manage roles
                if (!currentRole.canManageMembers) {
                    showMessage("Only Owners and Admins can manage roles", true)
                    onResult?.invoke(false, "Insufficient permissions")
                    return@launch
                }

                val targetCurrentRole = team.getMemberRole(targetUsername)

                // Only Owner can transfer Ownership or promote to Owner
                if (newRole == TeamRole.OWNER && currentRole != TeamRole.OWNER) {
                    showMessage("Only the Team Owner can transfer ownership", true)
                    onResult?.invoke(false, "Only Owner can transfer ownership")
                    return@launch
                }

                // Admins cannot change an Owner's role
                if (targetCurrentRole == TeamRole.OWNER && currentRole != TeamRole.OWNER) {
                    showMessage("Cannot modify Owner's role", true)
                    onResult?.invoke(false, "Cannot modify Owner's role")
                    return@launch
                }

                // Admins cannot demote or modify other Admins
                if (targetCurrentRole == TeamRole.ADMIN && currentRole == TeamRole.ADMIN && !currentUsername.equals(targetUsername, ignoreCase = true)) {
                    showMessage("Only the Team Owner can modify Admin roles", true)
                    onResult?.invoke(false, "Only Owner can modify Admin roles")
                    return@launch
                }

                val teamRoles = teamMemberRoles.getOrPut(team.id) { mutableMapOf() }

                // If transferring ownership, demote previous owner to Admin
                var updatedOwnerId = team.ownerId
                if (newRole == TeamRole.OWNER) {
                    teamRoles[currentUsername] = TeamRole.ADMIN
                    teamRoles[targetUsername] = TeamRole.OWNER
                    val targetUser = team.members.find { it.username.equals(targetUsername, ignoreCase = true) }
                    if (targetUser != null) {
                        updatedOwnerId = targetUser.id
                    }
                } else {
                    teamRoles[targetUsername] = newRole
                }

                val updatedTeam = team.copy(
                    ownerId = updatedOwnerId,
                    memberRoles = teamRoles.toMap()
                )
                mockTeams[team.id] = updatedTeam
                _teamUpdateTrigger.value += 1
                showMessage("Role updated: @$targetUsername is now a ${newRole.title}")
                onResult?.invoke(true, null)
            } catch (e: Exception) {
                val msg = com.example.util.ErrorUtil.getErrorMessage(e)
                showMessage(msg, true)
                onResult?.invoke(false, msg)
            }
        }
    }

    fun removeMemberFromTeam(
        teamName: String,
        targetUsername: String,
        onResult: ((Boolean, String?) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                val team = mockTeams.values.find { it.name.equals(teamName, ignoreCase = true) }
                    ?: mockTeams[teamName]
                if (team == null) {
                    showMessage("Team not found", true)
                    onResult?.invoke(false, "Team not found")
                    return@launch
                }

                val currentUsername = getCurrentUser().username
                val currentRole = team.getMemberRole(currentUsername)

                // Permissions check
                if (!currentRole.canManageMembers) {
                    showMessage("Only Owners and Admins can remove members", true)
                    onResult?.invoke(false, "Insufficient permissions")
                    return@launch
                }

                val targetRole = team.getMemberRole(targetUsername)
                if (targetRole == TeamRole.OWNER) {
                    showMessage("Cannot remove the Team Owner", true)
                    onResult?.invoke(false, "Cannot remove Owner")
                    return@launch
                }

                if (currentRole == TeamRole.ADMIN && targetRole == TeamRole.ADMIN) {
                    showMessage("Admins cannot remove other Admins", true)
                    onResult?.invoke(false, "Only Owner can remove Admins")
                    return@launch
                }

                // Call backend update
                updateTeamMembers(teamName = team.name, removeUsernames = listOf(targetUsername))

                // Update local map & mockTeams
                teamMemberRoles[team.id]?.remove(targetUsername)
                val updatedMembers = team.members.filterNot { it.username.equals(targetUsername, ignoreCase = true) }
                mockTeams[team.id] = team.copy(
                    members = updatedMembers,
                    memberRoles = (teamMemberRoles[team.id] ?: emptyMap()).toMap()
                )
                _teamUpdateTrigger.value += 1
                showMessage("@$targetUsername removed from ${team.name}")
                onResult?.invoke(true, null)
            } catch (e: Exception) {
                val msg = com.example.util.ErrorUtil.getErrorMessage(e)
                showMessage(msg, true)
                onResult?.invoke(false, msg)
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
                if (l.isPersonal || l.id == _personalListId.value || listName.equals("Personal", ignoreCase = true) || listName.equals("Personal Tasks", ignoreCase = true) || listName.equals("My Tasks", ignoreCase = true)) {
                    showMessage("Personal list cannot be deleted", true)
                    return@launch
                }
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

    fun updatePersonalList(name: String, description: String? = null, onResult: ((Boolean, String?) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val cleanName = name.trim().ifBlank { null }
                val cleanDesc = description?.trim()
                val req = UpdatePersonalListDto(name = cleanName, description = cleanDesc)
                val res = VioraNetworkModule.api.updatePersonalList(req)
                if (res.success && res.data != null) {
                    val p = res.data
                    _personalListInfo.value = p
                    _personalListId.value = p.id
                    mockLists[p.id] = TaskList(
                        id = p.id,
                        name = p.name,
                        teamId = "personal_space",
                        isPersonal = true,
                        description = p.description,
                        isArchived = p.isArchived
                    )
                    fullTaskPool.forEachIndexed { idx, task ->
                        if (task.listId == p.id) {
                            fullTaskPool[idx] = task.copy(folder = p.name)
                        }
                    }
                    updateActiveAndArchivedStates()
                    showMessage("Personal list updated")
                    onResult?.invoke(true, null)
                } else {
                    showMessage(res.message, true)
                    onResult?.invoke(false, res.message)
                }
            } catch (e: Exception) {
                val msg = com.example.util.ErrorUtil.getErrorMessage(e)
                showMessage(msg, true)
                onResult?.invoke(false, msg)
            }
        }
    }

    fun updateList(listId: String, name: String, description: String? = null, onResult: ((Boolean, String?) -> Unit)? = null) {
        if (listId == _personalListId.value || mockLists[listId]?.isPersonal == true) {
            updatePersonalList(name, description, onResult)
            return
        }
        viewModelScope.launch {
            try {
                val cleanName = name.trim().ifBlank { null }
                val cleanDesc = description?.trim()
                val req = UpdateListDto(name = cleanName, description = cleanDesc)
                val res = VioraNetworkModule.api.updateList(listId, req)
                if (res.success && res.data != null) {
                    val updated = res.data.list
                    mockLists[updated.id] = TaskList(
                        id = updated.id,
                        name = updated.name,
                        teamId = updated.teamId ?: "personal_space",
                        isArchived = updated.isArchived,
                        description = updated.description
                    )
                    fullTaskPool.forEachIndexed { idx, task ->
                        if (task.listId == updated.id) {
                            fullTaskPool[idx] = task.copy(folder = updated.name)
                        }
                    }
                    updateActiveAndArchivedStates()
                    showMessage("List updated")
                    onResult?.invoke(true, null)
                } else {
                    showMessage(res.message, true)
                    onResult?.invoke(false, res.message)
                }
            } catch (e: Exception) {
                val msg = com.example.util.ErrorUtil.getErrorMessage(e)
                showMessage(msg, true)
                onResult?.invoke(false, msg)
            }
        }
    }

    fun updateActiveAndArchivedStates() {
        // Ensure personal team entry exists
        if (!mockTeams.containsKey("personal_space")) {
            mockTeams["personal_space"] = Team(
                id = "personal_space",
                name = "Personal Space",
                ownerId = "user1",
                isArchived = false,
                members = emptyList()
            )
        } else {
            val existing = mockTeams["personal_space"]
            if (existing != null && existing.name != "Personal Space") {
                mockTeams["personal_space"] = existing.copy(name = "Personal Space")
            }
        }

        // Remove synthetic unplanned_tasks list so it doesn't appear in Personal Space
        mockLists.remove("unplanned_tasks")

        // If a real personal list exists, remove synthetic personal_tasks to avoid duplicates
        if (_personalListId.value != null || mockLists.values.any { it.id != "personal_tasks" && (it.isPersonal || it.name == "My Tasks") }) {
            mockLists.remove("personal_tasks")
        }

        // Ensure default My Tasks list for Personal Space exists if none exists
        if (!mockLists.containsKey("personal_tasks") && mockLists.values.none { (it.teamId == "personal_space" || it.teamId.isEmpty()) && it.isPersonal && !it.isArchived }) {
            mockLists["personal_tasks"] = TaskList(
                id = "personal_tasks",
                name = "My Tasks",
                teamId = "personal_space",
                isPersonal = true,
                isArchived = false
            )
        }

        _lists.value = mockLists.values.toList()

        // Update active lists - strictly pin Personal List (My Tasks) at index 0 of personal space lists!
        // All lists without a team belong to Personal Space!
        val rawActive = _lists.value.filter { list ->
            !list.isArchived && list.id != "unplanned_tasks"
        }
        val nonPersonalTeamIds = mockTeams.values.filter { !it.isArchived && it.id != "personal_space" && it.name != "Personal" && it.name != "Personal Space" && it.name != "All Lists" }.map { it.id }.toSet()

        val personalPinned = rawActive.filter { it.isPersonal || it.id == _personalListId.value || it.name.equals("My Tasks", ignoreCase = true) }
            .distinctBy { "My Tasks" }
        val otherPersonal = rawActive.filter { 
            it !in personalPinned && !it.name.equals("My Tasks", ignoreCase = true) && (it.teamId.isBlank() || it.teamId == "personal_space" || it.teamId !in nonPersonalTeamIds)
        }
        val teamLists = rawActive.filter { it !in personalPinned && it !in otherPersonal }

        _activeLists.value = personalPinned + otherPersonal + teamLists

        // Update active teams based on mockTeams isArchived status
        val activeNames = mutableListOf<String>()
        if (_teams.value.contains("All Lists")) {
            activeNames.add("All Lists")
        }
        if (!activeNames.contains("Personal Space")) {
            activeNames.add("Personal Space")
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
                val l = mockLists[listId] ?: mockLists.values.find { it.id == listId }
                if (l?.isPersonal == true || listId == _personalListId.value) {
                    showMessage("Personal list cannot be archived", true)
                    return@launch
                }
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
    fun getCurrentUser(): com.example.model.User {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val myName = authPrefs.getString("user_name", _userName.value) ?: "User"
        val myUsername = authPrefs.getString("user_username", _userHandle.value) ?: "user"
        val myAvatar = com.example.util.ImageUtil.resolveAvatarUrl(authPrefs.getString("user_avatar_uri", _userAvatarUri.value))
        val myId = authPrefs.getString("user_id", "me") ?: "me"

        val currentUser = User(
            id = myId,
            name = myName,
            username = myUsername,
            defaultDeadlineDays = getUserDefaultDeadline(),
            avatarUrl = myAvatar
        )
        com.example.util.UserAvatarRegistry.register(currentUser)
        return currentUser
    }

    fun getAllKnownUsers(): List<com.example.model.User> {
        val usersMap = mutableMapOf<String, com.example.model.User>()
        val currentUser = getCurrentUser()
        usersMap[currentUser.username.lowercase()] = currentUser

        mockTeams.values.forEach { team ->
            team.members.forEach { m ->
                usersMap[m.username.lowercase()] = m
                com.example.util.UserAvatarRegistry.register(m)
            }
        }
        return usersMap.values.toList()
    }

    fun getTeamMembers(teamId: String?): List<String> {
        return getTeamMemberUsers(teamId).map { it.username }
    }

    fun getTeamMemberUsers(teamId: String?): List<com.example.model.User> {
        val currentUser = getCurrentUser()
        if (teamId == null || teamId.isBlank() || teamId == "personal_space") {
            return listOf(currentUser)
        }
        // Look up by team ID or team name
        val team = mockTeams[teamId] ?: mockTeams.values.find { 
            it.id.equals(teamId, ignoreCase = true) || it.name.equals(teamId, ignoreCase = true) 
        }
        if (team == null) {
            // Check if teamId might be a list ID or name
            val list = mockLists[teamId] ?: mockLists.values.find { 
                it.id.equals(teamId, ignoreCase = true) || it.name.equals(teamId, ignoreCase = true) 
            }
            if (list != null && list.teamId.isNotBlank() && list.teamId != "personal_space") {
                val teamFromList = mockTeams[list.teamId] ?: mockTeams.values.find { 
                    it.id.equals(list.teamId, ignoreCase = true) || it.name.equals(list.teamId, ignoreCase = true) 
                }
                if (teamFromList != null) {
                    val members = teamFromList.members.toMutableList()
                    members.forEach { com.example.util.UserAvatarRegistry.register(it) }
                    if (teamFromList.ownerId == currentUser.id || teamFromList.ownerId == currentUser.username || teamFromList.ownerId == "user1") {
                        if (members.none { it.id == currentUser.id || it.username.equals(currentUser.username, ignoreCase = true) }) {
                            members.add(0, currentUser)
                        }
                    }
                    return members
                }
            }
            return listOf(currentUser)
        }
        val members = team.members.toMutableList()
        members.forEach { com.example.util.UserAvatarRegistry.register(it) }
        if (team.ownerId == currentUser.id || team.ownerId == currentUser.username || team.ownerId == "user1") {
            if (members.none { it.id == currentUser.id || it.username.equals(currentUser.username, ignoreCase = true) }) {
                members.add(0, currentUser)
            }
        }
        return members
    }

    fun getTeamPendingInvitations(teamIdOrName: String?): List<String> {
        if (teamIdOrName == null) return emptyList()
        val team = mockTeams[teamIdOrName] ?: mockTeams.values.find {
            it.id.equals(teamIdOrName, ignoreCase = true) || it.name.equals(teamIdOrName, ignoreCase = true)
        }
        return team?.pendingInvitations ?: emptyList()
    }

    fun getTeamIdByName(teamName: String): String? {
        return mockTeams.values.find { it.name == teamName }?.id
    }

    fun getListIdByName(listName: String): String? {
        return mockLists.values.find { it.name == listName }?.id
    }

    fun getTeamNameById(teamId: String): String {
        if (teamId.isBlank() || teamId == "personal_space") return "Personal Space"
        return mockTeams[teamId]?.name ?: mockTeams.values.find { it.id == teamId || it.name == teamId }?.name ?: "Personal Space"
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
            loadState()
            delay(1000)
            _isRefreshing.value = false
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

    private val _quickTasksCount = MutableStateFlow(0)
    val quickTasksCount: StateFlow<Int> = _quickTasksCount.asStateFlow()

    // Full task pool of mock data (40 tasks)
    private val fullTaskPool = mutableListOf<Task>()
    private val idMapping = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val pendingTaskCreations = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    // Mock entities for testing fallback logic
    private val mockUsers = mutableMapOf<String, User>()
    private val mockTeams = mutableMapOf<String, Team>()
    private val teamMemberRoles = mutableMapOf<String, MutableMap<String, TeamRole>>()
    private val _teamUpdateTrigger = MutableStateFlow(0)
    val teamUpdateTrigger: StateFlow<Int> = _teamUpdateTrigger.asStateFlow()
    private val mockLists = mutableMapOf<String, TaskList>()
    private var weatherRefreshMinutes = 0

    init {
        try {
            loadFromLocalRoom()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            loadState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            loadTeamInvitations()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            updateActiveAndArchivedStates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        try {
            val taskPrefs = getApplication<Application>().getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
            if (taskPrefs.getBoolean("wizard_completed", false) || fullTaskPool.isNotEmpty()) {
                _wizardCompleted.value = true
                if (!taskPrefs.getBoolean("wizard_completed", false)) {
                    taskPrefs.edit().putBoolean("wizard_completed", true).apply()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Load weather settings and cached weather info
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            startClock()
            startWeatherTimer()
            loadUserInfo()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchLocationAndWeather()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadUserInfo() {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        val name = authPrefs.getString("user_name", "User") ?: "User"
        val username = authPrefs.getString("user_username", "user") ?: "user"
        val avatar = com.example.util.ImageUtil.resolveAvatarUrl(authPrefs.getString("user_avatar_uri", null))
        _userName.value = name
        _userHandle.value = username
        _userAvatarUri.value = avatar
        com.example.util.UserAvatarRegistry.setCurrentUser(name, username, avatar)
    }

    fun getUserName(): String {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        return authPrefs.getString("user_name", "User") ?: "User"
    }

    fun getUserAvatarUri(): String? {
        val authPrefs = getApplication<Application>().getSharedPreferences("viora_auth_prefs", Context.MODE_PRIVATE)
        return com.example.util.ImageUtil.resolveAvatarUrl(authPrefs.getString("user_avatar_uri", null))
    }

    private val dao by lazy {
        com.example.data.AppDatabase.getDatabase(getApplication()).vioraDao()
    }

    private fun loadFromLocalRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cachedTasks = dao.getAllTasksList()
                if (cachedTasks.isNotEmpty() && fullTaskPool.isEmpty()) {
                    val restored = cachedTasks.map { t ->
                        val localStatus = when (t.status) {
                            "in-progress" -> TaskStatus.IN_PROGRESS
                            "done" -> TaskStatus.DONE
                            else -> TaskStatus.TODO
                        }
                        var computedMillis: Long? = t.deadline
                        var dueDateTextVal = ""
                        if (computedMillis != null) {
                            val outFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                            dueDateTextVal = outFormat.format(java.util.Date(computedMillis))
                        }
                        Task(
                            id = t.id,
                            title = t.name,
                            client = t.teamId ?: "Personal",
                            listId = t.listId,
                            teamId = t.teamId,
                            status = localStatus,
                            description = t.description ?: "",
                            tags = if (t.tags.isNotEmpty()) t.tags.split(",") else emptyList(),
                            daysLeft = 0,
                            computedDeadlineMillis = computedMillis,
                            dueDateText = dueDateTextVal,
                            isArchived = t.isArchived,
                            folder = "Personal"
                        )
                    }
                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        if (fullTaskPool.isEmpty()) {
                            fullTaskPool.addAll(restored)
                            updateActiveAndArchivedStates()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

        // Also persist tasks to local Room DB asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entities = fullTaskPool.map { task ->
                    val statusStr = when (task.status) {
                        TaskStatus.TODO -> "todo"
                        TaskStatus.IN_PROGRESS -> "in-progress"
                        TaskStatus.DONE -> "done"
                    }
                    com.example.data.entity.TaskEntity(
                        id = task.id,
                        name = task.title,
                        description = task.description,
                        status = statusStr,
                        listId = task.listId,
                        teamId = task.teamId,
                        deadline = task.computedDeadlineMillis,
                        tags = task.tags.joinToString(","),
                        isArchived = task.isArchived,
                        isDeleted = false,
                        createdAt = task.createdAtMillis,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = com.example.data.entity.SyncStatus.SYNCED
                    )
                }
                dao.insertTasks(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
                val cleanUser = username.removePrefix("@").trim()
                val req = com.example.model.viora.UpdateTeamDto(addUsernames = listOf(cleanUser))
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
            _isDashboardLoading.value = false
            return
        }
        viewModelScope.launch {
            _isDashboardLoading.value = true
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
                        val membersList = mutableListOf<User>()
                        t.owner?.let { o ->
                            val ownerUser = User(
                                id = o.id,
                                name = o.fullName,
                                username = o.username,
                                defaultDeadlineDays = 0,
                                avatarUrl = com.example.util.ImageUtil.resolveAvatarUrl(o.avatar)
                            )
                            membersList.add(ownerUser)
                            com.example.util.UserAvatarRegistry.register(ownerUser)
                        }
                        t.members?.forEach { m ->
                            val memberUser = User(
                                id = m.id, 
                                name = m.fullName, 
                                username = m.username, 
                                defaultDeadlineDays = 0,
                                avatarUrl = com.example.util.ImageUtil.resolveAvatarUrl(m.avatar)
                            )
                            if (membersList.none { it.id == m.id || it.username == m.username }) {
                                membersList.add(memberUser)
                            }
                            com.example.util.UserAvatarRegistry.register(memberUser)
                        }

                        val currentRoles = teamMemberRoles.getOrPut(t.id) { mutableMapOf() }
                        val ownerUsername = t.owner?.username ?: membersList.firstOrNull { it.id == (t.ownerId ?: "") }?.username
                        if (ownerUsername != null) {
                            currentRoles[ownerUsername] = TeamRole.OWNER
                        }
                        membersList.forEach { m ->
                            if (!currentRoles.containsKey(m.username)) {
                                currentRoles[m.username] = if (m.id == t.ownerId || m.username == ownerUsername) TeamRole.OWNER else TeamRole.MEMBER
                            }
                        }

                        mockTeams[t.id] = Team(
                            id = t.id, 
                            name = t.name, 
                            ownerId = t.ownerId ?: (t.owner?.id ?: "user1"), 
                            isArchived = t.isArchived,
                            members = membersList,
                            memberRoles = currentRoles.toMap()
                        )
                    }
                    val activeTeamList = mutableListOf("Personal Space")
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
                        val isRealTeam = !l.teamId.isNullOrBlank() && l.teamId != "personal_space" && mockTeams.containsKey(l.teamId)
                        val matchingTeamByName = if (!isRealTeam && !l.teamName.isNullOrBlank() && l.teamName != "Unknown Team" && l.teamName != "Personal" && l.teamName != "Personal Space") {
                            mockTeams.values.find { it.name.equals(l.teamName, ignoreCase = true) }?.id
                        } else null

                        val resolvedTeamId = when {
                            isRealTeam -> l.teamId!!
                            matchingTeamByName != null -> matchingTeamByName
                            else -> "personal_space" // All lists without a team belong to Personal Space
                        }
                        mockLists[l.id] = TaskList(
                            id = l.id, 
                            name = if (l.name == "Personal" || l.name == "Unplanned Tasks") "My Tasks" else l.name, 
                            teamId = resolvedTeamId, 
                            isArchived = l.isArchived,
                            isPersonal = l.isPersonal,
                            description = l.description
                        )
                    }
                }

                // Fetch Dedicated Personal List from Swagger
                try {
                    val personalRes = VioraNetworkModule.api.getPersonalList()
                    if (personalRes.success && personalRes.data != null) {
                        val p = personalRes.data
                        _personalListInfo.value = p
                        _personalListId.value = p.id
                        val resolvedName = if (p.name.equals("Personal", ignoreCase = true) || p.name.isBlank()) "My Tasks" else p.name
                        mockLists[p.id] = TaskList(
                            id = p.id,
                            name = resolvedName,
                            teamId = "personal_space",
                            isArchived = p.isArchived,
                            isPersonal = true,
                            description = p.description
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Remove synthetic unplanned_tasks list so it doesn't appear in Personal Space
                mockLists.remove("unplanned_tasks")

                if (_personalListId.value != null || mockLists.values.any { it.id != "personal_tasks" && (it.isPersonal || it.name == "My Tasks") }) {
                    mockLists.remove("personal_tasks")
                }

                if (!mockLists.containsKey("personal_tasks") && mockLists.values.none { (it.teamId == "personal_space" || it.teamId.isEmpty()) && it.isPersonal && !it.isArchived }) {
                    mockLists["personal_tasks"] = TaskList(
                        id = "personal_tasks",
                        name = "My Tasks",
                        teamId = "personal_space",
                        isPersonal = true,
                        isArchived = false
                    )
                }
                _lists.value = mockLists.values.toList()

                // Fetch Tasks
                val tasksRes = VioraNetworkModule.api.getTasks()
                if (tasksRes.success && tasksRes.data != null) {
                    val prevAssigneeMap = fullTaskPool.associate { it.id to it.assigneePhotos }
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
                        
                        val cachedAssignees = prevAssigneeMap[t.id] ?: emptyList()
                        val taskItem = Task(
                            id = t.id,
                            title = t.name,
                            client = if (t.teamName.isNullOrBlank() || t.teamName == "Personal") "Personal Space" else t.teamName,
                            listId = t.listId,
                            teamId = t.teamId,
                            status = localStatus,
                            description = t.description ?: "",
                            tags = t.tagNames ?: emptyList(),
                            daysLeft = t.deadlineInfo?.remainingDays ?: 0,
                            computedDeadlineMillis = computedMillis,
                            dueDateText = dueDateTextVal,
                            isArchived = t.isArchived,
                            folder = when {
                                t.listName != null && t.listName != "Unplanned Tasks" && t.listName != "Personal" -> t.listName
                                else -> "My Tasks"
                            },
                            assigneePhotos = cachedAssignees,
                            assigneesCount = t.assigneesCount
                        )
                        fullTaskPool.add(taskItem)
                    }
                }
                updateActiveAndArchivedStates()
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage("Failed to load: ${com.example.util.ErrorUtil.getErrorMessage(e)}")
            } finally {
                _isDashboardLoading.value = false
            }
        }
    }

    fun fetchTaskDetails(taskId: String) {
        viewModelScope.launch {
            try {
                val realId = idMapping[taskId] ?: taskId
                val res = VioraNetworkModule.api.getTask(realId)
                if (res.success && res.data != null) {
                    val taskData = res.data
                    val usernames = mutableListOf<String>()
                    taskData.users?.forEach { u ->
                        val resolvedAvatar = com.example.util.ImageUtil.resolveAvatarUrl(u.avatar)
                        com.example.util.UserAvatarRegistry.register(u.id, u.username, resolvedAvatar, u.fullName)
                        usernames.add(u.username)
                    }
                    val index = fullTaskPool.indexOfFirst { it.id == realId || it.id == taskId }
                    if (index != -1) {
                        val existing = fullTaskPool[index]
                        fullTaskPool[index] = existing.copy(assigneePhotos = usernames)
                        loadCurrentTasks()
                        updateActiveAndArchivedStates()
                    }
                }
            } catch (_: Exception) {}
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
        val realId = idMapping[taskId] ?: taskId
        val statusStr = when(newStatus) {
            TaskStatus.TODO -> "todo"
            TaskStatus.IN_PROGRESS -> "in-progress"
            TaskStatus.DONE -> "done"
        }

        // 1. Immediately update in-memory pool for fast UI response
        val idx = fullTaskPool.indexOfFirst { it.id == realId || it.id == taskId }
        if (idx != -1) {
            fullTaskPool[idx] = fullTaskPool[idx].copy(status = newStatus)
            loadCurrentTasks()
            updateNextTask()
            updateCompletedCount()
            if (newStatus == TaskStatus.DONE) {
                VioraAnalytics.logTaskCompleted(realId)
                com.example.util.VioraInAppReviewManager.recordSignificantAction(getApplication())
            }
        }
        saveState()

        // 2. Immediately write to Room DB with PENDING_UPDATE status
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = dao.getTaskById(realId)
                if (existing != null) {
                    dao.insertTask(existing.copy(
                        status = statusStr,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = com.example.data.entity.SyncStatus.PENDING_UPDATE
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Sync to API in background and trigger SyncManager if needed
        viewModelScope.launch {
            try {
                val req = UpdateTaskDto(status = statusStr)
                val res = VioraNetworkModule.api.updateTask(realId, req)
                if (res.success) {
                    withContext(Dispatchers.IO) {
                        dao.updateTaskSyncStatus(realId, com.example.data.entity.SyncStatus.SYNCED)
                    }
                    updateCompletedCount()
                    updateActiveAndArchivedStates()
                } else {
                    com.example.sync.SyncManager.triggerImmediateSync(getApplication())
                }
            } catch(e: Exception) {
                com.example.sync.SyncManager.triggerImmediateSync(getApplication())
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
        updateQuickTasksCount()
    }

    private fun updateQuickTasksCount() {
        _quickTasksCount.value = fullTaskPool.count { task ->
            task.tags.any { it.equals("Quick Task", ignoreCase = true) } &&
            task.status != TaskStatus.DONE &&
            !task.isArchived
        }
    }

    fun createQuickTask(title: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return

        markWizardCompleted()
        viewModelScope.launch {
            val localId = java.util.UUID.randomUUID().toString()
            val personalId = _personalListId.value ?: "personal_tasks"
            val deadlineDays = getUserDefaultDeadline()
            val createdAt = System.currentTimeMillis()

            val deadlineDate = java.util.Calendar.getInstance()
            deadlineDate.timeInMillis = createdAt
            deadlineDate.add(java.util.Calendar.DAY_OF_YEAR, deadlineDays)
            deadlineDate.set(java.util.Calendar.HOUR_OF_DAY, 23)
            deadlineDate.set(java.util.Calendar.MINUTE, 59)
            deadlineDate.set(java.util.Calendar.SECOND, 59)
            deadlineDate.set(java.util.Calendar.MILLISECOND, 999)

            val newTask = Task(
                id = localId,
                title = cleanTitle,
                client = "Personal Space",
                folder = "My Tasks",
                listId = personalId,
                teamId = "personal_space",
                status = TaskStatus.TODO,
                dueDateText = "$deadlineDays days",
                computedDeadlineMillis = deadlineDate.timeInMillis,
                tags = listOf("Quick Task"),
                createdAtMillis = createdAt,
                userId = "user1"
            )

            // 1. Instantly update in-memory pool
            fullTaskPool.add(0, newTask)
            loadCurrentTasks()
            updateNextTask()
            updateCompletedCount()
            saveState()

            // 2. Persist to local Room DB
            withContext(Dispatchers.IO) {
                try {
                    val entity = com.example.data.entity.TaskEntity(
                        id = newTask.id,
                        name = newTask.title,
                        description = "",
                        status = "todo",
                        listId = newTask.listId,
                        teamId = newTask.teamId,
                        deadline = newTask.computedDeadlineMillis,
                        tags = "Quick Task",
                        isArchived = false,
                        isDeleted = false,
                        createdAt = newTask.createdAtMillis,
                        updatedAt = System.currentTimeMillis(),
                        syncStatus = com.example.data.entity.SyncStatus.PENDING_CREATE
                    )
                    dao.insertTask(entity)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 3. Network Sync
            try {
                pendingTaskCreations.add(localId)
                val req = com.example.model.viora.CreateTaskDto(
                    name = cleanTitle,
                    listId = personalId,
                    deadlineDays = deadlineDays,
                    tags = listOf("Quick Task")
                )
                val res = VioraNetworkModule.api.createTask(req)
                if (res.success && res.data != null) {
                    val serverId = res.data.task.id
                    idMapping[localId] = serverId
                    val idx = fullTaskPool.indexOfFirst { it.id == localId }
                    if (idx != -1) {
                        fullTaskPool[idx] = fullTaskPool[idx].copy(id = serverId)
                    }
                    saveState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingTaskCreations.remove(localId)
            }
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
                val prevAssignees = fullTaskPool.find { it.id == realId }?.assigneePhotos ?: emptyList()

                // If existing task was customized or edited, remove "Quick Task" tag so it leaves the triage queue
                val cleanedTags = if (isExisting && task.tags.any { it.equals("Quick Task", ignoreCase = true) }) {
                    task.tags.filterNot { it.equals("Quick Task", ignoreCase = true) }
                } else {
                    task.tags
                }

                val taskToSave = task.copy(id = realId, tags = cleanedTags)

                // 1. Instantly update in-memory pool
                val existingIdx = fullTaskPool.indexOfFirst { it.id == realId }
                if (existingIdx != -1) {
                    fullTaskPool[existingIdx] = taskToSave
                } else {
                    fullTaskPool.add(0, taskToSave)
                }
                loadCurrentTasks()
                updateNextTask()
                updateCompletedCount()
                saveState()

                // 2. Instantly persist to local Room DB
                val statusStr = when(taskToSave.status) {
                    TaskStatus.TODO -> "todo"
                    TaskStatus.IN_PROGRESS -> "in-progress"
                    TaskStatus.DONE -> "done"
                }
                withContext(Dispatchers.IO) {
                    try {
                        val entity = com.example.data.entity.TaskEntity(
                            id = taskToSave.id,
                            name = taskToSave.title,
                            description = taskToSave.description,
                            status = statusStr,
                            listId = taskToSave.listId,
                            teamId = taskToSave.teamId,
                            deadline = taskToSave.computedDeadlineMillis,
                            tags = taskToSave.tags.joinToString(","),
                            isArchived = taskToSave.isArchived,
                            isDeleted = false,
                            createdAt = taskToSave.createdAtMillis,
                            updatedAt = System.currentTimeMillis(),
                            syncStatus = if (isExisting) com.example.data.entity.SyncStatus.PENDING_UPDATE else com.example.data.entity.SyncStatus.PENDING_CREATE
                        )
                        dao.insertTask(entity)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                var listId = mockLists.values.find { it.name == taskToSave.folder || it.id == taskToSave.listId }?.id
                if (listId == "unplanned_tasks" || listId == "personal_tasks" || listId == null) {
                    val actualPersonalId = _personalListId.value ?: mockLists.values.find { it.isPersonal }?.id
                    if (actualPersonalId != null && actualPersonalId != "personal_tasks") {
                        listId = actualPersonalId
                    } else {
                        var team = mockTeams.values.find { (it.name == "Personal" || it.name == "Personal Space") && it.id != "personal_space" }
                        if (team == null) {
                            val tReq = CreateTeamDto(name = "Personal Space", deadlineDays = 5)
                            val tRes = VioraNetworkModule.api.createTeam(tReq)
                            if (tRes.success && tRes.data != null) {
                                team = Team(id = tRes.data.id, name = tRes.data.name, ownerId = tRes.data.ownerId ?: "user1", isArchived = false)
                                mockTeams[tRes.data.id] = team
                            }
                        }
                        
                        val listName = if (taskToSave.folder.isNotEmpty() && taskToSave.folder != "Unplanned Tasks" && taskToSave.folder != "My Tasks" && taskToSave.folder != "unplanned_tasks" && taskToSave.folder != "personal_tasks") taskToSave.folder else "My Tasks"
                        var list = mockLists.values.find { it.name == listName && it.teamId == team?.id && it.id != "unplanned_tasks" && it.id != "personal_tasks" }
                        if (list == null && team != null) {
                            val lReq = CreateListDto(name = listName, teamId = team.id, deadlineDays = 5)
                            val lRes = VioraNetworkModule.api.createList(lReq)
                            if (lRes.success && lRes.data != null) {
                                list = TaskList(id = lRes.data.list.id, name = lRes.data.list.name, teamId = team.id, isArchived = false, isPersonal = true)
                                mockLists[lRes.data.list.id] = list
                            }
                        }
                        listId = list?.id ?: actualPersonalId
                    }
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

                val deadlineDaysForApi = calculatedDeadlineDays?.let { if (it < 1) 1 else it }
                val isUuid = listId?.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) == true
                val validListId = if (isUuid) listId else null
                val cleanAssignees = taskToSave.assigneePhotos.map { it.removePrefix("@").trim() }.filter { it.isNotBlank() }

                if (!isExisting) {
                    pendingTaskCreations.add(task.id)
                    try {
                        val req = CreateTaskDto(
                            name = taskToSave.title,
                            description = taskToSave.description.ifEmpty { null },
                            deadlineDays = deadlineDaysForApi,
                            listId = validListId,
                            tags = taskToSave.tags.ifEmpty { null },
                            status = statusStr,
                            usernames = cleanAssignees.ifEmpty { null }
                        )
                        val res = VioraNetworkModule.api.createTask(req)
                        if (res.success && res.data != null) {
                            res.data.task.users?.forEach { u ->
                                val resolvedAvatar = com.example.util.ImageUtil.resolveAvatarUrl(u.avatar)
                                com.example.util.UserAvatarRegistry.register(u.id, u.username, resolvedAvatar, u.fullName)
                            }
                            val serverId = res.data.task.id
                            val serverUsers = res.data.task.users?.map { it.username } ?: cleanAssignees
                            idMapping[task.id] = serverId
                            idMapping[serverId] = serverId
                            val existingIndex = fullTaskPool.indexOfFirst { it.id == serverId || it.id == task.id }
                            if (existingIndex != -1) {
                                fullTaskPool[existingIndex] = fullTaskPool[existingIndex].copy(id = serverId, assigneePhotos = serverUsers)
                            }
                            withContext(Dispatchers.IO) {
                                dao.hardDeleteTask(task.id)
                                dao.insertTask(com.example.data.entity.TaskEntity(
                                    id = serverId,
                                    name = taskToSave.title,
                                    description = taskToSave.description,
                                    status = statusStr,
                                    listId = validListId,
                                    teamId = taskToSave.teamId,
                                    deadline = taskToSave.computedDeadlineMillis,
                                    tags = taskToSave.tags.joinToString(","),
                                    isArchived = taskToSave.isArchived,
                                    isDeleted = false,
                                    createdAt = taskToSave.createdAtMillis,
                                    updatedAt = System.currentTimeMillis(),
                                    syncStatus = com.example.data.entity.SyncStatus.SYNCED
                                ))
                            }
                            showMessage("Task added")
                            VioraAnalytics.logTaskCreated(
                                taskId = serverId,
                                hasDueDate = taskToSave.computedDeadlineMillis != null,
                                hasAssignee = serverUsers.isNotEmpty(),
                                category = taskToSave.folder
                            )
                            loadCurrentTasks()
                            loadState()
                        } else {
                            com.example.sync.SyncManager.triggerImmediateSync(getApplication())
                        }
                    } catch (e: Exception) {
                        com.example.sync.SyncManager.triggerImmediateSync(getApplication())
                    } finally {
                        pendingTaskCreations.remove(task.id)
                    }
                    return@launch
                }

                // Existing task update
                val cleanCurrent = taskToSave.assigneePhotos.map { it.removePrefix("@").trim() }.filter { it.isNotBlank() }
                val cleanPrev = prevAssignees.map { it.removePrefix("@").trim() }.filter { it.isNotBlank() }
                val usernamesToAdd = cleanCurrent.filter { it !in cleanPrev }.ifEmpty { null }
                val usernamesToRemove = cleanPrev.filter { it !in cleanCurrent }.ifEmpty { null }

                val req = UpdateTaskDto(
                    name = taskToSave.title,
                    description = taskToSave.description,
                    deadlineDays = deadlineDaysForApi,
                    status = statusStr,
                    listId = validListId,
                    tagsToAdd = taskToSave.tags.ifEmpty { null },
                    usernamesToAdd = usernamesToAdd,
                    usernamesToRemove = usernamesToRemove
                )
                val res = VioraNetworkModule.api.updateTask(taskToSave.id, req)
                if (res.success) {
                    val serverUsers = res.data?.task?.users?.map { it.username } ?: cleanCurrent
                    res.data?.task?.users?.forEach { u ->
                        val resolvedAvatar = com.example.util.ImageUtil.resolveAvatarUrl(u.avatar)
                        com.example.util.UserAvatarRegistry.register(u.id, u.username, resolvedAvatar, u.fullName)
                    }
                    val existingIndex = fullTaskPool.indexOfFirst { it.id == taskToSave.id }
                    if (existingIndex != -1) {
                        fullTaskPool[existingIndex] = fullTaskPool[existingIndex].copy(assigneePhotos = serverUsers)
                    }
                    loadCurrentTasks()
                    withContext(Dispatchers.IO) {
                        dao.updateTaskSyncStatus(taskToSave.id, com.example.data.entity.SyncStatus.SYNCED)
                    }
                    showMessage("Task updated")
                    updateCompletedCount()
                    updateActiveAndArchivedStates()
                } else {
                    com.example.sync.SyncManager.triggerImmediateSync(getApplication())
                }
            } catch(e: Exception) {
                e.printStackTrace()
                com.example.sync.SyncManager.triggerImmediateSync(getApplication())
            }
        }
    }

    fun deleteTask(taskId: String) {
        val realId = idMapping[taskId] ?: taskId
        // Remove locally first
        fullTaskPool.removeAll { it.id == realId || it.id == taskId }
        loadCurrentTasks()
        updateNextTask()
        updateCompletedCount()
        saveState()

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dao.softDeleteTask(realId)
                }
                val res = VioraNetworkModule.api.deleteTask(realId)
                if (res.success) {
                    withContext(Dispatchers.IO) {
                        dao.hardDeleteTask(realId)
                    }
                    showMessage("Task deleted")
                    VioraAnalytics.logTaskDeleted(realId)
                    idMapping.remove(taskId)
                    idMapping.remove(realId)
                    loadState()
                } else {
                    com.example.sync.SyncManager.triggerImmediateSync(getApplication())
                }
            } catch(e: Exception) {
                com.example.sync.SyncManager.triggerImmediateSync(getApplication())
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

    private fun fetchUrlWithRedirects(urlString: String, timeoutMs: Int = 5000): String {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(timeoutMs.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMs.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
        val request = okhttp3.Request.Builder()
            .url(urlString)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .header("Accept", "application/json, text/plain, */*")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw java.io.IOException("HTTP error ${response.code}: ${response.message}")
            }
            return response.body?.string() ?: throw java.io.IOException("Empty response body")
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
            var locationResolved = false

            // Try 1: ipinfo.io
            if (!locationResolved) {
                try {
                    val response = fetchUrlWithRedirects("https://ipinfo.io/json")
                    val json = org.json.JSONObject(response)
                    val foundCity = json.optString("city", "").trim()
                    if (foundCity.isNotBlank()) {
                        city = foundCity
                        val loc = json.optString("loc", "")
                        val parts = loc.split(",")
                        if (parts.size == 2) {
                            parts[0].toDoubleOrNull()?.let { lat = it }
                            parts[1].toDoubleOrNull()?.let { lon = it }
                        }
                        locationResolved = true
                        android.util.Log.d("VioraTaskViewModel", "ipinfo city found: $city ($lat, $lon)")
                    }
                } catch (e: Exception) {
                    android.util.Log.d("VioraTaskViewModel", "ipinfo fallback: ${e.message ?: e.javaClass.simpleName}")
                }
            }

            // Try 2: freeipapi.com (handles 307 redirects)
            if (!locationResolved) {
                try {
                    val response = fetchUrlWithRedirects("https://freeipapi.com/api/json")
                    val json = org.json.JSONObject(response)
                    val foundCity = json.optString("cityName", "").trim()
                    if (foundCity.isNotBlank()) {
                        city = foundCity
                        lat = json.optDouble("latitude", lat)
                        lon = json.optDouble("longitude", lon)
                        locationResolved = true
                        android.util.Log.d("VioraTaskViewModel", "freeipapi city found: $city ($lat, $lon)")
                    }
                } catch (e: Exception) {
                    android.util.Log.d("VioraTaskViewModel", "freeipapi fallback: ${e.message ?: e.javaClass.simpleName}")
                }
            }

            // Try 3: ipapi.co
            if (!locationResolved) {
                try {
                    val response = fetchUrlWithRedirects("https://ipapi.co/json/")
                    val json = org.json.JSONObject(response)
                    val foundCity = json.optString("city", "").trim()
                    if (foundCity.isNotBlank()) {
                        city = foundCity
                        lat = json.optDouble("latitude", lat)
                        lon = json.optDouble("longitude", lon)
                        locationResolved = true
                        android.util.Log.d("VioraTaskViewModel", "ipapi city found: $city ($lat, $lon)")
                    }
                } catch (e: Exception) {
                    android.util.Log.d("VioraTaskViewModel", "ipapi fallback: ${e.message ?: e.javaClass.simpleName}")
                }
            }

            // Fallback: If network is offline or all fail, read from saved cache
            if (!locationResolved) {
                val weatherPrefs = getApplication<Application>().getSharedPreferences("viora_weather_prefs", Context.MODE_PRIVATE)
                city = weatherPrefs.getString("cached_weather_location", "Tehran") ?: "Tehran"
                lat = weatherPrefs.getFloat("weather_manual_lat", 35.6944f).toDouble()
                lon = weatherPrefs.getFloat("weather_manual_lon", 51.4215f).toDouble()
            }
        }

        // 2. Fetch current weather and max daily UV index
        val weatherPrefs = getApplication<Application>().getSharedPreferences("viora_weather_prefs", Context.MODE_PRIVATE)
        var tempStr = weatherPrefs.getString("cached_weather_temp", "9°") ?: "9°"
        var conditionStr = weatherPrefs.getString("cached_weather_condition", "Sunny") ?: "Sunny"
        var uvIndexStr = weatherPrefs.getString("cached_weather_uv", "Low") ?: "Low"
        try {
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true&daily=uv_index_max&timezone=auto"
            val response = fetchUrlWithRedirects(weatherUrl, timeoutMs = 6000)
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
            android.util.Log.d("VioraTaskViewModel", "Weather fetch fallback used: ${e.message ?: e.javaClass.simpleName}")
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

    fun updateSubtaskStatus(subtaskId: String, status: String, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val req = com.example.model.viora.UpdateSubtaskStatusDto(status = status)
                val res = VioraNetworkModule.api.updateSubtaskStatus(subtaskId, req)
                onResult?.invoke(res.success)
            } catch (e: Exception) {
                onResult?.invoke(false)
            }
        }
    }
}
