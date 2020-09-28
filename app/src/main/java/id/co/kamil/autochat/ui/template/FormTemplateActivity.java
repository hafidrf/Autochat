package id.co.kamil.autochat.ui.template;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
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
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.zelory.compressor.Compressor;

import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_TEMPLATE;
import static id.co.kamil.autochat.utils.API.URL_POST_EDIT_TEMPLATE;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_TEMPLATE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.ConvertBitmapToString;
import static id.co.kamil.autochat.utils.Utils.SaveImage;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.fileExist;
import static id.co.kamil.autochat.utils.Utils.getDirWabot;
import static id.co.kamil.autochat.utils.Utils.getFileExtension;

public class FormTemplateActivity extends AppCompatActivity {

    private static final String TAG = "FormTemplateActivity";
    private static final int LOAD_IMAGE_RESULT = 100;
    private String tipeForm;
    private String templateId;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private ImageButton btnTambah;
    private EditText edtKonten;
    private Button btnSimpan;
    private RecylerTagAdapter adapterTag;
    private List<ItemRecyclerTag> listKeyword = new ArrayList<>();
    private int id_tag = 0;
    private JSONArray dataTags = new JSONArray();
    private ImageButton btnBrowse, btnHapus;
    private ImageView imgPesan;
    private boolean imageSelect = false;
    private String imagePath;
    private File fileImage;
    private boolean editPicture = false;
    private DBHelper dbHelper;
    private EditText edtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_template);

        tipeForm = getIntent().getStringExtra("tipe");
        if (tipeForm.equals("edit")) {
            getSupportActionBar().setTitle("Edit Template");
            templateId = getIntent().getStringExtra("id");
        } else {
            getSupportActionBar().setTitle("Tambah Template");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DBHelper(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);


        pDialog = new ProgressDialog(this);
        imgPesan = (ImageView) findViewById(R.id.imgPesan);
        edtName = (EditText) findViewById(R.id.edtName);
        edtKonten = (EditText) findViewById(R.id.edtKonten);
        btnTambah = (ImageButton) findViewById(R.id.btnTambah);
        btnBrowse = (ImageButton) findViewById(R.id.btnBrowse);
        btnHapus = (ImageButton) findViewById(R.id.btnHapus);
        btnSimpan = (Button) findViewById(R.id.btnSimpan);
        btnSimpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listKeyword.size() <= 0) {
                    Toast.makeText(FormTemplateActivity.this, "Tags tidak boleh kosong", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(edtName.getText().toString())) {
                    edtName.setError("Field ini tidak boleh kosong");
                    edtName.requestFocus();
                } else if (TextUtils.isEmpty(edtKonten.getText().toString())) {
                    edtKonten.setError("Field ini tidak boleh kosong");
                    edtKonten.requestFocus();
                } else {
                    simpanTemplate();
                }
            }
        });
        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Alertdialog form input keyword
                dialogForm();
            }
        });


        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gridKeyword);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        adapterTag = new RecylerTagAdapter(listKeyword, this, new RecylerTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemRecyclerTag item) {
                for (int i = 0; i < listKeyword.size(); i++) {
                    if (listKeyword.get(i).getId().equals(item.getId())) {
                        listKeyword.remove(i);
                        adapterTag.notifyDataSetChanged();
                        break;
                    }
                }
            }
        });
        recyclerView.setAdapter(adapterTag);

        recyclerView.setMinimumHeight((int) convertDpToPixel(35, this));
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        recyclerView.setLayoutManager(layoutManager);

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
                editPicture = true;

            }
        });

        if (tipeForm.equals("edit")) {
            loadData();
        }
    }

    public void callGalleryPhoto() {

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

    private void loadData() {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("id", templateId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(URL_POST_GET_TEMPLATE)
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
                        final JSONObject data = response.getJSONObject("data");
                        final JSONArray tags = data.getJSONArray("tags");
                        final String name = data.getString("name");
                        final String content = data.getString("content");
                        final String picture = data.getString("picture");
                        for (int a = 0; a < tags.length(); a++) {
                            id_tag++;
                            String tmptag = String.valueOf(id_tag);
                            listKeyword.add(new ItemRecyclerTag(tmptag, tags.getString(a)));
                        }
                        if (!(picture.isEmpty() || picture == null)) {
                            Picasso.with(FormTemplateActivity.this).load(picture)
                                    .error(R.drawable.ic_image)
                                    .placeholder(R.drawable.ic_image)
                                    .into(imgPesan);
                        }
                        adapterTag.notifyDataSetChanged();
                        edtName.setText(name);
                        edtKonten.setText(content);
                    } else {
                        new AlertDialog.Builder(FormTemplateActivity.this)
                                .setMessage(message)
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
                    new AlertDialog.Builder(FormTemplateActivity.this)
                            .setMessage(e.getMessage())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
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
                    errorResponse(getApplicationContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormTemplateActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormTemplateActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormTemplateActivity.this)
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
                        new AlertDialog.Builder(FormTemplateActivity.this)
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
                }


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void dialogForm() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(FormTemplateActivity.this);
        final LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_form_keyword, null);
        TextView txtLabel = (TextView) dialogView.findViewById(R.id.label);
        txtLabel.setText("Tags");
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Form Tags");

        final EditText edtKeyword = (EditText) dialogView.findViewById(R.id.edtKeyword);
        edtKeyword.setHint("Tags");
        edtKeyword.setText("");

        dialog.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                id_tag++;
                String tmpid = String.valueOf(id_tag);
                listKeyword.add(new ItemRecyclerTag(tmpid, edtKeyword.getText().toString()));
                adapterTag.notifyDataSetChanged();
                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void simpanTemplate() {
        Bitmap compressedImageBitmap = null;
        dataTags = new JSONArray();
        for (int i = 0; i < listKeyword.size(); i++) {
            dataTags.put(listKeyword.get(i).getTitle());
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        String base64_picture = "";
        if (tipeForm.equals("edit") && editPicture || tipeForm.equals("add")) {
            if (imageSelect) {
                compressedImageBitmap = new Compressor.Builder(this).setQuality(75).build().compressToBitmap(fileImage);
                base64_picture = ConvertBitmapToString(compressedImageBitmap);
            } else {
                base64_picture = "";
            }
        } else if (tipeForm.equals("edit") && editPicture == false) {
            base64_picture = null;
        }


        final JSONObject requestBody = new JSONObject();
        String url = "";
        try {
            requestBody.put("tags", dataTags);
            requestBody.put("name", edtName.getText().toString());
            requestBody.put("content", edtKonten.getText().toString());
            requestBody.put("picture", base64_picture);
            if (tipeForm.equals("edit")) {
                url = URL_POST_EDIT_TEMPLATE;
                requestBody.put("id", templateId);
            } else {
                url = URL_POST_CREATE_TEMPLATE;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String uri = Uri.parse(url)
                .buildUpon()
                .toString();
        Log.i(TAG, "body:" + requestBody);
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        final Bitmap finalCompressedImageBitmap = compressedImageBitmap;
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status) {
                        if (response.isNull("picture_hash") == false) {
                            final String picture_hash = response.getString("picture_hash");

                            if (picture_hash.isEmpty() == false) {
                                if (fileExist(getApplicationContext(), getDirWabot("template_promosi") + "/" + picture_hash) == false) {
                                    SaveImage(finalCompressedImageBitmap, "template_promosi", picture_hash);
                                    int versionDB = dbHelper.getVersionCodeDB2("ver_template");
                                    dbHelper.updateDBVersion2(String.valueOf(versionDB + 1), "ver_template");
                                }
                            }
                        }


                        Toast.makeText(FormTemplateActivity.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        new AlertDialog.Builder(FormTemplateActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(FormTemplateActivity.this)
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
                if (response != null) {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(FormTemplateActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(FormTemplateActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(FormTemplateActivity.this)
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(FormTemplateActivity.this)
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } else {
                    errorResponse(getApplicationContext(), error);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOAD_IMAGE_RESULT) {
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
                fileImage = new File(picturePath);
                String exc = getFileExtension(fileImage);
                if (exc.toLowerCase().equals("jpg") || exc.toLowerCase().equals("jpeg") || exc.toLowerCase().equals("png") || exc.toLowerCase().equals("bmp")) {
                    imgPesan.setImageURI(Uri.parse(filePath));
                    imageSelect = true;
                    imagePath = filePath;
                    editPicture = true;
                } else {
                    Toast.makeText(this, "Maaf, Tipe File tidak diizinkan", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
