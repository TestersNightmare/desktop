package app.desktop.ui.preferences.destinations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.desktop.preferences.getAdapter
import app.desktop.preferences.preferenceManager
import app.desktop.preferences2.preferenceManager2
import app.desktop.ui.preferences.LocalIsExpandedScreen
import app.desktop.ui.preferences.components.controls.SliderPreference
import app.desktop.ui.preferences.components.controls.SwitchPreference
import app.desktop.ui.preferences.components.layout.ExpandAndShrink
import app.desktop.ui.preferences.components.layout.PreferenceGroup
import app.desktop.ui.preferences.components.layout.PreferenceLayout
import com.android.launcher3.R

@Composable
fun ExperimentalFeaturesPreferences(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()
    PreferenceLayout(
        label = stringResource(id = R.string.experimental_features_label),
        backArrowVisible = !LocalIsExpandedScreen.current,
        modifier = modifier,
    ) {
        PreferenceGroup {
            SwitchPreference(
                adapter = prefs2.enableFontSelection.getAdapter(),
                label = stringResource(id = R.string.font_picker_label),
                description = stringResource(id = R.string.font_picker_description),
            )
            SwitchPreference(
                adapter = prefs2.enableSmartspaceCalendarSelection.getAdapter(),
                label = stringResource(id = R.string.smartspace_calendar_label),
                description = stringResource(id = R.string.smartspace_calendar_description),
            )
            SwitchPreference(
                adapter = prefs.workspaceIncreaseMaxGridSize.getAdapter(),
                label = stringResource(id = R.string.workspace_increase_max_grid_size_label),
                description = stringResource(id = R.string.workspace_increase_max_grid_size_description),
            )
            SwitchPreference(
                adapter = prefs2.alwaysReloadIcons.getAdapter(),
                label = stringResource(id = R.string.always_reload_icons_label),
                description = stringResource(id = R.string.always_reload_icons_description),
            )

            val enableWallpaperBlur = prefs.enableWallpaperBlur.getAdapter()

            SwitchPreference(
                adapter = enableWallpaperBlur,
                label = stringResource(id = R.string.wallpaper_blur),
            )
            ExpandAndShrink(visible = enableWallpaperBlur.state.value) {
                SliderPreference(
                    label = stringResource(id = R.string.wallpaper_background_blur),
                    adapter = prefs.wallpaperBlur.getAdapter(),
                    step = 5,
                    valueRange = 0..100,
                    showUnit = "%",
                )
            }
            ExpandAndShrink(visible = enableWallpaperBlur.state.value) {
                SliderPreference(
                    label = stringResource(id = R.string.wallpaper_background_blur_factor),
                    adapter = prefs.wallpaperBlurFactorThreshold.getAdapter(),
                    step = 1F,
                    valueRange = 0F..10F,
                )
            }
        }
    }
}
