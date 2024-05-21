package com.android.packageinstaller.handheld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.usage.StorageStatsManager;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.packageinstaller.R;
import com.android.packageinstaller.UninstallerActivity;
import java.io.IOException;
import java.util.List;
/* loaded from: classes.dex */
public class UninstallAlertDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private static final String LOG_TAG = "UninstallAlertDialogFragment";
    private CheckBox mKeepData;

    private long getAppDataSizeForUser(String str, UserHandle userHandle) {
        StorageStatsManager storageStatsManager = (StorageStatsManager) getContext().getSystemService(StorageStatsManager.class);
        List<StorageVolume> storageVolumes = ((StorageManager) getContext().getSystemService(StorageManager.class)).getStorageVolumes();
        int size = storageVolumes.size();
        long j = 0;
        for (int i = 0; i < size; i++) {
            try {
                j += storageStatsManager.queryStatsForPackage(StorageManager.convert(storageVolumes.get(i).getUuid()), str, userHandle).getDataBytes();
            } catch (PackageManager.NameNotFoundException | IOException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "Cannot determine amount of app data for " + str + " on " + storageVolumes.get(i) + " (user " + userHandle + ")", e);
            }
        }
        return j;
    }

    private long getAppDataSize(String str, UserHandle userHandle) {
        UserManager userManager = (UserManager) getContext().getSystemService(UserManager.class);
        if (userHandle == null) {
            List users = userManager.getUsers();
            int size = users.size();
            long j = 0;
            for (int i = 0; i < size; i++) {
                j += getAppDataSizeForUser(str, UserHandle.of(((UserInfo) users.get(i)).id));
            }
            return j;
        }
        return getAppDataSizeForUser(str, userHandle);
    }

    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        boolean z;
        long j;
        PackageManager packageManager = getActivity().getPackageManager();
        UninstallerActivity.DialogInfo dialogInfo = ((UninstallerActivity) getActivity()).getDialogInfo();
        CharSequence loadSafeLabel = dialogInfo.appInfo.loadSafeLabel(packageManager);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        StringBuilder sb = new StringBuilder();
        ActivityInfo activityInfo = dialogInfo.activityInfo;
        if (activityInfo != null) {
            Object loadSafeLabel2 = activityInfo.loadSafeLabel(packageManager);
            if (!loadSafeLabel2.equals(loadSafeLabel)) {
                sb.append(getString(R.string.uninstall_activity_text, loadSafeLabel2));
                sb.append(" ");
                sb.append(loadSafeLabel);
                sb.append(".\n\n");
            }
        }
        boolean z2 = (dialogInfo.appInfo.flags & 128) != 0;
        UserManager userManager = UserManager.get(getActivity());
        if (z2) {
            if (isSingleUser(userManager)) {
                sb.append(getString(R.string.uninstall_update_text));
            } else {
                sb.append(getString(R.string.uninstall_update_text_multiuser));
            }
        } else if (dialogInfo.allUsers && !isSingleUser(userManager)) {
            sb.append(getString(R.string.uninstall_application_text_all_users));
        } else if (!dialogInfo.user.equals(Process.myUserHandle())) {
            sb.append(getString(R.string.uninstall_application_text_user, userManager.getUserInfo(dialogInfo.user.getIdentifier()).name));
        } else {
            sb.append(getString(R.string.uninstall_application_text));
        }
        builder.setTitle(loadSafeLabel);
        builder.setPositiveButton(17039370, this);
        builder.setNegativeButton(17039360, this);
        String str = dialogInfo.appInfo.packageName;
        try {
            z = packageManager.getPackageInfo(str, 0).applicationInfo.hasFragileUserData();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, "Cannot check hasFragileUserData for " + str, e);
            z = false;
        }
        if (z) {
            j = getAppDataSize(str, dialogInfo.allUsers ? null : dialogInfo.user);
        } else {
            j = 0;
        }
        if (j == 0) {
            builder.setMessage(sb.toString());
        } else {
            ViewGroup viewGroup = (ViewGroup) ((LayoutInflater) getContext().getSystemService(LayoutInflater.class)).inflate(R.layout.uninstall_content_view, (ViewGroup) null);
            ((TextView) viewGroup.requireViewById(R.id.message)).setText(sb.toString());
            this.mKeepData = (CheckBox) viewGroup.requireViewById(R.id.keepData);
            this.mKeepData.setVisibility(0);
            this.mKeepData.setText(getString(R.string.uninstall_keep_data, Formatter.formatFileSize(getContext(), j)));
            builder.setView(viewGroup);
        }
        return builder.create();
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            UninstallerActivity uninstallerActivity = (UninstallerActivity) getActivity();
            CheckBox checkBox = this.mKeepData;
            uninstallerActivity.startUninstallProgress(checkBox != null && checkBox.isChecked());
            return;
        }
        ((UninstallerActivity) getActivity()).dispatchAborted();
    }

    @Override // android.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        if (isAdded()) {
            getActivity().finish();
        }
    }

    private boolean isSingleUser(UserManager userManager) {
        int userCount = userManager.getUserCount();
        if (userCount != 1) {
            return UserManager.isSplitSystemUser() && userCount == 2;
        }
        return true;
    }
}
