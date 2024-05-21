package com.android.packageinstaller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import com.android.internal.app.AlertActivity;
import com.android.packageinstaller.InstallFailed;
import com.android.packageinstaller.PackageUtil;
import java.io.File;
/* loaded from: classes.dex */
public class InstallFailed extends AlertActivity {
    private static final String LOG_TAG = "InstallFailed";
    private CharSequence mLabel;

    private void setExplanationFromErrorCode(int i) {
        View requireViewById;
        String str = LOG_TAG;
        Log.d(str, "Installation status code: " + i);
        if (i == 2) {
            requireViewById = requireViewById((int) R.id.install_failed_blocked);
        } else if (i == 7) {
            requireViewById = requireViewById((int) R.id.install_failed_incompatible);
        } else if (i == 4) {
            requireViewById = requireViewById((int) R.id.install_failed_invalid_apk);
        } else if (i == 5) {
            requireViewById = requireViewById((int) R.id.install_failed_conflict);
        } else {
            requireViewById = requireViewById((int) R.id.install_failed);
        }
        requireViewById.setVisibility(0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void onCreate(Bundle bundle) {
        PackageUtil.AppSnippet appSnippet;
        super.onCreate(bundle);
        int intExtra = getIntent().getIntExtra("android.content.pm.extra.STATUS", 1);
        if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
            int intExtra2 = getIntent().getIntExtra("android.content.pm.extra.LEGACY_STATUS", -110);
            Intent intent = new Intent();
            intent.putExtra("android.intent.extra.INSTALL_RESULT", intExtra2);
            setResult(1, intent);
            finish();
            return;
        }
        Intent intent2 = getIntent();
        ApplicationInfo applicationInfo = (ApplicationInfo) intent2.getParcelableExtra("com.android.packageinstaller.applicationInfo");
        Uri data = intent2.getData();
        PackageManager packageManager = getPackageManager();
        if ("package".equals(data.getScheme())) {
            appSnippet = new PackageUtil.AppSnippet(packageManager.getApplicationLabel(applicationInfo), packageManager.getApplicationIcon(applicationInfo));
        } else {
            appSnippet = PackageUtil.getAppSnippet(this, applicationInfo, new File(data.getPath()));
        }
        this.mLabel = appSnippet.label;
        ((AlertActivity) this).mAlert.setIcon(appSnippet.icon);
        ((AlertActivity) this).mAlert.setTitle(appSnippet.label);
        ((AlertActivity) this).mAlert.setView((int) R.layout.install_content_view);
        ((AlertActivity) this).mAlert.setButton(-1, getString((int) R.string.done), new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallFailed$z0k0kenEFeFyyJkzlE55AnL0raU
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                InstallFailed.this.lambda$onCreate$0$InstallFailed(dialogInterface, i);
            }
        }, (Message) null);
        setupAlert();
        if (intExtra == 6) {
            new OutOfSpaceDialog().show(getFragmentManager(), "outofspace");
        }
        setExplanationFromErrorCode(intExtra);
    }

    public /* synthetic */ void lambda$onCreate$0$InstallFailed(DialogInterface dialogInterface, int i) {
        finish();
    }

    /* loaded from: classes.dex */
    public static class OutOfSpaceDialog extends DialogFragment {
        private InstallFailed mActivity;

        @Override // android.app.DialogFragment, android.app.Fragment
        public void onAttach(Context context) {
            super.onAttach(context);
            this.mActivity = (InstallFailed) context;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            return new AlertDialog.Builder(this.mActivity).setTitle(R.string.out_of_space_dlg_title).setMessage(getString(R.string.out_of_space_dlg_text, this.mActivity.mLabel)).setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallFailed$OutOfSpaceDialog$hsig_VFpVwRJT9IHP-ssU4vs4R0
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    InstallFailed.OutOfSpaceDialog.this.lambda$onCreateDialog$0$InstallFailed$OutOfSpaceDialog(dialogInterface, i);
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallFailed$OutOfSpaceDialog$lKM9kdrRWfY6VG1ieZS-pYyhkf0
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    InstallFailed.OutOfSpaceDialog.this.lambda$onCreateDialog$1$InstallFailed$OutOfSpaceDialog(dialogInterface, i);
                }
            }).create();
        }

        public /* synthetic */ void lambda$onCreateDialog$0$InstallFailed$OutOfSpaceDialog(DialogInterface dialogInterface, int i) {
            startActivity(new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE"));
            this.mActivity.finish();
        }

        public /* synthetic */ void lambda$onCreateDialog$1$InstallFailed$OutOfSpaceDialog(DialogInterface dialogInterface, int i) {
            this.mActivity.finish();
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            super.onCancel(dialogInterface);
            this.mActivity.finish();
        }
    }
}
