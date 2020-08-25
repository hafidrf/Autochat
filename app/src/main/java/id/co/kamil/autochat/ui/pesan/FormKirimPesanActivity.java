package id.co.kamil.autochat.ui.pesan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

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
import id.co.kamil.autochat.bulksender.WASendService;
import id.co.kamil.autochat.ui.kontak.CariKontakActivity;
import id.co.kamil.autochat.ui.kontak.ExcludeContactActivity;
import id.co.kamil.autochat.ui.kontak.PilihGrupKontakActivity;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_PESAN_ANTRIAN_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_PESAN_ANTRIAN_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.getDirWabot;
import static id.co.kamil.autochat.utils.Utils.getFileExtension;

public class FormKirimPesanActivity extends AppCompatActivity {
    private static final int REQUEST_KONTAK = 100;
    private static final String TAG = "FormKirimPesanActivity";
    private static final int REQUEST_GRUP = 101;
    private static final int REQUEST_EXCLUDE = 102;
    private static final int LOAD_IMAGE_RESULT = 103;
    private static final int LOAD_IMAGE_RESULT_PROMOSI = 104;
    private String tipeForm;
    private TextView lblNomorTujuan;
    private Button btnKontakTdkDipilih;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private EditText edtIsiPesan;
    private ImageButton btnCariKontak;
    private Button btnSimpan;
    private RecylerTagAdapter adapterTag;
    private JSONArray excludeContact = new JSONArray();
    private List<ItemRecyclerTag> listNoTujuan = new ArrayList<>();
    private TextView txtWarnWA;
    private TextView txtSapaan,txtNamaDepan,txtNamaBelakang;
    private ImageView imgPesan;
    private ImageButton btnBrowse,btnHapus;
    private boolean imageSelect = false;
    private String imagePath = "";
    private String stringPesan = "",stringFilename = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_kirim_pesan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        tipeForm = getIntent().getStringExtra("tipe");
        stringPesan = getIntent().getStringExtra("pesan");
        stringFilename = getIntent().getStringExtra("filename");

