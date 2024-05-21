package com.android.packageinstaller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.packageinstaller.InstallStaging;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
/* loaded from: classes.dex */
public class InstallStaging extends AlertActivity {
    private static final String LOG_TAG = "InstallStaging";
    private File mStagedFile;
    private StagingAsyncTask mStagingTask;

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ((AlertActivity) this).mAlert.setIcon((int) R.drawable.ic_file_download);
        ((AlertActivity) this).mAlert.setTitle(getString((int) R.string.app_name_unknown));
        ((AlertActivity) this).mAlert.setView((int) R.layout.install_content_view);
        ((AlertActivity) this).mAlert.setButton(-2, getString((int) R.string.cancel), new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallStaging$-n_J6648NTcOllT9pIbsLwncgvU
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                InstallStaging.this.lambda$onCreate$0$InstallStaging(dialogInterface, i);
            }
        }, (Message) null);
        setupAlert();
        requireViewById((int) R.id.staging).setVisibility(0);
        if (bundle != null) {
            this.mStagedFile = new File(bundle.getString("STAGED_FILE"));
            if (this.mStagedFile.exists()) {
                return;
            }
            this.mStagedFile = null;
        }
    }

    public /* synthetic */ void lambda$onCreate$0$InstallStaging(DialogInterface dialogInterface, int i) {
        StagingAsyncTask stagingAsyncTask = this.mStagingTask;
        if (stagingAsyncTask != null) {
            stagingAsyncTask.cancel(true);
        }
        setResult(0);
        finish();
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void onResume() {
        super.onResume();
        if (this.mStagingTask == null) {
            if (this.mStagedFile == null) {
                try {
                    this.mStagedFile = TemporaryFileManager.getStagedFile(this);
                } catch (IOException unused) {
                    showError();
                    return;
                }
            }
            this.mStagingTask = new StagingAsyncTask();
            this.mStagingTask.execute(getIntent().getData());
        }
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString("STAGED_FILE", this.mStagedFile.getPath());
    }

    protected void onDestroy() {
        StagingAsyncTask stagingAsyncTask = this.mStagingTask;
        if (stagingAsyncTask != null) {
            stagingAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showError() {
        new ErrorDialog().showAllowingStateLoss(getFragmentManager(), "error");
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.INSTALL_RESULT", -2);
        setResult(1, intent);
    }

    /* loaded from: classes.dex */
    public static class ErrorDialog extends DialogFragment {
        private Activity mActivity;

        @Override // android.app.DialogFragment, android.app.Fragment
        public void onAttach(Context context) {
            super.onAttach(context);
            this.mActivity = (Activity) context;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            AlertDialog create = new AlertDialog.Builder(this.mActivity).setMessage(R.string.Parse_error_dlg_text).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallStaging$ErrorDialog$z6Uttpne_gXZCqllYbnRjQ_S0Kk
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    InstallStaging.ErrorDialog.this.lambda$onCreateDialog$0$InstallStaging$ErrorDialog(dialogInterface, i);
                }
            }).create();
            create.setCanceledOnTouchOutside(false);
            return create;
        }

        public /* synthetic */ void lambda$onCreateDialog$0$InstallStaging$ErrorDialog(DialogInterface dialogInterface, int i) {
            this.mActivity.finish();
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            super.onCancel(dialogInterface);
            this.mActivity.finish();
        }
    }

    /* loaded from: classes.dex */
    private final class StagingAsyncTask extends AsyncTask<Uri, Void, Boolean> {
        private StagingAsyncTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public Boolean doInBackground(Uri... uriArr) {
            if (uriArr != null && uriArr.length > 0) {
                try {
                    InputStream openInputStream = InstallStaging.this.getContentResolver().openInputStream(uriArr[0]);
                    if (openInputStream == null) {
                        if (openInputStream != null) {
                            $closeResource(null, openInputStream);
                        }
                        return false;
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(InstallStaging.this.mStagedFile);
                    try {
                        byte[] bArr = new byte[1048576];
                        while (true) {
                            int read = openInputStream.read(bArr);
                            if (read < 0) {
                                $closeResource(null, fileOutputStream);
                                if (openInputStream != null) {
                                    $closeResource(null, openInputStream);
                                }
                                return true;
                            } else if (isCancelled()) {
                                $closeResource(null, fileOutputStream);
                                if (openInputStream != null) {
                                    $closeResource(null, openInputStream);
                                }
                                return false;
                            } else {
                                fileOutputStream.write(bArr, 0, read);
                            }
                        }
                    } finally {
                    }
                } catch (IOException | IllegalStateException | SecurityException e) {
                    Log.w(InstallStaging.LOG_TAG, "Error staging apk from content URI", e);
                }
            }
            return false;
        }

        private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
            if (th == null) {
                autoCloseable.close();
                return;
            }
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public void onPostExecute(Boolean bool) {
            if (!bool.booleanValue()) {
                InstallStaging.this.showError();
                return;
            }
            Intent intent = new Intent(InstallStaging.this.getIntent());
            intent.setClass(InstallStaging.this, DeleteStagedFileOnResult.class);
            intent.setData(Uri.fromFile(InstallStaging.this.mStagedFile));
            if (intent.getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
                intent.addFlags(33554432);
            }
            intent.addFlags(65536);
            InstallStaging.this.startActivity(intent);
            InstallStaging.this.finish();
        }
    }
}
