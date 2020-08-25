package id.co.kamil.autochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
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
import com.facebook.stetho.Stetho;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import id.co.kamil.autochat.adapter.ItemKontak;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.installreferrer.Application;
import id.co.kamil.autochat.installreferrer.ReferrerReceiver;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_GET_VERSION;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB;
import static id.co.kamil.autochat.utils.SessionManager.KEY_AFFILIATION;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.BULK_SENDER_ON_SCREEN;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_AUTO_REPLY;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_KONTAK;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_LEAD_MAGNET_BASIC;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_LEAD_MAGNET_PREMIUM;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_LINKPAGE_BASIC;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_LINKPAGE_PREMIUM;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_PESAN;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_SHORTEN;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_TEMPLATE_BASIC;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_TEMPLATE_PREMIUM;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_WAFORM_BASIC;
import static id.co.kamil.autochat.utils.SharPref.KEY_LIMIT_WAFORM_PREMIUM;
import static id.co.kamil.autochat.utils.SharPref.LINK_AKUN;
import static id.co.kamil.autochat.utils.SharPref.LINK_ECOURSE;
import static id.co.kamil.autochat.utils.SharPref.LINK_KURIR;
import static id.co.kamil.autochat.utils.SharPref.LINK_MARKETING_TOOL;
import static id.co.kamil.autochat.utils.SharPref.LINK_PULSA;
import static id.co.kamil.autochat.utils.SharPref.LINK_TIMWABOT;
import static id.co.kamil.autochat.utils.SharPref.LINK_TUTORIAL;
import static id.co.kamil.autochat.utils.SharPref.REFERRER_URL;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.openAppPlaystore;

public class SplashScreenActivity extends AppCompatActivity {

    private static final String TAG = "SplashScreenActivity";
    private static final int REQUEST_PERMISSION_STORAGE = 500;
    private DBHelper dbHelper;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private int dbVersionCode;
    private String affiliation;

