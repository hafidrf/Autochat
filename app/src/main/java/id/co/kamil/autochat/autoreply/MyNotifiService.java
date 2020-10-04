package id.co.kamil.autochat.autoreply;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.co.kamil.autochat.autoreply.models.Action;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.autoreply.NotificationUtils.getQuickReplyAction;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CHILD;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.AUTOREPLY_BUSINESS;
import static id.co.kamil.autochat.utils.SharPref.AUTOREPLY_PERSONAL;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

@SuppressLint("OverrideAbstract")
public class MyNotifiService extends NotificationListenerService {
    private static final String ID_SERVICE = "AutoReply";
    private BufferedWriter bw;
    public static final String TAG = "MyNotifiService";

    //    private SimpleDateFormat sdf;
//    private MyHandler handler = new MyHandler();
//    private String nMessage;
//    private String data;
    private DBHelper dbHelper;
    private boolean statusAutoReply = true;

    //    Handler mHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            String msgString = (String) msg.obj;
//            //Toast.makeText(getApplicationContext(), msgString, Toast.LENGTH_LONG).show();
//        }
//    };
    private boolean is_child;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private SharPref sharePref;
    private String user_id;
    public static String other_action = "null";

    private FirebaseDatabase dbFirebase;
    private DatabaseReference fReceivedRef;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("KEVIN", "Service is started" + "-----");
//        data = intent.getStringExtra("data");
        try {
            sharePref = new SharPref(this);
            dbHelper = new DBHelper(this);
            session = new SessionManager(this);
            userDetail = session.getUserDetails();
            is_child = Boolean.parseBoolean(userDetail.get(KEY_CHILD));
        } catch (NullPointerException e) {
            Log.i(TAG, e.getMessage());
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.i(TAG, "onNotificationRemoved");

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        return DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String created = df.format(c);
        try {
            if (sbn.getPackageName().equals("com.whatsapp") || sbn.getPackageName().equals("com.whatsapp.w4b")) {

                try {
                    sharePref = new SharPref(this);
                    dbHelper = new DBHelper(this);
                    session = new SessionManager(this);
                    userDetail = session.getUserDetails();
                    is_child = Boolean.parseBoolean(userDetail.get(KEY_CHILD));
                    user_id = userDetail.get(KEY_CUST_ID);

                    dbFirebase = FirebaseDatabase.getInstance();
                    fReceivedRef = dbFirebase.getReference().child("notification").child(user_id);
                    fReceivedRef.keepSynced(true);

                } catch (NullPointerException e) {
                    Log.i(TAG, e.getMessage());
                }
                Log.i(TAG, "Here");
                if (!statusAutoReply) return;
                try {

                    //this.cancelNotification(sbn.getKey());

                    Action action = getQuickReplyAction(sbn.getNotification(), sbn.getPackageName());

                    if (action != null) {
                        Log.i(TAG, "success");

                        boolean autoreply_personal = sharePref.getSessionBool(AUTOREPLY_PERSONAL);
                        boolean autoreply_business = sharePref.getSessionBool(AUTOREPLY_BUSINESS);

                        Object o = sbn.getNotification().extras.get("android.text");
                        String msg_client = o != null ? o.toString().toLowerCase().trim() : "";
                        //String cari = dbHelper.cari_keyword(msg_client);

                        String sender_ori = sbn.getNotification().extras.getString("android.title");

                        String[] tmp_sender = sender_ori != null ? sender_ori.split(":") : new String[]{};
                        if (tmp_sender.length > 1) {
                            sender_ori = tmp_sender[1].trim();
                        } else {
                            sender_ori = sender_ori != null ? sender_ori.trim() : "";
                        }
                        String sender = sender_ori;
                        sender = sender.replace("+62", "62")
                                .replace("-", "").replace(" ", "");
                        List<String[]> personalisasi = dbHelper.cari_phone(sender);
                        List<String> contactExist = searchContact(this, sender_ori);

                        final long when = sbn.getNotification().when;

                        if (personalisasi.size() > 0) {
//                            if(!fReceivedRef.getKey().equals(when) && !user_id.isEmpty()){
//                                fReceivedRef.child(String.valueOf(when)).child("sender").setValue(sender);
//                                fReceivedRef.child(String.valueOf(when)).child("message").setValue(msg_client);
//                                fReceivedRef.child(String.valueOf(when)).child("key").setValue(when);
//                                fReceivedRef.child(String.valueOf(when)).child("created").setValue(created);
//                            }
                            dbHelper.insertReceived(sender, msg_client, user_id, String.valueOf(when));
                        } else if (contactExist.size() > 0) {
                            for (int x = 0; x < contactExist.size(); x++) {
                                if (contactExist.get(x) != null) {
                                    String number = contactExist.get(x).replace("+62", "62")
                                            .replace("-", "").replace(" ", "");
                                    if (isNumeric(number)) {
//                                        if(!fReceivedRef.getKey().equals(when) && !user_id.isEmpty()) {
//                                            fReceivedRef.child(String.valueOf(when)).child("sender").setValue(number);
//                                            fReceivedRef.child(String.valueOf(when)).child("message").setValue(msg_client);
//                                            fReceivedRef.child(String.valueOf(when)).child("key").setValue(when);
//                                            fReceivedRef.child(String.valueOf(when)).child("created").setValue(created);
//                                        }
                                        dbHelper.insertReceived(number, msg_client, user_id, String.valueOf(when));
                                        break;
                                    }
                                }
                            }
                        } else if (isNumeric(sender)) {
//                            if(!fReceivedRef.getKey().equals(when) && !user_id.isEmpty()) {
//                                fReceivedRef.child(String.valueOf(when)).child("sender").setValue(sender);
//                                fReceivedRef.child(String.valueOf(when)).child("message").setValue(msg_client);
//                                fReceivedRef.child(String.valueOf(when)).child("key").setValue(when);
//                                fReceivedRef.child(String.valueOf(when)).child("created").setValue(created);
//                            }
                            dbHelper.insertReceived(sender, msg_client, user_id, String.valueOf(when));
                        }
                        Log.e(TAG, "when(): " + when);
                        Log.e(TAG, "tickerText:" + sbn.getNotification().extras.get("android.text"));
                        send_notif();
                        if (!autoreply_personal && sbn.getPackageName().equals("com.whatsapp")) {
                            dbHelper.insertLog(created, ID_SERVICE, "AutoReply Personal Disabled", "warning", user_id);
                        }
                        if (!autoreply_business && sbn.getPackageName().equals("com.whatsapp.w4b")) {
                            dbHelper.insertLog(created, ID_SERVICE, "AutoReply Business Disabled", "warning", user_id);
                        }
                        if (autoreply_personal && sbn.getPackageName().equals("com.whatsapp")) {
                            reply(sbn, action);
                        }
                        if (autoreply_business && sbn.getPackageName().equals("com.whatsapp.w4b")) {
                            reply(sbn, action);
                        }
                    } else {
                        dbHelper.insertLog(created, ID_SERVICE, "Tombol Quick Reply (" + sbn.getPackageName() + ") tidak tersedia. Sender : " + sbn.getNotification().extras.getString("android.title") + ". Message : " + sbn.getNotification().extras.get("android.text").toString().toLowerCase().trim(), "warning", user_id);
                        dbHelper.insertLog(created, ID_SERVICE, "Other Action : " + other_action, "warning", user_id);
                        Log.i(TAG, "not success");
                    }

                } catch (NullPointerException e) {
                    Log.i(TAG, e.getMessage());
                    String stackTrace = Log.getStackTraceString(e);
                    dbHelper.insertLog(created, ID_SERVICE, "Error : " + stackTrace, "warning", user_id);
                } catch (Exception e) {
                    String stackTrace = Log.getStackTraceString(e);
                    dbHelper.insertLog(created, ID_SERVICE, "Error : " + stackTrace, "warning", user_id);
                }
            }

        } catch (Exception e) {
            String stackTrace = Log.getStackTraceString(e);
            dbHelper.insertLog(created, ID_SERVICE, "Error : " + stackTrace, "warning", user_id);
            Toast.makeText(this, "NotifService: " + stackTrace, Toast.LENGTH_SHORT).show();
        }


    }

