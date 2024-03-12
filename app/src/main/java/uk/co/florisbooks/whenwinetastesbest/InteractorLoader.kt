package uk.co.florisbooks.whenwinetastesbest

import android.content.Context
import androidx.loader.content.Loader

class InteractorLoader(context: Context, private val interactors: InteractorHolder) : Loader<InteractorHolder>(context) {

    override fun onStartLoading() {
        deliverResult(interactors)
        interactors.allInteractors().forEach { it.onLoaded() }
    }

    override fun onReset() {
        interactors.allInteractors().forEach { it.onReset() }
    }
}