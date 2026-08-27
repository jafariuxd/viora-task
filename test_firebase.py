import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("// implementation(libs.firebase.auth)", "implementation(libs.firebase.auth)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
