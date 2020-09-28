package id.co.kamil.autochat.ui.waform;

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
import id.co.kamil.autochat.adapter.AdapterWaformData;
import id.co.kamil.autochat.adapter.ItemWaformData;
import id.co.kamil.autochat.utils.ExpandableHeightListView;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CHECK_SUBDOMAIN_WAFORM;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_WAFORM;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_WAFORM;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_WAFORM;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class WaformActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_FIELD = 100;
    private static final String TAG = "WaformActivity";
    private String tipeForm;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private Button btnTambahField;
    private EditText edtNama;
    private ExpandableHeightListView listWaform;
    private Button btnSimpan;
    private AdapterWaformData adapterWaformData;
    private List<ItemWaformData> dataField = new ArrayList<>();
    private String idWaform;
    private EditText edtRedaksi, edtDomain, edtSubmitText, edtDestNumber, edtDestName;
    private String lastSubdomain = "";
    private TextView txtLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waform);

        getSupportActionBar().setTitle("Form WA Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tipeForm = getIntent().getStringExtra("tipe");
        idWaform = getIntent().getStringExtra("id");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);

        btnTambahField = (Button) findViewById(R.id.btnTambah);
        txtLoading = (TextView) findViewById(R.id.loading);
        edtNama = (EditText) findViewById(R.id.edtNama);
        edtRedaksi = (EditText) findViewById(R.id.edtRedaksi);
        edtDomain = (EditText) findViewById(R.id.edtDomain);
        edtSubmitText = (EditText) findViewById(R.id.edtSubmitText);
        edtDestNumber = (EditText) findViewById(R.id.edtDestNumber);
        edtDestName = (EditText) findViewById(R.id.edtDestName);
        listWaform = (ExpandableHeightListView) findViewById(R.id.listField);

        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()) {
                    simpanWaform();
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
                boolean exist = true;
                int id = 0;
                do {
                    id++;
                    if (dataField.size() <= 0) {
                        exist = false;
                    } else {
                        for (int x = 0; x < dataField.size(); x++) {
                            //Log.e(TAG,dataField.get(x).getId() + "=" + id + " ? " + dataField.get(x).getId().equals(String.valueOf(id)));
                            if (dataField.get(x).getId().equals(String.valueOf(id))) {
                                exist = true;
                                break;
                            } else {
                                exist = false;
                            }
                        }
                    }
                } while (exist == true);
                Intent i = new Intent(WaformActivity.this, WaformAddFieldActivity.class);
                i.putExtra("tipe", "add");
                i.putExtra("id", String.valueOf(id));
                startActivityForResult(i, REQUEST_ADD_FIELD);

            }
        });
        listWaform.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (view.getId() == R.id.btnHapus) {
                    new AlertDialog.Builder(WaformActivity.this)
                            .setMessage("Apakah anda yakin akan menghapus item tersebut?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dataField.remove(position);
                                    adapterWaformData.notifyDataSetChanged();
                                    Toast.makeText(WaformActivity.this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();

                } else if (view.getId() == R.id.btnEdit) {
                    String placeholder = "";
                    String typeField = "";
                    boolean required = false;
                    JSONObject attr = dataField.get(position).getAttr();
                    JSONArray list = new JSONArray();
                    try {
                        String t_required = attr.getString("required");
                        if (t_required.isEmpty()) {
                            required = false;
                        } else if (Integer.parseInt(t_required) == 1) {
                            required = true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (dataField.get(position).getType().equals("input")) {
                        try {
                            placeholder = attr.getString("placeholder");
                            typeField = attr.getString("type");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (dataField.get(position).getList() != null) {
                        list = dataField.get(position).getList();
                    }
                    Intent i = new Intent(WaformActivity.this, WaformAddFieldActivity.class);
                    i.putExtra("tipe", "edit");
                    i.putExtra("type", dataField.get(position).getType());
                    i.putExtra("label", dataField.get(position).getLabel());
                    i.putExtra("id", dataField.get(position).getId());
                    i.putExtra("placeholderField", placeholder);
                    i.putExtra("requiredField", required);
                    i.putExtra("inputTypeField", typeField);
                    i.putExtra("positionList", position);
                    i.putExtra("listCombo", list.toString());
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
        String uri = Uri.parse(URL_POST_CHECK_SUBDOMAIN_WAFORM)
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
                    errorResponse(WaformActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(WaformActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(WaformActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(WaformActivity.this)
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

    private void getData() {
        dataField.clear();

        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", idWaform);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GET_WAFORM)
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
                        final String judul = data.getString("title");
                        final String redaksi = data.getString("redaksi");
                        final String submit_text = data.getString("submit_text");
                        final String dest_number = data.getString("dest_number");
                        final String dest_name = data.getString("dest_name");
                        final String domain = data.getString("domain");
                        final JSONArray field = data.getJSONArray("field");

                        lastSubdomain = domain;
                        edtNama.setText(judul);
                        edtRedaksi.setText(redaksi);
                        edtSubmitText.setText(submit_text);
                        edtDestNumber.setText(dest_number);
                        edtDestName.setText(dest_name);
                        edtDomain.setText(domain);
                        for (int i = 0; i < field.length(); i++) {
                            final String id = field.getJSONObject(i).getString("id");
                            final String type = field.getJSONObject(i).getString("type");
                            final String label = field.getJSONObject(i).getString("label");
                            JSONObject attr = new JSONObject();
                            if (field.getJSONObject(i).getJSONObject("attr") != null) {
                                attr = field.getJSONObject(i).getJSONObject("attr");
                            }
                            JSONArray list = new JSONArray();
                            if (field.getJSONObject(i).getJSONArray("list") != null) {
                                list = field.getJSONObject(i).getJSONArray("list");
                            }

                            dataField.add(new ItemWaformData(id, type, label, attr, list));

                        }
                        displayDataField();
                    } else {
                        new AlertDialog.Builder(WaformActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(WaformActivity.this)
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
                    errorResponse(WaformActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(WaformActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(WaformActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(WaformActivity.this)
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
                        new AlertDialog.Builder(WaformActivity.this)
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

    private void simpanWaform() {
        JSONArray field = new JSONArray();
        for (int i = 0; i < dataField.size(); i++) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("id", i + 1);
                obj.put("label", dataField.get(i).getLabel());
                obj.put("type", dataField.get(i).getType());
                obj.put("attr", dataField.get(i).getAttr());
                obj.put("list", dataField.get(i).getList());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            field.put(obj);
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            if (tipeForm.equals("edit")) {
                requestBody.put("id", idWaform);
            }
            requestBody.put("title", edtNama.getText().toString());
            requestBody.put("redaksi", edtRedaksi.getText().toString());
            requestBody.put("dest_number", edtDestNumber.getText().toString());
            requestBody.put("dest_name", edtDestName.getText().toString());
            requestBody.put("submit_text", edtSubmitText.getText().toString());
            requestBody.put("domain", edtDomain.getText().toString());
            requestBody.put("field", field);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = "";
        if (tipeForm.equals("edit")) {
            uri = Uri.parse(URL_POST_EDIT_WAFORM)
                    .buildUpon()
                    .toString();
        } else {
            uri = Uri.parse(URL_POST_CREATE_WAFORM)
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
                        Toast.makeText(WaformActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        new AlertDialog.Builder(WaformActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(WaformActivity.this)
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
                    errorResponse(WaformActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(WaformActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(WaformActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(WaformActivity.this)
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
                        new AlertDialog.Builder(WaformActivity.this)
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
        if (TextUtils.isEmpty(edtNama.getText())) {
            edtNama.setError("Field ini tidak boleh kosong");
            edtNama.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(edtRedaksi.getText())) {
            edtRedaksi.setError("Field ini tidak boleh kosong");
            edtRedaksi.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(edtDestNumber.getText())) {
            edtDestNumber.setError("Field ini tidak boleh kosong");
            edtDestNumber.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(edtDestName.getText())) {
            edtDestName.setError("Field ini tidak boleh kosong");
            edtDestName.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(edtSubmitText.getText())) {
            edtSubmitText.setError("Field ini tidak boleh kosong");
            edtSubmitText.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(edtDomain.getText())) {
            edtDomain.setError("Field ini tidak boleh kosong");
            edtDomain.requestFocus();
            return false;
        } else if (dataField.size() <= 0) {
            Toast.makeText(this, "Field WA Form tidak boleh kosong", Toast.LENGTH_SHORT).show();
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
                String tipeField = data.getStringExtra("type");
                String id = data.getStringExtra("id");
                String label = data.getStringExtra("label");
                String t_attr = data.getStringExtra("attr");
                String t_list = data.getStringExtra("list");
                int positionList = data.getIntExtra("positionList", 0);
                JSONObject attr = new JSONObject();
                try {
                    if (t_attr.isEmpty() == false && t_attr != null) {
                        attr = new JSONObject(t_attr);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray list = new JSONArray();
                try {
                    if (t_list.isEmpty() == false && t_list != null) {
                        list = new JSONArray(t_list);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (tipeForm.equals("add")) {

                    ItemWaformData itemFormField = new ItemWaformData(id, tipeField, label, attr, list);
                    Log.e(TAG, "item: " + itemFormField);
                    dataField.add(itemFormField);
                } else {
                    ItemWaformData itemFormField = new ItemWaformData(id, tipeField, label, attr, list);
                    Log.e(TAG, "item: " + itemFormField);
                    dataField.remove(positionList);
                    dataField.add(positionList, itemFormField);
                }
                displayDataField();
            }
        }
    }

    private void displayDataField() {
        adapterWaformData = new AdapterWaformData(this, dataField);
        listWaform.setAdapter(adapterWaformData);
        listWaform.setExpanded(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
