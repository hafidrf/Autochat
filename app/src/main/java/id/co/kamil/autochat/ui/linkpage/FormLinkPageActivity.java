package id.co.kamil.autochat.ui.linkpage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import id.co.kamil.autochat.adapter.AdapterLinkpageData;
import id.co.kamil.autochat.utils.ExpandableHeightListView;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CHECK_SUBDOMAIN_LINKPAGE;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_LINKPAGE;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_LINKPAGE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class FormLinkPageActivity extends AppCompatActivity {

    private static final String TAG = "FormLinkPageActivity";
    private static final int REQUEST_ADD_FIELD = 100;
    private List<JSONObject> dataField = new ArrayList<>();

    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private Button btnTambahField;
    private TextView txtLoading;
    private EditText edtDomain;
    private ExpandableHeightListView listLinkpage;
    private Button btnSimpan;
    private String tipeForm = "add";
    private String lastSubdomain;
    private AdapterLinkpageData adapterLinkpageData;
    private String idLinkPage = "";
    private Menu menuTop;
    private String urlLinkPage;
    private EditText edtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_link_page);
        getSupportActionBar().setTitle("Form Linkpage");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idLinkPage = getIntent().getStringExtra("id");
        tipeForm = getIntent().getStringExtra("tipe");
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);

        btnTambahField = (Button) findViewById(R.id.btnTambah);
        txtLoading = (TextView) findViewById(R.id.loading);
        edtName = (EditText) findViewById(R.id.edtName);
        edtDomain = (EditText) findViewById(R.id.edtDomain);
        listLinkpage = (ExpandableHeightListView) findViewById(R.id.listField);

        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()) {
                    simpanLinkpage();
                }
            }
        });


        edtDomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (tipeForm.equals("add")) {
                    checkAvailable(edtDomain.getText().toString());
                } else if (lastSubdomain.equals(edtDomain.getText().toString())) {
                    txtLoading.setText("Subdomain Tersedia");
                    txtLoading.setTextColor(Color.BLUE);
                } else {
                    checkAvailable(edtDomain.getText().toString());
                }
            }
        });
        btnTambahField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FormLinkPageActivity.this, FormLinkPageAddFieldActivity.class);
                i.putExtra("tipe", "add");
                startActivityForResult(i, REQUEST_ADD_FIELD);

            }
        });
        listLinkpage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (view.getId() == R.id.btnHapus) {
                    new AlertDialog.Builder(FormLinkPageActivity.this)
                            .setMessage("Apakah anda yakin akan menghapus item tersebut?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dataField.remove(position);
                                    adapterLinkpageData.notifyDataSetChanged();
                                    Toast.makeText(FormLinkPageActivity.this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();

                } else if (view.getId() == R.id.btnEdit) {
                    Intent i = new Intent(FormLinkPageActivity.this, FormLinkPageAddFieldActivity.class);
                    i.putExtra("tipe", "edit");
                    try {
                        i.putExtra("judul", dataField.get(position).getString("judul"));
                        i.putExtra("link", dataField.get(position).getString("link"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, REQUEST_ADD_FIELD);
                }

            }
        });
        if (tipeForm.equals("edit")) {
            getData();
        }
    }

    private void checkAvailable(String subdomain) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("domain", subdomain);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_CHECK_SUBDOMAIN_LINKPAGE)
                .buildUpon()
                .toString();
        txtLoading.setText("Cek ketersediaan subdomain...");
        txtLoading.setTextColor(Color.GREEN);

        Log.i(TAG, requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        txtLoading.setTextColor(Color.BLUE);
                        txtLoading.setText(message);
                    } else {
                        txtLoading.setTextColor(Color.RED);
                        txtLoading.setText(message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    txtLoading.setTextColor(Color.RED);
                    txtLoading.setText(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(FormLinkPageActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormLinkPageActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormLinkPageActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormLinkPageActivity.this)
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
                        txtLoading.setTextColor(Color.RED);
                        txtLoading.setText(msg);
                        Log.i(TAG, errorResponseString(error));
                    }

                }


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                //header.put("Content-Type","multipart/form-data");
                header.put("x-api-key", token);
                return header;
            }

        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void visibleMenu(boolean visible) {
        if (menuTop != null) {
            menuTop.findItem(R.id.actBagikan).setVisible(visible);
            menuTop.findItem(R.id.actSalin).setVisible(visible);
            menuTop.findItem(R.id.actOpen).setVisible(visible);
        }
    }

    private void getData() {
        dataField.clear();
        visibleMenu(false);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", idLinkPage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_GET_LINKPAGE)
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
                        final JSONObject data = response.getJSONObject("data");
                        final String domain = data.getString("domain");
                        final String name = data.getString("name");
                        final JSONArray field = data.getJSONArray("data");
                        urlLinkPage = data.getString("url");
                        lastSubdomain = domain;
                        edtName.setText(name);
                        edtDomain.setText(domain);
                        for (int i = 0; i < field.length(); i++) {
                            final String judul = field.getJSONObject(i).getString("judul");
                            final String link = field.getJSONObject(i).getString("link");
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("judul", judul);
                            jsonObj.put("link", link);

                            dataField.add(jsonObj);

                        }
                        displayDataField();
                        visibleMenu(true);
                    } else {
                        new AlertDialog.Builder(FormLinkPageActivity.this)
                                .setMessage(message)
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
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                    visibleMenu(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                visibleMenu(false);
                Log.i(TAG, errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(FormLinkPageActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormLinkPageActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormLinkPageActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormLinkPageActivity.this)
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
                        new AlertDialog.Builder(FormLinkPageActivity.this)
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

    private void simpanLinkpage() {
        JSONArray field = new JSONArray();
        for (int i = 0; i < dataField.size(); i++) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("judul", dataField.get(i).getString("judul"));
                obj.put("link", dataField.get(i).getString("link"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            field.put(obj);
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", idLinkPage);
            requestBody.put("name", edtName.getText().toString());
            requestBody.put("domain", edtDomain.getText().toString());
            requestBody.put("data", field);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_CREATE_LINKPAGE)
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
                        Toast.makeText(FormLinkPageActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        new AlertDialog.Builder(FormLinkPageActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormLinkPageActivity.this)
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
                    errorResponse(FormLinkPageActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormLinkPageActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormLinkPageActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormLinkPageActivity.this)
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
                        new AlertDialog.Builder(FormLinkPageActivity.this)
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

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtDomain.getText())) {
            edtDomain.setError("Field ini tidak boleh kosong");
            edtDomain.requestFocus();
            return false;
        } else if (dataField.size() <= 0) {
            Toast.makeText(this, "Field Linkpage tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_FIELD) {
            if (resultCode == RESULT_OK) {
                String tipeForm = data.getStringExtra("tipeForm");
                String judul = data.getStringExtra("judul");
                String link = data.getStringExtra("link");
                int positionList = data.getIntExtra("positionList", 0);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("judul", judul);
                    jsonObject.put("link", link);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (tipeForm.equals("add")) {

                    dataField.add(jsonObject);
                } else {
                    dataField.remove(positionList);
                    dataField.add(positionList, jsonObject);
                }
                displayDataField();
            }
        }
    }

    private void displayDataField() {
        adapterLinkpageData = new AdapterLinkpageData(this, dataField);
        listLinkpage.setAdapter(adapterLinkpageData);
        listLinkpage.setExpanded(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.actOpen) {
            String url = urlLinkPage;
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setData(Uri.parse(url));
            startActivity(intent2);
        } else if (item.getItemId() == R.id.actSalin) {
            setClipboard(this, urlLinkPage);
            Toast.makeText(this, "berhasil disalin", Toast.LENGTH_SHORT).show();
        } else if (item.getItemId() == R.id.actBagikan) {
            String konten = urlLinkPage;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_TEXT, konten);
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, "Bagikan lewat"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuTop = menu;
        getMenuInflater().inflate(R.menu.linkpage, menuTop);
        return super.onCreateOptionsMenu(menu);
    }

}
