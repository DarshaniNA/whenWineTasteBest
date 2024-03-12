package uk.co.florisbooks.whenwinetastesbest

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants.*
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsRepo

class DisplayInteractor(private val wineData: WineDataDataManager,
                        private val dateProvider: DateProvider,
                        private val settingsRepo: SettingsRepo,
                        billingWrapper: BillingClientWrapper) : Interactor {

    private var timeZoneUpdates: Disposable? = null
    private var purchaseUpdates: Disposable? = null

    init {
        timeZoneUpdates = settingsRepo.timeZoneUpdates.subscribe { newTimezone ->
            switchOnMode(
                    day = { onDateUpdated(it.currentDay.withZoneRetainFields(newTimezone)) },
                    week = { onDateUpdated(it.currentDay.withZoneRetainFields(newTimezone)) }
            )
        }

        purchaseUpdates = billingWrapper.purchaseUpdates.subscribe {
            switchOnMode(
                    day = { onDateUpdated(it.currentDay) },
                    week = { onDateUpdated(it.currentDay) }
            )
        }

    }

    override fun onReset() {
        timeZoneUpdates?.dispose()
        purchaseUpdates?.dispose()
    }

    private val dayFormat: DateTimeFormatter = DateTimeFormat.forPattern("EEEE")
    private val dateFormat: DateTimeFormatter = DateTimeFormat.forPattern("d MMM")
    private val twelveHourTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("h:mm aa")
    private val twentyFourHourTimeFormat: DateTimeFormatter = DateTimeFormat.forPattern("H:mm")

    private val circularDataState: BehaviorSubject<CircularViewModel> = BehaviorSubject.createDefault(createInitialViewModel())

    val circularDataUpdates: Observable<CircularViewModel> = circularDataState
            .map {
                switchOnMode(
                        day = { it.copy(is24HourTime = settingsRepo.use24HourFormat) },
                        week = { it.copy(is24HourTime = settingsRepo.use24HourFormat) }
                )
            }

    private val headerState: BehaviorSubject<HeaderState> = BehaviorSubject.createDefault(HeaderState(
            currentDay = dateProvider.now(settingsRepo.currentTimeZone).withTimeAtStartOfDay(),
            wine = (circularDataState.value as? DayViewModel)?.dayData?.values?.firstOrNull(),
            isWeekMode = false
    ))

    val headerUpdates: Observable<HeaderViewModel> = headerState.map(this::createHeaderViewModel)

    fun modeToggled() {
        switchOnMode(
                day = this::makeWeekViewModelFromDay,
                week = this::makeDayViewModelFromWeek
        ).let(circularDataState::onNext)

        HeaderState(headerState.value!!.currentDay, headerState.value!!.wine, isWeekMode = switchOnMode(
                day = { false },
                week = { true }
        ))
                .let(headerState::onNext)
    }

    fun onAdvanced() {
        onDateUpdated(switchOnMode(
                day = { it.currentDay.plusDays(1) },
                week = { it.currentDay.withDayOfWeek(MONDAY).plusWeeks(1) }
        ))
    }

    fun onReversed() {
        onDateUpdated(switchOnMode(
                day = { it.currentDay.minusDays(1) },
                week = { it.currentDay.withDayOfWeek(MONDAY).minusWeeks(1) }
        ))
    }

    fun angleChanged(angle: Double) {
        switchOnMode(
                day = { if (it.initialAngle != null) it.copy(initialAngle = null).let(circularDataState::onNext) },
                week = { if (it.initialAngle != null) it.copy(initialAngle = null).let(circularDataState::onNext) }
        )

        switchOnMode(
                day = handleDayAngleChanged(angle),
                week = handleWeekAngleChanged(angle)
        ).let(headerState::onNext)
    }

    fun onDateSetFromPicker(date: DateTime) = onDateUpdated(date.withZoneRetainFields(settingsRepo.currentTimeZone), resetDayAngle = true)

    fun onInfoButtonTapped(view: MainActivityView) = view.launchAbout()

    private fun handleDayAngleChanged(angle: Double): (DayViewModel) -> HeaderState {
        return {
            val minutesBasedOnAngle = ((angle * MINUTES_PER_DAY).toInt())
            val date = it.currentDay.plusMinutes(minutesBasedOnAngle)
            val wine = it.dayData[date.withMinuteOfHour(0)]
            HeaderState(date, wine, isWeekMode = false)
        }
    }

    private fun handleWeekAngleChanged(angle: Double): (WeekViewModel) -> HeaderState {
        return {
            val minutesBasedOnAngle = ((angle * MINUTES_PER_WEEK).toInt())
            val date = it.currentDay.withDayOfWeek(MONDAY).plusMinutes(minutesBasedOnAngle)
            val wine = it.weekData[date.withTimeAtStartOfDay()]?.get(date.withMinuteOfHour(0))
            HeaderState(date, wine, isWeekMode = true)
        }
    }

    private fun createHeaderViewModel(state: HeaderState): HeaderViewModel {
        val firstDate = if (state.isWeekMode) state.currentDay.withTimeAtStartOfDay().withDayOfWeek(MONDAY) else state.currentDay
        val endOfWeek = state.currentDay.withTimeAtStartOfDay().withDayOfWeek(SUNDAY)

        val timeFormat = if (settingsRepo.use24HourFormat) twentyFourHourTimeFormat else twelveHourTimeFormat

        return HeaderViewModel(
                dayOfWeek = dayFormat.print(firstDate),
                date = dateFormat.print(firstDate),
                time = timeFormat.print(state.currentDay).toUpperCase(),
                wine = state.wine,
                isFavourable = state.wine?.goodForDrinking ?: false,
                currentDate = state.currentDay,
                endDay = if (state.isWeekMode) endOfWeek.let(dayFormat::print) else null,
                endDate = if (state.isWeekMode) endOfWeek.let(dateFormat::print) else null
        )
    }

    private fun createInitialViewModel(): CircularViewModel {
        val startOfDay = dateProvider.now(settingsRepo.currentTimeZone).withTimeAtStartOfDay()
        return DayViewModel(
                minYear = wineData.availableYears().minOrNull() ?: 2012,
                maxYear = wineData.availableYears().maxOrNull() ?: 2017,
                currentDay = startOfDay,
                dayData = wineData.getForDay(startOfDay),
                initialAngle = 0.0
        )
    }

    private fun onDateUpdated(newDate: DateTime, resetDayAngle: Boolean = false) {
        switchOnMode(
                day = { updateDayViewModel(newDate, it, resetDayAngle) },
                week = { updateWeekViewModel(newDate, it) }
        )
    }

    private fun updateDayViewModel(newDate: DateTime, dayModel: DayViewModel, resetAngle: Boolean = false) {
        val existingDate = headerState.value!!.currentDay
        val dayData = wineData.getForDay(newDate.withTimeAtStartOfDay())
        dayModel.copy(
                currentDay = newDate.withTimeAtStartOfDay(),
                dayData = dayData,
                initialAngle = if (resetAngle) 0.5 else null
        ).let(circularDataState::onNext)

        val headerDate = if (resetAngle) newDate.withTime(12, 0, 0, 0) else adjustDayForExistingAngle(newDate, existingDate)
        HeaderState(headerDate, dayData[headerDate.withMinuteOfHour(0)], isWeekMode = false).let(headerState::onNext)
    }

    private fun adjustDayForExistingAngle(newDate: DateTime, existingDate: DateTime) =
            newDate.withHourOfDay(existingDate.hourOfDay).withMinuteOfHour(existingDate.minuteOfHour)

    private fun updateWeekViewModel(newDate: DateTime, weekModel: WeekViewModel) {
        val offsetDateBasedOnAngle = headerState.value!!.currentDay
                .let { Duration(it.atStartOfMonday(), it) }
                .let { newDate.atStartOfMonday().plus(it) }

        val wineData = wineData.getForWeek(newDate.withDayOfWeek(MONDAY).withTimeAtStartOfDay())
        weekModel.copy(
                currentDay = newDate.withTimeAtStartOfDay(),
                weekData = wineData,
                weekOverviewData = getWeekOverviewData(wineData),
                initialAngle = null
        ).let(circularDataState::onNext)

        HeaderState(currentDay = offsetDateBasedOnAngle,
                wine = wineData[offsetDateBasedOnAngle.withTimeAtStartOfDay()]?.get(offsetDateBasedOnAngle.withMinuteOfHour(0)),
                isWeekMode = true
        ).let(headerState::onNext)

    }

    private fun getWeekOverviewData(weekData: Map<DateTime, Map<DateTime, WinePeriodType?>>): List<WinePeriodType?> {
        return weekData
                .map { it.value.values.groupingBy { it }.eachCount() }
                .map { it.maxByOrNull { it.value } }
                .mapNotNull { it?.key }
    }

    private fun makeWeekViewModelFromDay(dayModel: DayViewModel): WeekViewModel {
        val initialAngle = Duration(dayModel.currentDay.withDayOfWeek(MONDAY), headerState.value!!.currentDay)
                .standardMinutes
                .toDouble() / MINUTES_PER_WEEK

        val weekData = dayModel.currentDay.withDayOfWeek(MONDAY).let(wineData::getForWeek)

        return WeekViewModel(
                minYear = dayModel.minYear,
                maxYear = dayModel.maxYear,
                currentDay = dayModel.currentDay.withTimeAtStartOfDay(),
                weekData = weekData,
                weekOverviewData = getWeekOverviewData(weekData),
                initialAngle = initialAngle
        )
    }

    private fun makeDayViewModelFromWeek(weekModel: WeekViewModel): DayViewModel {
        val dayStart = headerState.value!!.currentDay.withTimeAtStartOfDay()
        val initialAngle = Duration(dayStart, headerState.value!!.currentDay).standardMinutes.toDouble() / MINUTES_PER_DAY
        return DayViewModel(
                minYear = weekModel.minYear,
                maxYear = weekModel.maxYear,
                currentDay = dayStart,
                dayData = wineData.getForDay(dayStart),
                initialAngle = initialAngle
        )
    }

    private fun <R> switchOnMode(day: (dayModel: DayViewModel) -> R, week: (weekModel: WeekViewModel) -> R): R {
        return circularDataState.value!!.let {
            when (it) {
                is DayViewModel -> day.invoke(it)
                is WeekViewModel -> week.invoke(it)
            }
        }
    }

    fun onSettingsButtonTapped(view: MainActivityView) = view.launchSettings()
    fun onPurchaseButtonTapped(view: MainActivityView) = view.launchSettings()
}

