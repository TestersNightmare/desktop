package app.desktop.data.iconoverride

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.desktop.icons.IconPickerItem
import com.android.launcher3.util.ComponentKey

@Entity
data class IconOverride(
    @PrimaryKey val target: ComponentKey,
    @Embedded val iconPickerItem: IconPickerItem,
)
