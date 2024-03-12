package uk.co.florisbooks.whenwinetastesbest

import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.DAYS_PER_WEEK
import org.joda.time.DateTimeConstants.HOURS_PER_DAY
import org.joda.time.DateTimeZone.UTC

interface CsvDataRepo {

    val availableYears: List<Int>

    operator fun get(year: Int): String?
}

interface WineDataDataManager {
    fun getForDay(start: DateTime): Map<DateTime, WinePeriodType?>
    fun getForWeek(start: DateTime): Map<DateTime, Map<DateTime, WinePeriodType?>>
    fun availableYears(): List<Int>
}

class DefaultWineDataManager(private val csvRepo: CsvDataRepo) : WineDataDataManager {

    private val yearData = mutableMapOf<Int, Map<DateTime, WinePeriodType>>()

    override fun availableYears() = csvRepo.availableYears

    override fun getForDay(start: DateTime): Map<DateTime, WinePeriodType?> {
        return start.withZone(UTC)
                .also(this::checkRelevantYearsLoaded)
                .let(this::convertToHoursInDay)
                .associateBy({ it.withZone(start.zone) }, this::getWineTypeForHour)
    }

    override fun getForWeek(start: DateTime): Map<DateTime, Map<DateTime, WinePeriodType?>> {
        return (0 until DAYS_PER_WEEK)
                .map(start::plusDays)
                .associateBy({ it }, this::getForDay)
    }

    private fun checkRelevantYearsLoaded(utcStart: DateTime) {
        listOf(utcStart, utcStart.plusDays(1))
                .map { it.year }
                .distinct()
                .forEach { yearData.getOrPut(it) { parseYearData(it) } }
    }

    private fun parseYearData(year: Int): Map<DateTime, WinePeriodType> {
        return csvRepo[year]
                ?.parseCsv()
                ?.flatMap { parseDayDataToHours(it, year) }
                ?.toMap()
                ?: emptyMap()
    }

    private fun parseDayDataToHours(dayData: List<String>, year: Int): List<Pair<DateTime, WinePeriodType>> {
        return parseDateFromCsvCell(dayData[0], year)
                .let(this::convertToHoursInDay)
                .zip(dayData.subList(2, 26).map(this::parseWinePeriodType))
    }

    private fun parseDateFromCsvCell(csvDate: String, year: Int): DateTime {
        return csvDate.split("-")
                .map { it.toInt() }
                .let { (day, month) -> DateTime(year, month, day, 0, 0, UTC) }
    }

    private fun convertToHoursInDay(startHour: DateTime) = (0 until HOURS_PER_DAY).map(startHour::plusHours)

    private fun getWineTypeForHour(hour: DateTime) = yearData[hour.year]?.get(hour.withMinuteOfHour(0))

    private fun parseWinePeriodType(input: String): WinePeriodType {
        return when (input) {
            "L" -> WinePeriodType.LEAF
            "F" -> WinePeriodType.FRUIT
            "W" -> WinePeriodType.FLOWER
            "R" -> WinePeriodType.ROOT
            "S" -> WinePeriodType.SPECIAL_FRUIT
            "U" -> WinePeriodType.UNFAVOURABLE
            else -> WinePeriodType.UNFAVOURABLE
        }
    }
}

private fun String.parseCsv() = replace("\r", "").split("\n").map { it.split(",") }

enum class WinePeriodType(val goodForDrinking: Boolean) {
    LEAF(false), FRUIT(true), FLOWER(true), ROOT(false), SPECIAL_FRUIT(false), UNFAVOURABLE(false), NOT_PURCHASED(false)
}