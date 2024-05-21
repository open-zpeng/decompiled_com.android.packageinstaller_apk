package com.android.packageinstaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
/* loaded from: classes.dex */
public class PackageInstalledReceiver extends BroadcastReceiver {
    private static final String TAG = "PackageInstalledReceiver";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Uri data;
        if (Settings.Global.getInt(context.getContentResolver(), "show_new_app_installed_notification_enabled", 0) == 0 || !"android.intent.action.PACKAGE_ADDED".equals(intent.getAction()) || (data = intent.getData()) == null) {
            return;
        }
        String schemeSpecificPart = data.getSchemeSpecificPart();
        if (schemeSpecificPart == null) {
            Log.e(TAG, "No package name");
        } else if (intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
        } else {
            new PackageInstalledNotificationUtils(context, context.getPackageManager().getInstallerPackageName(schemeSpecificPart), schemeSpecificPart).postAppInstalledNotification();
        }
    }
}
