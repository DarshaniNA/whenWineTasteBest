package uk.co.florisbooks.whenwinetastesbest

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsRepo

class DisplayInteractorTest {

    private lateinit var interactor: DisplayInteractor

    @Mock
    private lateinit var mockDataManager: WineDataDataManager

    @Mock
    private lateinit var mockDateProvider: DateProvider

    @Mock
    private lateinit var mockCircularDataConsumer: Consumer<CircularViewModel>

    @Mock
    private lateinit var mockHeaderConsumer: Consumer<HeaderViewModel>

    @Mock
    private lateinit var mockView: MainActivityView

    @Mock
    private lateinit var mockSettingsRepo: SettingsRepo

    @Mock
    private lateinit var mockBillingWrapper: BillingClientWrapper

    private lateinit var stubTimeZoneUpdates: PublishSubject<DateTimeZone>

    private lateinit var stubBillingUpdates: PublishSubject<List<Int>>

    @Before
    fun setUp() {
        initMocks(this)
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.currentTimeZone).thenReturn(DateTimeZone.forOffsetHours(1))

        stubTimeZoneUpdates = PublishSubject.create()
        whenever(mockSettingsRepo.timeZoneUpdates).thenReturn(stubTimeZoneUpdates)

        stubBillingUpdates = PublishSubject.create()
        whenever(mockBillingWrapper.purchaseUpdates).thenReturn(stubBillingUpdates)

