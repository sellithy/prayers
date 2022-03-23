
import kotlinx.serialization.Serializable
import PrayerStatus.*
import Prayer.*


@Serializable
enum class PrayerStatus { NOTDONE, ONTIME, OFFTIME}

@Serializable
data class Entry(val fajr: PrayerStatus = NOTDONE, val zuhr: PrayerStatus = NOTDONE, val asr: PrayerStatus = NOTDONE,
                 val maghreb: PrayerStatus = NOTDONE, val isha: PrayerStatus = NOTDONE)

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