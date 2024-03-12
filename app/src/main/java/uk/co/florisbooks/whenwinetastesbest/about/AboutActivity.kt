package uk.co.florisbooks.whenwinetastesbest.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.core.text.HtmlCompat
import uk.co.florisbooks.whenwinetastesbest.BaseActivity
import uk.co.florisbooks.whenwinetastesbest.InteractorSupplier
import uk.co.florisbooks.whenwinetastesbest.Lifehook
import uk.co.florisbooks.whenwinetastesbest.R
import uk.co.florisbooks.whenwinetastesbest.databinding.ActivityAboutBinding

class AboutActivity : BaseActivity() {

    private lateinit var binding: ActivityAboutBinding


    override val lifehooks: Array<Lifehook> get() = emptyArray()
    override val interactorCreators: Array<InteractorSupplier> get() = emptyArray()

    override fun onInteractorsLoaded() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }

    companion object {
        @JvmStatic
        fun getLaunchIntent(context: Context) = Intent(context, AboutActivity::class.java)
    }
}

