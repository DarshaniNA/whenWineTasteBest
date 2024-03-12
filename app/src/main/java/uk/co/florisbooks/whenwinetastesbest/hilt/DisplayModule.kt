package uk.co.florisbooks.whenwinetastesbest.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import org.joda.time.DateTimeZone
import uk.co.florisbooks.whenwinetastesbest.*
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsInteractor
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsRepo
import uk.co.florisbooks.whenwinetastesbest.settings.TimeZoneInteractor


@Module
@InstallIn(ActivityComponent::class)
class DisplayModule {

    @Provides
    fun providesSettingInteractor(repo: SettingsRepo, billingClient: BillingClientWrapper): SettingsInteractor {
        return SettingsInteractor(repo, billingClient, AndroidSchedulers.mainThread())
    }

    @Provides
    fun providesTimeZoneInteractor(repo: SettingsRepo): TimeZoneInteractor {
        return TimeZoneInteractor(
            timezoneIds = DateTimeZone.getAvailableIDs().toList(),
            settingsRepo = repo
        )
    }

    @Provides
    fun providesDisplayInteractor(dateManager: WineDataDataManager,
                                  repo: SettingsRepo,
                                  billingWrapper: BillingClientWrapper,
                                  dateProvider: DateProvider ): DisplayInteractor {
        return DisplayInteractor(dateManager, dateProvider, repo, billingWrapper)
    }
}