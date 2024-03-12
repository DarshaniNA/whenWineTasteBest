package uk.co.florisbooks.whenwinetastesbest.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import uk.co.florisbooks.whenwinetastesbest.BaseActivity
import uk.co.florisbooks.whenwinetastesbest.InteractorSupplier
import uk.co.florisbooks.whenwinetastesbest.Lifehook
import uk.co.florisbooks.whenwinetastesbest.R
import uk.co.florisbooks.whenwinetastesbest.databinding.ActivityTimezoneBinding
import javax.inject.Inject

@AndroidEntryPoint
class TimezoneActivity : BaseActivity(), TimeZoneView {

    @Inject
    lateinit var interactor: Lazy<TimeZoneInteractor>

    override val lifehooks: Array<Lifehook> get() = emptyArray()
    override val interactorCreators: Array<InteractorSupplier> get() = arrayOf(interactor::get)

    val timezoneInteractor get() = interactors?.get(TimeZoneInteractor::class)

    private lateinit var binding: ActivityTimezoneBinding

    override fun onInteractorsLoaded() {
        // Do nothing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_timezone)
        binding = ActivityTimezoneBinding.inflate(layoutInflater)

        setSupportActionBar(binding.toolbar)
        super.onCreate(savedInstanceState)

        binding.timeZoneRecyclerView.layoutManager = LinearLayoutManager(this)

        binding.timeZoneRecyclerView.adapter = object : RecyclerView.Adapter<TimezoneViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimezoneViewHolder {
                return TimezoneViewHolder(layoutInflater, parent)
            }

            override fun onBindViewHolder(holder: TimezoneViewHolder, position: Int) {
                timezoneInteractor
                        ?.timezoneForPosition(position)
                        ?.let(holder::bind)

                holder.itemView.setOnClickListener { timezoneInteractor?.onItemSelected(position, this@TimezoneActivity) }
            }

            override fun getItemCount() = timezoneInteractor?.timeZoneCount ?: 0

        }

        binding.timezoneSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                timezoneInteractor?.onTextChanged(p0, this@TimezoneActivity)
            }
        })
    }

    override fun datasetChanged() {
        binding.timeZoneRecyclerView.adapter?.notifyDataSetChanged()
    }

    override fun finishActivity() = finish()

//    companion object {
//        @JvmStatic
//        fun getLaunchIntent(context: Context) = Intent(context, TimezoneActivity::class.java)
//    }
}


