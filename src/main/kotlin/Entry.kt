
import kotlinx.serialization.Serializable
import PrayerStatus.*
import Prayer.*


@Serializable
enum class PrayerStatus { NOT_DONE, ON_TIME, OFF_TIME}

@Serializable
data class Entry(val fajr: PrayerStatus = NOT_DONE, val zuhr: PrayerStatus = NOT_DONE, val asr: PrayerStatus = NOT_DONE,
                 val maghreb: PrayerStatus = NOT_DONE, val isha: PrayerStatus = NOT_DONE)

operator fun Entry.get(prayer: Prayer) = when(prayer) {
    FAJR -> fajr
    ZUHR -> zuhr
    ASR -> asr
    MAGHREB -> maghreb
    ISHA -> isha
}

fun Entry.copy(prayer: Prayer, status: PrayerStatus) = when (prayer) {
    FAJR -> copy(fajr = status)
    ZUHR -> copy(zuhr = status)
    ASR -> copy(asr = status)
    MAGHREB -> copy(maghreb = status)
    ISHA -> copy(isha = status)
}