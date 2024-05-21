package com.android.packageinstaller;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.VersionedPackage;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.packageinstaller.EventResultPersister;
import com.android.packageinstaller.handheld.ErrorDialogFragment;
import com.android.packageinstaller.handheld.UninstallAlertDialogFragment;
import com.android.packageinstaller.television.ErrorFragment;
import com.android.packageinstaller.television.UninstallAlertFragment;
import com.android.packageinstaller.television.UninstallAppProgress;
/* loaded from: classes.dex */
public class UninstallerActivity extends Activity {
    private DialogInfo mDialogInfo;
    private String mPackageName;

    /* loaded from: classes.dex */
    public static class DialogInfo {
        public ActivityInfo activityInfo;
        public boolean allUsers;
        public ApplicationInfo appInfo;
        public IBinder callback;
        public UserHandle user;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        getWindow().addSystemFlags(524288);
        super.onCreate(null);
        try {
            int launchedFromUid = ActivityManager.getService().getLaunchedFromUid(getActivityToken());
            String packageNameForUid = getPackageNameForUid(launchedFromUid);
            if (packageNameForUid == null) {
                Log.e("UninstallerActivity", "Package not found for originating uid " + launchedFromUid);
                setResult(1);
                finish();
            } else if (((AppOpsManager) getSystemService("appops")).noteOpNoThrow("android:request_delete_packages", launchedFromUid, packageNameForUid) != 0) {
                Log.e("UninstallerActivity", "Install from uid " + launchedFromUid + " disallowed by AppOps");
                setResult(1);
                finish();
            } else if (PackageUtil.getMaxTargetSdkVersionForUid(this, launchedFromUid) >= 28 && AppGlobals.getPackageManager().checkUidPermission("android.permission.REQUEST_DELETE_PACKAGES", launchedFromUid) != 0 && AppGlobals.getPackageManager().checkUidPermission("android.permission.DELETE_PACKAGES", launchedFromUid) != 0) {
                Log.e("UninstallerActivity", "Uid " + launchedFromUid + " does not have android.permission.REQUEST_DELETE_PACKAGES or android.permission.DELETE_PACKAGES");
                setResult(1);
                finish();
            } else {
                Intent intent = getIntent();
                Uri data = intent.getData();
                if (data == null) {
                    Log.e("UninstallerActivity", "No package URI in intent");
                    showAppNotFound();
                    return;
                }
                this.mPackageName = data.getEncodedSchemeSpecificPart();
                if (this.mPackageName == null) {
                    Log.e("UninstallerActivity", "Invalid package name in URI: " + data);
                    showAppNotFound();
                    return;
                }
                IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                this.mDialogInfo = new DialogInfo();
                this.mDialogInfo.allUsers = intent.getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
                if (this.mDialogInfo.allUsers && !UserManager.get(this).isAdminUser()) {
                    Log.e("UninstallerActivity", "Only admin user can request uninstall for all users");
                    showUserIsNotAllowed();
                    return;
                }
                this.mDialogInfo.user = (UserHandle) intent.getParcelableExtra("android.intent.extra.USER");
                DialogInfo dialogInfo = this.mDialogInfo;
                if (dialogInfo.user == null) {
                    dialogInfo.user = Process.myUserHandle();
                } else if (!((UserManager) getSystemService("user")).getUserProfiles().contains(this.mDialogInfo.user)) {
                    Log.e("UninstallerActivity", "User " + Process.myUserHandle() + " can't request uninstall for user " + this.mDialogInfo.user);
                    showUserIsNotAllowed();
                    return;
                }
                this.mDialogInfo.callback = intent.getIBinderExtra("android.content.pm.extra.CALLBACK");
                try {
                    this.mDialogInfo.appInfo = asInterface.getApplicationInfo(this.mPackageName, 4194304, this.mDialogInfo.user.getIdentifier());
                } catch (RemoteException unused) {
                    Log.e("UninstallerActivity", "Unable to get packageName. Package manager is dead?");
                }
                if (this.mDialogInfo.appInfo == null) {
                    Log.e("UninstallerActivity", "Invalid packageName: " + this.mPackageName);
                    showAppNotFound();
                    return;
                }
                String fragment = data.getFragment();
                if (fragment != null) {
                    try {
                        this.mDialogInfo.activityInfo = asInterface.getActivityInfo(new ComponentName(this.mPackageName, fragment), 0, this.mDialogInfo.user.getIdentifier());
                    } catch (RemoteException unused2) {
                        Log.e("UninstallerActivity", "Unable to get className. Package manager is dead?");
                    }
                }
                showConfirmationDialog();
            }
        } catch (RemoteException unused3) {
            Log.e("UninstallerActivity", "Could not determine the launching uid.");
            setResult(1);
            finish();
        }
    }

    public DialogInfo getDialogInfo() {
        return this.mDialogInfo;
    }

    private void showConfirmationDialog() {
        if (isTv()) {
            showContentFragment(new UninstallAlertFragment(), 0, 0);
        } else {
            showDialogFragment(new UninstallAlertDialogFragment(), 0, 0);
        }
    }

    private void showAppNotFound() {
        if (isTv()) {
            showContentFragment(new ErrorFragment(), R.string.app_not_found_dlg_title, R.string.app_not_found_dlg_text);
        } else {
            showDialogFragment(new ErrorDialogFragment(), R.string.app_not_found_dlg_title, R.string.app_not_found_dlg_text);
        }
    }

    private void showUserIsNotAllowed() {
        if (isTv()) {
            showContentFragment(new ErrorFragment(), R.string.user_is_not_allowed_dlg_title, R.string.user_is_not_allowed_dlg_text);
        } else {
            showDialogFragment(new ErrorDialogFragment(), 0, R.string.user_is_not_allowed_dlg_text);
        }
    }

