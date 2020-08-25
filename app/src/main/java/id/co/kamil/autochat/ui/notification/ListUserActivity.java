package id.co.kamil.autochat.ui.notification;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.StringJoiner;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterKontak;
import id.co.kamil.autochat.adapter.ItemKontak;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_USER_FIREBASE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;

public class ListUserActivity extends AppCompatActivity {

    private static final String TAG = "ListUserActivity";
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private JSONArray excludeContact;
    private SwipeRefreshLayout swipe_refresh;
    private EditText edtCari;
    private ListView listUser;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private AdapterKontak kontakAdapter;
    private Button btnTambahkan;
    private CheckBox chkSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Cari User");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        final String exclude = getIntent().getStringExtra("exclude");
        try {
            excludeContact = new JSONArray(exclude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        edtCari = (EditText) findViewById(R.id.edtCari);
        chkSelectAll = (CheckBox) findViewById(R.id.chkSelectAll);
        chkSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0;i<dataKontak.size();i++){
                    dataKontak.get(i).setCheckbox(chkSelectAll.isChecked());
                }
                kontakAdapter.notifyDataSetChanged();
            }
        });
        btnTambahkan = (Button) findViewById(R.id.btnTambahkan);
        btnTambahkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final JSONArray jsonId = new JSONArray();
                final JSONArray jsonTitle = new JSONArray();
                final JSONArray jsonFirebase = new JSONArray();

                int checked = 0;
                for (int i = 0;i<kontakAdapter.arraylist.size();i++){
                    if (kontakAdapter.arraylist.get(i).isCheckbox()){
                        jsonId.put(kontakAdapter.arraylist.get(i).getId());
                        jsonTitle.put(kontakAdapter.arraylist.get(i).getJudul());
                        jsonFirebase.put(kontakAdapter.arraylist.get(i).getNomorhp());
                        checked++;
                    }
                }
                if (checked<=0){
                    Toast.makeText(ListUserActivity.this, "Belum ada user yang dipilih", Toast.LENGTH_SHORT).show();
                    return;
                }
                new AlertDialog.Builder(ListUserActivity.this)
                        .setMessage("Apakah anda akan menambahkan user berikut ?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.putExtra("id",jsonId.toString());
                                intent.putExtra("title",jsonTitle.toString());
                                intent.putExtra("firebase",jsonFirebase.toString());
                                setResult(RESULT_OK,intent);
                                finish();
                            }
                        })
                        .setNegativeButton("Tidak", null)
                        .show();

            }
        });
        listUser = (ListView) findViewById(R.id.listUser);
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    kontakAdapter.filter(edtCari.getText().toString().trim());
                    listUser.invalidate();
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
        final String uri = Uri.parse(URL_POST_LIST_USER_FIREBASE)
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
                        for (int i = 0 ;i<data.length();i++){
                            final String id = data.getJSONObject(i).getString("user_id");
                            final String name = data.getJSONObject(i).getString("u_name");
                            final String firebase = data.getJSONObject(i).getString("firebase");
                            boolean exist = false;
                            for(int a=0;a<excludeContact.length();a++){
                                if (id.equals(excludeContact.get(a))){
                                    exist = true;
                                    break;
                                }
                            }
                            if (exist==false){
                                dataKontak.add(new ItemKontak(id,name,firebase,false,true,data.getJSONObject(i)));
                            }
                        }
                    }else{
                        new AlertDialog.Builder(ListUserActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();

                    }
                    displayKontak();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ListUserActivity.this)
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
                    errorResponse(ListUserActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(ListUserActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(ListUserActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(ListUserActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(ListUserActivity.this)
                                .setMessage(msg)
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
        listUser.setAdapter(kontakAdapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
