package id.co.kamil.autochat.ui.kontak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterKontak;
import id.co.kamil.autochat.adapter.ItemKontak;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_CONTACT_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;

public class ExcludeContactActivity extends AppCompatActivity {

    private static final String TAG = "ExcludeContactActivity";
    private static final int REQUEST_ADD = 100;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private EditText edtCari;
    private SwipeRefreshLayout swipe_refresh;
    private ListView listKontak;
    private AdapterKontak kontakAdapter;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private boolean adapterInstance = false;
    private Menu menuTop;
    private JSONArray contact_id = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exclude_contact);
        getSupportActionBar().setTitle("Exclude Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            contact_id = new JSONArray(getIntent().getStringExtra("contact_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);

        layMessage = (LinearLayout) findViewById(R.id.layMessage);
        lblMessage = (TextView) findViewById(R.id.lblMessage);
        btnCobaLagi = (Button) findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadKontak();
            }
        });
        edtCari = (EditText) findViewById(R.id.edtCari);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        listKontak = (ListView) findViewById(R.id.listKontak);

        listKontak.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listKontak == null || listKontak.getChildCount() == 0) ?
                                0 : listKontak.getChildAt(0).getTop();
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
                    kontakAdapter.filter(edtCari.getText().toString().trim());
                    listKontak.invalidate();
                }catch (NullPointerException e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
    }
    private void loadKontak(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject parameter = new JSONObject();
        try {
            parameter.put("contact_id",contact_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_LIST_CONTACT_ID)
                .buildUpon()
                .toString();
        Log.i(TAG,"parameter:" + parameter);
        showError(false,"",true);
        swipe_refresh.setRefreshing(true);
        dataKontak.clear();
        //dataKontak.add(new ItemKontak("grupku","Grup Kontak","",false));
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
                            final String first_name = data.getJSONObject(i).getString("first_name");
                            final String last_name = data.getJSONObject(i).getString("last_name");
                            String name;
                            if (last_name.isEmpty() || last_name.equals(null) || last_name.equals("null") || last_name == null){
                                name = first_name;
                            }else{
                                name = first_name + " " + last_name;
                            }
                            final String phone = data.getJSONObject(i).getString("phone");
                            dataKontak.add(new ItemKontak(id,name,phone,false,data.getJSONObject(i)));
                        }
                        Log.i(TAG,"data:" + data.toString());
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
                if (response == null){
                    errorResponse(ExcludeContactActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(ExcludeContactActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(ExcludeContactActivity.this, LoginActivity.class));
                                                finish();
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
            listKontak.setVisibility(View.GONE);
            lblMessage.setText(message);
        }else{
            layMessage.setVisibility(View.GONE);
            listKontak.setVisibility(View.VISIBLE);
        }
        if (visibleButton){
            btnCobaLagi.setVisibility(View.VISIBLE);
        }else{
            btnCobaLagi.setVisibility(View.GONE);
        }
    }
    private void displayKontak() {
        kontakAdapter = new AdapterKontak(dataKontak,this);
        listKontak.setAdapter(kontakAdapter);
        adapterInstance = true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(false);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance){
            listDefault();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void listDefault(){
        for (int i = 0 ; i < dataKontak.size();i++){
            ItemKontak ikontak = dataKontak.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataKontak.set(i,ikontak);
        }
        kontakAdapter.notifyDataSetChanged();
        if (dataKontak.size()==0){
            loadKontak();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuTop = menu;
        getMenuInflater().inflate(R.menu.kontak, menuTop);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.actTambah){
            Intent i = new Intent(this, FormKontakActivity.class);
            i.putExtra("tipe","add");
            startActivityForResult(i,REQUEST_ADD);
        }else if (item.getItemId()==R.id.actEdit) {
            if (dataKontak.size()>0){
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0 ; i < dataKontak.size();i++){
                    ItemKontak ikontak = dataKontak.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataKontak.set(i,ikontak);
                }
                kontakAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(this, "Data Kontak tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }else if (item.getItemId()==R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(false);
            listDefault();
        }else if (item.getItemId()==R.id.actHapus) {
            new AlertDialog.Builder(this)
                    .setMessage("Apakah anda yakin akan menghapus item berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusKontak();
                        }
                    })
                    .setNegativeButton("Tidak",null)
                    .show();

        }else if (item.getItemId()==R.id.actSemua) {
            for (int i = 0 ; i < dataKontak.size();i++){
                ItemKontak ikontak = dataKontak.get(i);
                ikontak.setCheckbox(true);
                dataKontak.set(i,ikontak);
            }
            kontakAdapter.notifyDataSetChanged();
        }else if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hapusKontak() {
        for (int i = 0 ; i < dataKontak.size();i++){
            if (dataKontak.get(i).isCheckbox() ){
                dataKontak.remove(i);
                i = i - 1;
            }
        }
        kontakAdapter.notifyDataSetChanged();
        menuTop.findItem(R.id.actBatal).setVisible(false);
        menuTop.findItem(R.id.actHapus).setVisible(false);
        menuTop.findItem(R.id.actSemua).setVisible(false);
        menuTop.findItem(R.id.actEdit).setVisible(true);
        menuTop.findItem(R.id.actTambah).setVisible(false);
        listDefault();
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
        }
    }

}
