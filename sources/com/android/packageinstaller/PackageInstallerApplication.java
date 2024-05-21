package com.android.packageinstaller;

import android.app.Application;
import android.content.pm.PackageItemInfo;
/* loaded from: classes.dex */
public class PackageInstallerApplication extends Application {
    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        PackageItemInfo.forceSafeLabels();
    }
}
