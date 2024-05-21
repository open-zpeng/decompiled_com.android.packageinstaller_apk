package com.android.packageinstaller.wear;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;
import com.android.packageinstaller.DeviceUtils;
import com.android.packageinstaller.R;
import com.android.packageinstaller.wear.PackageInstallerImpl;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
/* loaded from: classes.dex */
public class WearPackageInstallerService extends Service {
    private static volatile PowerManager.WakeLock lockStatic;
    private final int START_INSTALL = 1;
    private final int START_UNINSTALL = 2;
    private int mInstallNotificationId = 1;
    private final Map<String, Integer> mNotifIdMap = new ArrayMap();
    private NotificationChannel mNotificationChannel;
    private ServiceHandler mServiceHandler;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    /* loaded from: classes.dex */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                WearPackageInstallerService.this.installPackage(message.getData());
            } else if (i != 2) {
            } else {
                WearPackageInstallerService.this.uninstallPackage(message.getData());
            }
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("PackageInstallerThread", 10);
        handlerThread.start();
        this.mServiceHandler = new ServiceHandler(handlerThread.getLooper());
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        Message obtainMessage;
        String string;
        if (!DeviceUtils.isWear(this)) {
            Log.w("WearPkgInstallerService", "Not running on wearable.");
            finishServiceEarly(i2);
            return 2;
        } else if (intent == null) {
            Log.w("WearPkgInstallerService", "Got null intent.");
            finishServiceEarly(i2);
            return 2;
        } else {
            if (Log.isLoggable("WearPkgInstallerService", 3)) {
                Log.d("WearPkgInstallerService", "Got install/uninstall request " + intent);
            }
            Uri data = intent.getData();
            if (data == null) {
                Log.e("WearPkgInstallerService", "No package URI in intent");
                finishServiceEarly(i2);
                return 2;
            }
            String sanitizedPackageName = WearPackageUtil.getSanitizedPackageName(data);
            if (sanitizedPackageName == null) {
                Log.e("WearPkgInstallerService", "Invalid package name in URI (expected package:<pkgName>): " + data);
                finishServiceEarly(i2);
                return 2;
            }
            PowerManager.WakeLock lock = getLock(getApplicationContext());
            if (!lock.isHeld()) {
                lock.acquire();
            }
            Bundle extras = intent.getExtras();
            if (extras == null) {
                extras = new Bundle();
            }
            WearPackageArgs.setStartId(extras, i2);
            WearPackageArgs.setPackageName(extras, sanitizedPackageName);
            if ("android.intent.action.INSTALL_PACKAGE".equals(intent.getAction())) {
                obtainMessage = this.mServiceHandler.obtainMessage(1);
                string = getString(R.string.installing);
            } else if ("android.intent.action.UNINSTALL_PACKAGE".equals(intent.getAction())) {
                obtainMessage = this.mServiceHandler.obtainMessage(2);
                string = getString(R.string.uninstalling);
            } else {
                Log.e("WearPkgInstallerService", "Unknown action : " + intent.getAction());
                finishServiceEarly(i2);
                return 2;
            }
            Pair<Integer, Notification> buildNotification = buildNotification(sanitizedPackageName, string);
            startForeground(((Integer) buildNotification.first).intValue(), (Notification) buildNotification.second);
            obtainMessage.setData(extras);
            this.mServiceHandler.sendMessage(obtainMessage);
            return 2;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Removed duplicated region for block: B:173:0x03e7  */
    /* JADX WARN: Removed duplicated region for block: B:180:0x03f7  */
    /* JADX WARN: Removed duplicated region for block: B:203:0x0118 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:223:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:26:0x00f4 A[Catch: all -> 0x0111, FileNotFoundException -> 0x0114, TRY_ENTER, TRY_LEAVE, TryCatch #13 {all -> 0x0111, blocks: (B:26:0x00f4, B:37:0x011e, B:44:0x0143, B:55:0x01a1, B:60:0x01cd, B:70:0x020b), top: B:194:0x00f2 }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void installPackage(android.os.Bundle r28) {
        /*
            Method dump skipped, instructions count: 1024
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.packageinstaller.wear.WearPackageInstallerService.installPackage(android.os.Bundle):void");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void uninstallPackage(Bundle bundle) {
        int startId = WearPackageArgs.getStartId(bundle);
        String packageName = WearPackageArgs.getPackageName(bundle);
        PowerManager.WakeLock lock = getLock(getApplicationContext());
        PackageManager packageManager = getPackageManager();
        try {
            getLabelAndUpdateNotification(packageName, getString(R.string.uninstalling_app, new Object[]{packageManager.getPackageInfo(packageName, 0).applicationInfo.loadLabel(packageManager)}));
            packageManager.deletePackage(packageName, new PackageDeleteObserver(lock, startId), 2);
            Log.i("WearPkgInstallerService", "Sent delete request for " + packageName);
        } catch (PackageManager.NameNotFoundException | IllegalArgumentException e) {
            Log.w("WearPkgInstallerService", "Could not find package, not deleting " + packageName, e);
            finishService(lock, startId);
        }
    }

    private boolean checkPermissions(PackageParser.Package r2, int i, int i2, Uri uri, List<String> list, File file) {
        if (r2.applicationInfo.targetSdkVersion < 23 && doesWearHaveUngrantedPerms(r2.packageName, uri, list)) {
            if (i == 0 || i >= 23) {
                Log.e("WearPkgInstallerService", "MNC: Wear app's targetSdkVersion should be at least 23, if phone app is targeting at least 23, will continue.");
                return false;
            }
            return false;
        }
        return true;
    }

    private boolean doesWearHaveUngrantedPerms(String str, Uri uri, List<String> list) {
        boolean z;
        if (uri == null) {
            Log.e("WearPkgInstallerService", "Permission URI is null");
            return true;
        }
        Cursor query = getContentResolver().query(uri, null, null, null, null);
        if (query == null) {
            Log.e("WearPkgInstallerService", "Could not get the cursor for the permissions");
            return true;
        }
        HashSet hashSet = new HashSet();
        HashSet hashSet2 = new HashSet();
        while (true) {
            z = false;
            if (!query.moveToNext()) {
                break;
            } else if (query.getColumnCount() == 2 && 3 == query.getType(0) && 1 == query.getType(1)) {
                String string = query.getString(0);
                if (Integer.valueOf(query.getInt(1)).intValue() == 1) {
                    hashSet.add(string);
                } else {
                    hashSet2.add(string);
                }
            }
        }
        query.close();
        for (String str2 : list) {
            if (!hashSet.contains(str2)) {
                if (hashSet2.contains(str2)) {
                    Log.w("WearPkgInstallerService", "Wearable " + str + " has a permission \"" + str2 + "\" that is not granted in the host application.");
                } else {
                    Log.e("WearPkgInstallerService", "Wearable " + str + " has a permission \"" + str2 + "\" that is not defined in the host application's manifest.");
                }
                z = true;
            }
        }
        return z;
    }

    private void finishServiceEarly(int i) {
        Pair<Integer, Notification> buildNotification = buildNotification(getApplicationContext().getPackageName(), "");
        startForeground(((Integer) buildNotification.first).intValue(), (Notification) buildNotification.second);
        finishService(null, i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void finishService(PowerManager.WakeLock wakeLock, int i) {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopSelf(i);
    }

    private synchronized PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            lockStatic = ((PowerManager) context.getSystemService("power")).newWakeLock(1, context.getClass().getSimpleName());
            lockStatic.setReferenceCounted(true);
        }
        return lockStatic;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PackageInstallListener implements PackageInstallerImpl.InstallListener {
        private String mApplicationPackageName;
        private Context mContext;
        private int mStartId;
        private PowerManager.WakeLock mWakeLock;

        private PackageInstallListener(Context context, PowerManager.WakeLock wakeLock, int i, String str) {
            this.mContext = context;
            this.mWakeLock = wakeLock;
            this.mStartId = i;
            this.mApplicationPackageName = str;
        }

        @Override // com.android.packageinstaller.wear.PackageInstallerImpl.InstallListener
        public void installBeginning() {
            Log.i("WearPkgInstallerService", "Package " + this.mApplicationPackageName + " is being installed.");
        }

        @Override // com.android.packageinstaller.wear.PackageInstallerImpl.InstallListener
        public void installSucceeded() {
            try {
                Log.i("WearPkgInstallerService", "Package " + this.mApplicationPackageName + " was installed.");
                File temporaryFile = WearPackageUtil.getTemporaryFile(this.mContext, this.mApplicationPackageName);
                if (temporaryFile != null) {
                    temporaryFile.delete();
                }
            } finally {
                WearPackageInstallerService.this.finishService(this.mWakeLock, this.mStartId);
            }
        }

        @Override // com.android.packageinstaller.wear.PackageInstallerImpl.InstallListener
        public void installFailed(int i, String str) {
            Log.e("WearPkgInstallerService", "Package install failed " + this.mApplicationPackageName + ", errorCode " + i);
            WearPackageInstallerService.this.finishService(this.mWakeLock, this.mStartId);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        private int mStartId;
        private PowerManager.WakeLock mWakeLock;

        private PackageDeleteObserver(PowerManager.WakeLock wakeLock, int i) {
            this.mWakeLock = wakeLock;
            this.mStartId = i;
        }

        /* JADX WARN: Multi-variable type inference failed */
        /* JADX WARN: Type inference failed for: r3v3, types: [int] */
        public void packageDeleted(String str, int i) {
            try {
                if (i >= 0) {
                    Log.i("WearPkgInstallerService", "Package " + str + " was uninstalled.");
                } else {
                    Log.e("WearPkgInstallerService", "Package uninstall failed " + str + ", returnCode " + i);
                }
            } finally {
                WearPackageInstallerService.this.finishService(this.mWakeLock, this.mStartId);
            }
        }
    }

    private synchronized Pair<Integer, Notification> buildNotification(String str, String str2) {
        int i;
        if (this.mNotifIdMap.containsKey(str)) {
            i = this.mNotifIdMap.get(str).intValue();
        } else {
            int i2 = this.mInstallNotificationId;
            this.mInstallNotificationId = i2 + 1;
            this.mNotifIdMap.put(str, Integer.valueOf(i2));
            i = i2;
        }
        if (this.mNotificationChannel == null) {
            this.mNotificationChannel = new NotificationChannel("wear_app_install_uninstall", getString(R.string.wear_app_channel), 1);
            ((NotificationManager) getSystemService(NotificationManager.class)).createNotificationChannel(this.mNotificationChannel);
        }
        return new Pair<>(Integer.valueOf(i), new Notification.Builder(this, "wear_app_install_uninstall").setSmallIcon(R.drawable.ic_file_download).setContentTitle(str2).build());
    }

    private void getLabelAndUpdateNotification(String str, String str2) {
        Pair<Integer, Notification> buildNotification = buildNotification(str, str2);
        ((NotificationManager) getSystemService(NotificationManager.class)).notify(((Integer) buildNotification.first).intValue(), (Notification) buildNotification.second);
    }
}
