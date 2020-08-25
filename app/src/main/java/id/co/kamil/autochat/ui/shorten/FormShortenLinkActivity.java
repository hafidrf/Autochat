package id.co.kamil.autochat.ui.shorten;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CHECK_SUBDOMAIN;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_SHORTEN;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_SHORTEN;
import static id.co.kamil.autochat.utils.API.URL_POST_SHORTEN_BY_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class FormShortenLinkActivity extends AppCompatActivity {
    private static final String TAG = "FormShortenLinkActivity";
    private String tipeForm;
    private Button btnSimpan;
    private EditText edtPhone,edtRedaksi,edtSubdomain;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private String idShorten;
    private ProgressDialog pDialog;
    private EditText edtCode;
    private TextView txtLoading;
    private String lastSubdomain;
    private String[] arrDomain = {"autochat.id","autochat.my.id"};
    private Spinner spinDomain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_shorten_link);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        tipeForm = getIntent().getStringExtra("tipe");

        if (tipeForm.equals("edit")){
            getSupportActionBar().setTitle("Edit Shortenlink");
        }else{
            getSupportActionBar().setTitle("Tambah Shortenlink");
        }
        txtLoading = (TextView) findViewById(R.id.loading);
        edtCode = (EditText) findViewById(R.id.edtCode);
        edtPhone = (EditText) findViewById(R.id.edtPhone);
        edtSubdomain = (EditText) findViewById(R.id.edtSubdomain);
        edtRedaksi = (EditText) findViewById(R.id.edtRedaksi);
        spinDomain = (Spinner) findViewById(R.id.spinDomain);
        edtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (i==0 && charSequence.equals(0)){
                    return;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edtSubdomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (tipeForm.equals("add")){
                    checkAvailable(edtSubdomain.getText().toString());
                }else if (lastSubdomain.equals(edtSubdomain.getText().toString())){
                    txtLoading.setText("Subdomain Tersedia");
                    txtLoading.setTextColor(Color.BLUE);
                }else{
                    checkAvailable(edtSubdomain.getText().toString());
                }
            }
        });
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpan();
            }
        });
        final ArrayAdapter domainAdapter = new ArrayAdapter(this,R.layout.item_spinner,arrDomain);
        spinDomain.setAdapter(domainAdapter);

        edtPhone.requestFocus();
        if (tipeForm.equals("edit")){
            idShorten = getIntent().getStringExtra("id");
            getData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkAvailable(String subdomain){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("subdomain",subdomain);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_CHECK_SUBDOMAIN)
                .buildUpon()
                .toString();
        txtLoading.setText("Cek ketersediaan subdomain...");
        txtLoading.setTextColor(Color.GREEN);

        Log.i(TAG,requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        txtLoading.setTextColor(Color.BLUE);
                        txtLoading.setText(message);
                    }else{
                        txtLoading.setTextColor(Color.RED);
                        txtLoading.setText(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    txtLoading.setTextColor(Color.RED);
                    txtLoading.setText(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(FormShortenLinkActivity.this, error);
                }else {
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(FormShortenLinkActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormShortenLinkActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormShortenLinkActivity.this)
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
                        txtLoading.setTextColor(Color.RED);
                        txtLoading.setText(msg);
                        Log.i(TAG,errorResponseString(error));
                    }

                }


            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                //header.put("Content-Type","multipart/form-data");
                header.put("x-api-key",token);
                return header;
            }

        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void getData(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id",idShorten);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_SHORTEN_BY_ID)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG,requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        final JSONObject data = response.getJSONObject("data");
                        String phone = data.getString("s_phone");
                        final String domain = data.getString("s_domain");
                        final String subdomain = data.getString("s_subdomain");
                        final String redaksi = data.getString("s_redaksi");
                        phone = phone.replaceFirst("62","");
                        lastSubdomain = subdomain;
                        edtPhone.setText(phone);
                        edtSubdomain.setText(subdomain);
                        edtRedaksi.setText(redaksi);
                        if (domain.contains("autochat.id")){
                            spinDomain.setSelection(0);
                        }else{
                            spinDomain.setSelection(1);
                        }

                    }else{
                        new AlertDialog.Builder(FormShortenLinkActivity.this)
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                })
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormShortenLinkActivity.this)
                            .setMessage(e.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG,errorResponseString(error));
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(FormShortenLinkActivity.this)
                        .setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .show();

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                //header.put("Content-Type","multipart/form-data");
                header.put("x-api-key",token);
                return header;
            }

        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    private void simpan(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final String phone = edtPhone.getText().toString();
        final String code = edtCode.getText().toString();
        final JSONObject requestBody = new JSONObject();
        try {

            String uri = Uri.parse("https://api.whatsapp.com/send")
                    .buildUpon()
                    .appendQueryParameter("phone", code + phone)
                    .appendQueryParameter("text", edtRedaksi.getText().toString())
                    .build().toString();

            requestBody.put("domain","https://" + arrDomain[spinDomain.getSelectedItemPosition()] + "/");
            requestBody.put("phone",edtCode.getText().toString() + edtPhone.getText().toString());
            requestBody.put("redaksi",edtRedaksi.getText().toString());
            requestBody.put("url_redirect",uri);
            requestBody.put("subdomain",edtSubdomain.getText().toString());
            if (tipeForm.equals("edit")){
                requestBody.put("id",idShorten);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = "";
        if (tipeForm.equals("add")){
            uri = Uri.parse(URL_POST_CREATE_SHORTEN)
                    .buildUpon()
                    .toString();
        }else{
            uri = Uri.parse(URL_POST_EDIT_SHORTEN)
                    .buildUpon()
                    .toString();
        }

        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG,requestBody.toString());
        final StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, uri, new Response.Listener<String>() {
            @Override
            public void onResponse(String r) {
                hidePdialog();
                JSONObject response;
                try {
                    response = new JSONObject(r);
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        setResult(RESULT_OK);
                        Toast.makeText(FormShortenLinkActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        new AlertDialog.Builder(FormShortenLinkActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                } catch (JSONException e) {
                    Log.i(TAG,r);
                    e.printStackTrace();
                    new AlertDialog.Builder(FormShortenLinkActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG,errorResponseString(error));
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(FormShortenLinkActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                //header.put("Content-Type","multipart/form-data");
                header.put("x-api-key",token);
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String uri = Uri.parse("https://api.whatsapp.com/send")
                        .buildUpon()
                        .appendQueryParameter("phone", code + phone)
                        .appendQueryParameter("text", edtRedaksi.getText().toString())
                        .build().toString();
                Map<String,String> param = new HashMap<>();

                param.put("domain","https://" + arrDomain[spinDomain.getSelectedItemPosition()] + "/");
                param.put("phone",edtCode.getText().toString() + edtPhone.getText().toString());
                param.put("redaksi",edtRedaksi.getText().toString());
                param.put("url_redirect",uri);
                param.put("subdomain",edtSubdomain.getText().toString());
                if (tipeForm.equals("edit")){
                    param.put("id",idShorten);
                }
                return param;
            }
        };
//        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                hidePdialog();
//                try {
//                    final boolean status = response.getBoolean("status");
//                    final String message = response.getString("message");
//
//                    if (status){
//                        setResult(RESULT_OK);
//                        Toast.makeText(FormShortenLinkActivity.this, message, Toast.LENGTH_SHORT).show();
//                        finish();
//                    }else{
//                        new AlertDialog.Builder(FormShortenLinkActivity.this)
//                                .setMessage(message)
//                                .setPositiveButton("OK",null)
//                                .show();
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    new AlertDialog.Builder(FormShortenLinkActivity.this)
//                            .setMessage(e.getMessage())
//                            .setPositiveButton("OK",null)
//                            .show();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                hidePdialog();
//                Log.i(TAG,errorResponseString(error));
//                final String msg = getResources().getString(errorResponse(error));
//                new AlertDialog.Builder(FormShortenLinkActivity.this)
//                        .setMessage(msg)
//                        .setPositiveButton("OK",null)
//                        .show();
//
//            }
//        }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> header = new HashMap<>();
//                //header.put("Content-Type","multipart/form-data");
//                header.put("x-api-key",token);
//                return header;
//            }
//
//        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
