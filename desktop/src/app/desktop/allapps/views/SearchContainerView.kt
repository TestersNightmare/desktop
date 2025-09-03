package app.desktop.allapps.views

import android.content.Context
import android.util.AttributeSet
import app.desktop.search.DesktopSearchUiDelegate
import com.android.launcher3.allapps.LauncherAllAppsContainerView

class SearchContainerView(context: Context?, attrs: AttributeSet?) : LauncherAllAppsContainerView(context, attrs) {

    override fun createSearchUiDelegate() = DesktopSearchUiDelegate(this)
}
