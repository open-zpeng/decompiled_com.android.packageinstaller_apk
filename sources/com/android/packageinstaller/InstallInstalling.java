package com.android.packageinstaller;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import com.android.internal.app.AlertActivity;
import com.android.internal.content.PackageHelper;
import com.android.packageinstaller.EventResultPersister;
import com.android.packageinstaller.PackageUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
/* loaded from: classes.dex */
public class InstallInstalling extends AlertActivity {
    private static final String LOG_TAG = "InstallInstalling";
    private Button mCancelButton;
    private int mInstallId;
    private InstallingAsyncTask mInstallingTask;
    private Uri mPackageURI;
    private PackageInstaller.SessionCallback mSessionCallback;
    private int mSessionId;

    /* JADX WARN: Multi-variable type inference failed */
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ApplicationInfo applicationInfo = (ApplicationInfo) getIntent().getParcelableExtra("com.android.packageinstaller.applicationInfo");
        this.mPackageURI = getIntent().getData();
        if ("package".equals(this.mPackageURI.getScheme())) {
            try {
                getPackageManager().installExistingPackage(applicationInfo.packageName);
                launchSuccess();
                return;
            } catch (PackageManager.NameNotFoundException unused) {
                launchFailure(-110, null);
                return;
            }
        }
        PackageUtil.AppSnippet appSnippet = PackageUtil.getAppSnippet(this, applicationInfo, new File(this.mPackageURI.getPath()));
        ((AlertActivity) this).mAlert.setIcon(appSnippet.icon);
        ((AlertActivity) this).mAlert.setTitle(appSnippet.label);
        ((AlertActivity) this).mAlert.setView((int) R.layout.install_content_view);
        ((AlertActivity) this).mAlert.setButton(-2, getString((int) R.string.cancel), new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallInstalling$znOyJ0CDR9gFSSyLvwV7HaV9tJY
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                InstallInstalling.this.lambda$onCreate$0$InstallInstalling(dialogInterface, i);
            }
        }, (Message) null);
        setupAlert();
        requireViewById((int) R.id.installing).setVisibility(0);
        if (bundle != null) {
            this.mSessionId = bundle.getInt("com.android.packageinstaller.SESSION_ID");
            this.mInstallId = bundle.getInt("com.android.packageinstaller.INSTALL_ID");
            try {
                InstallEventReceiver.addObserver(this, this.mInstallId, new EventResultPersister.EventResultObserver() { // from class: com.android.packageinstaller.-$$Lambda$InstallInstalling$Fm5TSD2hD23Z_s5R1z4FKuv9cT0
                    @Override // com.android.packageinstaller.EventResultPersister.EventResultObserver
                    public final void onResult(int i, int i2, String str) {
                        InstallInstalling.this.launchFinishBasedOnResult(i, i2, str);
                    }
                });
            } catch (EventResultPersister.OutOfIdsException unused2) {
            }
        } else {
            PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(1);
            sessionParams.setInstallAsInstantApp(false);
            sessionParams.setReferrerUri((Uri) getIntent().getParcelableExtra("android.intent.extra.REFERRER"));
            sessionParams.setOriginatingUri((Uri) getIntent().getParcelableExtra("android.intent.extra.ORIGINATING_URI"));
            sessionParams.setOriginatingUid(getIntent().getIntExtra("android.intent.extra.ORIGINATING_UID", -1));
            sessionParams.setInstallerPackageName(getIntent().getStringExtra("android.intent.extra.INSTALLER_PACKAGE_NAME"));
            sessionParams.setInstallReason(4);
            File file = new File(this.mPackageURI.getPath());
            try {
                PackageParser.PackageLite parsePackageLite = PackageParser.parsePackageLite(file, 0);
                sessionParams.setAppPackageName(parsePackageLite.packageName);
                sessionParams.setInstallLocation(parsePackageLite.installLocation);
                sessionParams.setSize(PackageHelper.calculateInstalledSize(parsePackageLite, false, sessionParams.abiOverride));
            } catch (IOException unused3) {
                String str = LOG_TAG;
                Log.e(str, "Cannot calculate installed size " + file + ". Try only apk size.");
                sessionParams.setSize(file.length());
            } catch (PackageParser.PackageParserException unused4) {
                String str2 = LOG_TAG;
                Log.e(str2, "Cannot parse package " + file + ". Assuming defaults.");
                String str3 = LOG_TAG;
                Log.e(str3, "Cannot calculate installed size " + file + ". Try only apk size.");
                sessionParams.setSize(file.length());
            }
            try {
                this.mInstallId = InstallEventReceiver.addObserver(this, Integer.MIN_VALUE, new EventResultPersister.EventResultObserver() { // from class: com.android.packageinstaller.-$$Lambda$InstallInstalling$Fm5TSD2hD23Z_s5R1z4FKuv9cT0
                    @Override // com.android.packageinstaller.EventResultPersister.EventResultObserver
                    public final void onResult(int i, int i2, String str4) {
                        InstallInstalling.this.launchFinishBasedOnResult(i, i2, str4);
                    }
                });
            } catch (EventResultPersister.OutOfIdsException unused5) {
                launchFailure(-110, null);
            }
            try {
                this.mSessionId = getPackageManager().getPackageInstaller().createSession(sessionParams);
            } catch (IOException unused6) {
                launchFailure(-110, null);
            }
        }
        this.mCancelButton = ((AlertActivity) this).mAlert.getButton(-2);
        this.mSessionCallback = new InstallSessionCallback();
    }

    public /* synthetic */ void lambda$onCreate$0$InstallInstalling(DialogInterface dialogInterface, int i) {
        InstallingAsyncTask installingAsyncTask = this.mInstallingTask;
        if (installingAsyncTask != null) {
            installingAsyncTask.cancel(true);
        }
        if (this.mSessionId > 0) {
            getPackageManager().getPackageInstaller().abandonSession(this.mSessionId);
            this.mSessionId = 0;
        }
        setResult(0);
        finish();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void launchSuccess() {
        Intent intent = new Intent(getIntent());
        intent.setClass(this, InstallSuccess.class);
        intent.addFlags(33554432);
        startActivity(intent);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public void launchFailure(int i, String str) {
        Intent intent = new Intent(getIntent());
        intent.setClass(this, InstallFailed.class);
        intent.addFlags(33554432);
        intent.putExtra("android.content.pm.extra.LEGACY_STATUS", i);
        intent.putExtra("android.content.pm.extra.STATUS_MESSAGE", str);
        startActivity(intent);
        finish();
    }

    protected void onStart() {
        super.onStart();
        getPackageManager().getPackageInstaller().registerSessionCallback(this.mSessionCallback);
    }

    protected void onResume() {
        super.onResume();
        if (this.mInstallingTask == null) {
            PackageInstaller.SessionInfo sessionInfo = getPackageManager().getPackageInstaller().getSessionInfo(this.mSessionId);
            if (sessionInfo != null && !sessionInfo.isActive()) {
                this.mInstallingTask = new InstallingAsyncTask();
                this.mInstallingTask.execute(new Void[0]);
                return;
            }
            this.mCancelButton.setEnabled(false);
            setFinishOnTouchOutside(false);
        }
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("com.android.packageinstaller.SESSION_ID", this.mSessionId);
        bundle.putInt("com.android.packageinstaller.INSTALL_ID", this.mInstallId);
    }

    public void onBackPressed() {
        if (this.mCancelButton.isEnabled()) {
            super.onBackPressed();
        }
    }

    protected void onStop() {
        super.onStop();
        getPackageManager().getPackageInstaller().unregisterSessionCallback(this.mSessionCallback);
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void onDestroy() {
        InstallingAsyncTask installingAsyncTask = this.mInstallingTask;
        if (installingAsyncTask != null) {
            installingAsyncTask.cancel(true);
            synchronized (this.mInstallingTask) {
                while (!this.mInstallingTask.isDone) {
                    try {
                        this.mInstallingTask.wait();
                    } catch (InterruptedException e) {
                        Log.i(LOG_TAG, "Interrupted while waiting for installing task to cancel", e);
                    }
                }
            }
        }
        InstallEventReceiver.removeObserver(this, this.mInstallId);
        super.onDestroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void launchFinishBasedOnResult(int i, int i2, String str) {
        if (i == 0) {
            launchSuccess();
        } else {
            launchFailure(i2, str);
        }
    }

    /* loaded from: classes.dex */
    private class InstallSessionCallback extends PackageInstaller.SessionCallback {
        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onActiveChanged(int i, boolean z) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onBadgingChanged(int i) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onCreated(int i) {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onFinished(int i, boolean z) {
        }

        private InstallSessionCallback() {
        }

        @Override // android.content.pm.PackageInstaller.SessionCallback
        public void onProgressChanged(int i, float f) {
            if (i == InstallInstalling.this.mSessionId) {
                ProgressBar progressBar = (ProgressBar) InstallInstalling.this.requireViewById((int) R.id.progress);
                progressBar.setMax(Integer.MAX_VALUE);
                progressBar.setProgress((int) (f * 2.14748365E9f));
            }
        }
    }

    /* loaded from: classes.dex */
    private final class InstallingAsyncTask extends AsyncTask<Void, Void, PackageInstaller.Session> {
        volatile boolean isDone;

        private InstallingAsyncTask() {
        }

        /* JADX INFO: Access modifiers changed from: protected */
        @Override // android.os.AsyncTask
        public PackageInstaller.Session doInBackground(Void... voidArr) {
            try {
                PackageInstaller.Session openSession = InstallInstalling.this.getPackageManager().getPackageInstaller().openSession(InstallInstalling.this.mSessionId);
                openSession.setStagingProgress(0.0f);
                try {
                    try {
                        File file = new File(InstallInstalling.this.mPackageURI.getPath());
                        FileInputStream fileInputStream = new FileInputStream(file);
                        try {
                            long length = file.length();
                            OutputStream openWrite = openSession.openWrite("PackageInstaller", 0L, length);
                            byte[] bArr = new byte[1048576];
                            while (true) {
                                int read = fileInputStream.read(bArr);
                                if (read == -1) {
                                    openSession.fsync(openWrite);
                                    break;
                                } else if (isCancelled()) {
                                    openSession.close();
                                    break;
                                } else {
                                    openWrite.write(bArr, 0, read);
                                    if (length > 0) {
                                        openSession.addProgress(read / ((float) length));
                                    }
                                }
                            }
                            if (openWrite != null) {
                                $closeResource(null, openWrite);
                            }
                            $closeResource(null, fileInputStream);
                            synchronized (this) {
                                this.isDone = true;
                                notifyAll();
                            }
                            return openSession;
                        } finally {
                        }
                    } catch (IOException | SecurityException e) {
                        Log.e(InstallInstalling.LOG_TAG, "Could not write package", e);
                        openSession.close();
                        synchronized (this) {
                            this.isDone = true;
                            notifyAll();
                            return null;
                        }
                    }
                } catch (Throwable th) {
                    synchronized (this) {
                        this.isDone = true;
                        notifyAll();
                        throw th;
                    }
                }
            } catch (IOException unused) {
                return null;
            }
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
        /* JADX WARN: Type inference failed for: r1v6, types: [android.content.Context, com.android.packageinstaller.InstallInstalling] */
        @Override // android.os.AsyncTask
        public void onPostExecute(PackageInstaller.Session session) {
            if (session == null) {
                InstallInstalling.this.getPackageManager().getPackageInstaller().abandonSession(InstallInstalling.this.mSessionId);
                if (isCancelled()) {
                    return;
                }
                InstallInstalling.this.launchFailure(-2, null);
                return;
            }
            Intent intent = new Intent("com.android.packageinstaller.ACTION_INSTALL_COMMIT");
            intent.setFlags(268435456);
            intent.setPackage(InstallInstalling.this.getPackageName());
            intent.putExtra("EventResultPersister.EXTRA_ID", InstallInstalling.this.mInstallId);
            ?? r1 = InstallInstalling.this;
            session.commit(PendingIntent.getBroadcast(r1, ((InstallInstalling) r1).mInstallId, intent, 134217728).getIntentSender());
            InstallInstalling.this.mCancelButton.setEnabled(false);
            InstallInstalling.this.setFinishOnTouchOutside(false);
        }
    }
}
