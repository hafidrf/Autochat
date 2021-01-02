package id.co.kamil.autochat.ui.grup;

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
import android.widget.EditText;
import android.widget.ListView;

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
import id.co.kamil.autochat.adapter.AdapterGrup;
import id.co.kamil.autochat.adapter.ItemGrup;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GRUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;


public class CariGrupActivity extends AppCompatActivity {

    private ListView listGrup;
    private EditText edtCari;
    private ProgressDialog pDialog;
    private List<ItemGrup> dataGrup = new ArrayList<>();
    private AdapterGrup grupAdapter;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private JSONArray excludeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cari_grup);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Grup Kontak");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        final String tmp_exclude = getIntent().getStringExtra("exclude_group");
        try {
            excludeGroup = new JSONArray(tmp_exclude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listGrup = (ListView) findViewById(R.id.listGrup);
        edtCari = (EditText) findViewById(R.id.edtCari);
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    grupAdapter.filter(edtCari.getText().toString().trim());
                    listGrup.invalidate();
                } catch (Exception e) {

                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listGrup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ItemGrup dataJson = dataGrup.get(i);
                Intent intent = new Intent();
                intent.putExtra("dataJson", dataJson.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        loadGrup();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGrup() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_POST_LIST_GRUP)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        dataGrup.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        boolean exclude = false;
                        for (int i = 0; i < data.length(); i++) {
                            exclude = false;
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            final String description = data.getJSONObject(i).getString("description");
                            final String total_member = data.getJSONObject(i).getString("total_member");
                            for (int a = 0; a < excludeGroup.length(); a++) {
                                if (excludeGroup.get(a).toString().equals(id)) {
                                    exclude = true;
                                    break;
                                }
                            }
                            if (!exclude) {
                                dataGrup.add(new ItemGrup(id, name, description, total_member, data.getJSONObject(i)));
                            }
                        }
                    } else {
                        new AlertDialog.Builder(CariGrupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    displayGrup();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(CariGrupActivity.this)
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
                    errorResponse(CariGrupActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(CariGrupActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(CariGrupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(CariGrupActivity.this)
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
                        new AlertDialog.Builder(CariGrupActivity.this)
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
                header.put("X-API-KEY", token);
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

    private void displayGrup() {
        grupAdapter = new AdapterGrup(dataGrup, this);
        listGrup.setAdapter(grupAdapter);
    }
}
