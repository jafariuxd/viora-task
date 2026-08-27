import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add startDest logic
content = content.replace(
    'val prefs: SharedPreferences = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)',
    'val prefs: SharedPreferences = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)\n    val openDailyBrief = intent?.getBooleanExtra("OPEN_DAILY_BRIEF", false) == true'
)

content = content.replace(
    'AppNavGraph(',
    'AppNavGraph(\n              startDestinationOverride = if (openDailyBrief) "daily_brief" else null,'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
