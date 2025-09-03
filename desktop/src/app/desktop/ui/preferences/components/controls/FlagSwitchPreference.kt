package app.desktop.ui.preferences.components.controls

import androidx.compose.runtime.Composable
import app.desktop.util.hasFlag
import app.desktop.util.setFlag

@Composable
fun FlagSwitchPreference(
    flags: Int,
    setFlags: (Int) -> Unit,
    mask: Int,
    label: String,
    description: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    SwitchPreference(
        checked = flags.hasFlag(mask),
        onCheckedChange = { setFlags(flags.setFlag(mask, it)) },
        label = label,
        description = description,
        onClick = onClick,
        enabled = enabled,
    )
}
