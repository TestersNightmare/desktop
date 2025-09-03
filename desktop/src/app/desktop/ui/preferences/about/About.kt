/*
 * Copyright 2022, Desktop
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

package app.desktop.ui.preferences.about

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import app.desktop.preferences.preferenceManager
import app.desktop.ui.preferences.LocalIsExpandedScreen
import app.desktop.ui.preferences.components.CheckUpdate
import app.desktop.ui.preferences.components.NavigationActionPreference
import app.desktop.ui.preferences.components.controls.ClickablePreference
import app.desktop.ui.preferences.about.DesktopLink
//import app.desktop.ui.preferences.about.PreferenceGroup
import app.desktop.ui.preferences.components.layout.PreferenceGroup
import app.desktop.ui.preferences.components.layout.PreferenceLayout
import app.desktop.util.checkAndRequestFilesPermission
import com.android.launcher3.BuildConfig
import com.android.launcher3.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

private data class Link(
    @DrawableRes val iconResId: Int,
    @StringRes val labelResId: Int,
    val url: String,
)

private val links = listOf(
    Link(
        iconResId = R.drawable.ic_new_releases,
        labelResId = R.string.news,
        url = "https://t.me//+gX380iK5R25jOGZl",
    ),
    Link(
        iconResId = R.drawable.ic_help,
        labelResId = R.string.support,
        url = "https://t.me/desktop_me",
    ),
    Link(
        iconResId = R.drawable.ic_x_twitter,
        labelResId = R.string.x_twitter,
        url = "https://t.me/desktop_me",
    ),
    Link(
        iconResId = R.drawable.ic_github,
        labelResId = R.string.github,
        url = "https://t.me/desktop_me",
    ),
    Link(
        iconResId = R.drawable.ic_discord,
        labelResId = R.string.discord,
        url = "https://opencollective.com/foodondesktop",
    ),
)

object AboutRoutes {
    const val LICENSES = "licenses"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun About(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var clickCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    PreferenceLayout(
        label = stringResource(id = R.string.about_label),
        modifier = modifier,
        backArrowVisible = !LocalIsExpandedScreen.current,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_home_comp),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = {
                            clickCount++
                            if (clickCount >= 4) {
                                // ���͹㲥���� StressTestManager
                                val intent = Intent("com.android.launcher4.action.START_APP_TEST").apply {
                                    setPackage("com.android.launcher4")
                                    putExtra("duration", -1L)
                                }
                                context.sendBroadcast(intent)
                                clickCount = 0 // ���ü���
                            }
                            // 1�������ü���
                            coroutineScope.launch {
                                delay(1000)
                                clickCount = 0
                            }
                        }
                    ),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.derived_app_name),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = BuildConfig.VERSION_DISPLAY_NAME,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = {
                        val commitUrl = "https://opencollective.com/foodondesktop"
                        context.startActivity(Intent(Intent.ACTION_VIEW, commitUrl.toUri()))
                    },
                ),
            )
            if (BuildConfig.APPLICATION_ID.contains("nightly") &&
                checkAndRequestFilesPermission(
                    context,
                    preferenceManager(),
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                CheckUpdate()
            }
            Spacer(modifier = Modifier.requiredHeight(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                links.forEach { link ->
                    DesktopLink(
                        iconResId = link.iconResId,
                        label = stringResource(id = link.labelResId),
                        modifier = Modifier.weight(weight = 1f),
                        url = link.url,
                    )
                }
            }
        }
        PreferenceGroup {
            NavigationActionPreference(
                label = stringResource(id = R.string.acknowledgements),
                destination = AboutRoutes.LICENSES,
            )
            ClickablePreference(
                label = stringResource(id = R.string.translate),
                onClick = {
                    val webpage = Uri.parse(CROWDIN_URL)
                    val intent = Intent(Intent.ACTION_VIEW, webpage)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                },
            )
            ClickablePreference(
                label = stringResource(id = R.string.donate),
                onClick = {
                    val webpage = Uri.parse(OPENCOLLECTIVE_FUNDING_URL)
                    val intent = Intent(Intent.ACTION_VIEW, webpage)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    }
                },
            )
        }
    }
}

private const val OPENCOLLECTIVE_FUNDING_URL = "https://opencollective.com/foodondesktop"
private const val CROWDIN_URL = "https://t.me/desktop_me"
