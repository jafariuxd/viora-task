import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old_event_body = """            // Circular calendar date badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = event.day,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = event.month,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            // Text column is weighted so it expands to fill remaining space
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.title,
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = event.time,
                    color = Color.Black.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }"""

new_event_body = """            if (isLoading) {
                // Circular calendar date badge Skeleton
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).shimmerEffect())
                }
                Spacer(modifier = Modifier.width(14.dp))
                // Text column skeleton
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(modifier = Modifier.fillMaxWidth(0.3f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                }
            } else {
                // Circular calendar date badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = event.day,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = event.month,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                // Text column is weighted so it expands to fill remaining space
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.title,
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = event.time,
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }"""

content = content.replace(old_event_body, new_event_body)
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
