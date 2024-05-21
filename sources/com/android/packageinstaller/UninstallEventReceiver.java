package com.android.packageinstaller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.packageinstaller.EventResultPersister;
/* loaded from: classes.dex */
public class UninstallEventReceiver extends BroadcastReceiver {
    private static final Object sLock = new Object();
    private static EventResultPersister sReceiver;

    private static EventResultPersister getReceiver(Context context) {
        synchronized (sLock) {
            if (sReceiver == null) {
                sReceiver = new EventResultPersister(TemporaryFileManager.getUninstallStateFile(context));
            }
        }
        return sReceiver;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        getReceiver(context).onEventReceived(context, intent);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int addObserver(Context context, int i, EventResultPersister.EventResultObserver eventResultObserver) throws EventResultPersister.OutOfIdsException {
        return getReceiver(context).addObserver(i, eventResultObserver);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void removeObserver(Context context, int i) {
        getReceiver(context).removeObserver(i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int getNewId(Context context) throws EventResultPersister.OutOfIdsException {
        return getReceiver(context).getNewId();
    }
}
