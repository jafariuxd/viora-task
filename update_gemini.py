import re

with open("app/src/main/java/com/example/network/GeminiHelper.kt", "r") as f:
    content = f.read()

# Make generateTaskFromAudio and generateTaskFromJson accept an optional contextInfo
# We will inject contextInfo into the prompt
content = content.replace("suspend fun generateTaskFromJson(prompt: String): String?", "suspend fun generateTaskFromJson(prompt: String, contextInfo: String = \"\"): String?")
content = content.replace("suspend fun generateTaskFromAudio(base64Audio: String, mimeType: String): String?", "suspend fun generateTaskFromAudio(base64Audio: String, mimeType: String, contextInfo: String = \"\"): String?")

# For generateTaskFromJson:
content = content.replace("Part(text = prompt)", "Part(text = prompt + \"\\n\\n\" + contextInfo)")

# For generateTaskFromAudio:
content = content.replace("Part(text = \"Please transcribe this audio and create the JSON task.\")", "Part(text = \"Please transcribe this audio and create the JSON task.\\n\\n\" + contextInfo)")

with open("app/src/main/java/com/example/network/GeminiHelper.kt", "w") as f:
    f.write(content)
