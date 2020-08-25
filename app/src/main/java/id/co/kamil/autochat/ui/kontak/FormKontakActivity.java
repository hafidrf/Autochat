package id.co.kamil.autochat.ui.kontak;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterGrup;
import id.co.kamil.autochat.adapter.AdapterRiwayatKontak;
import id.co.kamil.autochat.adapter.ItemGrup;
import id.co.kamil.autochat.adapter.ItemRiwayatKontak;
import id.co.kamil.autochat.ui.grup.CariGrupActivity;
import id.co.kamil.autochat.utils.ExpandableHeightListView;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_GET_KOTA;
import static id.co.kamil.autochat.utils.API.URL_GET_PROVINSI;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_DELETE_RIWAYAT;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_RIWAYAT;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.formateDateFromstring;


public class FormKontakActivity extends AppCompatActivity {

    private static final String TAG = "FormKontakActivity";
    private static final int REQUEST_GRUP = 100;
    private static final int REQUEST_ADD_CONTACT = 5;
    private static final int REQUEST_CONTACT = 6;
    private LinearLayout layUmum,layRiwayat;
    private RadioButton optUmum,optRiwayat;
    private String tipeForm;
    private EditText edtNamaDepan,edtEmail,edtPhone,edtTelegram,edtAlamat,edtWeb,edtFacebook,edtInstagram,edtLinkedin,edtBukalapak,edtTokopedia,edtShopee,edtOlshopid,edtTglLahir,edtNote;
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private List<String> riwayatUpdate = new ArrayList<>();
    private Spinner spinGender;
    private String[] dataGender = new String[]{"Laki-laki","Perempuan"};
    private Button btnSimpan;
    private JSONArray customers = new JSONArray();
    private String idKontak;
    private JSONObject dataJson;
    private int selectedYear,selectedMonth,selectedDay;
    private List<ItemRiwayatKontak> dataRiwayat = new ArrayList<>();
    private ExpandableHeightListView listRiwayat;
    private List<ItemRiwayatKontak> deleteRiwayat = new ArrayList<>();
    private Button btnTambahRiwayat;
    private EditText edtKomentar;
    private AdapterRiwayatKontak adapterRiwayat;
    private List<ItemGrup> dataGrup = new ArrayList<>();
    private AdapterGrup grupAdapter ;
    private boolean adapterInstance;
    private ExpandableHeightListView listGrup;
    private Button btnTambahGrup;
    private TextView labelNotfoundRiwayat;

