package id.co.kamil.autochat.bulksender;

import android.accessibilityservice.AccessibilityService;
import android.net.Uri;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.ServiceSyncNew.ID_SERVICE_WA;
import static id.co.kamil.autochat.ServiceSyncNew.is_send;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB_OUTBOX;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDING;
import static id.co.kamil.autochat.utils.SharPref.SELECTED_WHATSAPP;


public class WASendService extends AccessibilityService {
    private static String typeApp;
    private static String idMessage,created;
    private static final String TAG = "WASendService";
    private static final String ID_SERVICE_SYNC = "Sync";
    private static String waButtonSendID = "com.whatsapp.w4b:id/send";
    private static String waBackButtonID = "com.whatsapp.w4b:id/back";
    private static long try_again = 0;
    private DBHelper dbHelper;
    private String user_id;


    public static void setID(String id,String tgl) {
        idMessage = id;
        created=tgl;
        if(id==null){
            return;
        }
        Log.e(TAG,"SetID:"+id);
        is_send=true;
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
        try {

            dbHelper = new DBHelper(this);
            SessionManager session = new SessionManager(this);
            HashMap<String, String> userDetail = session.getUserDetails();
            user_id = userDetail.get(KEY_CUST_ID);
            SharPref sharePref = new SharPref(this);

            saveParseOutbox(session.getValue(KEY_CUST_ID));
            boolean statusWASender = sharePref.getSessionBool(STATUS_BULK_SENDER);
            boolean statusSending = sharePref.getSessionBool(STATUS_BULK_SENDING);
            if (!statusWASender) {
                return;
            }
            if(idMessage==null){
                return;
            }

            final AccessibilityNodeInfo buttonSend;
            final AccessibilityNodeInfo backButton;
            waButtonSendID = sharePref.getSessionStr(SELECTED_WHATSAPP)+":id/send";
            waBackButtonID = sharePref.getSessionStr(SELECTED_WHATSAPP)+":id/back";
            buttonSend = getNodeInfo(rootNode, waButtonSendID);
            backButton = getNodeInfo(rootNode, waBackButtonID);

            if (buttonSend == null) {
                if (backButton == null) {
                    dbHelper.insertLog(created, ID_SERVICE_WA, "ID Button Send : " + null + " dan ID Button Back : " + null, "warning", user_id);
                    Log.e(TAG, "button Send : " +idMessage);
                    System.out.println("cek 1");
                } else if (statusSending) {
                    backButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    System.out.println("cek 2");
                } else {
                    dbHelper.insertLog(created, ID_SERVICE_WA, "ID Button Send : " + null, "warning", user_id);
                    System.out.println("cek 3");
                }
                updateSentPesan(idMessage,created, new SentPesanListener() {
                    @Override
                    public void done() {
                        is_send=false;
                    }
                });
            }else {
                buttonSend.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.e(TAG, "button Send : " +idMessage);
                dbHelper = new DBHelper(this);
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                final String tglSent = df.format(c);

                ParseQuery<ParseObject> query = ParseQuery.getQuery("OutboxMessage");
                query.whereEqualTo("idmessage",idMessage);
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject entity, ParseException e) {
                        if (e == null) {
                            String cek_sent=entity.get("sent")+"";
                            if(cek_sent.equals("")) {
                                entity.put("sent", tglSent);
                                entity.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        //dbHelper.updateSent(idMessage, "1", tglSent, "0");
                                        dbHelper.insertLog(tglSent, ID_SERVICE_WA, "Berhasil dikirim", "success", user_id);
                                        System.out.println("cek 4");
                                        setID(null , null);
                                        try_again = 0;

                                        backButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        updateSentPesan(idMessage,created, new SentPesanListener() {
                                            @Override
                                            public void done() {
                                                is_send=false;
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
            String stackTrace = Log.getStackTraceString(e);
            dbHelper.insertLog(created, ID_SERVICE_WA, stackTrace, "danger", user_id);
            System.out.println("cek 5");
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
    private void saveParseOutbox(final String user_id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Outbox");
        query.whereEqualTo("KeyCust",user_id);

        // Or use the the non-blocking method countInBackground method with a CountCallback
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                Date c = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                final String tglSent = df.format(c);
                if (e == null) {
                    if(count<=0){
                        ParseObject entity = new ParseObject("OutboxMessage");

                        //entity.put("KeyCust", user_id);
                        entity.put("sent", tglSent);
                        // Saves the new object.
                        // Notice that the SaveCallback is totally optional!
                        entity.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                // Here you can handle errors, if thrown. Otherwise, "e" should be null
                            }
                        });
                    }
                    Log.i(TAG,"count"+count);
                } else {
                    Log.i(TAG,e.getMessage());
                }
            }
        });
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onInterrupt() {

    }
    interface SentPesanListener{
        void done();
    }
    private void updateSentPesan(final String id,final String created,final SentPesanListener sentPesanListener) {
        SessionManager session = new SessionManager(this);
        HashMap<String, String> userDetail = session.getUserDetails();
        final String token = userDetail.get(KEY_TOKEN);
        if(token==null){
            sentPesanListener.done();
            return;
        }
        JSONArray arrIdOutbox = new JSONArray();
        JSONArray arrDate = new JSONArray();

        arrIdOutbox.put(id);
        arrDate.put(created);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", arrIdOutbox);
            requestBody.put("sent_date", arrDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, requestBody.toString());
        final String uri = Uri.parse(URL_SYNC_DB_OUTBOX)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG, message);
                    if (status) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("OutboxMessage");
                        query.whereEqualTo("idmessage",id);
                        // Or use the the non-blocking method countInBackground method with a CountCallback
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if(e==null){
                                    object.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            sentPesanListener.done();
                                        }
                                    });
                                }else{
                                    dbHelper.insertLog(created, ID_SERVICE_SYNC, e.getMessage(), "warning", user_id);
                                    Log.i(TAG, message);
                                    sentPesanListener.done();
                                }
                            }
                        });
                    } else {
                        dbHelper.insertLog(created, ID_SERVICE_SYNC, message, "warning", user_id);
                        Log.i(TAG, message);
                        sentPesanListener.done();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    sentPesanListener.done();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sentPesanListener.done();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("x-api-key", token);
                return header;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
}