    public static boolean isNumeric(String maybeNumeric) {
        return maybeNumeric != null && maybeNumeric.matches("[0-9]+");
    }

    private void reply(StatusBarNotification sbn, Action action) {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String created = df.format(c);

        Object o = sbn.getNotification().extras.get("android.text");
        String msg_client = o != null ? o.toString().toLowerCase().trim() : "";
        String cari = dbHelper.cari_keyword(msg_client);

        String sender_ori = sbn.getNotification().extras.getString("android.title");
        if (sender_ori == null) sender_ori = "";
        String[] tmp_sender = sender_ori.split(":");
        if (tmp_sender.length > 1) {
            sender_ori = tmp_sender[1].trim();
        } else {
            sender_ori = sender_ori.trim();
        }
        String sender = sender_ori;
        sender = sender.replace("+62", "62")
                .replace("-", "").replace(" ", "");
        List<String[]> personalisasi = dbHelper.cari_phone(sender);
        List<String> contactExist = searchContact(this, sender_ori);


        if (!(cari.equals("") || cari.isEmpty())) {
            dbHelper.insertLog(created, ID_SERVICE, "Keyword '" + msg_client + "' ditemukan", "success", user_id);
            try {
                if (personalisasi.size() > 0) {
                    cari = cari.replace("[sapaan]", personalisasi.get(0)[2])
                            .replace("[nama_depan]", personalisasi.get(0)[3]).replace("[nama_belakang]", personalisasi.get(0)[4]);
                    dbHelper.insertLog(created, ID_SERVICE, "Nomor Pengirim berhasil ditemukan di DB Wabot", "normal", user_id);
                    dbHelper.insertLog(created, ID_SERVICE, "Balas : " + cari + " ke " + sender + "(" + personalisasi.get(0)[3] + ")", "normal", user_id);
                    action.sendReply(getApplicationContext(), cari);
                    dbHelper.insertLog(created, ID_SERVICE, "Berhasil dibalas", "success", user_id);
                } else if (contactExist.size() > 0) {
                    dbHelper.insertLog(created, ID_SERVICE, "Ditemukan " + contactExist.size() + " kontak di kontak android.", "warning", user_id);
                    for (int x = 0; x < contactExist.size(); x++) {
                        if (contactExist.get(x) != null) {
                            String number = contactExist.get(x).replace("+62", "62")
                                    .replace("-", "").replace(" ", "");
                            personalisasi = dbHelper.cari_phone(number);
                            dbHelper.insertLog(created, ID_SERVICE, "Cari Nomor " + contactExist.get(x) + " di DB Wabot", "warning", user_id);
                            if (personalisasi.size() > 0) {
                                cari = cari.replace("[sapaan]", personalisasi.get(0)[2])
                                        .replace("[nama_depan]", personalisasi.get(0)[3]).replace("[nama_belakang]", personalisasi.get(0)[4]);
                                dbHelper.insertLog(created, ID_SERVICE, "Nomor Pengirim berhasil ditemukan di DB Wabot", "normal", user_id);
                                dbHelper.insertLog(created, ID_SERVICE, "Balas : " + cari + " ke " + sender + "(" + personalisasi.get(0)[3] + ")", "normal", user_id);
                                action.sendReply(getApplicationContext(), cari);
                                dbHelper.insertLog(created, ID_SERVICE, "Berhasil dibalas", "success", user_id);
                                break;
                            }
                        }

                    }
                }
                if (personalisasi.size() == 0) {
                    cari = cari.replace("[sapaan]", "")
                            .replace("[nama_depan]", sender_ori).replace("[nama_belakang]", sender_ori);
                    dbHelper.insertLog(created, ID_SERVICE, "Nomor Pengirim tidak ditemukan di DB Wabot", "warning", user_id);
                    dbHelper.insertLog(created, ID_SERVICE, "Balas : " + cari + " ke " + sender_ori, "normal", user_id);
                    action.sendReply(getApplicationContext(), cari);
                    dbHelper.insertLog(created, ID_SERVICE, "Berhasil dibalas", "success", user_id);
                }
            } catch (PendingIntent.CanceledException e) {
                String stackTrace = Log.getStackTraceString(e);
                e.printStackTrace();
                dbHelper.insertLog(created, ID_SERVICE, "Gagal dibalas. Error : " + stackTrace, "danger", user_id);
            } catch (Exception e) {
                String stackTrace = Log.getStackTraceString(e);
                dbHelper.insertLog(created, ID_SERVICE, "Error : " + stackTrace, "danger", user_id);
            }

        } else {
            Log.i(TAG, "Msg: " + msg_client);
        }

    }

