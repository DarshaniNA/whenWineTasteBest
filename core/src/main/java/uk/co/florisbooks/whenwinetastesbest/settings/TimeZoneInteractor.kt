package uk.co.florisbooks.whenwinetastesbest.settings

import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTimeZone
import uk.co.florisbooks.whenwinetastesbest.Interactor

class TimeZoneInteractor(private val timezoneIds: List<String>, private val settingsRepo: SettingsRepo) : Interactor {

    private var timezones = timezoneIds

    val timeZoneCount: Int get() = timezones.size

    fun timezoneForPosition(position: Int) = timezones[position]

    fun onTextChanged(text: CharSequence?, view: TimeZoneView) {
        timezones = timezoneIds
                .filter { it.contains(text ?: "", ignoreCase = true) }
        view.datasetChanged()
    }

    fun onItemSelected(position: Int, view: TimeZoneView) {
        settingsRepo.useSystemTimezone = false
        timezones[position]
                .let { settingsRepo.timezone = it }
        view.finishActivity()
    }

}

interface SettingsRepo {
    var useSystemTimezone: Boolean
    var timezone: String?
    var use24HourFormat: Boolean

    val timeZoneUpdates: PublishSubject<DateTimeZone>
    val currentTimeZone: DateTimeZone
}

