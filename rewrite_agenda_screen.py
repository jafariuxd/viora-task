import re

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

# Add OptIn
if "import androidx.compose.foundation.ExperimentalFoundationApi" not in content:
    content = content.replace("import androidx.compose.foundation.layout.Box", "import androidx.compose.foundation.ExperimentalFoundationApi\nimport androidx.compose.foundation.layout.Box")

if "import androidx.compose.foundation.lazy.items" not in content:
    content = content.replace("import androidx.compose.foundation.lazy.LazyColumn", "import androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items")
    
if "import android.content.Intent" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import android.content.Intent\nimport android.net.Uri\nimport androidx.compose.ui.Modifier")


content = content.replace("@Composable\nfun AgendaScreen", "@OptIn(ExperimentalFoundationApi::class)\n@Composable\nfun AgendaScreen")

# Replace LazyColumn content and add scroll effect
lazy_col_old = """            LazyColumn(
                state = listState,
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
                if (isFetchingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VioraNeonLime)
                        }
                    }
                }
                if (paginationError != null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { viewModel.clearPaginationError(); viewModel.loadMore(context) }, contentAlignment = Alignment.Center) {
                            Text(text = "Error: $paginationError (Tap to retry)", color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            }"""

lazy_col_new = """            
            // Scroll to next event
            LaunchedEffect(events) {
                if (events.isNotEmpty()) {
                    val grouped = events.groupBy { 
                         try {
                            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(it.originalDateTime.take(10))
                            java.text.SimpleDateFormat("MMMM", java.util.Locale.US).format(date!!)
                        } catch(e: Exception) { "Unknown" }
                    }
                    var targetIndex = 0
                    var found = false
                    for ((_, monthEvents) in grouped) {
                        targetIndex++ // For the sticky header
                        for (event in monthEvents) {
                            if (!event.isPast) {
                                found = true
                                break
                            }
                            targetIndex++
                        }
                        if (found) break
                    }
                    if (found && targetIndex > 0) {
                        listState.scrollToItem(targetIndex)
                    }
                }
            }

            LazyColumn(
                state = listState,
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
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = month,
                                color = Color.Gray.copy(alpha = 0.5f),
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    items(monthEvents, key = { it.id }) { item ->
                        AgendaItem(item = item, onClick = {
                            if (item.htmlLink.isNotEmpty()) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.htmlLink))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                if (isFetchingMore) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = VioraNeonLime)
                        }
                    }
                }
                if (paginationError != null) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { viewModel.clearPaginationError(); viewModel.loadMore(context) }, contentAlignment = Alignment.Center) {
                            Text(text = "Error: $paginationError (Tap to retry)", color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            }"""

content = content.replace(lazy_col_old, lazy_col_new)

# Remove old AgendaMonthSection
month_section_old = """@Composable
fun AgendaMonthSection(month: String, items: List<AgendaItemData>) {
    Column {
        Text(
            text = month,
            color = Color.Gray.copy(alpha = 0.5f),
            fontSize = 40.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        items.forEach { item ->
            AgendaItem(item)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}"""
content = content.replace(month_section_old, "")


agenda_item_old = """@Composable
fun AgendaItem(item: AgendaItemData) {
    Row("""

agenda_item_new = """@Composable
fun AgendaItem(item: AgendaItemData, onClick: () -> Unit = {}) {
    val alpha = if (item.isPast) 0.5f else 1f
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.alpha(alpha),"""
        
content = content.replace(agenda_item_old, agenda_item_new)

if "import androidx.compose.ui.draw.alpha" not in content:
    content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.alpha")

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)
