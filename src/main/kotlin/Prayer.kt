import kotlinx.serialization.Serializable
import Prayer.*

@Serializable
enum class Prayer {FAJR, ZUHR, ASR, MAGHREB, ISHA}

fun Char.toPrayer() = when(lowercaseChar()) {
    'f' -> FAJR
    'z' -> ZUHR
    'a' -> ASR
    'm' -> MAGHREB
    'i' -> ISHA
    else -> throw IllegalArgumentException()
}

val Prayer.asLetter get() = when(this) {
    FAJR -> 'f'
    ZUHR -> 'z'
    ASR -> 'a'
    MAGHREB -> 'm'
    ISHA -> 'i'
}

val Char.isValidPrayerLetter get() = lowercaseChar() in arrayOf('f', 'z', 'a', 'm', 'i')