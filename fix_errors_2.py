import re

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('showMessage("Failed to load from server")', 'showMessage("Failed to load: ${com.example.util.ErrorUtil.getErrorMessage(e)}")')
content = content.replace('showMessage("Error saving task: ${e.message}", true)', 'showMessage("Error saving task: ${com.example.util.ErrorUtil.getErrorMessage(e)}", true)')

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
