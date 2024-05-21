package com.android.packageinstaller;

import android.content.Context;
/* loaded from: classes.dex */
public class DeviceUtils {
    public static boolean isWear(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.type.watch");
    }
}
