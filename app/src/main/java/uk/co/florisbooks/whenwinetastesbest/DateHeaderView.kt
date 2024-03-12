package uk.co.florisbooks.whenwinetastesbest

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import uk.co.florisbooks.whenwinetastesbest.databinding.DateHeaderBinding

class DateHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: DateHeaderBinding

    init {
        val inflator = LayoutInflater.from(context)
        val fieldView = inflator.inflate(R.layout.date_header, this, false)
        binding = DateHeaderBinding.inflate(inflator, this, false )
        this.addView(fieldView)
    }

    fun bind(startDay: String,
             startDate: String,
             endDay: String?,
             endDate: String? ) {
        binding.dayOfWeek.text = startDay
        binding.dateText.text = startDate

        if (endDay != null && endDate != null) {
            binding.divider.visibility = View.VISIBLE
            binding.endContainer.visibility = View.VISIBLE
            binding.endDayOfWeek.text = endDay
            binding.endDateText.text = endDate
        } else {
            binding.divider.visibility = View.GONE
            binding.endContainer.visibility = View.GONE
        }
    }

}