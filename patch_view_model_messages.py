import re

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

def inject_message(func_name, message):
    global content
    pattern = r'(fun ' + func_name + r'\([^)]*\)\s*\{[^\}]*)(\})'
    replacement = r'\1    showMessage("' + message + r'")\n\2'
    content = re.sub(pattern, replacement, content, count=1)

inject_message('upsertTask', 'Task saved successfully')
inject_message('deleteTask', 'Task deleted')
inject_message('addTeam', 'Team created successfully')
inject_message('deleteTeam', 'Team deleted')

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
