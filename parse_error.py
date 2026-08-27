with open('app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt', 'r') as f:
    content = f.read()

import re
print("Matches for catch block:")
for m in re.finditer(r'catch\s*\(\s*([a-zA-Z0-9_]+)\s*:\s*Exception\s*\)\s*\{\s*showMessage\("([^"]+)"\s*,\s*true\)\s*\}', content):
    print(m.group(0))
