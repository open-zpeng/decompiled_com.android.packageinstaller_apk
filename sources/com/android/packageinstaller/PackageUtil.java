package com.android.packageinstaller;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
/* loaded from: classes.dex */
public class PackageUtil {
    private static final String LOG_TAG = "PackageUtil";

    public static PackageParser.Package getPackageInfo(Context context, File file) {
        PackageParser packageParser = new PackageParser();
        packageParser.setCallback(new PackageParser.CallbackImpl(context.getPackageManager()));
        try {
            return packageParser.parsePackage(file, 0);
        } catch (PackageParser.PackageParserException unused) {
            return null;
        }
    }

    public static View initSnippet(View view, CharSequence charSequence, Drawable drawable) {
        ((ImageView) view.findViewById(R.id.app_icon)).setImageDrawable(drawable);
        ((TextView) view.findViewById(R.id.app_name)).setText(charSequence);
        return view;
    }

    public static View initSnippetForInstalledApp(Context context, ApplicationInfo applicationInfo, View view) {
        initSnippetForInstalledApp(context, applicationInfo, view, null);
        return view;
    }

    public static View initSnippetForInstalledApp(Context context, ApplicationInfo applicationInfo, View view, UserHandle userHandle) {
        PackageManager packageManager = context.getPackageManager();
        Drawable loadIcon = applicationInfo.loadIcon(packageManager);
        if (userHandle != null) {
            loadIcon = context.getPackageManager().getUserBadgedIcon(loadIcon, userHandle);
        }
        initSnippet(view, applicationInfo.loadLabel(packageManager), loadIcon);
        return view;
    }

    /* loaded from: classes.dex */
    public static class AppSnippet {
        public Drawable icon;
        public CharSequence label;

        public AppSnippet(CharSequence charSequence, Drawable drawable) {
            this.label = charSequence;
            this.icon = drawable;
        }
    }

    /* JADX WARN: Can't wrap try/catch for region: R(10:1|(9:24|25|(2:5|(1:7)(1:8))|9|10|(2:18|19)|(1:13)|15|16)|3|(0)|9|10|(0)|(0)|15|16) */
    /* JADX WARN: Code restructure failed: missing block: B:18:0x004a, code lost:
        r3 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:19:0x004b, code lost:
        android.util.Log.i(com.android.packageinstaller.PackageUtil.LOG_TAG, "Could not load app icon", r3);
     */
    /* JADX WARN: Removed duplicated region for block: B:16:0x0041 A[Catch: OutOfMemoryError -> 0x004a, TRY_ENTER, TRY_LEAVE, TryCatch #1 {OutOfMemoryError -> 0x004a, blocks: (B:12:0x0035, B:14:0x0039, B:16:0x0041), top: B:26:0x0035 }] */
    /* JADX WARN: Removed duplicated region for block: B:28:0x0039 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:8:0x002c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static com.android.packageinstaller.PackageUtil.AppSnippet getAppSnippet(android.app.Activity r3, android.content.pm.ApplicationInfo r4, java.io.File r5) {
        /*
            java.lang.String r5 = r5.getAbsolutePath()
            android.content.res.Resources r0 = r3.getResources()
            android.content.res.AssetManager r1 = new android.content.res.AssetManager
            r1.<init>()
            r1.addAssetPath(r5)
            android.content.res.Resources r5 = new android.content.res.Resources
            android.util.DisplayMetrics r2 = r0.getDisplayMetrics()
            android.content.res.Configuration r0 = r0.getConfiguration()
            r5.<init>(r1, r2, r0)
            int r0 = r4.labelRes
            r1 = 0
            if (r0 == 0) goto L29
            int r0 = r4.labelRes     // Catch: android.content.res.Resources.NotFoundException -> L29
            java.lang.CharSequence r0 = r5.getText(r0)     // Catch: android.content.res.Resources.NotFoundException -> L29
            goto L2a
        L29:
            r0 = r1
        L2a:
            if (r0 != 0) goto L35
            java.lang.CharSequence r0 = r4.nonLocalizedLabel
            if (r0 == 0) goto L33
            java.lang.CharSequence r0 = r4.nonLocalizedLabel
            goto L35
        L33:
            java.lang.String r0 = r4.packageName
        L35:
            int r2 = r4.icon     // Catch: java.lang.OutOfMemoryError -> L4a
            if (r2 == 0) goto L3f
            int r4 = r4.icon     // Catch: android.content.res.Resources.NotFoundException -> L3f java.lang.OutOfMemoryError -> L4a
            android.graphics.drawable.Drawable r1 = r5.getDrawable(r4)     // Catch: android.content.res.Resources.NotFoundException -> L3f java.lang.OutOfMemoryError -> L4a
        L3f:
            if (r1 != 0) goto L52
            android.content.pm.PackageManager r3 = r3.getPackageManager()     // Catch: java.lang.OutOfMemoryError -> L4a
            android.graphics.drawable.Drawable r1 = r3.getDefaultActivityIcon()     // Catch: java.lang.OutOfMemoryError -> L4a
            goto L52
        L4a:
            r3 = move-exception
            java.lang.String r4 = com.android.packageinstaller.PackageUtil.LOG_TAG
            java.lang.String r5 = "Could not load app icon"
            android.util.Log.i(r4, r5, r3)
        L52:
            com.android.packageinstaller.PackageUtil$AppSnippet r3 = new com.android.packageinstaller.PackageUtil$AppSnippet
            r3.<init>(r0, r1)
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.packageinstaller.PackageUtil.getAppSnippet(android.app.Activity, android.content.pm.ApplicationInfo, java.io.File):com.android.packageinstaller.PackageUtil$AppSnippet");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getMaxTargetSdkVersionForUid(Context context, int i) {
        PackageManager packageManager = context.getPackageManager();
        String[] packagesForUid = packageManager.getPackagesForUid(i);
        if (packagesForUid != null) {
            int i2 = -1;
            for (String str : packagesForUid) {
                try {
                    i2 = Math.max(i2, packageManager.getApplicationInfo(str, 0).targetSdkVersion);
                } catch (PackageManager.NameNotFoundException unused) {
                }
            }
            return i2;
        }
        return -1;
    }
}
