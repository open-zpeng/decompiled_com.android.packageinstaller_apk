package com.android.packageinstaller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
/* loaded from: classes.dex */
class PackageInstalledNotificationUtils {
    private static final String TAG = "PackageInstalledNotificationUtils";
    private final String mChannelId;
    private final Context mContext;
    private final String mInstalledAppLabel;
    private final Icon mInstalledAppLargeIcon;
    private final String mInstalledPackage;
    private final Integer mInstallerAppColor;
    private final String mInstallerAppLabel;
    private final Icon mInstallerAppSmallIcon;
    private final String mInstallerPackage;
    private final NotificationManager mNotificationManager;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PackageInstalledNotificationUtils(Context context, String str, String str2) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(str, 128);
            try {
                ApplicationInfo applicationInfo2 = context.getPackageManager().getApplicationInfo(str2, 128);
                this.mInstallerPackage = str;
                this.mInstallerAppLabel = getAppLabel(context, applicationInfo, str);
                this.mInstallerAppSmallIcon = getAppNotificationIcon(context, applicationInfo);
                this.mInstallerAppColor = getAppNotificationColor(context, applicationInfo);
                this.mInstalledPackage = str2;
                this.mInstalledAppLabel = getAppLabel(context, applicationInfo2, str);
                this.mInstalledAppLargeIcon = getAppLargeIcon(applicationInfo2);
                this.mChannelId = "INSTALLER:" + str;
            } catch (PackageManager.NameNotFoundException unused) {
                throw new IllegalStateException("Unable to get application info: " + str2);
            }
        } catch (PackageManager.NameNotFoundException unused2) {
            throw new IllegalStateException("Unable to get application info: " + str);
        }
    }

    private static String getAppLabel(Context context, ApplicationInfo applicationInfo, String str) {
        String charSequence = applicationInfo.loadSafeLabel(context.getPackageManager(), 500.0f, 5).toString();
        return charSequence != null ? charSequence.toString() : str;
    }

    private static Icon getAppLargeIcon(ApplicationInfo applicationInfo) {
        if (applicationInfo.icon != 0) {
            return Icon.createWithResource(applicationInfo.packageName, applicationInfo.icon);
        }
        return Icon.createWithResource("android", 17301651);
    }

    private static Icon getAppNotificationIcon(Context context, ApplicationInfo applicationInfo) {
        if (applicationInfo.metaData == null) {
            return Icon.createWithResource(context, (int) R.drawable.ic_file_download);
        }
        int i = applicationInfo.metaData.getInt("com.android.packageinstaller.notification.smallIcon", 0);
        if (i != 0) {
            return Icon.createWithResource(applicationInfo.packageName, i);
        }
        return Icon.createWithResource(context, (int) R.drawable.ic_file_download);
    }

    private static Integer getAppNotificationColor(Context context, ApplicationInfo applicationInfo) {
        int i;
        if (applicationInfo.metaData != null && (i = applicationInfo.metaData.getInt("com.android.packageinstaller.notification.color", 0)) != 0) {
            try {
                return Integer.valueOf(context.getPackageManager().getResourcesForApplication(applicationInfo.packageName).getColor(i, context.getTheme()));
            } catch (PackageManager.NameNotFoundException unused) {
                String str = TAG;
                Log.e(str, "Error while loading notification color: " + i + " for " + applicationInfo.packageName);
            }
        }
        return null;
    }

    private static Intent getAppDetailIntent(String str) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(Uri.fromParts("package", str, null));
        return intent;
    }

    private static Intent resolveIntent(Context context, Intent intent) {
        ResolveInfo resolveActivity = context.getPackageManager().resolveActivity(intent, 0);
        if (resolveActivity == null) {
            return null;
        }
        return new Intent(intent.getAction()).setClassName(resolveActivity.activityInfo.packageName, resolveActivity.activityInfo.name);
    }

    private static Intent getAppStoreLink(Context context, String str, String str2) {
        Intent resolveIntent = resolveIntent(context, new Intent("android.intent.action.SHOW_APP_INFO").setPackage(str));
        if (resolveIntent != null) {
            resolveIntent.putExtra("android.intent.extra.PACKAGE_NAME", str2);
            return resolveIntent;
        }
        return null;
    }

    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(this.mChannelId, this.mInstallerAppLabel, 3);
        notificationChannel.setDescription(this.mContext.getString(R.string.app_installed_notification_channel_description));
        notificationChannel.enableVibration(false);
        notificationChannel.setSound(null, null);
        notificationChannel.setLockscreenVisibility(0);
        notificationChannel.setBlockableSystem(true);
        this.mNotificationManager.createNotificationChannel(notificationChannel);
    }

    private PendingIntent getInstalledAppLaunchIntent() {
        Intent launchIntentForPackage = this.mContext.getPackageManager().getLaunchIntentForPackage(this.mInstalledPackage);
        if (launchIntentForPackage == null) {
            launchIntentForPackage = getAppStoreLink(this.mContext, this.mInstallerPackage, this.mInstalledPackage);
        }
        if (launchIntentForPackage == null) {
            launchIntentForPackage = getAppDetailIntent(this.mInstalledPackage);
        }
        launchIntentForPackage.setFlags(268435456);
        return PendingIntent.getActivity(this.mContext, 0, launchIntentForPackage, 134217728);
    }

    private PendingIntent getInstallerEntranceIntent() {
        Intent launchIntentForPackage = this.mContext.getPackageManager().getLaunchIntentForPackage(this.mInstallerPackage);
        if (launchIntentForPackage == null) {
            launchIntentForPackage = getAppDetailIntent(this.mInstallerPackage);
        }
        launchIntentForPackage.setFlags(268435456);
        return PendingIntent.getActivity(this.mContext, 0, launchIntentForPackage, 134217728);
    }

    private Notification.Builder getGroupNotificationBuilder() {
        PendingIntent installerEntranceIntent = getInstallerEntranceIntent();
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", this.mInstallerAppLabel);
        Notification.Builder groupSummary = new Notification.Builder(this.mContext, this.mChannelId).setSmallIcon(this.mInstallerAppSmallIcon).setGroup(this.mChannelId).setExtras(bundle).setLocalOnly(true).setCategory("status").setContentIntent(installerEntranceIntent).setGroupSummary(true);
        Integer num = this.mInstallerAppColor;
        if (num != null) {
            groupSummary.setColor(num.intValue());
        }
        return groupSummary;
    }

    private Notification.Builder getAppInstalledNotificationBuilder() {
        PendingIntent installedAppLaunchIntent = getInstalledAppLaunchIntent();
        Bundle bundle = new Bundle();
        bundle.putString("android.substName", this.mInstallerAppLabel);
        Notification.Builder style = new Notification.Builder(this.mContext, this.mChannelId).setAutoCancel(true).setSmallIcon(this.mInstallerAppSmallIcon).setContentTitle(this.mInstalledAppLabel).setContentText(this.mContext.getString(R.string.notification_installation_success_message)).setContentIntent(installedAppLaunchIntent).setTicker(String.format(this.mContext.getString(R.string.notification_installation_success_status), this.mInstalledAppLabel)).setCategory("status").setShowWhen(true).setWhen(System.currentTimeMillis()).setLocalOnly(true).setGroup(this.mChannelId).addExtras(bundle).setStyle(new Notification.BigTextStyle());
        Icon icon = this.mInstalledAppLargeIcon;
        if (icon != null) {
            style.setLargeIcon(icon);
        }
        Integer num = this.mInstallerAppColor;
        if (num != null) {
            style.setColor(num.intValue());
        }
        return style;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void postAppInstalledNotification() {
        createChannel();
        Notification.Builder appInstalledNotificationBuilder = getAppInstalledNotificationBuilder();
        NotificationManager notificationManager = this.mNotificationManager;
        String str = this.mInstalledPackage;
        notificationManager.notify(str, str.hashCode(), appInstalledNotificationBuilder.build());
        Notification.Builder groupNotificationBuilder = getGroupNotificationBuilder();
        NotificationManager notificationManager2 = this.mNotificationManager;
        String str2 = this.mInstallerPackage;
        notificationManager2.notify(str2, str2.hashCode(), groupNotificationBuilder.build());
    }
}
