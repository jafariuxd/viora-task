import re

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

# archiveTeam
archive_team_rep = """
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
                showMessage("Error archiving team", true)
            }
        }
    }
"""
content = re.sub(r'fun archiveTeam\(teamName: String\) \{.*?(?=fun restoreTeam)', archive_team_rep, content, flags=re.DOTALL)

# restoreTeam
restore_team_rep = """
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
                showMessage("Error restoring team", true)
            }
        }
    }
"""
content = re.sub(r'fun restoreTeam\(teamId: String\) \{.*?(?=fun archiveList)', restore_team_rep, content, flags=re.DOTALL)

# archiveList
archive_list_rep = """
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
                showMessage("Error archiving list", true)
            }
        }
    }
"""
content = re.sub(r'fun archiveList\(listId: String\) \{.*?(?=fun restoreList)', archive_list_rep, content, flags=re.DOTALL)

# restoreList
restore_list_rep = """
    fun restoreList(listId: String) {
        // No unarchive endpoint available for lists based on JSON
        showMessage("Restoring lists is not supported currently.", true)
    }
"""
content = re.sub(r'fun restoreList\(listId: String\) \{.*?(?=fun archiveTask)', restore_list_rep, content, flags=re.DOTALL)

# archiveTask
archive_task_rep = """
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
                showMessage("Error archiving task", true)
            }
        }
    }
"""
content = re.sub(r'fun archiveTask\(taskId: String\) \{.*?(?=fun restoreTask)', archive_task_rep, content, flags=re.DOTALL)

# restoreTask
restore_task_rep = """
    fun restoreTask(taskId: String) {
        // No unarchive endpoint available for tasks based on JSON
        showMessage("Restoring tasks is not supported currently.", true)
    }
"""
content = re.sub(r'fun restoreTask\(taskId: String\) \{.*?(?=fun getTeamNameById)', restore_task_rep, content, flags=re.DOTALL)


with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
