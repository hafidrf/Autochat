package id.co.kamil.autochat.ui.autotext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.HashMap;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_AUTOTEXT;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_AUTOTEXT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GROUP_AUTOTEXT;
import static id.co.kamil.autochat.utils.API.URL_POST_UPDATE_AUTOTEXT;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class FormAutoTextActivity extends AppCompatActivity {

    private static final String TAG = "FormAutoReplyActivity";
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private Button btnSimpan;
    private EditText edtTemplate;

    private String tipeForm;
    private String autotextId;
    private long id_tag = 0;
    private Spinner spinGrup;
    private String[] dataGrup = new String[]{};
    private String[] dataIdGrup = new String[]{};
    private EditText edtShorcut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_auto_text);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tipeForm = getIntent().getStringExtra("tipe");
        if (tipeForm.equals("edit")){
            getSupportActionBar().setTitle("Edit Autotext");
            autotextId = getIntent().getStringExtra("id");
        }else{
            getSupportActionBar().setTitle("Tambah Autotext");
        }
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);

        edtShorcut = (EditText) findViewById(R.id.edtShorcut);
        edtTemplate = (EditText) findViewById(R.id.edtTemplate);
        spinGrup = (Spinner) findViewById(R.id.spinGrup);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtShorcut.getText().toString())){
                    edtShorcut.setError("Field ini tidak boleh kosong");
                    edtShorcut.requestFocus();
                }else if(TextUtils.isEmpty(edtTemplate.getText().toString())){
                    edtTemplate.setError("Field ini tidak boleh kosong");
                    edtTemplate.requestFocus();
                }else if(spinGrup.getSelectedItemPosition()<=0){
                    Toast.makeText(FormAutoTextActivity.this, "Grup belum dipilih", Toast.LENGTH_SHORT).show();
                }else{
                    simpanAutoText();
                }
            }
        });
        loadGrup();

    }
    private void displayGrup(){
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataGrup);
        spinGrup.setAdapter(arrayAdapter);
    }
    private void loadGrup() {
        dataGrup = new String[]{};
        dataIdGrup = new String[]{};
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        String uri = Uri.parse(URL_POST_LIST_GROUP_AUTOTEXT)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        dataGrup = new String[data.length()+1];
                        dataIdGrup = new String[data.length()+1];
                        dataGrup[0] = "PILIH";
                        dataIdGrup[0] = "";
                        for (int i = 0;i<data.length();i++){
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            dataGrup[i+1] = name;
                            dataIdGrup[i+1] = id;
                        }
                        if (tipeForm.equals("edit")){
                            loadData();
                        }
                    }else{
                        new AlertDialog.Builder(FormAutoTextActivity.this)
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                    displayGrup();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormAutoTextActivity.this)
                            .setMessage(e.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(FormAutoTextActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(FormAutoTextActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormAutoTextActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormAutoTextActivity.this)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormAutoTextActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
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

    private void loadData() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id",autotextId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GET_AUTOTEXT)
                .buildUpon()
                .toString();
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
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0;i<data.length();i++){
                            final String id = data.getJSONObject(i).getString("id");
                            final String shorcut = data.getJSONObject(i).getString("shorcut");
                            final String template = data.getJSONObject(i).getString("template");
                            final String group_id = data.getJSONObject(i).getString("group_id");
                            for (int a = 0 ;a<dataIdGrup.length;a++){
                                if (dataIdGrup[a].equals(group_id)){
                                    spinGrup.setSelection(a);
                                    break;
                                }
                            }
                            edtTemplate.setText(template);
                            edtShorcut.setText(shorcut);
                        }
                    }else{
                        new AlertDialog.Builder(FormAutoTextActivity.this)
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormAutoTextActivity.this)
                            .setMessage(e.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
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
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(FormAutoTextActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(FormAutoTextActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormAutoTextActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormAutoTextActivity.this)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        })
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormAutoTextActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
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



    private void simpanAutoText(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        String url = "";
        try {
            requestBody.put("group_id",dataIdGrup[spinGrup.getSelectedItemPosition()]);
            requestBody.put("template",edtTemplate.getText().toString());
            requestBody.put("shorcut", edtShorcut.getText().toString());
            if (tipeForm.equals("edit")){
                url = URL_POST_UPDATE_AUTOTEXT;
                requestBody.put("id",autotextId);
            }else{
                url = URL_POST_CREATE_AUTOTEXT;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(url)
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
                        Toast.makeText(FormAutoTextActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(FormAutoTextActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormAutoTextActivity.this)
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
                    errorResponse(FormAutoTextActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(FormAutoTextActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormAutoTextActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormAutoTextActivity.this)
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
                        new AlertDialog.Builder(FormAutoTextActivity.this)
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
