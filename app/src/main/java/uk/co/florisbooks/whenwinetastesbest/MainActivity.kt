package uk.co.florisbooks.whenwinetastesbest

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewTreeObserver
import android.view.animation.*
import android.widget.DatePicker
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import uk.co.florisbooks.whenwinetastesbest.about.AboutActivity
import uk.co.florisbooks.whenwinetastesbest.databinding.ActivityMainBinding
import uk.co.florisbooks.whenwinetastesbest.extensions.getScaledSize
import uk.co.florisbooks.whenwinetastesbest.settings.*
import javax.inject.Inject
import dagger.Lazy

@AndroidEntryPoint
class MainActivity : BaseActivity(), DatePickerDialog.OnDateSetListener, MainActivityView {

    @Inject
    lateinit var interactor: Lazy<DisplayInteractor>

    val displayInteractor get() = interactors?.get(DisplayInteractor::class)

    override val interactorCreators: Array<InteractorSupplier> get() = arrayOf(interactor::get)
    override val lifehooks: Array<Lifehook> get() = emptyArray()

    private var dayUpdateSubscription: Disposable? = null
    private var detailsUpdateSubscription: Disposable? = null

    private var dialog: DatePickerDialog? = null

    private val fruitIcons get() = listOf(binding.imgFruitLeft, binding.imgFruitRight)

    private var wineGlassFilled = false

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
//        inject { it.plus(DisplayModule()).inject(this) }
        setContentView(R.layout.activity_main)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.circular.mListener = CircleScrollListener()
        binding.weekCircular.mListener = CircleScrollListener()

        binding.circular.isActive = true
        binding.weekCircular.isActive = false

