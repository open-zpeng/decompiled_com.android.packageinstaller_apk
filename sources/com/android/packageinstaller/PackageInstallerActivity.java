package com.android.packageinstaller;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.os.UserManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.recyclerview.R$styleable;
import com.android.internal.app.AlertActivity;
import com.android.packageinstaller.PackageInstallerActivity;
import com.android.packageinstaller.PackageUtil;
import java.io.File;
import java.util.Set;
/* loaded from: classes.dex */
public class PackageInstallerActivity extends AlertActivity {
    private static final String ALLOW_UNKNOWN_SOURCES_KEY = PackageInstallerActivity.class.getName() + "ALLOW_UNKNOWN_SOURCES_KEY";
    private boolean mAllowUnknownSources;
    AppOpsManager mAppOpsManager;
    private PackageUtil.AppSnippet mAppSnippet;
    String mCallingPackage;
    PackageInstaller mInstaller;
    IPackageManager mIpm;
    private Button mOk;
    private String mOriginatingPackage;
    private Uri mOriginatingURI;
    private Uri mPackageURI;
    PackageInfo mPkgInfo;
    PackageManager mPm;
    private Uri mReferrerURI;
    ApplicationInfo mSourceInfo;
    UserManager mUserManager;
    private int mSessionId = -1;
    private int mOriginatingUid = -1;
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo = null;
    private boolean mEnableOk = false;

    private void startInstallConfirm() {
        View requireViewById;
        ApplicationInfo applicationInfo = this.mAppInfo;
        if (applicationInfo != null) {
            if ((applicationInfo.flags & 1) != 0) {
                requireViewById = requireViewById((int) R.id.install_confirm_question_update_system);
            } else {
                requireViewById = requireViewById((int) R.id.install_confirm_question_update);
            }
        } else {
            requireViewById = requireViewById((int) R.id.install_confirm_question);
        }
        requireViewById.setVisibility(0);
        this.mEnableOk = true;
        this.mOk.setEnabled(true);
    }

    private void showDialogInner(int i) {
        DialogFragment dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
        if (dialogFragment != null) {
            dialogFragment.dismissAllowingStateLoss();
        }
        DialogFragment createDialog = createDialog(i);
        if (createDialog != null) {
            createDialog.showAllowingStateLoss(getFragmentManager(), "dialog");
        }
    }

    private DialogFragment createDialog(int i) {
        switch (i) {
            case 2:
                return SimpleErrorDialog.newInstance(R.string.Parse_error_dlg_text);
            case 3:
                return OutOfSpaceDialog.newInstance(this.mPm.getApplicationLabel(this.mPkgInfo.applicationInfo));
            case 4:
                return InstallErrorDialog.newInstance(this.mPm.getApplicationLabel(this.mPkgInfo.applicationInfo));
            case 5:
                return SimpleErrorDialog.newInstance(R.string.unknown_apps_user_restriction_dlg_text);
            case 6:
                return AnonymousSourceDialog.newInstance();
            case 7:
                return NotSupportedOnWearDialog.newInstance();
            case 8:
                return ExternalSourcesBlockedDialog.newInstance(this.mOriginatingPackage);
            case R$styleable.RecyclerView_spanCount /* 9 */:
                return SimpleErrorDialog.newInstance(R.string.install_apps_user_restriction_dlg_text);
            default:
                return null;
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 == -1) {
            this.mAllowUnknownSources = true;
            this.mAppOpsManager.noteOpNoThrow(AppOpsManager.permissionToOpCode("android.permission.REQUEST_INSTALL_PACKAGES"), this.mOriginatingUid, this.mOriginatingPackage);
            DialogFragment dialogFragment = (DialogFragment) getFragmentManager().findFragmentByTag("dialog");
            if (dialogFragment != null) {
                dialogFragment.dismissAllowingStateLoss();
            }
            initiateInstall();
            return;
        }
        finish();
    }

    private String getPackageNameForUid(int i) {
        String[] packagesForUid = this.mPm.getPackagesForUid(i);
        if (packagesForUid == null) {
            return null;
        }
        if (packagesForUid.length > 1) {
            if (this.mCallingPackage != null) {
                for (String str : packagesForUid) {
                    if (str.equals(this.mCallingPackage)) {
                        return str;
                    }
                }
            }
            Log.i("PackageInstaller", "Multiple packages found for source uid " + i);
        }
        return packagesForUid[0];
    }

