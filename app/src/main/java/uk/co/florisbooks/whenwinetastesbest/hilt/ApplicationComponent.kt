//package uk.co.florisbooks.whenwinetastesbest
//
//import dagger.Component
//import dagger.Subcomponent
//import uk.co.florisbooks.whenwinetastesbest.hilt.DisplayComponent
//import uk.co.florisbooks.whenwinetastesbest.hilt.DisplayModule
//import uk.co.florisbooks.whenwinetastesbest.hilt.ScreenModule
//import javax.inject.Singleton
//
//@Singleton
//@Component(modules = [ApplicationModule::class])
//interface ApplicationComponent{
//    fun plus(screenModule: ScreenModule): ScreenComponent
//
//}
//
//@Subcomponent(modules = [ScreenModule::class])
//interface ScreenComponent {
//    fun  plus(displayModule: DisplayModule): DisplayComponent
//}
//
////@Module
////class ScreenModule