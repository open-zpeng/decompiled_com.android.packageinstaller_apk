package com.android.packageinstaller;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.AtomicFile;
import android.util.SparseArray;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class EventResultPersister {
    private static final String LOG_TAG = "EventResultPersister";
    private int mCounter;
    private boolean mIsPersistScheduled;
    private boolean mIsPersistingStateValid;
    private final AtomicFile mResultsFile;
    private final Object mLock = new Object();
    private final SparseArray<EventResult> mResults = new SparseArray<>();
    private final SparseArray<EventResultObserver> mObservers = new SparseArray<>();

    /* loaded from: classes.dex */
    interface EventResultObserver {
        void onResult(int i, int i2, String str);
    }

    public int getNewId() throws OutOfIdsException {
        int i;
        synchronized (this.mLock) {
            if (this.mCounter == Integer.MAX_VALUE) {
                throw new OutOfIdsException();
            }
            this.mCounter++;
            writeState();
            i = this.mCounter - 1;
        }
        return i;
    }

    private static void nextElement(XmlPullParser xmlPullParser) throws XmlPullParserException, IOException {
        int next;
        do {
            next = xmlPullParser.next();
            if (next == 2) {
                return;
            }
        } while (next != 1);
    }

    private static int readIntAttribute(XmlPullParser xmlPullParser, String str) {
        return Integer.parseInt(xmlPullParser.getAttributeValue(null, str));
    }

    private static String readStringAttribute(XmlPullParser xmlPullParser, String str) {
        return xmlPullParser.getAttributeValue(null, str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public EventResultPersister(File file) {
        this.mResultsFile = new AtomicFile(file);
        this.mCounter = -2147483647;
        try {
            FileInputStream openRead = this.mResultsFile.openRead();
            XmlPullParser newPullParser = Xml.newPullParser();
            newPullParser.setInput(openRead, StandardCharsets.UTF_8.name());
            nextElement(newPullParser);
            while (newPullParser.getEventType() != 1) {
                String name = newPullParser.getName();
                if ("results".equals(name)) {
                    this.mCounter = readIntAttribute(newPullParser, "counter");
                } else if ("result".equals(name)) {
                    int readIntAttribute = readIntAttribute(newPullParser, "id");
                    int readIntAttribute2 = readIntAttribute(newPullParser, "status");
                    int readIntAttribute3 = readIntAttribute(newPullParser, "legacyStatus");
                    String readStringAttribute = readStringAttribute(newPullParser, "statusMessage");
                    if (this.mResults.get(readIntAttribute) != null) {
                        throw new Exception("id " + readIntAttribute + " has two results");
                    }
                    this.mResults.put(readIntAttribute, new EventResult(readIntAttribute2, readIntAttribute3, readStringAttribute));
                } else {
                    throw new Exception("unexpected tag");
                }
                nextElement(newPullParser);
            }
            if (openRead != null) {
                openRead.close();
            }
        } catch (Exception unused) {
            this.mResults.clear();
            writeState();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onEventReceived(Context context, Intent intent) {
        int i = 0;
        int intExtra = intent.getIntExtra("android.content.pm.extra.STATUS", 0);
        if (intExtra == -1) {
            context.startActivity((Intent) intent.getParcelableExtra("android.intent.extra.INTENT"));
            return;
        }
        int intExtra2 = intent.getIntExtra("EventResultPersister.EXTRA_ID", 0);
        String stringExtra = intent.getStringExtra("android.content.pm.extra.STATUS_MESSAGE");
        int intExtra3 = intent.getIntExtra("android.content.pm.extra.LEGACY_STATUS", 0);
        EventResultObserver eventResultObserver = null;
        synchronized (this.mLock) {
            int size = this.mObservers.size();
            while (true) {
                if (i >= size) {
                    break;
                } else if (this.mObservers.keyAt(i) == intExtra2) {
                    eventResultObserver = this.mObservers.valueAt(i);
                    this.mObservers.removeAt(i);
                    break;
                } else {
                    i++;
                }
            }
            if (eventResultObserver != null) {
                eventResultObserver.onResult(intExtra, intExtra3, stringExtra);
            } else {
                this.mResults.put(intExtra2, new EventResult(intExtra, intExtra3, stringExtra));
                writeState();
            }
        }
    }

    private void writeState() {
        synchronized (this.mLock) {
            this.mIsPersistingStateValid = false;
            if (!this.mIsPersistScheduled) {
                this.mIsPersistScheduled = true;
                AsyncTask.execute(new Runnable() { // from class: com.android.packageinstaller.-$$Lambda$EventResultPersister$usQNOmFbHMT14knm3XmlDVnm0EE
                    @Override // java.lang.Runnable
                    public final void run() {
                        EventResultPersister.this.lambda$writeState$0$EventResultPersister();
                    }
                });
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:42:0x00c0 A[EXC_TOP_SPLITTER, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public /* synthetic */ void lambda$writeState$0$EventResultPersister() {
        /*
            r9 = this;
        L0:
            java.lang.Object r0 = r9.mLock
            monitor-enter(r0)
            int r1 = r9.mCounter     // Catch: java.lang.Throwable -> Lce
            android.util.SparseArray<com.android.packageinstaller.EventResultPersister$EventResult> r2 = r9.mResults     // Catch: java.lang.Throwable -> Lce
            android.util.SparseArray r2 = r2.clone()     // Catch: java.lang.Throwable -> Lce
            r3 = 1
            r9.mIsPersistingStateValid = r3     // Catch: java.lang.Throwable -> Lce
            monitor-exit(r0)     // Catch: java.lang.Throwable -> Lce
            r0 = 0
            r4 = 0
            android.util.AtomicFile r5 = r9.mResultsFile     // Catch: java.io.IOException -> La8
            java.io.FileOutputStream r5 = r5.startWrite()     // Catch: java.io.IOException -> La8
            org.xmlpull.v1.XmlSerializer r6 = android.util.Xml.newSerializer()     // Catch: java.io.IOException -> La6
            java.nio.charset.Charset r7 = java.nio.charset.StandardCharsets.UTF_8     // Catch: java.io.IOException -> La6
            java.lang.String r7 = r7.name()     // Catch: java.io.IOException -> La6
            r6.setOutput(r5, r7)     // Catch: java.io.IOException -> La6
            java.lang.Boolean r7 = java.lang.Boolean.valueOf(r3)     // Catch: java.io.IOException -> La6
            r6.startDocument(r4, r7)     // Catch: java.io.IOException -> La6
            java.lang.String r7 = "http://xmlpull.org/v1/doc/features.html#indent-output"
            r6.setFeature(r7, r3)     // Catch: java.io.IOException -> La6
            java.lang.String r3 = "results"
            r6.startTag(r4, r3)     // Catch: java.io.IOException -> La6
            java.lang.String r3 = "counter"
            java.lang.String r1 = java.lang.Integer.toString(r1)     // Catch: java.io.IOException -> La6
            r6.attribute(r4, r3, r1)     // Catch: java.io.IOException -> La6
            int r1 = r2.size()     // Catch: java.io.IOException -> La6
            r3 = r0
        L43:
            if (r3 >= r1) goto L98
            java.lang.String r7 = "result"
            r6.startTag(r4, r7)     // Catch: java.io.IOException -> La6
            java.lang.String r7 = "id"
            int r8 = r2.keyAt(r3)     // Catch: java.io.IOException -> La6
            java.lang.String r8 = java.lang.Integer.toString(r8)     // Catch: java.io.IOException -> La6
            r6.attribute(r4, r7, r8)     // Catch: java.io.IOException -> La6
            java.lang.String r7 = "status"
            java.lang.Object r8 = r2.valueAt(r3)     // Catch: java.io.IOException -> La6
            com.android.packageinstaller.EventResultPersister$EventResult r8 = (com.android.packageinstaller.EventResultPersister.EventResult) r8     // Catch: java.io.IOException -> La6
            int r8 = r8.status     // Catch: java.io.IOException -> La6
            java.lang.String r8 = java.lang.Integer.toString(r8)     // Catch: java.io.IOException -> La6
            r6.attribute(r4, r7, r8)     // Catch: java.io.IOException -> La6
            java.lang.String r7 = "legacyStatus"
            java.lang.Object r8 = r2.valueAt(r3)     // Catch: java.io.IOException -> La6
            com.android.packageinstaller.EventResultPersister$EventResult r8 = (com.android.packageinstaller.EventResultPersister.EventResult) r8     // Catch: java.io.IOException -> La6
            int r8 = r8.legacyStatus     // Catch: java.io.IOException -> La6
            java.lang.String r8 = java.lang.Integer.toString(r8)     // Catch: java.io.IOException -> La6
            r6.attribute(r4, r7, r8)     // Catch: java.io.IOException -> La6
            java.lang.Object r7 = r2.valueAt(r3)     // Catch: java.io.IOException -> La6
            com.android.packageinstaller.EventResultPersister$EventResult r7 = (com.android.packageinstaller.EventResultPersister.EventResult) r7     // Catch: java.io.IOException -> La6
            java.lang.String r7 = r7.message     // Catch: java.io.IOException -> La6
            if (r7 == 0) goto L90
            java.lang.String r7 = "statusMessage"
            java.lang.Object r8 = r2.valueAt(r3)     // Catch: java.io.IOException -> La6
            com.android.packageinstaller.EventResultPersister$EventResult r8 = (com.android.packageinstaller.EventResultPersister.EventResult) r8     // Catch: java.io.IOException -> La6
            java.lang.String r8 = r8.message     // Catch: java.io.IOException -> La6
            r6.attribute(r4, r7, r8)     // Catch: java.io.IOException -> La6
        L90:
            java.lang.String r7 = "result"
            r6.endTag(r4, r7)     // Catch: java.io.IOException -> La6
            int r3 = r3 + 1
            goto L43
        L98:
            java.lang.String r1 = "results"
            r6.endTag(r4, r1)     // Catch: java.io.IOException -> La6
            r6.endDocument()     // Catch: java.io.IOException -> La6
            android.util.AtomicFile r1 = r9.mResultsFile     // Catch: java.io.IOException -> La6
            r1.finishWrite(r5)     // Catch: java.io.IOException -> La6
            goto Lbd
        La6:
            r1 = move-exception
            goto Laa
        La8:
            r1 = move-exception
            r5 = r4
        Laa:
            if (r5 == 0) goto Lb1
            android.util.AtomicFile r2 = r9.mResultsFile
            r2.failWrite(r5)
        Lb1:
            java.lang.String r2 = com.android.packageinstaller.EventResultPersister.LOG_TAG
            java.lang.String r3 = "error writing results"
            android.util.Log.e(r2, r3, r1)
            android.util.AtomicFile r1 = r9.mResultsFile
            r1.delete()
        Lbd:
            java.lang.Object r1 = r9.mLock
            monitor-enter(r1)
            boolean r2 = r9.mIsPersistingStateValid     // Catch: java.lang.Throwable -> Lcb
            if (r2 == 0) goto Lc8
            r9.mIsPersistScheduled = r0     // Catch: java.lang.Throwable -> Lcb
            monitor-exit(r1)     // Catch: java.lang.Throwable -> Lcb
            return
        Lc8:
            monitor-exit(r1)     // Catch: java.lang.Throwable -> Lcb
            goto L0
        Lcb:
            r9 = move-exception
            monitor-exit(r1)     // Catch: java.lang.Throwable -> Lcb
            throw r9
        Lce:
            r9 = move-exception
            monitor-exit(r0)     // Catch: java.lang.Throwable -> Lce
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.packageinstaller.EventResultPersister.lambda$writeState$0$EventResultPersister():void");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int addObserver(int i, EventResultObserver eventResultObserver) throws OutOfIdsException {
        synchronized (this.mLock) {
            int i2 = -1;
            if (i == Integer.MIN_VALUE) {
                i = getNewId();
            } else {
                i2 = this.mResults.indexOfKey(i);
            }
            if (i2 >= 0) {
                EventResult valueAt = this.mResults.valueAt(i2);
                eventResultObserver.onResult(valueAt.status, valueAt.legacyStatus, valueAt.message);
                this.mResults.removeAt(i2);
                writeState();
            } else {
                this.mObservers.put(i, eventResultObserver);
            }
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void removeObserver(int i) {
        synchronized (this.mLock) {
            this.mObservers.delete(i);
        }
    }

    /* loaded from: classes.dex */
    private class EventResult {
        public final int legacyStatus;
        public final String message;
        public final int status;

        private EventResult(int i, int i2, String str) {
            this.status = i;
            this.legacyStatus = i2;
            this.message = str;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class OutOfIdsException extends Exception {
        OutOfIdsException() {
        }
    }
}
