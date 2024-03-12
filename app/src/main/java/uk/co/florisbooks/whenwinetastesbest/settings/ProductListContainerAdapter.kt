package uk.co.florisbooks.whenwinetastesbest.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import uk.co.florisbooks.whenwinetastesbest.InAppProduct
import uk.co.florisbooks.whenwinetastesbest.R
import uk.co.florisbooks.whenwinetastesbest.databinding.ProductItemBinding

typealias OnItemPressedCallback = (inAppProduct: InAppProduct) -> Unit

class ProductListContainerAdapter(
    context: Context,
    objects: List<InAppProduct>,
    private val onItemPressedCallback: OnItemPressedCallback,
) : ArrayAdapter<InAppProduct>(context, R.layout.product_item, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding: ProductItemBinding =
            if (convertView == null) {
                val inflater = LayoutInflater.from(context)
                ProductItemBinding.inflate(inflater)
            } else {
                ProductItemBinding.bind(convertView)
            }

        val product: InAppProduct = getItem(position)!!

        binding.productText.text = context.getString(R.string.buy_year_text, product.year)
        binding.productPrice.text = product.price
        binding.root.setOnClickListener {
            onItemPressedCallback.invoke(product)
        }

        return binding.root
    }
}