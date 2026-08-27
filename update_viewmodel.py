import re

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    content = f.read()

# 1. Update setupMockHierarchy
old_mock = """    private fun setupMockHierarchy() {
        val myUser = User(id = "user1", name = "Mehran", username = "mehran", defaultDeadlineDays = 5)
        val myTeam = Team(id = "team1", name = "My Team", ownerId = "user1", defaultDeadlineDays = 3)
        
        mockUsers[myUser.id] = myUser
        mockTeams[myTeam.id] = myTeam
        
        val folderNames = listOf("Charchoob", "GymShow", "Hub", "Mobile app", "Romak", "Users dashboard", "My Tasks")
        folderNames.forEachIndexed { index, name ->
            val days = (index % 3) + 2 // 2, 3, or 4 days as default deadline
            mockLists[name] = TaskList(id = name, name = name, teamId = "team1", defaultDeadlineDays = days)
        }
    }"""

new_mock = """    private fun setupMockHierarchy() {
        val myUser = User(id = "user1", name = "Mehran", username = "mehran", defaultDeadlineDays = 5)
        val myTeam = Team(id = "team1", name = "My Team", ownerId = "user1", defaultDeadlineDays = 3)
        val personalTeam = Team(id = "team_personal", name = "Personal Space", ownerId = "user1", defaultDeadlineDays = 3)
        
        mockUsers[myUser.id] = myUser
        mockTeams[myTeam.id] = myTeam
        mockTeams[personalTeam.id] = personalTeam
        
        val folderNames = listOf("Charchoob", "GymShow", "Hub", "Mobile app", "Romak", "Users dashboard", "Unplanned Tasks")
        folderNames.forEachIndexed { index, name ->
            val days = (index % 3) + 2 // 2, 3, or 4 days as default deadline
            val tId = if (name == "Unplanned Tasks") "team_personal" else "team1"
            mockLists[name] = TaskList(id = name, name = name, teamId = tId, defaultDeadlineDays = days)
        }
    }"""

content = content.replace(old_mock, new_mock)

# 2. Update task generation logic
content = content.replace('val list = mockLists.values.random()', 'val list = mockLists.values.filter { it.name != "Unplanned Tasks" }.random()')

# Actually wait, let's just create some unplanned tasks
# I'll just change `refreshTasks` to explicitly calculate unplannedCount

old_unplanned = 'private val _unplannedCount = MutableStateFlow(14)'
new_unplanned = 'private val _unplannedCount = MutableStateFlow(0)'
content = content.replace(old_unplanned, new_unplanned)

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
    f.write(content)
