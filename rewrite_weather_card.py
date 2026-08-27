import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Add import if missing
if "import com.example.ui.utils.shimmerEffect" not in content:
    content = content.replace("import com.example.ui.utils.animateEnter", "import com.example.ui.utils.animateEnter\nimport com.example.ui.utils.shimmerEffect")

# Replace WeatherTimeCard
old_weather_body = """            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherInfo.dateText,
                    color = VioraDarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Location",
                        tint = VioraDarkText.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = weatherInfo.location,
                        color = VioraDarkText.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeString,
                    color = VioraDarkText,
                    style = MaterialTheme.typography.headlineLarge
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val weatherEmoji = when (weatherInfo.condition) {
                        "Sunny" -> "☀️"
                        "Cloudy" -> "☁️"
                        "Foggy" -> "🌫️"
                        "Rainy" -> "🌧️"
                        "Snowy" -> "❄️"
                        "Thunderstorm" -> "⛈️"
                        else -> "☀️"
                    }
                    Text(
                        text = weatherEmoji,
                        fontSize = 30.sp
                    )
                    Text(
                        text = weatherInfo.temperature,
                        color = VioraDarkText,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weatherInfo.lastRefreshText,
                    color = VioraDarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "UV Index : ${weatherInfo.uvIndex}",
                    color = VioraDarkText.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }"""

new_weather_body = """            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.width(100.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                } else {
                    Text(
                        text = weatherInfo.dateText,
                        color = VioraDarkText.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Location",
                            tint = VioraDarkText.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = weatherInfo.location,
                            color = VioraDarkText.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                } else {
                    Text(
                        text = timeString,
                        color = VioraDarkText,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLoading) {
                        Box(modifier = Modifier.width(80.dp).height(32.dp).clip(RoundedCornerShape(8.dp)).shimmerEffect())
                    } else {
                        val weatherEmoji = when (weatherInfo.condition) {
                            "Sunny" -> "☀️"
                            "Cloudy" -> "☁️"
                            "Foggy" -> "🌫️"
                            "Rainy" -> "🌧️"
                            "Snowy" -> "❄️"
                            "Thunderstorm" -> "⛈️"
                            else -> "☀️"
                        }
                        Text(
                            text = weatherEmoji,
                            fontSize = 30.sp
                        )
                        Text(
                            text = weatherInfo.temperature,
                            color = VioraDarkText,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.width(120.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(modifier = Modifier.width(90.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                } else {
                    Text(
                        text = weatherInfo.lastRefreshText,
                        color = VioraDarkText.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "UV Index : ${weatherInfo.uvIndex}",
                        color = VioraDarkText.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }"""
content = content.replace(old_weather_body, new_weather_body)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
