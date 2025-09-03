package app.desktop.gestures.handlers

import android.content.Context
import app.desktop.DesktopLauncher
import app.desktop.preferences2.PreferenceManager2
import app.desktop.qsb.LawnQsbLayout

class OpenSearchGestureHandler(context: Context) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: DesktopLauncher) {
        val prefs = PreferenceManager2.getInstance(launcher)
        val searchProvider = LawnQsbLayout.getSearchProvider(launcher, prefs)
        searchProvider.launch(launcher)
    }
}
