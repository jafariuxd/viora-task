import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun main() {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
    sdf.timeZone = TimeZone.getDefault()
    println(sdf.format(cal.time))
}
