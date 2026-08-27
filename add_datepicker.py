with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "r") as f:
    content = f.read()

# Add imports
if "import androidx.compose.material3.DatePicker" not in content:
    content = content.replace("import androidx.compose.material3.Text", "import androidx.compose.material3.Text\nimport androidx.compose.material3.DatePicker\nimport androidx.compose.material3.DatePickerDialog\nimport androidx.compose.material3.rememberDatePickerState\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.DatePickerDefaults\nimport androidx.compose.material3.TextButton")

# Add state variable
target_state = "var showTagInput by remember { mutableStateOf(false) }"
rep_state = "var showTagInput by remember { mutableStateOf(false) }\n    var showDatePicker by remember { mutableStateOf(false) }"
content = content.replace(target_state, rep_state)

# Replace the DatePickerDialog call
target_click = """                            val dialog = android.app.DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                                    val formattedDate = dateFormat.format(selectedCalendar.time)
                                    updateTask(currentTask.copy(selectedDeadlineMillis = selectedCalendar.timeInMillis, dueDateText = formattedDate))
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            dialog.show()"""

rep_click = """                            showDatePicker = true"""
content = content.replace(target_click, rep_click)

# Add the DatePicker UI at the end, right before the Tag Input Modal
target_modal = """        // Tag Input Modal
        if (showTagInput) {"""

rep_modal = """        // DatePicker Modal
        @OptIn(ExperimentalMaterial3Api::class)
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = currentTask.selectedDeadlineMillis ?: System.currentTimeMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDatePicker = false
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedCalendar = Calendar.getInstance().apply { timeInMillis = millis }
                                val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
                                val formattedDate = dateFormat.format(selectedCalendar.time)
                                updateTask(currentTask.copy(selectedDeadlineMillis = millis, dueDateText = formattedDate))
                            }
                        }
                    ) {
                        Text("OK", color = VioraNeonLime)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1E1E1E)
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = VioraNeonLime,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.Gray,
                        subheadContentColor = Color.Gray,
                        navigationContentColor = Color.White,
                        yearContentColor = Color.White,
                        currentYearContentColor = VioraNeonLime,
                        selectedYearContentColor = Color.Black,
                        selectedYearContainerColor = VioraNeonLime,
                        dayContentColor = Color.White,
                        disabledDayContentColor = Color.DarkGray,
                        selectedDayContentColor = Color.Black,
                        selectedDayContainerColor = VioraNeonLime,
                        todayContentColor = VioraNeonLime,
                        todayDateBorderColor = VioraNeonLime
                    )
                )
            }
        }

        // Tag Input Modal
        if (showTagInput) {"""

content = content.replace(target_modal, rep_modal)

with open("app/src/main/java/com/example/ui/screens/TaskDetailScreen.kt", "w") as f:
    f.write(content)

