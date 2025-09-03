package app.desktop.search.algorithms.data

interface SearchCallback {
    fun onSearchLoaded(items: List<Any>)
    fun onSearchFailed(error: String)
    fun onLoading()
}
