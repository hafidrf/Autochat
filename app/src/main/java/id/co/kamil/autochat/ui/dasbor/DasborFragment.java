package id.co.kamil.autochat.ui.dasbor;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import id.co.kamil.autochat.FloatingViewService;
import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterDashboard;
import id.co.kamil.autochat.adapter.ItemDashboard;
import id.co.kamil.autochat.bulksender.WASendService;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.ui.AffiliasiActivity;
import id.co.kamil.autochat.ui.PengaturanActivity;
import id.co.kamil.autochat.utils.ExpandableHeightListView;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.MainActivity.MAIN_RECEIVER;
import static id.co.kamil.autochat.utils.API.DESKRIPSI_INFO;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_DIRECT_LINK_UPGRADE;
import static id.co.kamil.autochat.utils.API.URL_LANDING_PAGE;
import static id.co.kamil.autochat.utils.API.URL_POST_DASHBOARD;
import static id.co.kamil.autochat.utils.API.URL_SYNC_DB2;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_PARENT_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_FLOATING_WIDGET;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.convertPixelsToDp;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class DasborFragment extends Fragment  implements  ViewTreeObserver.OnScrollChangedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "DasborFragment";

    private String _URL_UPGRADE = "";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView txtInfo;
    private ExpandableHeightListView listDashboard;
    private SwipeRefreshLayout swipe_refresh;
    private ScrollView scrollView;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private TextView txtTypeAccount;
    private List<ItemDashboard> dataDashboard = new ArrayList<>();
    private TextView txtJoin;
    private Button btnShare;
    private Button btnAffiliasi;
    private String linkPlaystore,linkWeb;
    private String template_share;
    private Button btnUpgrade;
    private ImageView imgPrice;
    private Button btnPengaturan;
    private Button btnSingkronisasi;
    private DBHelper dbHelper;
    private SharPref sharePref;
    private int dbVersionCode;
    private ProgressDialog pDialog;
    private int page_kontak_wabot = 0;
    private Switch switchEnabledBulkSender,switchAksesibilitas, switchFloatingWidget;
    private boolean status_aksesibilitas;


    public DasborFragment() {
        // Required empty public constructor
    }

    public static DasborFragment newInstance(String param1, String param2) {
        DasborFragment fragment = new DasborFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dasbor, container, false);
        listDashboard = (ExpandableHeightListView) view.findViewById(R.id.listDashboard);
        switchEnabledBulkSender = (Switch) view.findViewById(R.id.switchEnabledBulkSender);
        switchAksesibilitas = (Switch) view.findViewById(R.id.switchAksesibilitas);
        switchFloatingWidget = (Switch) view.findViewById(R.id.switchFloatingWidget);
        txtInfo = (TextView) view.findViewById(R.id.txtInfo);
        txtJoin = (TextView) view.findViewById(R.id.txtJoin);
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        txtTypeAccount = (TextView) view.findViewById(R.id.txtTypeAccount);
        imgPrice = (ImageView) view.findViewById(R.id.imgPrice);
        btnUpgrade = (Button) view.findViewById(R.id.btnUpgrade);
        btnPengaturan = (Button) view.findViewById(R.id.btnPengaturan);
        btnSingkronisasi = (Button) view.findViewById(R.id.btnSingkronisasi);
        btnPengaturan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), PengaturanActivity.class));
            }
        });
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = _URL_UPGRADE;
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(url));
                startActivity(intent2);
            }
        });
        btnShare = (Button) view.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String konten = template_share;
                    konten = konten.replace("[linklanding]",linkPlaystore);
                    konten = konten.replace("[linkweb]",linkWeb);

                    String appId = getActivity().getPackageName();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = konten;
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Bagikan lewat"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });
        btnAffiliasi = (Button) view.findViewById(R.id.btnAffiliasi);
        btnAffiliasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AffiliasiActivity.class));
            }
        });
        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        sharePref = new SharPref(getContext());

        updateStatusBulkSender();
        switchEnabledBulkSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(STATUS_BULK_SENDER,isChecked);
            }
        });
        switchAksesibilitas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchAksesibilitas.setChecked(status_aksesibilitas);
                Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(i);
            }
        });
        switchFloatingWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(STATUS_FLOATING_WIDGET,isChecked);

                if (getActivity() != null) {
                    if (isChecked) {
                        getActivity().startService(new Intent(getContext(), FloatingViewService.class));
                    } else {
                        getActivity().stopService(new Intent(getContext(), FloatingViewService.class));
                    }
                }
            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadDashboard();
            }
        });
        btnSingkronisasi.setVisibility(View.GONE);
        if (userDetail.get(KEY_PARENT_ID) != null){
            if (userDetail.get(KEY_PARENT_ID).equals("5187")){
                btnSingkronisasi.setVisibility(View.VISIBLE);
            }
        }
        if (userDetail.get(KEY_CUST_ID) != null){
            if (userDetail.get(KEY_CUST_ID).equals("5187")){
                btnSingkronisasi.setVisibility(View.VISIBLE);
            }
        }
        Log.e(TAG,"ParentID" + userDetail.get(KEY_PARENT_ID));
        btnSingkronisasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singkronisasiWabot();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDashboard();
            }
        });
        setTextViewHTML(txtInfo,DESKRIPSI_INFO);
        listDashboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent pushNotification = new Intent(MAIN_RECEIVER);
                if (position==0){
                    pushNotification.putExtra("action", "kontak");
                }else if(position==1){
                    pushNotification.putExtra("action", "antrian");
                }else if(position==2){
                    pushNotification.putExtra("action", "jadwal");
                }else if(position==3){
                    pushNotification.putExtra("action", "terkirim");

                }
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(pushNotification);
            }
        });

        return view;
    }
    private void updateStatusBulkSender() {
        boolean status_bulk_sender = sharePref.getSessionBool(STATUS_BULK_SENDER);
        status_aksesibilitas = isAccessibilityEnabled();
        boolean status_floating_widget = sharePref.getSessionBool(STATUS_FLOATING_WIDGET);

        switchAksesibilitas.setChecked(status_aksesibilitas);
        switchEnabledBulkSender.setChecked(status_bulk_sender);
        switchFloatingWidget.setChecked(status_floating_widget);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStatusBulkSender();
    }

    private boolean isAccessibilityEnabled() {
        int enabled = 0;
        final String service = getActivity().getPackageName() +"/"+ WASendService.class.getCanonicalName();

        try {
            enabled = Settings.Secure.getInt(getContext().getContentResolver()
                    , Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        if (enabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getContext().getContentResolver()
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
    private void singkronisasiWabot(){
        final String fieldverdb = "ver_db_kontakwabot";
        final String created = getTgl();
        dbHelper = new DBHelper(getContext());
        sharePref = new SharPref(getContext());
        page_kontak_wabot = sharePref.getSessionInt("page_kontak_wabot");
        if (page_kontak_wabot <= 0 ){
            page_kontak_wabot = 1;
        }
        dbVersionCode = dbHelper.getVersionCodeDB2(fieldverdb);
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());

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

        Log.i(TAG,"body:" + requestBody);
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Singkronisasi kontak part " + page_kontak_wabot);
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    //Log.i(TAG,"response:" + response);
                    if (status){
                        final boolean uptodate = response.getBoolean("is_uptodate");
                        final String versionCode = response.getString("version_code");
                        if(!uptodate){
                            final JSONArray data = response.getJSONArray("data");

                            if (data.length()>0){
                                for (int i = 0;i<data.length();i++){
                                    String name = data.getJSONObject(i).getString("name");
                                    String phone = data.getJSONObject(i).getString("phone");
                                    if (!contactExists(getContext(),phone)){
                                        saveLocalContact(name,phone);
                                    }
                                }
                                hidePdialog();
                                sharePref.createSession("page_kontak_wabot",page_kontak_wabot + 1);
                                new AlertDialog.Builder(getContext())
                                        .setMessage("singkronisasi selesai")
                                        .setPositiveButton("OK",null)
                                        .setCancelable(false)
                                        .show();

                            }else{
                                hidePdialog();
                                new AlertDialog.Builder(getContext())
                                        .setMessage("database up to date")
                                        .setPositiveButton("OK",null)
                                        .setCancelable(false)
                                        .show();
                                dbHelper.updateDBVersion2(versionCode,fieldverdb);
                            }
                        }else{
                            hidePdialog();
                            new AlertDialog.Builder(getContext())
                                    .setMessage("database up to date")
                                    .setPositiveButton("OK",null)
                                    .setCancelable(false)
                                    .show();
                            dbHelper.updateDBVersion2(versionCode,fieldverdb);
                        }

                    }else{
                        hidePdialog();
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .setCancelable(false)
                                .show();
                    }

                } catch (JSONException e) {
                    hidePdialog();
                    e.printStackTrace();
                    Log.i(TAG,e.getMessage());

                    new AlertDialog.Builder(getContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .setCancelable(false)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG,errorResponseString(error));
                new AlertDialog.Builder(getContext())
                        .setMessage(errorResponseString(error))
                        .setPositiveButton("OK",null)
                        .setCancelable(false)
                        .show();
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

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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
            ContentProviderResult[] res = getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (RemoteException e)
        {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (OperationApplicationException e)
        {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void loadDashboard() {
        txtJoin.setVisibility(View.GONE);
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        final String uri = Uri.parse(URL_POST_DASHBOARD)
                .buildUpon()
                .toString();
        swipe_refresh.setRefreshing(true);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {

                        final String info = response.getString("info");
                        final String join = response.getString("join");
                        final String join_color = response.getString("join_color");
                        final String account_type_id = response.getString("account_type_id");
                        final String account_type = response.getString("account_type");
                        final String count_contact = response.getString("count_contact");
                        final String message_pending = response.getString("message_pending");
                        final String schedule_active = response.getString("schedule_active");
                        final String message_sent = response.getString("message_sent");
                        final String img_price = response.getString("img_price");
                        final String url_upgrade = response.getString("url_upgrade");
                        final String status_account = response.getString("status_account");
                        if (img_price == null || img_price.equals(null) || img_price.equals("null")) {
                            imgPrice.setVisibility(View.GONE);
                        } else {
                            Picasso.with(getContext()).load(img_price).placeholder(R.drawable.ic_image).error(R.drawable.ic_image).into(imgPrice);
                            imgPrice.setVisibility(View.VISIBLE);
                        }
                        if (url_upgrade == null || url_upgrade.equals(null) || url_upgrade.equals("null")) {
                            btnUpgrade.setVisibility(View.GONE);
                        } else {
                            _URL_UPGRADE = url_upgrade;
                            btnUpgrade.setVisibility(View.VISIBLE);
                        }
                        template_share = response.getString("template_share");
                        linkPlaystore = response.getString("link_playstore");
                        linkWeb = response.getString("link_web");
                        if (!(info.equals(null) || info.equals("null") || info == null)) {
                            setTextViewHTML(txtInfo, info);
                        }
                        if (!(join.equals(null) || join.equals("null") || join == null)) {
                            txtJoin.setVisibility(View.VISIBLE);
                            setTextViewHTML(txtJoin, join);
                            if (join_color.equals("red")) {
                                txtJoin.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_red));
                            } else if (join_color.equals("green")) {
                                txtJoin.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_green));
                            } else if (join_color.equals("orange")) {
                                txtJoin.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_orange));
                            } else if (join_color.equals("blue")) {
                                txtJoin.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_blue));
                            } else {
                                txtJoin.setBackground(getContext().getResources().getDrawable(R.drawable.rectangle_tosca));
                            }
                            int pad = (int) convertDpToPixel(10, getContext());
                            txtJoin.setPadding(pad, pad, pad, pad);
                        }
                        session.setKeyCustGroup(account_type_id);
                        if (account_type_id.equals("1")) {
                            if (status_account.equals(null) || status_account == null || status_account.equals("null")) {
                                txtTypeAccount.setVisibility(View.GONE);
                            } else {
                                setTextViewHTML(txtTypeAccount, status_account);
                            }
                        } else {
                            txtTypeAccount.setText("Type akun Anda Saat ini adalah: " + account_type);
                        }

                        dataDashboard.clear();
                        dataDashboard.add(new ItemDashboard("Jumlah Kontak", count_contact, "blue"));
                        dataDashboard.add(new ItemDashboard("Jumlah Pesan Pending", message_pending, "tosca"));
                        dataDashboard.add(new ItemDashboard("Jumlah Schedule Message Aktif", schedule_active, "orange"));
                        dataDashboard.add(new ItemDashboard("Jumlah Pesan Terkirim", message_sent, "green"));


                    } else {
                        txtTypeAccount.setText("Type Account : -");
                        dataDashboard.clear();
                        dataDashboard.add(new ItemDashboard("Jumlah Kontak", "0", "blue"));
                        dataDashboard.add(new ItemDashboard("Jumlah Pesan Pending", "0", "tosca"));
                        dataDashboard.add(new ItemDashboard("Jumlah Schedule Message Aktif", "0", "orange"));
                        dataDashboard.add(new ItemDashboard("Jumlah Pesan Terkirim", "0", "green"));

                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    displayList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(getContext(),error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            }else{

                                new AlertDialog.Builder(getContext())
                                        .setMessage(msg)
                                        .setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                loadDashboard();
                                            }
                                        })
                                        .setNegativeButton("Batal",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{
                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(getContext())
                                .setMessage(msg)
                                .setPositiveButton("Coba Lagi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadDashboard();
                                    }
                                })
                                .setNegativeButton("Batal",null)
                                .show();
                    }
                }

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
    private void displayList() {
        final AdapterDashboard adapterDashboard = new AdapterDashboard(getContext(),R.layout.item_list_dashboard,dataDashboard);
        listDashboard.setAdapter(adapterDashboard);
        listDashboard.setExpanded(true);
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        final ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                String url = span.getURL();
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(url));
                startActivity(intent2);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    protected void setTextViewHTML(TextView text, String html)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onStart() {
        super.onStart();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(this);
    }
    @Override
    public void onScrollChanged() {
        int scrollY = scrollView.getScrollY();
        if (scrollY==0){
            swipe_refresh.setEnabled(true);
        }else {
            if (!swipe_refresh.isRefreshing()){
                swipe_refresh.setEnabled(false);
            }
        }
    }
}
