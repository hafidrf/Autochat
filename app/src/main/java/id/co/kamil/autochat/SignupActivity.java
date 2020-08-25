package id.co.kamil.autochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;


import id.co.kamil.autochat.installreferrer.Application;
import id.co.kamil.autochat.installreferrer.ReferrerReceiver;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_GET_OLSHOP_KECAMATAN;
import static id.co.kamil.autochat.utils.API.URL_GET_OLSHOP_KOTA;
import static id.co.kamil.autochat.utils.API.URL_GET_OLSHOP_PROPINSI;
import static id.co.kamil.autochat.utils.API.URL_GET_PROVINSI;
import static id.co.kamil.autochat.utils.API.URL_POST_REGISTER;
import static id.co.kamil.autochat.utils.SessionManager.KEY_AFFILIATION;
import static id.co.kamil.autochat.utils.Utils.errorResponse;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private EditText edtNamaDepan,edtNamaBelakang,edtEmail,edtAlamat,edtPassword,edtKonfirmasiPassword,edtTelp;
    private Spinner spinPropinsi,spinKota,spinKecamatan;
    private CheckBox chkAgreement;
    private String affiliation = "";
    private String[] dataIdPropinsi  = new String[]{};
    private String[] dataIdKota = new String[]{};
    private String[] dataIdKecamatan= new String[]{};

    private String[] dataPropinsi  = new String[]{};
    private String[] dataKota = new String[]{};
    private String[] dataKecamatan= new String[]{};
    private ProgressDialog pDialog;
    private Button btnDaftar;
    private SessionManager session;
    private EditText edtReferal;

    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //updateData();
        }
    };
    private SharPref sharePref;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getSupportActionBar().setTitle("Daftar");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        session = new SessionManager(this);
        sharePref = new SharPref(this);

        pDialog = new ProgressDialog(this);

        edtNamaDepan = (EditText) findViewById(R.id.edtNamaDepan);
        edtNamaBelakang = (EditText) findViewById(R.id.edtNamaBelakang);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtTelp = (EditText) findViewById(R.id.edtTelp);
        edtAlamat = (EditText) findViewById(R.id.edtAlamat);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtKonfirmasiPassword = (EditText) findViewById(R.id.edtKonfirmasiPassword);
        edtReferal = (EditText) findViewById(R.id.edtReferal);
        spinPropinsi = (Spinner) findViewById(R.id.spinPropinsi);
        spinKota = (Spinner) findViewById(R.id.spinKota);
        spinKecamatan = (Spinner) findViewById(R.id.spinKecamatan);
        chkAgreement = (CheckBox) findViewById(R.id.chkAgreement);

        spinPropinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadKota(dataIdPropinsi[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadKecamatan(dataIdKota[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnDaftar = (Button) findViewById(R.id.btnDaftar);
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRequired()){
                    doSignup();
                }
            }
        });

        loadPropinsi();
        //updateData();
        try{
            String referrerUrl = sharePref.getSessionStr(SharPref.REFERRER_URL);
            String referrer = referrerUrl;
            if (!referrerUrl.contains("http:")){
                if (referrerUrl.contains("referrer")){
                    String base_url = "https://play.google.com/store/apps/details?id=id.co.kamil.autochat&";
                    referrerUrl = base_url + referrerUrl;
                    Uri uri = Uri.parse(referrerUrl);
                    referrer = uri.getQueryParameter("referrer");
                }else if(referrerUrl.contains("utm_source")){
                    referrer = "";
                }
            }else{
                Uri uri = Uri.parse(referrerUrl);
                referrer = uri.getQueryParameter("referrer");
            }
            Log.d(TAG,"referrerUrl:" + referrerUrl);
            Log.d(TAG,"referrer:" + referrer);
            edtReferal.setText(referrer);
            Bundle params = new Bundle();
            params.putString("referrerUrl", referrerUrl);
            params.putString("referrer", referrer);
            mFirebaseAnalytics.logEvent("SignupActivityReferal", params);
        }catch (Exception e){
            e.printStackTrace();
        }

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

    private void updateData() {
        affiliation = Application.getReferrerDataRaw(this);
        if (affiliation.contains("Undefined")){
            affiliation = "";
        }
        if (affiliation.contains("utm_source") || affiliation.contains("utm_medium")){
            affiliation = "";
        }
        edtReferal.setText(affiliation);
//        session.setValue(KEY_AFFILIATION,affiliation);
        Log.i(TAG,"affiliation: " + affiliation);
    }
    private boolean isRequired() {
        if (TextUtils.isEmpty(edtNamaDepan.getText())) {
            edtNamaDepan.setError("Field ini tidak boleh kosong");
            edtNamaDepan.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtNamaBelakang.getText())){
            edtNamaBelakang.setError("Field ini tidak boleh kosong");
            edtNamaBelakang.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtEmail.getText())){
            edtEmail.setError("Field ini tidak boleh kosong");
            edtEmail.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtTelp.getText())){
            edtTelp.setError("Field ini tidak boleh kosong");
            edtTelp.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtAlamat.getText())){
            edtAlamat.setError("Field ini tidak boleh kosong");
            edtAlamat.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtPassword.getText())){
            edtPassword.setError("Field ini tidak boleh kosong");
            edtPassword.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtKonfirmasiPassword.getText())){
            edtKonfirmasiPassword.setError("Field ini tidak boleh kosong");
            edtKonfirmasiPassword.requestFocus();
            return false;
        }else if (spinPropinsi.getSelectedItemPosition()<=0){
            Toast.makeText(this, "Propinsi belum dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }else if (spinKota.getSelectedItemPosition()<=0){
            Toast.makeText(this, "Kota belum dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }else if (spinKecamatan.getSelectedItemPosition()<=0){
            Toast.makeText(this, "Kecamatan belum dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }else if (!chkAgreement.isChecked()){
            chkAgreement.setError("Field ini belum diceklis");
            chkAgreement.requestFocus();
            return false;
        }
        return true;
    }
    private void loadPropinsi() {
        dataPropinsi = new String[]{};
        dataIdPropinsi = new String[]{};

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_GET_OLSHOP_PROPINSI)
                .buildUpon()
                .appendQueryParameter("country_id","100")
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final JSONArray zone = response.getJSONArray("zone");

                    if (!zone.equals(null)){
                        dataPropinsi = new String[zone.length()+1];
                        dataIdPropinsi = new String[zone.length()+1];

                        dataPropinsi[0] = "PILIH";
                        dataIdPropinsi[0] = "";

                        for(int i =0;i<zone.length();i++){
                            dataIdPropinsi[i+1] = zone.getJSONObject(i).getString("zone_id");
                            dataPropinsi[i+1] = zone.getJSONObject(i).getString("name");
                        }

                        displayPropinsi();
                    }else{
                        new AlertDialog.Builder(SignupActivity.this)
                                .setMessage("Data Propinsi tidak bisa diload")
                                .setPositiveButton("Coba lagi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadPropinsi();
                                    }
                                })
                                .setNegativeButton("Batal",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(SignupActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(SignupActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void displayPropinsi() {
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataPropinsi);
        spinPropinsi.setAdapter(arrayAdapter);

        loadKota("");
    }

    private void displayKota() {
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataKota);
        spinKota.setAdapter(arrayAdapter);

        loadKecamatan("");
    }
    private void displayKecamatan() {
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataKecamatan);
        spinKecamatan.setAdapter(arrayAdapter);
    }
    private void loadKota(final String id_prop) {
        dataIdKota = new String[]{};
        dataKota = new String[]{};
        if (id_prop.equals("")){
            return;
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_GET_OLSHOP_KOTA)
                .buildUpon()
                .appendQueryParameter("zone_id",id_prop)
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final JSONArray zone = response.getJSONArray("zone");
                    if (!zone.equals(null)){
                        dataKota = new String[zone.length()+1];
                        dataIdKota = new String[zone.length()+1];

                        dataKota[0] = "PILIH";
                        dataIdKota[0] = "";

                        for(int i =0;i<zone.length();i++){
                            dataIdKota[i+1] = zone.getJSONObject(i).getString("city_id");
                            dataKota[i+1] = zone.getJSONObject(i).getString("name");
                        }

                        displayKota();
                    }else{
                        new AlertDialog.Builder(SignupActivity.this)
                                .setMessage("Data Kota tidak bisa diload")
                                .setPositiveButton("Coba lagi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadKota(id_prop);
                                    }
                                })
                                .setNegativeButton("Batal",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(SignupActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(SignupActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);

    }
    private void loadKecamatan(final String id_kota) {
        dataIdKecamatan = new String[]{};
        dataKecamatan = new String[]{};
        if (id_kota.equals("")){
            return;
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_GET_OLSHOP_KECAMATAN)
                .buildUpon()
                .appendQueryParameter("city_id",id_kota)
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final JSONArray zone = response.getJSONArray("zone");
                    if (!zone.equals(null)){

                        dataKecamatan = new String[zone.length()+1];
                        dataIdKecamatan = new String[zone.length()+1];

                        dataKecamatan[0] = "PILIH";
                        dataIdKecamatan[0] = "";

                        for(int i =0;i<zone.length();i++){
                            dataIdKecamatan[i+1] = zone.getJSONObject(i).getString("subdistrict_id");
                            dataKecamatan[i+1] = zone.getJSONObject(i).getString("name");
                        }

                        displayKecamatan();
                    }else{
                        new AlertDialog.Builder(SignupActivity.this)
                                .setMessage("Data Kecamatan tidak bisa diload")
                                .setPositiveButton("Coba lagi", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadKecamatan(id_kota);
                                    }
                                })
                                .setNegativeButton("Batal",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(SignupActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(SignupActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);

    }
    private void doSignup() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final HashMap<String,String> body = new HashMap<>();
        body.put("firstname",edtNamaDepan.getText().toString());
        body.put("lastname",edtNamaBelakang.getText().toString());
        body.put("telephone",edtTelp.getText().toString());
        body.put("email",edtEmail.getText().toString());
        body.put("password",edtPassword.getText().toString());
        body.put("confirm",edtKonfirmasiPassword.getText().toString());
        body.put("address_1",edtAlamat.getText().toString());
        //body.put("country_id",dataIdPropinsi.get(spinPropinsi.getSelectedItemPosition()));
        body.put("zone_id",dataIdPropinsi[spinPropinsi.getSelectedItemPosition()]);
        body.put("city_id",dataIdKota[spinKota.getSelectedItemPosition()]);
        body.put("district_id",dataIdKecamatan[spinKecamatan.getSelectedItemPosition()]);
        body.put("marketingcode",edtReferal.getText().toString());

        final JSONObject param = new JSONObject(body);
        final String uri = Uri.parse(URL_POST_REGISTER)
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        new AlertDialog.Builder(SignupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(SignupActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(SignupActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
