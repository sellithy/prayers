import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

operator fun LocalDate.rangeTo(other: LocalDate) = LocalDateRange(this, other)
fun LocalDate.nextDay() = plus(DatePeriod(days = 1))

class LocalDateRange(override val start: LocalDate, override val endInclusive: LocalDate) :
    ClosedRange<LocalDate>, Iterable<LocalDate> {

    override fun iterator() = object : AbstractIterator<LocalDate>() {
        var current = start
        override fun computeNext() {
            if (current !in this@LocalDateRange) return done()
            setNext(current)
            current = current.nextDay()
        }
    }
}
