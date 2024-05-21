package com.android.packageinstaller;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.VersionedPackage;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Toast;
import com.android.packageinstaller.EventResultPersister;
/* loaded from: classes.dex */
public class UninstallUninstalling extends Activity implements EventResultPersister.EventResultObserver {
    private static final String LOG_TAG = "UninstallUninstalling";
    private ApplicationInfo mAppInfo;
    private IBinder mCallback;
    private String mLabel;
    private boolean mReturnResult;
    private int mUninstallId;

    @Override // android.app.Activity
    public void onBackPressed() {
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setFinishOnTouchOutside(false);
        this.mAppInfo = (ApplicationInfo) getIntent().getParcelableExtra("com.android.packageinstaller.applicationInfo");
        this.mCallback = getIntent().getIBinderExtra("android.content.pm.extra.CALLBACK");
        this.mReturnResult = getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false);
        this.mLabel = getIntent().getStringExtra("com.android.packageinstaller.extra.APP_LABEL");
        try {
            if (bundle == null) {
                boolean booleanExtra = getIntent().getBooleanExtra("android.intent.extra.UNINSTALL_ALL_USERS", false);
                boolean booleanExtra2 = getIntent().getBooleanExtra("com.android.packageinstaller.extra.KEEP_DATA", false);
                UserHandle userHandle = (UserHandle) getIntent().getParcelableExtra("android.intent.extra.USER");
                FragmentTransaction beginTransaction = getFragmentManager().beginTransaction();
                Fragment findFragmentByTag = getFragmentManager().findFragmentByTag("dialog");
                if (findFragmentByTag != null) {
                    beginTransaction.remove(findFragmentByTag);
                }
                UninstallUninstallingFragment uninstallUninstallingFragment = new UninstallUninstallingFragment();
                uninstallUninstallingFragment.setCancelable(false);
                uninstallUninstallingFragment.show(beginTransaction, "dialog");
                this.mUninstallId = UninstallEventReceiver.addObserver(this, Integer.MIN_VALUE, this);
                Intent intent = new Intent("com.android.packageinstaller.ACTION_UNINSTALL_COMMIT");
                intent.setFlags(268435456);
                intent.putExtra("EventResultPersister.EXTRA_ID", this.mUninstallId);
                intent.setPackage(getPackageName());
                try {
                    ActivityThread.getPackageManager().getPackageInstaller().uninstall(new VersionedPackage(this.mAppInfo.packageName, -1), getPackageName(), (booleanExtra ? 2 : 0) | booleanExtra2, PendingIntent.getBroadcast(this, this.mUninstallId, intent, 134217728).getIntentSender(), userHandle.getIdentifier());
                    return;
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                    return;
                }
            }
            this.mUninstallId = bundle.getInt("com.android.packageinstaller.UNINSTALL_ID");
            UninstallEventReceiver.addObserver(this, this.mUninstallId, this);
        } catch (EventResultPersister.OutOfIdsException | IllegalArgumentException e2) {
            Log.e(LOG_TAG, "Fails to start uninstall", e2);
            onResult(1, -1, null);
        }
    }

    @Override // android.app.Activity
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("com.android.packageinstaller.UNINSTALL_ID", this.mUninstallId);
    }

    @Override // com.android.packageinstaller.EventResultPersister.EventResultObserver
    public void onResult(int i, int i2, String str) {
        IBinder iBinder = this.mCallback;
        if (iBinder != null) {
            try {
                IPackageDeleteObserver2.Stub.asInterface(iBinder).onPackageDeleted(this.mAppInfo.packageName, i2, str);
            } catch (RemoteException unused) {
            }
        } else {
            if (this.mReturnResult) {
                Intent intent = new Intent();
                intent.putExtra("android.intent.extra.INSTALL_RESULT", i2);
                setResult(i == 0 ? -1 : 1, intent);
            } else if (i != 0) {
                Toast.makeText(this, getString(R.string.uninstall_failed_app, new Object[]{this.mLabel}), 1).show();
            }
        }
        finish();
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        UninstallEventReceiver.removeObserver(this, this.mUninstallId);
        super.onDestroy();
    }

    /* loaded from: classes.dex */
    public static class UninstallUninstallingFragment extends DialogFragment {
        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setMessage(getActivity().getString(R.string.uninstalling_app, new Object[]{((UninstallUninstalling) getActivity()).mLabel}));
            AlertDialog create = builder.create();
            create.setCanceledOnTouchOutside(false);
            return create;
        }
    }
}
