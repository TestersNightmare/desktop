package app.desktop.ui.preferences.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.desktop.preferences.BasePreferenceManager
import app.desktop.preferences.getAdapter
import app.desktop.ui.preferences.LocalNavController
import app.desktop.ui.preferences.components.layout.PreferenceTemplate

@Composable
fun FontPreference(
    fontPref: BasePreferenceManager.FontPref,
    label: String,
    modifier: Modifier = Modifier,
) {
    val navController = LocalNavController.current

    PreferenceTemplate(
        title = { Text(text = label) },
        description = {
            val font = fontPref.getAdapter().state.value
            Text(
                text = font.fullDisplayName,
                fontFamily = font.composeFontFamily,
            )
        },
        modifier = modifier
            .clickable { navController.navigate(route = "fontSelection/${fontPref.key}") },
    )
}
