package com.android.packageinstaller;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.android.internal.app.AlertActivity;
import com.android.packageinstaller.PackageUtil;
import java.io.File;
import java.util.List;
/* loaded from: classes.dex */
public class InstallSuccess extends AlertActivity {
    private static final String LOG_TAG = "InstallSuccess";

    /* JADX WARN: Multi-variable type inference failed */
    protected void onCreate(Bundle bundle) {
        PackageUtil.AppSnippet appSnippet;
        List<ResolveInfo> queryIntentActivities;
        super.onCreate(bundle);
        boolean z = true;
        if (getIntent().getBooleanExtra("android.intent.extra.RETURN_RESULT", false)) {
            Intent intent = new Intent();
            intent.putExtra("android.intent.extra.INSTALL_RESULT", 1);
            setResult(-1, intent);
            finish();
            return;
        }
        Intent intent2 = getIntent();
        final ApplicationInfo applicationInfo = (ApplicationInfo) intent2.getParcelableExtra("com.android.packageinstaller.applicationInfo");
        Uri data = intent2.getData();
        PackageManager packageManager = getPackageManager();
        if ("package".equals(data.getScheme())) {
            appSnippet = new PackageUtil.AppSnippet(packageManager.getApplicationLabel(applicationInfo), packageManager.getApplicationIcon(applicationInfo));
        } else {
            appSnippet = PackageUtil.getAppSnippet(this, applicationInfo, new File(data.getPath()));
        }
        ((AlertActivity) this).mAlert.setIcon(appSnippet.icon);
        ((AlertActivity) this).mAlert.setTitle(appSnippet.label);
        ((AlertActivity) this).mAlert.setView((int) R.layout.install_content_view);
        ((AlertActivity) this).mAlert.setButton(-1, getString((int) R.string.launch), (DialogInterface.OnClickListener) null, (Message) null);
        ((AlertActivity) this).mAlert.setButton(-2, getString((int) R.string.done), new DialogInterface.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallSuccess$wg_zwfwMas82DCBZSGSiaINwWxk
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                InstallSuccess.this.lambda$onCreate$0$InstallSuccess(applicationInfo, dialogInterface, i);
            }
        }, (Message) null);
        setupAlert();
        requireViewById((int) R.id.install_success).setVisibility(0);
        final Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(applicationInfo.packageName);
        if (launchIntentForPackage == null || (queryIntentActivities = getPackageManager().queryIntentActivities(launchIntentForPackage, 0)) == null || queryIntentActivities.size() <= 0) {
            z = false;
        }
        Button button = ((AlertActivity) this).mAlert.getButton(-1);
        if (z) {
            button.setOnClickListener(new View.OnClickListener() { // from class: com.android.packageinstaller.-$$Lambda$InstallSuccess$2vWvr9bvgN8Tfp2L3TxaAphjvy8
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    InstallSuccess.this.lambda$onCreate$1$InstallSuccess(launchIntentForPackage, view);
                }
            });
        } else {
            button.setEnabled(false);
        }
    }

    public /* synthetic */ void lambda$onCreate$0$InstallSuccess(ApplicationInfo applicationInfo, DialogInterface dialogInterface, int i) {
        if (applicationInfo.packageName != null) {
            String str = LOG_TAG;
            Log.i(str, "Finished installing " + applicationInfo.packageName);
        }
        finish();
    }

    public /* synthetic */ void lambda$onCreate$1$InstallSuccess(Intent intent, View view) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.e(LOG_TAG, "Could not start activity", e);
        }
        finish();
    }
}
