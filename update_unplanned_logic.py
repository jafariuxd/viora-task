import re

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    content = f.read()

# 1. Add updateUnplannedCount method
update_unplanned = """
    private fun updateUnplannedCount() {
        _unplannedCount.value = fullTaskPool.count { it.folder == "Unplanned Tasks" }
    }
"""
content = content.replace("    fun upsertTask", update_unplanned + "    fun upsertTask")

# 2. Call it in upsertTask and deleteTask
content = content.replace("updateNextTask()", "updateNextTask()\n        updateUnplannedCount()")

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
    f.write(content)
