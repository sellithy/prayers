import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.*
import kotlinx.datetime.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.nio.file.Files

val prettyJson = Json { prettyPrint = true }
val helpTexts = "help_texts.properties".asProperties
val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
val yesterday = today - DatePeriod(days = 1)

typealias PrayersFile = LinkedHashMap<LocalDate, NewEntry>

class Prayers : NoOpCliktCommand() {
    override fun aliases(): Map<String, List<String>> = mapOf(
        "p" to listOf("print"),
        "s" to listOf("set"),
        "t" to listOf("set", "today"),
        "y" to listOf("set", "yesterday"),
    )

    override fun getFormattedHelp() = """
        |${super.getFormattedHelp()}
        
        |Aliases:
        |${aliases().entries.joinToString(separator = "\n") { "  ${it.key}: ${it.value.joinToString(separator = " ")}" }}
    """.trimMargin()
}

class PrintPrayers :
    FilePathSubCommand(help = helpTexts["Print_command"], epilog = helpTexts["Print_epilog"], name = "print") {
    private val date1: LocalDate by argument(help = helpTexts["Print_date1"])
        .convert { it.toLocalDate() }.default(today)

    private val date2: LocalDate by argument(help = helpTexts["Print_date2"])
        .convert { it.toLocalDate() }
        .defaultLazy { date1 - DatePeriod(days = 6) }

    override fun run() {
        super.run()
        throw PrintMessage(
            (minOf(date1, date2)..maxOf(date1, date2))
                .joinToString(separator = "\n") { "$it: ${entryFromDate[it]!!.toFormatted()}" }
        )
    }
}

class SetPrayers : FilePathSubCommand(help = helpTexts["Set_command"], epilog = helpTexts["Set_epilog"], name = "set") {
    private val dateType by validatedTypedDate()
    private val prayers by prayers()

    @OptIn(ExperimentalSerializationApi::class)
    override fun run() {
        super.run()
        val date = dateType

        fun assignPrayer(day: LocalDate, prayer: Prayer, status: NewPrayerStatus) {
            entryFromDate[day] = entryFromDate[day]!!.copy(prayer, status)
        }

        if (date is DateType.Unspecified) {
            prayers.forEach { (prayer, _) ->
                // This only works because we know that the map is sorted by date because it's a LinkedHashMap
                entryFromDate.firstNotNullOfOrNull { (date, entry) ->
                    if (entry[prayer] == NewPrayerStatus.NotDone) date else null
                }?.let {
                    assignPrayer(it, prayer, NewPrayerStatus.OffTime)
                } ?: throw PrintMessage(
                    "Cannot Set an unspecified prayer because all past days are filled",
                    error = true
                )
            }
            return
        }

        val day = when (date) {
            is DateType.Today -> today
            is DateType.Yesterday -> yesterday
            is DateType.Specific -> date.date
            is DateType.Unspecified -> throw RuntimeException("Should not be possible")
        }
        prayers.forEach { (prayer, status) -> entryFromDate[day] = entryFromDate[day]!!.copy(prayer, status) }

        prettyJson.encodeToStream(entryFromDate, path.outputStream())
    }
}

class DeletePrayers : FilePathSubCommand(help = helpTexts["Delete_command"], name = "delete") {
    private val dateType by validatedTypedDate(name = "DATE")
    private val prayers by prayers()

    @OptIn(ExperimentalSerializationApi::class)
    override fun run() {
        super.run()
        val date = dateType

        if (date is DateType.Unspecified)
            throw BadParameterValue(date.toString(), "DATE")

        val day = when (date) {
            is DateType.Today -> today
            is DateType.Yesterday -> yesterday
            is DateType.Specific -> date.date
            is DateType.Unspecified -> throw RuntimeException("Should not be possible")
        }
        prayers.forEach { (prayer, _) ->
            entryFromDate[day] = entryFromDate[day]!!.copy(prayer, NewPrayerStatus.NotDone)
        }

        prettyJson.encodeToStream(entryFromDate, path.outputStream())
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    Prayers().subcommands(PrintPrayers(), SetPrayers(), DeletePrayers()).main(args)
//    val path = "/Users/shehabellithy/bin/test.txt"
//    val prayersFile = prettyJson.decodeFromStream<PrayersFile>(File(path).inputStream())
//    prayersFile[today] = prayersFile[today]!!.copy(Prayer.FAJR, NewPrayerStatus.Unspecified(today))
//    println(prettyJson.encodeToString(prayersFile))
}