    private List<String> dataIdProvinsi = new ArrayList<>();
    private List<String> dataNameProvinsi = new ArrayList<>();
    private List<String> dataIdKota = new ArrayList<>();
    private List<String> dataNameKota = new ArrayList<>();
    private String provinsiId = "";
    private Spinner spinKota;
    private String cityId = "";
    private Spinner spinProvinsi;
    private Spinner spinSapaan;
    private String[] dataSapaan = new String[]{"Mas","Mba","Sis","Bro","Bapak","Ibu","Kakak","Kak"};
    private EditText edtNamaBelakang;
    private ImageButton btnCariKontak;
    private CheckBox chkUcapanSelamat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_kontak);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        tipeForm = getIntent().getStringExtra("tipe");
        if (tipeForm.equals("add")){
            getSupportActionBar().setTitle("Tambah Kontak");
        }else{
            getSupportActionBar().setTitle("Edit Kontak");
            idKontak = getIntent().getStringExtra("id");

        }
        pDialog = new ProgressDialog(this);

        layUmum = (LinearLayout) findViewById(R.id.layUmum);
        layRiwayat = (LinearLayout) findViewById(R.id.layRiwayatKontak);
        listRiwayat = (ExpandableHeightListView) findViewById(R.id.listRiwayat);
        listGrup = (ExpandableHeightListView) findViewById(R.id.listGrup);
        labelNotfoundRiwayat = (TextView) findViewById(R.id.labelNotfoundRiwayat);

        chkUcapanSelamat = (CheckBox) findViewById(R.id.chkUltah);
        edtNamaDepan = (EditText) findViewById(R.id.edtNamaDepan);
        edtNamaBelakang = (EditText) findViewById(R.id.edtNamaBelakang);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPhone = (EditText) findViewById(R.id.edtHP);
        spinSapaan = (Spinner) findViewById(R.id.spinSapaan);
        edtTelegram = (EditText) findViewById(R.id.edtTelegram);
        edtAlamat = (EditText) findViewById(R.id.edtAlamat);
        edtWeb = (EditText) findViewById(R.id.edtWeb);
        edtFacebook = (EditText) findViewById(R.id.edtFacebook);
        edtInstagram = (EditText) findViewById(R.id.edtInstagram);
        edtLinkedin = (EditText) findViewById(R.id.edtLinkedin);
        edtBukalapak = (EditText) findViewById(R.id.edtBukalapak);
        edtTokopedia = (EditText) findViewById(R.id.edtTokopedia);
        edtShopee = (EditText) findViewById(R.id.edtShopee);
        edtOlshopid = (EditText) findViewById(R.id.edtOlshopid);
        spinGender = (Spinner) findViewById(R.id.spinGender);
        spinKota = (Spinner) findViewById(R.id.spinKota);
        spinProvinsi = (Spinner) findViewById(R.id.spinProvinsi);
        edtTglLahir = (EditText) findViewById(R.id.edtTglLahir);
        edtNote = (EditText) findViewById(R.id.edtNote);
        edtKomentar = (EditText) findViewById(R.id.edtKomentar);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnTambahGrup = (Button) findViewById(R.id.btnTambahGrup);
        btnTambahRiwayat = (Button) findViewById(R.id.tambahRiwayat);
        btnCariKontak = (ImageButton) findViewById(R.id.btnCariKontak);
        btnCariKontak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(FormKontakActivity.this,KontakLokalActivity.class),REQUEST_CONTACT);
            }
        });
        btnTambahRiwayat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edtKomentar.getText())){
                    edtKomentar.setError("Field ini tidak boleh kosong");
                    edtKomentar.requestFocus();
                }else{
                    ItemRiwayatKontak item = new ItemRiwayatKontak("",edtKomentar.getText().toString(),"add","pending");
                    deleteRiwayat.add(item);
                    dataRiwayat.add(item);
                    adapterRiwayat.notifyDataSetChanged();
                    edtKomentar.setText("");
                    edtKomentar.requestFocus();
                }
            }
        });
        optUmum = (RadioButton) findViewById(R.id.optUmum);
        optRiwayat = (RadioButton) findViewById(R.id.optRiwayat);

        optUmum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    layUmum.setVisibility(View.VISIBLE);
                    layRiwayat.setVisibility(View.GONE);
                }else{
                    layUmum.setVisibility(View.GONE);
                    layRiwayat.setVisibility(View.VISIBLE);
                }

            }
        });
        optRiwayat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    layUmum.setVisibility(View.GONE);
                    layRiwayat.setVisibility(View.VISIBLE);
                }else{
                    layUmum.setVisibility(View.VISIBLE);
                    layRiwayat.setVisibility(View.GONE);
                }

            }
        });
        if (optUmum.isChecked()){
            layUmum.setVisibility(View.VISIBLE);
            layRiwayat.setVisibility(View.GONE);
        }else{
            layUmum.setVisibility(View.GONE);
            layRiwayat.setVisibility(View.VISIBLE);
        }
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()){
                    simpan();
                }
            }
        });

        final ArrayAdapter sapaanAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataSapaan);
        spinSapaan.setAdapter(sapaanAdapter);

        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataGender);
        spinGender.setAdapter(arrayAdapter);

        if (tipeForm.equals("edit")){
            final String tmpData = getIntent().getStringExtra("data");
            try {
                dataJson = new JSONObject(tmpData);
                Log.i(TAG,dataJson.toString());
                String itemNamaDepan = dataJson.getString("first_name");
                String itemNamaBelakang = dataJson.getString("last_name");
                String itemEmail = dataJson.getString("email");
                String itemPhone = dataJson.getString("phone");
                String itemSapaan = dataJson.getString("sapaan");
                String itemTelegram = dataJson.getString("telegram");
                String itemAddress = dataJson.getString("address");
                String itemWeb = dataJson.getString("web");
                String itemFacebook = dataJson.getString("facebook");
                String itemInstagram = dataJson.getString("instagram");
                String itemLinkedin = dataJson.getString("linkedin");
                String itemTokopedia = dataJson.getString("tokopedia");
                String itemBukalapak = dataJson.getString("bukalapak");
                String itemShopee = dataJson.getString("shopee");
                String itemOlshop = dataJson.getString("olshop_id");
                String itemNote = dataJson.getString("note");
                String itemCity = dataJson.getString("id_city");
                String itemProvinsi = dataJson.getString("id_provinsi");
                String itemUltah = dataJson.getString("kirim_ultah");

                if (itemNamaDepan.equals("null") || itemNamaDepan.equals(null)){
                    itemNamaDepan = "";
                }
                if (itemNamaBelakang.equals("null") || itemNamaBelakang.equals(null)){
                    itemNamaBelakang = "";
                }
                if (itemEmail.equals("null") || itemEmail.equals(null)){
                    itemEmail = "";
                }

                if (itemPhone.equals("null") || itemPhone.equals(null)){
                    itemPhone = "";
                }
                if (itemSapaan.equals("null") || itemSapaan.equals(null)){
                    itemSapaan = "";
                }
                if (itemTelegram.equals("null") || itemTelegram.equals(null)){
                    itemTelegram = "";
                }
                if (itemAddress.equals("null") || itemAddress.equals(null)){
                    itemAddress = "";
                }
                if (itemWeb.equals("null") || itemWeb.equals(null)){
                    itemWeb = "";
                }
                if (itemFacebook.equals("null") || itemFacebook.equals(null)){
                    itemFacebook = "";
                }
                if (itemInstagram.equals("null") || itemInstagram.equals(null)){
                    itemInstagram = "";
                }
                if (itemLinkedin.equals("null") || itemLinkedin.equals(null)){
                    itemLinkedin = "";
                }
                if (itemTokopedia.equals("null") || itemTokopedia.equals(null)){
                    itemTokopedia = "";
                }
                if (itemBukalapak.equals("null") || itemBukalapak.equals(null)){
                    itemBukalapak = "";
                }
                if (itemShopee.equals("null") || itemShopee.equals(null)){
                    itemShopee = "";
                }
                if (itemOlshop.equals("null") || itemOlshop.equals(null)){
                    itemOlshop = "";
                }
                if (itemNote.equals("null") || itemNote.equals(null)){
                    itemNote = "";
                }
                if (itemCity.equals("null") || itemCity.equals(null)){
                    itemCity = "";
                }
                if (itemProvinsi.equals("null") || itemProvinsi.equals(null)){
                    itemProvinsi = "";
                }
                if (itemUltah.equals("1")){
                    chkUcapanSelamat.setChecked(true);
                }else{
                    chkUcapanSelamat.setChecked(false);
                }
                edtNamaDepan.setText(itemNamaDepan);
                edtNamaBelakang.setText(itemNamaBelakang);
                edtEmail.setText(itemEmail);
                edtPhone.setText(itemPhone);
                for (int sp =0;sp<dataSapaan.length;sp++){
                    if (dataSapaan[sp].equals(itemSapaan)){
                        spinSapaan.setSelection(sp);
                    }
                }
                edtTelegram.setText(itemTelegram);
                edtAlamat.setText(itemAddress);
                edtWeb.setText(itemWeb);
                edtFacebook.setText(itemFacebook);
                edtInstagram.setText(itemInstagram);
                edtLinkedin.setText(itemLinkedin);
                edtTokopedia.setText(itemTokopedia);
                edtBukalapak.setText(itemBukalapak);
                edtShopee.setText(itemShopee);
                edtOlshopid.setText(itemOlshop);

                final String gender = dataJson.getString("gender");
                if (gender.toLowerCase().equals("laki-laki") || gender.equals("null") || gender.equals(null)){
                    spinGender.setSelection(0);
                }else{
                    spinGender.setSelection(1);
                }
                provinsiId = itemProvinsi;
                cityId = itemCity;

                final String tmptgl = dataJson.getString("date_of_birth");
                String date_after = formateDateFromstring("yyyy-MM-dd", "dd-MM-yyyy", tmptgl);

                edtTglLahir.setText(date_after);
                edtNote.setText(itemNote);
                customers = dataJson.getJSONArray("customer_groups");
                dataGrup.clear();
                for (int i = 0;i<customers.length();i++){
                    final String id = customers.getJSONObject(i).getString("id");
                    final String name = customers.getJSONObject(i).getString("name");
                    final String description = customers.getJSONObject(i).getString("description");
                    dataGrup.add(new ItemGrup(id,name,description,true,true));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        edtTglLahir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(FormKontakActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                final String month = String.valueOf(monthOfYear+1);
                                selectedYear = year;
                                selectedMonth = monthOfYear;
                                selectedDay = dayOfMonth;
                                edtTglLahir.setText(dayOfMonth + "-" + month + "-" + year);
                            }
                        }, selectedYear, selectedMonth, selectedDay);
                datePickerDialog.show();
            }
        });

        final Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR); // current year
        selectedMonth = c.get(Calendar.MONTH); // current month
        selectedDay = c.get(Calendar.DAY_OF_MONTH); // current day
        btnTambahGrup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"cust_groups:" + customers.toString());
                JSONArray exclude_group = new JSONArray();
                for (int i = 0;i<customers.length();i++ ){
                    try {
                        exclude_group.put(customers.getJSONObject(i).getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(FormKontakActivity.this, CariGrupActivity.class);
                intent.putExtra("exclude_group",exclude_group.toString());
                startActivityForResult(intent,REQUEST_GRUP);
            }
        });
        if (tipeForm.equals("edit")){
            loadRiwayat();
        }else{
            displayRiwayat();
        }
        listRiwayat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (view.getId()==R.id.btnHapus){
                    new AlertDialog.Builder(FormKontakActivity.this)
                            .setMessage("Apakah anda yakin akan menghapus item riwayat berikut ?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String historiId = dataRiwayat.get(position).getId();
                                    if (TextUtils.isEmpty(historiId)){
                                        dataRiwayat.remove(position);
                                        adapterRiwayat.notifyDataSetChanged();
                                    }else{
                                        hapus_riwayat(historiId);
                                    }
                                }
                            })
                            .setNegativeButton("Tidak",null)
                            .show();
                }
            }
        });
        spinProvinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                provinsiId = dataIdProvinsi.get(position);
                loadKota();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinKota.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cityId = dataIdKota.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadProvinsi();
        displayGrup();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_GRUP) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "OK");
                try {
                    final JSONObject dataJson = new JSONObject(data.getStringExtra("dataJson"));
                    if (dataJson.length() > 0) {
                        ItemGrup itemGrup = new ItemGrup();
                        itemGrup.setId(dataJson.getString("id"));
                        itemGrup.setJudul(dataJson.getString("judul"));
                        itemGrup.setDeskripsi(dataJson.getString("deskripsi"));
                        itemGrup.setCheckbox(true);
                        itemGrup.setChkvisible(true);
                        itemGrup.setJsonObject(dataJson);
                        dataGrup.add(itemGrup);
                        HashMap<String,String> dataItemGroup = new HashMap<>();
                        dataItemGroup.put("id",itemGrup.getId());
                        dataItemGroup.put("name",itemGrup.getJudul());
                        dataItemGroup.put("description",itemGrup.getDeskripsi());
                        JSONObject jsonObject = new JSONObject(dataItemGroup);
                        customers.put(jsonObject);
                    }
                    grupAdapter.notifyDataSetChanged();
                    grupAdapter.reloadArrayList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }else if(requestCode == REQUEST_CONTACT){
            if (resultCode==RESULT_OK){
                String number = data.getStringExtra("nomor")
                        .replace("-","")
                        .replace(" ","")
                        .replace("+62","62");
                edtPhone.setText(number);
            }
        }
    }

    private void loadProvinsi(){
        dataIdProvinsi.clear();
        dataNameProvinsi.clear();
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_GET_PROVINSI)
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
                        dataIdProvinsi.add("");
                        dataNameProvinsi.add("Pilih Provinsi");
                        final JSONArray data = response.getJSONArray("data");
                        for (int a = 0;a<data.length();a++){
                            final String id = data.getJSONObject(a).getString("provinsiId");
                            final String name = data.getJSONObject(a).getString("provinsiName");
                            dataIdProvinsi.add(id);
                            dataNameProvinsi.add(name);
                        }
                    }else{
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                    displayProvinsi();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormKontakActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(FormKontakActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }


            }
        }){
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

    private void displayProvinsi() {
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,dataNameProvinsi);
        spinProvinsi.setAdapter(arrayAdapter);
        if (tipeForm.equals("edit") && !provinsiId.isEmpty()){
            for (int i = 0;i<dataIdProvinsi.size();i++){
                if (provinsiId.equals(dataIdProvinsi.get(i))){
                    spinProvinsi.setSelection(i);
                    break;
                }
            }
        }else if(dataIdProvinsi.size()>0){
            provinsiId = dataIdProvinsi.get(0);
        }
        loadKota();
    }

    private void loadKota() {
        dataIdKota.clear();
        dataNameKota.clear();
        if (provinsiId.equals("") || provinsiId.isEmpty()){
            return;
        }
        HashMap<String,String> param = new HashMap<>();
        param.put("provinsiId",provinsiId);
        final JSONObject parameter = new JSONObject(param);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String uri = Uri.parse(URL_GET_KOTA)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        for (int a = 0;a<data.length();a++){
                            final String id = data.getJSONObject(a).getString("kotaId");
                            final String name = data.getJSONObject(a).getString("kotaName");
                            dataIdKota.add(id);
                            dataNameKota.add(name);
                        }
                        displayKota();
                    }else{
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormKontakActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(FormKontakActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }

            }
        }){
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

    private void displayKota() {
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item, dataNameKota);
        spinKota.setAdapter(arrayAdapter);
        if (tipeForm.equals("edit") && !cityId.isEmpty()){
            for (int i = 0;i<dataIdKota.size();i++){
                if (cityId.equals(dataIdKota.get(i))){
                    spinKota.setSelection(i);
                    break;
                }
            }
        }else if(dataIdKota.size()>0){
            cityId = dataIdKota.get(0);
        }
    }

    private void displayGrup() {
        grupAdapter = new AdapterGrup(dataGrup,this);
        listGrup.setAdapter(grupAdapter);
        listGrup.setExpanded(true);
        adapterInstance = true;
    }

    private void loadRiwayat() {
        dataRiwayat.clear();
        labelNotfoundRiwayat.setVisibility(View.VISIBLE);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("contactId",idKontak);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_LIST_RIWAYAT)
                .buildUpon()
                .toString();

        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG,requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        Log.i(TAG,"dataRiwayat : " + data.toString());
                        if (data.length()>0){
                            for (int i = 0 ;i<data.length();i++){
                                final String id = data.getJSONObject(i).getString("id");
                                final String komentar = data.getJSONObject(i).getString("comment");
                                dataRiwayat.add(new ItemRiwayatKontak(id,komentar,"-","-"));
                            }
                            labelNotfoundRiwayat.setVisibility(View.GONE);
                        }
                    }else{
                        labelNotfoundRiwayat.setVisibility(View.VISIBLE);
                        labelNotfoundRiwayat.setText(message);

                    }
                    displayRiwayat();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormKontakActivity.this)
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
                    errorResponse(FormKontakActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }


            }
        }){
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

    private void displayRiwayat() {
        adapterRiwayat = new AdapterRiwayatKontak(this,R.layout.item_riwayat_kontak,dataRiwayat);
        listRiwayat.setAdapter(adapterRiwayat);
        listRiwayat.setExpanded(true);
        if (dataRiwayat.size()>0){
            labelNotfoundRiwayat.setVisibility(View.GONE);
        }else{
            labelNotfoundRiwayat.setVisibility(View.VISIBLE);
        }
    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtNamaDepan.getText())){
            edtNamaDepan.setError("Field ini tidak boleh kosong");
            edtNamaDepan.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(edtPhone.getText())){
            edtPhone.setError("Field ini tidak boleh kosong");
            edtPhone.requestFocus();
            return false;
        }
        if(!Patterns.PHONE.matcher(edtPhone.getText().toString()).matches()){
            edtPhone.setError("Format No Salah");
            edtPhone.requestFocus();
            return false;
        }

        return true;
    }

    private void simpan(){
        String ucapan_ultah = "0";
        if (chkUcapanSelamat.isChecked()) ucapan_ultah = "1";
        final JSONArray jsonArray = new JSONArray();

        for (int i=0;i<dataRiwayat.size();i++){
            final String statusRiwayat = dataRiwayat.get(i).getStatus();
            final String actionRiwayat = dataRiwayat.get(i).getAction();
            final String id = dataRiwayat.get(i).getId();
            final String komentar = dataRiwayat.get(i).getKomentar();
            if (statusRiwayat.equals("pending") && actionRiwayat.equals("add")){
                jsonArray.put(komentar);
            }
        }
        Log.i(TAG,"dataR:"+dataRiwayat.toString());
        Log.i(TAG,"riwayat:" + jsonArray.toString());
        final JSONArray customer_group = new JSONArray();
        for(int i=0;i<dataGrup.size();i++){
            if (dataGrup.get(i).isCheckbox()){
                customer_group.put(Integer.parseInt(dataGrup.get(i).getId()));
            }
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("first_name", edtNamaDepan.getText().toString());
            requestBody.put("last_name", edtNamaBelakang.getText().toString());
            requestBody.put("email",edtEmail.getText().toString());
            requestBody.put("phone",edtPhone.getText().toString());
            requestBody.put("customer_group",customer_group);
            requestBody.put("sapaan",dataSapaan[spinSapaan.getSelectedItemPosition()]);
            requestBody.put("telegram",edtTelegram.getText().toString());
            requestBody.put("address",edtAlamat.getText().toString());
            requestBody.put("web",edtWeb.getText().toString());
            requestBody.put("facebook",edtFacebook.getText().toString());
            requestBody.put("instagram",edtInstagram.getText().toString());
            requestBody.put("linkedin",edtLinkedin.getText().toString());
            requestBody.put("tokopedia",edtTokopedia.getText().toString());
            requestBody.put("bukalapak",edtBukalapak.getText().toString());
            requestBody.put("shopee",edtShopee.getText().toString());
            requestBody.put("olshop_id",edtOlshopid.getText().toString());
            requestBody.put("gender",dataGender[spinGender.getSelectedItemPosition()]);
            requestBody.put("city",cityId);
            requestBody.put("date_of_birth",edtTglLahir.getText().toString());
            requestBody.put("note",edtNote.getText().toString());
            requestBody.put("histori_kontak",jsonArray);
            requestBody.put("ucapan_ultah",ucapan_ultah);
            if (tipeForm.equals("edit")){
                requestBody.put("id",idKontak);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = "";
        if (tipeForm.equals("add")){
            uri = Uri.parse(URL_POST_CREATE_CONTACT)
                    .buildUpon()
                    .toString();
        }else{
            uri = Uri.parse(URL_POST_EDIT_CONTACT)
                    .buildUpon()
                    .toString();
        }
        Log.i(TAG,"idKontak : " + idKontak + ";url:" + uri + ", " + requestBody.toString());

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
                        final String contactId = response.getString("id");
                        idKontak = contactId;
                        setResult(RESULT_OK);
                        Toast.makeText(FormKontakActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (contactExists(FormKontakActivity.this,edtPhone.getText().toString())==false){
                            saveLocalContact();
                        }
                        finish();
                    }else{
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormKontakActivity.this)
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
                    errorResponse(FormKontakActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }


            }
        }){
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
    public boolean contactExists(Context context, String number) {
/// number is the phone number
        String selection = String.format("%s > 0", ContactsContract.Contacts.HAS_PHONE_NUMBER);
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME };
        Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, selection, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        }
        finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
    private void saveLocalContact() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, edtNamaDepan.getText().toString()) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, edtPhone.getText().toString()) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
        try
        {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        }
        catch (RemoteException e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (OperationApplicationException e)
        {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private boolean hapus_riwayat(String id_riwayat) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("historiId",id_riwayat);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_DELETE_RIWAYAT)
                .buildUpon()
                .toString();

        pDialog.setMessage("Sedang menghapus...");
        pDialog.setCancelable(false);
        pDialog.show();
        final boolean[] returnRiwayat = {false};
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        returnRiwayat[0] = true;
                        Toast.makeText(FormKontakActivity.this, message, Toast.LENGTH_SHORT).show();
                    }else{
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormKontakActivity.this)
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
                    errorResponse(FormKontakActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(FormKontakActivity.this)
                                        .setMessage(msg)
                                        .setPositiveButton("OK",null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                }


            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("X-API-KEY",token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
        return returnRiwayat[0];
    }


    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
