package id.co.kamil.autochat.ui.followup;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterFollowup;
import id.co.kamil.autochat.adapter.ItemFollowup;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_FOLLOW_UP;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_FOLLOW_UP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class MainFollowupActivity extends AppCompatActivity {

    private static final int REQUEST_ADD = 100;
    private static final String TAG = "MainFollowupActivity";
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private EditText edtCari;
    private ListView listFollowup;
    private List<ItemFollowup> dataFollowup = new ArrayList<>();
    private AdapterFollowup followupAdapter;
    private boolean adapterInstance = false;
    private Menu menuTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_followup);
        getSupportActionBar().setTitle("Follow Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        layMessage = (LinearLayout)  findViewById(R.id.layMessage);
        lblMessage = (TextView)  findViewById(R.id.lblMessage);
        btnCobaLagi = (Button)  findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFollowup();
            }
        });
        edtCari = (EditText) findViewById(R.id.edtCari);
        listFollowup = (ListView) findViewById(R.id.listFollowup);
        listFollowup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainFollowupActivity.this,FormFollowupActivity.class);
                intent.putExtra("id", dataFollowup.get(i).getId());
                intent.putExtra("data", dataFollowup.get(i).getJsonObject().toString());
                intent.putExtra("tipe", "edit");
                startActivityForResult(intent,REQUEST_ADD);

            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    followupAdapter.filter(edtCari.getText().toString().trim());
                    listFollowup.invalidate();
                }catch (NullPointerException e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listFollowup.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listFollowup == null || listFollowup.getChildCount() == 0) ?
                                0 : listFollowup.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadFollowup();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFollowup();
            }
        });

    }
    private void loadFollowup(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LIST_FOLLOW_UP)
                .buildUpon()
                .toString();
        showError(false,"",true);
        swipe_refresh.setRefreshing(true);
        dataFollowup.clear();
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
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            dataFollowup.add(new ItemFollowup(id,name,data.getJSONObject(i),false,false));
                        }
                    }else{
                        showError(true,message,false);
                    }
                    displayFollowup();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(true,e.getMessage(),true);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(MainFollowupActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(MainFollowupActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(MainFollowupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                showError(true,msg,true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        showError(true,msg,true);
                    }
                }

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
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
    private void showError(boolean show,String message, boolean visibleButton){
        if (show){
            layMessage.setVisibility(View.VISIBLE);
            listFollowup.setVisibility(View.GONE);
            lblMessage.setText(message);
        }else{
            layMessage.setVisibility(View.GONE);
            listFollowup.setVisibility(View.VISIBLE);
        }
        if (visibleButton){
            btnCobaLagi.setVisibility(View.VISIBLE);
        }else{
            btnCobaLagi.setVisibility(View.GONE);
        }
    }
    private void displayFollowup() {
        followupAdapter = new AdapterFollowup(dataFollowup,this);
        listFollowup.setAdapter(followupAdapter);
        adapterInstance = true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.actTambah){
            Intent i = new Intent(this, FormFollowupActivity.class);
            i.putExtra("tipe","add");
            startActivityForResult(i,REQUEST_ADD);
        }else if (item.getItemId()==R.id.actEdit) {
            if (dataFollowup.size()>0){
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataFollowup.size(); i++){
                    ItemFollowup ikontak = dataFollowup.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataFollowup.set(i,ikontak);
                }
                followupAdapter.notifyDataSetChanged();
            }else{
                Toast.makeText(MainFollowupActivity.this, "Data Followup tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }else if (item.getItemId()==R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            listDefault();
        }else if (item.getItemId()==R.id.actHapus) {
            new AlertDialog.Builder(this)
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusFollowup();
                        }
                    })
                    .setNegativeButton("Tidak",null)
                    .show();

        }else if (item.getItemId()==R.id.actSemua) {
            for (int i = 0; i < dataFollowup.size(); i++){
                ItemFollowup ikontak = dataFollowup.get(i);
                ikontak.setCheckbox(true);
                dataFollowup.set(i,ikontak);
            }
            followupAdapter.notifyDataSetChanged();
        }else if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private void listDefault(){
        for (int i = 0; i < dataFollowup.size(); i++){
            ItemFollowup ikontak = dataFollowup.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataFollowup.set(i,ikontak);
        }
        followupAdapter.notifyDataSetChanged();
        if(dataFollowup.size()==0){
            loadFollowup();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance){
            listDefault();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuTop = menu;
        getMenuInflater().inflate(R.menu.kontak, menuTop);
        return super.onCreateOptionsMenu(menu);
    }

    private void hapusFollowup() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONArray idHapus = new JSONArray();
        for (int i = 0; i < dataFollowup.size(); i++){
            if (dataFollowup.get(i).isCheckbox()){
                idHapus.put(Integer.parseInt(dataFollowup.get(i).getId()));
            }
        }
        final JSONObject param = new JSONObject();
        try {
            param.put("id",idHapus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_HAPUS_FOLLOW_UP)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        for (int i = 0; i < dataFollowup.size(); i++){
                            if (dataFollowup.get(i).isCheckbox()){
                                dataFollowup.remove(i);
                                i = i - 1;
                            }
                        }
                        followupAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        listDefault();
                    }else{
                        new AlertDialog.Builder(MainFollowupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(MainFollowupActivity.this)
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
                    errorResponse(MainFollowupActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(MainFollowupActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(MainFollowupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(MainFollowupActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(MainFollowupActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }


            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
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
        if(pDialog.isShowing())
            pDialog.dismiss();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD){
            if (resultCode==RESULT_OK){
                loadFollowup();
            }
        }
    }

}
