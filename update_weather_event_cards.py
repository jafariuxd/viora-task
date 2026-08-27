import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# WeatherTimeCard Signature
target_weather_sig = """fun WeatherTimeCard(
    weatherInfo: WeatherInfo,
    timeString: String
) {"""
rep_weather_sig = """fun WeatherTimeCard(
    weatherInfo: WeatherInfo,
    timeString: String,
    isLoading: Boolean = false
) {"""
content = content.replace(target_weather_sig, rep_weather_sig)

# UpcomingEventCard Signature
target_event_sig = """fun UpcomingEventCard(
    event: CalendarEvent,
    onClick: () -> Unit
) {"""
rep_event_sig = """fun UpcomingEventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    isLoading: Boolean = false
) {"""
content = content.replace(target_event_sig, rep_event_sig)

# Update WeatherTimeCard content
# Just going to replace some Texts with skeletons.
# Let's see the weather card.
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
