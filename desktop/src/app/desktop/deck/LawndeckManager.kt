package app.desktop.deck

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.util.Log
import app.desktop.DesktopLauncher
import app.desktop.launcher
import app.desktop.launcherNullable
import app.desktop.util.restartLauncher
import com.android.launcher3.InvariantDeviceProfile
import com.android.launcher3.model.ItemInstallQueue
import com.android.launcher3.model.ModelDbController
import com.android.launcher3.pm.UserCache
import com.android.launcher3.provider.RestoreDbTask
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LawndeckManager(private val context: Context) {

    private val launcher = context.launcherNullable ?: DesktopLauncher.instance?.launcher

    // 
    private val excludedPackages: Set<String> by lazy { getHiddenPackagesFromAssets(context) }

    suspend fun enableLawndeck() = withContext(Dispatchers.IO) {
        if (!backupExists("bk")) createBackup("bk")
        if (backupExists("lawndeck")) {
            restoreBackup("lawndeck")
        } else {
            addAllAppsToWorkspace()
        }
    }

    suspend fun disableLawndeck() = withContext(Dispatchers.IO) {
        if (backupExists("bk")) {
            createBackup("lawndeck")
            restoreBackup("bk")
        }
    }

    private fun createBackup(suffix: String) = runCatching {
        getDatabaseFiles(suffix).apply {
            db.copyTo(backupDb, overwrite = true)
            if (journal.exists()) journal.copyTo(backupJournal, overwrite = true)
        }
    }.onFailure { Log.e("LawndeckManager", "Failed to create backup: $suffix", it) }

    private fun restoreBackup(suffix: String) = runCatching {
        getDatabaseFiles(suffix).apply {
            backupDb.copyTo(db, overwrite = true)
            if (backupJournal.exists()) backupJournal.copyTo(journal, overwrite = true)
        }
        postRestoreActions()
    }.onFailure { Log.e("LawndeckManager", "Failed to restore backup: $suffix", it) }

    private fun getDatabaseFiles(suffix: String): DatabaseFiles {
        val idp = InvariantDeviceProfile.INSTANCE.get(context)
        val dbFile = context.getDatabasePath(idp.dbFile)
        return DatabaseFiles(
            db = dbFile,
            backupDb = File(dbFile.parent, "${suffix}_${idp.dbFile}"),
            journal = File(dbFile.parent, "${idp.dbFile}-journal"),
            backupJournal = File(dbFile.parent, "${suffix}_${idp.dbFile}-journal"),
        )
    }

    private fun backupExists(suffix: String): Boolean = getDatabaseFiles(suffix).backupDb.exists()

    private fun postRestoreActions() {
        ModelDbController(context).let { RestoreDbTask.performRestore(context, it) }
        restartLauncher(context)
    }

    private fun addAllAppsToWorkspace() {
        val launcher = launcher ?: return
        val context = launcher

        val modelDbController = ModelDbController(context)
        val db = modelDbController.db

        val existingApps = mutableSetOf<Pair<String, UserHandle>>()

        val cursor = db.query(
            "favorites",
            arrayOf("intent", "profileId"),
            "itemType = 0", // 只查应用图标
            null,
            null,
            null,
            null
        )

        val userCache = UserCache.INSTANCE.get(context)

        try {
            if (cursor.moveToFirst()) {
                do {
                    val intentStr = cursor.getString(0) ?: continue
                    val profileId = cursor.getLong(1)
                    val user = userCache.getUserForSerialNumber(profileId) ?: continue

                    val intent = try {
                        Intent.parseUri(intentStr, 0)
                    } catch (_: Exception) {
                        null
                    } ?: continue

                    val component = intent.component ?: continue
                    existingApps.add(component.packageName to user)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }

        launcher.appsView.appsStore.apps
            .sortedBy { it.title?.toString()?.lowercase(Locale.getDefault()) }
            .forEach { app ->
                val packageName = app.targetPackage
                val user = app.user
                val key = packageName to user

                // 
                if (packageName in excludedPackages) {
                    return@forEach
                }

                if (!existingApps.contains(key)) {
                    ItemInstallQueue.INSTANCE.get(context).queueItem(packageName, user)
                }
            }
    }
    
    companion object {
        // assets/hmd.txt 每行一个包名，返回Set
        fun getHiddenPackagesFromAssets(context: Context): Set<String> {
            return try {
                context.assets.open("hmd.txt").bufferedReader().useLines { lines ->
                    lines.map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .toSet()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptySet()
            }
        }
    }
    
    private data class DatabaseFiles(
        val db: File,
        val backupDb: File,
        val journal: File,
        val backupJournal: File,
    )
}