        lblNomorTujuan = (TextView) findViewById(R.id.labelNomorTujuan);
        btnKontakTdkDipilih = (Button) findViewById(R.id.btnKontakTdkDipilih);
        if (tipeForm.equals("kontak")){
            getSupportActionBar().setTitle("Kirim Pesan berdasarkan Kontak");
            lblNomorTujuan.setText("Nomor Tujuan");
            btnKontakTdkDipilih.setVisibility(View.GONE);
        }else{
            getSupportActionBar().setTitle("Kirim Pesan berdasarkan Grup Kontak");
            lblNomorTujuan.setText("Nama Grup");
            btnKontakTdkDipilih.setVisibility(View.VISIBLE);
        }

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);

        imgPesan = (ImageView) findViewById(R.id.imgPesan);
        btnBrowse = (ImageButton) findViewById(R.id.btnBrowse);
        btnHapus = (ImageButton) findViewById(R.id.btnHapus);

        txtSapaan = (TextView) findViewById(R.id.txtSapaan);
        txtNamaBelakang = (TextView) findViewById(R.id.txtNamaBelakang);
        txtNamaDepan = (TextView) findViewById(R.id.txtNamaDepan);

        txtSapaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtIsiPesan.getText().insert(edtIsiPesan.getSelectionStart(),"[sapaan] " );
            }
        });

        txtNamaDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtIsiPesan.getText().insert(edtIsiPesan.getSelectionStart(),"[nama_depan] " );
            }
        });
        txtNamaBelakang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtIsiPesan.getText().insert(edtIsiPesan.getSelectionStart(),"[nama_belakang] " );
            }
        });
        txtWarnWA = (TextView) findViewById(R.id.txtWarningWhatsApp);
        edtIsiPesan = (EditText) findViewById(R.id.edtPesan);
        btnCariKontak = (ImageButton) findViewById(R.id.btnKontak);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()){
                    simpanPesanAntrian();
                }
            }
        });
        btnKontakTdkDipilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FormKirimPesanActivity.this, ExcludeContactActivity.class);
                intent.putExtra("contact_id",excludeContact.toString());
                startActivityForResult(intent,REQUEST_EXCLUDE);
            }
        });
        btnCariKontak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tipeForm.equals("grup")){
                    Intent i = new Intent(FormKirimPesanActivity.this, PilihGrupKontakActivity.class);
                    startActivityForResult(i,REQUEST_GRUP);
                }else{
                    Intent i = new Intent(FormKirimPesanActivity.this, CariKontakActivity.class);
                    i.putExtra("exclude",excludeContact.toString());
                    startActivityForResult(i,REQUEST_KONTAK);
                }
            }
        });


        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gridNoTujuan);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        adapterTag = new RecylerTagAdapter(listNoTujuan, this,new RecylerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for(int i=0;i<listNoTujuan.size();i++){
                    if (listNoTujuan.get(i).getId().equals(item.getId())){
                        listNoTujuan.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
                reloadExcludeContact();
            }
        });
        recyclerView.setAdapter(adapterTag);

        recyclerView.setMinimumHeight((int) convertDpToPixel(35,this));
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerView.setLayoutManager(layoutManager);
        if (isAccessibilityEnabled()==false){
            View v = (LinearLayoutCompat) findViewById(R.id.layMaster);
            showSnackBar(v);
        }
        reloadExcludeContact();
        if (appInstalledOrNot("com.whatsapp")){
            txtWarnWA.setVisibility(View.GONE);
        }else{
            txtWarnWA.setVisibility(View.VISIBLE);
        }

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
                imageSelect = false;

            }
        });

        if (stringPesan != null){
            if (stringPesan.isEmpty() == false){
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                edtIsiPesan.setText(stringPesan);
                if (stringFilename.isEmpty() == false && stringFilename != null){
                    String pathPicture = getDirWabot("template_promosi") + "/" + stringFilename;
                    File filePath = new File(pathPicture);
                    if(filePath.exists()){
                        Uri uri = Uri.fromFile(filePath);
                        if (checkPermissionGallery()){
                            imgPesan.setImageURI(uri);
                            imagePath = pathPicture;
                            imageSelect = true;
                        }
                    }

                }
            }
        }
    }
    public boolean checkPermissionGallery(){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, LOAD_IMAGE_RESULT_PROMOSI);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }
    public void callGalleryPhoto(){

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
    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
    private boolean isRequired() {
        if(listNoTujuan.size()<=0 && tipeForm.equals("grup")){
            Toast.makeText(this, "Grup belum dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }else if (excludeContact.length()<=0 && tipeForm.equals("kontak")){
            Toast.makeText(this, "Kontak belum dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(edtIsiPesan.getText().toString())){
            edtIsiPesan.setError("Field ini tidak boleh kosong");
            edtIsiPesan.requestFocus();
            return false;
        }
        return true;
    }

    public void showSnackBar(View llShow)
    {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Snackbar snackbar = Snackbar.make(llShow, "", Snackbar.LENGTH_INDEFINITE);
        // Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(0,0,0,0);
        // Hide the text
        TextView textView = (TextView) layout.findViewById(R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate our custom view
        View snackView = getLayoutInflater().inflate(R.layout.my_snackbar, null);
        // Configure the view
        TextView textViewOne = (TextView) snackView.findViewById(R.id.txtOne);
        TextView textMessage = (TextView) snackView.findViewById(R.id.txtMessage);
        textMessage.setText(getString(R.string.warning_disable_accessibility));
        textViewOne.setText(getString(R.string.snack_enable_service));
        textViewOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(i);
            }
        });

        ImageView imgClose = (ImageView) snackView.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        // Add the view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
    }
    private void simpanPesanAntrian(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        String url = "";
        try {
            requestBody.put("message",edtIsiPesan.getText().toString());
            if (imageSelect){
                requestBody.put("image_hash",imagePath);
            }
            if (tipeForm.equals("grup")){
                requestBody.put("groupId",listNoTujuan.get(0).getId());
                requestBody.put("exclude_contact",excludeContact);
                url = URL_POST_CREATE_PESAN_ANTRIAN_GROUP;
            }else{
                url = URL_POST_CREATE_PESAN_ANTRIAN_CONTACT;
                requestBody.put("contactId",excludeContact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(url)
                .buildUpon()
                .toString();
        Log.i(TAG,"body:" + requestBody);
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
                        Toast.makeText(FormKirimPesanActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(FormKirimPesanActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormKirimPesanActivity.this)
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
                if (response == null) {
                    errorResponse(FormKirimPesanActivity.this, error);
                }else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormKirimPesanActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormKirimPesanActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormKirimPesanActivity.this)
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
                        new AlertDialog.Builder(FormKirimPesanActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("x-api-key",token);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_KONTAK){
            if (resultCode==RESULT_OK){
                String dataId = data.getStringExtra("id");
                String dataTitle = data.getStringExtra("title");
                String dataNomor = data.getStringExtra("nomor");
                listNoTujuan.add(new ItemRecyclerTag(dataId,dataTitle));
                adapterTag.notifyDataSetChanged();
                reloadExcludeContact();
            }
        }else if(requestCode==REQUEST_GRUP){
            if (resultCode==RESULT_OK){
                String dataExclude = data.getStringExtra("exclude");
                String dataGroupName = data.getStringExtra("group_name");
                String dataGroupId = data.getStringExtra("group_id");
                try {
                    excludeContact = new JSONArray(dataExclude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listNoTujuan.add(new ItemRecyclerTag(dataGroupId,dataGroupName));
                adapterTag.notifyDataSetChanged();
                reloadExcludeContact();
            }
        }else if(requestCode== LOAD_IMAGE_RESULT) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                Log.e(TAG, "picturePath:" + picturePath);
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
        }else if(requestCode==LOAD_IMAGE_RESULT_PROMOSI){
            checkPermissionGallery();
        }
    }
    private void reloadExcludeContact(){
        if (tipeForm.equals("kontak")){
            excludeContact = new JSONArray();
            for(int i = 0; i< listNoTujuan.size();i++){
                excludeContact.put(listNoTujuan.get(i).getId());
            }

//            if (excludeContact.length()>0){
//                btnCariKontak.setEnabled(false);
//            }else{
//                btnCariKontak.setEnabled(true);
//            }
        }else{

            if (listNoTujuan.size()>0){
                btnCariKontak.setEnabled(false);
            }else{
                btnCariKontak.setEnabled(true);
            }
            if (excludeContact.length()>0){
                btnKontakTdkDipilih.setEnabled(true);
            }else{
                btnKontakTdkDipilih.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean isAccessibilityEnabled() {
        int enabled = 0;
        final String service = getPackageName() +"/"+ WASendService.class.getCanonicalName();

        try {
            enabled = Settings.Secure.getInt(getApplicationContext().getContentResolver()
                    , Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        if (enabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getApplicationContext().getContentResolver()
                    , Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (settingValue != null) {
                String[] values = settingValue.split(":");
                for (String s : values) {
                    if (s.equalsIgnoreCase(service))
                        return true;
                }
            }
        }

        return false;
    }
}
