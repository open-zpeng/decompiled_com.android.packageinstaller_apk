package com.android.packageinstaller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
/* loaded from: classes.dex */
public class InstallStart extends Activity {
    private static final String LOG_TAG = "InstallStart";
    private IPackageManager mIPackageManager;
    private boolean mAbortInstall = false;
    private final boolean isXUIInstallEnable = SystemProperties.getBoolean("persist.xui.installer.enable", true);

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mIPackageManager = AppGlobals.getPackageManager();
        Intent intent = getIntent();
        String callingPackage = getCallingPackage();
        boolean equals = "android.content.pm.action.CONFIRM_INSTALL".equals(intent.getAction());
        int intExtra = equals ? intent.getIntExtra("android.content.pm.extra.SESSION_ID", -1) : -1;
        if (callingPackage == null && intExtra != -1) {
            PackageInstaller.SessionInfo sessionInfo = getPackageManager().getPackageInstaller().getSessionInfo(intExtra);
            callingPackage = sessionInfo != null ? sessionInfo.getInstallerPackageName() : null;
        }
        ApplicationInfo sourceInfo = getSourceInfo(callingPackage);
        int originatingUid = getOriginatingUid(sourceInfo);
        if (this.isXUIInstallEnable) {
            Intent intent2 = new Intent(intent);
            intent2.addFlags(268435457);
            intent2.setClassName("com.xiaopeng.appstore", "com.xiaopeng.appstore.installer.XpInstallForbiddenActivity");
            intent2.putExtra("EXTRA_CALLING_PACKAGE", callingPackage);
            intent2.putExtra("EXTRA_ORIGINAL_SOURCE_INFO", sourceInfo);
            intent2.putExtra("android.intent.extra.ORIGINATING_UID", originatingUid);
            try {
                startActivity(intent2);
            } catch (Exception e) {
                Log.w(LOG_TAG, "start xuiAppInstaller failed:" + e);
            }
            setResult(0);
            finish();
            return;
        }
        if (!((sourceInfo == null || (sourceInfo.privateFlags & 8) == 0) ? false : intent.getBooleanExtra("android.intent.extra.NOT_UNKNOWN_SOURCE", false)) && originatingUid != -1) {
            int maxTargetSdkVersionForUid = PackageUtil.getMaxTargetSdkVersionForUid(this, originatingUid);
            if (maxTargetSdkVersionForUid < 0) {
                Log.w(LOG_TAG, "Cannot get target sdk version for uid " + originatingUid);
                this.mAbortInstall = true;
            } else if (maxTargetSdkVersionForUid >= 26 && !declaresAppOpPermission(originatingUid, "android.permission.REQUEST_INSTALL_PACKAGES")) {
                Log.e(LOG_TAG, "Requesting uid " + originatingUid + " needs to declare permission android.permission.REQUEST_INSTALL_PACKAGES");
                this.mAbortInstall = true;
            }
        }
        if (this.mAbortInstall) {
            setResult(0);
            finish();
            return;
        }
        Intent intent3 = new Intent(intent);
        intent3.setFlags(33554432);
        intent3.putExtra("EXTRA_CALLING_PACKAGE", callingPackage);
        intent3.putExtra("EXTRA_ORIGINAL_SOURCE_INFO", sourceInfo);
        intent3.putExtra("android.intent.extra.ORIGINATING_UID", originatingUid);
        if (equals) {
            intent3.setClass(this, PackageInstallerActivity.class);
        } else {
            Uri data = intent.getData();
            if (data != null && data.getScheme().equals("content")) {
                intent3.setClass(this, InstallStaging.class);
            } else if (data != null && data.getScheme().equals("package")) {
                intent3.setClass(this, PackageInstallerActivity.class);
            } else {
                Intent intent4 = new Intent();
                intent4.putExtra("android.intent.extra.INSTALL_RESULT", -3);
                setResult(1, intent4);
                intent3 = null;
            }
        }
        if (intent3 != null) {
            startActivity(intent3);
        }
        finish();
    }

    private boolean declaresAppOpPermission(int i, String str) {
        String[] appOpPermissionPackages;
        try {
            appOpPermissionPackages = this.mIPackageManager.getAppOpPermissionPackages(str);
        } catch (RemoteException unused) {
        }
        if (appOpPermissionPackages == null) {
            return false;
        }
        for (String str2 : appOpPermissionPackages) {
            if (i == getPackageManager().getPackageUid(str2, 0)) {
                return true;
            }
        }
        return false;
    }

    private ApplicationInfo getSourceInfo(String str) {
        if (str != null) {
            try {
                return getPackageManager().getApplicationInfo(str, 0);
            } catch (PackageManager.NameNotFoundException unused) {
                return null;
            }
        }
        return null;
    }

    private int getOriginatingUid(ApplicationInfo applicationInfo) {
        int launchedFromUid;
        int intExtra = getIntent().getIntExtra("android.intent.extra.ORIGINATING_UID", -1);
        if (applicationInfo != null) {
            launchedFromUid = applicationInfo.uid;
        } else {
            try {
                launchedFromUid = ActivityManager.getService().getLaunchedFromUid(getActivityToken());
            } catch (RemoteException unused) {
                Log.e(LOG_TAG, "Could not determine the launching uid.");
                this.mAbortInstall = true;
                return -1;
            }
        }
        try {
            if (this.mIPackageManager.checkUidPermission("android.permission.MANAGE_DOCUMENTS", launchedFromUid) == 0) {
                return intExtra;
            }
        } catch (RemoteException unused2) {
        }
        return isSystemDownloadsProvider(launchedFromUid) ? intExtra : launchedFromUid;
    }

    private boolean isSystemDownloadsProvider(int i) {
        ProviderInfo resolveContentProvider = getPackageManager().resolveContentProvider("downloads", 0);
        if (resolveContentProvider == null) {
            return false;
        }
        ApplicationInfo applicationInfo = resolveContentProvider.applicationInfo;
        return applicationInfo.isSystemApp() && i == applicationInfo.uid;
    }
}
