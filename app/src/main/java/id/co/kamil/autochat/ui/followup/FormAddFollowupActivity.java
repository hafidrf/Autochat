package id.co.kamil.autochat.ui.followup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;

public class FormAddFollowupActivity extends AppCompatActivity {

    private EditText edtJadwalKirim;
    private Button btnSimpan;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private EditText edtIsiPesan;
    private String tipeForm;
    private int positionList = 0;
    private String followup,schedule;
    private TextView txtSapaan,txtNamaBelakang,txtNamaDepan;
    private Spinner spinTipeJadwal;
    private EditText edtInterval;
    private LinearLayout layInterval;
    private LinearLayout layJadwal;
    private String[] arrTipe = {"Tanggal","Interval Hari"};
    private String interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_add_followup);

        getSupportActionBar().setTitle("Form Follow Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tipeForm = getIntent().getStringExtra("tipe");
        positionList = getIntent().getIntExtra("positionList",0);
        followup = getIntent().getStringExtra("followup");
        schedule = getIntent().getStringExtra("schedule");
        interval = getIntent().getStringExtra("interval_val");
        boolean isInterval = getIntent().getBooleanExtra("interval", false);

        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        txtSapaan = (TextView) findViewById(R.id.txtSapaan);
        txtNamaBelakang = (TextView) findViewById(R.id.txtNamaBelakang);
        txtNamaDepan = (TextView) findViewById(R.id.txtNamaDepan);
        spinTipeJadwal = (Spinner) findViewById(R.id.spinTipeJadwal);
        edtInterval = (EditText) findViewById(R.id.edtInterval);
        layInterval = (LinearLayout) findViewById(R.id.layInterval);
        layJadwal = (LinearLayout) findViewById(R.id.layJadwal);

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
        edtIsiPesan = (EditText) findViewById(R.id.edtPesan);
        edtJadwalKirim = (EditText) findViewById(R.id.edtJadwalKirim);
        edtJadwalKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                final String[] tempSelectDate = {""};
                DatePickerDialog datePickerDialog = new DatePickerDialog(FormAddFollowupActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                tempSelectDate[0] = year + "-" +  (monthOfYear + 1) + "-" + dayOfMonth;
                                Calendar mcurrentTime = Calendar.getInstance();
                                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                                final int minute = mcurrentTime.get(Calendar.MINUTE);
                                TimePickerDialog mTimePicker;
                                mTimePicker = new TimePickerDialog(FormAddFollowupActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                        tempSelectDate[0] = tempSelectDate[0] + " "+ selectedHour + ":" + selectedMinute;
                                        edtJadwalKirim.setText(tempSelectDate[0]);
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
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRequired()){
                    String val_interval = "0";
                    if(spinTipeJadwal.getSelectedItemPosition()==0){
                        val_interval = "0";
                    }else{
                        val_interval = edtInterval.getText().toString();
                        if (val_interval.equals(null) || val_interval.equals("null") || val_interval == null  || val_interval.equals("") || val_interval.isEmpty() || TextUtils.isEmpty(val_interval)){
                            Toast.makeText(FormAddFollowupActivity.this, "Interval minimal 1 hari", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (Integer.parseInt(val_interval)<=0){
                            Toast.makeText(FormAddFollowupActivity.this, "Interval minimal 1 hari", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Intent intent = new Intent();
                    intent.putExtra("schedule",edtJadwalKirim.getText().toString());
                    intent.putExtra("followup",edtIsiPesan.getText().toString());
                    intent.putExtra("tipe",tipeForm);
                    intent.putExtra("interval",val_interval);
                    intent.putExtra("positionList",positionList);
                    setResult(RESULT_OK,intent);
                    if (tipeForm.equals("edit")){
                        Toast.makeText(FormAddFollowupActivity.this,"Berhasil diupdate",Toast.LENGTH_SHORT);
                    }else{
                        Toast.makeText(FormAddFollowupActivity.this,"Berhasil disimpan",Toast.LENGTH_SHORT);
                    }
                    finish();
                }

            }
        });
        if(tipeForm.equals("edit")){
            edtJadwalKirim.setText(schedule);
            edtIsiPesan.setText(followup);
            edtInterval.setText(interval);
        }
        final ArrayAdapter statusAdapter = new ArrayAdapter(this,R.layout.item_spinner,arrTipe);
        spinTipeJadwal.setAdapter(statusAdapter);
        spinTipeJadwal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    layInterval.setVisibility(View.GONE);
                    layJadwal.setVisibility(View.VISIBLE);
                }else {
                    layInterval.setVisibility(View.VISIBLE);
                    layJadwal.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (isInterval){
            spinTipeJadwal.setEnabled(true);
            if (Integer.parseInt(interval)>0){
                spinTipeJadwal.setSelection(1);
                spinTipeJadwal.callOnClick();
            }
        }else{
            spinTipeJadwal.setEnabled(false);
            spinTipeJadwal.setSelection(0);
            spinTipeJadwal.callOnClick();
        }
    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtJadwalKirim.getText()) && spinTipeJadwal.getSelectedItemPosition()==0){
            edtJadwalKirim.setError("Field ini tidak boleh kosong");
            edtJadwalKirim.requestFocus();
            return false;
        }else if (TextUtils.isEmpty(edtIsiPesan.getText())){
            edtIsiPesan.setError("Field ini tidak boleh kosong");
            edtIsiPesan.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
