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