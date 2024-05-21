package com.android.packageinstaller.wear;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import com.android.packageinstaller.wear.PackageInstallerImpl;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
/* loaded from: classes.dex */
public class InstallTask {
    private PackageInstallerImpl.InstallListener mCallback;
    private IntentSender mCommitCallback;
    private final Context mContext;
    private String mPackageName;
    private ParcelFileDescriptor mParcelFileDescriptor;
    private PackageInstaller.Session mSession;
    private Exception mException = null;
    private int mErrorCode = 0;
    private String mErrorDesc = null;

    public InstallTask(Context context, String str, ParcelFileDescriptor parcelFileDescriptor, PackageInstallerImpl.InstallListener installListener, PackageInstaller.Session session, IntentSender intentSender) {
        this.mContext = context;
        this.mPackageName = str;
        this.mParcelFileDescriptor = parcelFileDescriptor;
        this.mCallback = installListener;
        this.mSession = session;
        this.mCommitCallback = intentSender;
    }

    public boolean isError() {
        return (this.mErrorCode == 0 && TextUtils.isEmpty(this.mErrorDesc)) ? false : true;
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x002c, code lost:
        if (r9.mException != null) goto L7;
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x002e, code lost:
        r9.mException = r1;
        r9.mErrorCode = -621;
        r9.mErrorDesc = "Could not close session stream";
     */
    /* JADX WARN: Code restructure failed: missing block: B:22:0x004c, code lost:
        if (r9.mException != null) goto L7;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void execute() {
        /*
            r9 = this;
            java.lang.String r0 = "Could not close session stream"
            android.os.Looper r1 = android.os.Looper.myLooper()
            android.os.Looper r2 = android.os.Looper.getMainLooper()
            if (r1 == r2) goto Ld5
            r1 = 0
            r2 = -621(0xfffffffffffffd93, float:NaN)
            android.content.pm.PackageInstaller$Session r3 = r9.mSession     // Catch: java.lang.Throwable -> L35 java.lang.Exception -> L38
            java.lang.String r4 = r9.mPackageName     // Catch: java.lang.Throwable -> L35 java.lang.Exception -> L38
            r5 = 0
            r7 = -1
            java.io.OutputStream r1 = r3.openWrite(r4, r5, r7)     // Catch: java.lang.Throwable -> L35 java.lang.Exception -> L38
            r9.writeToOutputStreamFromAsset(r1)     // Catch: java.lang.Throwable -> L35 java.lang.Exception -> L38
            android.content.pm.PackageInstaller$Session r3 = r9.mSession     // Catch: java.lang.Throwable -> L35 java.lang.Exception -> L38
            r3.fsync(r1)     // Catch: java.lang.Throwable -> L35 java.lang.Exception -> L38
            if (r1 == 0) goto L4f
            r1.close()     // Catch: java.lang.Exception -> L29
            goto L4f
        L29:
            r1 = move-exception
            java.lang.Exception r3 = r9.mException
            if (r3 != 0) goto L4f
        L2e:
            r9.mException = r1
            r9.mErrorCode = r2
            r9.mErrorDesc = r0
            goto L4f
        L35:
            r3 = move-exception
            goto Lc3
        L38:
            r3 = move-exception
            r9.mException = r3     // Catch: java.lang.Throwable -> L35
            r3 = -620(0xfffffffffffffd94, float:NaN)
            r9.mErrorCode = r3     // Catch: java.lang.Throwable -> L35
            java.lang.String r3 = "Could not write to stream"
            r9.mErrorDesc = r3     // Catch: java.lang.Throwable -> L35
            if (r1 == 0) goto L4f
            r1.close()     // Catch: java.lang.Exception -> L49
            goto L4f
        L49:
            r1 = move-exception
            java.lang.Exception r3 = r9.mException
            if (r3 != 0) goto L4f
            goto L2e
        L4f:
            int r0 = r9.mErrorCode
            if (r0 == 0) goto Lb1
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "Exception while installing "
            r0.append(r1)
            java.lang.String r1 = r9.mPackageName
            r0.append(r1)
            java.lang.String r1 = ": "
            r0.append(r1)
            int r1 = r9.mErrorCode
            r0.append(r1)
            java.lang.String r1 = ", "
            r0.append(r1)
            java.lang.String r2 = r9.mErrorDesc
            r0.append(r2)
            r0.append(r1)
            java.lang.Exception r1 = r9.mException
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "InstallTask"
            android.util.Log.e(r1, r0)
            android.content.pm.PackageInstaller$Session r0 = r9.mSession
            r0.close()
            com.android.packageinstaller.wear.PackageInstallerImpl$InstallListener r0 = r9.mCallback
            int r1 = r9.mErrorCode
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "["
            r2.append(r3)
            java.lang.String r3 = r9.mPackageName
            r2.append(r3)
            java.lang.String r3 = "]"
            r2.append(r3)
            java.lang.String r9 = r9.mErrorDesc
            r2.append(r9)
            java.lang.String r9 = r2.toString()
            r0.installFailed(r1, r9)
            goto Lc2
        Lb1:
            com.android.packageinstaller.wear.PackageInstallerImpl$InstallListener r0 = r9.mCallback
            r0.installBeginning()
            android.content.pm.PackageInstaller$Session r0 = r9.mSession
            android.content.IntentSender r1 = r9.mCommitCallback
            r0.commit(r1)
            android.content.pm.PackageInstaller$Session r9 = r9.mSession
            r9.close()
        Lc2:
            return
        Lc3:
            if (r1 == 0) goto Ld4
            r1.close()     // Catch: java.lang.Exception -> Lc9
            goto Ld4
        Lc9:
            r1 = move-exception
            java.lang.Exception r4 = r9.mException
            if (r4 != 0) goto Ld4
            r9.mException = r1
            r9.mErrorCode = r2
            r9.mErrorDesc = r0
        Ld4:
            throw r3
        Ld5:
            java.lang.IllegalStateException r9 = new java.lang.IllegalStateException
            java.lang.String r0 = "This method cannot be called from the UI thread."
            r9.<init>(r0)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.packageinstaller.wear.InstallTask.execute():void");
    }

    private boolean writeToOutputStreamFromAsset(OutputStream outputStream) {
        if (outputStream == null) {
            this.mErrorCode = -615;
            this.mErrorDesc = "Got a null OutputStream.";
            return false;
        }
        ParcelFileDescriptor parcelFileDescriptor = this.mParcelFileDescriptor;
        if (parcelFileDescriptor == null || parcelFileDescriptor.getFileDescriptor() == null) {
            this.mErrorCode = -603;
            this.mErrorDesc = "Could not get FD";
            return false;
        }
        ParcelFileDescriptor.AutoCloseInputStream autoCloseInputStream = null;
        try {
            try {
                byte[] bArr = new byte[8192];
                ParcelFileDescriptor.AutoCloseInputStream autoCloseInputStream2 = new ParcelFileDescriptor.AutoCloseInputStream(this.mParcelFileDescriptor);
                while (true) {
                    try {
                        int read = autoCloseInputStream2.read(bArr);
                        if (read <= -1) {
                            outputStream.flush();
                            safeClose(autoCloseInputStream2);
                            return true;
                        } else if (read > 0) {
                            outputStream.write(bArr, 0, read);
                        }
                    } catch (IOException e) {
                        e = e;
                        autoCloseInputStream = autoCloseInputStream2;
                        this.mErrorCode = -619;
                        this.mErrorDesc = "Reading from Asset FD or writing to temp file failed: " + e;
                        safeClose(autoCloseInputStream);
                        return false;
                    } catch (Throwable th) {
                        th = th;
                        autoCloseInputStream = autoCloseInputStream2;
                        safeClose(autoCloseInputStream);
                        throw th;
                    }
                }
            } catch (IOException e2) {
                e = e2;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }
}
