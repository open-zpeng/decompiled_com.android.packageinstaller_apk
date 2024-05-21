package com.android.packageinstaller.television;

import android.app.Activity;
import android.app.admin.IDevicePolicyManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.widget.Toast;
import com.android.packageinstaller.R;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
/* loaded from: classes.dex */
public class UninstallAppProgress extends Activity {
    private boolean mAllUsers;
    private ApplicationInfo mAppInfo;
    private IBinder mCallback;
    private boolean mIsViewInitialized;
    private volatile int mResultCode = -1;
    private Handler mHandler = new MessageHandler(this);

    /* loaded from: classes.dex */
    public interface ProgressFragment {
        void setDeviceManagerButtonVisible(boolean z);

        void setUsersButtonVisible(boolean z);

        void showCompletion(CharSequence charSequence);
    }

    /* loaded from: classes.dex */
    private static class MessageHandler extends Handler {
        private final WeakReference<UninstallAppProgress> mActivity;

        public MessageHandler(UninstallAppProgress uninstallAppProgress) {
            this.mActivity = new WeakReference<>(uninstallAppProgress);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            UninstallAppProgress uninstallAppProgress = this.mActivity.get();
            if (uninstallAppProgress != null) {
                uninstallAppProgress.handleMessage(message);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleMessage(Message message) {
        int i;
        String string;
        UserInfo userInfo;
        if (isFinishing() || isDestroyed()) {
            return;
        }
        int i2 = message.what;
        if (i2 != 1) {
            if (i2 != 2) {
                return;
            }
            initView();
            return;
        }
        this.mHandler.removeMessages(2);
        if (message.arg1 != 1) {
            initView();
        }
        this.mResultCode = message.arg1;
        String str = (String) message.obj;
        IBinder iBinder = this.mCallback;
        if (iBinder != null) {
            try {
                IPackageDeleteObserver2.Stub.asInterface(iBinder).onPackageDeleted(this.mAppInfo.packageName, this.mResultCode, str);
            } catch (RemoteException unused) {
            }
            finish();
        } else if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
            Intent intent = new Intent();
            intent.putExtra("android.intent.extra.INSTALL_RESULT", this.mResultCode);
            setResult(this.mResultCode == 1 ? -1 : 1, intent);
            finish();
        } else {
            int i3 = message.arg1;
            if (i3 == -4) {
                UserManager userManager = (UserManager) getSystemService("user");
                IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
                List users = userManager.getUsers();
                int i4 = 0;
                while (true) {
                    if (i4 >= users.size()) {
                        i = -10000;
                        break;
                    }
                    UserInfo userInfo2 = (UserInfo) users.get(i4);
                    try {
                    } catch (RemoteException e) {
                        Log.e("UninstallAppProgress", "Failed to talk to package manager", e);
                    }
                    if (asInterface.getBlockUninstallForUser(str, userInfo2.id)) {
                        i = userInfo2.id;
                        break;
                    } else {
                        continue;
                        i4++;
                    }
                }
                if (isProfileOfOrSame(userManager, UserHandle.myUserId(), i)) {
                    getProgressFragment().setDeviceManagerButtonVisible(true);
                } else {
                    getProgressFragment().setDeviceManagerButtonVisible(false);
                    getProgressFragment().setUsersButtonVisible(true);
                }
                if (i == 0) {
                    string = getString(R.string.uninstall_blocked_device_owner);
                } else if (i == -10000) {
                    Log.d("UninstallAppProgress", "Uninstall failed for " + str + " with code " + message.arg1 + " no blocking user");
                    string = getString(R.string.uninstall_failed);
                } else if (this.mAllUsers) {
                    string = getString(R.string.uninstall_all_blocked_profile_owner);
                } else {
                    string = getString(R.string.uninstall_blocked_profile_owner);
                }
            } else if (i3 == -2) {
                UserManager userManager2 = (UserManager) getSystemService("user");
                IDevicePolicyManager asInterface2 = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
                int myUserId = UserHandle.myUserId();
                Iterator it = userManager2.getUsers().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        userInfo = null;
                        break;
                    }
                    userInfo = (UserInfo) it.next();
                    if (!isProfileOfOrSame(userManager2, myUserId, userInfo.id)) {
                        try {
                            if (asInterface2.packageHasActiveAdmins(str, userInfo.id)) {
                                break;
                            }
                        } catch (RemoteException e2) {
                            Log.e("UninstallAppProgress", "Failed to talk to package manager", e2);
                        }
                    }
                }
                if (userInfo == null) {
                    Log.d("UninstallAppProgress", "Uninstall failed because " + str + " is a device admin");
                    getProgressFragment().setDeviceManagerButtonVisible(true);
                    string = getString(R.string.uninstall_failed_device_policy_manager);
                } else {
                    Log.d("UninstallAppProgress", "Uninstall failed because " + str + " is a device admin of user " + userInfo);
                    getProgressFragment().setDeviceManagerButtonVisible(false);
                    string = String.format(getString(R.string.uninstall_failed_device_policy_manager_of_user), userInfo.name);
                }
            } else if (i3 == 1) {
                Toast.makeText(getBaseContext(), getString(R.string.uninstall_done), 1).show();
                setResultAndFinish();
                return;
            } else {
                Log.d("UninstallAppProgress", "Uninstall failed for " + str + " with code " + message.arg1);
                string = getString(R.string.uninstall_failed);
            }
            getProgressFragment().showCompletion(string);
        }
    }

