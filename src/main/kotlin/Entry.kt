import kotlinx.serialization.Serializable
//import PrayerStatus.*
import NewPrayerStatus.*
import Prayer.*
import kotlinx.datetime.LocalDate

@Serializable
sealed class NewPrayerStatus {
    @Serializable
    object OnTime : NewPrayerStatus()

    @Serializable
    object OffTime : NewPrayerStatus()

    @Serializable
    object NotDone : NewPrayerStatus()

    @Serializable
    // Null means prayedDate is unknown
    data class Unspecified(val prayedDate: LocalDate? = null) : NewPrayerStatus()
}

@Serializable
data class NewEntry(
    val fajr: NewPrayerStatus = NotDone, val zuhr: NewPrayerStatus = NotDone, val asr: NewPrayerStatus = NotDone,
    val maghreb: NewPrayerStatus = NotDone, val isha: NewPrayerStatus = NotDone
) : Map<Prayer, NewPrayerStatus> by mapOf(FAJR to fajr, ZUHR to zuhr, ASR to asr, MAGHREB to maghreb, ISHA to isha) {
    fun toFormatted() = entries.joinToString(separator = "") { it.toPair().toFormatted() }
}
object ANSIColors {
    const val RED = "\u001b[31m"
    const val RESET = "\u001b[0m"
}

fun Pair<Prayer, NewPrayerStatus>.toFormatted() = when (second) {
    NotDone -> " "
    is OnTime -> first.asLetter.toString()
    is OffTime -> first.asLetter.uppercase()
    is Unspecified -> "${ANSIColors.RED}${first.asLetter.uppercase()}${ANSIColors.RESET}"
}

fun NewEntry.copy(prayer: Prayer, status: NewPrayerStatus) = when (prayer) {
    FAJR -> copy(fajr = status)
    ZUHR -> copy(zuhr = status)
    ASR -> copy(asr = status)
    MAGHREB -> copy(maghreb = status)
    ISHA -> copy(isha = status)
}

fun Char.toNewPrayerPair() = toPrayer() to if (isUpperCase()) OffTime else OnTime
