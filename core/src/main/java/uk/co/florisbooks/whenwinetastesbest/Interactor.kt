package uk.co.florisbooks.whenwinetastesbest

typealias InteractorSupplier = () -> Interactor
interface Interactor {
    fun onReset(){}
    fun onLoaded(){}
}