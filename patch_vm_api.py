import re

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """import com.example.network.viora.VioraNetworkModule
import com.example.model.viora.*
"""
content = content.replace('import androidx.lifecycle.viewModelScope', imports + 'import androidx.lifecycle.viewModelScope', 1)

# loadState
load_state_replacement = """
    fun loadState() {
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
                    // avatar can be set if needed
                }
                
                // Fetch Teams
                val teamsRes = VioraNetworkModule.api.getTeams(perPage = 100)
                if (teamsRes.success && teamsRes.data != null) {
                    mockTeams.clear()
                    teamsRes.data.items.forEach { t ->
                        mockTeams[t.id] = Team(id = t.id, name = t.name, ownerId = t.ownerId, isArchived = t.isArchived)
                    }
                    _teams.value = mockTeams.values.filter { !it.isArchived }.map { it.name }.toList()
                    _archivedTeams.value = mockTeams.values.filter { it.isArchived }.toList()
                }

                // Fetch Lists
                val listsRes = VioraNetworkModule.api.getLists()
                if (listsRes.success && listsRes.data != null) {
                    mockLists.clear()
                    listsRes.data.forEach { l ->
                        mockLists[l.id] = TaskList(id = l.id, name = l.name, teamId = l.teamId ?: "", isArchived = l.isArchived)
                    }
                    _lists.value = mockLists.values.filter { !it.isArchived }.toList()
                    _archivedLists.value = mockLists.values.filter { it.isArchived }.toList()
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
                        
                        fullTaskPool.add(Task(
                            id = t.id,
                            title = t.name,
                            client = t.teamName ?: "Personal",
                            listId = t.listId,
                            teamId = t.teamId,
                            status = localStatus,
                            description = t.description ?: "",
                            tags = t.tagNames ?: emptyList(),
                            daysLeft = t.deadlineInfo.remainingDays,
                            isArchived = t.isArchived,
                            folder = t.listName ?: "Unplanned Tasks"
                        ))
                    }
                    _archivedTasks.value = fullTaskPool.filter { it.isArchived }
                    loadCurrentTasks()
                    updateNextTask()
                    updateUnplannedCount()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage("Failed to load from server")
            }
        }
    }
"""

content = re.sub(r'fun loadState\(\) \{.*?(?=private fun computeTaskDeadline)', load_state_replacement, content, flags=re.DOTALL)


# addTeam
add_team_rep = """
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
                showMessage("Error creating team", true)
            }
        }
    }
"""
content = re.sub(r'fun addTeam\(team: String, defaultDeadlineDays: Int\? = null\) \{.*?(?=fun updateTeam)', add_team_rep, content, flags=re.DOTALL)

# updateTeam
update_team_rep = """
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
                showMessage("Error updating team", true)
            }
        }
    }
"""
content = re.sub(r'fun updateTeam\(oldTeamName: String, newTeamName: String, defaultDeadlineDays: Int\?\) \{.*?(?=fun deleteTeam)', update_team_rep, content, flags=re.DOTALL)

# deleteTeam
delete_team_rep = """
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
                showMessage("Error deleting team", true)
            }
        }
    }
"""
content = re.sub(r'fun deleteTeam\(teamName: String\) \{.*?(?=fun addListToTeam)', delete_team_rep, content, flags=re.DOTALL)

# addListToTeam
add_list_rep = """
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
                showMessage("Error creating list", true)
            }
        }
    }
"""
content = re.sub(r'fun addListToTeam\(teamName: String, listName: String, defaultDeadlineDays: Int\? = null\) \{.*?(?=fun deleteList)', add_list_rep, content, flags=re.DOTALL)

# deleteList
delete_list_rep = """
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
                showMessage("Error deleting list", true)
            }
        }
    }
"""
content = re.sub(r'fun deleteList\(listName: String\) \{.*?(?=fun updateActiveAndArchivedStates)', delete_list_rep, content, flags=re.DOTALL)

# addTask
add_task_rep = """
    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                val listId = mockLists.values.find { it.name == task.folder || it.id == task.listId }?.id
                val req = CreateTaskDto(
                    name = task.title,
                    description = task.description,
                    listId = listId,
                    tags = task.tags,
                    status = "todo"
                )
                val res = VioraNetworkModule.api.createTask(req)
                if (res.success) {
                    showMessage("Task added")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage("Error adding task", true)
            }
        }
    }
"""
content = re.sub(r'fun addTask\(task: Task\) \{.*?(?=private fun updateUnplannedCount)', add_task_rep, content, flags=re.DOTALL)

# upsertTask
upsert_task_rep = """
    fun upsertTask(task: Task) {
        viewModelScope.launch {
            try {
                val listId = mockLists.values.find { it.name == task.folder || it.id == task.listId }?.id
                val statusStr = when(task.status) {
                    TaskStatus.TODO -> "todo"
                    TaskStatus.IN_PROGRESS -> "in-progress"
                    TaskStatus.DONE -> "done"
                }
                
                // If ID starts with something random we created locally, but wait we only edit existing ones with their API ID
                val isNew = task.id.length < 15 && !task.id.contains("-")
                if (isNew) {
                    addTask(task)
                    return@launch
                }

                val req = UpdateTaskDto(
                    name = task.title,
                    description = task.description,
                    status = statusStr,
                    listId = listId,
                    tagsToAdd = task.tags
                )
                val res = VioraNetworkModule.api.updateTask(task.id, req)
                if (res.success) {
                    showMessage("Task updated")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage("Error updating task", true)
            }
        }
    }
"""
content = re.sub(r'fun upsertTask\(task: Task\) \{.*?(?=fun deleteTask)', upsert_task_rep, content, flags=re.DOTALL)

# deleteTask
delete_task_rep = """
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                val res = VioraNetworkModule.api.deleteTask(taskId)
                if (res.success) {
                    showMessage("Task deleted")
                    loadState()
                } else {
                    showMessage(res.message, true)
                }
            } catch(e: Exception) {
                showMessage("Error deleting task", true)
            }
        }
    }
"""
content = re.sub(r'fun deleteTask\(taskId: String\) \{.*?(?=fun selectTab)', delete_task_rep, content, flags=re.DOTALL)


# updateTaskStatus
update_task_status_rep = """
    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                val statusStr = when(newStatus) {
                    TaskStatus.TODO -> "todo"
                    TaskStatus.IN_PROGRESS -> "in-progress"
                    TaskStatus.DONE -> "done"
                }
                val req = UpdateTaskDto(status = statusStr)
                val res = VioraNetworkModule.api.updateTask(taskId, req)
                if (res.success) {
                    // Update local pool immediately for UI speed
                    val idx = fullTaskPool.indexOfFirst { it.id == taskId }
                    if (idx != -1) {
                        fullTaskPool[idx] = fullTaskPool[idx].copy(status = newStatus)
                        loadCurrentTasks()
                        updateNextTask()
                        updateUnplannedCount()
                    }
                    // Sync fully
                    loadState()
                }
            } catch(e: Exception) {
                showMessage("Failed to update status", true)
            }
        }
    }
"""
content = re.sub(r'fun updateTaskStatus\(taskId: String, newStatus: TaskStatus\) \{.*?(?=fun addTask)', update_task_status_rep, content, flags=re.DOTALL)


with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
