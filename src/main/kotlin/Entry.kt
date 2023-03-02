import kotlinx.serialization.Serializable
import PrayerStatus.*
import Prayer.*

@Serializable
enum class PrayerStatus { NOT_DONE, ON_TIME, OFF_TIME }

@Serializable
data class Entry(
    val fajr: PrayerStatus = NOT_DONE, val zuhr: PrayerStatus = NOT_DONE, val asr: PrayerStatus = NOT_DONE,
    val maghreb: PrayerStatus = NOT_DONE, val isha: PrayerStatus = NOT_DONE
) : Map<Prayer, PrayerStatus> by mapOf(FAJR to fajr, ZUHR to zuhr, ASR to asr, MAGHREB to maghreb, ISHA to isha) {

    fun toFormatted() = entries.joinToString(separator = "") { it.toPair().toFormatted() }
}

fun Pair<Prayer, PrayerStatus>.toFormatted() = when (second) {
    NOT_DONE -> " "
    ON_TIME -> first.asLetter.toString()
    OFF_TIME -> first.asLetter.uppercase()
}

fun Char.toPrayerPair() = toPrayer() to if (isUpperCase()) OFF_TIME else ON_TIME

fun Entry.copy(prayer: Prayer, status: PrayerStatus) = when (prayer) {
    FAJR -> copy(fajr = status)
    ZUHR -> copy(zuhr = status)
    ASR -> copy(asr = status)
    MAGHREB -> copy(maghreb = status)
    ISHA -> copy(isha = status)
}