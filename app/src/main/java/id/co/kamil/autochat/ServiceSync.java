package id.co.kamil.autochat;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.bulksender.WASendService;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.database.Kontak;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;
import rkr.simplekeyboard.inputmethod.keyboard.internal.PointerTrackerQueue;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_PESAN_ANTRIAN;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB2;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB_OUTBOX;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB_OUTBOX_REAL;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CHILD;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.BULK_SENDER_ON_SCREEN;
import static id.co.kamil.autochat.utils.SharPref.DELAY_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDING;
import static id.co.kamil.autochat.utils.SharPref.STATUS_ERROR_TRY_AGAIN;
import static id.co.kamil.autochat.utils.SharPref.TRY_AGAIN_BULKSENDER;
import static id.co.kamil.autochat.utils.Utils.SaveImage;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.fileExist;
import static id.co.kamil.autochat.utils.Utils.getDirWabot;
import static id.co.kamil.autochat.utils.Utils.sha1;

public class ServiceSync extends Service {
    private static final String TAG = "ServiceSync";
    public static final String ID_SERVICE_WA = "BulkSender";
    private static final String ID_SERVICE_SYNC = "Sync";
    int mStartMode; // indicates how to behave if the service is killed
    IBinder mBinder; // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    private DBHelper dbHelper;
    private int dbVersionCode = 0;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private boolean is_synchronizing = false;
    private Handler handler;
    private boolean statusWASender = true;
    private Handler handlerWA;
    private boolean is_send = false;
    private boolean is_uploading_outbox = false;
    //private boolean layar_nyala = true;
    private boolean config_on_screen = true;
    private SharPref sharePref;
    private boolean is_parent;
    private String user_id;
    private PowerManager.WakeLock wakelock;
    private Handler handlerOutbox;
    private boolean is_synchronizing_outbox = false;
    private boolean is_synchronizing_db = false;
    private int page_kontak_wabot = 0;
    private DatabaseReference fOutboxRef;
    private List<String[]> dataAntrianPesanFirebase = new ArrayList<>();
    private List<String[]> dataOutboxUploadPending = new ArrayList<>();
    private FirebaseDatabase dbFirebase;
//    private DatabaseReference mKontakReference;


