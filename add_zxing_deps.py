import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("dependencies {", "dependencies {\n    implementation(libs.zxing.core)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
