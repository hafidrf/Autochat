package id.co.kamil.autochat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.co.kamil.autochat.bulksender.WASendService;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_PESAN_ANTRIAN;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CHILD;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.SELECTED_WHATSAPP;
import static id.co.kamil.autochat.utils.SharPref.DELAY_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDING;
import static id.co.kamil.autochat.utils.SharPref.STATUS_ERROR_TRY_AGAIN;
import static id.co.kamil.autochat.utils.SharPref.TRY_AGAIN_BULKSENDER;
import static id.co.kamil.autochat.utils.Utils.SaveImage;
import static id.co.kamil.autochat.utils.Utils.fileExist;
import static id.co.kamil.autochat.utils.Utils.getDirWabot;
import static id.co.kamil.autochat.utils.Utils.sha1;

public class ServiceSyncNew extends Service {
    private static final String ANDROID_CHANNEL_ID = "id.co.kamil.autochat.ForegroundChannel";
    private static final int ANDROID_FOREGROUND_ID = 3940;
    private static final String TAG = "ServiceSyncNew" ;
    public static final String ID_SERVICE_WA = "BulkSender";
    private DBHelper dbHelper;
    private SharPref sharePref;
    private String token;
    private boolean is_parent;
    private SessionManager session;
    private boolean is_synchronizing = false;
    public static boolean is_send = false;


