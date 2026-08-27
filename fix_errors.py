import re

def replacer(match):
    prefix = match.group(1)
    return prefix + 'showMessage(com.example.util.ErrorUtil.getErrorMessage(e), true)\n            }'

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

content = re.sub(r'(catch\s*\(\s*[a-zA-Z0-9_]+\s*:\s*Exception\s*\)\s*\{\n\s*)showMessage\("[^"]+"\s*,\s*true\)\n\s*\}', replacer, content)

with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'w') as f:
    f.write(content)
