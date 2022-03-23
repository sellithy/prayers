import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

val prettyJson = Json { prettyPrint = true }
private val today = Clock.System.todayAt(TimeZone.currentSystemDefault())
private val yesterday = today - DatePeriod(days = 1)

class Hello : CliktCommand() {
    private val prayersInput: List<Pair<Prayer, PrayerStatus>> by argument(help = "The prayers that were prayed. Uppercase for offtime prayers. Only the last letter for each prayer is counted. Format: [fFZzAamMIi]*").convert { input ->
        input.map {
            it.toPrayer() to if (it.isUpperCase()) PrayerStatus.OFFTIME else PrayerStatus.ONTIME
        }
    }

    private val unspecifiedFlag: Boolean by option(
        "-u",
        "--unspecified",
        help = "If the prayers were for an unspecified day. Cannot be passed with --day or --yesterday"
    ).flag()

    private val yesterdayFlag: Boolean by option(
        "-y", "--yesterday", help = "Shortcut for yesterday. Cannot be passed with --day or --unspecified"
    ).flag()

    private val dayInput: LocalDate? by option(
        "-d",
        "--day",
        help = "The day when the prayers were prayed. Defaults to today. Cannot be passed with --unspecified or --yesterday. Format: yyyy-mm-dd"
    ).convert { it.toLocalDate() }

    private val path: File by option(
        envvar = "PRAYERS_FILE_PATH",
        help = "The path to the storage file. Defaults to the env variable 'PRAYERS_FILE_PATH'"
    ).file().required()

    private fun ensureCorrectFlags() =
        mutableListOf<String>().run {
            if (dayInput != null) add("--day")
            if (yesterdayFlag) add("--yesterday")
            if (unspecifiedFlag) add("--unspecified")

            if (size >= 2)
                throw MutuallyExclusiveGroupException(this)
        }

    override fun run() {
        ensureCorrectFlags()

        val dateToEntry = Json.decodeFromStream<MutableMap<LocalDate, Entry>>(path.inputStream())
        val lastDay = dateToEntry.keys.maxOf { it }

        fun copyPrayer(day: LocalDate, prayer: Prayer, status: PrayerStatus) =
            dateToEntry.set(day, dateToEntry[day]!!.copy(prayer, status))

        // Add empty entries
        (lastDay.nextDay()..today).forEach { dateToEntry[it] = Entry() }

        if (!unspecifiedFlag)
            prayersInput.forEach { (prayer, status) ->
                copyPrayer(dayInput ?: if (yesterdayFlag) yesterday else today, prayer, status)
            }
        else
            prayersInput.forEach { (prayer, _) ->
                dateToEntry.firstNotNullOfOrNull { (date, entry) ->
                    if (entry[prayer] == PrayerStatus.NOTDONE) date else null
                }.let {
                    copyPrayer(it!!, prayer, PrayerStatus.OFFTIME)
                }
            }

        // Write back to file
        prettyJson.encodeToStream(dateToEntry, path.outputStream())
    }
}

fun main(args: Array<String>) = Hello().main(args)