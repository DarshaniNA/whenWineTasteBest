package uk.co.florisbooks.whenwinetastesbest

import android.app.Application
import com.android.billingclient.api.*
import io.mockk.*
import org.junit.Before
import org.junit.Test
import io.mockk.impl.annotations.MockK
import io.reactivex.observers.TestObserver
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat


class DefaultBillingClientWrapperTest {

    @MockK(relaxed = true)
    lateinit var mockClient: BillingClient

    @MockK(relaxed = true)
    lateinit var mockApplication: Application

    @MockK(relaxed = true)
    lateinit var mockCsvRepo: CsvDataRepo


    private lateinit var clientWrapper: DefaultBillingClientWrapper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fakeCsvYearData()
        clientWrapper = DefaultBillingClientWrapper(mockApplication, mockCsvRepo, mockClient)
    }

    @Test
    fun `starts the billing connection`() {
        verify { mockClient.startConnection(any()) }
    }

    @Test
    fun `should get available products for csv year data in the future when finished setting up`() {
        clientWrapper.onBillingSetupFinished(BillingResult())

        val query = fakeBillingApiProductResponseAndCaptureQueryMade()

        assertThat(query.skuType, equalTo("inapp"))
        assertThat(query.skusList.size, equalTo(2))
        assertThat(query.skusList.first(), equalTo("year_2120"))
        assertThat(query.skusList.last(), equalTo("year_2140"))
    }

    @Test
    fun `should add the returned products to the list of available products`() {
        clientWrapper.onBillingSetupFinished(BillingResult())

        fakeBillingApiProductResponseAndCaptureQueryMade(makeListOfSkuDetails())

        assertThat(
            clientWrapper.availablePurchaseItems.first().sku,
            equalTo("year_2120")
        )
    }

//    @Test
    fun `should store the owned years it got from the api`() {
        fakeHasPurchases(listOf(makeFakePurchase("year_2120")))
        clientWrapper.onBillingSetupFinished(BillingResult())
        assertThat(clientWrapper.ownedYears.first(), equalTo(2120))
    }

//    @Test
    fun `should override the owned years it got from the api more recent purchases`() {
        fakeHasPurchases(listOf(makeFakePurchase("year_2120")))
        clientWrapper.onBillingSetupFinished(BillingResult())
        val recentPurchaseRecords = listOf(mockk<PurchaseHistoryRecord>().apply {
            every { skus } returns arrayListOf("year_2130")
        })
        fakeHasRecentPurchasesRecords(recentPurchaseRecords)
        assertThat(clientWrapper.ownedYears.first(), equalTo(2130))
    }

//    @Test
    fun `should add new purchased years to list of owned years and signal new years`() {
        val observer =  TestObserver<List<Int>>().apply {
            clientWrapper.purchaseUpdates.subscribe(this) }
        clientWrapper.ownedYears = listOf(2100)
        clientWrapper.onPurchasesUpdated(BillingResult(), mutableListOf(makeFakePurchase("year_2140")))
        clientWrapper.purchaseUpdates.onComplete()
        assertThat(clientWrapper.ownedYears.first(), equalTo(2100))
        assertThat(clientWrapper.ownedYears.last(), equalTo(2140))
        observer.assertValue(listOf(2140))
    }

    @Test
    fun `launch billing flow`() {
        setupInitializedWithSkuFromApi(makeListOfSkuDetails("year_2120"))
        clientWrapper.launchFlowForSku("year_2120", mockk(relaxed = true))

        val slot = slot<BillingFlowParams>()
        verify { mockClient.launchBillingFlow(any(), capture(slot)) }
        assertThat(slot.captured.zzj().size, equalTo(1))
        assertThat(slot.captured.zzj().first().sku, equalTo("year_2120"))
    }

    @Test
    fun `do not launch billing flow when the sku is not known`() {
        setupInitializedWithSkuFromApi(makeListOfSkuDetails("year_2120"))
        clientWrapper.launchFlowForSku("year_2222", mockk(relaxed = true))
        verify(exactly = 0) { mockClient.launchBillingFlow(any(), any()) }
    }

    private fun setupInitializedWithSkuFromApi(skus: List<SkuDetails>) {
        clientWrapper.onBillingSetupFinished(BillingResult())
        fakeBillingApiProductResponseAndCaptureQueryMade(skus)
    }

    private fun fakeHasPurchases(purchases: List<Purchase>) {
        every {
            mockClient.queryPurchases(BillingClient.SkuType.INAPP).purchasesList
        }.returns(purchases)
    }

    private fun fakeHasRecentPurchasesRecords(purchases: List<PurchaseHistoryRecord>) {
        val slot = slot<PurchaseHistoryResponseListener>()
        verify {
            mockClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, capture(slot))
        }
        slot.captured.onPurchaseHistoryResponse(BillingResult(), purchases)
    }

    private fun fakeBillingApiProductResponseAndCaptureQueryMade(
        skus: List<SkuDetails> = emptyList()
    ): SkuDetailsParams {
        val slotResponseListener = slot<SkuDetailsResponseListener>()
        val slotQuery = slot<SkuDetailsParams>()
        verify {
            mockClient.querySkuDetailsAsync(
                capture(slotQuery),
                capture(slotResponseListener)
            )
        }
        slotResponseListener.captured.onSkuDetailsResponse(BillingResult(), skus)
        return slotQuery.captured
    }

    private fun fakeCsvYearData() {
        val futureYears = listOf(
            2000,
            2120,
            2140,
        )
        every { mockCsvRepo.availableYears }.returns(futureYears)
    }

    private fun makeListOfSkuDetails(skuValue: String = "year_2120"): List<SkuDetails> {
        return listOf(
            mockk<SkuDetails>(relaxed = true).apply {
                every { sku } returns skuValue
            })
    }

    private fun makeFakePurchase(sku: String) = mockk<Purchase>().apply {
        every { skus }.returns(arrayListOf(sku))
    }

    private fun makeFakePurchases(skus: List<String>) = mockk<Purchase>().apply {
        every { skus }.returns(skus)
    }

}