private fun DateTime.atStartOfMonday() = withDayOfWeek(MONDAY).withTimeAtStartOfDay()

sealed class CircularViewModel

data class DayViewModel(val minYear: Int,
                        val maxYear: Int,
                        val currentDay: DateTime,
                        val dayData: Map<DateTime, WinePeriodType?> = emptyMap(),
                        val initialAngle: Double?,
                        val is24HourTime: Boolean = true) : CircularViewModel()

data class WeekViewModel(val minYear: Int,
                         val maxYear: Int,
                         val currentDay: DateTime,
                         val weekData: Map<DateTime, Map<DateTime, WinePeriodType?>>,
                         val weekOverviewData: List<WinePeriodType?>,
                         val initialAngle: Double?,
                         val is24HourTime: Boolean = true) : CircularViewModel()

data class HeaderState(val currentDay: DateTime,
                       val wine: WinePeriodType?,
                       val isWeekMode: Boolean)

data class HeaderViewModel(val dayOfWeek: String,
                           val date: String,
                           val time: String,
                           val wine: WinePeriodType?,
                           val isFavourable: Boolean,
                           val currentDate: DateTime,
                           val endDay: String?,
                           val endDate: String?)

interface DateProvider {
    fun now(currentTimeZone: DateTimeZone): DateTime
}