    private static final String[] PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private String[][] kontakWabot = {};
    private String currentVersion = "0";
    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData();
        }
    };
    private SharPref sharePref;
    private int try_again_version;
    private TextView txtVersion;
    private KeyguardManager.KeyguardLock lock;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Stetho.initializeWithDefaults(this);
        updateData();
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, new IntentFilter(ReferrerReceiver.ACTION_UPDATE_DATA));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        sharePref = new SharPref(this);
        dbHelper = new DBHelper(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        txtVersion = (TextView) findViewById(R.id.txtVersion);

        boolean config_on_screen = sharePref.getSessionBool(BULK_SENDER_ON_SCREEN);
        //onscreen(config_on_screen);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        final InstallReferrerClient referrerClient;

        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        // Connection established.
                        try {
                            ReferrerDetails response = referrerClient.getInstallReferrer();
                            String referrerUrl = response.getInstallReferrer();
                            long referrerClickTime = response.getReferrerClickTimestampSeconds();
                            long appInstallTime = response.getInstallBeginTimestampSeconds();
                            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();

                            Log.i(TAG,"referrerUrl:" + referrerUrl);
                            Log.i(TAG,"referrerClickTime:" + referrerClickTime);
                            Log.i(TAG,"appInstallTime:" + appInstallTime);
                            Log.i(TAG,"instantExperienceLaunched:" + instantExperienceLaunched);

                            sharePref.createSession(REFERRER_URL,referrerUrl);
                            Bundle params = new Bundle();
                            params.putString("referrerUrl", referrerUrl);
                            mFirebaseAnalytics.logEvent("SplashReferrerUrl", params);

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        //Toast.makeText(SplashActivity.this, "FEATURE_NOT_SUPPORTED", Toast.LENGTH_SHORT).show();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        //Toast.makeText(SplashActivity.this, "SERVICE_UNAVAILABLE", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                //Toast.makeText(SplashActivity.this, "onInstallReferrerServiceDisconnected", Toast.LENGTH_SHORT).show();
            }
        });

        try_again_version = sharePref.getSessionInt("try_again_version");
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            txtVersion.setText(currentVersion);
            checkVersion();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Logger.getObservable().addObserver(this);
        //Logger.log(this, "Application.onCreate()");


    }

    private void checkVersion(){

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = Uri.parse(URL_GET_VERSION)
                .buildUpon()
                .appendQueryParameter("version",currentVersion)
                .toString();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    final boolean skip = response.getBoolean("skip");
                    final JSONArray kontak = response.getJSONArray("kontak_cs");
                    final String link_timwabot = response.getString("link_timwabot");
                    final String link_ecourse = response.getString("link_ecourse");
                    final String link_tutorial = response.getString("link_tutorial");
                    final String link_marketing_tool = response.getString("link_marketing_tool");
                    final String link_akun = response.getString("link_akun");
                    final String link_pulsa = response.getString("link_pulsa");
                    final String link_kurir = response.getString("link_kurir");
                    final String limit_kontak = response.getString("limit_kontak");
                    final String limit_shorten = response.getString("limit_shorten");
                    final String limit_auto_reply = response.getString("limit_auto_reply");
                    final String limit_pesan = response.getString("limit_pesan");
                    final String limit_lead_magnet_basic = response.getString("limit_lead_magnet_basic");
                    final String limit_lead_magnet_premium = response.getString("limit_lead_magnet_premium");
                    final String limit_waform_basic = response.getString("limit_waform_basic");
                    final String limit_waform_premium = response.getString("limit_waform_premium");
                    final String limit_linkpage_basic = response.getString("limit_linkpage_basic");
                    final String limit_linkpage_premium = response.getString("limit_linkpage_premium");
                    final String limit_template_basic = response.getString("limit_template_basic");
                    final String limit_template_premium = response.getString("limit_template_premium");

                    sharePref.createSession(LINK_TIMWABOT,link_timwabot);
                    sharePref.createSession(LINK_ECOURSE,link_ecourse);
                    sharePref.createSession(LINK_TUTORIAL,link_tutorial);
                    sharePref.createSession(LINK_MARKETING_TOOL,link_marketing_tool);
                    sharePref.createSession(LINK_AKUN,link_akun);
                    sharePref.createSession(LINK_PULSA,link_pulsa);
                    sharePref.createSession(LINK_KURIR,link_kurir);

                    sharePref.createSession(KEY_LIMIT_KONTAK,limit_kontak);
                    sharePref.createSession(KEY_LIMIT_SHORTEN,limit_shorten);
                    sharePref.createSession(KEY_LIMIT_AUTO_REPLY,limit_auto_reply);
                    sharePref.createSession(KEY_LIMIT_PESAN,limit_pesan);
                    sharePref.createSession(KEY_LIMIT_LEAD_MAGNET_BASIC,limit_lead_magnet_basic);
                    sharePref.createSession(KEY_LIMIT_LEAD_MAGNET_PREMIUM,limit_lead_magnet_premium);
                    sharePref.createSession(KEY_LIMIT_WAFORM_BASIC,limit_waform_basic);
                    sharePref.createSession(KEY_LIMIT_WAFORM_PREMIUM,limit_waform_premium);
                    sharePref.createSession(KEY_LIMIT_LINKPAGE_BASIC,limit_linkpage_basic);
                    sharePref.createSession(KEY_LIMIT_LINKPAGE_PREMIUM,limit_linkpage_premium);
                    sharePref.createSession(KEY_LIMIT_TEMPLATE_BASIC,limit_template_basic);
                    sharePref.createSession(KEY_LIMIT_TEMPLATE_PREMIUM,limit_template_premium);

                    kontakWabot = new String[kontak.length()][2];
                    for(int i =0;i<kontak.length();i++){
                        kontakWabot[i][0] = kontak.getJSONObject(i).getString("nomor");
                        kontakWabot[i][1] = kontak.getJSONObject(i).getString("nama");
                    }
                    for(int i =0;i<kontak.length();i++){
                        Log.i(TAG,"kontakWabot:" + kontakWabot[i][0] + " - " + kontakWabot[i][1]);
                    }
                    //Log.i(TAG,"kontakWabot:" + kontakWabot.toString());
                    if (status){
                        final String data = response.getString("data");
                        if (!currentVersion.equals(data)){
                            if (try_again_version==0 || try_again_version >=10){
                                if (skip){
                                    new AlertDialog.Builder(SplashScreenActivity.this)
                                            .setMessage(message)
                                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    openAppPlaystore(SplashScreenActivity.this);
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton("Lain kali", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (try_again_version>=10){
                                                        try_again_version = 0;
                                                    }
                                                    try_again_version++;
                                                    sharePref.createSession("try_again_version",try_again_version);
                                                    singkron();
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }else{
                                    new AlertDialog.Builder(SplashScreenActivity.this)
                                            .setMessage(message)
                                            .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    openAppPlaystore(SplashScreenActivity.this);
                                                    finish();
                                                }
                                            })
                                            .setCancelable(false)
                                            .show();
                                }

                            }else{
                                try_again_version++;
                                sharePref.createSession("try_again_version",try_again_version);
                                singkron();
                            }
                            Log.i(TAG,"try:" + try_again_version);

                        }else{
                            singkron();

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i(TAG,"Error Response : " + e.getMessage());
                    new AlertDialog.Builder(SplashScreenActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(final VolleyError error) {
                Log.i(TAG,"Volley Error : " + error.getMessage());
                //errorResponse(getApplicationContext(),error);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!SplashScreenActivity.this.isFinishing()) {
                            try {
                                new AlertDialog.Builder(SplashScreenActivity.this)
                                        .setMessage(getString(errorResponse(error)))
                                        .setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                checkVersion();
                                            }
                                        })
                                        .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .setCancelable(false)
                                        .show();
                            } catch (WindowManager.BadTokenException e) {
                                Log.e("WindowManagerBad ", e.toString());
                            }
                        }
                    }
                });

            }
        });
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void singkron() {
        checkContactWabot();
        callRequestPermission();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (session.isLoggedIn()){
                    //sync db
                    token = userDetail.get(KEY_TOKEN);
                    syncDatabase();
                }else{
                    startActivity(new
                            Intent(SplashScreenActivity.this,LoginActivity.class));
                    finish();
                }
            }
        },1000);
    }

    private void callRequestPermission() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
                callRequestPermission();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean contactExists(Context context, String number) {
/// number is the phone number
        try {
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
        }catch (NullPointerException e){

        }
        return false;
    }
    private void checkContactWabot() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            List<Integer> indexKontak = new ArrayList<>();
            for(int x=0;x<kontakWabot.length;x++){

                if (contactExists(this,kontakWabot[x][0])){
                    indexKontak.add(x);
                }
            }

            if (indexKontak.size() == kontakWabot.length){
                Log.i(TAG,"Kontak CS sudah terdaftar");
            }else{
                boolean add = true;
                for(int a = 0;a<kontakWabot.length;a++){
                    add = true;
                    for(int x = 0;x<indexKontak.size();x++){
                        if (a==indexKontak.get(x)){
                            add = false;
                            break;
                        }
                    }
                    if (add){
                        saveLocalContact(kontakWabot[a][1],kontakWabot[a][0]);
                        Log.i(TAG,"Add Kontak : " + kontakWabot[a][1]);
                    }
                }
            }

        }else{
            Toast.makeText(this, "Membutuhkan Permission Kontak", Toast.LENGTH_SHORT).show();

        }
    }

    private void saveLocalContact(String nama, String nomor) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, nama) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, nomor) // Number of the person
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
    private void updateData() {
        affiliation = Application.getReferrerDataRaw(this);
        if (affiliation.contains("Undefined")){
            affiliation = "";
        }
        if (affiliation.contains("utm_source") || affiliation.contains("utm_medium")){
            affiliation = "";
        }

//        session.setValue(KEY_AFFILIATION,affiliation);
        Log.i(TAG,"affiliation: " + affiliation);
    }

    private void syncDatabase() {
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

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String url_reversal = response.getString("url_reversal");
                        Log.e(TAG,"url_reversal"+url_reversal);
                        session.setValue("url_reversal",url_reversal);
                        if (uptodate){
                            startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                            finish();
                        }else{
                            final String versionCode = response.getString("version_code");
                            final JSONArray autoreply = response.getJSONArray("autoreply");
                            //final JSONArray outbox = response.getJSONArray("outbox");
                            dbHelper.deleteAllAutoReply();
                            if (autoreply.length()>0){
                                for(int i=0;i<autoreply.length();i++){
                                    final String keyword = autoreply.getJSONObject(i).getString("keyword");
                                    final String balasan = autoreply.getJSONObject(i).getString("balasan");
                                    dbHelper.insertAutoReply(keyword,balasan);
                                }
                            }
                            dbHelper.updateDBVersion(versionCode);
//                            dbHelper.deleteAllOutbox();
//                            if (outbox.length()>0){
//                                for(int i=0;i<outbox.length();i++){
//                                    final String id = outbox.getJSONObject(i).getString("id");
//                                    final String destination_number = outbox.getJSONObject(i).getString("destination_number");
//                                    final String message_outbox = outbox.getJSONObject(i).getString("message");
//                                    dbHelper.insertOutbox(id,destination_number,message_outbox);
//                                }
//                            }
                            startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                            finish();
                        }
                    }else {
                        new AlertDialog.Builder(SplashScreenActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(SplashScreenActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG,errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(SplashScreenActivity.this,error);
                }else{

                    Log.i(TAG,"Status Code : " + response.statusCode);
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(SplashScreenActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(SplashScreenActivity.this)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(SplashScreenActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                }
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
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateReceiver, new IntentFilter(ReferrerReceiver.ACTION_UPDATE_DATA));
        super.onResume();
    }

}
