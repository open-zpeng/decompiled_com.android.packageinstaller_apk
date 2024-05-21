package com.android.packageinstaller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.IDevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.graphics.drawable.Icon;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.widget.Toast;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class UninstallFinish extends BroadcastReceiver {
    private static final String LOG_TAG = "UninstallFinish";

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        int i;
        NotificationManager notificationManager;
        int i2;
        List list;
        UserInfo userInfo;
        int intExtra = intent.getIntExtra("android.content.pm.extra.STATUS", 0);
        String str = LOG_TAG;
        Log.i(str, "Uninstall finished extras=" + intent.getExtras());
        if (intExtra == -1) {
            context.startActivity((Intent) intent.getParcelableExtra("android.intent.extra.INTENT"));
            return;
        }
        int intExtra2 = intent.getIntExtra("com.android.packageinstaller.extra.UNINSTALL_ID", 0);
        ApplicationInfo applicationInfo = (ApplicationInfo) intent.getParcelableExtra("com.android.packageinstaller.applicationInfo");
        String stringExtra = intent.getStringExtra("com.android.packageinstaller.extra.APP_LABEL");
        boolean booleanExtra = intent.getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
        NotificationManager notificationManager2 = (NotificationManager) context.getSystemService(NotificationManager.class);
        UserManager userManager = (UserManager) context.getSystemService(UserManager.class);
        notificationManager2.createNotificationChannel(new NotificationChannel("uninstall failure", context.getString(R.string.uninstall_failure_notification_channel), 3));
        Notification.Builder builder = new Notification.Builder(context, "uninstall failure");
        if (intExtra == 0) {
            notificationManager2.cancel(intExtra2);
            Toast.makeText(context, context.getString(R.string.uninstall_done_app, stringExtra), 1).show();
            return;
        }
        if (intExtra == 2) {
            int intExtra3 = intent.getIntExtra("android.content.pm.extra.LEGACY_STATUS", 0);
            if (intExtra3 == -4) {
                IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                List users = userManager.getUsers();
                i = intExtra2;
                int i3 = 0;
                while (true) {
                    notificationManager = notificationManager2;
                    if (i3 >= users.size()) {
                        i2 = -10000;
                        break;
                    }
                    UserInfo userInfo2 = (UserInfo) users.get(i3);
                    try {
                        list = users;
                        try {
                        } catch (RemoteException e) {
                            e = e;
                            Log.e(LOG_TAG, "Failed to talk to package manager", e);
                            i3++;
                            notificationManager2 = notificationManager;
                            users = list;
                        }
                    } catch (RemoteException e2) {
                        e = e2;
                        list = users;
                    }
                    if (asInterface.getBlockUninstallForUser(applicationInfo.packageName, userInfo2.id)) {
                        i2 = userInfo2.id;
                        break;
                    }
                    continue;
                    i3++;
                    notificationManager2 = notificationManager;
                    users = list;
                }
                if (isProfileOfOrSame(userManager, UserHandle.myUserId(), i2)) {
                    addDeviceManagerButton(context, builder);
                } else {
                    addManageUsersButton(context, builder);
                }
                if (i2 == -10000) {
                    String str2 = LOG_TAG;
                    Log.d(str2, "Uninstall failed for " + applicationInfo.packageName + " with code " + intExtra + " no blocking user");
                } else if (i2 == 0) {
                    setBigText(builder, context.getString(R.string.uninstall_blocked_device_owner));
                } else if (booleanExtra) {
                    setBigText(builder, context.getString(R.string.uninstall_all_blocked_profile_owner));
                } else {
                    setBigText(builder, context.getString(R.string.uninstall_blocked_profile_owner));
                }
                builder.setContentTitle(context.getString(R.string.uninstall_failed_app, stringExtra));
                builder.setOngoing(false);
                builder.setSmallIcon(R.drawable.ic_error);
                notificationManager.notify(i, builder.build());
            } else if (intExtra3 == -2) {
                IDevicePolicyManager asInterface2 = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
                int myUserId = UserHandle.myUserId();
                Iterator it = userManager.getUsers().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        userInfo = null;
                        break;
                    }
                    userInfo = (UserInfo) it.next();
                    if (!isProfileOfOrSame(userManager, myUserId, userInfo.id)) {
                        try {
                            if (asInterface2.packageHasActiveAdmins(applicationInfo.packageName, userInfo.id)) {
                                break;
                            }
                        } catch (RemoteException e3) {
                            Log.e(LOG_TAG, "Failed to talk to package manager", e3);
                        }
                    }
                }
                if (userInfo == null) {
                    String str3 = LOG_TAG;
                    Log.d(str3, "Uninstall failed because " + applicationInfo.packageName + " is a device admin");
                    addDeviceManagerButton(context, builder);
                    setBigText(builder, context.getString(R.string.uninstall_failed_device_policy_manager));
                } else {
                    String str4 = LOG_TAG;
                    Log.d(str4, "Uninstall failed because " + applicationInfo.packageName + " is a device admin of user " + userInfo);
                    setBigText(builder, String.format(context.getString(R.string.uninstall_failed_device_policy_manager_of_user), userInfo.name));
                }
            } else {
                String str5 = LOG_TAG;
                Log.d(str5, "Uninstall blocked for " + applicationInfo.packageName + " with legacy code " + intExtra3);
            }
        } else {
            String str6 = LOG_TAG;
            Log.d(str6, "Uninstall failed for " + applicationInfo.packageName + " with code " + intExtra);
        }
        i = intExtra2;
        notificationManager = notificationManager2;
        builder.setContentTitle(context.getString(R.string.uninstall_failed_app, stringExtra));
        builder.setOngoing(false);
        builder.setSmallIcon(R.drawable.ic_error);
        notificationManager.notify(i, builder.build());
    }

    private boolean isProfileOfOrSame(UserManager userManager, int i, int i2) {
        if (i == i2) {
            return true;
        }
        UserInfo profileParent = userManager.getProfileParent(i2);
        return profileParent != null && profileParent.id == i;
    }

    private void setBigText(Notification.Builder builder, CharSequence charSequence) {
        builder.setStyle(new Notification.BigTextStyle().bigText(charSequence));
    }

    private void addManageUsersButton(Context context, Notification.Builder builder) {
        Intent intent = new Intent("android.settings.USER_SETTINGS");
        intent.setFlags(1342177280);
        builder.addAction(new Notification.Action.Builder(Icon.createWithResource(context, (int) R.drawable.ic_settings_multiuser), context.getString(R.string.manage_users), PendingIntent.getActivity(context, 0, intent, 134217728)).build());
    }

    private void addDeviceManagerButton(Context context, Notification.Builder builder) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.Settings$DeviceAdminSettingsActivity");
        intent.setFlags(1342177280);
        builder.addAction(new Notification.Action.Builder(Icon.createWithResource(context, (int) R.drawable.ic_lock), context.getString(R.string.manage_device_administrators), PendingIntent.getActivity(context, 0, intent, 134217728)).build());
    }
}
