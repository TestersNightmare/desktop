package app.desktop.ui.preferences.components.search

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.desktop.hotseat.DisabledHotseat
import app.desktop.hotseat.HotseatMode
import app.desktop.hotseat.DesktopHotseat
import app.desktop.preferences.PreferenceAdapter
import app.desktop.preferences.getAdapter
import app.desktop.preferences.preferenceManager
import app.desktop.preferences2.preferenceManager2
import app.desktop.qsb.providers.QsbSearchProvider
import app.desktop.ui.preferences.components.NavigationActionPreference
import app.desktop.ui.preferences.components.colorpreference.ColorPreference
import app.desktop.ui.preferences.components.controls.ListPreference
import app.desktop.ui.preferences.components.controls.ListPreferenceEntry
import app.desktop.ui.preferences.components.controls.SliderPreference
import app.desktop.ui.preferences.components.controls.SwitchPreference
import app.desktop.ui.preferences.components.layout.ExpandAndShrink
import app.desktop.ui.preferences.components.layout.PreferenceGroup
import app.desktop.ui.preferences.components.layout.PreferenceTemplate
import app.desktop.ui.preferences.destinations.DockPreferencesPreview
import app.desktop.ui.preferences.destinations.DockRoutes
import com.android.launcher3.R

@Composable
fun DockSearchPreference(
    modifier: Modifier = Modifier,
) {
    val prefs = preferenceManager()
    val prefs2 = preferenceManager2()

    val isHotseatEnabled = prefs2.isHotseatEnabled.getAdapter()
    val hotseatModeAdapter = prefs2.hotseatMode.getAdapter()
    val themeQsbAdapter = prefs2.themedHotseatQsb.getAdapter()
    val qsbCornerAdapter = prefs.hotseatQsbCornerRadius.getAdapter()
    val qsbAlphaAdapter = prefs.hotseatQsbAlpha.getAdapter()
    val qsbHotseatStrokeWidth = prefs.hotseatQsbStrokeWidth.getAdapter()

    Crossfade(isHotseatEnabled.state.value, label = "transition", modifier = modifier) { hotseatEnabled ->
        val isDesktopHotseat = hotseatModeAdapter.state.value == DesktopHotseat
        if (hotseatEnabled) {
            Column {
                PreferenceGroup {
                    HotseatModePreference(
                        adapter = hotseatModeAdapter,
                    )
                }
                ExpandAndShrink(visible = hotseatModeAdapter.state.value != DisabledHotseat) {
                    DockPreferencesPreview()
                }
                ExpandAndShrink(visible = isDesktopHotseat) {
                    Column {
                        val hotseatQsbProviderAdapter by preferenceManager2().hotseatQsbProvider.getAdapter()
                        PreferenceGroup(
                            heading = stringResource(R.string.search_bar_settings),
                        ) {
                            NavigationActionPreference(
                                label = stringResource(R.string.search_provider),
                                destination = DockRoutes.SEARCH_PROVIDER,
                                subtitle = stringResource(
                                    id = QsbSearchProvider.values()
                                        .first { it == hotseatQsbProviderAdapter }
                                        .name,
                                ),
                            )
                        }
                        PreferenceGroup(
                            heading = stringResource(R.string.style),
                        ) {
                            SwitchPreference(
                                adapter = themeQsbAdapter,
                                label = stringResource(id = R.string.apply_accent_color_label),
                            )
                            SliderPreference(
                                label = stringResource(id = R.string.corner_radius_label),
                                adapter = qsbCornerAdapter,
                                step = 0.05F,
                                valueRange = 0F..1F,
                                showAsPercentage = true,
                            )
                            SliderPreference(
                                label = stringResource(id = R.string.qsb_hotseat_background_transparency),
                                adapter = qsbAlphaAdapter,
                                step = 5,
                                valueRange = 0..100,
                                showUnit = "%",
                            )

                            SliderPreference(
                                label = stringResource(id = R.string.qsb_hotseat_stroke_width),
                                adapter = qsbHotseatStrokeWidth,
                                step = 1f,
                                valueRange = 0f..10f,
                                showUnit = "vw",
                            )
                            ExpandAndShrink(visible = qsbHotseatStrokeWidth.state.value > 0f) {
                                ColorPreference(preference = prefs2.strokeColorStyle)
                            }
                        }
                    }
                }
            }
        } else {
            PreferenceTemplate(
                modifier = Modifier
                    .clickable {
                        isHotseatEnabled.onChange(true)
                    }
                    .padding(horizontal = 16.dp),
                title = {},
                description = {
                    Text(
                        text = stringResource(id = R.string.enable_dock_to_access_qsb_settings),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                startWidget = {
                    Icon(
                        imageVector = Icons.Rounded.TipsAndUpdates,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Composable
private fun HotseatModePreference(
    adapter: PreferenceAdapter<HotseatMode>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val entries = remember {
        HotseatMode.values().map { mode ->
            ListPreferenceEntry(
                value = mode,
                label = { stringResource(id = mode.nameResourceId) },
                enabled = mode.isAvailable(context = context),
            )
        }
    }

    ListPreference(
        adapter = adapter,
        entries = entries,
        label = stringResource(id = R.string.hotseat_mode_label),
        modifier = modifier,
    )
}
