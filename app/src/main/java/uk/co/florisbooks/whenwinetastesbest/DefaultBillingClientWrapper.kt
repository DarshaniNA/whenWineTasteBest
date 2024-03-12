package uk.co.florisbooks.whenwinetastesbest

import android.app.Activity
import android.app.Application
import com.android.billingclient.api.*
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTime


class DefaultBillingClientWrapper(
    application: Application,
    private val csvRepo: CsvDataRepo,
    billingClient: BillingClient? = null,
) : PurchasesUpdatedListener,
    BillingClientStateListener,
    BillingClientWrapper,
    BillingFlowLauncher {

    override val purchaseUpdates: PublishSubject<List<Int>> = PublishSubject.create()

    override var ownedYears = emptyList<Int>()
    override val availableYears: List<Int> get() = availableProducts.map { it.year }
    override val availablePurchaseItems: List<InAppProduct>
        get() = availableProducts.filter { it.year !in ownedYears }

    private val client = billingClient ?: BillingClient
        .newBuilder(application)
        .enablePendingPurchases()
        .setListener(this)
        .build()

    private var listOfSkuDetails: List<SkuDetails> = emptyList()
    private var availableProducts: List<InAppProduct> = emptyList()

    private val availableSkus
        get() = csvRepo.availableYears
            .filter { it >= DateTime.now().year }
            .map { "$SKU_PREFIX$it" }

    init {
        client.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        fetchAvailableProducts()
        fetchOwnedProducts()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val newYears = purchases?.flatMap { convertSkusToYears(it.skus) } ?: emptyList()
        ownedYears = ownedYears.plus(newYears)

        purchases?.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED
        }?.forEach {
            client.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()
            ) {

            }
        }

        purchaseUpdates.onNext(newYears)
    }

    override fun launchFlowForSku(sku: String, activity: Activity) {
        listOfSkuDetails.firstOrNull { it.sku == sku }?.let { productSkuDetails ->
            BillingFlowParams
                .newBuilder()
                .setSkuDetails(productSkuDetails)
                .build()
                .let { client.launchBillingFlow(activity, it) }
        }
    }

    private fun fetchOwnedProducts() {



        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)


        client.queryPurchasesAsync(params.build(),
            PurchasesResponseListener { billingResult,
                                        list ->

                ownedYears = list.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                                 .flatMap { convertSkusToYears(it.skus) }
            })
    }

    private fun fetchAvailableProducts() {
        val query = makeSkuDetailsParams(availableSkus)
        client.querySkuDetailsAsync(query) { _, skuDetailsList ->
            listOfSkuDetails = skuDetailsList ?: emptyList()
            availableProducts = skuDetailsList
                ?.map { InAppProduct(it.sku, convertSkuToYear(it.sku), it.title, it.price) }
                ?: emptyList()
        }
    }

    private fun makeSkuDetailsParams(years: List<String>) = SkuDetailsParams.newBuilder()
        .setSkusList(years)
        .setType(BillingClient.SkuType.INAPP)
        .build()

    private fun convertSkusToYears(skus: List<String>) = skus.map { convertSkuToYear(it) }
    private fun convertSkuToYear(sku: String) = sku.removePrefix(SKU_PREFIX).toInt()

    override fun onBillingServiceDisconnected() = Unit

    companion object {
        var SKU_PREFIX = "year_"
    }
}

interface BillingFlowLauncher {
    fun launchFlowForSku(sku: String, activity: Activity)
}

