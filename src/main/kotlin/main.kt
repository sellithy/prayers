import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.datetime.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

val prettyJson = Json { prettyPrint = true }
val helpTexts = "help_texts.properties".asProperties
val today = Clock.System.todayAt(TimeZone.currentSystemDefault())
val yesterday = today - DatePeriod(days = 1)

typealias PrayersFile = LinkedHashMap<LocalDate, Entry>

class Hello : CliktCommand() {
    private val path: File by option(
        envvar = "PRAYERS_FILE_PATH", help = helpTexts["path"]
    ).file().required()

    private val prayersInput: List<Pair<Prayer, PrayerStatus>>? by argument(
        help = helpTexts["prayersInput"]
    ).convert { input ->
        input.map {
            it.toPrayer() to if (it.isUpperCase()) PrayerStatus.OFF_TIME else PrayerStatus.ON_TIME
        }
    }.optional()

    private val unspecifiedFlag: Boolean by option(
        "-u", "--unspecified", help = helpTexts["unspecifiedFlag"]
    ).flag()

    private val yesterdayFlag: Boolean by option(
        "-y", "--yesterday", help = helpTexts["yesterdayFlag"]
    ).flag()

    private val printFlag: Boolean by option(
        "-p", "--print", help = helpTexts["printFlag"]
    ).flag()

    private val dayInput: LocalDate? by option(
        "-d", "--day", help = helpTexts["dayInput"]
    ).convert { it.toLocalDate() }

    private fun ensureCorrectFlags() = mutableListOf<String>().run {
        if (dayInput != null) add("--day")
        if (yesterdayFlag) add("--yesterday")
        if (unspecifiedFlag) add("--unspecified")

        if (size >= 2) throw MutuallyExclusiveGroupException(this)
    }

    override fun run() {
        ensureCorrectFlags()
        val dateToEntry = Json.decodeFromStream<PrayersFile>(path.inputStream())
        fun assignPrayer(day: LocalDate, prayer: Prayer, status: PrayerStatus) {
            val entry = dateToEntry[day]!!
            if (entry[prayer] != PrayerStatus.NOT_DONE && entry[prayer] != status) throw PrintMessage(
                "Cannot reassign a prayer. day: $day, prayer: $prayer from ${entry[prayer]} to $status", error = true
            )

            dateToEntry[day] = entry.copy(prayer, status)
        }

        // Add empty entries
        (dateToEntry.keys.maxOf { it }.nextDay()..today).forEach { dateToEntry[it] = Entry() }

        val defaultedToDay = dayInput ?: if (yesterdayFlag) yesterday else today
        if (prayersInput == null) {
            if (!printFlag) throw UsageError("Must include value for prayers if not printing")
            throw PrintMessage("$defaultedToDay is ${dateToEntry[defaultedToDay]}")
        }

        if (!unspecifiedFlag) prayersInput!!.forEach { (prayer, status) ->
            assignPrayer(defaultedToDay, prayer, status)
        }
        else prayersInput!!.forEach { (prayer, _) ->
            dateToEntry.firstNotNullOfOrNull { (date, entry) ->
                if (entry[prayer] == PrayerStatus.NOT_DONE) date else null
            }!!.let {
                assignPrayer(it, prayer, PrayerStatus.OFF_TIME)
            }
        }

        // Write back to file
        prettyJson.encodeToStream(dateToEntry, path.outputStream())
    }
}

fun main(args: Array<String>) = Hello().main(args)