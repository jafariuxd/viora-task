import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# 1. UpcomingEventCard
event_pattern = r"""        Row\(
            modifier = Modifier
                \.fillMaxWidth\(\)
                \.padding\(14\.dp\),
            verticalAlignment = Alignment\.CenterVertically
        \) \{
            if \(isLoading\) \{
                // Shimmer layout
                Box\("""

event_replacement = r"""        AnimatedContent(
            targetState = isLoading to (event == null),
            transitionSpec = {
                (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))) togetherWith
                        fadeOut(animationSpec = tween(300))
            },
            label = "event_anim"
        ) { (loading, isEmpty) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                // Shimmer layout
                Box("""

content = re.sub(event_pattern, event_replacement, content)

event_end_pattern = r"""                        textAlign = androidx\.compose\.ui\.text\.style\.TextAlign\.Center
                    \)
                \}
            \}
        \}
    \}
\}

@Composable
fun MetricBadge\("""

event_end_replacement = r"""                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        }
    }
}

@Composable
fun MetricBadge("""

content = re.sub(event_end_pattern, event_end_replacement, content)


# 2. DiscoverDailyInsightCard
insight_pattern = r"""        Column\(modifier = Modifier\.padding\(20\.dp\)\) \{
            Icon\(
                imageVector = Icons\.Rounded\.FormatQuote,
                contentDescription = null,
                tint = VioraNeonLime,
                modifier = Modifier\.size\(28\.dp\)
            \)
            Spacer\(modifier = Modifier\.height\(12\.dp\)\)
            if \(insight == null\) \{
                Box"""

insight_replacement = r"""        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Rounded.FormatQuote,
                contentDescription = null,
                tint = VioraNeonLime,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(
                targetState = insight,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f, animationSpec = tween(400))) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "insight_anim"
            ) { currentInsight ->
            if (currentInsight == null) {
                Column {
                Box"""

content = re.sub(insight_pattern, insight_replacement, content)

insight_end_pattern = r"""                    fontFamily = SFProDisplayFontFamily
                \)
            \}
        \}
    \}
\}

@Composable
fun SuggestedArticlesSection\("""

insight_end_replacement = r"""                    fontFamily = SFProDisplayFontFamily
                )
            }
            }
        }
    }
}

@Composable
fun SuggestedArticlesSection("""

content = re.sub(insight_end_pattern, insight_end_replacement, content)

# 3. SuggestedArticlesSection
articles_pattern = r"""fun SuggestedArticlesSection\(articles: List<SuggestedArticle>\?, onArticleClick: \(String\) -> Unit\) \{
    Column\(modifier = Modifier\.padding\(bottom = 0\.dp\)\) \{
        if \(articles == null\) \{
            for \(i in 0\.\.1\) \{
                Card\("""

articles_replacement = r"""fun SuggestedArticlesSection(articles: List<SuggestedArticle>?, onArticleClick: (String) -> Unit) {
    AnimatedContent(
        targetState = articles,
        transitionSpec = {
            (fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { 20 }, animationSpec = tween(400))) togetherWith
                    fadeOut(animationSpec = tween(300))
        },
        label = "articles_anim"
    ) { currentArticles ->
    Column(modifier = Modifier.padding(bottom = 0.dp)) {
        if (currentArticles == null) {
            for (i in 0..1) {
                Card("""

content = re.sub(articles_pattern, articles_replacement, content)

articles_end_pattern = r"""                    \}
                \}
            \}
        \}
    \}
\}

@OptIn\(ExperimentalMaterial3Api::class\)
@Composable
fun QuickAddActivity"""

articles_end_replacement = r"""                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddActivity"""

content = re.sub(articles_end_pattern, articles_end_replacement, content)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