    private void send_notif() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        final String created = df.format(c);
        dbHelper = new DBHelper(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        String url_reversal = session.getValue("url_reversal");
        Log.e(TAG, "url_reversal:" + url_reversal);
        if (url_reversal == null || url_reversal.isEmpty() || url_reversal.equals("null")) {
            return;
        }
        if (!session.isLoggedIn()) {
            return;
        }
        //final String token = userDetail.get(KEY_TOKEN);

        final List<String[]> dataReceived = dbHelper.getReceivePending(user_id);
        JSONArray sender = new JSONArray();
        JSONArray message = new JSONArray();
        JSONArray when = new JSONArray();
        JSONArray date_created = new JSONArray();
        Log.e(TAG, "dataReceived:" + dataReceived.toString());
        if (dataReceived.size() <= 0) {
            return;
        } else {
            for (int i = 0; i < dataReceived.size(); i++) {
                sender.put(dataReceived.get(i)[1]);
                message.put(dataReceived.get(i)[2]);
                when.put(dataReceived.get(i)[4]);
                date_created.put(dataReceived.get(i)[5]);
            }
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("sender", sender);
            requestBody.put("message", message);
            requestBody.put("id", when);
            requestBody.put("created", date_created);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(url_reversal)
                .buildUpon()
                .toString();
        Log.i(TAG, "uri : " + uri + " body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                for (int i = 0; i < dataReceived.size(); i++) {
                    dbHelper.updateReceived(dataReceived.get(i)[0], "1");
                }
                try {
                    Log.e(TAG, response.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, errorResponseString(error));
                dbHelper.insertLog(created, ID_SERVICE, "send notif onErrorResponse: " + errorResponseString(error), "danger", user_id);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                return new HashMap<>();
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);


    }

    public List<String> searchContact(Context context, String name) {
/// number is the phone number
        String[] whereParams = new String[]{"%" + name + "%"};
        String selection = String.format("%s > 0 and display_name LIKE ?", ContactsContract.Contacts.HAS_PHONE_NUMBER);
        String[] mPhoneNumberProjection = {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        try (Cursor cur = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, mPhoneNumberProjection, selection, whereParams, null)) {
            if (cur != null && cur.moveToFirst()) {
                List<String> list = new ArrayList<>();
                while (cur.moveToNext()) {
                    int phoneNumberIndex = cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cur.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    String phoneNumber = cur.getString(phoneNumberIndex);
                    String displayName = cur.getString(nameIndex);
                    //String number = cur.getString(1);
                    if (displayName.trim().equals(name)) {
                        list.add(phoneNumber);
                    }
                }
                cur.close();
                return list;
            }
        }
        return new ArrayList<>();
    }

//    private void writeData(String str) {
//        try {
////            bw.newLine();
////            bw.write("NOTE");
//            bw.newLine();
//            bw.write(str);
//            bw.newLine();
////            bw.newLine();
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void init() {
//        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        try {
//            FileOutputStream fos = new FileOutputStream(newFile(), true);
//            OutputStreamWriter osw = new OutputStreamWriter(fos);
//            bw = new BufferedWriter(osw);
//        } catch (IOException e) {
//            Log.d("KEVIN", "BufferedWriter Initialization error");
//        }
//        Log.d("KEVIN", "Initialization Successful");
//    }
//
//    private File newFile() {
//        File fileDir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ANotification");
//        fileDir.mkdir();
//        String basePath = Environment.getExternalStorageDirectory() + File.separator + "ANotification" + File.separator + "record.txt";
//        return new File(basePath);
//
//    }
//
//
//    class MyHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//
//            switch (msg.what) {
//                case 1:
////                    Toast.makeText(MyService.this,"Bingo",Toast.LENGTH_SHORT).show();
//
//
//
//
//            }
//        }
//    }
}