package id.co.kamil.autochat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.installreferrer.Application;
import id.co.kamil.autochat.installreferrer.ReferrerReceiver;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_LUPA_PASSWORD;
import static id.co.kamil.autochat.utils.API.URL_POST_LOGIN;
import static id.co.kamil.autochat.utils.SessionManager.KEY_AFFILIATION;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class LoginActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 11;
    private static final String TAG = "LoginActivity";
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private Button btnDaftar;
    private DBHelper dbHelper;
    private String affiliation;
    private TextView txtLupaPassword;
    private SharPref sharePref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);
        sharePref = new SharPref(this);
        pDialog = new ProgressDialog(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        if (session.isLoggedIn()){
            goToMain();
        }
        txtLupaPassword = (TextView) findViewById(R.id.txtLupaPassword);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnDaftar = (Button) findViewById(R.id.btnDaftar);
        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()){
                    doLogin();
                }
            }
        });
        txtLupaPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = URL_LUPA_PASSWORD;
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(url));
                startActivity(intent2);
            }
        });
        cekPermission();
    }

    private boolean isRequired(){
        if (TextUtils.isEmpty(edtEmail.getText())){
            edtEmail.setError("Field ini tidak boleh kosong");
            edtEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(edtPassword.getText())){
            edtPassword.setError("Field ini tidak boleh kosong");
            edtPassword.requestFocus();
            return false;
        }
        return true;
    }
    private void doLogin() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final HashMap<String,String> body = new HashMap<>();
        body.put("username", edtEmail.getText().toString());
        body.put("password",edtPassword.getText().toString());

        final JSONObject param = new JSONObject(body);
        final String uri = Uri.parse(URL_POST_LOGIN)
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
                        final JSONObject data = response.getJSONObject("customer_info");
                        final String auth_token = response.getString("access_token");
                        final String customer_id = data.getString("customer_id");
                        final String firstname = data.getString("firstname");
                        final String lastname = data.getString("lastname");
                        final String email = data.getString("email");
                        final String phone = data.getString("telephone");
                        final String parent_id = response.getString("parent_id");
                        final String customer_group_id = data.getString("customer_group_id");
                        final boolean child = response.getBoolean("is_child");

                        Log.i(TAG,"token:" + auth_token);
                        Log.e(TAG,"parent_id" + parent_id);
                        session.createLoginSession(customer_id,auth_token,firstname,lastname,email,phone,customer_group_id,child,parent_id);
                        dbHelper.clearData();
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        goToMain();
                    }else{
                        new AlertDialog.Builder(LoginActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG,"error:" + errorResponseString(error));
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(LoginActivity.this)
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
        try {
            if(pDialog.isShowing())
                pDialog.dismiss();
        }catch (Exception e){

        }
    }
    private void goToMain() {
        startActivity(new Intent(LoginActivity.this,MainActivity.class));
        finish();
    }

    private void cekPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

}
