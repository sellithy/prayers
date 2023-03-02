import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

sealed class DateType {
    object Yesterday : DateType()
    object Today : DateType()
    object Unspecified : DateType()
    data class Specific(val date: LocalDate) : DateType()
}

fun CliktCommand.typedDate(name: String = "") = argument(help = helpTexts["dateType"], name = name)
    .convert { it.toDateType() }

fun String.toDateType() = when (this) {
    "today", "t" -> DateType.Today
    "yesterday", "y" -> DateType.Yesterday
    "unspecified", "u" -> DateType.Unspecified
    else -> DateType.Specific(toLocalDate())
}

abstract class FilePathSubCommand(help: String = "", name: String? = null, epilog: String = "") :
    CliktCommand(help = help, name = name, epilog = epilog) {
    protected val path: File by option(
        envvar = "PRAYERS_FILE_PATH", help = helpTexts["path"], hidden = true
    ).file(mustBeReadable = true, mustBeWritable = true, canBeDir = false).required()

    lateinit var entryFromDate: PrayersFile

    @OptIn(ExperimentalSerializationApi::class)
    override fun run() {
        entryFromDate = Json.decodeFromStream(path.inputStream())
        (entryFromDate.keys.maxOf { it }.nextDay()..today).forEach { entryFromDate[it] = Entry() }
    }
}

fun CliktCommand.validatedTypedDate(name: String = "") = typedDate(name = name)
    .validate {
        if (it is DateType.Specific && it.date > today) throw PrintMessage("Cannot add a future date", error = true)
    }

fun CliktCommand.prayers(name: String = "") = argument(name = name, help = helpTexts["prayers"])
        .convert { input -> input.map { it.toPrayerPair() } }