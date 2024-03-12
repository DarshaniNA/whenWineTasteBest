package uk.co.florisbooks.whenwinetastesbest

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class PurchasedDataFilter(private val baseManager: WineDataDataManager,
                          private val billingClientWrapper: BillingClientWrapper,
                          private val dateProvider: DateProvider) : WineDataDataManager by baseManager {
    override fun getForDay(start: DateTime): Map<DateTime, WinePeriodType?> {
        return filterDayData(start.zone, baseManager.getForDay(start))
    }

    override fun getForWeek(start: DateTime): Map<DateTime, Map<DateTime, WinePeriodType?>> {
        return baseManager.getForWeek(start)
                .mapValues { filterDayData(start.zone, it.value) }
    }

    private fun filterDayData(zone: DateTimeZone, dayData: Map<DateTime, WinePeriodType?>): Map<DateTime, WinePeriodType?> {
        val today = dateProvider.now(zone)
        return dayData.mapValues {
            when {
                it.key.isBefore(today.plusDays(1).withTimeAtStartOfDay()) -> it.value
                it.key.year in billingClientWrapper.ownedYears -> it.value
                else -> WinePeriodType.NOT_PURCHASED
            }
        }
    }
}