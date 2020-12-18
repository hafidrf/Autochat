package id.co.kamil.autochat.ui.grup;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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
import id.co.kamil.autochat.ui.kontak.LihatKontakActivity;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_CONTACT_FROM_GROUP;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_CONTACT_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class ListGrupKontakActivity extends AppCompatActivity {

    private static final String TAG = "ListGrupKontak";
    private static final int REQUEST_EDIT_GROUP = 100;
    private static final int REQUEST_ADD = 101;
    private String idGrup;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private AdapterKontak kontakAdapter;
    private ListView listKontak;
    private EditText edtCari;
    private Button btnEditGrup;
    private JSONObject dataJsonGrup;
    private String name;
    private String description;
    private TextView txtGrup, txtDeskripsi;
    private Menu menuTop;
    private boolean adapterInstance = false;
    private JSONArray customers_id;
    private boolean is_default = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_grup_kontak);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detail Grup");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);
        listKontak = (ListView) findViewById(R.id.listKontak);
        edtCari = (EditText) findViewById(R.id.edtCari);
        txtGrup = (TextView) findViewById(R.id.txtGrup);
        txtDeskripsi = (TextView) findViewById(R.id.txtDeskripsi);
        btnEditGrup = (Button) findViewById(R.id.btnEditGrup);
        btnEditGrup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListGrupKontakActivity.this, FormGrupActivity.class);
                intent.putExtra("tipe", "edit");
                try {
                    intent.putExtra("id", dataJsonGrup.getString("id"));
                    intent.putExtra("data", dataJsonGrup.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, REQUEST_EDIT_GROUP);
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
        idGrup = getIntent().getStringExtra("id");
        final String tmpData = getIntent().getStringExtra("data");
        try {
            dataJsonGrup = new JSONObject(tmpData);
            customers_id = new JSONArray();
            is_default = dataJsonGrup.getBoolean("default");
            name = dataJsonGrup.getString("name");
            description = dataJsonGrup.getString("description");
            txtGrup.setText(name);
            txtDeskripsi.setText(description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (is_default) {
            btnEditGrup.setEnabled(false);
        } else {
            btnEditGrup.setEnabled(true);
        }
        listKontak.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0) {
                    Intent intent = new Intent(ListGrupKontakActivity.this, LihatKontakActivity.class);
                    intent.putExtra("id", dataKontak.get(position).getId());
                    startActivityForResult(intent, REQUEST_ADD);
                }
            }
        });
        loadKontak();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance) {
            listDefault();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void listDefault() {
        for (int i = 0; i < dataKontak.size(); i++) {
            ItemKontak ikontak = dataKontak.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataKontak.set(i, ikontak);
        }
        kontakAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuTop = menu;
        getMenuInflater().inflate(R.menu.kontak, menuTop);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.actTambah) {
            Intent i = new Intent(this, FormTambahKontakGrupActivity.class);
            i.putExtra("exclude_customers", customers_id.toString());
            i.putExtra("dataJson", dataJsonGrup.toString());
            startActivityForResult(i, REQUEST_ADD);
        } else if (item.getItemId() == R.id.actEdit) {
            menuTop.findItem(R.id.actBatal).setVisible(true);
            menuTop.findItem(R.id.actHapus).setVisible(true);
            menuTop.findItem(R.id.actSemua).setVisible(true);
            menuTop.findItem(R.id.actEdit).setVisible(false);
            menuTop.findItem(R.id.actTambah).setVisible(false);
            for (int i = 0; i < dataKontak.size(); i++) {
                ItemKontak ikontak = dataKontak.get(i);
                ikontak.setChkvisible(!ikontak.isChkvisible());
                dataKontak.set(i, ikontak);
            }
            kontakAdapter.notifyDataSetChanged();
        } else if (item.getItemId() == R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            listDefault();
        } else if (item.getItemId() == R.id.actHapus) {
            new AlertDialog.Builder(this)
                    .setMessage("Apakah anda yakin akan mengeluarkan kontak berikut di grup ini?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusKontakGrup();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();

        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataKontak.size(); i++) {
                ItemKontak ikontak = dataKontak.get(i);
                ikontak.setCheckbox(true);
                dataKontak.set(i, ikontak);
            }
            kontakAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hapusKontakGrup() {
        JSONArray listHapusKontak = new JSONArray();
        for (int i = 0; i < dataKontak.size(); i++) {
            if (dataKontak.get(i).isCheckbox()) {
                final JSONObject tmpObject = dataKontak.get(i).getJsonObject();
                try {
                    final String id_grup_kontak = tmpObject.getString("id_grup_kontak");
                    Log.e(TAG, "idGrupKontak:" + id_grup_kontak);
                    listHapusKontak.put(id_grup_kontak);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", listHapusKontak);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_HAPUS_CONTACT_FROM_GROUP)
                .buildUpon()
                .toString();

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
                        Toast.makeText(ListGrupKontakActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        loadKontak();

                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        listDefault();
                        //finish();
                    } else {
                        new AlertDialog.Builder(ListGrupKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ListGrupKontakActivity.this)
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
                    errorResponse(ListGrupKontakActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(ListGrupKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(ListGrupKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(ListGrupKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(ListGrupKontakActivity.this)
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
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void loadKontak() {
        dataKontak.clear();
        customers_id = new JSONArray();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("groupId", idGrup);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LIST_CONTACT_GROUP)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        dataKontak.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            final String id_kontak = data.getJSONObject(i).getString("id");
                            final String first_name = data.getJSONObject(i).getString("first_name");
                            final String last_name = data.getJSONObject(i).getString("last_name");
                            final String phone_kontak = data.getJSONObject(i).getString("phone");
                            final String nama_kontak = first_name + " " + last_name;
                            customers_id.put(id_kontak);
                            dataKontak.add(new ItemKontak(id_kontak, nama_kontak, phone_kontak, false, data.getJSONObject(i)));
                        }
                        Log.i(TAG, "data:" + data.toString());
                    } else {
                        new AlertDialog.Builder(ListGrupKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    displayKontak();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ListGrupKontakActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(ListGrupKontakActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                        .show();
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

    private void displayKontak() {
        kontakAdapter = new AdapterKontak(dataKontak, this);
        listKontak.setAdapter(kontakAdapter);
        adapterInstance = true;
    }

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_GROUP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        } else if (requestCode == REQUEST_ADD) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                loadKontak();
            }
        }
    }
}
