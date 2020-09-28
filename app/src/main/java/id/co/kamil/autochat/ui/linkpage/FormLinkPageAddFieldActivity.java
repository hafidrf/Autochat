package id.co.kamil.autochat.ui.linkpage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import id.co.kamil.autochat.R;

public class FormLinkPageAddFieldActivity extends AppCompatActivity {

    private String tipeForm;
    private String judulField, linkField;
    private int positionList;
    private EditText edtJudul, edtLink;
    private Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_link_page_add_field);
        getSupportActionBar().setTitle("Field Linkpage");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tipeForm = getIntent().getStringExtra("tipe");
        judulField = getIntent().getStringExtra("judul");
        linkField = getIntent().getStringExtra("link");
        positionList = getIntent().getIntExtra("positionList", 0);


        edtJudul = (EditText) findViewById(R.id.edtJudul);
        edtLink = (EditText) findViewById(R.id.edtLink);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRequired()) {
                    simpan();
                }
            }
        });

        if (tipeForm.equals("edit")) {
            edtJudul.setText(judulField);
            edtLink.setText(linkField);
        }

    }

    private void simpan() {
        new AlertDialog.Builder(this)
                .setMessage("Apakah anda yakin akan menyimpan data berikut?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent();
                        intent.putExtra("tipeForm", tipeForm);
                        intent.putExtra("judul", edtJudul.getText().toString());
                        intent.putExtra("link", edtLink.getText().toString());
                        intent.putExtra("positionList", positionList);
                        setResult(RESULT_OK, intent);
                        finish();
                        Toast.makeText(getApplicationContext(), "Field berhasil disimpan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak", null)
                .show();

    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtJudul.getText())) {
            edtJudul.setError("Field ini tidak boleh kosong");
            edtJudul.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(edtLink.getText())) {
            edtLink.setError("Field ini tidak boleh kosong");
            edtLink.requestFocus();
            return false;

        }

        return true;
    }

}
