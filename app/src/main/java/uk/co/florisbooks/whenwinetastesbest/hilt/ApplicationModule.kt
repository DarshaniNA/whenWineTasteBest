package uk.co.florisbooks.whenwinetastesbest.hilt

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.florisbooks.whenwinetastesbest.*
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsRepo
import javax.inject.Named
import javax.inject.Singleton

@Suppress("unused", "HardCodedStringLiteral")
@Module
@InstallIn(SingletonComponent::class)
class ApplicationModule {

//    @Provides
//    internal fun providesApplication(): Application {
//        return wineApplication
//    }

    @Provides
    fun providesWineDataManager(@Named("base") baseManager: WineDataDataManager,
                                billingClient: BillingClientWrapper,
                                dateProvider: DateProvider
    ): WineDataDataManager {
        return PurchasedDataFilter(baseManager, billingClient, dateProvider)
    }

    @Provides
    @Named("base")
    fun providesBaseWineDataManager(csvRepo: CsvDataRepo): WineDataDataManager {
        return DefaultWineDataManager(csvRepo)
    }

    @Provides
    fun providesCsvRepo( @ApplicationContext appContext: Context ): CsvDataRepo {
        return DefaultCsvRepo(appContext as Application)
    }

    @Provides
    fun providesSettingsRepo(@ApplicationContext appContext: Context): SettingsRepo {
        return DefaultSettingsRepo(appContext as Application)
    }

    @Provides
    @Singleton
    fun providesBillingClientInstance(csvRepo: CsvDataRepo,
                                      @ApplicationContext appContext: Context): DefaultBillingClientWrapper {
        return DefaultBillingClientWrapper(appContext as Application, csvRepo)
    }

    @Provides
    @Singleton
    fun providesBillingClientWrapper(wrapper: DefaultBillingClientWrapper): BillingClientWrapper {
        return wrapper
    }

    @Provides
    fun providesBillingFlowLauncher(wrapper: DefaultBillingClientWrapper): BillingFlowLauncher {
        return wrapper
    }

    @Provides
    fun providesDateProvider(): DateProvider {
        return object : DateProvider {
            override fun now(currentTimeZone: DateTimeZone) = DateTime.now(currentTimeZone)
        }
    }
}