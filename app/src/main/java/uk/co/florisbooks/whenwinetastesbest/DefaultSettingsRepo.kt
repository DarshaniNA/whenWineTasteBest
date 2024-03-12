package uk.co.florisbooks.whenwinetastesbest

import android.app.Application
import android.content.Context
import io.reactivex.subjects.PublishSubject
import org.joda.time.DateTimeZone
import uk.co.florisbooks.whenwinetastesbest.settings.SettingsRepo

private const val USE_SYSTEM_TIME_KEY = "SystemTime"
private const val TIMEZONE_KEY = "Timezone"
private const val TWENTY_FOUR_HOUR_MODE_KEY = "24HourMode"

class DefaultSettingsRepo(val application: Application) : SettingsRepo {

    private val sharedPrefs get() = application.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)

    override val timeZoneUpdates: PublishSubject<DateTimeZone> = PublishSubject.create()

    private var cachedUseSystemTimeZone: Boolean? = null
    private var cachedTimezone: String? = null
    private var cachedUse24HourFormat: Boolean? = null

    override val currentTimeZone: DateTimeZone
        get() {
            return if (useSystemTimezone) {
                DateTimeZone.getDefault()
            } else {
                DateTimeZone.forID(timezone)
            }
        }

    override var useSystemTimezone: Boolean
        get() {
            return cachedUseSystemTimeZone ?: sharedPrefs.getBoolean(USE_SYSTEM_TIME_KEY, true).also { cachedUseSystemTimeZone = it }
        }
        set(value) {
            sharedPrefs.edit().putBoolean(USE_SYSTEM_TIME_KEY, value).commit()
            cachedUseSystemTimeZone = value
            timeZoneUpdates.onNext(currentTimeZone)
        }

    override var timezone: String?
        get() {
            return cachedTimezone ?: sharedPrefs.getString(TIMEZONE_KEY, null).also { cachedTimezone = it }
        }
        set(value) {
            sharedPrefs.edit().putString(TIMEZONE_KEY, value).commit()
            cachedTimezone = value
            timeZoneUpdates.onNext(DateTimeZone.forID(value))
        }

    override var use24HourFormat: Boolean
        get() {
            return cachedUse24HourFormat ?: sharedPrefs.getBoolean(TWENTY_FOUR_HOUR_MODE_KEY, true).also { cachedUse24HourFormat = it }
        }
        set(value) {
            sharedPrefs.edit().putBoolean(TWENTY_FOUR_HOUR_MODE_KEY, value).commit()
            cachedUse24HourFormat = value
        }
}