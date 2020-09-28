package id.co.kamil.autochat.ui.autotext;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_GROUP_AUTOTEXT;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_GROUP_AUTOTEXT;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class FormGrupAutoTextActivity extends AppCompatActivity {

    private static final String TAG = "FormGrupATextActivity";
    private String tipeForm;
    private EditText edtName, edtDesc;
    private Button btnSimpan;
    private String idGrup;
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private JSONObject data;
    private String name;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_grup_auto_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        Log.i(TAG, token);
        pDialog = new ProgressDialog(this);
        edtName = (EditText) findViewById(R.id.edtNama);
        edtDesc = (EditText) findViewById(R.id.edtDeskripsi);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()) {
                    simpan();
                }
            }
        });
        tipeForm = getIntent().getStringExtra("tipe");
        if (tipeForm.equals("add")) {
            getSupportActionBar().setTitle("Tambah Grup");
        } else {
            getSupportActionBar().setTitle("Edit Grup");
            idGrup = getIntent().getStringExtra("id");
            final String tmpData = getIntent().getStringExtra("data");
            try {
                data = new JSONObject(tmpData);
                name = data.getString("name");
                description = data.getString("description");
                edtName.setText(name);
                edtDesc.setText(description);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtName.getText())) {
            edtName.setError("Field ini tidak boleh kosong");
            edtName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(edtDesc.getText())) {
            edtDesc.setError("Field ini tidak boleh kosong");
            edtDesc.requestFocus();
            return false;
        }
        return true;
    }

    private void simpan() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            if (tipeForm.equals("edit")) {
                requestBody.put("id", idGrup);
            }
            requestBody.put("name", edtName.getText().toString());
            requestBody.put("description", edtDesc.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = "";
        if (tipeForm.equals("edit")) {
            uri = Uri.parse(URL_POST_EDIT_GROUP_AUTOTEXT)
                    .buildUpon()
                    .toString();
        } else {
            uri = Uri.parse(URL_POST_CREATE_GROUP_AUTOTEXT)
                    .buildUpon()
                    .toString();
        }
        Log.i(TAG, "body:" + requestBody);
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
                    if (status) {
                        Toast.makeText(FormGrupAutoTextActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        new AlertDialog.Builder(FormGrupAutoTextActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormGrupAutoTextActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG, errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(FormGrupAutoTextActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormGrupAutoTextActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormGrupAutoTextActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormGrupAutoTextActivity.this)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormGrupAutoTextActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
