package uk.co.florisbooks.whenwinetastesbest.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.Disposable
import uk.co.florisbooks.whenwinetastesbest.*
import uk.co.florisbooks.whenwinetastesbest.databinding.ActivitySettingsBinding
import uk.co.florisbooks.whenwinetastesbest.databinding.ProductItemBinding
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : BaseActivity() {

    @Inject
    lateinit var interactor: Lazy<SettingsInteractor>

    @Inject
    lateinit var billingFlowLauncher: BillingFlowLauncher

    private val settingsInteractor get() = interactors?.get(SettingsInteractor::class)

    override val lifehooks: Array<Lifehook> get() = emptyArray()
    override val interactorCreators: Array<InteractorSupplier> get() = arrayOf(interactor::get)

    private var subscription: Disposable? = null

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        inject { it.plus(DisplayModule()).inject(this) }
        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)

        binding.timeModeContainer.setOnClickListener { settingsInteractor?.onTimeModeToggled() }

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    override fun onInteractorsLoaded() {
        subscription = settingsInteractor?.viewModelUpdates?.subscribe { (is24HourTime, products) ->
            binding.timeModeSwitch.isChecked = is24HourTime

            binding.productListContainer.adapter =
                ProductListContainerAdapter(applicationContext, products) { product ->
                    billingFlowLauncher.launchFlowForSku(product.sku, this@SettingsActivity)
                }
        }
    }

    override fun onStop() {
        super.onStop()
        subscription?.dispose()
    }

    companion object {
        @JvmStatic
        fun getLaunchIntent(context: Context) = Intent(context, SettingsActivity::class.java)
    }
}