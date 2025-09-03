package app.desktop.gestures.handlers

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.UserHandle
import androidx.core.content.getSystemService
import app.desktop.DesktopLauncher
import app.desktop.util.ComponentKeySerializer
import app.desktop.util.IntentSerializer
import app.desktop.util.UserHandlerSerializer
import com.android.launcher3.util.ComponentKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OpenAppGestureHandler(
    context: Context,
    private val target: OpenAppTarget,
) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: DesktopLauncher) {
        when (target) {
            is OpenAppTarget.App -> {
                val key = target.key
                launcher.getSystemService<LauncherApps>()?.startMainActivity(
                    key.componentName,
                    key.user,
                    null,
                    null,
                )
            }
            is OpenAppTarget.Shortcut -> Unit
        }
    }
}

@Serializable
sealed class OpenAppTarget {
    @Serializable
    @SerialName("app")
    data class App(
        @Serializable(ComponentKeySerializer::class) val key: ComponentKey,
    ) : OpenAppTarget()

    @Serializable
    @SerialName("shortcut")
    data class Shortcut(
        @Serializable(IntentSerializer::class) val intent: Intent,
        @Serializable(UserHandlerSerializer::class) val user: UserHandle,
        val packageName: String,
        val id: String,
    ) : OpenAppTarget()
}
