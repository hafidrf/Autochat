package id.co.kamil.autochat.ui.kontak;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import static id.co.kamil.autochat.utils.API.URL_POST_IMPORT_CONTACT;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class ImporKontakActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private static final String TAG = "ImporKontakActivity";
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private EditText edtCari;
    private ListView listKontak;
    private AdapterKontak kontakAdapter;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private Button btnImpor;
    private CheckBox chkSemua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impor_kontak);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Import Kontak");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        btnImpor = (Button) findViewById(R.id.btnImport);
        chkSemua = (CheckBox) findViewById(R.id.chkSemua);

        pDialog = new ProgressDialog(this);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        edtCari = (EditText) findViewById(R.id.edtCari);
        listKontak = (ListView) findViewById(R.id.listKontak);
        listKontak.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent();
//                intent.putExtra("title",dataKontak.get(i).getJudul());
//                intent.putExtra("nomor",dataKontak.get(i).getNomorhp());
//                setResult(RESULT_OK,intent);
//                finish();
            }
        });
        chkSemua.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < kontakAdapter.listKontak.size(); i++) {
                    kontakAdapter.listKontak.get(i).setCheckbox(isChecked);
                    for (int a = 0; a < kontakAdapter.arraylist.size(); a++) {
                        if (kontakAdapter.arraylist.get(a).getId().equals(kontakAdapter.listKontak.get(i).getId())) {
                            kontakAdapter.arraylist.get(a).setCheckbox(isChecked);
                        }
                    }
                }
                kontakAdapter.notifyDataSetChanged();
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
        btnImpor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chk = 0;
                for (int i = 0; i < kontakAdapter.arraylist.size(); i++) {
                    if (kontakAdapter.arraylist.get(i).isCheckbox()) {
                        chk++;
                    }
                }
                if (chk == 0) {
                    new AlertDialog.Builder(ImporKontakActivity.this)
                            .setMessage("Tidak ada kontak yang dipilih")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    new AlertDialog.Builder(ImporKontakActivity.this)
                            .setMessage("Apakah anda yakin akan impor " + chk + " kontak ke Database Wabot?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    simpan();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();
                }

            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                loadKontak();

            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadKontak();
            }
        });
    }

    private void loadKontak() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            swipe_refresh.setRefreshing(true);
            dataKontak.clear();
            String selection = String.format("%s > 0", ContactsContract.Contacts.HAS_PHONE_NUMBER);
            // Permission has already been granted
            ContentResolver cr = getContentResolver();
            Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, selection, null, null);
            if (cursor != null) {
                try {
                    final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    String name, number;
                    int i = 0;
                    while (cursor.moveToNext()) {
                        name = cursor.getString(nameIndex);
                        number = cursor.getString(numberIndex);
                        dataKontak.add(new ItemKontak(String.valueOf(i), name, number, false, true, null));
                        i++;
                    }
                } finally {
                    cursor.close();
                }
            }

            swipe_refresh.setRefreshing(false);
            displayKontak();
            chkSemua.callOnClick();
        }

    }

    private void simpan() {
        final JSONArray jsonPhone = new JSONArray();
        final JSONArray jsonName = new JSONArray();
        for (int i = 0; i < kontakAdapter.arraylist.size(); i++) {
            if (kontakAdapter.arraylist.get(i).isCheckbox()) {
                final String phone = kontakAdapter.arraylist.get(i).getNomorhp();
                final String name = kontakAdapter.arraylist.get(i).getJudul();
                jsonPhone.put(phone);
                jsonName.put(name);
            }
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("phone", jsonPhone);
            requestBody.put("name", jsonName);
            requestBody.put("cust_group", userDetail.get(KEY_CUST_GROUP));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_IMPORT_CONTACT)
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

                    if (status) {
                        new AlertDialog.Builder(ImporKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        new AlertDialog.Builder(ImporKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(ImporKontakActivity.this)
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
                    errorResponse(ImporKontakActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new androidx.appcompat.app.AlertDialog.Builder(ImporKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(ImporKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(ImporKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(ImporKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
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
