import kotlinx.cinterop.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.posix.*


val path = "C:\\Users\\sheha\\OneDrive\\Keep\\Prayers\\prayers2.txt"

val prettyJson = Json { prettyPrint = true }

@Serializable
data class Prayers(val fajr: Int, val zuhr: Int, val asr: Int, val maghrib: Int, val isha: Int, val lastDate: LocalDate)

fun main() {
    println(readAllText(path))
}

// This is C but in kotlin
fun readAllText(filePath: String) =
    fopen(filePath, "r")!!.let {
        fseek(it, 0, SEEK_END)
        val numBytes = ftell(it)
        rewind(it)

        memScoped {
            val buffer = malloc((numBytes + 1).toULong())!!.reinterpret<ByteVar>()
            fread(buffer, 1, numBytes.toULong(), it)
            buffer[numBytes] = 0
            buffer.toKString()
        }.apply {
            fclose(it)
        }
    }


