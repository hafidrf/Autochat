package id.co.kamil.autochat.ui.schedule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.ItemRecyclerTag;
import id.co.kamil.autochat.adapter.RecylerTagAdapter;
import id.co.kamil.autochat.ui.kontak.CariKontakActivity;
import id.co.kamil.autochat.ui.kontak.ExcludeContactActivity;
import id.co.kamil.autochat.ui.kontak.PilihGrupKontakActivity;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_JADWAL_BY_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_JADWAL_BY_GROUP;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_SCHEDULE;
import static id.co.kamil.autochat.utils.API.URL_POST_UPDATE_JADWAL_BY_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_UPDATE_JADWAL_BY_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.formateDateFromstring;
import static id.co.kamil.autochat.utils.Utils.getDirWabot;
import static id.co.kamil.autochat.utils.Utils.getFileExtension;

public class FormScheduleActivity extends AppCompatActivity {
    private static final int REQUEST_KONTAK = 100;
    private static final String TAG = "FormScheduleActivity";
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
    private String[] dataStatus = new String[]{"aktif","tidak aktif"};
    private String[] dataTipe = new String[]{"once (satu kali)","daily","weekly","monthly","annually"};
    private EditText edtJadwalKirim;
    private Spinner spinTipeJadwal,spinStatus;
    private boolean is_new;
    private EditText edtJadwalKirimSelanjutnya;
    private LinearLayout layJadwalNext;
    private String idSchedule;
    private TextView txtSapaan,txtNamaBelakang,txtNamaDepan;
    private ImageView imgPesan;
    private ImageButton btnBrowse;
    private ImageButton btnHapus;
    private String imagePath = null;
    private boolean imageSelect = false;
    private String stringPesan,stringFilename;
    private LinearLayout layHari,layTgl;
    private TextView labelJadwalKirim;
    private String[] dataHari = {"Minggu","Senin","Selasa","Rabu","Kamis","Jumat","Sabtu"};
    private String[] dataTgl;
    private Spinner spinHari,spinTgl;
    private boolean loadData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_schedule);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dataTgl = new String[28];
        for (int i=1;i<29;i++){
            dataTgl[i-1] = String.valueOf(i);
        }
        stringPesan = getIntent().getStringExtra("pesan");
        stringFilename = getIntent().getStringExtra("filename");
        is_new = getIntent().getBooleanExtra("is_new",false);
        tipeForm = getIntent().getStringExtra("tipe");
        lblNomorTujuan = (TextView) findViewById(R.id.labelNomorTujuan);
        btnKontakTdkDipilih = (Button) findViewById(R.id.btnKontakTdkDipilih);
        if (tipeForm.equals("kontak")){
            getSupportActionBar().setTitle("Jadwal Pesan berdasarkan Kontak");
            lblNomorTujuan.setText("Nomor Tujuan");
            btnKontakTdkDipilih.setVisibility(View.GONE);
        }else{
            getSupportActionBar().setTitle("Jadwal Pesan berdasarkan Grup Kontak");
            lblNomorTujuan.setText("Nama Grup");
            btnKontakTdkDipilih.setVisibility(View.VISIBLE);
        }

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        idSchedule = getIntent().getStringExtra("id");
        pDialog = new ProgressDialog(this);
        layJadwalNext = (LinearLayout) findViewById(R.id.layJadwalSelanjutnya);
        layHari = (LinearLayout) findViewById(R.id.layHari);
        layTgl = (LinearLayout) findViewById(R.id.layTgl);
        edtIsiPesan = (EditText) findViewById(R.id.edtPesan);
        edtJadwalKirim = (EditText) findViewById(R.id.edtJadwalKirim);
        edtJadwalKirimSelanjutnya = (EditText) findViewById(R.id.edtJadwalKirimSelanjutnya);
        spinTipeJadwal = (Spinner) findViewById(R.id.spinTipeJadwal);
        spinStatus = (Spinner) findViewById(R.id.spinStatus);
        spinHari = (Spinner) findViewById(R.id.spinHari);
        spinTgl = (Spinner) findViewById(R.id.spinTgl);
        txtSapaan = (TextView) findViewById(R.id.txtSapaan);
        txtNamaBelakang = (TextView) findViewById(R.id.txtNamaBelakang);
        txtNamaDepan = (TextView) findViewById(R.id.txtNamaDepan);
        labelJadwalKirim = (TextView) findViewById(R.id.labelJadwalKirim);

        imgPesan = (ImageView) findViewById(R.id.imgPesan);
        btnBrowse = (ImageButton) findViewById(R.id.btnBrowse);
        btnHapus = (ImageButton) findViewById(R.id.btnHapus);

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
        edtJadwalKirimSelanjutnya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                final String[] tempSelectDate = {""};
                DatePickerDialog datePickerDialog = new DatePickerDialog(FormScheduleActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                tempSelectDate[0] = year + "-" +  (monthOfYear + 1) + "-" + dayOfMonth;
                                Calendar mcurrentTime = Calendar.getInstance();
                                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                                final int minute = mcurrentTime.get(Calendar.MINUTE);
                                TimePickerDialog mTimePicker;
                                mTimePicker = new TimePickerDialog(FormScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                        tempSelectDate[0] = tempSelectDate[0] + " "+ selectedHour + ":" + selectedMinute;
                                        edtJadwalKirimSelanjutnya.setText(tempSelectDate[0]);
                                    }
                                }, hour, minute, true);//Yes 24 hour time
                                mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        //edtJadwalKirimSelanjutnya.setText(edtJadwalKirimSelanjutnya.getText().toString() + " " + hour + ":" + minute);
                                    }
                                });
                                mTimePicker.setTitle("Pilih Jam Jadwal Kirim");
                                mTimePicker.show();
                            }
                        }, year, month, day);
                //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
                datePickerDialog.show();
            }
        });
        spinTipeJadwal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                layHari.setVisibility(View.GONE);
                layTgl.setVisibility(View.GONE);
                if(spinTipeJadwal.getSelectedItemPosition()==3 || spinTipeJadwal.getSelectedItemPosition()==4){
                    labelJadwalKirim.setText("Jadwal Kirim");
                    edtJadwalKirim.setHint("Masukan Tgl dan Jam Kirim");
                }else if(spinTipeJadwal.getSelectedItemPosition()==0){
                    labelJadwalKirim.setText("Jadwal Kirim");
                    edtJadwalKirim.setHint("Masukan Jam Kirim");
                }else if(spinTipeJadwal.getSelectedItemPosition()==1){
                    layHari.setVisibility(View.VISIBLE);
                    labelJadwalKirim.setText("Jam Kirim");
                    edtJadwalKirim.setHint("Masukan Jam Kirim");
                }else{
                    layTgl.setVisibility(View.VISIBLE);
                    labelJadwalKirim.setText("Jam Kirim");
                    edtJadwalKirim.setHint("Masukan Jam Kirim");
                }
                if(loadData == false){
                    edtJadwalKirim.setText("");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        edtJadwalKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(spinTipeJadwal.getSelectedItemPosition()==0 || spinTipeJadwal.getSelectedItemPosition()==4){
                    pilihTglJam(edtJadwalKirim);
                }else if(spinTipeJadwal.getSelectedItemPosition()==1){
                    pilihJam(edtJadwalKirim);
                }else if(spinTipeJadwal.getSelectedItemPosition()==2){
                    pilihJam(edtJadwalKirim);
                }else{
                    pilihJam(edtJadwalKirim);
                }
            }
        });
        btnCariKontak = (ImageButton) findViewById(R.id.btnKontak);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRequired()){
                    simpanSchedule();
                }

            }
        });
        btnKontakTdkDipilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                JSONArray id_exclude_contact = new JSONArray();
                if (excludeContact.length()>0){
                    for(int i =0;i<excludeContact.length();i++){
                        try {
                            id_exclude_contact.put(excludeContact.getJSONObject(i).getString("contact_id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Intent intent = new Intent(FormScheduleActivity.this, ExcludeContactActivity.class);
                intent.putExtra("contact_id",id_exclude_contact.toString());
                startActivityForResult(intent,REQUEST_EXCLUDE);
            }
        });
        btnCariKontak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tipeForm.equals("grup")){
                    Intent i = new Intent(FormScheduleActivity.this, PilihGrupKontakActivity.class);
                    startActivityForResult(i,REQUEST_GRUP);
                }else{
                    Intent i = new Intent(FormScheduleActivity.this, CariKontakActivity.class);
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

        final ArrayAdapter tipeAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataTipe);
        spinTipeJadwal.setAdapter(tipeAdapter);
        final ArrayAdapter statusAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataStatus);
        spinStatus.setAdapter(statusAdapter);
        final ArrayAdapter hariAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataHari);
        spinHari.setAdapter(hariAdapter);
        final ArrayAdapter tglAdapter = new ArrayAdapter(this,R.layout.item_spinner,dataTgl);
        spinTgl.setAdapter(tglAdapter);

        if(is_new){
            layJadwalNext.setVisibility(View.GONE);
        }else{
            layJadwalNext.setVisibility(View.VISIBLE);
            loadDataSchedule();
        }
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
                            imageSelect = true;
                            imagePath = pathPicture;

                        }
                    }

                }
            }
        }
    }
    private void pilihJam(final EditText editText){
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(FormScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                editText.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Pilih Jam Jadwal Kirim");
        mTimePicker.show();
    }
    private void pilihTglJam(final EditText editText){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        final String[] tempSelectDate = {""};
        DatePickerDialog datePickerDialog = new DatePickerDialog(FormScheduleActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        tempSelectDate[0] = year + "-" +  (monthOfYear + 1) + "-" + dayOfMonth;
                        Calendar mcurrentTime = Calendar.getInstance();
                        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                        final int minute = mcurrentTime.get(Calendar.MINUTE);
                        TimePickerDialog mTimePicker;
                        mTimePicker = new TimePickerDialog(FormScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                tempSelectDate[0] = tempSelectDate[0] + " "+ selectedHour + ":" + selectedMinute;
                                editText.setText(tempSelectDate[0]);
                            }
                        }, hour, minute, true);//Yes 24 hour time
                        mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                editText.setText(editText.getText().toString() + " " + hour + ":" + minute);
                            }
                        });
                        mTimePicker.setTitle("Pilih Jadwal Kirim");
                        mTimePicker.show();
                    }
                }, year, month, day);
        //datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
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
    private boolean isRequired() {
        if (tipeForm.equals("grup")){
            if (listNoTujuan.size()<=0){
                Toast.makeText(FormScheduleActivity.this, "Belum ada grup yang dipilih", Toast.LENGTH_SHORT).show();
                return false;
            }
        }else if(excludeContact.length()<=0){
            Toast.makeText(FormScheduleActivity.this, "Belum ada kontak yang dipilih", Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(edtIsiPesan.getText().toString())){
            edtIsiPesan.setError("Field ini tidak boleh kosong");
            edtIsiPesan.requestFocus();
            return false;
        }else if(TextUtils.isEmpty(edtJadwalKirim.getText().toString())){
            edtJadwalKirim.setError("Field ini tidak boleh kosong");
            edtJadwalKirim.requestFocus();
            return false;
        }else if(TextUtils.isEmpty(edtJadwalKirimSelanjutnya.getText()) && is_new == false){
            edtJadwalKirimSelanjutnya.setError("Field ini tidak boleh kosong");
            edtJadwalKirimSelanjutnya.requestFocus();
            return false;
        }
        return true;
    }

    private void loadDataSchedule() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id",idSchedule);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GET_SCHEDULE)
                .buildUpon()
                .toString();
        Log.i(TAG,"body:" + requestBody);
        loadData = true;
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
                        excludeContact = new JSONArray();
                        listNoTujuan.clear();
                        final JSONArray data = response.getJSONArray("data");
                        final String id = data.getJSONObject(0).getString("id");
                        final String img_hash = data.getJSONObject(0).getString("img_hash");
                        final String schedule_at = data.getJSONObject(0).getString("schedule_at");
                        final String tgl_kirim = data.getJSONObject(0).getString("tgl_kirim");
                        final String next_schedule = data.getJSONObject(0).getString("next_schedule");
                        final String time_type = data.getJSONObject(0).getString("time_type");
                        final String day = data.getJSONObject(0).getString("day");
                        final String tgl = data.getJSONObject(0).getString("tgl");
                        final String message_sch = data.getJSONObject(0).getString("message");
                        final String group_id = data.getJSONObject(0).getString("group_id");
                        final String group_name = data.getJSONObject(0).getString("group_name");
                        final String group_description = data.getJSONObject(0).getString("group_description");
                        final String contact_id = data.getJSONObject(0).getString("contact_id");
                        final String contact_name = data.getJSONObject(0).getString("contact_name");
                        final String contact_phone = data.getJSONObject(0).getString("contact_phone");
                        final JSONArray exclude_contact = data.getJSONObject(0).getJSONArray("exclude_contact");
                        final String status_sch = data.getJSONObject(0).getString("status");

                        if (!(img_hash.equals(null) || img_hash.equals("null") || img_hash == null )){
                            try {
                                imgPesan.setImageURI(Uri.parse(img_hash));
                                imagePath = img_hash;
                            }catch (Exception e){

                            }
                        }
                        if (time_type.equals("daily")){
                            spinTipeJadwal.setSelection(1);
                            edtJadwalKirim.setText(formateDateFromstring("HH:mm:ss","HH:mm",schedule_at));
                        }else if (time_type.equals("weekly")){
                            edtJadwalKirim.setText(formateDateFromstring("HH:mm:ss","HH:mm",schedule_at));
                            spinTipeJadwal.setSelection(2);
                        }else if (time_type.equals("monthly")){
                            edtJadwalKirim.setText(formateDateFromstring("HH:mm:ss","HH:mm",schedule_at));
                            spinTipeJadwal.setSelection(3);
                        }else if (time_type.equals("annually")){
                            edtJadwalKirim.setText(formateDateFromstring("yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm",tgl_kirim));
                            spinTipeJadwal.setSelection(4);
                        }else if (time_type.equals("once")){
                            edtJadwalKirim.setText(formateDateFromstring("yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm",tgl_kirim));
                            spinTipeJadwal.setSelection(0);
                        }
                        if (day!=null && !day.equals("null")){
                            spinHari.setSelection(Integer.parseInt(day));
                        }
                        if (tgl!=null && !tgl.equals("null")){
                            spinTgl.setSelection(Integer.parseInt(tgl)-1);
                        }

                        edtIsiPesan.setText(message_sch);
                        edtJadwalKirimSelanjutnya.setText(formateDateFromstring("yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm",next_schedule));



                        if (group_id.equals(null) || group_id.equals("null") || group_id == null){
                            excludeContact.put(contact_id);
                            if (status_sch.equals("aktif")){
                                spinStatus.setSelection(0);
                            }else{
                                spinStatus.setSelection(1);
                            }
                            listNoTujuan.add(new ItemRecyclerTag(contact_id,contact_name));
                        }else{
                            excludeContact = exclude_contact;
                            if (status_sch.equals("aktif")){
                                spinStatus.setSelection(0);
                            }else{
                                spinStatus.setSelection(1);
                            }
                            listNoTujuan.add(new ItemRecyclerTag(group_id,group_name));
                        }
                        reloadExcludeContact();

                    }else{
                        new AlertDialog.Builder(FormScheduleActivity.this)
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
                    new AlertDialog.Builder(FormScheduleActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();

                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData = false;
                    }
                },1500);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                loadData = false;
                Log.i(TAG,errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response.statusCode==403){
                    try {
                        JSONObject jsonObject = new JSONObject(response.data.toString());
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")){
                            new AlertDialog.Builder(FormScheduleActivity.this)
                                    .setMessage("Session telah habias / telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormScheduleActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        }else{
                            new AlertDialog.Builder(FormScheduleActivity.this)
                                    .setMessage(msg)
                                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
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
                    new AlertDialog.Builder(FormScheduleActivity.this)
                            .setMessage(msg)
                            .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
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

    private void simpanSchedule(){
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        String time_type = dataTipe[spinTipeJadwal.getSelectedItemPosition()];
        if (spinTipeJadwal.getSelectedItemPosition()==0){
            time_type = "once";
        }
        final JSONObject requestBody = new JSONObject();
        String url = "";
        try {
            requestBody.put("versi",2);
            requestBody.put("day",spinHari.getSelectedItemPosition());
            requestBody.put("tgl",dataTgl[spinTgl.getSelectedItemPosition()]);
            requestBody.put("img_hash",imagePath);
            requestBody.put("message",edtIsiPesan.getText().toString());
            requestBody.put("schedule_at",edtJadwalKirim.getText().toString());
            requestBody.put("time_type",time_type);
            requestBody.put("status",dataStatus[spinStatus.getSelectedItemPosition()]);
            if (tipeForm.equals("grup")){
                requestBody.put("group_id",listNoTujuan.get(0).getId());
                requestBody.put("exclude_contact",excludeContact);
            }else{
                requestBody.put("contact_id",excludeContact.get(0));
            }

            if (is_new){
                if (tipeForm.equals("grup")){
                    url = URL_POST_CREATE_JADWAL_BY_GROUP;
                }else{
                    url = URL_POST_CREATE_JADWAL_BY_CONTACT;
                }
            }else{
                if (tipeForm.equals("grup")){
                    url = URL_POST_UPDATE_JADWAL_BY_GROUP;
                }else{
                    url = URL_POST_UPDATE_JADWAL_BY_CONTACT;
                }
                requestBody.put("schedule_id",idSchedule);
                requestBody.put("next_schedule",edtJadwalKirimSelanjutnya.getText().toString());
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
                        Toast.makeText(FormScheduleActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }else{
                        new AlertDialog.Builder(FormScheduleActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormScheduleActivity.this)
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
                if (response.statusCode==403){
                    try {
                        JSONObject jsonObject = new JSONObject(response.data.toString());
                        final boolean status = jsonObject.getBoolean("status");
                        final String msg = jsonObject.getString("error");
                        if (msg.trim().toLowerCase().equals("invalid api key")){
                            new AlertDialog.Builder(FormScheduleActivity.this)
                                    .setMessage("Session telah habias / telah login di perangkat lain.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            session.clearData();
                                            startActivity(new Intent(FormScheduleActivity.this, LoginActivity.class));
                                            finish();
                                        }
                                    })
                                    .show();
                        }else{
                            new AlertDialog.Builder(FormScheduleActivity.this)
                                    .setMessage(msg)
                                    .setPositiveButton("OK",null)
                                    .show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }else{
                    final String msg = getResources().getString(errorResponse(error));
                    new AlertDialog.Builder(FormScheduleActivity.this)
                            .setMessage(msg)
                            .setPositiveButton("OK",null)
                            .show();
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
        }else if(requestCode==REQUEST_EXCLUDE){
            if (resultCode==RESULT_OK){

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
            if (excludeContact.length()>0){
                btnCariKontak.setEnabled(false);
            }else{
                btnCariKontak.setEnabled(true);
            }
        }else{
            if (listNoTujuan.size()>0){
                btnCariKontak.setEnabled(false);
            }else{
                btnCariKontak.setEnabled(true);
            }
        }
        if (excludeContact.length()>0){
            btnKontakTdkDipilih.setEnabled(true);
        }else{
            btnKontakTdkDipilih.setEnabled(false);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
