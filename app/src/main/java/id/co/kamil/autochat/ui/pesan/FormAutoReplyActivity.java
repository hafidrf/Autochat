package id.co.kamil.autochat.ui.pesan;

import androidx.annotation.NonNull;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_AUTOREPLY;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_AUTOREPLY;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GRUP_AUTO_REPLY;
import static id.co.kamil.autochat.utils.API.URL_POST_UPDATE_AUTOREPLY;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class FormAutoReplyActivity extends AppCompatActivity {

    private static final String TAG = "FormAutoReplyActivity";
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private Button btnSimpan;
    private EditText edtBalasan;
    private ImageButton btnTambah;

    private List<ItemRecyclerTag> listKeyword = new ArrayList<>();
    private RecylerTagAdapter adapterTag;
    private String tipeForm;
    private String autoreplyId;
    private JSONArray dataKeyword = new JSONArray();
    private long id_tag = 0;
    private Spinner spinStatus;
    private String[] dataStatus = {"aktif","tidak aktif"};
    private Spinner spinGrup;

    private String[] dataGrup = new String[]{};
    private String[] dataIdGrup = new String[]{};
    private TextView txtSapaan, txtNamaDepan,txtNamaBelakang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_autoreply);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tipeForm = getIntent().getStringExtra("tipe");
        if (tipeForm.equals("edit")){
            getSupportActionBar().setTitle("Edit Autoreply");
            autoreplyId = getIntent().getStringExtra("id");
        }else{
            getSupportActionBar().setTitle("Tambah Autoreply");
        }
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);
        spinGrup = (Spinner) findViewById(R.id.spinGrup);
        edtBalasan = (EditText) findViewById(R.id.edtBalasan);
        spinStatus = (Spinner) findViewById(R.id.spinStatus);
        btnTambah = (ImageButton) findViewById(R.id.btnTambah);
        txtSapaan = (TextView) findViewById(R.id.txtSapaan);
        txtNamaDepan = (TextView) findViewById(R.id.txtNamaDepan);
        txtNamaBelakang = (TextView) findViewById(R.id.txtNamaBelakang);

        txtSapaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtBalasan.getText().insert(edtBalasan.getSelectionStart(),"[sapaan] " );
            }
        });

        txtNamaDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtBalasan.getText().insert(edtBalasan.getSelectionStart(),"[nama_depan] " );
            }
        });
        txtNamaBelakang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtBalasan.getText().insert(edtBalasan.getSelectionStart(),"[nama_belakang] " );
            }
        });
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listKeyword.size()<=0){
                    Toast.makeText(FormAutoReplyActivity.this, "Keyword tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(edtBalasan.getText().toString())){
                    edtBalasan.setError("Field ini tidak boleh kosong");
                    edtBalasan.requestFocus();
                }else if(spinGrup.getSelectedItemPosition()<=0){
                    Toast.makeText(FormAutoReplyActivity.this, "Grup belum dipilih", Toast.LENGTH_SHORT).show();
                }else{
                    simpanAutoreply();
                }
            }
        });
        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Alertdialog form input keyword
                dialogForm();
            }
        });


        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gridKeyword);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        adapterTag = new RecylerTagAdapter(listKeyword, this,new RecylerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for(int i = 0; i< listKeyword.size(); i++){
                    if (listKeyword.get(i).getId().equals(item.getId())){
                        listKeyword.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        recyclerView.setAdapter(adapterTag);

        recyclerView.setMinimumHeight((int) convertDpToPixel(35,this));
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerView.setLayoutManager(layoutManager);
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataStatus);
        spinStatus.setAdapter(arrayAdapter);

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

        String uri = Uri.parse(URL_POST_LIST_GRUP_AUTO_REPLY)
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
                        new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                    new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                if (response.statusCode==403){
                    try {
                        JSONObject jsonObject = new JSONObject(new String(response.data));
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")){
                            new AlertDialog.Builder(FormAutoReplyActivity.this)
                                    .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormAutoReplyActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        }else{
                            new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                    new AlertDialog.Builder(FormAutoReplyActivity.this)
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
            requestBody.put("id",autoreplyId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GET_AUTOREPLY)
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
                            final JSONArray keyword = data.getJSONObject(i).getJSONArray("keyword");
                            final String balasan = data.getJSONObject(i).getString("reply");
                            final String status_pesan = data.getJSONObject(i).getString("status");
                            final String group_id = data.getJSONObject(i).getString("group_id");
                            for (int a = 0 ;a<keyword.length();a++){
                                id_tag++;
                                String tmptag = String.valueOf(id_tag);
                                listKeyword.add(new ItemRecyclerTag(tmptag,keyword.getString(a)));
                            }
                            adapterTag.notifyDataSetChanged();
                            edtBalasan.setText(balasan);
                            if (status_pesan.equals("aktif")){
                                spinStatus.setSelection(0);
                            }else{
                                spinStatus.setSelection(1);
                            }
                            for (int a = 0 ;a<dataIdGrup.length;a++){
                                if (dataIdGrup[a].equals(group_id)){
                                    spinGrup.setSelection(a);
                                    break;
                                }
                            }
                        }
                    }else{
                        new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                    new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                if (response.statusCode==403){
                    try {
                        JSONObject jsonObject = new JSONObject(new String(response.data));
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")){
                            new AlertDialog.Builder(FormAutoReplyActivity.this)
                                    .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormAutoReplyActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        }else{
                            new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                    new AlertDialog.Builder(FormAutoReplyActivity.this)
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

    private void dialogForm() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(FormAutoReplyActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_form_keyword, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Form Keyword");

        final EditText edtKeyword    = (EditText) dialogView.findViewById(R.id.edtKeyword);

        edtKeyword.setText("");

        dialog.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                id_tag++;
                String tmpid = String.valueOf(id_tag);
                listKeyword.add(new ItemRecyclerTag(tmpid,edtKeyword.getText().toString()));
                adapterTag.notifyDataSetChanged();
                dialog.dismiss();

            }
        });
        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void simpanAutoreply(){
        dataKeyword = new JSONArray();
        for (int i=0;i<listKeyword.size();i++){
            dataKeyword.put(listKeyword.get(i).getTitle());
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        String url = "";
        try {
            requestBody.put("status",dataStatus[spinStatus.getSelectedItemPosition()]);
            requestBody.put("keyword",dataKeyword);
            requestBody.put("reply",edtBalasan.getText().toString());
            requestBody.put("group_id",dataIdGrup[spinGrup.getSelectedItemPosition()]);
            if (tipeForm.equals("edit")){
                url = URL_POST_UPDATE_AUTOREPLY;
                requestBody.put("id",autoreplyId);
            }else{
                url = URL_POST_CREATE_AUTOREPLY;
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
                        Toast.makeText(FormAutoReplyActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(FormAutoReplyActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                if (response.statusCode==403){
                    try {
                        JSONObject jsonObject = new JSONObject(new String(response.data));
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")){
                            new AlertDialog.Builder(FormAutoReplyActivity.this)
                                    .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormAutoReplyActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        }else{
                            new AlertDialog.Builder(FormAutoReplyActivity.this)
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
                    new AlertDialog.Builder(FormAutoReplyActivity.this)
                            .setMessage(msg)
                            .setCancelable(false)
                            .setPositiveButton("OK", null)
                            .show();
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
