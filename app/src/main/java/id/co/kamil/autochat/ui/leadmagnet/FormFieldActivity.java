package id.co.kamil.autochat.ui.leadmagnet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterField;
import id.co.kamil.autochat.adapter.ItemField;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;

public class FormFieldActivity extends AppCompatActivity {

    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private Button btnPilih;
    private CheckBox chkSemua;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private EditText edtCari;
    private ListView listKontak;
    private String[] arrField = {"nama_depan", "nama_belakang", "email", "telepon", "sapaan", "telegram", "alamat", "web", "facebook", "instagram", "linkedin", "tokopedia", "bukalapak", "shopee", "id olshop", "jenis kelamin", "tgl lahir", "catatan", "kota"};
    private AdapterField adapterField;
    private List<ItemField> dataField = new ArrayList<>();
    private String exclude;
    private JSONArray excludeField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_field);
        getSupportActionBar().setTitle("Pilih Field");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        btnPilih = (Button) findViewById(R.id.btnPilih);
        chkSemua = (CheckBox) findViewById(R.id.chkSemua);

        pDialog = new ProgressDialog(this);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        edtCari = (EditText) findViewById(R.id.edtCari);
        listKontak = (ListView) findViewById(R.id.listKontak);

        exclude = getIntent().getStringExtra("exclude");

        chkSemua.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < adapterField.dataField.size(); i++) {
                    adapterField.dataField.get(i).setCheckbox(isChecked);
                    for (int a = 0; a < adapterField.dataField.size(); a++) {
                        if (adapterField.dataField.get(a).getJudul().equals(adapterField.dataField.get(i).getJudul())) {
                            adapterField.dataField.get(a).setCheckbox(isChecked);
                        }
                    }
                }
                adapterField.notifyDataSetChanged();
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterField.filter(edtCari.getText().toString().trim());
                    listKontak.invalidate();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btnPilih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int chk = 0;
                for (int i = 0; i < adapterField.arraylist.size(); i++) {
                    if (adapterField.arraylist.get(i).isCheckbox()) {
                        chk++;
                    }
                }
                if (chk == 0) {
                    new AlertDialog.Builder(FormFieldActivity.this)
                            .setMessage("Tidak ada field yang dipilih")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    new AlertDialog.Builder(FormFieldActivity.this)
                            .setMessage("Apakah anda yakin akan pilih field tersebut?")
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
        try {
            excludeField = new JSONArray(exclude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                loadField();

            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadField();
            }
        });

    }

    private void simpan() {
        JSONArray arrTemp = new JSONArray();
        for (int i = 0; i < dataField.size(); i++) {
            if (dataField.get(i).isCheckbox()) {
                arrTemp.put(dataField.get(i).getJudul());
            }
        }
        Intent intent = new Intent();
        intent.putExtra("arrField", arrTemp.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadField() {
        swipe_refresh.setRefreshing(true);
        dataField.clear();
        if (excludeField.length() > 0) {
            boolean ada = false;
            for (int i = 0; i < arrField.length; i++) {
                ada = false;
                for (int a = 0; a < excludeField.length(); a++) {
                    try {
                        if (arrField[i].equals(excludeField.getString(a))) {
                            ada = true;
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (ada == false) {
                    dataField.add(new ItemField(arrField[i], false, true));
                } else {
                    dataField.add(new ItemField(arrField[i], true, true));
                }
            }
        } else {
            for (int i = 0; i < arrField.length; i++) {
                dataField.add(new ItemField(arrField[i], false, true));
            }
        }
        displayField();
        swipe_refresh.setRefreshing(false);

    }

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void displayField() {
        adapterField = new AdapterField(dataField, this);
        listKontak.setAdapter(adapterField);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
