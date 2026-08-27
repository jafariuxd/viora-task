import re

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

import_statement = """import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.AgendaViewModel
import com.google.android.gms.auth.api.identity.Identity
"""

content = content.replace("import androidx.compose.runtime.Composable", import_statement)

screen_old = """@Composable
fun AgendaScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {"""

screen_new = """@Composable
fun AgendaScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = viewModel()
) {
    val context = LocalContext.current as Activity
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val authIntent by viewModel.authIntent.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val authResult = Identity.getAuthorizationClient(context).getAuthorizationResultFromIntent(result.data)
            viewModel.handleAuthorizationResult(authResult.accessToken)
        } else {
            viewModel.handleAuthorizationResult(null)
        }
    }

    LaunchedEffect(authIntent) {
        if (authIntent != null) {
            launcher.launch(IntentSenderRequest.Builder(authIntent!!).build())
            viewModel.clearAuthIntent()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.authorizeAndFetch(context)
    }
"""

content = content.replace(screen_old, screen_new)

# Now update the LazyColumn
list_old = """        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                AgendaMonthSection(
                    month = "November",
                    items = listOf(
                        AgendaItemData("19", "Online meeting", true, "11:00 am - 11:30 am", "Weekly Leadership"),
                        AgendaItemData("23", "In-person meeting", false, "11:00 am - 11:30 am", "Weekly Leadership"),
                        AgendaItemData("28", "Online meeting", true, "11:00 am - 11:30 am", "Weekly Leadership")
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                AgendaMonthSection(
                    month = "December",
                    items = listOf(
                        AgendaItemData("01", "Online meeting", true, "11:00 am - 11:30 am", "Weekly Leadership")
                    )
                )
            }
        }"""

list_new = """        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VioraNeonLime)
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error ?: "Error", color = Color.Red, fontSize = 16.sp)
            }
        } else if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No events scheduled.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Group by month
                val grouped = events.groupBy { 
                    try {
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it.originalDateTime.take(10))
                        java.text.SimpleDateFormat("MMMM", java.util.Locale.US).format(date!!)
                    } catch(e: Exception) { "Unknown" }
                }

                grouped.forEach { (month, monthEvents) ->
                    item {
                        AgendaMonthSection(
                            month = month,
                            items = monthEvents
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }"""

content = content.replace(list_old, list_new)

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)