    private boolean isInstallRequestFromUnknownSource(Intent intent) {
        ApplicationInfo applicationInfo;
        return this.mCallingPackage == null || !intent.getBooleanExtra("android.intent.extra.NOT_UNKNOWN_SOURCE", false) || (applicationInfo = this.mSourceInfo) == null || (applicationInfo.privateFlags & 8) == 0;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initiateInstall() {
        String str = this.mPkgInfo.packageName;
        String[] canonicalToCurrentPackageNames = this.mPm.canonicalToCurrentPackageNames(new String[]{str});
        if (canonicalToCurrentPackageNames != null && canonicalToCurrentPackageNames.length > 0 && canonicalToCurrentPackageNames[0] != null) {
            str = canonicalToCurrentPackageNames[0];
            PackageInfo packageInfo = this.mPkgInfo;
            packageInfo.packageName = str;
            packageInfo.applicationInfo.packageName = str;
        }
        try {
            this.mAppInfo = this.mPm.getApplicationInfo(str, 8192);
            if ((this.mAppInfo.flags & 8388608) == 0) {
                this.mAppInfo = null;
            }
        } catch (PackageManager.NameNotFoundException unused) {
            this.mAppInfo = null;
        }
        startInstallConfirm();
    }

    void setPmResult(int i) {
        Intent intent = new Intent();
        intent.putExtra("android.intent.extra.INSTALL_RESULT", i);
        setResult(i == 1 ? -1 : 1, intent);
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void onCreate(Bundle bundle) {
        Uri uri;
        String str;
        getWindow().addSystemFlags(524288);
        super.onCreate((Bundle) null);
        if (bundle != null) {
            this.mAllowUnknownSources = bundle.getBoolean(ALLOW_UNKNOWN_SOURCES_KEY);
        }
        this.mPm = getPackageManager();
        this.mIpm = AppGlobals.getPackageManager();
        this.mAppOpsManager = (AppOpsManager) getSystemService("appops");
        this.mInstaller = this.mPm.getPackageInstaller();
        this.mUserManager = (UserManager) getSystemService("user");
        Intent intent = getIntent();
        this.mCallingPackage = intent.getStringExtra("EXTRA_CALLING_PACKAGE");
        this.mSourceInfo = (ApplicationInfo) intent.getParcelableExtra("EXTRA_ORIGINAL_SOURCE_INFO");
        this.mOriginatingUid = intent.getIntExtra("android.intent.extra.ORIGINATING_UID", -1);
        int i = this.mOriginatingUid;
        this.mOriginatingPackage = i != -1 ? getPackageNameForUid(i) : null;
        if ("android.content.pm.action.CONFIRM_INSTALL".equals(intent.getAction())) {
            int intExtra = intent.getIntExtra("android.content.pm.extra.SESSION_ID", -1);
            PackageInstaller.SessionInfo sessionInfo = this.mInstaller.getSessionInfo(intExtra);
            if (sessionInfo == null || !sessionInfo.sealed || (str = sessionInfo.resolvedBaseCodePath) == null) {
                Log.w("PackageInstaller", "Session " + this.mSessionId + " in funky state; ignoring");
                finish();
                return;
            }
            this.mSessionId = intExtra;
            uri = Uri.fromFile(new File(str));
            this.mOriginatingURI = null;
            this.mReferrerURI = null;
        } else {
            this.mSessionId = -1;
            Uri data = intent.getData();
            this.mOriginatingURI = (Uri) intent.getParcelableExtra("android.intent.extra.ORIGINATING_URI");
            this.mReferrerURI = (Uri) intent.getParcelableExtra("android.intent.extra.REFERRER");
            uri = data;
        }
        if (uri == null) {
            Log.w("PackageInstaller", "Unspecified source");
            setPmResult(-3);
            finish();
        } else if (DeviceUtils.isWear(this)) {
            showDialogInner(7);
        } else if (processPackageUri(uri)) {
            bindUi();
            checkIfAllowedAndInitiateInstall();
        }
    }

    protected void onResume() {
        super.onResume();
        Button button = this.mOk;
        if (button != null) {
            button.setEnabled(this.mEnableOk);
        }
    }

    protected void onPause() {
        super.onPause();
        Button button = this.mOk;
        if (button != null) {
            button.setEnabled(false);
        }
    }

    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(ALLOW_UNKNOWN_SOURCES_KEY, this.mAllowUnknownSources);
    }

    private void bindUi() {
        ((AlertActivity) this).mAlert.setIcon(this.mAppSnippet.icon);
        ((AlertActivity) this).mAlert.setTitle(this.mAppSnippet.label);
        ((AlertActivity) this).mAlert.setView((int) R.layout.install_content_view);
        ((AlertActivity) this).mAlert.setButton(-1, getString((int) R.string.install), new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$Vln2nluA_QJFjJV_na_l6jbopHM
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                PackageInstallerActivity.this.lambda$bindUi$0$PackageInstallerActivity(dialogInterface, i);
            }
        }, (Message) null);
        ((AlertActivity) this).mAlert.setButton(-2, getString((int) R.string.cancel), new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$5ZUeBcoeWNB5sojcJYIj3vi7S2w
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                PackageInstallerActivity.this.lambda$bindUi$1$PackageInstallerActivity(dialogInterface, i);
            }
        }, (Message) null);
        setupAlert();
        this.mOk = ((AlertActivity) this).mAlert.getButton(-1);
        this.mOk.setEnabled(false);
    }

    public /* synthetic */ void lambda$bindUi$0$PackageInstallerActivity(DialogInterface dialogInterface, int i) {
        if (this.mOk.isEnabled()) {
            int i2 = this.mSessionId;
            if (i2 != -1) {
                this.mInstaller.setPermissionsResult(i2, true);
                finish();
                return;
            }
            startInstall();
        }
    }

    public /* synthetic */ void lambda$bindUi$1$PackageInstallerActivity(DialogInterface dialogInterface, int i) {
        setResult(0);
        int i2 = this.mSessionId;
        if (i2 != -1) {
            this.mInstaller.setPermissionsResult(i2, false);
        }
        finish();
    }

    private void checkIfAllowedAndInitiateInstall() {
        int userRestrictionSource = this.mUserManager.getUserRestrictionSource("no_install_apps", Process.myUserHandle());
        if ((userRestrictionSource & 1) != 0) {
            showDialogInner(9);
        } else if (userRestrictionSource != 0) {
            startActivity(new Intent("android.settings.SHOW_ADMIN_SUPPORT_DETAILS"));
            finish();
        } else if (this.mAllowUnknownSources || !isInstallRequestFromUnknownSource(getIntent())) {
            initiateInstall();
        } else {
            int userRestrictionSource2 = this.mUserManager.getUserRestrictionSource("no_install_unknown_sources", Process.myUserHandle());
            int userRestrictionSource3 = this.mUserManager.getUserRestrictionSource("no_install_unknown_sources_globally", Process.myUserHandle());
            if (((userRestrictionSource2 | userRestrictionSource3) & 1) != 0) {
                showDialogInner(5);
            } else if (userRestrictionSource2 != 0) {
                startAdminSupportDetailsActivity("no_install_unknown_sources");
            } else if (userRestrictionSource3 != 0) {
                startAdminSupportDetailsActivity("no_install_unknown_sources_globally");
            } else {
                handleUnknownSources();
            }
        }
    }

    private void startAdminSupportDetailsActivity(String str) {
        Intent createAdminSupportIntent = ((DevicePolicyManager) getSystemService(DevicePolicyManager.class)).createAdminSupportIntent(str);
        if (createAdminSupportIntent != null) {
            startActivity(createAdminSupportIntent);
        }
        finish();
    }

    private void handleUnknownSources() {
        if (this.mOriginatingPackage == null) {
            Log.i("PackageInstaller", "No source found for package " + this.mPkgInfo.packageName);
            showDialogInner(6);
            return;
        }
        int permissionToOpCode = AppOpsManager.permissionToOpCode("android.permission.REQUEST_INSTALL_PACKAGES");
        int noteOpNoThrow = this.mAppOpsManager.noteOpNoThrow(permissionToOpCode, this.mOriginatingUid, this.mOriginatingPackage);
        if (noteOpNoThrow != 0) {
            if (noteOpNoThrow != 2) {
                if (noteOpNoThrow == 3) {
                    this.mAppOpsManager.setMode(permissionToOpCode, this.mOriginatingUid, this.mOriginatingPackage, 2);
                } else {
                    Log.e("PackageInstaller", "Invalid app op mode " + noteOpNoThrow + " for OP_REQUEST_INSTALL_PACKAGES found for uid " + this.mOriginatingUid);
                    finish();
                    return;
                }
            }
            showDialogInner(8);
            return;
        }
        initiateInstall();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private boolean processPackageUri(Uri uri) {
        char c;
        this.mPackageURI = uri;
        String scheme = uri.getScheme();
        int hashCode = scheme.hashCode();
        if (hashCode != -807062458) {
            if (hashCode == 3143036 && scheme.equals("file")) {
                c = 1;
            }
            c = 65535;
        } else {
            if (scheme.equals("package")) {
                c = 0;
            }
            c = 65535;
        }
        if (c == 0) {
            try {
                this.mPkgInfo = this.mPm.getPackageInfo(uri.getSchemeSpecificPart(), 12288);
            } catch (PackageManager.NameNotFoundException unused) {
            }
            PackageInfo packageInfo = this.mPkgInfo;
            if (packageInfo == null) {
                Log.w("PackageInstaller", "Requested package " + uri.getScheme() + " not available. Discontinuing installation");
                showDialogInner(2);
                setPmResult(-2);
                return false;
            }
            this.mAppSnippet = new PackageUtil.AppSnippet(this.mPm.getApplicationLabel(packageInfo.applicationInfo), this.mPm.getApplicationIcon(this.mPkgInfo.applicationInfo));
        } else if (c == 1) {
            File file = new File(uri.getPath());
            PackageParser.Package packageInfo2 = PackageUtil.getPackageInfo(this, file);
            if (packageInfo2 == null) {
                Log.w("PackageInstaller", "Parse error when parsing manifest. Discontinuing installation");
                showDialogInner(2);
                setPmResult(-2);
                return false;
            }
            this.mPkgInfo = PackageParser.generatePackageInfo(packageInfo2, (int[]) null, 4096, 0L, 0L, (Set) null, new PackageUserState());
            this.mAppSnippet = PackageUtil.getAppSnippet(this, this.mPkgInfo.applicationInfo, file);
        } else {
            throw new IllegalArgumentException("Unexpected URI scheme " + uri);
        }
        return true;
    }

    public void onBackPressed() {
        int i = this.mSessionId;
        if (i != -1) {
            this.mInstaller.setPermissionsResult(i, false);
        }
        super.onBackPressed();
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void startInstall() {
        Intent intent = new Intent();
        intent.putExtra("com.android.packageinstaller.applicationInfo", this.mPkgInfo.applicationInfo);
        intent.setData(this.mPackageURI);
        intent.setClass(this, InstallInstalling.class);
        String stringExtra = getIntent().getStringExtra("android.intent.extra.INSTALLER_PACKAGE_NAME");
        Uri uri = this.mOriginatingURI;
        if (uri != null) {
            intent.putExtra("android.intent.extra.ORIGINATING_URI", uri);
        }
        Uri uri2 = this.mReferrerURI;
        if (uri2 != null) {
            intent.putExtra("android.intent.extra.REFERRER", uri2);
        }
        int i = this.mOriginatingUid;
        if (i != -1) {
            intent.putExtra("android.intent.extra.ORIGINATING_UID", i);
        }
        if (stringExtra != null) {
            intent.putExtra("android.intent.extra.INSTALLER_PACKAGE_NAME", stringExtra);
        }
        if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
            intent.putExtra("android.intent.extra.RETURN_RESULT", true);
        }
        intent.addFlags(33554432);
        if (this.localLOGV) {
            Log.i("PackageInstaller", "downloaded app uri=" + this.mPackageURI);
        }
        startActivity(intent);
        finish();
    }

    /* loaded from: classes.dex */
    public static class SimpleErrorDialog extends DialogFragment {
        private static final String MESSAGE_KEY = SimpleErrorDialog.class.getName() + "MESSAGE_KEY";

        static SimpleErrorDialog newInstance(int i) {
            SimpleErrorDialog simpleErrorDialog = new SimpleErrorDialog();
            Bundle bundle = new Bundle();
            bundle.putInt(MESSAGE_KEY, i);
            simpleErrorDialog.setArguments(bundle);
            return simpleErrorDialog;
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            return new AlertDialog.Builder(getActivity()).setMessage(getArguments().getInt(MESSAGE_KEY)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$SimpleErrorDialog$CjGDh-fPYU-Pu9mKlkOS2BcORqU
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PackageInstallerActivity.SimpleErrorDialog.this.lambda$onCreateDialog$0$PackageInstallerActivity$SimpleErrorDialog(dialogInterface, i);
                }
            }).create();
        }

        public /* synthetic */ void lambda$onCreateDialog$0$PackageInstallerActivity$SimpleErrorDialog(DialogInterface dialogInterface, int i) {
            getActivity().finish();
        }
    }

    /* loaded from: classes.dex */
    public static class AnonymousSourceDialog extends DialogFragment {
        static AnonymousSourceDialog newInstance() {
            return new AnonymousSourceDialog();
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            return new AlertDialog.Builder(getActivity()).setMessage(R.string.anonymous_source_warning).setPositiveButton(R.string.anonymous_source_continue, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$AnonymousSourceDialog$j7ZkjYfuLCxI-yvPw_BH4gKwnyQ
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PackageInstallerActivity.AnonymousSourceDialog.this.lambda$onCreateDialog$0$PackageInstallerActivity$AnonymousSourceDialog(dialogInterface, i);
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$AnonymousSourceDialog$8kZWiPYiC_m6DQoz2Y8AkkrIOm0
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PackageInstallerActivity.AnonymousSourceDialog.this.lambda$onCreateDialog$1$PackageInstallerActivity$AnonymousSourceDialog(dialogInterface, i);
                }
            }).create();
        }

        public /* synthetic */ void lambda$onCreateDialog$0$PackageInstallerActivity$AnonymousSourceDialog(DialogInterface dialogInterface, int i) {
            PackageInstallerActivity packageInstallerActivity = (PackageInstallerActivity) getActivity();
            packageInstallerActivity.mAllowUnknownSources = true;
            packageInstallerActivity.initiateInstall();
        }

        public /* synthetic */ void lambda$onCreateDialog$1$PackageInstallerActivity$AnonymousSourceDialog(DialogInterface dialogInterface, int i) {
            getActivity().finish();
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            getActivity().finish();
        }
    }

    /* loaded from: classes.dex */
    public static class NotSupportedOnWearDialog extends SimpleErrorDialog {
        static SimpleErrorDialog newInstance() {
            return SimpleErrorDialog.newInstance(R.string.wear_not_allowed_dlg_text);
        }
    }

    /* loaded from: classes.dex */
    public static class OutOfSpaceDialog extends AppErrorDialog {
        static AppErrorDialog newInstance(CharSequence charSequence) {
            OutOfSpaceDialog outOfSpaceDialog = new OutOfSpaceDialog();
            outOfSpaceDialog.setArgument(charSequence);
            return outOfSpaceDialog;
        }

        @Override // com.android.packageinstaller.PackageInstallerActivity.AppErrorDialog
        protected Dialog createDialog(CharSequence charSequence) {
            return new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.out_of_space_dlg_text, charSequence)).setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$OutOfSpaceDialog$1uGsoLrZEkf3PtFRxGm1HRlY75M
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PackageInstallerActivity.OutOfSpaceDialog.this.lambda$createDialog$0$PackageInstallerActivity$OutOfSpaceDialog(dialogInterface, i);
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$OutOfSpaceDialog$y1myQ4PNdkwtdc9tjmUKChqA78g
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PackageInstallerActivity.OutOfSpaceDialog.this.lambda$createDialog$1$PackageInstallerActivity$OutOfSpaceDialog(dialogInterface, i);
                }
            }).create();
        }

        public /* synthetic */ void lambda$createDialog$0$PackageInstallerActivity$OutOfSpaceDialog(DialogInterface dialogInterface, int i) {
            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
            intent.setFlags(268435456);
            startActivity(intent);
            getActivity().finish();
        }

        public /* synthetic */ void lambda$createDialog$1$PackageInstallerActivity$OutOfSpaceDialog(DialogInterface dialogInterface, int i) {
            getActivity().finish();
        }
    }

    /* loaded from: classes.dex */
    public static class InstallErrorDialog extends AppErrorDialog {
        static AppErrorDialog newInstance(CharSequence charSequence) {
            InstallErrorDialog installErrorDialog = new InstallErrorDialog();
            installErrorDialog.setArgument(charSequence);
            return installErrorDialog;
        }

        @Override // com.android.packageinstaller.PackageInstallerActivity.AppErrorDialog
        protected Dialog createDialog(CharSequence charSequence) {
            return new AlertDialog.Builder(getActivity()).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$InstallErrorDialog$o_75ob8nQvqldslkDnXYGzX9T6E
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    PackageInstallerActivity.InstallErrorDialog.this.lambda$createDialog$0$PackageInstallerActivity$InstallErrorDialog(dialogInterface, i);
                }
            }).setMessage(getString(R.string.install_failed_msg, charSequence)).create();
        }

        public /* synthetic */ void lambda$createDialog$0$PackageInstallerActivity$InstallErrorDialog(DialogInterface dialogInterface, int i) {
            getActivity().finish();
        }
    }

    /* loaded from: classes.dex */
    public static class ExternalSourcesBlockedDialog extends AppErrorDialog {
        static AppErrorDialog newInstance(String str) {
            ExternalSourcesBlockedDialog externalSourcesBlockedDialog = new ExternalSourcesBlockedDialog();
            externalSourcesBlockedDialog.setArgument(str);
            return externalSourcesBlockedDialog;
        }

        @Override // com.android.packageinstaller.PackageInstallerActivity.AppErrorDialog
        protected Dialog createDialog(final CharSequence charSequence) {
            try {
                PackageManager packageManager = getActivity().getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(charSequence.toString(), 0);
                return new AlertDialog.Builder(getActivity()).setTitle(packageManager.getApplicationLabel(applicationInfo)).setIcon(packageManager.getApplicationIcon(applicationInfo)).setMessage(R.string.untrusted_external_source_warning).setPositiveButton(R.string.external_sources_settings, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$ExternalSourcesBlockedDialog$A5PlzBSROq1mgW2jWjKzk3yMh4U
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        PackageInstallerActivity.ExternalSourcesBlockedDialog.this.lambda$createDialog$0$PackageInstallerActivity$ExternalSourcesBlockedDialog(charSequence, dialogInterface, i);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$PackageInstallerActivity$ExternalSourcesBlockedDialog$L2lERdinc3mjPuJONSQQSBxnEFQ
                    @Override // android.content.DialogInterface.OnClickListener
                    public final void onClick(DialogInterface dialogInterface, int i) {
                        PackageInstallerActivity.ExternalSourcesBlockedDialog.this.lambda$createDialog$1$PackageInstallerActivity$ExternalSourcesBlockedDialog(dialogInterface, i);
                    }
                }).create();
            } catch (PackageManager.NameNotFoundException unused) {
                Log.e("PackageInstaller", "Did not find app info for " + ((Object) charSequence));
                this.getActivity().finish();
                return null;
            }
        }

        public /* synthetic */ void lambda$createDialog$0$PackageInstallerActivity$ExternalSourcesBlockedDialog(CharSequence charSequence, DialogInterface dialogInterface, int i) {
            Intent intent = new Intent();
            intent.setAction("android.settings.MANAGE_UNKNOWN_APP_SOURCES");
            intent.setData(Uri.parse("package:" + ((Object) charSequence)));
            try {
                getActivity().startActivityForResult(intent, 1);
            } catch (ActivityNotFoundException unused) {
                Log.e("PackageInstaller", "Settings activity not found for action: android.settings.MANAGE_UNKNOWN_APP_SOURCES");
            }
        }

        public /* synthetic */ void lambda$createDialog$1$PackageInstallerActivity$ExternalSourcesBlockedDialog(DialogInterface dialogInterface, int i) {
            getActivity().finish();
        }
    }

    /* loaded from: classes.dex */
    public static abstract class AppErrorDialog extends DialogFragment {
        private static final String ARGUMENT_KEY = AppErrorDialog.class.getName() + "ARGUMENT_KEY";

        protected abstract Dialog createDialog(CharSequence charSequence);

        protected void setArgument(CharSequence charSequence) {
            Bundle bundle = new Bundle();
            bundle.putCharSequence(ARGUMENT_KEY, charSequence);
            setArguments(bundle);
        }

        @Override // android.app.DialogFragment
        public Dialog onCreateDialog(Bundle bundle) {
            return createDialog(getArguments().getString(ARGUMENT_KEY));
        }

        @Override // android.app.DialogFragment, android.content.DialogInterface.OnCancelListener
        public void onCancel(DialogInterface dialogInterface) {
            getActivity().finish();
        }
    }
}
