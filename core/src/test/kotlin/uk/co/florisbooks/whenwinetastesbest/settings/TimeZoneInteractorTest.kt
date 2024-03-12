package uk.co.florisbooks.whenwinetastesbest.settings

import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks


class TimeZoneInteractorTest {
    private lateinit var interactor: TimeZoneInteractor

    @Mock
    private lateinit var mockSettingsRepo: SettingsRepo

    @Mock
    private lateinit var mockView: TimeZoneView

    @Before
    fun setUp() {
        initMocks(this)
        interactor = TimeZoneInteractor(listOf("New York", "London", "Tokyo"), mockSettingsRepo)
    }

    @Test
    fun `On initial load return correct count`() {
        assertThat(interactor.timeZoneCount).isEqualTo(3)
    }

    @Test
    fun `On text changed update items`() {
        interactor.onTextChanged("lon", mockView)
        assertThat(interactor.timeZoneCount).isEqualTo(1)

        assertThat(interactor.timezoneForPosition(0)).isEqualTo("London")
    }

    @Test
    fun `On text changed call dataset changed`() {
        interactor.onTextChanged("lon", mockView)
        verify(mockView).datasetChanged()
    }

    @Test
    fun `On item selected change use system timezone to false`() {
        interactor.onItemSelected(position = 2, view = mockView)
        verify(mockSettingsRepo).useSystemTimezone = false
    }

    @Test
    fun `On item selected update timezone in settings`() {
        interactor.onItemSelected(position = 2, view = mockView)
        verify(mockSettingsRepo).timezone = "Tokyo"
    }

    @Test
    fun `On item selected finish activity`() {
        interactor.onItemSelected(position = 2, view = mockView)
        verify(mockView).finishActivity()
    }
}