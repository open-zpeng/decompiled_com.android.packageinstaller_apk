package com.android.packageinstaller.wear;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
/* loaded from: classes.dex */
public class WearPackageIconProvider extends ContentProvider {
    @Override // android.content.ContentProvider
    public boolean onCreate() {
        return true;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        throw new UnsupportedOperationException("Query is not supported.");
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI passed in is null.");
        }
        if ("com.google.android.packageinstaller.wear.provider".equals(uri.getEncodedAuthority())) {
            return "vnd.android.cursor.item/cw_package_icon";
        }
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException("Insert is not supported.");
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        File iconFile;
        if (uri == null) {
            throw new IllegalArgumentException("URI passed in is null.");
        }
        enforcePermissions(uri);
        if (!"vnd.android.cursor.item/cw_package_icon".equals(getType(uri)) || (iconFile = WearPackageUtil.getIconFile(getContext().getApplicationContext(), getPackageNameFromUri(uri))) == null) {
            return 0;
        }
        iconFile.delete();
        return 0;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        throw new UnsupportedOperationException("Update is not supported.");
    }

    @Override // android.content.ContentProvider
    public ParcelFileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        File iconFile;
        if (uri == null) {
            throw new IllegalArgumentException("URI passed in is null.");
        }
        enforcePermissions(uri);
        if (!"vnd.android.cursor.item/cw_package_icon".equals(getType(uri)) || (iconFile = WearPackageUtil.getIconFile(getContext().getApplicationContext(), getPackageNameFromUri(uri))) == null) {
            return null;
        }
        return ParcelFileDescriptor.open(iconFile, 268435456);
    }

    private String getPackageNameFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        List<String> pathSegments = uri.getPathSegments();
        String str = pathSegments.get(pathSegments.size() - 1);
        return str.endsWith(".icon") ? str.substring(0, str.lastIndexOf(".")) : str;
    }

    @TargetApi(2)
    private void enforcePermissions(Uri uri) {
        Context context = getContext();
        int callingPid = Binder.getCallingPid();
        int callingUid = Binder.getCallingUid();
        if (callingUid == Process.myUid() || isSystemApp(context, callingPid) || context.checkPermission("com.google.android.permission.INSTALL_WEARABLE_PACKAGES", callingPid, callingUid) == 0 || context.checkUriPermission(uri, callingPid, callingUid, 1) == 0) {
            return;
        }
        throw new SecurityException("Permission Denial: reading " + WearPackageIconProvider.class.getName() + " uri " + uri + " from pid=" + callingPid + ", uid=" + callingUid);
    }

    @TargetApi(3)
    private boolean isSystemApp(Context context, int i) {
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == i) {
                try {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(runningAppProcessInfo.pkgList[0], 0);
                    if (packageInfo != null && packageInfo.applicationInfo != null && (packageInfo.applicationInfo.flags & 1) != 0) {
                        Log.d("WearPackageIconProvider", i + " is a system app.");
                        return true;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("WearPackageIconProvider", "Could not find package information.", e);
                }
            }
        }
        return false;
    }
}
