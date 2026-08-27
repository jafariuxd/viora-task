import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

import_str = """import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions"""

content = content.replace("import com.example.viewmodel.VioraTaskViewModel", "import com.example.viewmodel.VioraTaskViewModel\n" + import_str)

init_str = """    super.onCreate(savedInstanceState)
    if (FirebaseApp.getApps(this).isEmpty()) {
        val options = FirebaseOptions.Builder()
            .setProjectId("asymmetric-granite-b40ks")
            .setApplicationId("1:477569508984:web:9eaa938baf00e53f5a6f8d")
            .setApiKey("AIzaSyDNLg2jYVJG_YCaDF3zkvsgVttkqa1Qco8")
            .build()
        FirebaseApp.initializeApp(this, options)
    }"""

content = content.replace("    super.onCreate(savedInstanceState)", init_str)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
