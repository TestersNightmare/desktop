/*
 * Copyright 2021, Desktop
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.desktop.gestures.handlers

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import app.desktop.DesktopLauncher
import app.desktop.desktopApp
import app.desktop.views.ComposeBottomSheet
import com.android.launcher3.R

class RecentsGestureHandler(context: Context) : GestureHandler(context) {

    override suspend fun onTrigger(launcher: DesktopLauncher) {
        val app = launcher.desktopApp
        if (!app.isAccessibilityServiceBound()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ComposeBottomSheet.show(launcher) {
                ServiceWarningDialog(
                    title = R.string.d2ts_recents_a11y_hint_title,
                    description = R.string.recents_a11y_hint,
                    settingsIntent = intent,
                ) { close(true) }
            }
            return
        }
        app.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
    }
}
