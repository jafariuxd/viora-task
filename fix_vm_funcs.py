import re

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "r") as f:
    content = f.read()

def remove_dups(content, func_name):
    parts = content.split(func_name)
    if len(parts) > 2:
        # Keep the first part and the last part, effectively removing all but the first occurrence
        # Wait, the string 'fun func_name' is better
        pass

# It is easier to use regex
content = re.sub(r'(    fun fetchScannedUser\(.*?\n        }\n    }\n)', r'\1', content, count=1)
# Actually, I'll just remove all of them and add them once.

content = re.sub(r'    fun fetchScannedUser\(.*?\n        }\n    }\n\n', '', content, flags=re.DOTALL)
content = re.sub(r'    fun addScannedUserToTeam\(.*?\n        }\n    }\n\n', '', content, flags=re.DOTALL)
content = re.sub(r'    fun clearScannedUser\(\) \{\n        _scannedUser\.value = null\n    \}\n\n', '', content, flags=re.DOTALL)

with open("app/src/main/java/com/example/viewmodel/VioraTaskViewModel.kt", "w") as f:
    f.write(content)
