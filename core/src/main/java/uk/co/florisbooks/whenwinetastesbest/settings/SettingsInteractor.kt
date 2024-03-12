package uk.co.florisbooks.whenwinetastesbest.settings

import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import uk.co.florisbooks.whenwinetastesbest.BillingClientWrapper
import uk.co.florisbooks.whenwinetastesbest.InAppProduct
import uk.co.florisbooks.whenwinetastesbest.Interactor

class SettingsInteractor(
    val settings: SettingsRepo,
    private val billingClientWrapper: BillingClientWrapper,
    scheduler: Scheduler
) : Interactor {

    private var billingSubscription: Disposable? = null

    private var timeZoneSubscription: Disposable? = null

    val viewModelUpdates: BehaviorSubject<SettingsViewModel> =
        BehaviorSubject.createDefault(createViewModel())

    init {
        billingSubscription = billingClientWrapper.purchaseUpdates
            .subscribeOn(scheduler)
            .subscribe {
                viewModelUpdates.value
                    ?.copy(items = billingClientWrapper.availablePurchaseItems)
                    ?.let(viewModelUpdates::onNext)
            }

        timeZoneSubscription = settings.timeZoneUpdates.subscribe {
            createViewModel().let(viewModelUpdates::onNext)
        }
    }

    override fun onReset() {
        billingSubscription?.dispose()
        timeZoneSubscription?.dispose()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(
            is24HourTime = settings.use24HourFormat,
            items = billingClientWrapper.availablePurchaseItems
        )
    }

    fun onTimeModeToggled() {
        updateViewModel { it.copy(is24HourTime = !it.is24HourTime) }
        settings.use24HourFormat = viewModelUpdates.value?.is24HourTime ?: false
    }

    private fun updateViewModel(updateAction: (SettingsViewModel) -> SettingsViewModel) {
        viewModelUpdates
            .value
            ?.let(updateAction::invoke)
            ?.let(viewModelUpdates::onNext)
    }

}

data class SettingsViewModel(
    val is24HourTime: Boolean,
    val items: List<InAppProduct> = emptyList()
)