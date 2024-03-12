package uk.co.florisbooks.whenwinetastesbest.hilt

import dagger.Subcomponent
import uk.co.florisbooks.whenwinetastesbest.MainActivity
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsActivity
import uk.co.florisbooks.whenwinetastesbest.settings.TimezoneActivity


@Subcomponent(modules = [DisplayModule::class])
interface DisplayComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: SettingsActivity)
    fun inject(activity: TimezoneActivity)
}