    private void showGenericError() {
        if (isTv()) {
            showContentFragment(new ErrorFragment(), R.string.generic_error_dlg_title, R.string.generic_error_dlg_text);
        } else {
            showDialogFragment(new ErrorDialogFragment(), 0, R.string.generic_error_dlg_text);
        }
    }

    private boolean isTv() {
        return (getResources().getConfiguration().uiMode & 15) == 4;
    }

    private void showContentFragment(Fragment fragment, int i, int i2) {
        Bundle bundle = new Bundle();
        bundle.putInt("com.android.packageinstaller.arg.title", i);
        bundle.putInt("com.android.packageinstaller.arg.text", i2);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(16908290, fragment).commit();
    }

    private void showDialogFragment(DialogFragment dialogFragment, int i, int i2) {
        FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
        Fragment findFragmentByTag = getFragmentManager().findFragmentByTag("dialog");
        if (findFragmentByTag != null) {
            beginTransaction.remove(findFragmentByTag);
        }
        Bundle bundle = new Bundle();
        if (i != 0) {
            bundle.putInt("com.android.packageinstaller.arg.title", i);
        }
        bundle.putInt("com.android.packageinstaller.arg.text", i2);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(beginTransaction, "dialog");
    }

    public void startUninstallProgress(boolean z) {
        boolean booleanExtra = getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false);
        CharSequence loadSafeLabel = this.mDialogInfo.appInfo.loadSafeLabel(getPackageManager());
        if (isTv()) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.putExtra("android.intent.extra.USER", this.mDialogInfo.user);
            intent.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", this.mDialogInfo.allUsers);
            intent.putExtra("android.content.pm.extra.CALLBACK", this.mDialogInfo.callback);
            intent.putExtra("com.android.packageinstaller.applicationInfo", this.mDialogInfo.appInfo);
            if (booleanExtra) {
                intent.putExtra("android.intent.extra.RETURN_RESULT", true);
                intent.addFlags(33554432);
            }
            intent.setClass(this, UninstallAppProgress.class);
            startActivity(intent);
        } else if (booleanExtra || this.mDialogInfo.callback != null || getCallingActivity() != null) {
            Intent intent2 = new Intent(this, UninstallUninstalling.class);
            intent2.putExtra("android.intent.extra.USER", this.mDialogInfo.user);
            intent2.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", this.mDialogInfo.allUsers);
            intent2.putExtra("com.android.packageinstaller.applicationInfo", this.mDialogInfo.appInfo);
            intent2.putExtra("com.android.packageinstaller.extra.APP_LABEL", loadSafeLabel);
            intent2.putExtra("com.android.packageinstaller.extra.KEEP_DATA", z);
            intent2.putExtra("android.content.pm.extra.CALLBACK", this.mDialogInfo.callback);
            if (booleanExtra) {
                intent2.putExtra("android.intent.extra.RETURN_RESULT", true);
            }
            if (booleanExtra || getCallingActivity() != null) {
                intent2.addFlags(33554432);
            }
            startActivity(intent2);
        } else {
            try {
                int newId = UninstallEventReceiver.getNewId(this);
                Intent intent3 = new Intent(this, UninstallFinish.class);
                intent3.setFlags(268435456);
                intent3.putExtra("android.intent.extra.UNINSTALL_ALL_USERS", this.mDialogInfo.allUsers);
                intent3.putExtra("com.android.packageinstaller.applicationInfo", this.mDialogInfo.appInfo);
                intent3.putExtra("com.android.packageinstaller.extra.APP_LABEL", loadSafeLabel);
                intent3.putExtra("com.android.packageinstaller.extra.UNINSTALL_ID", newId);
                PendingIntent broadcast = PendingIntent.getBroadcast(this, newId, intent3, 134217728);
                NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(new NotificationChannel("uninstalling", getString(R.string.uninstalling_notification_channel), 1));
                notificationManager.notify(newId, new Notification.Builder(this, "uninstalling").setSmallIcon(R.drawable.ic_remove).setProgress(0, 1, true).setContentTitle(getString(R.string.uninstalling_app, new Object[]{loadSafeLabel})).setOngoing(true).build());
                try {
                    Log.i("UninstallerActivity", "Uninstalling extras=" + intent3.getExtras());
                    ActivityThread.getPackageManager().getPackageInstaller().uninstall(new VersionedPackage(this.mDialogInfo.appInfo.packageName, -1), getPackageName(), (this.mDialogInfo.allUsers ? 2 : 0) | (z ? 1 : 0), broadcast.getIntentSender(), this.mDialogInfo.user.getIdentifier());
                } catch (Exception e) {
                    notificationManager.cancel(newId);
                    Log.e("UninstallerActivity", "Cannot start uninstall", e);
                    showGenericError();
                }
            } catch (EventResultPersister.OutOfIdsException unused) {
                showGenericError();
            }
        }
    }

    public void dispatchAborted() {
        IBinder iBinder;
        DialogInfo dialogInfo = this.mDialogInfo;
        if (dialogInfo == null || (iBinder = dialogInfo.callback) == null) {
            return;
        }
        try {
            IPackageDeleteObserver2.Stub.asInterface(iBinder).onPackageDeleted(this.mPackageName, -5, "Cancelled by user");
        } catch (RemoteException unused) {
        }
    }

    private String getPackageNameForUid(int i) {
        String[] packagesForUid = getPackageManager().getPackagesForUid(i);
        if (packagesForUid == null) {
            return null;
        }
        return packagesForUid[0];
    }
}
