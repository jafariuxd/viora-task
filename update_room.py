import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("  implementation(libs.androidx.room.ktx)", "  // implementation(libs.androidx.room.ktx)")
content = content.replace("  implementation(libs.androidx.room.runtime)", "  // implementation(libs.androidx.room.runtime)")
content = content.replace('  "ksp"(libs.androidx.room.compiler)', '  // "ksp"(libs.androidx.room.compiler)')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
