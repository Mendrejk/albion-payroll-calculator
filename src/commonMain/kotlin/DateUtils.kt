import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun getCurrentFormattedDate(): String {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val day = currentDate.dayOfMonth.toString().padStart(2, '0')
    val month = currentDate.monthNumber.toString().padStart(2, '0')
    val year = currentDate.year.toString()
    return "${day}_${month}_${year}"
}