        setAvailableYears(listOf(2010, 2011, 2012))
    }

    @Test
    fun `On create circular data updates has correct initial view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = wineData,
                initialAngle = 0.0
        ))
    }

    @Test
    fun `On create header updates view model has correct initial view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Saturday",
                        date = "26 Aug",
                        time = "0:00",
                        wine = WinePeriodType.FRUIT,
                        isFavourable = true,
                        currentDate = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `On timezone change update circular data`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(8)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(8)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(8))))
                .thenReturn(wineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        reset(mockCircularDataConsumer)
        stubTimeZoneUpdates.onNext(DateTimeZone.forOffsetHours(8))

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(8)),
                dayData = wineData,
                initialAngle = null
        ))
    }

    @Test
    fun `On timezone change update header data`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(8)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(8)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(8))))
                .thenReturn(wineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)
        reset(mockHeaderConsumer)
        stubTimeZoneUpdates.onNext(DateTimeZone.forOffsetHours(8))

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Saturday",
                        date = "26 Aug",
                        time = "0:00",
                        wine = WinePeriodType.FRUIT,
                        isFavourable = true,
                        currentDate = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(8)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `On billing update update circular data`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        reset(mockCircularDataConsumer)
        stubBillingUpdates.onNext(listOf(2017))

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = wineData,
                initialAngle = null
        ))
    }

    @Test
    fun `On billing update update header data`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)
        reset(mockHeaderConsumer)
        stubBillingUpdates.onNext(listOf(2017))

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Saturday",
                        date = "26 Aug",
                        time = "0:00",
                        wine = WinePeriodType.FRUIT,
                        isFavourable = true,
                        currentDate = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `Change view model to week when mode toggled from day`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        interactor.modeToggled()

        verify(mockCircularDataConsumer).accept(WeekViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                weekData = weekWineData,
                weekOverviewData = listOf(WinePeriodType.ROOT),
                initialAngle = 0.7142857142857143
        ))
    }

    @Test
    fun `Change view model to day when toggled from week`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        interactor.modeToggled()
        reset(mockCircularDataConsumer)
        interactor.modeToggled()

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = wineData,
                initialAngle = 0.0
        ))
    }

    @Test
    fun `Update circular day view model when date changed from picker`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val secondWineData = mapOf(
                DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                DateTime(2017, 9, 1, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        reset(mockCircularDataConsumer)

        interactor.onDateSetFromPicker(DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1)))

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = secondWineData,
                initialAngle = 0.5
        ))
    }

    @Test
    fun `Update header model when date changed in day mode from picker`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val secondWineData = mapOf(
                DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                DateTime(2017, 9, 1, 12, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        reset(mockHeaderConsumer)

        interactor.onDateSetFromPicker(DateTime(2017, 9, 1, 0, 0, DateTimeZone.forOffsetHours(1)))

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Friday",
                        date = "1 Sep",
                        time = "12:00",
                        wine = WinePeriodType.LEAF,
                        isFavourable = false,
                        currentDate = DateTime(2017, 9, 1, 12, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `Update circular week view model when date changed from picker`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        val secondWeekWineData = mapOf(
                DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.UNFAVOURABLE,
                        DateTime(2017, 9, 2, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                        DateTime(2017, 9, 2, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 28, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWeekWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        interactor.modeToggled()

        reset(mockCircularDataConsumer)

        interactor.onDateSetFromPicker(DateTime(2017, 8, 29, 0, 0, DateTimeZone.forOffsetHours(1)))

        verify(mockCircularDataConsumer).accept(WeekViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 29, 0, 0, DateTimeZone.forOffsetHours(1)),
                weekData = secondWeekWineData,
                weekOverviewData = listOf(WinePeriodType.LEAF),
                initialAngle = null
        ))
    }

    @Test
    fun `Update header model when date changed in week mode from picker`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        val secondWeekWineData = mapOf(
                DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.UNFAVOURABLE,
                        DateTime(2017, 9, 2, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                        DateTime(2017, 9, 2, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 28, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWeekWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        interactor.modeToggled()

        reset(mockHeaderConsumer)

        interactor.onDateSetFromPicker(DateTime(2017, 8, 29, 0, 0, DateTimeZone.forOffsetHours(1)))

        verify(mockHeaderConsumer).accept(HeaderViewModel(
                dayOfWeek = "Monday",
                date = "28 Aug",
                time = "0:00",
                wine = WinePeriodType.UNFAVOURABLE,
                isFavourable = false,
                currentDate = DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)),
                endDay = "Sunday",
                endDate = "3 Sep"
        ))
    }

    @Test
    fun `On angle changed in day view model update header`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                DateTime(2017, 8, 26, 12, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FLOWER
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)
        reset(mockHeaderConsumer)

        interactor.angleChanged(0.5)

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Saturday",
                        date = "26 Aug",
                        time = "12:00",
                        wine = WinePeriodType.FLOWER,
                        isFavourable = true,
                        currentDate = DateTime(2017, 8, 26, 12, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `On angle changed in week mode update header view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 24, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 24, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 24, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 24, 12, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)
        interactor.modeToggled()

        reset(mockHeaderConsumer)

        interactor.angleChanged(0.5)

        verify(mockHeaderConsumer).accept(HeaderViewModel(
                dayOfWeek = "Monday",
                date = "21 Aug",
                time = "12:00",
                wine = WinePeriodType.ROOT,
                isFavourable = false,
                currentDate = DateTime(2017, 8, 24, 12, 0, DateTimeZone.forOffsetHours(1)),
                endDay = "Sunday",
                endDate = "27 Aug"
        ))
    }

    @Test
    fun `On advanced in day mode update circular view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val secondWineData = mapOf(
                DateTime(2017, 8, 27, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                DateTime(2017, 8, 27, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 27, 0, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        reset(mockCircularDataConsumer)

        interactor.onAdvanced()

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 27, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = secondWineData,
                initialAngle = null
        ))
    }

    @Test
    fun `On advanced in day mode update header view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val secondWineData = mapOf(
                DateTime(2017, 8, 27, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                DateTime(2017, 8, 27, 12, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 27, 0, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        reset(mockHeaderConsumer)

        interactor.onAdvanced()

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Sunday",
                        date = "27 Aug",
                        time = "0:00",
                        wine = WinePeriodType.LEAF,
                        isFavourable = false,
                        currentDate = DateTime(2017, 8, 27, 0, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `On reversed in day mode update circular view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val secondWineData = mapOf(
                DateTime(2017, 8, 25, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                DateTime(2017, 8, 25, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 25, 0, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        reset(mockCircularDataConsumer)

        interactor.onReversed()

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 25, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = secondWineData,
                initialAngle = null
        ))
    }

    @Test
    fun `On reversed in day mode update header view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        val secondWineData = mapOf(
                DateTime(2017, 8, 25, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                DateTime(2017, 8, 25, 12, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 25, 0, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        reset(mockHeaderConsumer)

        interactor.onReversed()

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Friday",
                        date = "25 Aug",
                        time = "0:00",
                        wine = WinePeriodType.LEAF,
                        isFavourable = false,
                        currentDate = DateTime(2017, 8, 25, 0, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `On advanced in week mode update circular view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        val secondWeekWineData = mapOf(
                DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.UNFAVOURABLE,
                        DateTime(2017, 9, 2, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                        DateTime(2017, 9, 2, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 28, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWeekWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        interactor.modeToggled()

        reset(mockCircularDataConsumer)

        interactor.onAdvanced()

        verify(mockCircularDataConsumer).accept(WeekViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 28, 0, 0, DateTimeZone.forOffsetHours(1)),
                weekData = secondWeekWineData,
                weekOverviewData = listOf(WinePeriodType.LEAF),
                initialAngle = null
        ))
    }

    @Test
    fun `On advanced in week mode update header view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        val secondWeekWineData = mapOf(
                DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.UNFAVOURABLE,
                        DateTime(2017, 9, 2, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                        DateTime(2017, 9, 2, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 28, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWeekWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        interactor.modeToggled()

        reset(mockHeaderConsumer)

        interactor.onAdvanced()

        verify(mockHeaderConsumer).accept(HeaderViewModel(
                dayOfWeek = "Monday",
                date = "28 Aug",
                time = "0:00",
                wine = WinePeriodType.UNFAVOURABLE,
                isFavourable = false,
                currentDate = DateTime(2017, 9, 2, 0, 0, DateTimeZone.forOffsetHours(1)),
                endDay = "Sunday",
                endDate = "3 Sep"
        ))
    }

    @Test
    fun `On reversed in week mode update circular view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        val secondWeekWineData = mapOf(
                DateTime(2017, 8, 14, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 14, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.UNFAVOURABLE,
                        DateTime(2017, 8, 14, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                        DateTime(2017, 8, 14, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 14, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWeekWineData)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        interactor.modeToggled()

        reset(mockCircularDataConsumer)

        interactor.onReversed()

        verify(mockCircularDataConsumer).accept(WeekViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 14, 0, 0, DateTimeZone.forOffsetHours(1)),
                weekData = secondWeekWineData,
                weekOverviewData = listOf(WinePeriodType.LEAF),
                initialAngle = null
        ))
    }

    @Test
    fun `On reversed in week mode update header view model`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val weekWineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                        DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT,
                        DateTime(2017, 8, 26, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 21, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(weekWineData)

        val secondWeekWineData = mapOf(
                DateTime(2017, 8, 19, 0, 0, DateTimeZone.forOffsetHours(1)) to mapOf(
                        DateTime(2017, 8, 19, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.UNFAVOURABLE,
                        DateTime(2017, 8, 19, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF,
                        DateTime(2017, 8, 19, 2, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.LEAF
                )
        )
        whenever(mockDataManager.getForWeek(DateTime(2017, 8, 14, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(secondWeekWineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        interactor.modeToggled()

        reset(mockHeaderConsumer)

        interactor.onReversed()

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Monday",
                        date = "14 Aug",
                        time = "0:00",
                        wine = WinePeriodType.UNFAVOURABLE,
                        isFavourable = false,
                        currentDate = DateTime(2017, 8, 19, 0, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = "Sunday",
                        endDate = "20 Aug"
                )
        )
    }

    @Test
    fun `On about button tapped launch about screen`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()
        createInteractor()
        interactor.onInfoButtonTapped(mockView)
        verify(mockView).launchAbout()
    }

    @Test
    fun `On create header updates view model has correct initial view model when using 12 hour time`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        whenever(mockSettingsRepo.use24HourFormat).thenReturn(false)

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        createInteractor()

        interactor.headerUpdates.subscribe(mockHeaderConsumer)

        verify(mockHeaderConsumer).accept(
                HeaderViewModel(
                        dayOfWeek = "Saturday",
                        date = "26 Aug",
                        time = "12:00 AM",
                        wine = WinePeriodType.FRUIT,
                        isFavourable = true,
                        currentDate = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                        endDay = null,
                        endDate = null
                )
        )
    }

    @Test
    fun `On create circular data updates has correct initial view model when using 12 hour time`() {
        DateTime(2017, 8, 26, 11, 23, DateTimeZone.forOffsetHours(1)).stubAsNow()

        val wineData = mapOf(
                DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.FRUIT,
                DateTime(2017, 8, 26, 1, 0, DateTimeZone.forOffsetHours(1)) to WinePeriodType.ROOT
        )
        whenever(mockDataManager.getForDay(DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1))))
                .thenReturn(wineData)

        whenever(mockSettingsRepo.use24HourFormat).thenReturn(false)

        createInteractor()

        interactor.circularDataUpdates.subscribe(mockCircularDataConsumer)

        verify(mockCircularDataConsumer).accept(DayViewModel(
                minYear = 2010,
                maxYear = 2012,
                currentDay = DateTime(2017, 8, 26, 0, 0, DateTimeZone.forOffsetHours(1)),
                dayData = wineData,
                initialAngle = 0.0,
                is24HourTime = false
        ))
    }

    private fun createInteractor() {
        interactor = DisplayInteractor(mockDataManager, mockDateProvider, mockSettingsRepo, mockBillingWrapper)
    }

    private fun setAvailableYears(years: List<Int>) {
        whenever(mockDataManager.availableYears()).thenReturn(years)
    }

    private fun DateTime.stubAsNow() = this.apply {
        whenever(mockDateProvider.now(any())).thenAnswer { this }
    }
}


