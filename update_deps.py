import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("  implementation(libs.firebase.ai)", "  // implementation(libs.firebase.ai)")
content = content.replace("  implementation(libs.firebase.auth)", "  // implementation(libs.firebase.auth)")
content = content.replace("  implementation(libs.androidx.credentials)", "  // implementation(libs.androidx.credentials)")
content = content.replace("  implementation(libs.androidx.credentials.play.services)", "  // implementation(libs.androidx.credentials.play.services)")
content = content.replace("  implementation(libs.googleid)", "  // implementation(libs.googleid)")
content = content.replace('  implementation("com.google.android.gms:play-services-auth:21.0.0")', '  // implementation("com.google.android.gms:play-services-auth:21.0.0")')
content = content.replace("  implementation(libs.firebase.appcheck.recaptcha)", "  // implementation(libs.firebase.appcheck.recaptcha)")

# also test dependencies
content = content.replace("  testImplementation(libs.androidx.compose.ui.test.junit4)", "  // testImplementation(libs.androidx.compose.ui.test.junit4)")
content = content.replace("  testImplementation(libs.androidx.core)", "  // testImplementation(libs.androidx.core)")
content = content.replace("  testImplementation(libs.androidx.junit)", "  // testImplementation(libs.androidx.junit)")
content = content.replace("  testImplementation(libs.junit)", "  // testImplementation(libs.junit)")
content = content.replace("  testImplementation(libs.kotlinx.coroutines.test)", "  // testImplementation(libs.kotlinx.coroutines.test)")
content = content.replace("  testImplementation(libs.robolectric)", "  // testImplementation(libs.robolectric)")
content = content.replace("  testImplementation(libs.roborazzi)", "  // testImplementation(libs.roborazzi)")
content = content.replace("  testImplementation(libs.roborazzi.compose)", "  // testImplementation(libs.roborazzi.compose)")
content = content.replace("  testImplementation(libs.roborazzi.junit.rule)", "  // testImplementation(libs.roborazzi.junit.rule)")

content = content.replace("  androidTestImplementation(platform(libs.androidx.compose.bom))", "  // androidTestImplementation(platform(libs.androidx.compose.bom))")
content = content.replace("  androidTestImplementation(libs.androidx.compose.ui.test.junit4)", "  // androidTestImplementation(libs.androidx.compose.ui.test.junit4)")
content = content.replace("  androidTestImplementation(libs.androidx.espresso.core)", "  // androidTestImplementation(libs.androidx.espresso.core)")
content = content.replace("  androidTestImplementation(libs.androidx.junit)", "  // androidTestImplementation(libs.androidx.junit)")
content = content.replace("  androidTestImplementation(libs.androidx.runner)", "  // androidTestImplementation(libs.androidx.runner)")


with open("app/build.gradle.kts", "w") as f:
    f.write(content)
