package id.co.kamil.autochat.bulksender;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.ServiceSync.ID_SERVICE_WA;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDING;

public class WASendService extends AccessibilityService {
    private static String typeApp;
    private static String idMessage;
    private final String TAG = this.getClass().getSimpleName();

    private static final String waTextFieldID = "com.whatsapp:id/entry";
    private static final String waButtonSendID = "com.whatsapp:id/send";
    private static final String waBackButtonID = "com.whatsapp:id/back";
    private static long try_again = 0;
    private DBHelper dbHelper;
    private String user_id;

    public static void setID(String id) {
        idMessage = id;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        // get the source node of the event
        try {

            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null)
                return;

            CharSequence packageName = nodeInfo.getPackageName();
            Log.i(TAG, packageName.toString());

            performWhatsAppMessage(nodeInfo);

            // recycle the nodeInfo object
            nodeInfo.recycle();
        } catch (Exception e) {
            Log.e(TAG, "NullPointerException Caught");
        }

    }

    private void performWhatsAppMessage(final AccessibilityNodeInfo rootNode) {
        Date c2 = Calendar.getInstance().getTime();

        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final String created = df2.format(c2);
        try {

            dbHelper = new DBHelper(this);
            SessionManager session = new SessionManager(this);
            HashMap<String, String> userDetail = session.getUserDetails();
            user_id = userDetail.get(KEY_CUST_ID);
            SharPref sharePref = new SharPref(this);

            FirebaseDatabase dbFirebase = FirebaseDatabase.getInstance();
            DatabaseReference fOutboxRef = dbFirebase.getReference().child("outbox").child(session.getValue(KEY_CUST_ID));
            fOutboxRef.keepSynced(true);
            boolean statusWASender = sharePref.getSessionBool(STATUS_BULK_SENDER);
            boolean statusSending = sharePref.getSessionBool(STATUS_BULK_SENDING);
            if (!statusWASender) {
                return;
            }


            final AccessibilityNodeInfo buttonSend;
            final AccessibilityNodeInfo backButton;
            buttonSend = getNodeInfo(rootNode, waButtonSendID);
            backButton = getNodeInfo(rootNode, waBackButtonID);

            if (buttonSend == null) {
                if (backButton == null) {
                    dbHelper.insertLog(created, ID_SERVICE_WA, "ID Button Send : " + buttonSend + " dan ID Button Back : " + backButton, "warning", user_id);
                    Log.e(TAG, "button Send : " + buttonSend);
                } else if (statusSending) {
                    backButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    dbHelper.insertLog(created, ID_SERVICE_WA, "ID Button Send : " + buttonSend, "warning", user_id);
                }
                return;
            }
            if (idMessage == null) {
                dbHelper.insertLog(created, ID_SERVICE_WA, "Tidak ada pesan yang akan dikirim", "warning", user_id);
                return;
            }


            buttonSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            dbHelper = new DBHelper(this);
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String tglSent = df.format(c);

            fOutboxRef.child(idMessage).child("sent").setValue(tglSent);
            //dbHelper.updateSent(idMessage, "1", tglSent, "0");
            dbHelper.insertLog(tglSent, ID_SERVICE_WA, "Berhasil dikirim", "success", user_id);

            setID(null);
            try_again = 0;

            backButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);

        } catch (Exception e) {
            e.printStackTrace();
            String stackTrace = Log.getStackTraceString(e);
            dbHelper.insertLog(created, ID_SERVICE_WA, stackTrace, "danger", user_id);

        }
    }

    private AccessibilityNodeInfo getNodeInfo(AccessibilityNodeInfo rootNode, String viewId) {
        Date c2 = Calendar.getInstance().getTime();

        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final String created = df2.format(c2);
        AccessibilityNodeInfo node = null;
        try {
            List<AccessibilityNodeInfo> nodes = rootNode
                    .findAccessibilityNodeInfosByViewId(viewId);

            if (nodes.size() > 0)
                node = nodes.get(0);
        } catch (Exception e) {
            String stackTrace = Log.getStackTraceString(e);
            dbHelper.insertLog(created, ID_SERVICE_WA, stackTrace, "danger", user_id);
        }


        return node;
    }

    @Override
    public void onInterrupt() {

    }
}
