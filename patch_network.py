import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("implementation(libs.androidx.core.ktx)", "implementation(libs.androidx.core.ktx)\n  implementation(libs.retrofit)\n  implementation(libs.converter.moshi)\n  implementation(libs.moshi.kotlin)\n  implementation(libs.okhttp)\n  implementation(libs.logging.interceptor)")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
