package id.co.kamil.autochat.ui.waform;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.ItemGrup;
import id.co.kamil.autochat.utils.ExpandableHeightListView;

public class WaformAddFieldActivity extends AppCompatActivity {

    private String tipeForm;
    private String idField;
    private EditText edtLabel;
    private Spinner spinTipe;
    private CheckBox chkRequired;
    private Spinner spinInputTipe;
    private EditText edtPlaceholder;
    private Button btnTambah,btnSimpan;
    private ExpandableHeightListView listCombo;
    private String[] dataTipe = {"input","combobox"};
    private String[] dataInputTipe = {"text","email","number","url"};
    private LinearLayout layInput,layCombo;
    private String typeField,labelField;
    private String placeholderField;
    private boolean requiredField;
    private String inputTypeField;
    private int positionList;
    private List<JSONObject> list = new ArrayList<>();
    private int id_tag = 0;
    private AdapterListCombo adapterListCombo;
    private String listComboString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waform_add_field);
        getSupportActionBar().setTitle("Field WA Form");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tipeForm = getIntent().getStringExtra("tipe");
        idField = getIntent().getStringExtra("id");
        typeField = getIntent().getStringExtra("type");
        labelField = getIntent().getStringExtra("label");
        placeholderField = getIntent().getStringExtra("placeholderField");
        requiredField = getIntent().getBooleanExtra("requiredField",false);
        inputTypeField = getIntent().getStringExtra("inputTypeField");
        listComboString = getIntent().getStringExtra("listCombo");
        positionList = getIntent().getIntExtra("positionList",0);

        layInput = (LinearLayout) findViewById(R.id.layInput);
        layCombo = (LinearLayout) findViewById(R.id.layCombo);

        edtLabel = (EditText) findViewById(R.id.edtLabel);
        edtPlaceholder = (EditText) findViewById(R.id.edtPlaceholder);
        spinTipe = (Spinner) findViewById(R.id.spinTipe);
        spinInputTipe = (Spinner) findViewById(R.id.spinInputType);
        chkRequired = (CheckBox) findViewById(R.id.chkRequired);
        btnTambah = (Button) findViewById(R.id.btnTambah);
        listCombo = (ExpandableHeightListView) findViewById(R.id.listCombo);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRequired()){
                    simpan();
                }
            }
        });
        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogForm(false,0,"","");
            }
        });
        final ArrayAdapter adapterTipe = new ArrayAdapter(this,R.layout.item_spinner,dataTipe);
        spinTipe.setAdapter(adapterTipe);

        final ArrayAdapter adapterInputTipe = new ArrayAdapter(this,R.layout.item_spinner,dataInputTipe);
        spinInputTipe.setAdapter(adapterInputTipe);

        spinTipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position==0){
                    layInput.setVisibility(View.VISIBLE);
                    layCombo.setVisibility(View.GONE);
                }else{
                    layInput.setVisibility(View.GONE);
                    layCombo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (tipeForm.equals("edit")){
            edtLabel.setText(labelField);
            chkRequired.setChecked(requiredField);
            if (typeField.equals("input")){
                spinTipe.setSelection(0);
                edtPlaceholder.setText(placeholderField);
                for (int i = 0 ;i<dataInputTipe.length;i++){
                    if (dataInputTipe[i].equals(inputTypeField)){
                        spinInputTipe.setSelection(i);
                    }
                }
            }else{
                spinTipe.setSelection(1);
                try {
                    JSONArray listComboJSON = new JSONArray(listComboString);
                    for (int i = 0;i<listComboJSON.length();i++){
                        list.add(listComboJSON.getJSONObject(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                displayList();
            }
        }
        listCombo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    dialogForm(true,position,list.get(position).getString("label"),list.get(position).getString("value"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayList() {
        adapterListCombo = new AdapterListCombo(this,R.layout.item_list_combo,list);
        listCombo.setAdapter(adapterListCombo);
        listCombo.setExpanded(true);
    }

    private void dialogForm(final boolean edit, final int indexList,String caption,String value ) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_form_field_waform, null);

        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Form List");

        final EditText edtCaption    = (EditText) dialogView.findViewById(R.id.edtCaption);
        final EditText edtNilai    = (EditText) dialogView.findViewById(R.id.edtNilai);
        edtCaption.setText(caption);
        edtNilai.setText(value);
        String captionAdd = "Tambah";
        if (edit){
            captionAdd = "Update";
        }else{
            captionAdd = "Tambah";
        }
        dialog.setPositiveButton(captionAdd, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                id_tag++;
                JSONObject jsonObject = new JSONObject();
                try {
                    if (TextUtils.isEmpty(edtCaption.getText())){
                        edtCaption.setError("Field ini tidak boleh kosong");
                        edtCaption.requestFocus();
                        return;
                    }
                    if (TextUtils.isEmpty(edtNilai.getText())){
                        edtNilai.setText(edtCaption.getText().toString());
                    }
                    jsonObject.put("label",edtCaption.getText().toString());
                    jsonObject.put("value",edtNilai.getText().toString());
                    if (edit){
                        list.remove(indexList);
                        list.add(indexList,jsonObject);
                    }else{
                        list.add(jsonObject);
                    }

                    displayList();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        if (edit){
            dialog.setNeutralButton("Hapus", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    list.remove(indexList);
                    displayList();
                    Toast.makeText(WaformAddFieldActivity.this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }

        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void simpan() {
        new AlertDialog.Builder(this)
                .setMessage("Apakah anda yakin akan menyimpan data berikut?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject attr = new JSONObject();
                        JSONArray tmpList = new JSONArray();
                        try {
                            String required = "0";
                            if (chkRequired.isChecked()){
                                required = "1";
                            }
                            attr.put("required",required);
                            if (spinTipe.getSelectedItemPosition()==0){
                                attr.put("placeholder",edtPlaceholder.getText().toString());
                                attr.put("type",dataInputTipe[spinInputTipe.getSelectedItemPosition()]);
                            }else{
                                for (int i = 0 ;i<list.size();i++){
                                    tmpList.put(list.get(i));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("tipeForm",tipeForm);
                        intent.putExtra("type",dataTipe[spinTipe.getSelectedItemPosition()]);
                        intent.putExtra("id",idField);
                        intent.putExtra("label",edtLabel.getText().toString());
                        intent.putExtra("attr",attr.toString());
                        intent.putExtra("list",tmpList.toString());
                        intent.putExtra("positionList",positionList);
                        setResult(RESULT_OK,intent);
                        finish();
                        Toast.makeText(getApplicationContext(), "Field berhasil disimpan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Tidak",null)
                .show();

    }

    private boolean isRequired() {
        if (TextUtils.isEmpty(edtLabel.getText())){
            edtLabel.setError("Field ini tidak boleh kosong");
            edtLabel.requestFocus();
            return false;
        }
        if(spinTipe.getSelectedItemPosition()==1){
            if (list.size()<=0){
                Toast.makeText(this, "List ComboBox tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    class AdapterListCombo extends ArrayAdapter<JSONObject>{

        private int _resource;

        public AdapterListCombo(@NonNull Context context, int resource, @NonNull List<JSONObject> objects) {
            super(context, resource,objects);
            this._resource = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            final View view = layoutInflater.inflate(this._resource,parent,false);

            TextView txtCaption = (TextView) view.findViewById(R.id.txtCaption);
            TextView txtValue = (TextView) view.findViewById(R.id.txtValue);

            JSONObject jsonOb = getItem(position);
            try {
                txtCaption.setText(jsonOb.getString("label"));
                txtValue.setText(jsonOb.getString("value"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return view;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
