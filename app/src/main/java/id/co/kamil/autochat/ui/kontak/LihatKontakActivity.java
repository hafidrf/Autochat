package id.co.kamil.autochat.ui.kontak;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.ItemGrup;
import id.co.kamil.autochat.adapter.ItemRecyclerKontak;
import id.co.kamil.autochat.adapter.RecyclerKontakAdapter;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_CONTACT_ID;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GRUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.formateDateFromstring;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class LihatKontakActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT = 100;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final String TAG = "LihatKontakActivity";
    private TextView txtNama, txtPhone, txtGender, txtTglLahir, txtAlamat, txtKota, txtNote;
    private RecyclerView recycleView;
    private List<ItemRecyclerKontak> list = new ArrayList<>();
    private LinearLayout layPhone, layAlamat, layGender, layTglLahir, layKota, layNote;
    private TextView txtOlshop;
    private LinearLayout layOlshop;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private JSONObject dataJson = new JSONObject();
    private String idKontak;
    private LinearLayout layGroup;
    private ProgressDialog pDialog;
    private List<ItemGrup> dataGrup = new ArrayList<>();
    private JSONArray itemGroup;
    private TextView txtGrup;
    private ImageView imgWA, imgClipboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lihat_kontak);
        getSupportActionBar().setTitle("Lihat Kontak");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        imgWA = (ImageView) findViewById(R.id.imgWA);
        imgClipboard = (ImageView) findViewById(R.id.imgClipboard);
        txtNama = (TextView) findViewById(R.id.txtNama);
        txtPhone = (TextView) findViewById(R.id.txtPhone);
        txtGender = (TextView) findViewById(R.id.txtGender);
        txtTglLahir = (TextView) findViewById(R.id.txtTglLahir);
        txtAlamat = (TextView) findViewById(R.id.txtAlamat);
        txtKota = (TextView) findViewById(R.id.txtKota);
        txtNote = (TextView) findViewById(R.id.txtNote);
        txtOlshop = (TextView) findViewById(R.id.txtOlshop);
        txtGrup = (TextView) findViewById(R.id.txtGrup);

        layPhone = (LinearLayout) findViewById(R.id.layPhone);
        layAlamat = (LinearLayout) findViewById(R.id.layAlamat);
        layGender = (LinearLayout) findViewById(R.id.layGender);
        layKota = (LinearLayout) findViewById(R.id.layKota);
        layNote = (LinearLayout) findViewById(R.id.layNote);
        layTglLahir = (LinearLayout) findViewById(R.id.layTglLahir);
        layOlshop = (LinearLayout) findViewById(R.id.layOlshop);
        layGroup = (LinearLayout) findViewById(R.id.layGroup);

        recycleView = (RecyclerView) findViewById(R.id.recyclerView);

        final String tmpData = getIntent().getStringExtra("data");
        idKontak = getIntent().getStringExtra("id");
        if (tmpData == null){
            loadKontakId(idKontak);
        }else{
            displayData(tmpData);
        }

        imgWA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = txtPhone.getText().toString();
                phone = phone.replace(" ","");
                phone = phone.replace("-","");
                phone = phone.replace("08","628");
                phone = phone.replace("+62","62");

                String url = "https://api.whatsapp.com/send?phone=" + phone;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        imgClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setClipboard(LihatKontakActivity.this, txtPhone.getText().toString());
                Toast.makeText(LihatKontakActivity.this, "berhasil disalin", Toast.LENGTH_SHORT).show();

            }
        });
        txtPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone = txtPhone.getText().toString();
                phone = phone.replace(" ","");
                phone = phone.replace("-","");
                phone = phone.replace("08","628");

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                if (ActivityCompat.checkSelfPermission(LihatKontakActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(LihatKontakActivity.this,
                            android.Manifest.permission.CALL_PHONE)) {
                    } else {
                        ActivityCompat.requestPermissions(LihatKontakActivity.this,
                                new String[]{android.Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    }
                }else{
                    startActivity(callIntent);
                }

            }
        });


    }

    private void loadKontakId(String idKontak) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("contact_id",idKontak);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_LIST_CONTACT_ID)
                .buildUpon()
                .toString();

        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG,"idKontak:" + idKontak);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        displayData(data.getJSONObject(0).toString());
                    }else{
                        new AlertDialog.Builder(LihatKontakActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(LihatKontakActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setCancelable(false)
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
                    errorResponse(LihatKontakActivity.this,error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new androidx.appcompat.app.AlertDialog.Builder(LihatKontakActivity.this)
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(LihatKontakActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            }else{
                                new AlertDialog.Builder(LihatKontakActivity.this)
                                        .setMessage(msg)
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

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(LihatKontakActivity.this)
                                .setMessage(msg)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                                .setCancelable(false)
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
    }

    private void displayData(String tmpData) {
        try {
            final JSONObject data = new JSONObject(tmpData);
            dataJson = data;

            list.clear();
            String itemEmail = data.getString("email");
            String itemTelegram = data.getString("telegram");
            String itemWeb = data.getString("web");
            String itemFb = data.getString("facebook");
            String itemIG = data.getString("instagram");
            String itemLinkedin = data.getString("linkedin");
            String itemTokopedia = data.getString("tokopedia");
            String itemBL = data.getString("bukalapak");
            String itemShopee = data.getString("shopee");
            String itemOlshop = data.getString("olshop_id");
            String itemNamaDepan = data.getString("first_name");
            String itemNamaBelakang = data.getString("last_name");
            String itemSapaan = data.getString("sapaan");
            String itemPhone = data.getString("phone");
            String itemGender = data.getString("gender");
            String itemTglLahir = data.getString("date_of_birth");
            String itemAlamat = data.getString("address");
            String itemKota = data.getString("city");
            String itemNote = data.getString("note");
            final String tmpGroup = data.getString("customer_groups");
            Log.i(TAG,"json:" + data.toString());
            itemGroup = new JSONArray(tmpGroup);

            itemTglLahir = formateDateFromstring("yyyy-MM-dd HH:mm:ss", "dd-MM-yyyy", itemTglLahir);
            if (itemNamaDepan.equals("null") || itemNamaDepan.equals(null)) {
                itemNamaDepan = "";
            }
            if (itemNamaBelakang.equals("null") || itemNamaBelakang.equals(null)) {
                itemNamaBelakang = "";
            }
            if (itemEmail.equals("null") || itemEmail.equals(null)) {
                itemEmail = "";
            }

            if (itemPhone.equals("null") || itemPhone.equals(null)) {
                itemPhone = "";
            }
            if (itemSapaan.equals("null") || itemSapaan.equals(null)) {
                itemSapaan = "";
            }
            if (itemTelegram.equals("null") || itemTelegram.equals(null)) {
                itemTelegram = "";
            }
            if (itemAlamat.equals("null") || itemAlamat.equals(null)) {
                itemAlamat = "";
            }
            if (itemWeb.equals("null") || itemWeb.equals(null)) {
                itemWeb = "";
            }
            if (itemFb.equals("null") || itemFb.equals(null)) {
                itemFb = "";
            }
            if (itemIG.equals("null") || itemIG.equals(null)) {
                itemIG = "";
            }
            if (itemLinkedin.equals("null") || itemLinkedin.equals(null)) {
                itemLinkedin = "";
            }
            if (itemTokopedia.equals("null") || itemTokopedia.equals(null)) {
                itemTokopedia = "";
            }
            if (itemBL.equals("null") || itemBL.equals(null)) {
                itemBL = "";
            }
            if (itemShopee.equals("null") || itemShopee.equals(null)) {
                itemShopee = "";
            }
            if (itemOlshop.equals("null") || itemOlshop.equals(null)) {
                itemOlshop = "";
            }
            if (itemNote.equals("null") || itemNote.equals(null)) {
                itemNote = "";
            }
            if (itemKota.equals("null") || itemKota.equals(null)) {
                itemKota = "";
            }
            txtNama.setText(itemSapaan + " " + itemNamaDepan + " " + itemNamaBelakang);
            txtPhone.setText(itemPhone);
            txtGender.setText(itemGender);
            txtTglLahir.setText(itemTglLahir);
            txtAlamat.setText(itemAlamat);
            txtKota.setText(itemKota);
            txtNote.setText(itemNote);
            txtOlshop.setText(itemOlshop);

            if (TextUtils.isEmpty(itemPhone) || itemPhone.equals(null) || itemPhone.equals("null")) {
                layPhone.setVisibility(View.GONE);
            } else {
                layPhone.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(itemGender) || itemGender.equals(null) || itemGender.equals("null")) {
                layGender.setVisibility(View.GONE);
            } else {
                layGender.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(itemTglLahir) || itemTglLahir.equals(null) || itemTglLahir.equals("null")) {
                layTglLahir.setVisibility(View.GONE);
            } else {
                layTglLahir.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(itemAlamat) || itemAlamat.equals(null) || itemAlamat.equals("null")) {
                layAlamat.setVisibility(View.GONE);
            } else {
                layAlamat.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(itemKota) || itemKota.equals(null) || itemKota.equals("null")) {
                layKota.setVisibility(View.GONE);
            } else {
                layKota.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(itemNote) || itemNote.equals(null) || itemNote.equals("null")) {
                layNote.setVisibility(View.GONE);
            } else {
                layNote.setVisibility(View.VISIBLE);
            }

            if (TextUtils.isEmpty(itemOlshop) || itemOlshop.equals(null) || itemOlshop.equals("null")) {
                layOlshop.setVisibility(View.GONE);
            } else {
                layOlshop.setVisibility(View.VISIBLE);
            }

            if (!TextUtils.isEmpty(itemEmail) || itemEmail.equals("null") || itemEmail.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_gmail, itemEmail, "email", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_gmail_disabled, itemEmail, "email", false));

            }
            if (!TextUtils.isEmpty(itemTelegram) || itemTelegram.equals("null") || itemTelegram.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_telegram, itemTelegram, "telegram", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_telegram_disabled, itemTelegram, "url", false));

            }
            if (!TextUtils.isEmpty(itemWeb) || itemWeb.equals("null") || itemWeb.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_browser, itemWeb, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_browser_disabled, itemWeb, "url", false));
            }

            if (!TextUtils.isEmpty(itemFb) || itemFb.equals("null") || itemFb.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_facebook, itemFb, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_facebook_disabled, itemFb, "url", false));
            }

            if (!TextUtils.isEmpty(itemIG) || itemIG.equals("null") || itemIG.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_instagram, itemIG, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_instagram_disabled, itemIG, "url", false));
            }

            if (!TextUtils.isEmpty(itemLinkedin) || itemLinkedin.equals("null") || itemLinkedin.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_linkedin, itemLinkedin, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_linkedin_disabled, itemLinkedin, "url", false));
            }

            if (!TextUtils.isEmpty(itemBL) || itemBL.equals("null") || itemBL.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_bukalapak, itemBL, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_bukalapak_disabled, itemBL, "url", false));
            }
            if (!TextUtils.isEmpty(itemTokopedia) || itemTokopedia.equals("null") || itemTokopedia.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_tokopedia, itemTokopedia, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_tokopedia_disabled, itemTokopedia, "url", false));
            }
            if (!TextUtils.isEmpty(itemShopee) || itemShopee.equals("null") || itemShopee.equals(null)) {
                list.add(new ItemRecyclerKontak(R.drawable.ic_shopee, itemShopee, "url", true));
            } else {
                list.add(new ItemRecyclerKontak(R.drawable.ic_shopee_disabled, itemShopee, "url", false));
            }
            if (list.size() > 0) {
                recycleView.setVisibility(View.VISIBLE);
            } else {
                recycleView.setVisibility(View.GONE);
            }
            if (itemGroup.length() > 0) {
                layGroup.setVisibility(View.VISIBLE);
            } else {
                layGroup.setVisibility(View.GONE);
            }
            final RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            new LinearSnapHelper().attachToRecyclerView(recyclerView);
            recycleView.setAdapter(new RecyclerKontakAdapter(list, this, new RecyclerKontakAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(final ItemRecyclerKontak item) {
                    if (item.isEnabled()) {
                        new AlertDialog.Builder(LihatKontakActivity.this)
                                .setMessage(item.getLink())
                                .setPositiveButton("Buka Link", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (item.getTipe().equals("email")) {
                                            String mailto = "mailto:" + item.getLink() +
                                                    "?cc=" +
                                                    "&subject=" +
                                                    "&body=";

                                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                            emailIntent.setData(Uri.parse(mailto));

                                            try {
                                                startActivity(emailIntent);
                                            } catch (ActivityNotFoundException e) {
                                                //TODO: Handle case where no email app is available
                                            }
                                        }else if(item.getTipe().equals("telegram")){
                                            String url = "";
                                            if (item.getLink().substring(0, 4).equals("http")) {
                                                url = item.getLink();
                                            } else {
                                                url = "http://t.me/" + item.getLink();
                                            }
                                            Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                            intent2.setData(Uri.parse(url));
                                            startActivity(intent2);
                                        } else {
                                            String url = "";
                                            if (item.getLink().substring(0, 4).equals("http")) {
                                                url = item.getLink();
                                            } else {
                                                url = "http://" + item.getLink();
                                            }
                                            Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                            intent2.setData(Uri.parse(url));
                                            startActivity(intent2);
                                        }
                                    }
                                })
                                .setNegativeButton("Batal", null)
                                .show();
                    }


                }
            }));
            displayGrup();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayGrup() {
        String grup = "";
        if (itemGroup.length()>0){
            for(int i = 0;i<itemGroup.length();i++){
                try {
                    grup += itemGroup.getJSONObject(i).getString("name") + ", ";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        txtGrup.setText(grup);
    }

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_only,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.actEdit){
            Intent i = new Intent(this,FormKontakActivity.class);
            i.putExtra("tipe","edit");
            i.putExtra("id",idKontak);
            i.putExtra("data",dataJson.toString());
            startActivityForResult(i,REQUEST_EDIT);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_EDIT){
            if (resultCode==RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
