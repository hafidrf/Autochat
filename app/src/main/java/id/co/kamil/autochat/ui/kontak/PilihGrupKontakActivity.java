package id.co.kamil.autochat.ui.kontak;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import id.co.kamil.autochat.adapter.AdapterKontak;
import id.co.kamil.autochat.adapter.ItemKontak;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_CONTACT_GROUP;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GRUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;

public class PilihGrupKontakActivity extends AppCompatActivity {

    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private EditText edtCari;
    private Button btnTambahkan;
    private ListView listKontak;
    private AdapterKontak kontakAdapter;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private String[] dataIdGrup = new String[]{};
    private String[] dataGrup = new String[]{};
    private Spinner spinGrup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilih_grup_kontak);

        getSupportActionBar().setTitle("Pilih Grup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);

        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        spinGrup = (Spinner) findViewById(R.id.spinGrup);
        edtCari = (EditText) findViewById(R.id.edtCari);
        btnTambahkan = (Button) findViewById(R.id.btnTambahkan);
        listKontak = (ListView) findViewById(R.id.listKontak);
        spinGrup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadKontak();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    kontakAdapter.filter(edtCari.getText().toString().trim());
                    listKontak.invalidate();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (dataIdGrup.length > 0) {
                    loadKontak();
                }
            }
        });
        btnTambahkan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONArray exclude = new JSONArray();
                for (int i = 0; i < dataKontak.size(); i++) {
                    if (dataKontak.get(i).isCheckbox() == false) {
                        exclude.put(Integer.parseInt(dataKontak.get(i).getId()));
                    }
                }
                if (dataGrup.length > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("exclude", exclude.toString());
                    intent.putExtra("group_name", dataGrup[spinGrup.getSelectedItemPosition()]);
                    intent.putExtra("group_id", dataIdGrup[spinGrup.getSelectedItemPosition()]);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(PilihGrupKontakActivity.this, "Data grup tidak tersedia", Toast.LENGTH_SHORT).show();
                }

            }
        });
        loadGrup();
    }

    private void loadGrup() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LIST_GRUP)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.show();
        dataGrup = new String[]{};
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        dataGrup = new String[data.length()];
                        dataIdGrup = new String[data.length()];
                        for (int i = 0; i < data.length(); i++) {
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            final String description = data.getJSONObject(i).getString("description");
                            dataGrup[i] = name;
                            dataIdGrup[i] = id;
                        }
                    } else {
                        new AlertDialog.Builder(PilihGrupKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    displayGrup();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(PilihGrupKontakActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(PilihGrupKontakActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new android.app.AlertDialog.Builder(PilihGrupKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(PilihGrupKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(PilihGrupKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));

                        new AlertDialog.Builder(PilihGrupKontakActivity.this)
                                .setMessage(msg)
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
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void displayGrup() {
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.item_spinner, dataGrup);
        spinGrup.setAdapter(arrayAdapter);
    }

    private void loadKontak() {
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("groupId", dataIdGrup[spinGrup.getSelectedItemPosition()]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LIST_CONTACT_GROUP)
                .buildUpon()
                .toString();
        dataKontak.clear();
        swipe_refresh.setRefreshing(true);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            final String phone = data.getJSONObject(i).getString("phone");

                            dataKontak.add(new ItemKontak(id, name, phone, true, true, data.getJSONObject(i)));

                        }
                        displayKontak();
                    } else {
                        new AlertDialog.Builder(PilihGrupKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(PilihGrupKontakActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(PilihGrupKontakActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new android.app.AlertDialog.Builder(PilihGrupKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(PilihGrupKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(PilihGrupKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));

                        new AlertDialog.Builder(PilihGrupKontakActivity.this)
                                .setMessage(msg)
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
                header.put("x-api-key", token);
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
        kontakAdapter = new AdapterKontak(dataKontak, this);
        listKontak.setAdapter(kontakAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
