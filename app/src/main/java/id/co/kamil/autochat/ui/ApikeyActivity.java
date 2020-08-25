package id.co.kamil.autochat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_GET_API_KEY;
import static id.co.kamil.autochat.utils.API.URL_POST_GENERATE_API_KEY;
import static id.co.kamil.autochat.utils.API.URL_POST_URL_CALLBACK;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class ApikeyActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private EditText edtApiKey;
    private Button btnGenerate;
    private Button btnCopy;
    private boolean fitur_enabled = false;
    private Button btnSimpan;
    private EditText edtUrlCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apikey);
        getSupportActionBar().setTitle("API Key");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        if (userDetail.get(KEY_CUST_GROUP).equals("1") || userDetail.get(KEY_CUST_GROUP).equals(1)){
            fitur_enabled = false;
        }else{
            fitur_enabled = true;
        }
        edtUrlCallback = (EditText) findViewById(R.id.edtUrlCallback);
        edtApiKey = (EditText) findViewById(R.id.edtAPIKey);
        btnGenerate = (Button) findViewById(R.id.btnGenerate);
        btnCopy = (Button) findViewById(R.id.btnCopy);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(ApikeyActivity.this,edtApiKey.getText().toString());
                Toast.makeText(ApikeyActivity.this, "API Key berhasil disalin", Toast.LENGTH_SHORT).show();
            }
        });
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fitur_enabled){
                    new AlertDialog.Builder(ApikeyActivity.this)
                            .setMessage("Apakah anda yakin akan generate API Key baru?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    generateApiKey();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();
                }
            }
        });
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fitur_enabled){
                    new AlertDialog.Builder(ApikeyActivity.this)
                            .setMessage("Apakah anda yakin akan menyimpan Url Callback untuk Notifikasi?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    simpanUrlCallbak();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();
                }
            }
        });
        pDialog = new ProgressDialog(this);
        btnCopy.setEnabled(fitur_enabled);
        btnGenerate.setEnabled(fitur_enabled);
        if (fitur_enabled){
            loadApikey();
        }else{
            new AlertDialog.Builder(this)
                    .setMessage("Fitur ini hanya untuk akun premium atau bisnis")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void simpanUrlCallbak() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final HashMap<String,String> body = new HashMap<>();
        body.put("url_reversal",edtUrlCallback.getText().toString());
        JSONObject param = new JSONObject(body);

        final String uri = Uri.parse(URL_POST_URL_CALLBACK)
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
                    if(status){
                        session.setValue("url_reversal",edtUrlCallback.getText().toString());
                    }
                    new AlertDialog.Builder(ApikeyActivity.this)
                            .setMessage(message)
                            .setPositiveButton("OK",null)
                            .show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ApikeyActivity.this)
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
                new AlertDialog.Builder(ApikeyActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

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

    private void generateApiKey() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final HashMap<String,String> body = new HashMap<>();


        final String uri = Uri.parse(URL_POST_GENERATE_API_KEY)
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
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){

                        final String apikey = response.getString("apikey");
                        edtApiKey.setText(apikey);
                    }else{
                        new AlertDialog.Builder(ApikeyActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ApikeyActivity.this)
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
                new AlertDialog.Builder(ApikeyActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

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
    private void loadApikey() {
        edtApiKey.setText("");
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final HashMap<String,String> body = new HashMap<>();


        final String uri = Uri.parse(URL_GET_API_KEY)
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
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){

                        final String apikey = response.getString("apikey");
                        final String url = response.getString("url_reversal");
                        edtApiKey.setText(apikey);
                        edtUrlCallback.setText(url);
                    }else{
                        new AlertDialog.Builder(ApikeyActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ApikeyActivity.this)
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
                new AlertDialog.Builder(ApikeyActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
