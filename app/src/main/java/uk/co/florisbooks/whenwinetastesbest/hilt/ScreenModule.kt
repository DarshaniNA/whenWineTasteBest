//package uk.co.florisbooks.whenwinetastesbest.hilt
//
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//
//// Tells Dagger this is a Dagger module
//// Install this module in Hilt-generated SingletonComponent
//@InstallIn(SingletonComponent::class)
//@Module
//abstract class ScreenModule {
//
//    // Makes Dagger provide SharedPreferencesStorage when a Storage type is requested
//    @Binds
//    abstract fun provideStorage(storage: SharedPreferencesStorage): Storage
//}