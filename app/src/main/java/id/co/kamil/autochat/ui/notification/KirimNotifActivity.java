package id.co.kamil.autochat.ui.notification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ImageButton;
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
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.ItemRecyclerTag;
import id.co.kamil.autochat.adapter.RecylerTagAdapter;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_NOTIF_FIREBASE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class KirimNotifActivity extends AppCompatActivity {

    private static final String TAG = "KirimNotifActivity";
    private static final int REQUEST_USER = 100;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private EditText edtJudul;
    private EditText edtUrl;
    private EditText edtPesan;
    private Button btnSimpan;
    private RecyclerView recyclerView;
    private RecylerTagAdapter adapterTag;
    private JSONArray excludeContact;
    private JSONArray excludeId;
    private List<ItemRecyclerTag> listNoTujuan = new ArrayList<>();
    private ImageButton btnCariUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kirim_notif);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Kirim Langsung");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        edtJudul = (EditText) findViewById(R.id.edtJudul);
        edtUrl = (EditText) findViewById(R.id.edtUrl);
        edtPesan = (EditText) findViewById(R.id.edtPesan);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnCariUser = (ImageButton) findViewById(R.id.btnUser);
        btnCariUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(KirimNotifActivity.this, ListUserActivity.class);
                i.putExtra("exclude",excludeContact.toString());
                startActivityForResult(i,REQUEST_USER);
            }
        });
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()){
                    new AlertDialog.Builder(KirimNotifActivity.this)
                            .setMessage("Apakah anda yakin akan kirim notif berikut ?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendNotif();
                                }
                            })
                            .setNegativeButton("Tidak",null)
                            .show();

                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.gridUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        adapterTag = new RecylerTagAdapter(listNoTujuan, this,new RecylerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for(int i=0;i<listNoTujuan.size();i++){
                    if (listNoTujuan.get(i).getId().equals(item.getId())){
                        listNoTujuan.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
                reloadExcludeContact();

            }
        });
        recyclerView.setAdapter(adapterTag);
        recyclerView.setMinimumHeight((int) convertDpToPixel(35,this));
        reloadExcludeContact();
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void reloadExcludeContact(){
        excludeContact = new JSONArray();
        excludeId = new JSONArray();
        for(int i = 0; i< listNoTujuan.size();i++){
            excludeContact.put(listNoTujuan.get(i).getId());
            excludeId.put(listNoTujuan.get(i).getFirebase());
        }

    }
    private boolean isRequired() {
        if (TextUtils.isEmpty(edtJudul.getText())){
            edtJudul.setError("Field ini tidak boleh kosong");
            edtJudul.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(edtPesan.getText())){
            edtPesan.setError("Field ini tidak boleh kosong");
            edtPesan.requestFocus();
            return false;
        }
        if (excludeContact.length()<=0){
            Toast.makeText(this, "Pengguna Tujuan belum dipilih", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void sendNotif(){
        JSONArray regId = excludeId;

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        String url = URL_POST_NOTIF_FIREBASE;
        String fieldUrl = edtUrl.getText().toString();
        if(fieldUrl.isEmpty() || fieldUrl==null){

        }else{
            if (!fieldUrl.substring(0, 4).equals("http")) {
                fieldUrl = "http://" + fieldUrl;
            }
        }
        try {
            requestBody.put("message",edtPesan.getText().toString());
            requestBody.put("title",edtJudul.getText().toString());
            requestBody.put("push_type","individual");
            requestBody.put("url",fieldUrl);
            requestBody.put("regId",regId);

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
                        Toast.makeText(KirimNotifActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(KirimNotifActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(KirimNotifActivity.this)
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
                if (response == null) {
                    errorResponse(KirimNotifActivity.this, error);
                }else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(KirimNotifActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(KirimNotifActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(KirimNotifActivity.this)
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

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(KirimNotifActivity.this)
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_USER){
            if (resultCode==RESULT_OK){
                String dataId = data.getStringExtra("id");
                String dataTitle = data.getStringExtra("title");
                String dataNomor = data.getStringExtra("firebase");
                try {
                    JSONArray jsonId = new JSONArray(dataId);
                    JSONArray jsonTitle = new JSONArray(dataTitle);
                    JSONArray jsonNomor = new JSONArray(dataNomor);
                    for (int i = 0;i<jsonId.length();i++){
                        listNoTujuan.add(new ItemRecyclerTag(jsonId.get(i).toString(),jsonTitle.get(i).toString(),"",jsonNomor.get(i).toString()));
                        excludeContact.put(jsonId.getString(i));
                        excludeId.put(jsonNomor.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                adapterTag.notifyDataSetChanged();
                reloadExcludeContact();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
