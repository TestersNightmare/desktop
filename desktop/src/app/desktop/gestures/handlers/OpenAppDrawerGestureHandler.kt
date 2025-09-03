package app.desktop.gestures.handlers

import android.content.Context
import app.desktop.DesktopLauncher
import app.desktop.animateToAllApps

open class OpenAppDrawerGestureHandler(context: Context) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: DesktopLauncher) {
        launcher.animateToAllApps()
    }
}
