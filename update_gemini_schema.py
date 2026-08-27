import re

with open("app/src/main/java/com/example/network/GeminiHelper.kt", "r") as f:
    content = f.read()

new_schema = """        The JSON object must match this schema:
        {
          "title": "Task title in English",
          "description": "Any additional details or context in English (optional)",
          "listId": "The folder/list it belongs to",
          "teamId": "The name of the team this list belongs to (optional, but include if available)",
          "daysLeft": 7
        }"""

content = re.sub(r'        The JSON object must match this schema:[\s\S]*?\}', new_schema, content)

with open("app/src/main/java/com/example/network/GeminiHelper.kt", "w") as f:
    f.write(content)