    private boolean isProfileOfOrSame(UserManager userManager, int i, int i2) {
        if (i == i2) {
            return true;
        }
        UserInfo profileParent = userManager.getProfileParent(i2);
        return profileParent != null && profileParent.id == i;
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        this.mAppInfo = (ApplicationInfo) intent.getParcelableExtra("com.android.packageinstaller.applicationInfo");
        this.mCallback = intent.getIBinderExtra("android.content.pm.extra.CALLBACK");
        if (bundle != null) {
            this.mResultCode = -1;
            IBinder iBinder = this.mCallback;
            if (iBinder != null) {
                try {
                    IPackageDeleteObserver2.Stub.asInterface(iBinder).onPackageDeleted(this.mAppInfo.packageName, this.mResultCode, (String) null);
                } catch (RemoteException unused) {
                }
                finish();
                return;
            }
            setResultAndFinish();
            return;
        }
        this.mAllUsers = intent.getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
        UserHandle userHandle = (UserHandle) intent.getParcelableExtra("android.intent.extra.USER");
        if (userHandle == null) {
            userHandle = Process.myUserHandle();
        }
        PackageDeleteObserver packageDeleteObserver = new PackageDeleteObserver();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getWindow().setStatusBarColor(0);
        getWindow().setNavigationBarColor(0);
        try {
            getPackageManager().deletePackageAsUser(this.mAppInfo.packageName, packageDeleteObserver, this.mAllUsers ? 2 : 0, userHandle.getIdentifier());
        } catch (IllegalArgumentException e) {
            Log.w("UninstallAppProgress", "Could not find package, not deleting " + this.mAppInfo.packageName, e);
        }
        Handler handler = this.mHandler;
        handler.sendMessageDelayed(handler.obtainMessage(2), 500L);
    }

    public ApplicationInfo getAppInfo() {
        return this.mAppInfo;
    }

    /* loaded from: classes.dex */
    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        private PackageDeleteObserver() {
        }

        public void packageDeleted(String str, int i) {
            Message obtainMessage = UninstallAppProgress.this.mHandler.obtainMessage(1);
            obtainMessage.arg1 = i;
            obtainMessage.obj = str;
            UninstallAppProgress.this.mHandler.sendMessage(obtainMessage);
        }
    }

    public void setResultAndFinish() {
        setResult(this.mResultCode);
        finish();
    }

    private void initView() {
        if (this.mIsViewInitialized) {
            return;
        }
        this.mIsViewInitialized = true;
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(16842836, typedValue, true);
        int i = typedValue.type;
        if (i >= 28 && i <= 31) {
            getWindow().setBackgroundDrawable(new ColorDrawable(typedValue.data));
        } else {
            getWindow().setBackgroundDrawable(getResources().getDrawable(typedValue.resourceId, getTheme()));
        }
        getTheme().resolveAttribute(16843858, typedValue, true);
        getWindow().setNavigationBarColor(typedValue.data);
        getTheme().resolveAttribute(16843857, typedValue, true);
        getWindow().setStatusBarColor(typedValue.data);
        setTitle((this.mAppInfo.flags & 128) != 0 ? R.string.uninstall_update_title : R.string.uninstall_application_title);
        getFragmentManager().beginTransaction().add(16908290, new UninstallAppProgressFragment(), "progress_fragment").commitNowAllowingStateLoss();
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 4) {
            if (this.mResultCode == -1) {
                return true;
            }
            setResult(this.mResultCode);
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    private ProgressFragment getProgressFragment() {
        return (ProgressFragment) getFragmentManager().findFragmentByTag("progress_fragment");
    }
}
