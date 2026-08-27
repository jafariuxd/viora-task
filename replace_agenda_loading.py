with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

if "import com.example.ui.utils.shimmerEffect" not in content:
    content = content.replace("import com.example.ui.utils.animateEnter", "import com.example.ui.utils.animateEnter\nimport com.example.ui.utils.shimmerEffect")

old_loading = """            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VioraNeonLime)
                }
            } else if (error != null && events.isEmpty()) {"""

new_loading = """            if (isLoading) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxWidth().height(60.dp).padding(vertical = 16.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                    for (i in 0..4) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(60.dp).height(100.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.width(12.dp))
                            Card(
                                modifier = Modifier.weight(1f).height(100.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                                }
                            }
                        }
                    }
                }
            } else if (error != null && events.isEmpty()) {"""

content = content.replace(old_loading, new_loading)

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)
