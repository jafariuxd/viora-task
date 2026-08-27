with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

target1 = """@Composable
fun DailyInsightCard(insight: DailyInsight) {"""
rep1 = """@Composable
fun DailyInsightCard(insight: DailyInsight?) {"""
content = content.replace(target1, rep1)

target2 = """        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Rounded.FormatQuote,
                contentDescription = null,
                tint = VioraNeonLime,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = insight.quote,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = SFProDisplayFontFamily,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "— ${insight.author}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontFamily = SFProDisplayFontFamily
            )
        }"""
rep2 = """        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Rounded.FormatQuote,
                contentDescription = null,
                tint = VioraNeonLime,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (insight == null) {
                Box(modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.7f).height(24.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            } else {
                Text(
                    text = insight.quote,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = SFProDisplayFontFamily,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "— ${insight.author}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontFamily = SFProDisplayFontFamily
                )
            }
        }"""
content = content.replace(target2, rep2)

target3 = """@Composable
fun SuggestedArticlesSection(articles: List<SuggestedArticle>, onArticleClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        articles.forEach { article ->
            SuggestedArticleCard(article = article, onClick = { onArticleClick(article.url) })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun SuggestedArticleCard(article: SuggestedArticle, onClick: () -> Unit) {"""

rep3 = """@Composable
fun SuggestedArticlesSection(articles: List<SuggestedArticle>?, onArticleClick: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        if (articles == null) {
            SuggestedArticleCard(article = null, onClick = {})
            Spacer(modifier = Modifier.height(12.dp))
            SuggestedArticleCard(article = null, onClick = {})
        } else {
            articles.forEach { article ->
                SuggestedArticleCard(article = article, onClick = { onArticleClick(article.url) })
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SuggestedArticleCard(article: SuggestedArticle?, onClick: () -> Unit) {"""
content = content.replace(target3, rep3)

target4 = """        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF2C2C2E), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = article.icon,
                    contentDescription = null,
                    tint = VioraNeonLime,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = article.category.uppercase(),
                    color = VioraNeonLime,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFProDisplayFontFamily,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = SFProDisplayFontFamily,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = article.readTime,
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp,
                        fontFamily = SFProDisplayFontFamily
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }"""
rep4 = """        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (article == null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp).clip(RoundedCornerShape(2.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(2.dp)).shimmerEffect())
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF2C2C2E), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = article.icon,
                        contentDescription = null,
                        tint = VioraNeonLime,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = article.category.uppercase(),
                        color = VioraNeonLime,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFProDisplayFontFamily,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = SFProDisplayFontFamily,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = article.readTime,
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp,
                            fontFamily = SFProDisplayFontFamily
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }"""
content = content.replace(target4, rep4)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
