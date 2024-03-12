package uk.co.florisbooks.whenwinetastesbest

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
open class WineApplication: Application() {

////    lateinit var appComponent: ApplicationComponent
//
//    override fun onCreate() {
//        super.onCreate()
////        appComponent = DaggerApplicationComponent
////                .builder()
////                .applicationModule(ApplicationModule(this))
////                .build()
//    }
//
//
//    // Instance of the AppComponent that will be used by all the Activities in the project
//    val appComponent: ApplicationComponent by lazy {
//        initializeComponent()
//    }
//
//    open fun initializeComponent(): ApplicationComponent {
//        // Creates an instance of AppComponent using its Factory constructor
//        // We pass the applicationContext that will be used as Context in the graph
//        return DaggerAppComponent.factory().create(applicationContext)
//    }

}