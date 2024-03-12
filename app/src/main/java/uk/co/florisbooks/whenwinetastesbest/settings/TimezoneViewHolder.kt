package uk.co.florisbooks.whenwinetastesbest.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.co.florisbooks.whenwinetastesbest.R
import uk.co.florisbooks.whenwinetastesbest.databinding.TimezoneListItemBinding

class TimezoneViewHolder(inflater: LayoutInflater, parent: ViewGroup?) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.timezone_list_item, parent, false)) {

    private var binding: TimezoneListItemBinding

    init {
        binding = TimezoneListItemBinding.inflate( inflater )
    }

    fun bind(name: String) {
        binding.timeZoneItemName.text = name
    }
}