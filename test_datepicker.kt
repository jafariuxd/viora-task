import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDP() {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = { }) { Text("OK", color = Color.Green) }
        },
        dismissButton = {
            TextButton(onClick = { }) { Text("Cancel", color = Color.White) }
        },
        colors = DatePickerDefaults.colors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1E1E1E),
                titleContentColor = Color.Green,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.Gray,
                subheadContentColor = Color.Gray,
                navigationContentColor = Color.White,
                yearContentColor = Color.White,
                currentYearContentColor = Color.Green,
                selectedYearContentColor = Color.Black,
                selectedYearContainerColor = Color.Green,
                dayContentColor = Color.White,
                disabledDayContentColor = Color.DarkGray,
                selectedDayContentColor = Color.Black,
                selectedDayContainerColor = Color.Green,
                todayContentColor = Color.Green,
                todayDateBorderColor = Color.Green
            )
        )
    }
}
