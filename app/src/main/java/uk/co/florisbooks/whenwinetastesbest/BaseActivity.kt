package uk.co.florisbooks.whenwinetastesbest

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import kotlin.reflect.KClass

abstract class BaseActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<InteractorHolder> {

    abstract val lifehooks: Array<Lifehook>
    abstract val interactorCreators: Array<InteractorSupplier>
    var interactors: InteractorHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportLoaderManager.initLoader(1, Bundle(), this)

        lifehooks.forEach(Lifehook::onCreate)
    }

    override fun onStart() {
        super.onStart()
        lifehooks.forEach(Lifehook::onStart)
        onInteractorsLoaded()
    }

    override fun onStop() {
        super.onStop()
        lifehooks.forEach(Lifehook::onStop)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifehooks.forEach(Lifehook::onDestroy)
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<InteractorHolder> {
        return InteractorHolder(interactorCreators
                .map { it() }
                .associateBy { it::class })
                .also { interactors = it }
                .let { InteractorLoader(this, it) }
    }

    override fun onLoaderReset(loader: Loader<InteractorHolder>) = Unit

    override fun onLoadFinished(loader: Loader<InteractorHolder>, data: InteractorHolder) {
        interactors = data
        onInteractorsLoaded()
    }

    abstract fun onInteractorsLoaded()

//    fun inject(injection: (ScreenComponent) -> Unit) {
//        (application as WineApplication).appComponent.plus(ScreenModule())
//                .apply { injection.invoke(this) }
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

data class InteractorHolder(private val interactorMap: Map<KClass<out Interactor>, Interactor>) {
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Interactor> get(interactorClass: KClass<T>): T {
        return interactorMap[interactorClass] as T
    }

    fun allInteractors() = interactorMap.values
}

