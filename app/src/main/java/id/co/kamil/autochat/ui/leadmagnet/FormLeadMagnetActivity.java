package id.co.kamil.autochat.ui.leadmagnet;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

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

import java.io.File;
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
import static id.co.kamil.autochat.utils.API.URL_POST_CHECK_SUBDOMAIN_LEAD;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_LEAD_MAGNET;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_LEAD_MAGNET;
import static id.co.kamil.autochat.utils.API.URL_POST_LEAD_MAGNET_BY_ID;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GRUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.getFileExtension;

public class FormLeadMagnetActivity extends AppCompatActivity {

    private static final String TAG = "FormLeadMagnet";
    private static final int REQUEST_FIELD = 100;
    private static final int LOAD_IMAGE_RESULT = 101;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private String tipeForm;
    private TextView txtLoading;
    private EditText edtNama, edtSubdomain, edtSubmitText, edtRespon;
    private Button btnSimpan;
    private String idShorten;
    private String lastSubdomain;
    private Spinner spinStatus;
    private Spinner spinGroup;
    private String[] arrKodeGrup;
    private String[] arrStatus = {"tidak aktif", "aktif"};
    private String[] arrNamaGrup;
    private RecylerTagAdapter adapterTag;
    private List<ItemRecyclerTag> listField = new ArrayList<>();
    private RecyclerView recyclerView;
    private String url;
    private ImageButton btnField;
    private TextView txtSapaan, txtNamaBelakang, txtNamaDepan;
    private ImageView imgPesan;
    private ImageButton btnBrowse, btnHapus;
    private boolean imageSelect = false;
    private String imagePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_lead_magnet);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        tipeForm = getIntent().getStringExtra("tipe");

        if (tipeForm.equals("edit")) {
            getSupportActionBar().setTitle("Edit Lead Magnet");
        } else {
            getSupportActionBar().setTitle("Tambah Lead Magnet");
        }
        imgPesan = (ImageView) findViewById(R.id.imgPesan);
        btnBrowse = (ImageButton) findViewById(R.id.btnBrowse);
        btnHapus = (ImageButton) findViewById(R.id.btnHapus);

        txtSapaan = (TextView) findViewById(R.id.txtSapaan);
        txtNamaBelakang = (TextView) findViewById(R.id.txtNamaBelakang);
        txtNamaDepan = (TextView) findViewById(R.id.txtNamaDepan);

        txtSapaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtRespon.getText().insert(edtRespon.getSelectionStart(), "[sapaan] ");
            }
        });

        txtNamaDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtRespon.getText().insert(edtRespon.getSelectionStart(), "[nama_depan] ");
            }
        });
        txtNamaBelakang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtRespon.getText().insert(edtRespon.getSelectionStart(), "[nama_belakang] ");
            }
        });
        txtLoading = (TextView) findViewById(R.id.loading);
        edtNama = (EditText) findViewById(R.id.edtNama);
        edtSubdomain = (EditText) findViewById(R.id.edtSubdomain);
        edtSubmitText = (EditText) findViewById(R.id.edtSubmitText);
        edtRespon = (EditText) findViewById(R.id.edtRespon);
        spinStatus = (Spinner) findViewById(R.id.spinStatus);
        spinGroup = (Spinner) findViewById(R.id.spinGrup);
        btnField = (ImageButton) findViewById(R.id.btnField);
        btnField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONArray exclude = new JSONArray();
                for (int i = 0; i < listField.size(); i++) {
                    exclude.put(listField.get(i).getId());
                }
                Intent i = new Intent(FormLeadMagnetActivity.this, FormFieldActivity.class);
                i.putExtra("exclude", exclude.toString());
                startActivityForResult(i, REQUEST_FIELD);
            }
        });
        edtSubdomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (tipeForm.equals("add")) {
                    checkAvailable(edtSubdomain.getText().toString());
                } else if (lastSubdomain.equals(edtSubdomain.getText().toString())) {
                    txtLoading.setText("Subdomain Tersedia");
                    txtLoading.setTextColor(Color.BLUE);
                } else {
                    checkAvailable(edtSubdomain.getText().toString());
                }
            }
        });
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGalleryPhoto();
            }
        });
        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgPesan.setImageDrawable(getDrawable(R.drawable.ic_image));
                imagePath = null;
                imageSelect = false;

            }
        });
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        loadGrup();
        final ArrayAdapter statusAdapter = new ArrayAdapter(this, R.layout.item_spinner, arrStatus);
        spinStatus.setAdapter(statusAdapter);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtSubmitText.getText().toString().trim())) {
                    edtSubmitText.setError("Field ini tidak boleh kosong");
                    edtSubmitText.requestFocus();
                } else {
                    simpan();
                }
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.gridField);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setMinimumHeight((int) convertDpToPixel(35, this));
        //recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        if (tipeForm.equals("add")) {
            displayField();
        }

    }

    public void callGalleryPhoto() {

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOAD_IMAGE_RESULT);
                callGalleryPhoto();
            } else {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, LOAD_IMAGE_RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGrup() {
        arrKodeGrup = new String[1];
        arrNamaGrup = new String[1];
        arrKodeGrup[0] = "";
        arrNamaGrup[0] = "Pilih Grup";
        final RequestQueue requestQueue = Volley.newRequestQueue(FormLeadMagnetActivity.this);
        final String uri = Uri.parse(URL_POST_LIST_GRUP)
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

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        arrKodeGrup = new String[data.length() + 1];
                        arrNamaGrup = new String[data.length() + 1];
                        arrKodeGrup[0] = "";
                        arrNamaGrup[0] = "Pilih Grup";
                        for (int i = 0; i < data.length(); i++) {
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            final String description = data.getJSONObject(i).getString("description");
                            arrKodeGrup[i + 1] = id;
                            arrNamaGrup[i + 1] = name;
                        }

                    } else {
                        new AlertDialog.Builder(FormLeadMagnetActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                    displayGrup();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormLeadMagnetActivity.this)
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
                if (response.statusCode == 403) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.data.toString());
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")) {
                            new androidx.appcompat.app.AlertDialog.Builder(FormLeadMagnetActivity.this)
                                    .setMessage("Session telah habias / telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormLeadMagnetActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(FormLeadMagnetActivity.this)
                                    .setMessage(msg)
                                    .setPositiveButton("OK", null)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    final String msg = getResources().getString(errorResponse(error));

                    new AlertDialog.Builder(FormLeadMagnetActivity.this)
                            .setMessage(msg)
                            .setPositiveButton("OK", null)
                            .show();
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

    private void displayGrup() {
        final ArrayAdapter statusAdapter = new ArrayAdapter(this, R.layout.item_spinner, arrNamaGrup);
        spinGroup.setAdapter(statusAdapter);
        if (tipeForm.equals("edit")) {
            idShorten = getIntent().getStringExtra("id");
            Log.i(TAG, "idShorten:" + idShorten);
            getData();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (tipeForm.equals("edit")) {
            if (item.getItemId() == R.id.actShare) {
                try {
                    String konten = url;
                    String appId = getPackageName();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Lead Magnet");
                    String sAux = konten;
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Bagikan lewat"));
                } catch (Exception e) {
                    //e.toString();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAvailable(String subdomain) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("subdomain", subdomain);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_CHECK_SUBDOMAIN_LEAD)
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
                if (response.statusCode == 403) {
                    try {
                        JSONObject jsonObject = new JSONObject(new String(response.data));
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")) {
                            new AlertDialog.Builder(FormLeadMagnetActivity.this)
                                    .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormLeadMagnetActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        } else {
                            new AlertDialog.Builder(FormLeadMagnetActivity.this)
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
        listField.clear();
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", idShorten);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_LEAD_MAGNET_BY_ID)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG, requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONObject data = response.getJSONObject("data");
                        final String nama = data.getString("name");
                        final String domain = data.getString("domain");
                        final String subdomain = data.getString("sub_domain");
                        final String respon = data.getString("respon_msg");
                        final String submitText = data.getString("submit_text");
                        final String group_contact = data.getString("group_contact");
                        final String status_lm = data.getString("status");
                        final String img_hash = data.getString("img_hash");
                        final JSONArray field = data.getJSONArray("field");
                        for (int i = 0; i < field.length(); i++) {
                            listField.add(new ItemRecyclerTag(field.get(i).toString(), field.get(i).toString()));
                        }
                        if (!(group_contact.equals(null) || group_contact.equals("null") || group_contact == null)) {
                            for (int i = 0; i < arrKodeGrup.length; i++) {
                                if (arrKodeGrup[i].equals(group_contact)) {
                                    spinGroup.setSelection(i);
                                    break;
                                }
                            }
                        }
                        if (!(img_hash.equals(null) || img_hash == null || img_hash.equals("null"))) {
                            try {
                                imgPesan.setImageURI(Uri.parse(img_hash));
                                imagePath = img_hash;
                            } catch (Exception e) {

                            }
                        }
                        spinStatus.setSelection(Integer.parseInt(status_lm));
                        lastSubdomain = subdomain;
                        edtNama.setText(nama);
                        edtSubmitText.setText(submitText);
                        edtSubdomain.setText(subdomain);
                        edtRespon.setText(respon);
                        url = domain + subdomain;

                    } else {
                        new AlertDialog.Builder(FormLeadMagnetActivity.this)
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        finish();
                                    }
                                })
                                .show();
                    }
                    displayField();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormLeadMagnetActivity.this)
                            .setMessage(e.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
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
                Log.i(TAG, errorResponseString(error));
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(FormLeadMagnetActivity.this)
                        .setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .show();

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

    private void displayField() {
        adapterTag = new RecylerTagAdapter(listField, this, new RecylerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for (int i = 0; i < listField.size(); i++) {
                    if (listField.get(i).getId().equals(item.getId())) {
                        listField.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        recyclerView.setAdapter(adapterTag);
    }

    private void simpan() {
        JSONArray arrField = new JSONArray();
        if (listField.size() <= 0) {
            Toast.makeText(this, "Belum ada field yang dipilih", Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < listField.size(); i++) {
                arrField.put(listField.get(i).getId());
            }
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", edtNama.getText().toString());
            requestBody.put("sub_domain", edtSubdomain.getText().toString());
            requestBody.put("respon_msg", edtRespon.getText().toString());
            requestBody.put("submit_text", edtSubmitText.getText().toString());
            requestBody.put("status", spinStatus.getSelectedItemPosition());
            requestBody.put("field", arrField);
            requestBody.put("img_hash", imagePath);
            requestBody.put("group_contact", arrKodeGrup[spinGroup.getSelectedItemPosition()]);

            if (tipeForm.equals("edit")) {
                requestBody.put("id", idShorten);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = "";
        if (tipeForm.equals("add")) {
            uri = Uri.parse(URL_POST_CREATE_LEAD_MAGNET)
                    .buildUpon()
                    .toString();
        } else {
            uri = Uri.parse(URL_POST_EDIT_LEAD_MAGNET)
                    .buildUpon()
                    .toString();
        }

        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG, requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        setResult(RESULT_OK);
                        Toast.makeText(FormLeadMagnetActivity.this, message, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        new AlertDialog.Builder(FormLeadMagnetActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormLeadMagnetActivity.this)
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
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(FormLeadMagnetActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                        .show();

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

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (tipeForm.equals("edit")) {
            getMenuInflater().inflate(R.menu.lead_magnet, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FIELD) {
            if (resultCode == RESULT_OK) {
                String arr = data.getStringExtra("arrField");
                try {
                    JSONArray jsonArr = new JSONArray(arr);
                    listField.clear();
                    for (int i = 0; i < jsonArr.length(); i++) {
                        listField.add(new ItemRecyclerTag(jsonArr.getString(i), jsonArr.getString(i)));
                    }
                    displayField();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } else if (requestCode == LOAD_IMAGE_RESULT) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                String filePath = picturePath;
                Log.i(TAG, "File path" + filePath);
                File f = new File(picturePath);
                String exc = getFileExtension(f);
                if (exc.toLowerCase().equals("jpg") || exc.toLowerCase().equals("jpeg") || exc.toLowerCase().equals("png") || exc.toLowerCase().equals("bmp")) {
                    imgPesan.setImageURI(Uri.parse(filePath));
                    imageSelect = true;
                    imagePath = filePath;
                } else {
                    Toast.makeText(this, "Maaf, Tipe File tidak diizinkan", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
