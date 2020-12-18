package id.co.kamil.autochat.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.bulksender.WASendService;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;
import rkr.simplekeyboard.inputmethod.latin.settings.SettingsActivity;

import static android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_CREATE_SETTINGS;
import static id.co.kamil.autochat.utils.API.URL_POST_GET_SETTINGS;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.AUTOREPLY_BUSINESS;
import static id.co.kamil.autochat.utils.SharPref.AUTOREPLY_PERSONAL;
import static id.co.kamil.autochat.utils.SharPref.BULK_SENDER_ON_SCREEN;
import static id.co.kamil.autochat.utils.SharPref.DELAY_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.DIR_IMAGE;
import static id.co.kamil.autochat.utils.SharPref.STATUS_AUTOTEXT;
import static id.co.kamil.autochat.utils.SharPref.STATUS_BULK_SENDER;
import static id.co.kamil.autochat.utils.SharPref.STATUS_ERROR_TRY_AGAIN;
import static id.co.kamil.autochat.utils.SharPref.STATUS_TOOLBAR;
import static id.co.kamil.autochat.utils.SharPref.SYNC_CONTACT_WABOT;
import static id.co.kamil.autochat.utils.SharPref.TRY_AGAIN_BULKSENDER;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class PengaturanActivity extends AppCompatActivity {

    private static final String TAG = "PengaturanActivity";
    private EditText edtDelayBulkSender;
    private Switch switchAutoText;
    private Switch switchToolbar;
    private Button btnPengaturanKeyboard;
    private SharPref sharePref;
    private Switch switchOnScreen, switchEnabledBulkSender, switchAksesibilitas;
    private boolean status_aksesibilitas;
    private Button btnPengaturanAutoReply;
    private Switch switchPersonal, switchBusiness;
    private EditText edtTryAgain;
    private Switch siwtchOnOffError;
    private Button btnKeyboardPref;
    private Button btnRedaksiUltah;
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);
        getSupportActionBar().setTitle("Pengaturan");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pDialog = new ProgressDialog(this);
        sharePref = new SharPref(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        edtDelayBulkSender = (EditText) findViewById(R.id.edtDelayBulkSender);
        edtTryAgain = (EditText) findViewById(R.id.edtTryAgain);
        switchOnScreen = (Switch) findViewById(R.id.switchOnScreen);
        switchEnabledBulkSender = (Switch) findViewById(R.id.switchEnabledBulkSender);
        switchAksesibilitas = (Switch) findViewById(R.id.switchAksesibilitas);
        switchAutoText = (Switch) findViewById(R.id.switchAutoText);
        switchToolbar = (Switch) findViewById(R.id.switchToolbar);
        switchPersonal = (Switch) findViewById(R.id.switchAutoReplyPersonal);
        switchBusiness = (Switch) findViewById(R.id.switchAutoReplyBusiness);
        siwtchOnOffError = (Switch) findViewById(R.id.switchEnabledStatusError);
        btnPengaturanKeyboard = (Button) findViewById(R.id.btnPengaturanKeyboard);
        btnPengaturanAutoReply = (Button) findViewById(R.id.btnPengaturanAutoReply);
        btnKeyboardPref = (Button) findViewById(R.id.btnKeyboardPref);
        btnRedaksiUltah = (Button) findViewById(R.id.btnRedaksiUltah);

        String dirImage = sharePref.getSessionStr(DIR_IMAGE);
        String delayBulkSender = sharePref.getSessionStr(DELAY_BULK_SENDER);
        boolean auto_text_enabled = sharePref.getSessionBool(STATUS_AUTOTEXT);
        boolean toolbar_enabled = sharePref.getSessionBool(STATUS_TOOLBAR);
        boolean bulk_sender_onscreen = sharePref.getSessionBool(BULK_SENDER_ON_SCREEN);
        boolean status_bulk_sender = sharePref.getSessionBool(STATUS_BULK_SENDER);
        boolean status_error_try_again = sharePref.getSessionBool(STATUS_ERROR_TRY_AGAIN);
        boolean autoreply_personal = sharePref.getSessionBool(AUTOREPLY_PERSONAL);
        boolean autoreply_business = sharePref.getSessionBool(AUTOREPLY_BUSINESS);
        boolean sync_contact_wabot = sharePref.getSessionBool(SYNC_CONTACT_WABOT);
        String try_again = sharePref.getSessionStr(TRY_AGAIN_BULKSENDER);
        status_aksesibilitas = isAccessibilityEnabled();

        switchAutoText.setChecked(auto_text_enabled);
        siwtchOnOffError.setChecked(status_error_try_again);
        switchToolbar.setChecked(toolbar_enabled);
        switchOnScreen.setChecked(bulk_sender_onscreen);
        switchAksesibilitas.setChecked(status_aksesibilitas);
        switchEnabledBulkSender.setChecked(status_bulk_sender);
        switchBusiness.setChecked(autoreply_business);
        switchPersonal.setChecked(autoreply_personal);

        if (delayBulkSender.isEmpty() || delayBulkSender.equals("")) {
            delayBulkSender = "0";
        }
        if (try_again.isEmpty() || try_again.equals("")) {
            try_again = "0";
        }
        if (Integer.parseInt(delayBulkSender) == 0) {
            edtDelayBulkSender.setText("5");
        } else {
            edtDelayBulkSender.setText(delayBulkSender);
        }
        if (Integer.parseInt(try_again) == 0) {
            edtTryAgain.setText("5");
        } else {
            edtTryAgain.setText(try_again);
        }

        if (status_error_try_again) {
            edtTryAgain.setEnabled(true);
        } else {
            edtTryAgain.setEnabled(false);
        }
        // listener
        siwtchOnOffError.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(STATUS_ERROR_TRY_AGAIN, isChecked);
                if (isChecked) {
                    edtTryAgain.setEnabled(true);
                } else {
                    edtTryAgain.setEnabled(false);
                }
            }
        });
        btnKeyboardPref.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PengaturanActivity.this, SettingsActivity.class));
            }
        });
        btnPengaturanAutoReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });
        switchEnabledBulkSender.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(STATUS_BULK_SENDER, isChecked);
            }
        });
        switchAksesibilitas.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchAksesibilitas.setChecked(status_aksesibilitas);
                Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(i);
            }
        });
        switchOnScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(BULK_SENDER_ON_SCREEN, isChecked);
            }
        });
        switchPersonal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(AUTOREPLY_PERSONAL, isChecked);
            }
        });
        switchBusiness.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(AUTOREPLY_BUSINESS, isChecked);
            }
        });
        edtDelayBulkSender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sharePref.createSession(DELAY_BULK_SENDER, edtDelayBulkSender.getText().toString());
            }
        });

        edtTryAgain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sharePref.createSession(TRY_AGAIN_BULKSENDER, edtTryAgain.getText().toString());
            }
        });
        switchAutoText.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(STATUS_AUTOTEXT, isChecked);
            }
        });
        switchToolbar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sharePref.createSession(STATUS_TOOLBAR, isChecked);
            }
        });
        btnPengaturanKeyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        btnRedaksiUltah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogRedaksiultah();
            }
        });
    }

    private void showDialogRedaksiultah() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_form_redaksi_ultah, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Form Redaksi Ulang Tahun");

        final EditText edtContent = (EditText) dialogView.findViewById(R.id.edtContent);
        TextView txtSapaan = (TextView) dialogView.findViewById(R.id.txtSapaan);
        TextView txtNamaBelakang = (TextView) dialogView.findViewById(R.id.txtNamaBelakang);
        TextView txtNamaDepan = (TextView) dialogView.findViewById(R.id.txtNamaDepan);

        loadRedaksi(edtContent);

        txtSapaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtContent.getText().insert(edtContent.getSelectionStart(), "[sapaan] ");
            }
        });

        txtNamaDepan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtContent.getText().insert(edtContent.getSelectionStart(), "[nama_depan] ");
            }
        });
        txtNamaBelakang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtContent.getText().insert(edtContent.getSelectionStart(), "[nama_belakang] ");
            }
        });
        dialog.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                simpanRedaksiultah(edtContent.getText().toString(), dialog);

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

    private void loadRedaksi(final EditText edtContent) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        String url = URL_POST_GET_SETTINGS;
        try {
            requestBody.put("key", "redaksi_ultah");
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status) {
                        final String konten = response.getString("value");
                        edtContent.setText(konten);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(PengaturanActivity.this)
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
                    errorResponse(PengaturanActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(PengaturanActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(PengaturanActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(PengaturanActivity.this)
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
                        new AlertDialog.Builder(PengaturanActivity.this)
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
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void simpanRedaksiultah(String value, final DialogInterface dialog) {
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        final JSONObject requestBody = new JSONObject();
        String url = URL_POST_CREATE_SETTINGS;
        try {
            requestBody.put("key", "redaksi_ultah");
            requestBody.put("value", value);
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
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status) {
                        Toast.makeText(PengaturanActivity.this, message, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        new AlertDialog.Builder(PengaturanActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(PengaturanActivity.this)
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
                    errorResponse(PengaturanActivity.this, error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(PengaturanActivity.this)
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(PengaturanActivity.this, LoginActivity.class));
                                                finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(PengaturanActivity.this)
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
                        new AlertDialog.Builder(PengaturanActivity.this)
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

    private boolean isAccessibilityEnabled() {
        int enabled = 0;
        final String service = getPackageName() + "/" + WASendService.class.getCanonicalName();

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status_aksesibilitas = isAccessibilityEnabled();
        switchAksesibilitas.setChecked(status_aksesibilitas);
    }
}
