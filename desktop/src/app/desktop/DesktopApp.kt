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

package app.desktop

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.desktop.backup.DesktopBackup
import app.desktop.flowerpot.Flowerpot
import app.desktop.preferences.PreferenceManager
import app.desktop.ui.ModalBottomSheetContent
import app.desktop.ui.preferences.destinations.openAppInfo
import app.desktop.util.restartLauncher
import app.desktop.util.unsafeLazy
import app.desktop.views.ComposeBottomSheet
import com.android.launcher3.BuildConfig
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.Launcher
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.quickstep.RecentsActivity
import com.android.systemui.shared.system.QuickStepContract
import app.desktop.deck.LawndeckManager
import java.io.File
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import android.app.ActivityManager
import android.os.Process
import com.thinkup.core.api.TUSDK
//import com.anythink.core.api.ATSDK
import java.io.FileOutputStream


class DesktopApp : Application() {
    private val compatible = Build.VERSION.SDK_INT in BuildConfig.QUICKSTEP_MIN_SDK..BuildConfig.QUICKSTEP_MAX_SDK
    private val isRecentsComponent: Boolean by unsafeLazy { checkRecentsComponent() }
    private val recentsEnabled: Boolean get() = compatible && isRecentsComponent
    private val isAtleastT = Utilities.ATLEAST_T

    private val appId = "h67ee47b841e7d"
    private val appKey = "ace3587ddd45c3561be5022db9b7ed53b"
    internal var accessibilityService: DesktopAccessibilityService? = null
    val isVibrateOnIconAnimation: Boolean by unsafeLazy { getSystemUiBoolean("config_vibrateOnIconAnimation", false) }

      override fun onCreate() {
        super.onCreate()
        if (isMainProcess()) {
            copyDatabaseIfFirstRun()
            instance = this
            try {
                FirebaseApp.initializeApp(this)
                val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
            } catch (e: Exception) {
                Log.w("Firebase", "初始化失败", e)
            }
            QuickStepContract.sRecentsDisabled = !recentsEnabled
            Flowerpot.Manager.getInstance(this)

            val customMap: HashMap<String, Any> = HashMap()
            customMap["channel"] = Build.MANUFACTURER
//            ATSDK.initCustomMap(customMap) //  对全部Placement有效
////        ATSDK.init(this, "a62b013be01931", "c3d0d2a9a9d451b07e62b509659f7c97");
//            ATSDK.init(this, "h67ee47b841e7d", "ace3587ddd45c3561be5022db9b7ed53b");
//            //        ATNetworkConfig atNetworkConfig = getAtNetworkConfig();
////        ATSDK.init(this, appid, appKey, atNetworkConfig);
////        ATDebuggerUITest.showDebuggerUI(this)
//            ATSDK.setNetworkLogDebug(true)
            //topon thinkup初始化
            TUSDK.init(this, appId, appKey);
        }
    }

    private fun isMainProcess(): Boolean {
        val pid = Process.myPid()
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfo = activityManager.runningAppProcesses?.firstOrNull { processInfo ->
            processInfo.pid == pid
        }
        val processName = processInfo?.processName
        return processName == packageName
    }

