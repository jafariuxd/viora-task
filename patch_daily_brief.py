with open("app/src/main/java/com/example/ui/screens/DailyBriefScreen.kt", "r") as f:
    content = f.read()

content = content.replace("""        if (isAuthorized) {
            // Load if not loaded
            if (events.isEmpty()) {
                agendaViewModel.loadFromLocal()
            }
        }""", "")

with open("app/src/main/java/com/example/ui/screens/DailyBriefScreen.kt", "w") as f:
    f.write(content)
