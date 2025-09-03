package app.desktop.gestures.handlers

import android.content.Context
import app.desktop.DesktopLauncher

class OpenAppSearchGestureHandler(context: Context) : OpenAppDrawerGestureHandler(context) {

    override suspend fun onTrigger(launcher: DesktopLauncher) {
        super.onTrigger(launcher)
        launcher.appsView.searchUiManager.editText?.showKeyboard()
    }
}
