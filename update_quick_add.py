with open("app/src/main/java/com/example/QuickAddActivity.kt", "r") as f:
    content = f.read()

# Replace the view model instantiation
target_vm = "private val viewModel: VioraTaskViewModel by viewModels()"
content = content.replace(target_vm, "")

# Import Moshi and Types if needed
if "import com.squareup.moshi.Moshi" not in content:
    content = content.replace("import com.example.model.Task", "import com.example.model.Task\nimport com.squareup.moshi.Moshi\nimport com.squareup.moshi.Types\nimport com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory\nimport android.content.Context")

# Replace saveTask
target_save = """    private fun saveTask(title: String, onSaved: () -> Unit) {
        val newTask = Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            client = "Viora design",
            userId = "user1",
            folder = "Unplanned Tasks",
            daysLeft = 7,
            status = TaskStatus.TODO
        )
        viewModel.upsertTask(newTask)
        onSaved()
    }"""

rep_save = """    private fun saveTask(title: String, onSaved: () -> Unit) {
        val newTask = Task(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            client = "Viora design",
            userId = "user1",
            folder = "Unplanned Tasks",
            daysLeft = 7,
            status = TaskStatus.TODO
        )
        
        Thread {
            try {
                val prefs = getSharedPreferences("viora_task_prefs", Context.MODE_PRIVATE)
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val tasksAdapter = moshi.adapter<List<Task>>(Types.newParameterizedType(List::class.java, Task::class.java))
                
                val tasksJson = prefs.getString("tasks", null)
                val currentTasks = if (tasksJson != null) {
                    tasksAdapter.fromJson(tasksJson)?.toMutableList() ?: mutableListOf()
                } else {
                    mutableListOf()
                }
                
                currentTasks.add(0, newTask) // Add to top
                
                prefs.edit().putString("tasks", tasksAdapter.toJson(currentTasks)).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runOnUiThread {
                onSaved()
            }
        }.start()
    }"""

content = content.replace(target_save, rep_save)
with open("app/src/main/java/com/example/QuickAddActivity.kt", "w") as f:
    f.write(content)