    private fun copyDatabaseIfFirstRun() {
        val prefs = getSharedPreferences("init", Context.MODE_PRIVATE)
        val isFirstRun = prefs.getBoolean("isFirstRun", true)

        if (!isFirstRun) {
            Log.d("DB_INIT", "已复制过数据库，跳过")
            return
        }


        try {
            // 1. 列出 assets 根目录下以 launcher 开头的 .db 文件
            val assetFiles = assets.list("") ?: emptyArray()
            val launcherDbFiles = assetFiles.filter { it.startsWith("launcher", ignoreCase = true) && it.endsWith(".db", ignoreCase = true) }

            // 2. 复制数据库文件
            if (launcherDbFiles.isEmpty()) {
                Log.w("DB_INIT", "assets 目录下没有发现以 launcher 开头的 .db 文件")
            } else {
                // 确保 databases 目录存在
                val dbDir = getDatabasePath("dummy.db").parentFile
                if (dbDir != null && !dbDir.exists()) {
                    dbDir.mkdirs()
                }

                // 逐个拷贝 launcher 开头的 .db 文件
                launcherDbFiles.forEach { fileName ->
                    val outFile = File(dbDir, fileName)
                    assets.open(fileName).use { input ->
                        FileOutputStream(outFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d("DB_INIT", "已复制数据库: $fileName → ${outFile.absolutePath}")
                }
            }

            // 3. 标记已完成
            prefs.edit().putBoolean("isFirstRun", false).apply()
            Log.d("DB_INIT", "launcher 数据库拷贝完成，标记首次运行结束")

        } catch (e: Exception) {
            Log.e("DB_INIT", "复制 launcher 数据库失败", e)
        }
    }


    fun hideClockInStatusBar() {
        if (!isRecentsEnabled) return
        try {
            val currentBlacklist = Settings.Secure.getString(contentResolver, "icon_blacklist") ?: ""
            val newBlacklist = if (currentBlacklist.contains("clock")) {
                currentBlacklist
            } else {
                "$currentBlacklist,clock"
            }
            Settings.Secure.putString(contentResolver, "icon_blacklist", newBlacklist)
        } catch (_: Exception) {
            // ignore
        }
    }

    fun restoreClockInStatusBar() {
        if (!isRecentsEnabled) return
        try {
            val currentBlacklist = Settings.Secure.getString(contentResolver, "icon_blacklist") ?: ""
            val newBlacklist = currentBlacklist.split(",").filter { it != "clock" }.joinToString(",")
            Settings.Secure.putString(contentResolver, "icon_blacklist", newBlacklist)
        } catch (_: Exception) {
        }
    }

    fun onLauncherAppStateCreated() {
        registerActivityLifecycleCallbacks(activityHandler)
    }

    fun restart(recreateLauncher: Boolean = true) {
        if (recreateLauncher) {
            activityHandler.finishAll()
        } else {
            restartLauncher(this)
        }
    }

    fun renameRestoredDb(dbName: String) {
        val restoredDbFile = getDatabasePath(DesktopBackup.RESTORED_DB_FILE_NAME)
        if (!restoredDbFile.exists()) return
        val dbFile = getDatabasePath(dbName)
        restoredDbFile.renameTo(dbFile)
    }

    fun migrateDbName(dbName: String) {
        val dbFile = getDatabasePath(dbName)
        if (dbFile.exists()) return
        val prefs = PreferenceManager.INSTANCE.get(this)
        val dbJournalFile = getJournalFile(dbFile)
        val oldDbSlot = prefs.sp.getString("pref_currentDbSlot", "a")
        val oldDbName = if (oldDbSlot == "a") "launcher.db" else "launcher.db_b"
        val oldDbFile = getDatabasePath(oldDbName)
        val oldDbJournalFile = getJournalFile(oldDbFile)
        if (oldDbFile.exists()) {
            oldDbFile.copyTo(dbFile)
            oldDbJournalFile.copyTo(dbJournalFile)
            oldDbFile.delete()
            oldDbJournalFile.delete()
        }
    }

    fun cleanUpDatabases() {
        val idp = InvariantDeviceProfile.INSTANCE.get(this)
        val dbName = idp.dbFile
        val dbFile = getDatabasePath(dbName)
        dbFile?.parentFile?.listFiles()?.forEach { file ->
            val name = file.name
            if (name.startsWith("launcher") && !name.startsWith(dbName)) {
                file.delete()
            }
        }
    }

    private fun getJournalFile(file: File): File = File(file.parentFile, "${file.name}-journal")

    private fun getSystemUiBoolean(resName: String, fallback: Boolean): Boolean {
        val systemUiPackage = "com.android.systemui"
        val res = packageManager.getResourcesForApplication(systemUiPackage)

        @SuppressLint("DiscouragedApi")
        val resId = res.getIdentifier(resName, "bool", systemUiPackage)
        if (resId == 0) {
            return fallback
        }
        return res.getBoolean(resId)
    }

    private val activityHandler = object : ActivityLifecycleCallbacks {
        private val activities = HashSet<Activity>()
        private var foregroundActivity: Activity? = null

        fun finishAll() {
            HashSet(activities).forEach { it.finish() }
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityResumed(activity: Activity) {
            foregroundActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == foregroundActivity) foregroundActivity = null
            activities.remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(activity)
        }
    }

    private fun checkRecentsComponent(): Boolean {
        @SuppressLint("DiscouragedApi")
        val resId = resources.getIdentifier("config_recentsComponentName", "string", "android")
        if (resId == 0) {
            Log.d(TAG, "config_recentsComponentName not found, disabling recents")
            return false
        }

        val recentsComponent = ComponentName.unflattenFromString(resources.getString(resId))
        if (recentsComponent == null) {
            Log.d(TAG, "config_recentsComponentName is empty, disabling recents")
            return false
        }

        val isRecentsComponent = recentsComponent.packageName == packageName &&
            recentsComponent.className == RecentsActivity::class.java.name
        if (!isRecentsComponent) {
            Log.d(TAG, "config_recentsComponentName ($recentsComponent) is not Desktop, disabling recents")
            return false
        }

        return true
    }

    fun isAccessibilityServiceBound(): Boolean = accessibilityService != null

    fun performGlobalAction(action: Int): Boolean {
        return accessibilityService?.performGlobalAction(action) ?: run {
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(::startActivity)
            false
        }
    }

    companion object {
        private const val TAG = "DesktopApp"

        @JvmStatic
        lateinit var instance: DesktopApp
            private set

        @JvmStatic
        val isRecentsEnabled: Boolean get() = instance.recentsEnabled

        @JvmStatic
        val isAtleastT: Boolean get() = instance.isAtleastT

        fun Launcher.showQuickstepWarningIfNecessary() {
            val launcher = this
            if (!desktopApp.isRecentsComponent || isRecentsEnabled) return
            ComposeBottomSheet.show(this) {
                ModalBottomSheetContent(
                    title = { Text(text = stringResource(id = R.string.quickstep_incompatible)) },
                    text = {
                        val description = stringResource(
                            id = R.string.quickstep_incompatible_description,
                            stringResource(id = R.string.derived_app_name),
                            Build.VERSION.RELEASE,
                        )
                        Text(text = description)
                    },
                    buttons = {
                        OutlinedButton(
                            onClick = {
                                openAppInfo(launcher)
                                close(true)
                            },
                        ) {
                            Text(text = stringResource(id = R.string.app_info_drop_target_label))
                        }
                        Spacer(modifier = Modifier.requiredWidth(8.dp))
                        Button(
                            onClick = { close(true) },
                        ) {
                            Text(text = stringResource(id = android.R.string.ok))
                        }
                    },
                )
            }
        }

        fun getUriForFile(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        }
    }
}

val Context.desktopApp get() = applicationContext as DesktopApp
