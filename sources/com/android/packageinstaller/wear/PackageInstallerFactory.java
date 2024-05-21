package com.android.packageinstaller.wear;

import android.content.Context;
/* loaded from: classes.dex */
public class PackageInstallerFactory {
    private static PackageInstallerImpl sPackageInstaller;

    public static synchronized PackageInstallerImpl getPackageInstaller(Context context) {
        PackageInstallerImpl packageInstallerImpl;
        synchronized (PackageInstallerFactory.class) {
            if (sPackageInstaller == null) {
                sPackageInstaller = new PackageInstallerImpl(context);
            }
            packageInstallerImpl = sPackageInstaller;
        }
        return packageInstallerImpl;
    }
}
