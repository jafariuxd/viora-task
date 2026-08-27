import re
with open("app/src/main/java/com/example/network/GoogleCalendarApi.kt", "r") as f:
    content = f.read()

content = content.replace("val location: String?", "val location: String?,\n    val htmlLink: String?")

with open("app/src/main/java/com/example/network/GoogleCalendarApi.kt", "w") as f:
    f.write(content)
