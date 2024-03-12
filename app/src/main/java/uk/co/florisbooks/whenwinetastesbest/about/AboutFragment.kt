package uk.co.florisbooks.whenwinetastesbest.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import uk.co.florisbooks.whenwinetastesbest.R
import uk.co.florisbooks.whenwinetastesbest.databinding.FragmentAboutBinding


class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(inflater: LayoutInflater,
                              @Nullable container: ViewGroup?,
                              @Nullable savedInstanceState: Bundle?): View {

        binding = FragmentAboutBinding.inflate(inflater)

        val htmlString = getString(R.string.about_app)
        val htmlSpanned = HtmlCompat.fromHtml(htmlString, HtmlCompat.FROM_HTML_MODE_LEGACY)

        binding.aboutText.text = htmlSpanned
        binding.aboutText.movementMethod = LinkMovementMethod.getInstance()
        binding.aboutText.linksClickable = true

        return binding.root
    }
}