package com.android.launcher3.compat;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** 兼容获取“归档”状态：老系统无字段/方法时恒 false，避免 NoSuchField/MethodError。 */
public final class ArchivedCompat {
    private ArchivedCompat() {}

    public static boolean isArchived(LauncherActivityInfo lai) {
        if (lai == null) return false;

        // 1) 先试 ActivityInfo（有些分支把 isArchived 放在这里）
        ActivityInfo act = lai.getActivityInfo();
        if (act != null) {
            // 字段
            try {
                Field f = ActivityInfo.class.getDeclaredField("isArchived");
                f.setAccessible(true);
                return f.getBoolean(act);
            } catch (Throwable ignored) {}
            // 方法
            try {
                Method m = ActivityInfo.class.getDeclaredMethod("isArchived");
                m.setAccessible(true);
                Object r = m.invoke(act);
                if (r instanceof Boolean) return (Boolean) r;
            } catch (Throwable ignored) {}
        }

        // 2) 再试 ApplicationInfo（AOSP 新字段在这里）
        return isArchived(lai.getApplicationInfo());
    }

    public static boolean isArchived(ApplicationInfo ai) {
        if (ai == null) return false;

        // 方法 ApplicationInfo.isArchived()
        try {
            Method m = ApplicationInfo.class.getDeclaredMethod("isArchived");
            m.setAccessible(true);
            Object r = m.invoke(ai);
            if (r instanceof Boolean) return (Boolean) r;
        } catch (Throwable ignored) {}

        // 字段 ApplicationInfo.isArchived
        try {
            Field f = ApplicationInfo.class.getDeclaredField("isArchived");
            f.setAccessible(true);
            return f.getBoolean(ai);
        } catch (Throwable ignored) {}

        // 老系统没有归档概念
        return false;
    }
}
