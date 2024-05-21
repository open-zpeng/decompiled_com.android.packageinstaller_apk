package com.android.packageinstaller.television;

import android.os.Bundle;
import androidx.leanback.app.GuidedStepFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;
import com.android.packageinstaller.UninstallerActivity;
import java.util.List;
/* loaded from: classes.dex */
public class ErrorFragment extends GuidedStepFragment {
    @Override // androidx.leanback.app.GuidedStepFragment
    public int onProvideTheme() {
        return 2131558443;
    }

    @Override // androidx.leanback.app.GuidedStepFragment
    public GuidanceStylist.Guidance onCreateGuidance(Bundle bundle) {
        return new GuidanceStylist.Guidance(getString(getArguments().getInt("com.android.packageinstaller.arg.title")), getString(getArguments().getInt("com.android.packageinstaller.arg.text")), null, null);
    }

    @Override // androidx.leanback.app.GuidedStepFragment
    public void onCreateActions(List<GuidedAction> list, Bundle bundle) {
        GuidedAction.Builder builder = new GuidedAction.Builder(getContext());
        builder.clickAction(-4L);
        list.add(builder.build());
    }

    @Override // androidx.leanback.app.GuidedStepFragment
    public void onGuidedActionClicked(GuidedAction guidedAction) {
        if (isAdded()) {
            if (getActivity() instanceof UninstallerActivity) {
                ((UninstallerActivity) getActivity()).dispatchAborted();
            }
            getActivity().setResult(1);
            getActivity().finish();
        }
    }
}