        binding.modeTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabSelected(tab: TabLayout.Tab) {
                displayInteractor?.modeToggled()
                when (tab.position) {
                    1 -> {
                        createAnimation(fromScale = 1.0f, toScale = 0.4f, fromOpacity = 1f, toOpacity = 0f)
                                .let(binding.circular::startAnimation)

                        createAnimation(fromScale = 2.0f, toScale = 1.0f, fromOpacity = 0.0f, toOpacity = 1f)
                                .let(binding.weekCircular::startAnimation)

                        binding.weekCircular.isActive = true
                        binding.circular.isActive = false
                    }
                    else -> {
                        createAnimation(fromScale = 0.4f, toScale = 1.0f, fromOpacity = 0f, toOpacity = 1f)
                                .let(binding.circular::startAnimation)

                        createAnimation(fromScale = 1.0f, toScale = 2.0f, fromOpacity = 1f, toOpacity = 0f)
                                .let(binding.weekCircular::startAnimation)

                        binding.weekCircular.isActive = false
                        binding.circular.isActive = true
                    }
                }
            }
        })

        binding.leftChevron.setOnClickListener { displayInteractor?.onReversed() }
        binding.rightChevron.setOnClickListener { displayInteractor?.onAdvanced() }

        binding.infoIcon.setOnClickListener {
            displayInteractor?.onInfoButtonTapped(this)
        }

        binding.settingsIcon.setOnClickListener {
            displayInteractor?.onSettingsButtonTapped(this)
        }

        binding.purchaseButton.setOnClickListener {
            displayInteractor?.onPurchaseButtonTapped(this)
        }

        setupFruitIcons()
        adjustProgressBarSize()
    }

    private fun adjustProgressBarSize() {
        binding.wineGlass.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {

            override fun onPreDraw(): Boolean {
                val (width, height) = binding.wineGlass.getScaledSize()
                binding.vprogressbar.layoutParams = FrameLayout.LayoutParams(width - 5, height - 5, Gravity.CENTER)
                binding.vprogressbar.setPadding(0, (height * 0.1).toInt(), 0, 0)
                binding.wineGlass.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }

        })
    }

    override fun launchAbout() = AboutActivity.getLaunchIntent(this).let(this::startActivity)

    override fun launchSettings() = SettingsActivity.getLaunchIntent(this).let(this::startActivity)

    private fun setupFruitIcons() {
        binding.imgFruitLeft.setFactory(createSymbolImageViewFactory())
        binding.imgFruitRight.setFactory(createSymbolImageViewFactory(scaleX = -1f))

        fruitIcons.forEach {
            it.setInAnimation(this, android.R.anim.fade_in)
            it.setOutAnimation(this, android.R.anim.fade_out)
        }
    }

    private fun createSymbolImageViewFactory(scaleX: Float? = null): () -> ImageView {
        return {
            ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                scaleX?.let { this.scaleX = it }
            }
        }
    }

    private fun createAnimation(fromScale: Float, toScale: Float, fromOpacity: Float, toOpacity: Float): AnimationSet {
        return AnimationSet(this, null)
                .apply { addAnimation(ScaleAnimation(fromScale, toScale, fromScale, toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)) }
                .apply { addAnimation(AlphaAnimation(fromOpacity, toOpacity)) }
                .apply { interpolator = DecelerateInterpolator() }
                .apply { fillAfter = true }
                .apply { duration = 400 }
    }

    override fun onInteractorsLoaded() {
        dayUpdateSubscription = interactors?.get(DisplayInteractor::class)
                ?.circularDataUpdates
                ?.returnOnUiThread()
                ?.subscribe(this::onNewModel)

        detailsUpdateSubscription = displayInteractor
                ?.headerUpdates
                ?.returnOnUiThread()
                ?.subscribe { (dayOfWeek, date, time, wine, isFavourable, _, endDay, endDate) ->

                    if (wine == WinePeriodType.NOT_PURCHASED) {
                        binding.upsellPanel.visibility = View.VISIBLE
                    } else {
                        binding.upsellPanel.visibility = View.INVISIBLE
                        binding.dateHeader.bind(startDay = dayOfWeek, startDate = date, endDay = endDay, endDate = endDate)
                        binding.winePeriodTypeText.text = wine?.name
                        binding.timeText.text = time
                        changeWineGlass(isFavourable)
                        changeFavourableText(isFavourable)
                        changeFruitImage(wine)
                    }

                }
    }

    private fun changeFavourableText(favourable: Boolean) {
        binding.txtFavourable.text = if (favourable)
            getString(R.string.yes)
        else
            getString(R.string.no)
    }

    private fun changeFruitImage(wine: WinePeriodType?) {
        val drawableResource = when (wine) {
            WinePeriodType.LEAF -> R.drawable.img_leaf_left
            WinePeriodType.FLOWER -> R.drawable.img_flower_left
            WinePeriodType.FRUIT -> R.drawable.img_grapes_left
            WinePeriodType.ROOT -> R.drawable.img_roots_left
            else -> android.R.color.transparent
        }

        fruitIcons.forEach { switcher ->
            drawableResource.takeIf { switcher.tag != drawableResource }
                    ?.also { switcher.setImageResource(it) }
                    ?.also { switcher.tag = drawableResource }
        }
    }

    private fun changeWineGlass(isFavourable: Boolean) {
        if (isFavourable != wineGlassFilled) {
            fillWineGlass(if (isFavourable) 100 else 0)
            wineGlassFilled = isFavourable
        }
    }

    private fun onNewModel(viewModel: CircularViewModel) {
        when (viewModel) {
            is DayViewModel -> {
                showDatePicker(viewModel.currentDay, viewModel.minYear, viewModel.maxYear)
                binding.circular.bind(viewModel.dayData.values.toList(), viewModel.initialAngle, viewModel.is24HourTime)
            }
            is WeekViewModel -> {
                showDatePicker(viewModel.currentDay, viewModel.minYear, viewModel.maxYear)
                binding.weekCircular.bind(viewModel.weekOverviewData, viewModel.initialAngle, viewModel.is24HourTime)
            }
        }
    }

    private fun showDatePicker(date: DateTime, minYear: Int, maxYear: Int) {
        binding.datePickerIcon.setOnClickListener {
            dialog = DatePickerDialog(this, this, date.year, date.monthOfYear - 1, date.dayOfMonth)
                    .apply { datePicker.minDate = DateTime(minYear, 1, 1, 0, 0).millis }
                    .apply { datePicker.maxDate = DateTime(maxYear, 12, 31, 0, 0).millis }
            dialog?.show()
        }
    }


    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        DateTime(year, month + 1, day, 0, 0)
                .let { interactors?.get(DisplayInteractor::class)?.onDateSetFromPicker(it) }
    }

    private fun fillWineGlass(end: Int) {
        ObjectAnimator.ofInt(binding.vprogressbar, "progress", binding.vprogressbar.progress, end)
                .apply { duration = 700 }
                .apply { interpolator = DecelerateInterpolator() }
                .start()
    }

    override fun onStop() {
        super.onStop()
        dayUpdateSubscription?.dispose()
        detailsUpdateSubscription?.dispose()
        dialog?.dismiss()
    }

    inner class CircleScrollListener : CircularSlider.CircleScrollListener {
        override fun onDayAdvanced() {
            displayInteractor?.onAdvanced()
        }

        override fun onDayReversed() {
            displayInteractor?.onReversed()
        }

        override fun onAngleChanged(angle: Double) {
            displayInteractor?.angleChanged(angle)
        }
    }

}

private fun <T> Observable<T>.returnOnUiThread(): Observable<T> {
    return subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
}
//
//@Subcomponent(modules = [DisplayModule::class])
//interface DisplayComponent {
//    fun inject(activity: MainActivity)
//    fun inject(activity: SettingsActivity)
//    fun inject(activity: TimezoneActivity)
//}
