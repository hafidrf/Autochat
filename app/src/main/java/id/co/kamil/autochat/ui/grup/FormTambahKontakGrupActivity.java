package id.co.kamil.autochat.ui.grup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_GROUP;
import static id.co.kamil.autochat.utils.API.URL_POST_GROUP_ADD_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_CONTACT;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
public class FormTambahKontakGrupActivity extends AppCompatActivity {

    private static final String TAG = "FormTambahKontakGrup";
    private EditText edtCari;
    private ListView listKontak;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private AdapterKontak kontakAdapter;
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private SwipeRefreshLayout swipe_refresh;
    private JSONArray exclude_customers;
    private Button btnTambahkan;
    private JSONObject dataJsonGrup;
    private JSONArray add_kontak = new JSONArray();
    private CheckBox chkSemua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_tambah_kontak_grup);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Tambahkan Kontak ke Grup");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        try {
            exclude_customers = new JSONArray(getIntent().getStringExtra("exclude_customers"));
            dataJsonGrup = new JSONObject(getIntent().getStringExtra("dataJson"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        chkSemua = (CheckBox) findViewById(R.id.chkSemua);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        edtCari = (EditText) findViewById(R.id.edtCari);
        btnTambahkan = (Button) findViewById(R.id.btnTambahkan);
        listKontak = (ListView) findViewById(R.id.listKontak);
        listKontak.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("nomor",dataKontak.get(i).getNomorhp());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        chkSemua.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0 ;i<kontakAdapter.listKontak.size();i++){
                    kontakAdapter.listKontak.get(i).setCheckbox(isChecked);
                    for (int a = 0;a<kontakAdapter.arraylist.size();a++){
                        if (kontakAdapter.arraylist.get(a).getId().equals(kontakAdapter.listKontak.get(i).getId())){
                            kontakAdapter.arraylist.get(a).setCheckbox(isChecked);
                        }
                    }
                }
                kontakAdapter.notifyDataSetChanged();
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
                }catch (Exception e){

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                loadKontak();

            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadKontak();
            }
        });
        btnTambahkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add_kontak = new JSONArray();
                for (int i=0;i<kontakAdapter.arraylist.size();i++){
                    if (kontakAdapter.arraylist.get(i).isCheckbox()){
                        add_kontak.put(kontakAdapter.arraylist.get(i).getId());

                    }
                }
                if (add_kontak.length()>0){
                    addKontaktoGroup();
                }else{
                    Toast.makeText(FormTambahKontakGrupActivity.this, "Data kontak belum ada yang dipilih", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void addKontaktoGroup() {

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("groupId",dataJsonGrup.get("id"));
            requestBody.put("contactId",add_kontak);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GROUP_ADD_CONTACT)
                .buildUpon()
                .toString();

        Log.i(TAG,"body:" + requestBody);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status){
                        Toast.makeText(FormTambahKontakGrupActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
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
                    errorResponse(FormTambahKontakGrupActivity.this,error);
                }else {
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormTambahKontakGrupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
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
                        new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
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
    private void loadKontak(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LIST_CONTACT)
                .buildUpon()
                .toString();
        dataKontak.clear();
        swipe_refresh.setRefreshing(true);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        boolean exist = false;
                        for (int i = 0 ;i<data.length();i++){
                            exist = false;
                            final String id = data.getJSONObject(i).getString("id");
                            final String first_name = data.getJSONObject(i).getString("first_name");
                            final String last_name = data.getJSONObject(i).getString("last_name");
                            final String name = first_name + " " + last_name;
                            final String phone = data.getJSONObject(i).getString("phone");
                            for (int a=0;a<exclude_customers.length();a++){
                                if (id.equals(exclude_customers.get(a).toString())){
                                    exist = true;
                                    break;
                                }
                            }
                            if (exist == false){
                                dataKontak.add(new ItemKontak(id,name,phone,false,true,data.getJSONObject(i)));
                            }
                        }
                    }else{
                        new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();

                    }
                    displayKontak();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(FormTambahKontakGrupActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormTambahKontakGrupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormTambahKontakGrupActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK",null)
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

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void displayKontak() {
        kontakAdapter = new AdapterKontak(dataKontak,this);
        listKontak.setAdapter(kontakAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
