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

    private final String waTextFieldID = "com.whatsapp:id/entry";
    private final String waButtonSendID = "com.whatsapp:id/send";
    private final String waBackButtonID = "com.whatsapp:id/back";
    private static long try_again = 0;
    private SharPref sharePref;
    private DBHelper dbHelper;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String user_id;
    private FirebaseDatabase dbFirebase;
    private DatabaseReference fOutboxRef;

    public static void setID(String id) {
        idMessage = id;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        // get the source node of the event
        try {

            AccessibilityNodeInfo nodeInfo = event.getSource();
            if (nodeInfo == null || nodeInfo.equals(null))
                return;

            String packageName = nodeInfo.getPackageName().toString();
            if (packageName.equals(null) || packageName == null) {
                return;
            }
            Log.i(TAG, packageName);

            performWhatsAppMessage(nodeInfo);

            // recycle the nodeInfo object
            nodeInfo.recycle();
        } catch (Exception e) {
            Log.i(TAG, "NullPointerException Caught");
        }

    }

    private void performWhatsAppMessage(final AccessibilityNodeInfo rootNode) {
        Date c2 = Calendar.getInstance().getTime();

        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate2 = df2.format(c2);
        final String created = formattedDate2;
        try {

            dbHelper = new DBHelper(this);
            session = new SessionManager(this);
            userDetail = session.getUserDetails();
            user_id = userDetail.get(KEY_CUST_ID);
            sharePref = new SharPref(this);

            dbFirebase = FirebaseDatabase.getInstance();
            fOutboxRef = dbFirebase.getReference().child("outbox").child(session.getValue(KEY_CUST_ID));
            fOutboxRef.keepSynced(true);
            boolean statusWASender = sharePref.getSessionBool(STATUS_BULK_SENDER);
            boolean statusSending = sharePref.getSessionBool(STATUS_BULK_SENDING);
            if (statusWASender == false) {
                return;
            }


            final AccessibilityNodeInfo buttonSend;
            final AccessibilityNodeInfo backButton;
            buttonSend = getNodeInfo(rootNode, waButtonSendID);
            backButton = getNodeInfo(rootNode, waBackButtonID);

            if (buttonSend == null || buttonSend.equals(null) || buttonSend.equals("null")) {
                if (backButton == null || backButton.equals(null) || backButton.equals("null")) {
                    dbHelper.insertLog(created, ID_SERVICE_WA, "ID Button Send : " + buttonSend + " dan ID Button Back : " + backButton, "warning", user_id);
                    Log.e(TAG, "button Send : " + buttonSend);
                    return;
                } else if (statusSending) {
                    backButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return;
                } else {
                    dbHelper.insertLog(created, ID_SERVICE_WA, "ID Button Send : " + buttonSend, "warning", user_id);
                }
            }
            if (idMessage == null) {
                dbHelper.insertLog(created, ID_SERVICE_WA, "Tidak ada pesan yang akan dikirim", "warning", user_id);
                return;
            }


            buttonSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            dbHelper = new DBHelper(this);
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formattedDate = df.format(c);
            String tglSent = formattedDate;

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

        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate2 = df2.format(c2);
        final String created = formattedDate2;
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