    @Override
    public void onCreate() {
        // The service is being created
        session = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        refreshToken();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        session = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        try{
            if (FirebaseApp.getApps(this).isEmpty())
            {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        dbFirebase = FirebaseDatabase.getInstance();

        fOutboxRef = dbFirebase.getReference().child("outbox").child(session.getValue(KEY_CUST_ID));
        fOutboxRef.keepSynced(true);
        ChildEventListener childListenerOutbox = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    Log.e(TAG, "childAdd:" + dataSnapshot.toString() + ", String : " + s);
                    //dbHelper = new DBHelper(getApplicationContext());
                    String id = dataSnapshot.child("id").getValue().toString();
                    String destination_number = dataSnapshot.child("destination_number").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    String image_hash = dataSnapshot.child("image_hash").getValue().toString();
                    String image_url = dataSnapshot.child("image_url").getValue().toString();
                    String index_order = dataSnapshot.child("error_again").getValue().toString();
                    String sent = dataSnapshot.child("sent").getValue().toString();
                    if (sent.isEmpty()) {
                        dataAntrianPesanFirebase.add(new String[]{id, destination_number, message, image_hash, image_url, index_order});
                    } else {
                        dataOutboxUploadPending.add(new String[]{id, sent});
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    Log.e(TAG, "childChanged:" + dataSnapshot.toString() + ", String : " + s);
                    //dbHelper = new DBHelper(getApplicationContext());
                    String id = dataSnapshot.child("id").getValue().toString();
                    String destination_number = dataSnapshot.child("destination_number").getValue().toString();
                    String message = dataSnapshot.child("message").getValue().toString();
                    String image_hash = dataSnapshot.child("image_hash").getValue().toString();
                    String image_url = dataSnapshot.child("image_url").getValue().toString();
                    String index_order = dataSnapshot.child("error_again").getValue().toString();
                    String sent = dataSnapshot.child("sent").getValue().toString();
                    for (int i = 0; i < dataAntrianPesanFirebase.size(); i++) {
                        String[] str = dataAntrianPesanFirebase.get(i);
                        if (str[i].equals(id)) {
                            dataAntrianPesanFirebase.remove(i);
                            if (sent.isEmpty()) {
                                dataAntrianPesanFirebase.add(i, new String[]{id, destination_number, message, image_hash, image_url, index_order});
                            } else {
                                dataOutboxUploadPending.add(new String[]{id, sent});
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Log.e(TAG, "childRemoved:" + dataSnapshot.toString());
                    //dbHelper = new DBHelper(getApplicationContext());
                    String id = dataSnapshot.getKey();
                    deleteArrayListFirebaseAntrian(id);
                    //dbHelper.deleteOutboxById(id);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.e(TAG, "onChildMoved: " + dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                throw databaseError.toException();
            }
        };
        fOutboxRef.addChildEventListener(childListenerOutbox);

        startTimerDB(0);
        //startTimer();
        startWASender();
        startUploadOutbox();
        //startTimerOutbox();
        //registerReceiver(screenactionreceiver, screenactionreceiver.getFilter());

        return mStartMode;
    }


    private boolean deleteArrayListFirebaseAntrian(String id){
        try{
            for (int i = 0;i<dataAntrianPesanFirebase.size();i++){
                String[] str = dataAntrianPesanFirebase.get(i);
                if (str[0].equals(id)){
                    dataAntrianPesanFirebase.remove(id);
                    return true;
                }
            }
            for (int i = 0;i<dataOutboxUploadPending.size();i++){
                String[] str = dataOutboxUploadPending.get(i);
                if (str[0].equals(id)){
                    dataOutboxUploadPending.remove(id);
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
//    private void updateContact(Kontak itemKontak){
//        mKontakReference.setValue(itemKontak);
//    }
//    private void deleteContact(Kontak itemKontak){
//        mKontakReference.
//    }
//    ScreenActionReceiver screenactionreceiver = new ScreenActionReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            super.onReceive(context, intent);
//            String action = intent.getAction();
//
//            if(Intent.ACTION_SCREEN_ON.equals(action))
//            {
//                layar_nyala = true;
//                Log.d(TAG, "screen is on...");
//            }
//
//            else if(Intent.ACTION_SCREEN_OFF.equals(action))
//            {
//                layar_nyala = false;
//                Log.d(TAG, "screen is off...");
//            }
//
//            else if(Intent.ACTION_USER_PRESENT.equals(action))
//            {
//                layar_nyala = false;
//                Log.d(TAG, "screen is unlock...");
//
//            }
//        }
//    };


    private void refreshToken(){
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        boolean is_child = Boolean.parseBoolean(userDetail.get(KEY_CHILD));
        if (is_child){
            is_parent = false;
        }else{
            is_parent = true;
        }
    }
    private void startTimerDB(final int position) {
        handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        if(is_synchronizing_db==false){
                            if (position == 0){
                                syncKontak();
                            }else if(position==1){
                                syncAutoText();
                            }else if(position==2){
                                syncAutoreply();
                            }else if(position==3){
                                syncKamus();
                            }else if(position==4){
                                syncTemplatePromosi();
                                //syncKontakWabot();
                            }
                        }
                    }
                }, 10000);
    }
    private void startTimer() {
        handler = new Handler();
        handler.postDelayed(
                new Runnable() {
                    public void run() {
                        if(is_synchronizing==false && is_send == false){
                            //syncDatabase();
                        }
                    }
                }, 20000);
    }
    private void startTimerOutbox() {
        handlerOutbox = new Handler();
        handlerOutbox.postDelayed(
                new Runnable() {
                    public void run() {
                        if(is_synchronizing_outbox==false && is_send == false){
                            syncDatabaseOutbox();
                        }
                    }
                }, 15000);
    }
    private void syncDatabaseOutbox() {
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing_outbox = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_outbox = false;
            startTimerOutbox();
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database Outbox" ,"normal",user_id);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String uri = Uri.parse(URL_SYNC_DB_OUTBOX_REAL)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_outbox = false;
                startTimerOutbox();
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing_outbox);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB Outbox: " + message ,"warning",user_id);
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Outbox Lokal" ,"normal",user_id);
                        final JSONArray outbox = response.getJSONArray("outbox");

                        dbHelper.deleteAllOutbox(0);
                        if (outbox.length()>0){
                            for(int i=0;i<outbox.length();i++){
                                final String id = outbox.getJSONObject(i).getString("id");
                                final String destination_number = outbox.getJSONObject(i).getString("destination_number");
                                final String message_outbox = outbox.getJSONObject(i).getString("message");
                                final String image_hash = outbox.getJSONObject(i).getString("image_hash");
                                final String image_url = outbox.getJSONObject(i).getString("image_url");
                                Cursor outbox_by_id = null;
                                try {
                                    outbox_by_id = dbHelper.outboxById(id);
                                    if (outbox_by_id.getCount()<=0){
                                        dbHelper.insertOutbox(id,destination_number,message_outbox,image_hash,image_url);
                                    }
                                } finally {
                                    if(outbox_by_id != null)
                                        outbox_by_id.close();
                                }


                            }
                        }
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB Outbox : " + message ,"normal",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB Outbox : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB Outbox : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_outbox = false;
                startTimerOutbox();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncKontak(){
        final String fieldverdb = "ver_db_kontak";
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing_db = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_db = false;
            startTimerDB(1);
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi kontak....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
            requestBody.put("field","kontak");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB2)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_db = false;
                startTimerDB(1);
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    Log.i(TAG,"response:" + response);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final JSONArray data = response.getJSONArray("data");

                            dbHelper.deleteAllContact();
                            if (data.length()>0){
                                for(int i=0;i<data.length();i++){
                                    final String id = data.getJSONObject(i).getString("id");
                                    final String phone = data.getJSONObject(i).getString("phone");
                                    final String sapaan = data.getJSONObject(i).getString("sapaan");
                                    final String firstname = data.getJSONObject(i).getString("firstname");
                                    final String lastname = data.getJSONObject(i).getString("lastname");
                                    dbHelper.insertContact(id,phone,sapaan,firstname,lastname);
                                }
                            }
                        }
                        dbHelper.updateDBVersion2(versionCode,fieldverdb);
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_db = false;
                startTimerDB(1);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncAutoText(){
        final String fieldverdb = "ver_db_autotext";
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing_db = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_db = false;
            startTimerDB(2);
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi autotext....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
            requestBody.put("field","autotext");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB2)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_db = false;
                startTimerDB(2);
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    Log.i(TAG,"response:" + response);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final JSONArray data = response.getJSONArray("data");

                            dbHelper.deleteAllAutoText();
                            if (data.length()>0){
                                for(int i=0;i<data.length();i++){
                                    final String shorcut = data.getJSONObject(i).getString("shorcut");
                                    final String template = data.getJSONObject(i).getString("template");
                                    dbHelper.insertAutoText(shorcut,template);
                                }
                            }
                        }
                        dbHelper.updateDBVersion2(versionCode,fieldverdb);
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_db = false;
                startTimerDB(2);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncAutoreply(){
        final String fieldverdb = "ver_db_autoreply";
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing_db = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_db = false;
            startTimerDB(3);
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi autoreply....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
            requestBody.put("field","autoreply");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB2)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_db = false;
                startTimerDB(3);
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    Log.i(TAG,"response:" + response);
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final JSONArray data = response.getJSONArray("data");

                            dbHelper.deleteAllAutoReply();
                            if (data.length()>0){
                                for(int i=0;i<data.length();i++){
                                    final String keyword = data.getJSONObject(i).getString("keyword");
                                    final String balasan = data.getJSONObject(i).getString("balasan");
                                    dbHelper.insertAutoReply(keyword,balasan);
                                }
                            }
                        }
                        dbHelper.updateDBVersion2(versionCode,fieldverdb);
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_db = false;
                startTimerDB(3);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncKamus(){
        final String fieldverdb = "ver_kamus";
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing_db = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_db = false;
            startTimerDB(4);
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi kamus....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
            requestBody.put("field","t_dictionary");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB2)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_db = false;
                startTimerDB(4);
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    Log.i(TAG,"response:" + response);
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final JSONArray data = response.getJSONArray("data");

                            dbHelper.deleteAllDictionary();
                            if (data.length()>0){
                                for(int i=0;i<data.length();i++){
                                    final String id = data.getJSONObject(i).getString("id");
                                    final String keyword = data.getJSONObject(i).getString("keyword");
                                    final String nilai = data.getJSONObject(i).getString("nilai");
                                    dbHelper.insertKamus(id,keyword,nilai);
                                }
                            }
                        }
                        dbHelper.updateDBVersion2(versionCode,fieldverdb);
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_db = false;
                startTimerDB(4);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncTemplatePromosi(){
        final String fieldverdb = "ver_template_promosi";
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing_db = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_db = false;
            startTimerDB(4);
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi template....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
            requestBody.put("field","t_promosi");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB2)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_db = false;
                startTimerDB(4);
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    Log.i(TAG,"response:" + response);
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final JSONArray data = response.getJSONArray("data");

                            if (data.length()>0){
                                for(int i=0;i<data.length();i++){
                                    final String hash = data.getJSONObject(i).getString("picture_hash");
                                    final String url = data.getJSONObject(i).getString("picture_url");
                                    if (fileExist(getApplicationContext(),getDirWabot("template_promosi") + "/" + hash) == false){
                                        Picasso.with(getApplicationContext())
                                                .load(url)
                                                .into(new Target() {
                                                    @Override
                                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                        // Todo: Do something with your bitmap here
                                                        SaveImage(bitmap,"template_promosi",hash);
                                                    }

                                                    @Override
                                                    public void onBitmapFailed(Drawable errorDrawable) {
                                                    }

                                                    @Override
                                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                                    }
                                                });

                                    }
                                }
                            }
                        }
                        dbHelper.updateDBVersion2(versionCode,fieldverdb);
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_db = false;
                startTimerDB(4);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncKontakWabot(){
        final String fieldverdb = "ver_db_kontakwabot";
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        page_kontak_wabot = sharePref.getSessionInt("page_kontak_wabot");
        if (page_kontak_wabot <= 0 ){
            page_kontak_wabot = 1;
        }
        is_synchronizing_db = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing_db = false;
            startTimerDB(0);
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi kontak wabot....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
            requestBody.put("field","kontak_wabot");
            requestBody.put("page",page_kontak_wabot);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB2)
                .buildUpon()
                .toString();
        Log.e(TAG, "singrkonisasi part " + page_kontak_wabot);
        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing_db = false;
                startTimerDB(0);
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    Log.i(TAG,"response:" + response);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final JSONArray data = response.getJSONArray("data");

                            if (data.length()>0){
                                for (int i = 0;i<data.length();i++){
                                    String name = data.getJSONObject(i).getString("name");
                                    String phone = data.getJSONObject(i).getString("phone");
                                    if (!contactExists(getApplicationContext(),phone)){
                                        saveLocalContact(name,phone);
                                    }
                                }
                                sharePref.createSession("page_kontak_wabot",page_kontak_wabot + 1);
                            }else{
                                dbHelper.updateDBVersion2(versionCode,fieldverdb);
                            }
                        }else{
                            dbHelper.updateDBVersion2(versionCode,fieldverdb);
                        }
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing_db = false;
                startTimerDB(0);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void syncDatabase() {
        final String created = getTgl();
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        is_synchronizing = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){
            is_synchronizing = false;
            startTimer();
            return;
        }
        Log.i(TAG,"sedang melakukan sinkronisasi....");
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan Singkronisasi Database " ,"normal",user_id);
        dbVersionCode = dbHelper.getVersionCodeDB();
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("currentVersion",dbVersionCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_synchronizing = false;
                startTimer();
                Log.i(TAG,"sinkronisasi selesai! Status : " + is_synchronizing);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        if(!uptodate){
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"normal",user_id);
                            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sedang melakukan singkronisasi ke DB Lokal" ,"normal",user_id);
                            final String versionCode = response.getString("version_code");
                            final JSONArray autoreply = response.getJSONArray("autoreply");
                            final JSONArray autotext = response.getJSONArray("autotext");
                            final JSONArray outbox = response.getJSONArray("outbox");
                            final JSONArray kontak = response.getJSONArray("kontak");
                            final JSONArray singkron_kontak_wabot = response.getJSONArray("singkron_kontak_wabot");
                            //Log.i(TAG,response.toString());
                            if (singkron_kontak_wabot.length()>0){
                                for (int i = 0;i<singkron_kontak_wabot.length();i++){
                                    String name = singkron_kontak_wabot.getJSONObject(i).getString("name");
                                    String phone = singkron_kontak_wabot.getJSONObject(i).getString("phone");
                                    if (!contactExists(getApplicationContext(),phone)){
                                        saveLocalContact(name,phone);
                                    }
                                }
                            }
                            dbHelper.deleteAllContact();
                            if (kontak.length()>0){
                                for(int i=0;i<kontak.length();i++){
                                    final String id = kontak.getJSONObject(i).getString("id");
                                    final String phone = kontak.getJSONObject(i).getString("phone");
                                    final String sapaan = kontak.getJSONObject(i).getString("sapaan");
                                    final String firstname = kontak.getJSONObject(i).getString("firstname");
                                    final String lastname = kontak.getJSONObject(i).getString("lastname");
//                                    Kontak itemKontak = new Kontak(id, phone, sapaan, firstname, lastname);
//                                    mKontakReference.child("kontak").child(session.getValue(KEY_CUST_ID)).setValue(itemKontak);
                                    dbHelper.insertContact(id,phone,sapaan,firstname,lastname);
                                }
                            }
                            dbHelper.deleteAllAutoReply();
                            if (autoreply.length()>0){
                                for(int i=0;i<autoreply.length();i++){
                                    final String keyword = autoreply.getJSONObject(i).getString("keyword");
                                    final String balasan = autoreply.getJSONObject(i).getString("balasan");
                                    dbHelper.insertAutoReply(keyword,balasan);
                                }
                            }
                            dbHelper.deleteAllAutoText();
                            if (autotext.length()>0){
                                for(int i=0;i<autotext.length();i++){
                                    final String shorcut = autotext.getJSONObject(i).getString("shorcut");
                                    final String template = autotext.getJSONObject(i).getString("template");
                                    dbHelper.insertAutoText(shorcut,template);
                                }
                            }
                            dbHelper.updateDBVersion(versionCode);

//                            dbHelper.deleteAllOutbox(0);
//                            if (outbox.length()>0){
//                                for(int i=0;i<outbox.length();i++){
//                                    final String id = outbox.getJSONObject(i).getString("id");
//                                    Cursor outbox_by_id = dbHelper.outboxById(id);
//                                    final String destination_number = outbox.getJSONObject(i).getString("destination_number");
//                                    final String message_outbox = outbox.getJSONObject(i).getString("message");
//                                    final String image_hash = outbox.getJSONObject(i).getString("image_hash");
//                                    final String image_url = outbox.getJSONObject(i).getString("image_url");
//                                    if (outbox_by_id.getCount()<=0){
//                                        dbHelper.insertOutbox(id,destination_number,message_outbox,image_hash,image_url);
//                                    }
//                                }
//                            }

                        }
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + message ,"warning",user_id);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + e.getMessage() ,"danger",user_id);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Sync DB : " + errorResponseString(error) ,"danger",user_id);
                Log.i(TAG,"sinkronisasi error.");
                is_synchronizing = false;
                startTimer();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    public boolean contactExists(Context context, String number) {
/// number is the phone number
        String selection = String.format("%s > 0", ContactsContract.Contacts.HAS_PHONE_NUMBER);
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, selection, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        }
        finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
    private void saveLocalContact(String name,String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
        try
        {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (RemoteException e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (OperationApplicationException e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void startWASender(){
        trimCache(this);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c);
        final String created = formattedDate;

        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        user_id = userDetail.get(KEY_CUST_ID);

        //dbFirebase = FirebaseDatabase.getInstance();
        //fOutboxRef = dbFirebase.getReference().child("outbox").child(user_id);
        //Log.e(TAG,"PathOutbox:" + fOutboxRef);
        //addListenerOutbox();

        String config_delay_bulk = sharePref.getSessionStr(DELAY_BULK_SENDER);
        if (config_delay_bulk.isEmpty() || config_delay_bulk.equals("")){
            config_delay_bulk = "5";
        }
        long timerBulkSender = 3000;
        if (Integer.parseInt(config_delay_bulk)<5){
            config_delay_bulk = "5";
        }
        if(Integer.parseInt(config_delay_bulk)>0){
            timerBulkSender = Integer.parseInt(config_delay_bulk) * 1000;
        }
        handlerWA = new Handler();
        handlerWA.postDelayed(
                new Runnable() {
                    public void run() {
                        statusWASender = sharePref.getSessionBool(STATUS_BULK_SENDER);

                        if(is_send==false && statusWASender && is_parent && is_synchronizing == false){
                            //WASendService.setTextToSend(null);
                            Log.e("WhatsApp","SENDING....");
                            dbHelper.insertLog(created,ID_SERVICE_WA,"Persiapan pengiriman Pesan","normal",user_id);
                            sendWA();
                        }else{
                            startWASender();
                        }
                    }
                }, timerBulkSender);
    }
    private void startUploadOutbox(){
        handlerWA = new Handler();
        handlerWA.postDelayed(
                new Runnable() {
                    public void run() {
                        if (is_uploading_outbox==false && is_send==false){
                            uploadDataOutbox();
                        }
                    }
                }, 10000);
    }
    private boolean isAccessibilityEnabled() {
        int enabled = 0;
        final String service = getPackageName() +"/"+ WASendService.class.getCanonicalName();

        try {
            enabled = Settings.Secure.getInt(getApplicationContext().getContentResolver()
                    , Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        if (enabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getApplicationContext().getContentResolver()
                    , Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (settingValue != null) {
                String[] values = settingValue.split(":");
                for (String s : values) {
                    if (s.equalsIgnoreCase(service))
                        return true;
                }
            }
        }

        return false;
    }
    private String getTgl(){

        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    private void sendWA() {
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File dirImage = wrapper.getDir("images",MODE_PRIVATE);

        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        final String created = getTgl();
        config_on_screen = sharePref.getSessionBool(BULK_SENDER_ON_SCREEN);
        boolean accessibility = isAccessibilityEnabled();
        if (accessibility==false){
            is_send = false;
            dbHelper.insertLog(created,ID_SERVICE_WA,"Aksesibilitas Wabot tidak aktif","warning",user_id);
            startWASender();
            return;
        }
        if (session.isLoggedIn()==false){
            startWASender();
            return;
        }
        is_send = true;
        final List<String[]> antrianPesan = getAntrianPesan();
        Log.i(TAG,"countAntrian:" + antrianPesan.size());
        dbHelper.insertLog(created,ID_SERVICE_WA,"Sedang memeriksa antrian pesan","normal",user_id);
        if(antrianPesan.size()>0){

            String prefTryagain = sharePref.getSessionStr(TRY_AGAIN_BULKSENDER);
            boolean status_prefTryagain = sharePref.getSessionBool(STATUS_ERROR_TRY_AGAIN);
            if (prefTryagain.isEmpty() || prefTryagain.equals(null) || prefTryagain.equals("") || prefTryagain==null){
                prefTryagain = "5";
            }else if(Integer.parseInt(prefTryagain)<5){
                prefTryagain = "5";
            }
            if (appInstalledOrNot("com.whatsapp")){
                String id = antrianPesan.get(0)[0];
                String phoneNumber = antrianPesan.get(0)[1];
                String bodyMessage = antrianPesan.get(0)[2];
                String image_hash = antrianPesan.get(0)[3];
                String image_url = antrianPesan.get(0)[4];
                String index_order = antrianPesan.get(0)[5];
                int try_again = sharePref.getSessionInt("tryagain" + id);
                if (index_order == null || index_order.equals(null) || index_order.equals("null") || index_order.isEmpty()){
                    fOutboxRef.child(id).child("error_again").setValue(1);
                    //dbHelper.updateAntrianPesan(id,"1");
                }else{
                    if(status_prefTryagain){
                        if (Integer.valueOf(index_order) >= Integer.parseInt(prefTryagain)){
                            //sharePref.createSession(STATUS_BULK_SENDER, false);
                            is_send = false;
                            hapusPesan();
//                            deleteArrayListFirebaseAntrian(id);
//                            fOutboxRef.child(id).removeValue();
                            startWASender();
                            return;
                        }
                    }
                    fOutboxRef.child(id).child("error_again").setValue(Integer.parseInt(index_order)+1);
                }
                Log.i(TAG,"send message...");
                Log.i(TAG,"phone : " + phoneNumber);
                Log.i(TAG,"message : " + bodyMessage);
                String bodyLog  = "";
                if (bodyMessage.length()>100){
                    bodyLog = bodyMessage.substring(0,100) + "...";
                }else{
                    bodyLog = bodyMessage;
                }
                dbHelper.insertLog(created,ID_SERVICE_WA,"Persiapan Kirim Pesan ke " + phoneNumber + ", isi Pesan : " + bodyLog,"normal",user_id);
                Log.e(TAG,"ImageHash:" + image_hash);
                Log.e(TAG,"ImageHUrl:" + image_url);
                WASendService.setID(id);
                if ((image_hash==null || image_hash.isEmpty() || image_hash.equals(null) || image_hash.equals("null")) && (image_url == null || image_url.isEmpty() || image_url.equals(null)|| image_url.equals("null"))){
//                    Intent sendIntent = new Intent();
//                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    sendIntent.setAction(Intent.ACTION_SEND);
//                    sendIntent.setType("text/plain");
//                    sendIntent.setPackage("com.whatsapp");
//                    sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(phoneNumber)+"@s.whatsapp.net");
//                    sendIntent.putExtra(Intent.EXTRA_TEXT,bodyMessage);
//                    startActivity(sendIntent);
                        String text = bodyMessage;// Replace with your message.

                        String toNumber = PhoneNumberUtils.stripSeparators(phoneNumber);

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setPackage("com.whatsapp");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+ URLEncoder.encode(text, "UTF-8") ));
                        startActivity(intent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG,"Send Text:" );
                }else if ((image_hash==null || image_hash.isEmpty() || image_hash.equals(null) || image_hash.equals("null")) && !(image_url == null || image_url.isEmpty() || image_url.equals(null)|| image_url.equals("null"))){
                    DownloadTask task  = new DownloadTask();
                    task.setId(id);
                    task.execute(stringToURL(image_url));
                    Log.e(TAG,"Download Image:" );
                }else {

                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Uri uri = Uri.parse(image_hash);
                    Intent sendIntent = new Intent();
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setType("image");
                    sendIntent.putExtra(Intent.EXTRA_STREAM,uri);
                    sendIntent.setComponent(new ComponentName("com.whatsapp","com.whatsapp.ContactPicker"));
                    sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(phoneNumber)+"@s.whatsapp.net");
                    sendIntent.putExtra(Intent.EXTRA_TEXT,bodyMessage);
                    startActivity(sendIntent);
                    Log.e(TAG,"Send Image" );
                }


            }
        }else{
            sharePref.createSession(STATUS_BULK_SENDING,false);
            dbHelper.insertLog(created,ID_SERVICE_WA,"Tidak ada antrian pesan","normal",user_id);
            //dbHelper.updateDBVersion("0");
            Log.e(TAG,"tidak ada pesan antrian");
        }
        is_send = false;
        startWASender();
    }
    private void hapusPesan() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONArray jsonArray = new JSONArray();

        final List<String[]> antrianPesan = getAntrianPesan();
        String prefTryagain = sharePref.getSessionStr(TRY_AGAIN_BULKSENDER);
        if (TextUtils.isEmpty(prefTryagain)){
            prefTryagain = "5";
        }
        Integer maxTryAgain = Integer.parseInt(prefTryagain);
//        else if(Integer.parseInt(prefTryagain)<5){
//            prefTryagain = "5";
//        }
        if(antrianPesan.size()<=0){
            return;
        }
        for (int i = 0 ; i < antrianPesan.size();i++){
            String id = antrianPesan.get(i)[0];
            String index_order = antrianPesan.get(i)[5];
            Log.e(TAG,"index:"+ index_order);
            Log.e(TAG,"prefTryagain:"+ prefTryagain);
            Integer tryAgain = Integer.parseInt(index_order);
            if (tryAgain >= maxTryAgain){
//                deleteArrayListFirebaseAntrian(id);
                jsonArray.put(Integer.parseInt(id));
            }
        }
        if(jsonArray.length()<=0) return;
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,request_body.toString());
        final String uri = Uri.parse(URL_POST_HAPUS_PESAN_ANTRIAN)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        for (int i =0;i<jsonArray.length();i++){
                            fOutboxRef.child(jsonArray.getString(i)).removeValue();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getApplicationContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("x-api-key",token);
                return header;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);

    }
    private List<String[]> getAntrianPesan() {
        Collections.sort(dataAntrianPesanFirebase,new Comparator<String[]>() {
            public int compare(String[] strings, String[] otherStrings) {
                return strings[5].compareTo(otherStrings[5]);
            }
        });
        return dataAntrianPesanFirebase;
    }
    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
            //Toast.makeText(context, "Cache berhasil di bersihkan", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        }
        else {
            return false;
        }
    }
    private class DownloadTask extends AsyncTask<URL,Void,Bitmap> {
        // Before the tasks execution
        String idMessage;
        public void setId(String id){
            idMessage = id;
        }
        protected void onPreExecute(){
            // Display the progress dialog on async task start
            String created = getTgl();
            dbHelper.lockOutboxById(idMessage);
            dbHelper.insertLog(created,ID_SERVICE_WA,"Sedang persiapan download gambar pesan..","normal",user_id);
            //mProgressDialog.show();
        }

        // Do the task in background/non UI thread
        protected Bitmap doInBackground(URL...urls){
            String created = getTgl();
            URL url = urls[0];
            HttpURLConnection connection = null;

            try{
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                dbHelper.insertLog(created,ID_SERVICE_WA,"Sedang download gambar pesan..","normal",user_id);
                InputStream inputStream = connection.getInputStream();
                /*
                    BufferedInputStream
                        A BufferedInputStream adds functionality to another input stream-namely,
                        the ability to buffer the input and to support the mark and reset methods.
                */
                /*
                    BufferedInputStream(InputStream in)
                        Creates a BufferedInputStream and saves its argument,
                        the input stream in, for later use.
                */
                // Initialize a new BufferedInputStream from InputStream
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                /*
                    decodeStream
                        Bitmap decodeStream (InputStream is)
                            Decode an input stream into a bitmap. If the input stream is null, or
                            cannot be used to decode a bitmap, the function returns null. The stream's
                            position will be where ever it was after the encoded data was read.

                        Parameters
                            is InputStream : The input stream that holds the raw data
                                              to be decoded into a bitmap.
                        Returns
                            Bitmap : The decoded bitmap, or null if the image data could not be decoded.
                */
                // Convert BufferedInputStream to Bitmap object
                Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                // Return the downloaded bitmap
                return bmp;

            }catch(IOException e){
                e.printStackTrace();
            }finally{
                // Disconnect the http url connection
                connection.disconnect();
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result){
            String created = getTgl();
            // Hide the progress dialog
            //dbHelper.unlockOutboxById(idMessage);
            if(result!=null){
                // Display the downloaded image into ImageView

                // Save bitmap to internal storage
                dbHelper.insertLog(created,ID_SERVICE_WA,"berhail download gambar pesan","success",user_id);
                try {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    String sha1 = sha1(timestamp.getTime() + ".jpg");
                    //dbHelper.updateOutboxImageById(idMessage,sha1);
                    fOutboxRef.child(idMessage).child("image_hash").setValue(sha1);
                    Uri imageInternalUri = saveImageToInternalStorage(result,sha1);
                    for (int i = 0; i < dataAntrianPesanFirebase.size(); i++) {
                        String[] str = dataAntrianPesanFirebase.get(i);
                        if (str[i].equals(idMessage)) {
                            dataAntrianPesanFirebase.remove(i);
                            dataAntrianPesanFirebase.add(i, new String[]{idMessage, str[1], str[2], sha1, str[4], str[5]});
                        }
                    }
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }else {
                dbHelper.insertLog(created,ID_SERVICE_WA,"download gambar pesan gagal","danger",user_id);
                // Notify user that an error occurred while downloading image
                //Snackbar.make(mCLayout,"Error",Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // Custom method to convert string to url
    protected URL stringToURL(String urlString){
        try{
            URL url = new URL(urlString);
            return url;
        }catch(MalformedURLException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    // Custom method to save a bitmap into internal storage
    protected Uri saveImageToInternalStorage(Bitmap bitmap, String filename){
        // Initialize ContextWrapper
        ContextWrapper wrapper = new ContextWrapper(getApplicationContext());

        // Initializing a new file
        // The bellow line return a directory in internal storage
        File file = wrapper.getDir("Images",MODE_PRIVATE);

        // Create a file to save the image
        file = new File(file, filename+".jpg");

        try{
            // Initialize a new OutputStream
            OutputStream stream = null;

            // If the output file exists, it can be replaced or appended to it
            stream = new FileOutputStream(file);

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

            // Flushes the stream
            stream.flush();

            // Closes the stream
            stream.close();

        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }

        // Parse the gallery image url to uri
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());

        // Return the saved image Uri
        return savedImageURI;
    }
    public static File savebitmap(Bitmap bmp) throws IOException {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "testimage.jpg");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    private void uploadDataOutbox() {
        final String created = getTgl();
        // Upload data outbox yg sudah berhasil kirim
        dbHelper = new DBHelper(this);
        is_uploading_outbox = true;
        refreshToken();
        if (token.isEmpty() || token == null || token.equals(null)){

            is_uploading_outbox = false;
            startUploadOutbox();
            return;
        }

        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Check Outbox..","normal",user_id);
        Log.i(TAG,"persiapan upload data outbox");

        JSONArray arrIdOutbox = new JSONArray();
        JSONArray arrDate = new JSONArray();
        if (dataOutboxUploadPending.size()>0){
            for(int i =0;i<dataOutboxUploadPending.size();i++){
                arrIdOutbox.put(dataOutboxUploadPending.get(i)[0]);
                arrDate.put(dataOutboxUploadPending.get(i)[1]);
            }
        }
        if (arrIdOutbox.length()<=0){
            dbHelper.insertLog(created,ID_SERVICE_SYNC,"Data Outbox kosong","normal",user_id);
            is_uploading_outbox = false;
            startUploadOutbox();
            return;
        }
        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Persiapan singkronisasi outbox","normal",user_id);
        Log.i(TAG,"sedang upload data outbox");
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id",arrIdOutbox);
            requestBody.put("sent_date",arrDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_SYNC_DB_OUTBOX)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                is_uploading_outbox = false;
                startUploadOutbox();
                Log.i(TAG,"upload outbox selesai! Status : " + is_uploading_outbox);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        for (int i=0;i<data.length();i++){
                            String id = data.getString(i);
                            fOutboxRef.child(id).removeValue();
                            //dbHelper.deleteOutboxById(id);
                        }
                        Log.i(TAG,"upload outbox selesai! data : " + data.toString());
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,"Singkronisasi outbox selesai","success",user_id);
                    }else{
                        dbHelper.insertLog(created,ID_SERVICE_SYNC,message,"warning",user_id);
                        Log.i(TAG,message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dbHelper.insertLog(created,ID_SERVICE_SYNC,"Error Singkronisasi Outbox : " + e.getMessage(),"danger",user_id);
                    Log.i(TAG,e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                Log.i(TAG,"upload outbox error.");
                dbHelper.insertLog(created,ID_SERVICE_SYNC,"Error Singkronisasi Outbox : " +errorResponseString(error),"danger",user_id);
                is_uploading_outbox = false;
                startUploadOutbox();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key",token);
                return header;
            }
        };
        //RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        //jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
        startUploadOutbox();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        //layar_nyala = false;
    }



}
