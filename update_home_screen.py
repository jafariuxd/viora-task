import sys

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_task = """                    activeDetailTask = Task(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "",
                        client = "Viora design",
                        daysLeft = 7,
                        status = TaskStatus.TODO
                    )"""

new_task = """                    activeDetailTask = Task(
                        id = java.util.UUID.randomUUID().toString(),
                        title = "",
                        client = "Viora design",
                        userId = "user1",
                        daysLeft = 7,
                        status = TaskStatus.TODO
                    )"""

if old_task in content:
    content = content.replace(old_task, new_task)
    with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Could not find old task block")
