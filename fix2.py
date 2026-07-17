import sys

# 2. TeamsScreen
with open('app/src/main/java/com/example/ui/screens/TeamsScreen.kt', 'r') as f:
    c2 = f.read()
c2 = c2.replace('var deadline by remember { mutableStateOf("Daily") }', 
                'var deadline by remember { mutableStateOf("Daily") }\n    var customDays by remember { mutableStateOf(3) }')
with open('app/src/main/java/com/example/ui/screens/TeamsScreen.kt', 'w') as f:
    f.write(c2)

# 3. TeamDetailScreen
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'r') as f:
    c3 = f.read()
c3 = c3.replace('var deadline by remember { mutableStateOf("Daily") }', 
                'var deadline by remember { mutableStateOf("Daily") }\n    var customDays by remember { mutableStateOf(3) }')
with open('app/src/main/java/com/example/ui/screens/TeamDetailScreen.kt', 'w') as f:
    f.write(c3)

