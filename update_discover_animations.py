import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

discover_target = """                                // 8. Discover Section
                                if (!isLoadingMore) {
                                    item {
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Text(
                                            text = "Discover",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFProDisplayFontFamily,
                                            modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 16.dp)
                                        )
                                    }
                                    
                                    item {
                                        Box(modifier = Modifier.animateEnter()) {
                                            DailyInsightCard(insight = dailyInsight)
                                        }
                                    }
                                    
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 100)) {
                                            SuggestedArticlesSection(articles = suggestedArticles) { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            }
                                        }
                                    }"""

discover_rep = """                                // 8. Discover Section
                                if (!isLoadingMore) {
                                    item {
                                        Spacer(modifier = Modifier.height(32.dp))
                                        Box(modifier = Modifier.animateEnter(delayMillis = 200)) {
                                            Text(
                                                text = "Discover",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFProDisplayFontFamily,
                                                modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 16.dp)
                                            )
                                        }
                                    }
                                    
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 250)) {
                                            DailyInsightCard(insight = dailyInsight)
                                        }
                                    }
                                    
                                    item {
                                        Box(modifier = Modifier.animateEnter(delayMillis = 300)) {
                                            SuggestedArticlesSection(articles = suggestedArticles) { url ->
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            }
                                        }
                                    }"""
content = content.replace(discover_target, discover_rep)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