    public ServiceSyncNew() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // The service is being created
        session = new SessionManager(this);
        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        refreshToken(session);
        initDataQueryParse(session.getValue(KEY_CUST_ID));
        startWASender(session.getValue(KEY_CUST_ID));
    }
    private void refreshToken(SessionManager session) {
        HashMap<String, String> userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        boolean is_child = Boolean.parseBoolean(userDetail.get(KEY_CHILD));
        is_parent = !is_child;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelName) {
        NotificationChannel chan = new NotificationChannel(ANDROID_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
    }

    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else {
            return false;
        }
    }
    private void startWASender(final String user_id) {
        trimCache(this);
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        final String created = df.format(c);

        String config_delay_bulk = sharePref.getSessionStr(DELAY_BULK_SENDER);
        if (config_delay_bulk.equals("")) {
            config_delay_bulk = "5";
        }
        long timerBulkSender = 3000;
        if (Integer.parseInt(config_delay_bulk) < 5) {
            config_delay_bulk = "5";
        }
        if (Integer.parseInt(config_delay_bulk) > 0) {
            timerBulkSender = Integer.parseInt(config_delay_bulk) * 1000;
        }
        Handler handlerWA = new Handler();
        handlerWA.postDelayed(new Runnable() {
            public void run() {
                boolean statusWASender = sharePref.getSessionBool(STATUS_BULK_SENDER);
                if (!is_send && statusWASender && is_parent && !is_synchronizing) {
                    Log.e("WhatsApp", "SENDING....");
                    dbHelper.insertLog(created, ID_SERVICE_WA, "Persiapan pengiriman Pesan", "normal", user_id);
                    sendWA(user_id);
                } else {
                    startWASender(user_id);
                }
            }
        }, timerBulkSender);
    }
    private String getTgl() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        return df.format(c);
    }
    private boolean isAccessibilityEnabled() {
        int enabled = 0;
        final String service = getPackageName() + "/" + WASendService.class.getCanonicalName();

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
    private List<String[]> getAntrianPesan() {
        Collections.sort(dataAntrianPesanFirebase, new Comparator<String[]>() {
            public int compare(String[] strings, String[] otherStrings) {
                return strings[5].compareTo(otherStrings[5]);
            }
        });
        return dataAntrianPesanFirebase;
    }
    interface updateParseInterface{
        void success();
    }
    private void updateParseOutboxMessage(final String idmessage, final String param, final String value, final updateParseInterface parseInterface) {
        Log.e(TAG,"idmessage:"+idmessage);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("OutboxMessage");
        query.whereEqualTo("idmessage",idmessage);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject entity, ParseException e) {
                if(e==null) {
                    entity.put(param, value);
                    entity.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            parseInterface.success();
                        }
                    });

                }
            }
        });
    }
    private void sendWA(final String user_id) {
        final String created = getTgl();
        if (!session.isLoggedIn()) {
            is_send = false;
            startWASender(user_id);
            return;
        }
        final List<String[]> antrianPesan = getAntrianPesan();
        Log.i(TAG, "countAntrian:" + antrianPesan.size());
        dbHelper.insertLog(created, ID_SERVICE_WA, "Sedang memeriksa antrian pesan", "normal", user_id);
        if (antrianPesan.size() > 0) {

            String prefTryagain = sharePref.getSessionStr(TRY_AGAIN_BULKSENDER);
            boolean status_prefTryagain = sharePref.getSessionBool(STATUS_ERROR_TRY_AGAIN);
            if (prefTryagain.equals("")) {
                prefTryagain = "5";
            } else if (Integer.parseInt(prefTryagain) < 5) {
                prefTryagain = "5";
            }
            Log.d(TAG,"INSTALLED OR NOT :"+sharePref.getSessionStr(SELECTED_WHATSAPP));
            if (cekWAInstalledOrNot(sharePref.getSessionStr(SELECTED_WHATSAPP))) {
                final String id = antrianPesan.get(0)[0];
                final String phoneNumber = antrianPesan.get(0)[1];
                final String bodyMessage = antrianPesan.get(0)[2];
                final String image_hash = antrianPesan.get(0)[3];
                final String image_url = antrianPesan.get(0)[4];
                String index_order = antrianPesan.get(0)[5];
                if (index_order.equals("")) {
                    updateParseOutboxMessage(id, "error_again", "1", new updateParseInterface() {
                        @Override
                        public void success() {
                            actionWaSender(created,phoneNumber,image_hash,image_url,user_id,bodyMessage,id);
                        }
                    });
                } else {
                    if (status_prefTryagain && (Integer.parseInt(index_order) >= Integer.parseInt(prefTryagain)) ) {
                        hapusPesan(new hapusDoneListener() {
                            @Override
                            public void done() {
                                is_send = false;
                                startWASender(user_id);
                            }
                        });
                        return;
                    }
                    int idxOrder=Integer.parseInt(index_order) + 1;
                    updateParseOutboxMessage(id, "error_again", String.valueOf(idxOrder), new updateParseInterface() {
                        @Override
                        public void success() {
                            actionWaSender(created,phoneNumber,image_hash,image_url,user_id,bodyMessage,id);
                        }
                    });
                }
            }else{
                is_send = false;
                startWASender(user_id);
            }
        } else {
            sharePref.createSession(STATUS_BULK_SENDING, false);
            dbHelper.insertLog(created, ID_SERVICE_WA, "Tidak ada antrian pesan", "normal", user_id);
            //dbHelper.updateDBVersion("0");
            Log.e(TAG, "tidak ada pesan antrian");
            WASendService.setID(null,null);
            is_send = false;
            startWASender(user_id);
        }
    }

    interface hapusDoneListener{
        void done();
    }
    private void hapusPesan(final hapusDoneListener doneListener) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONArray jsonArray = new JSONArray();

        final List<String[]> antrianPesan = getAntrianPesan();
        String prefTryagain = sharePref.getSessionStr(TRY_AGAIN_BULKSENDER);
        if (TextUtils.isEmpty(prefTryagain)) {
            prefTryagain = "5";
        }
        int maxTryAgain = Integer.parseInt(prefTryagain);
        if (antrianPesan.size() <= 0) {
            doneListener.done();
            return;
        }
        for (int i = 0; i < antrianPesan.size(); i++) {
            String id = antrianPesan.get(i)[0];
            String index_order = antrianPesan.get(i)[5];
            Log.e(TAG, "index:" + index_order);
            Log.e(TAG, "prefTryagain:" + prefTryagain);
            int tryAgain = Integer.parseInt(index_order);
            if (tryAgain >= maxTryAgain) {
                jsonArray.put(Integer.parseInt(id));
            }
        }
        if (jsonArray.length() <= 0) {
            doneListener.done();
            return;
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id", jsonArray);
        } catch (JSONException e) {
            doneListener.done();
            e.printStackTrace();
            return;
        }
        Log.i(TAG, request_body.toString());
        final String uri = Uri.parse(URL_POST_HAPUS_PESAN_ANTRIAN)
                .buildUpon()
                .toString();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    //final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG, message);
                    doneListener.done();
                } catch (JSONException e) {
                    e.printStackTrace();
                    doneListener.done();
                    new AlertDialog.Builder(getApplicationContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                doneListener.done();
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
    protected URL stringToURL(String urlString) {
        try {
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void actionWaSender(String created, String phoneNumber, String image_hash, String image_url,
                                final String user_id, String bodyMessage, final String id){
        dbHelper.insertLog(created, ID_SERVICE_WA, "Persiapan Kirim Pesan ke " + phoneNumber + ", isi Pesan : " + bodyMessage, "normal", user_id);
        Log.e(TAG, "ImageHash:" + image_hash);
        Log.e(TAG, "ImageHUrl:" + image_url);

        boolean accessibility = isAccessibilityEnabled();
        if (!accessibility) {
            dbHelper.insertLog(created, ID_SERVICE_WA, "Aksesibilitas Wabot tidak aktif", "warning", user_id);
            is_send = false;
            startWASender(user_id);
            return;
        }
        if ((image_hash.equals("")) && (image_url.equals(""))) {
            WASendService.setID(id,created);
            String toNumber = PhoneNumberUtils.stripSeparators(phoneNumber);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage(sharePref.getSessionStr(SELECTED_WHATSAPP));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("http://api.whatsapp.com/send?phone=" + toNumber + "&text=" + URLEncoder.encode(bodyMessage, "UTF-8")));
                startActivity(intent);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            startWASender(user_id);
            //kirim ke whatsapp
        } else if ((image_hash.equals(""))) {
            try {
                String sha1 = sha1(image_url) + ".jpg";
                final String path = getDirWabot("bulk") + "/" + sha1;
                if (fileExist(getApplicationContext(), path)) {
                    updateParseOutboxMessage(id, "image_hash", path, new updateParseInterface() {
                        @Override
                        public void success() {
                            for (int i = 0; i < dataAntrianPesanFirebase.size(); i++) {
                                String[] str = dataAntrianPesanFirebase.get(i);
                                if (str[i].equals(id)) {
                                    dataAntrianPesanFirebase.set(i, new String[]{id, str[1], str[2], path, str[4], str[5]});
                                }
                            }
                            is_send = false;
                            startWASender(user_id);
                        }
                    });
                } else {
                    ServiceSyncNew.DownloadTask task = new ServiceSyncNew.DownloadTask();
                    task.setParam(id, user_id, new DownLoadListener() {
                        @Override
                        public void done() {
                            is_send = false;
                            startWASender(user_id);
                        }
                    });
                    task.execute(stringToURL(image_url));
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                is_send = false;
                startWASender(user_id);
            }
            Log.e(TAG, "Download Image:");
        } else {
            WASendService.setID(id,created);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            File filePath = new File(image_hash);
            Uri uri = Uri.fromFile(filePath);
            Intent sendIntent = new Intent();
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("image");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setComponent(new ComponentName(sharePref.getSessionStr(SELECTED_WHATSAPP), sharePref.getSessionStr(SELECTED_WHATSAPP)+".ContactPicker"));
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(phoneNumber) + "@s.whatsapp.net");
            sendIntent.putExtra(Intent.EXTRA_TEXT, bodyMessage);
            startActivity(sendIntent);
            Log.e(TAG, "Send Image");
            //kirim ke whatsapp
            startWASender(user_id);
        }
    }

    interface DownLoadListener{
        void done();
    }
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<URL, Void, Bitmap> {
        String idPesan,user_id;
        DownLoadListener downLoadListener;
        URL url;
        public void setParam(String idm,String idu,DownLoadListener listener) {
           idPesan = idm;
           user_id= idu;
           downLoadListener=listener;
        }
        protected void onPreExecute() {
            String created = getTgl();
            dbHelper.lockOutboxById(idPesan);
            dbHelper.insertLog(created, ID_SERVICE_WA, "Sedang persiapan download gambar pesan..", "normal", user_id);
        }

        protected Bitmap doInBackground(URL... urls) {
            String created = getTgl();
            url = urls[0];
            URL url = urls[0];
            HttpURLConnection connection = null;

            try {
                // Initialize a new http url connection
                connection = (HttpURLConnection) url.openConnection();

                // Connect the http url connection
                connection.connect();

                // Get the input stream from http url connection
                dbHelper.insertLog(created, ID_SERVICE_WA, "Sedang download gambar pesan..", "normal", user_id);
                InputStream inputStream = connection.getInputStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                return BitmapFactory.decodeStream(bufferedInputStream);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Disconnect the http url connection
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        // When all async task done
        protected void onPostExecute(Bitmap result) {
            String created = getTgl();
            String image_url = url.toString();
            if (result != null) {
                dbHelper.insertLog(created, ID_SERVICE_WA, "berhail download gambar pesan", "success", user_id);
                try {
                    String sha1 = sha1(image_url) + ".jpg";
                    SaveImage(result, "bulk", sha1);
                    String path = getDirWabot("bulk") + "/" + sha1;

                    for (int i = 0; i < dataAntrianPesanFirebase.size(); i++) {
                        String[] str = dataAntrianPesanFirebase.get(i);
                        if (str[i].equals(idPesan)) {
                            dataAntrianPesanFirebase.set(i, new String[]{idPesan, str[1], str[2], path, str[4], str[5]});
                        }
                    }
                    updateParseOutboxMessage(idPesan, "image_hash", path, new updateParseInterface() {
                        @Override
                        public void success() {
                            downLoadListener.done();
                        }
                    });
                } catch (NoSuchAlgorithmException e) {
                    downLoadListener.done();
                }
            } else {
                downLoadListener.done();
                dbHelper.insertLog(created, ID_SERVICE_WA, "download gambar pesan gagal", "danger", user_id);
            }
        }
    }
    private boolean cekWAInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(SharPref.STATUS_FOREGROUND_SERVICE, false)) {
            String contentText = "Running service in foreground";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(contentText);
                Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(contentText)
                        .setAutoCancel(true);
                Notification notification = builder.build();
                startForeground(ANDROID_FOREGROUND_ID, notification);
            } else {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(contentText)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);
                Notification notification = builder.build();
                startForeground(ANDROID_FOREGROUND_ID, notification);
            }
        } else {
            stopForeground(true);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initDataQueryParse(final String user_id){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("OutboxMessage");
        query.whereEqualTo("KeyCust",user_id);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                    return;
                }
                for(ParseObject object:objects){
                    try {
                        String id = object.get("idmessage") + "";
                        String destination_number = object.get("destination_number") + "";
                        String message = object.get("message") + "";
                        String image_hash = object.get("image_hash") + "";
                        String image_url = object.get("image_url") + "";
                        String index_order = object.get("error_again") + "";
                        String sent = object.get("sent") + "";

                        String sha1 = sha1(image_url) + ".jpg";
                        String path = getDirWabot("bulk") + "/" + sha1;
                        if (fileExist(getApplicationContext(), path)) {
                            if (image_hash.equals("")) {
                                image_hash = path;
                            }
                        }
                        if (sent.equals("")) {
                            dataAntrianPesanFirebase.add(new String[]{id, destination_number, message, image_hash, image_url, index_order});
                        } else {
                            dataOutboxUploadPending.add(new String[]{id, sent});
                        }
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                liveQueryParse(user_id);
            }
        });
    }
    private void liveQueryParse(final String user_id){
        ParseLiveQueryClient parseLiveQueryClient = null;
        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://dash.wabot.id:1337"));
        } catch (URISyntaxException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("OutboxMessage");
        parseQuery.whereEqualTo("KeyCust", user_id);
        if(parseLiveQueryClient==null){
            return;
        }
        SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.e(TAG, "childAdd:" + object.get("KeyCust") + ", String : " + object.get("idmessage"));
                            //dbHelper = new DBHelper(getApplicationContext());
                            String id = object.get("idmessage")+"";
                            String destination_number = object.get("destination_number")+"";
                            String message = object.get("message")+"";
                            String image_hash = object.get("image_hash")+"";
                            String image_url = object.get("image_url")+"";
                            String index_order = object.get("error_again")+"";
                            String sent = object.get("sent")+"";

                            String sha1 = sha1(image_url) + ".jpg";
                            String path = getDirWabot("bulk") + "/" + sha1;
                            if (fileExist(getApplicationContext(), path)) {
                                if (image_hash.equals("")) {
                                    image_hash = path;
                                }
                            }
                            Log.e(TAG, "sent:" + sent + ",image_hash : " + image_hash);
                            if (sent.equals("")) {
                                dataAntrianPesanFirebase.add(new String[]{id, destination_number, message, image_hash, image_url, index_order});
                            } else {
                                dataOutboxUploadPending.add(new String[]{id, sent});
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.e(TAG, "childChanged:" + object.get("KeyCust") + ", String : " + object.get("idmessage"));
                            //dbHelper = new DBHelper(getApplicationContext());
                            String id = object.get("idmessage")+"";
                            String destination_number = object.get("destination_number")+"";
                            String message = object.get("message")+"";
                            String image_hash = object.get("image_hash")+"";
                            String image_url = object.get("image_url")+"";
                            String index_order = object.get("error_again")+"";
                            String sent = object.get("sent")+"";

                            String sha1 = sha1(image_url) + ".jpg";
                            String path = getDirWabot("bulk") + "/" + sha1;
                            if (fileExist(getApplicationContext(), path)) {
                                if (image_hash.equals("")) {
                                    image_hash = path;
                                }
                            }
                            Log.e(TAG, "sent:" + sent + ",image_hash : " + image_hash);
                            for (int i = 0; i < dataAntrianPesanFirebase.size(); i++) {
                                String[] str = dataAntrianPesanFirebase.get(i);
                                if (str[i].equals(id)) {
                                    if (sent.equals("")) {
                                        dataAntrianPesanFirebase.set(i, new String[]{id, destination_number, message, image_hash, image_url, index_order});
                                    } else {
                                        dataOutboxUploadPending.add(new String[]{id, sent});
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.DELETE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Log.e(TAG, "childRemoved:" + object.get("idmessage")+"");
                            String id = object.get("idmessage")+"";
                            deleteArrayListFirebaseAntrian(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private List<String[]> dataAntrianPesanFirebase = new ArrayList<>();
    private List<String[]> dataOutboxUploadPending = new ArrayList<>();
    private void deleteArrayListFirebaseAntrian(String id) {
        try {
            for (int i = 0; i < dataAntrianPesanFirebase.size(); i++) {
                String[] str = dataAntrianPesanFirebase.get(i);
                if (str[0].equals(id)) {
                    dataAntrianPesanFirebase.remove(i);
                    break;
                }
            }
            for (int i = 0; i < dataOutboxUploadPending.size(); i++) {
                String[] str = dataOutboxUploadPending.get(i);
                if (str[0].equals(id)) {
                    dataOutboxUploadPending.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
