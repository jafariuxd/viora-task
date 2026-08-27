import re

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "r") as f:
    content = f.read()

# Update AgendaItemData
data_class_old = """data class AgendaItemData(
    val day: String,
    val type: String,
    val isOnline: Boolean,
    val time: String,
    val title: String,
    val originalDateTime: String = ""
)"""

data_class_new = """data class AgendaItemData(
    val id: String,
    val day: String,
    val type: String,
    val isOnline: Boolean,
    val time: String,
    val title: String,
    val originalDateTime: String = "",
    val htmlLink: String = "",
    val isPast: Boolean = false
)"""
content = content.replace(data_class_old, data_class_new)

with open("app/src/main/java/com/example/ui/screens/AgendaScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "r") as f:
    viewmodel_content = f.read()

parse_event_old = """        return AgendaItemData(
            day = day,
            type = type,
            isOnline = isOnline,
            time = timeString,
            title = item.summary ?: "No Title",
            originalDateTime = dtStart // We can store this to sort or group later
        )"""

parse_event_new = """        
        val isPast = try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).parse(dtEnd.ifEmpty { dtStart })
            date?.before(java.util.Date()) == true
        } catch (e: Exception) {
            false
        }

        return AgendaItemData(
            id = item.id ?: java.util.UUID.randomUUID().toString(),
            day = day,
            type = type,
            isOnline = isOnline,
            time = timeString,
            title = item.summary ?: "No Title",
            originalDateTime = dtStart,
            htmlLink = item.htmlLink ?: "",
            isPast = isPast
        )"""

viewmodel_content = viewmodel_content.replace(parse_event_old, parse_event_new)

with open("app/src/main/java/com/example/viewmodel/AgendaViewModel.kt", "w") as f:
    f.write(viewmodel_content)

