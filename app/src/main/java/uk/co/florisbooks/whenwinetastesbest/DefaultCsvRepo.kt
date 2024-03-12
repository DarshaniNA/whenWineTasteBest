package uk.co.florisbooks.whenwinetastesbest

import android.app.Application

class DefaultCsvRepo(val application: Application) : CsvDataRepo {

    val r = Regex("wine-data-(\\d+)")

    override val availableYears: List<Int> =
            application.assets
                    .list("")
                    .orEmpty()
                    .map { r.find(it) }
                    .mapNotNull { it?.groups?.get(1)?.value }
                    .map { it.toInt() }

    override fun get(year: Int): String? {
        return application
                .assets
                ?.takeIf { availableYears.contains(year) }
                ?.open("wine-data-$year.csv")
                ?.bufferedReader()
                ?.use { it.readText() }
    }

}