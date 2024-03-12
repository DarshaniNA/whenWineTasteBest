package uk.co.florisbooks.whenwinetastesbest

import io.reactivex.subjects.PublishSubject

interface BillingClientWrapper {
    val availablePurchaseItems : List<InAppProduct>
    var ownedYears: List<Int>
    val availableYears: List<Int>
    val purchaseUpdates: PublishSubject<List<Int>>
}

data class InAppProduct(val sku: String, val year: Int, val name: String, val price: String)