package uk.co.florisbooks.whenwinetastesbest.settings

import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import uk.co.florisbooks.whenwinetastesbest.BillingClientWrapper
import uk.co.florisbooks.whenwinetastesbest.InAppProduct

class SettingsInteractorTest {

    private lateinit var interactor: SettingsInteractor

    @Mock
    private lateinit var mockSettingsRepo: SettingsRepo

    @Mock
    private lateinit var mockBillingWrapper: BillingClientWrapper

    @Mock
    private lateinit var mockViewModelObserver: Consumer<SettingsViewModel>

    private lateinit var stubBillingUpdates: PublishSubject<List<Int>>

    private lateinit var stubTimeZoneSettings: PublishSubject<DateTimeZone>

    @Before
    fun setUp() {
        initMocks(this)

        stubBillingUpdates = PublishSubject.create()
        whenever(mockBillingWrapper.purchaseUpdates).thenReturn(stubBillingUpdates)

        stubTimeZoneSettings = PublishSubject.create()
        whenever(mockSettingsRepo.timeZoneUpdates).thenReturn(stubTimeZoneSettings)
    }

    @Test
    fun `On create make expected view model`() {
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(false)
        val products = listOf(
            InAppProduct(sku = "year_2017", year = 2017, name = "2017 Data", price = "£0.99"),
            InAppProduct(sku = "year_2018", year = 2018, name = "2018 Data", price = "£0.99")
        )
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(products)

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, Schedulers.io())

        interactor.viewModelUpdates.subscribe(mockViewModelObserver)
        verify(mockViewModelObserver).accept(
            SettingsViewModel(
                is24HourTime = true,
                usingSystemTimeZone = false,
                items = products
            )
        )
    }

    @Test
    fun `On billing update change items available to purchase in view model`() {
        val scheduler = TestScheduler()

        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(false)
        val products = listOf(
            InAppProduct(sku = "year_2017", year = 2017, name = "2017 Data", price = "£0.99"),
            InAppProduct(sku = "year_2018", year = 2018, name = "2018 Data", price = "£0.99")
        )
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(products)

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, scheduler)
        interactor.viewModelUpdates.subscribe(mockViewModelObserver)
        scheduler.triggerActions()
        reset(mockViewModelObserver)

        val newProducts = listOf(
            InAppProduct(
                sku = "year_2017",
                year = 2017,
                name = "2017 Data",
                price = "£0.99"
            )
        )
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(newProducts)


        stubBillingUpdates.onNext(listOf(2017))


        verify(mockViewModelObserver).accept(
            SettingsViewModel(
                is24HourTime = true,
                usingSystemTimeZone = false,
                items = newProducts
            )
        )
    }

    @Test
    fun `On Time Zone change update settings`() {
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(true)
        val products = listOf(
            InAppProduct(sku = "year_2017", year = 2017, name = "2017 Data", price = "£0.99"),
            InAppProduct(sku = "year_2018", year = 2018, name = "2018 Data", price = "£0.99")
        )
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(products)

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, Schedulers.io())
        interactor.viewModelUpdates.subscribe(mockViewModelObserver)

        verify(mockViewModelObserver).accept(
            SettingsViewModel(
                is24HourTime = true,
                usingSystemTimeZone = true,
                items = products
            )
        )

        reset(mockViewModelObserver)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(false)
        stubTimeZoneSettings.onNext(DateTimeZone.UTC)

        verify(mockViewModelObserver).accept(
            SettingsViewModel(
                is24HourTime = true,
                usingSystemTimeZone = false,
                items = products
            )
        )
    }

    @Test
    fun `On time mode toggled update view model`() {
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(true)
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(emptyList())

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, Schedulers.io())
        interactor.viewModelUpdates.subscribe(mockViewModelObserver)
        reset(mockViewModelObserver)

        interactor.onTimeModeToggled()

        verify(mockViewModelObserver).accept(
            SettingsViewModel(
                is24HourTime = false,
                usingSystemTimeZone = true,
                items = emptyList()
            )
        )
    }

    @Test
    fun `On time mode toggled update settings repo`() {
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(true)
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(emptyList())

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, Schedulers.io())
        interactor.viewModelUpdates.subscribe(mockViewModelObserver)
        reset(mockViewModelObserver)

        interactor.onTimeModeToggled()

        verify(mockSettingsRepo).use24HourFormat = false
    }

    @Test
    fun `On system timezone toggled update view model`() {
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(true)
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(emptyList())

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, Schedulers.io())
        interactor.viewModelUpdates.subscribe(mockViewModelObserver)
        reset(mockViewModelObserver)

        interactor.onSystemTimezoneToggled()

        verify(mockViewModelObserver).accept(
            SettingsViewModel(
                is24HourTime = true,
                usingSystemTimeZone = false,
                items = emptyList()
            )
        )
    }

    @Test
    fun `On system timezone toggled update settings repo`() {
        whenever(mockSettingsRepo.use24HourFormat).thenReturn(true)
        whenever(mockSettingsRepo.useSystemTimezone).thenReturn(true)
        whenever(mockBillingWrapper.availablePurchaseItems).thenReturn(emptyList())

        interactor = SettingsInteractor(mockSettingsRepo, mockBillingWrapper, Schedulers.io())
        interactor.viewModelUpdates.subscribe(mockViewModelObserver)
        reset(mockViewModelObserver)

        interactor.onSystemTimezoneToggled()

        verify(mockSettingsRepo).useSystemTimezone = false
    }
}