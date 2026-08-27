import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

imports = """
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess
"""

content = content.replace("import android.os.Bundle", imports + "\nimport android.os.Bundle")

handler_code = """
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    val prefs: SharedPreferences = getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
    val lastCrash = prefs.getString("last_crash", null)
    
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        exception.printStackTrace(pw)
        prefs.edit().putString("last_crash", sw.toString()).commit()
        defaultHandler?.uncaughtException(thread, exception)
    }
"""

content = content.replace("  override fun onCreate(savedInstanceState: Bundle?) {\n    super.onCreate(savedInstanceState)", handler_code)

ui_code = """
    setContent {
      val crashText = remember { mutableStateOf(lastCrash) }
      if (crashText.value != null) {
          AlertDialog(
              onDismissRequest = { crashText.value = null; prefs.edit().remove("last_crash").apply() },
              title = { Text("App Crashed Last Time") },
              text = { Text(crashText.value ?: "") },
              confirmButton = {
                  TextButton(onClick = { crashText.value = null; prefs.edit().remove("last_crash").apply() }) {
                      Text("Dismiss")
                  }
              }
          )
      }
      MyApplicationTheme {"""

content = content.replace("    setContent {\n      MyApplicationTheme {", ui_code)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
