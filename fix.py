import sys

# 1. EditProfileScreen
with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'r') as f:
    c1 = f.read()
c1 = c1.replace('var defaultDeadline by remember { mutableStateOf("Weekly") }', 
                'var defaultDeadline by remember { mutableStateOf("Weekly") }\n    var customDays by remember { mutableStateOf(3) }')
with open('app/src/main/java/com/example/ui/screens/EditProfileScreen.kt', 'w') as f:
    f.write(c1)

# 2. TeamsScreen
with open('app/src/main/java/com/example/ui/screens/TeamsScreen.kt', 'r') as f:
    c2 = f.read()
c2 = c2.replace('var deadline by remember { mutableStateOf("Weekly") }', 
                'var deadline by remember { mutableStateOf("Weekly") }\n    var customDays by remember { mutableStateOf(3) }')
with open('app/src/main/java/com/example/ui/screens/TeamsScreen.kt', 'w') as f:
    f.write(c2)

# 3. TeamDetailScreen
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'r') as f:
    c3 = f.read()
c3 = c3.replace('var deadline by remember { mutableStateOf("Weekly") }', 
                'var deadline by remember { mutableStateOf("Weekly") }\n    var customDays by remember { mutableStateOf(3) }')
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'w') as f:
    f.write(c3)

