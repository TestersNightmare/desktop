package com.android.launcher3.util

import android.content.*
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class StressTestManager(private val context: Context) {
    private var testJob: Job? = null
    private var clickJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private val testScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val logFile: File
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var initialBatteryLevel: Int = 0 // 任务开始时的电量
    private var taskStartTime: Long = 0 // 任务开始时间

    // 广播接收器，用于处理测试开始和停止
    private val testReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Toast.makeText(context, "收到广播: ${intent?.action}", Toast.LENGTH_SHORT).show()
            when (intent.action) {
                "com.android.launcher4.action.START_APP_TEST" -> {
                    val duration = intent.getLongExtra("duration", -1L)
                    val durationText = if (duration == -1L) "无限制" else "${duration / (60 * 60 * 1000)} 小时"
                    Toast.makeText(context, "测试开始，时长：$durationText", Toast.LENGTH_LONG).show()
                    startAppTesting(duration)
                }
                "com.android.launcher4.action.STOP_APP_TEST" -> {
                    stopAppTesting()
                    Toast.makeText(context, "测试已停止", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun appendLog(context: Context, event: String, success: Boolean) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val logFile = File("/sdcard/log_${getSerialNo(context)}.txt")
            val time = dateFormat.format(Date())
            val battery = getBatteryLevel(context)
            val status = if (success) "启动成功" else "启动失败"
            val log = "$time | $event | $status | 剩余电量: $battery%\n"
            try {
                logFile.appendText(log)
            } catch (e: Exception) {
                try {
                    val process = Runtime.getRuntime().exec("su")
                    val writer = process.outputStream.bufferedWriter()
                    writer.write("echo '$log' >> ${logFile.absolutePath}\n")
                    writer.flush()
                    writer.close()
                    process.waitFor()
                } catch (e2: Exception) {
                    Log.e("StressTestManager", "日志写入失败: ${e2.message}")
                }
            }
        }

        private fun getBatteryLevel(context: Context): Int {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }

        private fun getSerialNo(context: Context): String {
            return try {
                val process = Runtime.getRuntime().exec("su -c getprop ro.serialno")
                val reader = process.inputStream.bufferedReader()
                val serial = reader.readLine()?.trim() ?: "unknown"
                reader.close()
                process.waitFor()
                serial
            } catch (e: Exception) {
                Log.e("StressTestManager", "获取序列号失败: ${e.message}")
                "unknown"
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction("com.android.launcher4.action.START_APP_TEST")
            addAction("com.android.launcher4.action.STOP_APP_TEST")
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(testReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(testReceiver, filter)
        }
        val serialNo = try {
            val process = Runtime.getRuntime().exec("su -c getprop ro.serialno")
            val reader = process.inputStream.bufferedReader()
            val serial = reader.readLine()?.trim() ?: "unknown"
            reader.close()
            process.waitFor()
            serial
        } catch (e: Exception) {
            Log.e("StressTestManager", "获取序列号失败: ${e.message}")
            "unknown"
        }
        logFile = File("/sdcard/log_$serialNo.txt")
    }

    // -------------------- 保持屏幕常亮 --------------------
    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld == true) return
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "launcher:StressTestWakeLock"
            )
            wakeLock?.acquire()
        } catch (_: Exception) {}
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { if (it.isHeld) it.release() }
        } catch (_: Exception) {}
        wakeLock = null
    }

    // -------------------- 添加到白名单 --------------------
    private fun addToWhitelist() {
        // MIUI 性能保护白名单
        try {
            val process1 = Runtime.getRuntime().exec("su")
            val writer1 = process1.outputStream.bufferedWriter()
            writer1.write("settings put system perf_proc_protect_list \"com.android.launcher4\"\n")
            writer1.flush()
            writer1.close()
            process1.waitFor()
        } catch (_: Exception) {}

        // 禁用 MIUI 优化
        try {
            val process2 = Runtime.getRuntime().exec("su")
            val writer2 = process2.outputStream.bufferedWriter()
            writer2.write("settings put global miui_optimization 0\n")
            writer2.flush()
            writer2.close()
            process2.waitFor()
        } catch (_: Exception) {}

        // 电池优化白名单
        try {
            val process3 = Runtime.getRuntime().exec("su")
            val writer3 = process3.outputStream.bufferedWriter()
            writer3.write("dumpsys deviceidle whitelist +com.android.launcher4\n")
            writer3.flush()
            writer3.close()
            process3.waitFor()
        } catch (_: Exception) {}
    }

    // -------------------- 设置屏幕亮度为 50% --------------------
    private fun setScreenBrightness() {
        try {
            val process = Runtime.getRuntime().exec("su")
            val writer = process.outputStream.bufferedWriter()
            // 禁用自动亮度
            writer.write("settings put system screen_brightness_mode 0\n")
            // 设置亮度为 50% (0-255 范围，128 约为 50%)
            writer.write("settings put system screen_brightness 128\n")
            writer.flush()
            writer.close()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Log.i("StressTestManager", "屏幕亮度设置为 50% 成功")
                appendLog("屏幕亮度设置", true)
            } else {
                Log.w("StressTestManager", "屏幕亮度设置失败，退出码: $exitCode")
                appendLog("屏幕亮度设置", false)
            }
        } catch (e: Exception) {
            Log.e("StressTestManager", "屏幕亮度设置失败: ${e.message}")
            appendLog("屏幕亮度设置", false)
        }
    }

    // -------------------- 发送 Home 键 --------------------
    private fun sendHomeKey() {
        try {
            val process = Runtime.getRuntime().exec("su")
            val writer = process.outputStream.bufferedWriter()
            writer.write("input keyevent 3\n") // KEYCODE_HOME
            writer.flush()
            writer.close()
            process.waitFor()
        } catch (_: Exception) {}
    }

    // -------------------- 获取当前顶层应用的包名 --------------------
    private fun getTopPackageName(): String {
        try {
            val process = Runtime.getRuntime().exec("su -c dumpsys activity activities")
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            reader.close()
            process.waitFor()
            // 查找 mResumedActivity 或 mFocusedActivity
            val regex = Regex("mResumedActivity:.*\\{.* (\\S+)/.*\\}|^ *mFocusedActivity:.*\\{.* (\\S+)/.*\\}")
            val match = regex.find(output)
            return match?.groups?.get(1)?.value ?: match?.groups?.get(2)?.value ?: ""
        } catch (e: Exception) {
            Log.e("StressTestManager", "获取顶层包名失败: ${e.message}")
            return ""
        }
    }

    // -------------------- 获取包名和点击方式 --------------------
    private fun getPackageAndClickMode(): List<Pair<String, String>> {
        try {
            val runFile = File("/sdcard/run.txt")
            if (runFile.exists()) {
                val packageManager = context.packageManager
                val packageList = runFile.readLines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { line ->
                        val parts = line.split("\\s+".toRegex())
                        val packageName = parts[0]
                        val clickMode = if (parts.size > 1 && parts[1] == "1") "swipe" else "tap"
                        Pair(packageName, clickMode)
                    }
                val validPackages = packageList.filter { (packageName, _) ->
                    try {
                        packageManager.getApplicationInfo(packageName, 0)
                        true
                    } catch (e: PackageManager.NameNotFoundException) {
                        Log.w("StressTestManager", "Package not installed: $packageName")
                        false
                    }
                }
                if (validPackages.isEmpty()) {
                    Toast.makeText(context, "run.txt 中未找到有效包名", Toast.LENGTH_LONG).show()
                }
                return validPackages
            }
        } catch (_: Exception) {
            Log.w("StressTestManager", "无法读取 run.txt，切换到 rmd.txt")
        }
        // Fallback to rmd.txt
        return getThirdPartyApks()
    }

    // -------------------- 获取被测包名列表 --------------------
    // assets/rmd.txt 优先，没有则自动查找所有三方app
    private fun getThirdPartyApks(): List<Pair<String, String>> {
        try {
            val packageManager = context.packageManager
            val inputStream = context.assets.open("rmd.txt")
            val packageList = inputStream.bufferedReader().useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { line ->
                        val parts = line.split("\\s+".toRegex())
                        val packageName = parts[0]
                        val clickMode = if (parts.size > 1 && parts[1] == "1") "swipe" else "tap"
                        Pair(packageName, clickMode)
                    }.toList()
            }
            val validPackages = packageList.filter { (packageName, _) ->
                try {
                    packageManager.getApplicationInfo(packageName, 0)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w("StressTestManager", "Package not installed: $packageName")
                    false
                }
            }
            if (validPackages.isEmpty()) {
                Toast.makeText(context, "rmd.txt 中未找到有效包名", Toast.LENGTH_LONG).show()
            }
            return validPackages
        } catch (_: Exception) {
            // 回退用自动发现
            return getThirdPartyApps().map { Pair(it, "tap") }
        }
    }

    // 自动查找所有第三方包
    private fun getThirdPartyApps(): List<String> {
        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return packages
            .filter {
                it.packageName != "com.android.launcher4" &&
                !it.packageName.startsWith("com.android.") &&
                !it.packageName.startsWith("com.miui.") &&
                !it.packageName.startsWith("com.mi.") &&
                !it.packageName.startsWith("com.google.") &&
                !it.packageName.startsWith("com.xiaomi.")
            }
            .map { it.packageName }
    }

    // -------------------- 日志记录 --------------------
    private fun getBatteryLevel(): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun appendLog(packageName: String, success: Boolean) {
        val time = dateFormat.format(Date())
        val battery = getBatteryLevel()
        val status = if (success) "启动成功" else "启动失败"
        val log = "$time | $packageName | $status | 剩余电量: $battery%\n"
        try {
            logFile.appendText(log)
        } catch (_: Exception) {}
    }

    // -------------------- 耗电量计算 --------------------
    private fun logBatteryConsumption() {
        val currentTime = System.currentTimeMillis()
        val durationMillis = currentTime - taskStartTime
        val hours = durationMillis / (1000 * 60 * 60)
        val minutes = (durationMillis / (1000 * 60)) % 60
        val currentBattery = getBatteryLevel()
        val consumption = initialBatteryLevel - currentBattery
        val log = "当前任务已用时：${hours}小时${minutes}分 耗电量：${consumption}%\n"
        try {
            logFile.appendText(log)
        } catch (_: Exception) {}
    }

    // -------------------- 启动App（root） --------------------
    private fun launchAppWithRoot(packageName: String): Boolean {
        // 强制启动 launcher 以防止进程被杀死
        try {
            val process4 = Runtime.getRuntime().exec("su")
            val writer4 = process4.outputStream.bufferedWriter()
            writer4.write("am start -n com.android.launcher4/app.desktop.DesktopLauncher\n")
            writer4.flush()
            writer4.close()
            process4.waitFor()
        } catch (_: Exception) {}
        try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                val component = intent.component
                if (component != null) {
                    val cmd = "am start --user 0 -n ${component.packageName}/${component.className}\n"
                    val process = Runtime.getRuntime().exec("su")
                    process.outputStream.bufferedWriter().apply {
                        write(cmd)
                        flush()
                        close()
                    }
                    val exitCode = process.waitFor()
                    if (exitCode == 0) appendLog(packageName, true)
                    return exitCode == 0
                } else {
                    val monkeyCmd = "monkey -p $packageName -c android.intent.category.LAUNCHER 1\n"
                    val process = Runtime.getRuntime().exec("su")
                    process.outputStream.bufferedWriter().apply {
                        write(monkeyCmd)
                        flush()
                        close()
                    }
                    val exitCode = process.waitFor()
                    if (exitCode == 0) appendLog(packageName, true)
                    return exitCode == 0
                }
            }
        } catch (_: Exception) {}
        appendLog(packageName, false)
        return false
    }

    // -------------------- 自动点击任务 --------------------
    private fun startAutoTapTask(packageName: String, clickMode: String, isLastApp: Boolean) {
        stopAutoTapTask()
        clickJob = CoroutineScope(Dispatchers.IO).launch {
            val metrics = context.resources.displayMetrics
            val maxX = metrics.widthPixels - 100
            val maxY = metrics.heightPixels - 100
            val random = Random(System.currentTimeMillis())
            val startTime = System.currentTimeMillis()
            if (clickMode == "swipe") {
                // 模拟上滑
                while (isActive) {
                    if (isLastApp && getTopPackageName() != packageName) {
                        launchAppWithRoot(packageName) // 顶层应用不是目标应用时唤起
                    }
                    try {
                        val process = Runtime.getRuntime().exec("su")
                        process.outputStream.bufferedWriter().apply {
                            write("input swipe 500 1000 500 200 300\n")
                            flush()
                            close()
                        }
                        process.waitFor()
                    } catch (_: Exception) {}
                    delay(10_000)
                }
            } else {
                // 随机点击
                while (isActive && System.currentTimeMillis() - startTime < TimeUnit.MINUTES.toMillis(2)) {
                    if (isLastApp) {
                        launchAppWithRoot(packageName) // 每次点击前唤起主界面
                    }
                    val x = random.nextInt(100, maxX)
                    val y = random.nextInt(100, maxY)
                    try {
                        val process = Runtime.getRuntime().exec("su")
                        process.outputStream.bufferedWriter().apply {
                            write("input tap $x $y\n")
                            flush()
                            close()
                        }
                        process.waitFor()
                    } catch (_: Exception) {}
                    delay(10_000)
                }
            }
        }
    }

    private fun stopAutoTapTask() {
        clickJob?.cancel()
        clickJob = null
    }

    // -------------------- 主测试流程 --------------------
    private fun startAppTesting(durationMillis: Long) {
        stopAppTesting()
        addToWhitelist() // 测试开始时添加白名单
        setScreenBrightness() // 设置屏幕亮度为 50%
        acquireWakeLock()
        testJob = testScope.launch {
            val apps = getPackageAndClickMode()
            if (apps.isEmpty()) {
                Toast.makeText(context, "未找到第三方应用", Toast.LENGTH_SHORT).show()
                releaseWakeLock()
                return@launch
            }
            taskStartTime = System.currentTimeMillis()
            initialBatteryLevel = getBatteryLevel()
            // 每 30 分钟记录耗电量
            launch {
                while (isActive) {
                    delay(TimeUnit.MINUTES.toMillis(30))
                    if (isActive) logBatteryConsumption()
                }
            }
            val startTime = System.currentTimeMillis()
            var index = 0
            while (isActive && (durationMillis == -1L || System.currentTimeMillis() - startTime < durationMillis)) {
                val (appPackage, clickMode) = apps[index]
                val success = launchAppWithRoot(appPackage)
                if (success) {
                    startAutoTapTask(appPackage, clickMode, index == apps.size - 1)
                    if (index == apps.size - 1) {
                        // 最后一个应用：持续运行直到 durationMillis 结束
                        while (isActive && (durationMillis == -1L || System.currentTimeMillis() - startTime < durationMillis)) {
                            delay(1000)
                        }
                        stopAutoTapTask()
                        sendHomeKey() // 最后一个应用结束后发送 Home 键
                    } else {
                        // 其他应用：运行 2 分钟后发送 Home 键
                        delay(TimeUnit.MINUTES.toMillis(2))
                        stopAutoTapTask()
                        sendHomeKey()
                    }
                }
                index = (index + 1) % apps.size
            }
            releaseWakeLock()
        }
    }

    private fun stopAppTesting() {
        testJob?.cancel()
        testJob = null
        stopAutoTapTask()
        sendHomeKey()
        releaseWakeLock()
    }

    fun cleanup() {
        context.unregisterReceiver(testReceiver)
        testScope.cancel()
        stopAutoTapTask()
        releaseWakeLock()
    }
}