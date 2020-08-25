package id.co.kamil.autochat.ui.pesan;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterAutoReply;
import id.co.kamil.autochat.adapter.ItemAutoReply;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static id.co.kamil.autochat.utils.API.LIMIT_AUTO_REPLY;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_DIRECT_LINK_UPGRADE;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_AUTO_REPLY;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_AUTO_REPLY;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class AutoReplyFragment extends Fragment {
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "AutoReplyFragment";
    private static final int REQUEST_ADD = 100;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private EditText edtCari;

    private List<ItemAutoReply> dataAutoReply = new ArrayList<>();

    private AdapterAutoReply pesanAdapter;
    private ListView listPesan;
    private Menu menuTop;
    private boolean adapterInstance = false;
    private Button btnPengaturan;
    private String type;
    private TextView labelStorage;
    private ProgressBar progressStorage;
    private SharPref sharePref;
    private int limit_auto_reply;
    private DBHelper dbHelper;

    public AutoReplyFragment() {
        // Required empty public constructor
    }

    public static AutoReplyFragment newInstance(String param1, String param2) {
        AutoReplyFragment fragment = new AutoReplyFragment();
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
        View view = inflater.inflate(R.layout.fragment_auto_reply, container, false);

        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        type = userDetail.get(KEY_CUST_GROUP);
        sharePref = new SharPref(getContext());
        dbHelper = new DBHelper(getContext());

        limit_auto_reply = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_AUTO_REPLY));
        if (limit_auto_reply<=0){
            limit_auto_reply = LIMIT_AUTO_REPLY;
        }

        pDialog = new ProgressDialog(getContext());
        labelStorage = (TextView) view.findViewById(R.id.labelStorage);
        progressStorage = (ProgressBar) view.findViewById(R.id.progressStorage);

        if (type.equals("1")){ // basic
            progressStorage.setVisibility(View.VISIBLE);
            labelStorage.setVisibility(View.VISIBLE);
        }else{
            progressStorage.setVisibility(View.GONE);
            labelStorage.setVisibility(View.GONE);
        }


        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        layMessage = (LinearLayout) view.findViewById(R.id.layMessage);
        lblMessage = (TextView) view.findViewById(R.id.lblMessage);
        btnCobaLagi = (Button) view.findViewById(R.id.btnCobaLagi);
        btnPengaturan = (Button) view.findViewById(R.id.btnSetting);
        btnPengaturan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData();
            }
        });

        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listPesan = (ListView) view.findViewById(R.id.listKontak);
        listPesan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(),FormAutoReplyActivity.class);
                intent.putExtra("tipe","edit");
                intent.putExtra("id",dataAutoReply.get(i).getId());
                startActivityForResult(intent,REQUEST_ADD);
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    pesanAdapter.filter(edtCari.getText().toString().trim());
                    listPesan.invalidate();
                }catch (NullPointerException e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listPesan.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listPesan== null || listPesan.getChildCount() == 0) ?
                                0 : listPesan.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        setHasOptionsMenu(true);
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadData();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //boolean permission = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();

            View v = (LinearLayoutCompat) view.findViewById(R.id.layMaster);
            //showSnackBar(v);
        }
        return view;
    }
    public void showSnackBar(View llShow)
    {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Snackbar snackbar = Snackbar.make(llShow, "", Snackbar.LENGTH_INDEFINITE);
        // Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(0,0,0,0);
        // Hide the text
        TextView textView = (TextView) layout.findViewById(R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate our custom view
        View snackView = getLayoutInflater().inflate(R.layout.my_snackbar, null);
        // Configure the view
        TextView textViewOne = (TextView) snackView.findViewById(R.id.txtOne);
        TextView textMessage = (TextView) snackView.findViewById(R.id.txtMessage);
        textMessage.setText(getString(R.string.warning_disable_accessibility));
        textViewOne.setText(getString(R.string.snack_enable_service));
        textViewOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(i);
            }
        });

        ImageView imgClose = (ImageView) snackView.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        // Add the view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_ADD){
            if (resultCode==RESULT_OK){
                loadData();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menuTop = menu;
        inflater.inflate(R.menu.kontak, menuTop);
        super.onCreateOptionsMenu(menu, inflater);

    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance){
            listDefault();
        }
    }
    private void listDefault(){
        for (int i = 0; i < dataAutoReply.size(); i++){
            ItemAutoReply ikontak = dataAutoReply.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataAutoReply.set(i,ikontak);
        }
        pesanAdapter.notifyDataSetChanged();
        if (dataAutoReply.size()==0){
            loadData();
        }
    }
    private void loadData(){
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("status","pending");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_AUTO_REPLY)
                .buildUpon()
                .toString();
        showError(false,"",true);
        swipe_refresh.setRefreshing(true);
        dataAutoReply.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
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
                            final String created_at = data.getJSONObject(i).getString("created");
                            final JSONArray keyword = data.getJSONObject(i).getJSONArray("keyword");
                            final String reply = data.getJSONObject(i).getString("reply");
                            final String status_pesan = data.getJSONObject(i).getString("status");
                            dataAutoReply.add(new ItemAutoReply(id,created_at,keyword.toString(),reply,status_pesan,false,false));
                        }
                    }else{
                        Log.i(TAG,response.toString());
                        showError(true,message,false);
                    }
                    displayPesan();
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
                if (response == null){
                    errorResponse(getContext(),error);
                }else{
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
    private void showError(boolean show,String message, boolean visibleButton){
        if (show){
            layMessage.setVisibility(View.VISIBLE);
            listPesan.setVisibility(View.GONE);
            lblMessage.setText(message);
        }else{
            layMessage.setVisibility(View.GONE);
            listPesan.setVisibility(View.VISIBLE);
        }
        if (visibleButton){
            btnCobaLagi.setVisibility(View.VISIBLE);
        }else{
            btnCobaLagi.setVisibility(View.GONE);
        }
    }
    private void displayPesan() {
        pesanAdapter = new AdapterAutoReply(dataAutoReply,getContext());
        listPesan.setAdapter(pesanAdapter);
        adapterInstance = true;
        labelStorage.setText("Penyimpanan (Akun Basic) : " + dataAutoReply.size() + " s.d " + limit_auto_reply);
        if (dataAutoReply.size()<limit_auto_reply){
            progressStorage.setProgress((dataAutoReply.size() * 100) / limit_auto_reply);
        }else{
            progressStorage.setProgress(100);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.actTambah){
            if (type.equals("1")){ // basic
                if (dataAutoReply.size()>=limit_auto_reply){
                    new android.app.AlertDialog.Builder(getContext())
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
            }
            Intent intent = new Intent(getContext(),FormAutoReplyActivity.class);
            intent.putExtra("tipe","tambah");
            startActivityForResult(intent,REQUEST_ADD);
        }else if (item.getItemId()==R.id.actEdit) {
            if (dataAutoReply.size()>0){
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataAutoReply.size(); i++){
                    ItemAutoReply ikontak = dataAutoReply.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataAutoReply.set(i,ikontak);
                }
                pesanAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(getContext(), "Data Autoreply tidak tersedia", Toast.LENGTH_SHORT).show();
            }

        }else if (item.getItemId()==R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            listDefault();
        }else if (item.getItemId()==R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusPesan();
                        }
                    })
                    .setNegativeButton("Tidak",null)
                    .show();

        }else if (item.getItemId()==R.id.actSemua) {
            for (int i = 0; i < dataAutoReply.size(); i++){
                ItemAutoReply ikontak = dataAutoReply.get(i);
                ikontak.setCheckbox(true);
                dataAutoReply.set(i,ikontak);
            }
            pesanAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
    private void hapusPesan() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dataAutoReply.size(); i++){
            if (dataAutoReply.get(i).isCheckbox() ){
                jsonArray.put(Integer.parseInt(dataAutoReply.get(i).getId()));
            }
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,request_body.toString());
        final String uri = Uri.parse(URL_POST_HAPUS_AUTO_REPLY)
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
                        for (int i = 0; i < dataAutoReply.size(); i++){
                            if (dataAutoReply.get(i).isCheckbox()){
                                dataAutoReply.remove(i);
                                i = i - 1;
                            }
                        }
                        pesanAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
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
                if (response == null){
                    errorResponse(getContext(),error);
                }else{
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
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(getContext())
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
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

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}
