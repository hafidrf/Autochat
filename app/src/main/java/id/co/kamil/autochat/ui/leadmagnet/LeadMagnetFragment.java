package id.co.kamil.autochat.ui.leadmagnet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterLeadmagnet;
import id.co.kamil.autochat.adapter.ItemLeadmagnet;
import id.co.kamil.autochat.ui.shorten.ScanQrActivity;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.LIMIT_LEAD_MAGNET_BASIC;
import static id.co.kamil.autochat.utils.API.LIMIT_LEAD_MAGNET_PREMIUM;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_DIRECT_LINK_UPGRADE;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_LEADMAGNET;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_LEAD_MAGNET;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class LeadMagnetFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_ADD = 100;
    private static final String TAG = "LeadMagnet";
    private static final int PERMISSION_REQUEST_CAMERA = 102;
    private static final int REQUEST_QRCODE = 103;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token,type;
    private ProgressDialog pDialog;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private EditText edtCari;
    private SwipeRefreshLayout swipe_refresh;
    private ListView listLead;
    private List<ItemLeadmagnet> dataLead = new ArrayList<>();
    private AdapterLeadmagnet leadAdapter;
    private Menu menuTop;
    private boolean adapterInstance = false;
    private TextView labelStorage;
    private ProgressBar progressStorage;
    private SharPref sharePref;
    private int limit_lead_basic;
    private int limit_lead_premium;


    public LeadMagnetFragment() {
        // Required empty public constructor
    }

    public static LeadMagnetFragment newInstance(String param1, String param2) {
        LeadMagnetFragment fragment = new LeadMagnetFragment();
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
        View view = inflater.inflate(R.layout.fragment_lead_magnet, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        type = userDetail.get(KEY_CUST_GROUP);
        sharePref = new SharPref(getContext());

        limit_lead_basic = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_LEAD_MAGNET_BASIC));
        limit_lead_premium = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_LEAD_MAGNET_PREMIUM));
        if (limit_lead_basic<=0){
            limit_lead_basic = LIMIT_LEAD_MAGNET_BASIC;
        }
        if (limit_lead_premium<=0){
            limit_lead_premium = LIMIT_LEAD_MAGNET_PREMIUM;
        }

        pDialog = new ProgressDialog(getContext());
        labelStorage = (TextView) view.findViewById(R.id.labelStorage);
        progressStorage = (ProgressBar) view.findViewById(R.id.progressStorage);

        if (!(type.equals("6")  || type.equals(6))){ // selain bisnis
            progressStorage.setVisibility(View.VISIBLE);
            labelStorage.setVisibility(View.VISIBLE);
        }else{
            progressStorage.setVisibility(View.GONE);
            labelStorage.setVisibility(View.GONE);
        }

        layMessage = (LinearLayout) view.findViewById(R.id.layMessage);
        lblMessage = (TextView) view.findViewById(R.id.lblMessage);
        btnCobaLagi = (Button) view.findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadKontak();
            }
        });
        edtCari = (EditText) view.findViewById(R.id.edtCari);
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        listLead = (ListView) view.findViewById(R.id.listLead);
        listLead.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                String[] arr = {"Edit", "Buka link","Salin Link","Tampilkan QR-Code"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which){
                            case 0:
                                Intent intent = new Intent(getContext(), FormLeadMagnetActivity.class);
                                intent.putExtra("id", dataLead.get(i).getId());
                                intent.putExtra("tipe","edit");
                                intent.putExtra("data", dataLead.get(i).getJson().toString());
                                startActivityForResult(intent,REQUEST_ADD);
                                break;
                            case 1:
                                String url = dataLead.get(i).getDomain() + dataLead.get(i).getSub_domain();
                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                intent2.setData(Uri.parse(url));
                                startActivity(intent2);
                                break;
                            case 2:
                                setClipboard(getContext(),dataLead.get(i).getDomain() + dataLead.get(i).getSub_domain());
                                Toast.makeText(getContext(), "berhasil disalin", Toast.LENGTH_SHORT).show();
                                break;
                            case 3:

                                final LayoutInflater inflater = getLayoutInflater();
                                final View dialogLayout = inflater.inflate(R.layout.item_qrcode, null);
                                final String loc_qrcode = dataLead.get(i).getUrl_qrcode();
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) convertDpToPixel(200,getContext()));
                                ImageView img = (ImageView) dialogLayout.findViewById(R.id.imageView);

                                Picasso.with(getContext()).load(loc_qrcode).placeholder(R.drawable.ic_image).error(R.drawable.ic_image).into(img);


                                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                        .setView(dialogLayout)
                                        .setNeutralButton("Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i2) {
                                                String url = dataLead.get(i).getUrl_download();
                                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                                intent2.setData(Uri.parse(url));
                                                startActivity(intent2);
                                            }
                                        })
                                        .setPositiveButton("OK",null)
                                        .show();
                                break;
                        }
                    }
                });
                builder.create();
                builder.show();

            }
        });
        listLead.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listLead == null || listLead.getChildCount() == 0) ?
                                0 : listLead.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    leadAdapter.filter(edtCari.getText().toString().trim());
                    listLead.invalidate();
                }catch (NullPointerException e){
                    Log.i(TAG,e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        setHasOptionsMenu(true);
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadKontak();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadKontak();
            }
        });
        return view;
    }
    private void loadKontak(){
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_LEAD_MAGNET)
                .buildUpon()
                .toString();
        showError(false,"",true);
        swipe_refresh.setRefreshing(true);
        dataLead.clear();
        //dataLead.add(new ItemKontak("grupku","Grup Kontak","",false));
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0 ;i<data.length();i++){
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            final String field = data.getJSONObject(i).getString("field");
                            final String domain = data.getJSONObject(i).getString("domain");
                            final String sub_domain = data.getJSONObject(i).getString("sub_domain");
                            final String status_lm = data.getJSONObject(i).getString("status");
                            final String group_contact = data.getJSONObject(i).getString("group_contact");
                            final String respon = data.getJSONObject(i).getString("respon_msg");
                            final String klik = data.getJSONObject(i).getString("klik");
                            final String submit = data.getJSONObject(i).getString("submit");
                            final String url_qrcode = data.getJSONObject(i).getString("url_qrcode");
                            final String url_download = data.getJSONObject(i).getString("url_download");

                            dataLead.add(new ItemLeadmagnet(id,name,field,domain,sub_domain,respon,group_contact,status_lm,klik,submit,url_qrcode,url_download,data.getJSONObject(i),false,false));
                        }
                        //Log.i(TAG,"data:" + data.toString());
                    }else{
                        showError(true,message,false);
                    }
                    displayKontak();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(true,e.getMessage(),true);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response.statusCode==403){
                    try {
                        JSONObject jsonObject = new JSONObject(response.data.toString());
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")){
                            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                    .setMessage("Session telah habias / telah login di perangkat lain.")
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
                            showError(true,msg,true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{

                    final String msg = getResources().getString(errorResponse(error));
                    showError(true,msg,true);
                }

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("X-API-KEY",token);
                return header;
            }

        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void showError(boolean show,String message, boolean visibleButton){
        if (show){
            layMessage.setVisibility(View.VISIBLE);
            listLead.setVisibility(View.GONE);
            lblMessage.setText(message);
        }else{
            layMessage.setVisibility(View.GONE);
            listLead.setVisibility(View.VISIBLE);
        }
        if (visibleButton){
            btnCobaLagi.setVisibility(View.VISIBLE);
        }else{
            btnCobaLagi.setVisibility(View.GONE);
        }
    }
    private void displayKontak() {
        leadAdapter = new AdapterLeadmagnet(dataLead,getContext());
        listLead.setAdapter(leadAdapter);
        adapterInstance = true;
        if (type.equals("1") || type.equals(1)){
            labelStorage.setText("Penyimpanan (akun basic) : " + dataLead.size() + " s.d " + limit_lead_basic);
            if (dataLead.size()<limit_lead_basic){
                progressStorage.setProgress((dataLead.size()* 100 ) /limit_lead_basic);
            }else{
                progressStorage.setProgress(100);
            }
        }else if (!(type.equals("6") || type.equals(6))) {
            labelStorage.setText("Penyimpanan (akun premium) : " + dataLead.size() + " s.d " + limit_lead_premium);
            if (dataLead.size()<limit_lead_premium){
                progressStorage.setProgress((dataLead.size()* 100 ) /limit_lead_premium);
            }else{
                progressStorage.setProgress(100);
            }
        }


    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actImporKontak).setVisible(false);
        menu.findItem(R.id.actScan).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance){
            listDefault();
        }
    }
    private void listDefault(){
        for (int i = 0; i < dataLead.size(); i++){
            ItemLeadmagnet ikontak = dataLead.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataLead.set(i,ikontak);
        }
        leadAdapter.notifyDataSetChanged();
        if (dataLead.size()==0){
            loadKontak();
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menuTop = menu;
        inflater.inflate(R.menu.kontak, menuTop);

        super.onCreateOptionsMenu(menu, inflater);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.actTambah){
            if (type.equals("1") || type.equals(1)){ // basic
                if (dataLead.size()>=limit_lead_basic){
                    new AlertDialog.Builder(getContext())
                            .setMessage("Kuota Penyimpanan telah penuh, silahkan upgrade Akun Premium")
                            .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = URL_DIRECT_LINK_UPGRADE;
                                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                    intent2.setData(Uri.parse(url));
                                    startActivity(intent2);
                                }
                            })
                            .setNegativeButton("Batal",null)
                            .show();
                    return false;
                }
            }else if (!(type.equals("6") || type.equals(6))) { // basic
                if (dataLead.size() >= limit_lead_premium) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Kuota Penyimpanan telah penuh, silahkan upgrade Akun Premium")
                            .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = URL_DIRECT_LINK_UPGRADE;
                                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                    intent2.setData(Uri.parse(url));
                                    startActivity(intent2);
                                }
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                    return false;
                }
            }
            Intent i = new Intent(getContext(), FormLeadMagnetActivity.class);
            i.putExtra("tipe","add");
            startActivityForResult(i,REQUEST_ADD);
        }else if (item.getItemId()==R.id.actEdit) {
            if (dataLead.size()>0){
                menuTop.findItem(R.id.actImporKontak).setVisible(false);
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                menuTop.findItem(R.id.actScan).setVisible(false);
                for (int i = 0; i < dataLead.size(); i++){
                    ItemLeadmagnet ikontak = dataLead.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataLead.set(i,ikontak);
                }
                leadAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getContext(), "Data Kontak tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }else if (item.getItemId()==R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actImporKontak).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            menuTop.findItem(R.id.actScan).setVisible(true);
            listDefault();
        }else if (item.getItemId()==R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusKontak();
                        }
                    })
                    .setNegativeButton("Tidak",null)
                    .show();

        }else if (item.getItemId()==R.id.actSemua) {
            for (int i = 0; i < dataLead.size(); i++){
                ItemLeadmagnet ikontak = dataLead.get(i);
                ikontak.setCheckbox(true);
                dataLead.set(i,ikontak);
            }
            leadAdapter.notifyDataSetChanged();
        }else if(item.getItemId()==R.id.actScan){
            requestCameraPermission();
        }
        return super.onOptionsItemSelected(item);
    }
    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Toast.makeText(getContext(), "Camera access is required to display the camera preview.", Toast.LENGTH_SHORT).show();
            /*Snackbar.make(mLayout, "",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();*/
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            startActivityForResult(new Intent(getContext(), ScanQrActivity.class),REQUEST_QRCODE);
        }

    }
    private void hapusKontak() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dataLead.size(); i++){
            if (dataLead.get(i).isCheckbox()){
                jsonArray.put(Integer.parseInt(dataLead.get(i).getId()));
            }
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_HAPUS_LEADMAGNET)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        for (int i = 0; i < dataLead.size(); i++){
                            if (dataLead.get(i).isCheckbox() ){
                                dataLead.remove(i);
                                i = i - 1;
                            }
                        }
                        leadAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        menuTop.findItem(R.id.actImporKontak).setVisible(false);
                        menuTop.findItem(R.id.actScan).setVisible(true);
                        listDefault();
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }else{
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG,errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response!=null){
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
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
                                        .setCancelable(false)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(getContext())
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }else{
                    final String msg = getResources().getString(errorResponse(error));
                    new AlertDialog.Builder(getContext())
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                header.put("X-API-KEY",token);
                return header;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);

    }
    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_ADD){
            if (resultCode==RESULT_OK){
                loadKontak();
            }
        }else if(requestCode==REQUEST_QRCODE){
            if (resultCode==RESULT_OK){
                final String qrcode = data.getStringExtra("result");
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setMessage(qrcode)
                        .setPositiveButton("Buka Link", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String url = qrcode;
                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                intent2.setData(Uri.parse(url));
                                startActivity(intent2);
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton("Batal",null)
                        .show();
            }
        }
    }

}
