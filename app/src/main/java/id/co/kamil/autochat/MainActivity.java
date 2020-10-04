package id.co.kamil.autochat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.co.kamil.autochat.autoreply.MyNotifiService;
import id.co.kamil.autochat.firebase.app.Config;
import id.co.kamil.autochat.firebase.util.NotificationUtils;
import id.co.kamil.autochat.ui.AffiliasiActivity;
import id.co.kamil.autochat.ui.ApikeyActivity;
import id.co.kamil.autochat.ui.LogActivity;
import id.co.kamil.autochat.ui.PengaturanActivity;
import id.co.kamil.autochat.ui.followup.MainFollowupActivity;
import id.co.kamil.autochat.ui.linkpage.FormLinkPageActivity;
import id.co.kamil.autochat.ui.pesan.FormKirimPesanActivity;
import id.co.kamil.autochat.utils.PermissionManagement;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_DIRECT_LINK_UPGRADE;
import static id.co.kamil.autochat.utils.API.URL_POST_LOGOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_UPDATE_TOKEN_FIREBASE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CHILD;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_EMAIL;
import static id.co.kamil.autochat.utils.SessionManager.KEY_FIRSTNAME;
import static id.co.kamil.autochat.utils.SessionManager.KEY_LASTNAME;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.LINK_AKUN;
import static id.co.kamil.autochat.utils.SharPref.LINK_ECOURSE;
import static id.co.kamil.autochat.utils.SharPref.LINK_MARKETING_TOOL;
import static id.co.kamil.autochat.utils.SharPref.LINK_TIMWABOT;
import static id.co.kamil.autochat.utils.SharPref.LINK_TUTORIAL;
import static id.co.kamil.autochat.utils.SharPref.STATUS_FLOATING_WIDGET;
import static id.co.kamil.autochat.utils.SharPref.STATUS_FOREGROUND_SERVICE;
import static id.co.kamil.autochat.utils.SharPref.STATUS_SCREEN_ALWAYS_ON;
import static id.co.kamil.autochat.utils.Utils.errorResponse;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 11;
    private static final int REQUEST_ADD = 100;
    public static final String MAIN_RECEIVER = "MAIN_RECEIVER";
    private static final String TAG = "MainActivity";
    public static final String TAG_OPEN_MENU = "MainActivity_OpenMenu";

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private View navHeader;
    private TextView txtNama, txtEmail;
    private ImageView imgProfile;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private RelativeLayout mainLayout;
    private ProgressDialog pDialog;
    private String token;
    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private final String[][] kontakWabot = {
            {"083128302901", "WABOT"},
            {"085314855832", "CS WABOT 1 (Mita)"},
            {"085759730309", "CS WABOT 2 (Dela)"},
            {"081394565865", "CS WABOT 3 (Hendi)"},
    };
    private SharPref sharePref;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private BroadcastReceiver mainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            switch (action) {
                case "kontak":
                    navController.navigate(R.id.nav_kontak);
                    break;
                case "antrian":
                    navController.navigate(R.id.nav_antrian_pesan);
                    break;
                case "jadwal":
                    navController.navigate(R.id.nav_kirim_terjadwal);
                    break;
                case "terkirim":
                    navController.navigate(R.id.nav_pesan_terkirim);
                    break;
                case "enableAlwaysScreenOn":
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                case "disableAlwaysScreenOn":
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    break;
                case "enableForegroundService":
                    Intent intent1 = new Intent(MainActivity.this, ServiceSync.class);
                    intent1.putExtra(SharPref.STATUS_FOREGROUND_SERVICE, true);
                    startService(intent1);
                    break;
                case "disableForegroundService":
                    intent1 = new Intent(MainActivity.this, ServiceSync.class);
                    intent1.putExtra(SharPref.STATUS_FOREGROUND_SERVICE, false);
                    startService(intent1);
                    break;
            }
        }
    };
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        LocalBroadcastManager.getInstance(this).registerReceiver(mainReceiver, new IntentFilter(MAIN_RECEIVER));
        sharePref = new SharPref(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        pDialog = new ProgressDialog(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home, R.id.nav_kontak, R.id.nav_antrian_pesan, R.id.nav_linkpage, R.id.nav_template_share, R.id.nav_waform, R.id.nav_template, R.id.nav_template_dictionary,
                R.id.nav_wa_generator, R.id.nav_pesan_terkirim, R.id.nav_autoreply, R.id.nav_group, R.id.nav_group_autotext,
                R.id.nav_autotext, R.id.nav_kirim_terjadwal, R.id.nav_operator, R.id.nav_leadmagnet, R.id.nav_group_autoreply, R.id.nav_notification)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if ("true".equals(userDetail.get(KEY_CHILD))) {
            Menu menuNav = navigationView.getMenu();
            menuNav.findItem(R.id.nav_operator).setVisible(false);
            menuNav.findItem(R.id.nav_apikey).setVisible(false);
            //menuNav.findItem(R.id.nav_log).setVisible(false);
        }
//        if (userDetail.get(KEY_CUST_GROUP).equals(1) || userDetail.get(KEY_CUST_GROUP).equals("1")){
//            Menu menuNav = navigationView.getMenu();
//            //menuNav.findItem(R.id.nav_apikey).setVisible(false);
//        }
        if (userDetail.get(KEY_CUST_ID) != null) {
//            if (userDetail.get(KEY_CUST_ID).equals("5187")){
//                Menu menuNav = navigationView.getMenu();
//                menuNav.findItem(R.id.nav_notification).setVisible(true);
//            }else{
//                Menu menuNav = navigationView.getMenu();
//                menuNav.findItem(R.id.nav_notification).setVisible(false);
//            }
        } else {
            Menu menuNav = navigationView.getMenu();
            menuNav.findItem(R.id.nav_notification).setVisible(false);
        }
        navHeader = navigationView.getHeaderView(0);
        txtNama = (TextView) navHeader.findViewById(R.id.txtNama);
        txtEmail = (TextView) navHeader.findViewById(R.id.txtEmail);
        imgProfile = (ImageView) navHeader.findViewById(R.id.imgProfile);

        txtNama.setText(String.format(Locale.getDefault(), "%s %s",
                userDetail.get(KEY_FIRSTNAME),
                userDetail.get(KEY_LASTNAME)
        ));
        txtEmail.setText(userDetail.get(KEY_EMAIL));

        loadService();

        checkContactWabot();

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (Config.REGISTRATION_COMPLETE.equals(intent.getAction())) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (Config.PUSH_NOTIFICATION.equals(intent.getAction())) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    //txtMessage.setText(message);
                }
            }
        };

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        InstanceIdResult result = task.getResult();
                        if (result == null) return;
                        String token = result.getToken();
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("regId", token);
                        editor.apply();
                        displayFirebaseRegId();
                    }
                });
        final String intentFragment = getIntent().getStringExtra("fragment");
        if (intentFragment != null) {
            switch (intentFragment) {
                case "kontak":
                    navController.navigate(R.id.nav_kontak);
                    break;
                case "broadcast":
                    MenuItem menuNav = navigationView.getMenu().findItem(R.id.nav_kirim_pesan);
                    showKirimPesan(menuNav);
                    break;
                case "autotext":
                    navController.navigate(R.id.nav_autotext);
                    break;
                case "templatepromosi":
                    navController.navigate(R.id.nav_template);
                    break;
            }
        }

        boolean status_floating_widget = sharePref.getSessionBool(STATUS_FLOATING_WIDGET);
        if (!PermissionManagement.getInstance().needAccessToOverDrawApps(this) && status_floating_widget) {
            startService(new Intent(this, FloatingViewService.class));
        }

        boolean screen_always_on = sharePref.getSessionBool(STATUS_SCREEN_ALWAYS_ON);
        if (screen_always_on) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        Intent pushNotification = getIntent().getParcelableExtra(MainActivity.TAG_OPEN_MENU);
        if (pushNotification != null) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
        }
    }

    private void loadService() {
        try {
            Intent intent = new Intent(MainActivity.this, MyNotifiService.class);//启动服务
            startService(intent);//Start service

            Intent intent1 = new Intent(MainActivity.this, ServiceSync.class);
            boolean foreground_service = sharePref.getSessionBool(STATUS_FOREGROUND_SERVICE);
            intent1.putExtra(SharPref.STATUS_FOREGROUND_SERVICE, foreground_service);
            startService(intent1);
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage("Gagal Load Service, coba lagi?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            loadService();
                        }
                    })
                    .setNegativeButton("Keluar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

    }

    public boolean contactExists(Context context, String number) {
/// number is the phone number
        String selection = String.format("%s > 0", ContactsContract.Contacts.HAS_PHONE_NUMBER);
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, selection, null, null);
        if (cur == null) return false;

        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            cur.close();
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
            try {
                List<Integer> indexKontak = new ArrayList<>();
                for (int x = 0; x < kontakWabot.length; x++) {
                    if (contactExists(this, kontakWabot[x][0])) {
                        indexKontak.add(x);
                    }
                }
                if (indexKontak.size() != kontakWabot.length) {
                    boolean add;
                    for (int a = 0; a < kontakWabot.length; a++) {
                        add = true;
                        for (int x = 0; x < indexKontak.size(); x++) {
                            if (a == indexKontak.get(x)) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            saveLocalContact(kontakWabot[a][1], kontakWabot[a][0]);
                            Log.i(TAG, "Add Kontak : " + kontakWabot[a][1]);
                        }
                    }
                }
            } catch (Exception ignored) {

            }
        } else {
            cekPermission();
        }
    }

    private void cekPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    private void saveLocalContact(String nama, String nomor) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, nama) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, nomor) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException | OperationApplicationException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void doLogout(MenuItem item) {
        new AlertDialog.Builder(this)
                .setMessage("Apakah anda yakin akan keluar dari Aplikasi ?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        logout_service();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();


    }

    public void doMarketingTool(MenuItem item) {
        String url = sharePref.getSessionStr(LINK_MARKETING_TOOL);
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setData(Uri.parse(url));
        startActivity(intent2);
    }

    public void doAkun(MenuItem item) {
        String url = sharePref.getSessionStr(LINK_AKUN);
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setData(Uri.parse(url));
        startActivity(intent2);
    }

    public void doFollowup(MenuItem item) {
        startActivity(new Intent(this, MainFollowupActivity.class));
    }

    public void doTutorial(MenuItem item) {
        String url = sharePref.getSessionStr(LINK_TUTORIAL);
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setData(Uri.parse(url));
        startActivity(intent2);
    }

    public void doHelp(MenuItem item) {
        String url = sharePref.getSessionStr(LINK_TIMWABOT);
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setData(Uri.parse(url));
        startActivity(intent2);
    }

    public void doEcourse(MenuItem item) {
        String url = sharePref.getSessionStr(LINK_ECOURSE);
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setData(Uri.parse(url));
        startActivity(intent2);
    }

    public void doApikey(MenuItem item) {
        startActivity(new Intent(this, ApikeyActivity.class));
    }

    public void doAffiliasi(MenuItem item) {
        startActivity(new Intent(this, AffiliasiActivity.class));
    }

    public void doUpgrade(MenuItem item) {
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        intent2.setData(Uri.parse(URL_DIRECT_LINK_UPGRADE));
        startActivity(intent2);
    }

    private void logout_service() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LOGOUT)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                requestQueue.stop();
            }
        });
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                session.clearData();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                session.clearData();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .show();

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

    private void hidePdialog() {
        if (pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    public void doLog(MenuItem item) {
        Intent intent = new Intent(this, LogActivity.class);
        startActivity(intent);
    }

    public void doSetting(MenuItem item) {
        Intent intent = new Intent(this, PengaturanActivity.class);
        startActivity(intent);
    }

    public void doLinkpage(MenuItem item) {
        Intent intent = new Intent(this, FormLinkPageActivity.class);
        startActivity(intent);
    }

    public void showKirimPesan(MenuItem item) {
        String[] arr = {"Kirim per Grup Kontak", "Kirim per Kontak"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setItems(arr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(MainActivity.this, FormKirimPesanActivity.class);
                        intent.putExtra("tipe", "grup");
                        startActivityForResult(intent, REQUEST_ADD);
                        break;
                    case 1:
                        Intent intent2 = new Intent(MainActivity.this, FormKirimPesanActivity.class);
                        intent2.putExtra("tipe", "kontak");
                        startActivityForResult(intent2, REQUEST_ADD);
                        break;
                }
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PermissionManagement.CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) {
                boolean status_floating_widget = sharePref.getSessionBool(STATUS_FLOATING_WIDGET);
                if (status_floating_widget) {
                    startService(new Intent(this, FloatingViewService.class));
                }
            } else { //Permission is not available
                Toast.makeText(this,
                        "Draw over other app permission not available.",
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
        drawer.closeDrawers();
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);
        final String token_id = userDetail.get(KEY_TOKEN);

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("token", regId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_UPDATE_TOKEN_FIREBASE)
                .buildUpon()
                .toString();

        Log.i(TAG, "body:" + requestBody);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG, "updateToken:" + message);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "updateToken:" + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorResponse(getApplicationContext(), error);

            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key", token_id);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
//        if (!TextUtils.isEmpty(regId))
//            txtRegId.setText("Firebase Reg Id: " + regId);
//        else
//            txtRegId.setText("Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }
}
