import re

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    content = f.read()

# Replace if (t.deadlineInfo.actualDeadline.isNotEmpty()) {
content = content.replace("if (t.deadlineInfo.actualDeadline.isNotEmpty()) {", "if (t.deadlineInfo?.actualDeadline?.isNotEmpty() == true) {")

# Replace val date = sdf.parse(t.deadlineInfo.actualDeadline)
content = content.replace("val date = sdf.parse(t.deadlineInfo.actualDeadline)", "val date = t.deadlineInfo?.actualDeadline?.let { sdf.parse(it) }")

# Replace daysLeft = t.deadlineInfo.remainingDays
content = content.replace("daysLeft = t.deadlineInfo.remainingDays,", "daysLeft = t.deadlineInfo?.remainingDays ?: 0,")

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
    f.write(content)
