package id.co.kamil.autochat.ui.followup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterFollowupData;
import id.co.kamil.autochat.adapter.ItemFollowupData;
import id.co.kamil.autochat.adapter.ItemRecyclerTag;
import id.co.kamil.autochat.adapter.RecylerTagAdapter;
import id.co.kamil.autochat.ui.kontak.CariKontakActivity;
import id.co.kamil.autochat.utils.ExpandableHeightListView;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_FOLLOWUP;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_FOLLOWUP;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_FOLLOWUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class FormFollowupActivity extends AppCompatActivity {

    private static final int REQUEST_KONTAK = 100;
    private static final int REQUEST_GRUP = 101;
    private static final String TAG = "FormFollowupActivity";
    private static final int REQUEST_ADD_FOLLOWUP = 102;
    private String tipeForm;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private Button btnTambahFollowup;
    private ImageButton btnCariKontak;
    private Button btnSimpan;
    private RecylerTagAdapter adapterTag;
    private List<ItemRecyclerTag> listNoTujuan = new ArrayList<>();
    private EditText edtNama;
    private ExpandableHeightListView listFollowup;
    private List<ItemFollowupData> dataFollowup = new ArrayList<>();
    private JSONArray excludeContact = new JSONArray();
    private JSONArray excludeGroup = new JSONArray();
    private String idFollowup;
    private AdapterFollowupData adapterFollowupData;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_followup);

        getSupportActionBar().setTitle("Form Followup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tipeForm = getIntent().getStringExtra("tipe");
        idFollowup = getIntent().getStringExtra("id");

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);

        btnTambahFollowup = (Button) findViewById(R.id.btnTambah);

        edtNama = (EditText) findViewById(R.id.edtNama);
        listFollowup = (ExpandableHeightListView) findViewById(R.id.listFollowup);

        btnCariKontak = (ImageButton) findViewById(R.id.btnBrowse);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()) {
                    simpanFollowup();
                }
            }
        });
        btnCariKontak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] arr = {"Data Kontak", "Data Grup"};
                AlertDialog.Builder builder = new AlertDialog.Builder(FormFollowupActivity.this);
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case 0:
                                Intent i = new Intent(FormFollowupActivity.this, CariKontakActivity.class);
                                i.putExtra("exclude", excludeContact.toString());
                                startActivityForResult(i, REQUEST_KONTAK);
                                break;
                            case 1:
                                Intent intent = new Intent(FormFollowupActivity.this, PilihGrupActivity.class);
                                intent.putExtra("exclude", excludeGroup.toString());
                                startActivityForResult(intent, REQUEST_GRUP);
                                break;

                        }
                    }
                });
                builder.create();
                builder.show();
            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.gridContact);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        adapterTag = new RecylerTagAdapter(listNoTujuan, this, new RecylerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for (int i = 0; i < listNoTujuan.size(); i++) {
                    if (listNoTujuan.get(i).getId().equals(item.getId())) {
                        listNoTujuan.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
                reloadExcludeContact();

            }
        });
        recyclerView.setAdapter(adapterTag);
        recyclerView.setMinimumHeight((int) convertDpToPixel(35, this));
        reloadExcludeContact();
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerView.setLayoutManager(layoutManager);
        btnTambahFollowup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FormFollowupActivity.this, FormAddFollowupActivity.class);
                i.putExtra("tipe", "add");
                i.putExtra("positionList", 0);
                i.putExtra("interval_val", "0");
                i.putExtra("interval", dataFollowup.size() != 0);
                startActivityForResult(i, REQUEST_ADD_FOLLOWUP);

            }
        });
        listFollowup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (view.getId() == R.id.btnHapus) {
                    new AlertDialog.Builder(FormFollowupActivity.this)
                            .setMessage("Apakah anda yakin akan menghapus item tersebut?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dataFollowup.remove(position);
                                    adapterFollowupData.notifyDataSetChanged();
                                    Toast.makeText(FormFollowupActivity.this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();

                } else if (view.getId() == R.id.btnEdit) {
                    Intent i = new Intent(FormFollowupActivity.this, FormAddFollowupActivity.class);
                    i.putExtra("tipe", "edit");
                    i.putExtra("positionList", position);
                    i.putExtra("interval", position != 0);
                    i.putExtra("schedule", dataFollowup.get(position).getSchedule());
                    i.putExtra("interval_val", dataFollowup.get(position).getInterval());
                    i.putExtra("followup", dataFollowup.get(position).getMessage());
                    startActivityForResult(i, REQUEST_ADD_FOLLOWUP);
                }

            }
        });
        if (tipeForm.equals("edit")) {
            getData();
        }
        String contact_id = getIntent().getStringExtra("contact_id");
        String contact_title = getIntent().getStringExtra("contact_title");
        String contact_nomor = getIntent().getStringExtra("contact_nomor");

        if (contact_id != null) {
            String dataId = contact_id;
            String dataTitle = contact_title;
            String dataNomor = contact_nomor;
            listNoTujuan.add(new ItemRecyclerTag(dataId, dataTitle, "contact"));
            excludeContact.put(dataNomor);
            adapterTag.notifyDataSetChanged();
            reloadExcludeContact();
        }
    }

    private void getData() {
        dataFollowup.clear();
        listNoTujuan.clear();
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", idFollowup);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GET_FOLLOWUP)
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
                        final String name = data.getString("name");
                        final JSONArray contact = data.getJSONArray("contact");
                        final JSONArray detail = data.getJSONArray("detail");

                        edtNama.setText(name);
                        for (int i = 0; i < contact.length(); i++) {
                            final String contact_id = contact.getJSONObject(i).getString("id");
                            final String contact_name = contact.getJSONObject(i).getString("name");
                            final String tipe_contact = contact.getJSONObject(i).getString("tipe");

                            listNoTujuan.add(new ItemRecyclerTag(contact_id, contact_name, tipe_contact));

                        }
                        for (int i = 0; i < detail.length(); i++) {
                            final String detail_id = detail.getJSONObject(i).getString("id");
                            final String followup_name = detail.getJSONObject(i).getString("followup");
                            final String schedule = detail.getJSONObject(i).getString("schedule");
                            final String interval = detail.getJSONObject(i).getString("interval");

                            dataFollowup.add(new ItemFollowupData(detail_id, followup_name, schedule, interval));

                        }
                        adapterTag.notifyDataSetChanged();
                        reloadExcludeContact();
                        displayDataFollowUp();
                    } else {
                        new AlertDialog.Builder(FormFollowupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormFollowupActivity.this)
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
                    errorResponse(FormFollowupActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormFollowupActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormFollowupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormFollowupActivity.this)
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
                        new AlertDialog.Builder(FormFollowupActivity.this)
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

    private void simpanFollowup() {
        JSONArray groupSelected = new JSONArray();
        JSONArray contactSelected = new JSONArray();
        JSONArray schedule = new JSONArray();
        JSONArray followup = new JSONArray();
        JSONArray interval = new JSONArray();
        JSONArray id_detail = new JSONArray();
        for (int i = 0; i < listNoTujuan.size(); i++) {
            if (listNoTujuan.get(i).getTipe().equals("group")) {
                groupSelected.put(listNoTujuan.get(i).getId());
            } else {
                contactSelected.put(listNoTujuan.get(i).getId());
            }
        }
        for (int i = 0; i < dataFollowup.size(); i++) {
            schedule.put(dataFollowup.get(i).getSchedule());
            interval.put(dataFollowup.get(i).getInterval());
            followup.put(dataFollowup.get(i).getMessage());
            id_detail.put(dataFollowup.get(i).getId());
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            if (tipeForm.equals("edit")) {
                requestBody.put("id", idFollowup);
            }
            requestBody.put("name", edtNama.getText().toString());
            requestBody.put("schedule", schedule);
            requestBody.put("id_detail", id_detail);
            requestBody.put("followup", followup);
            requestBody.put("contact", contactSelected);
            requestBody.put("interval", interval);
            requestBody.put("group", groupSelected);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = "";
        if (tipeForm.equals("edit")) {
            uri = Uri.parse(URL_POST_EDIT_FOLLOWUP)
                    .buildUpon()
                    .toString();
        } else {
            uri = Uri.parse(URL_POST_CREATE_FOLLOWUP)
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
                        Toast.makeText(FormFollowupActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        new AlertDialog.Builder(FormFollowupActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormFollowupActivity.this)
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
                    errorResponse(FormFollowupActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormFollowupActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormFollowupActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormFollowupActivity.this)
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
                        new AlertDialog.Builder(FormFollowupActivity.this)
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

    private void reloadExcludeContact() {
        excludeContact = new JSONArray();
        excludeGroup = new JSONArray();
        for (int i = 0; i < listNoTujuan.size(); i++) {
            if (listNoTujuan.get(i).getTipe().equals("contact")) {
                excludeContact.put(listNoTujuan.get(i).getId());
            }
            if (listNoTujuan.get(i).getTipe().equals("group")) {
                excludeGroup.put(listNoTujuan.get(i).getId());
            }
        }

    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtNama.getText())) {
            edtNama.setError("Field ini tidak boleh kosong");
            edtNama.requestFocus();
            return false;
        } else if (listNoTujuan.size() <= 0) {
            Toast.makeText(this, "Nomor / Grup tujuan tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        } else if (dataFollowup.size() <= 0) {
            Toast.makeText(this, "Data Followup tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_KONTAK) {
            if (resultCode == RESULT_OK) {
                String dataId = data.getStringExtra("id");
                String dataTitle = data.getStringExtra("title");
                String dataNomor = data.getStringExtra("nomor");
                listNoTujuan.add(new ItemRecyclerTag(dataId, dataTitle, "contact"));
                excludeContact.put(dataNomor);
                adapterTag.notifyDataSetChanged();
                reloadExcludeContact();
            }
        } else if (requestCode == REQUEST_GRUP) {
            if (resultCode == RESULT_OK) {
                String dataGroupName = data.getStringExtra("group_name");
                String dataGroupId = data.getStringExtra("group_id");
                excludeGroup.put(dataGroupId);
                listNoTujuan.add(new ItemRecyclerTag(dataGroupId, dataGroupName, "group"));
                adapterTag.notifyDataSetChanged();
                reloadExcludeContact();
            }
        } else if (requestCode == REQUEST_ADD_FOLLOWUP) {
            if (resultCode == RESULT_OK) {
                String tipe = data.getStringExtra("tipe");
                String schedule = data.getStringExtra("schedule");
                String followup = data.getStringExtra("followup");
                String interval = data.getStringExtra("interval");
                Log.i(TAG, "interval:" + interval);
                int positionList = data.getIntExtra("positionList", 0);
                if (tipe.equals("add")) {
                    dataFollowup.add(new ItemFollowupData("", followup, schedule, interval));
                } else {
                    ItemFollowupData itemFollowup = dataFollowup.get(positionList);
                    dataFollowup.remove(positionList);
                    dataFollowup.add(positionList, new ItemFollowupData(itemFollowup.getId(), followup, schedule, interval));
                }
                displayDataFollowUp();
            }
        }
    }

    private void displayDataFollowUp() {
        adapterFollowupData = new AdapterFollowupData(this, dataFollowup);
        listFollowup.setAdapter(adapterFollowupData);
        listFollowup.setExpanded(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
