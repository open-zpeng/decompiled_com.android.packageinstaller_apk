package com.android.packageinstaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import java.io.File;
import java.io.IOException;
/* loaded from: classes.dex */
public class TemporaryFileManager extends BroadcastReceiver {
    private static final String LOG_TAG = "TemporaryFileManager";

    public static File getStagedFile(Context context) throws IOException {
        return File.createTempFile("package", ".apk", context.getNoBackupFilesDir());
    }

    public static File getInstallStateFile(Context context) {
        return new File(context.getNoBackupFilesDir(), "install_results.xml");
    }

    public static File getUninstallStateFile(Context context) {
        return new File(context.getNoBackupFilesDir(), "uninstall_results.xml");
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        long currentTimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime();
        File[] listFiles = context.getNoBackupFilesDir().listFiles();
        if (listFiles == null) {
            return;
        }
        for (File file : listFiles) {
            if (currentTimeMillis > file.lastModified()) {
                if (!file.delete()) {
                    Log.w(LOG_TAG, "Could not delete " + file.getName() + " onBoot");
                }
            } else {
                Log.w(LOG_TAG, file.getName() + " was created before onBoot broadcast was received");
            }
        }
    }
}
