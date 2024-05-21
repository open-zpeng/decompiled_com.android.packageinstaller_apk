package com.android.packageinstaller.handheld;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.android.packageinstaller.UninstallerActivity;
/* loaded from: classes.dex */
public class ErrorDialogFragment extends DialogFragment {
    @Override // android.app.DialogFragment
    public Dialog onCreateDialog(Bundle bundle) {
        AlertDialog.Builder positiveButton = new AlertDialog.Builder(getActivity()).setMessage(getArguments().getInt("com.android.packageinstaller.arg.text")).setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        if (getArguments().containsKey("com.android.packageinstaller.arg.title")) {
            positiveButton.setTitle(getArguments().getInt("com.android.packageinstaller.arg.title"));
        }
        return positiveButton.create();
    }

    @Override // android.app.DialogFragment, android.content.DialogInterface.OnDismissListener
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        if (isAdded()) {
            if (getActivity() instanceof UninstallerActivity) {
                ((UninstallerActivity) getActivity()).dispatchAborted();
            }
            getActivity().setResult(1);
            getActivity().finish();
        }
    }
}
