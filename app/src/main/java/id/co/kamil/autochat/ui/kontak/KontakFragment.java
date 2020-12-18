package id.co.kamil.autochat.ui.kontak;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterFollowup;
import id.co.kamil.autochat.adapter.AdapterKontak;
import id.co.kamil.autochat.adapter.ItemFollowup;
import id.co.kamil.autochat.adapter.ItemKontak;
import id.co.kamil.autochat.ui.followup.FormFollowupActivity;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.LIMIT_KONTAK;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_DIRECT_LINK_UPGRADE;
import static id.co.kamil.autochat.utils.API.URL_POST_ADD_CONTACT_FOLLOW_UP;
import static id.co.kamil.autochat.utils.API.URL_POST_ALL_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_IMPORT_CONTACT_CSV;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_CONTACT;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_FOLLOW_UP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.fileExist;
import static id.co.kamil.autochat.utils.Utils.getDirWabot;

public class KontakFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_ADD = 100;
    private static final String TAG = "KontakFragment";
    private static final int REQUEST_IMPORT = 90;
    private static final int REQUEST_IMPORT_CSV = 101;
    private static final int CHECK_PERMISSION = 102;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListView listKontak;
    private List<ItemKontak> dataKontak = new ArrayList<>();
    private AdapterKontak kontakAdapter;
    private EditText edtCari;
    private Menu menuTop;
    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout layMessage;
    private Button btnCobaLagi;
    private TextView lblMessage;
    private boolean adapterInstance = false;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private TextView labelStorage;
    private ProgressBar progressStorage;
    private String type;
    private int limit_kontak;
    private SharPref sharePref;
    private List<ItemFollowup> dataFollowup = new ArrayList<>();
    private AdapterFollowup followupAdapter;

    public KontakFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KontakFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KontakFragment newInstance(String param1, String param2) {
        KontakFragment fragment = new KontakFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kontak, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        type = userDetail.get(KEY_CUST_GROUP);
        sharePref = new SharPref(getContext());

        limit_kontak = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_KONTAK));
        if (limit_kontak <= 0) {
            limit_kontak = LIMIT_KONTAK;
        }
        pDialog = new ProgressDialog(getContext());
        labelStorage = (TextView) view.findViewById(R.id.labelStorage);
        progressStorage = (ProgressBar) view.findViewById(R.id.progressStorage);

        if (type.equals("1")) { // basic
            progressStorage.setVisibility(View.VISIBLE);
            labelStorage.setVisibility(View.VISIBLE);
        } else {
            progressStorage.setVisibility(View.GONE);
            labelStorage.setVisibility(View.GONE);
        }

        layMessage = (LinearLayout) view.findViewById(R.id.layMessage);
        lblMessage = (TextView) view.findViewById(R.id.lblMessage);
        btnCobaLagi = (Button) view.findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadKontak();
            }
        });
        edtCari = (EditText) view.findViewById(R.id.edtCari);
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        listKontak = (ListView) view.findViewById(R.id.listKontak);
        listKontak.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                String[] arr = {"Edit", "Followup"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(getContext(), LihatKontakActivity.class);
                                intent.putExtra("id", dataKontak.get(i).getId());
                                intent.putExtra("data", dataKontak.get(i).getJsonObject().toString());
                                startActivityForResult(intent, REQUEST_ADD);
                                break;
                            case 1:
                                dialogFollowup(i);
                                break;
                        }
                    }
                });
                builder.create();
                builder.show();


            }
        });
        listKontak.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listKontak == null || listKontak.getChildCount() == 0) ?
                                0 : listKontak.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    kontakAdapter.filter(edtCari.getText().toString().trim());
                    listKontak.invalidate();
                } catch (NullPointerException e) {
                    Log.i(TAG, e.getMessage());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        setHasOptionsMenu(true);
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadKontak();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadKontak();
            }
        });
        return view;
    }

    private void dialogFollowup(final int indexList) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        final LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_form_followup, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setTitle("Form Followup");

        final EditText edtCari = (EditText) dialogView.findViewById(R.id.edtCari);
        final ListView listFollowup = (ListView) dialogView.findViewById(R.id.listFollowup);
        final TextView labelMessage = (TextView) dialogView.findViewById(R.id.labelMessage);
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    followupAdapter.filter(edtCari.getText().toString().trim());
                    listFollowup.invalidate();
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        listFollowup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position >= 0) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Apakah anda yakin akan menambahkan ke list followup untuk kontak tersebut?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    tambahFollowup(dataFollowup.get(position).getId(), dataKontak.get(indexList).getId());
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();
                }
            }
        });
        loadFollowUp(listFollowup, labelMessage);

        dialog.setPositiveButton("Buat Baru", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(), FormFollowupActivity.class);
                intent.putExtra("contact_id", dataKontak.get(indexList).getId());
                intent.putExtra("contact_title", dataKontak.get(indexList).getJudul());
                intent.putExtra("contact_nomor", dataKontak.get(indexList).getNomorhp());
                intent.putExtra("tipe", "add");
                startActivity(intent);
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

    private void tambahFollowup(String id_follow_up, String id_kontak) {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_ADD_CONTACT_FOLLOW_UP)
                .buildUpon()
                .toString();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("id_follow_up", id_follow_up);
            parameter.put("id_kontak", id_kontak);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        pDialog.setMessage("Loading...");
        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(getContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            } else {
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void loadFollowUp(final ListView listFollowup, final TextView labelMessage) {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_FOLLOW_UP)
                .buildUpon()
                .toString();
        labelMessage.setText("Loading...");
        labelMessage.setVisibility(View.VISIBLE);
        dataFollowup.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                labelMessage.setVisibility(View.GONE);
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            final String id = data.getJSONObject(i).getString("id");
                            final String name = data.getJSONObject(i).getString("name");
                            dataFollowup.add(new ItemFollowup(id, name, data.getJSONObject(i), false, false));
                        }
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                    followupAdapter = new AdapterFollowup(dataFollowup, getContext());
                    listFollowup.setAdapter(followupAdapter);
                    if (dataFollowup.size() > 0) {
                        labelMessage.setVisibility(View.GONE);
                        listFollowup.setVisibility(View.VISIBLE);
                        labelMessage.setText("Data Followup tidak tersedia");
                    } else {
                        labelMessage.setVisibility(View.VISIBLE);
                        listFollowup.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    labelMessage.setVisibility(View.VISIBLE);
                    labelMessage.setText(e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                labelMessage.setVisibility(View.VISIBLE);
                labelMessage.setText(errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(getContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            } else {
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void loadKontak() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_CONTACT)
                .buildUpon()
                .toString();
        showError(false, "", true);
        swipe_refresh.setRefreshing(true);
        dataKontak.clear();
        //dataKontak.add(new ItemKontak("grupku","Grup Kontak","",false));
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            final String id = data.getJSONObject(i).getString("id");
                            final String first_name = data.getJSONObject(i).getString("first_name");
                            final String last_name = data.getJSONObject(i).getString("last_name");
                            String name;
                            if (last_name.isEmpty() || last_name.equals(null) || last_name.equals("null") || last_name == null) {
                                name = first_name;
                            } else {
                                name = first_name + " " + last_name;
                            }
                            final String phone = data.getJSONObject(i).getString("phone");
                            dataKontak.add(new ItemKontak(id, name, phone, false, data.getJSONObject(i)));
                        }
                        //Log.i(TAG,"data:" + data.toString());
                    } else {
                        showError(true, message, false);
                    }
                    displayKontak();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(true, e.getMessage(), true);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response == null) {
                    errorResponse(getContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            } else {
                                showError(true, msg, true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {

                        final String msg = getResources().getString(errorResponse(error));
                        showError(true, msg, true);
                    }
                }

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("X-API-KEY", token);
                return header;
            }

        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void showError(boolean show, String message, boolean visibleButton) {
        if (show) {
            layMessage.setVisibility(View.VISIBLE);
            listKontak.setVisibility(View.GONE);
            lblMessage.setText(message);
        } else {
            layMessage.setVisibility(View.GONE);
            listKontak.setVisibility(View.VISIBLE);
        }
        if (visibleButton) {
            btnCobaLagi.setVisibility(View.VISIBLE);
        } else {
            btnCobaLagi.setVisibility(View.GONE);
        }
    }

    private void displayKontak() {
        kontakAdapter = new AdapterKontak(dataKontak, getContext());
        listKontak.setAdapter(kontakAdapter);
        adapterInstance = true;
        labelStorage.setText("Penyimpanan (Akun Basic) : " + dataKontak.size() + " s.d " + limit_kontak);

        if (dataKontak.size() < limit_kontak) {
            progressStorage.setProgress((dataKontak.size() * 100) / limit_kontak);
        } else {
            progressStorage.setProgress(100);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actEditForExport).setVisible(true);
        menu.findItem(R.id.actImporKontak).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        menu.findItem(R.id.actImport).setVisible(true);
        menu.findItem(R.id.actExport).setVisible(false);
        if (adapterInstance) {
            listDefault();
        }
    }

    private void listDefault() {
        for (int i = 0; i < dataKontak.size(); i++) {
            ItemKontak ikontak = dataKontak.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataKontak.set(i, ikontak);
        }
        kontakAdapter.notifyDataSetChanged();
        if (dataKontak.size() == 0) {
            loadKontak();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menuTop = menu;
        inflater.inflate(R.menu.kontak, menuTop);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actTambah) {
            if (type.equals("1")) { // basic
                if (dataKontak.size() >= limit_kontak) {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Kuota Penyimpanan telah penuh, silahkan upgrade Akun Premium")
                            .setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = URL_DIRECT_LINK_UPGRADE;
                                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                    intent2.setData(Uri.parse(url));
                                    startActivity(intent2);
                                }
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                    return false;
                }
            }
            Intent i = new Intent(getContext(), FormKontakActivity.class);
            i.putExtra("tipe", "add");
            startActivityForResult(i, REQUEST_ADD);
        } else if (item.getItemId() == R.id.actEdit) {
            if (dataKontak.size() > 0) {
                menuTop.findItem(R.id.actImporKontak).setVisible(false);
                menuTop.findItem(R.id.actImport).setVisible(false);
                menuTop.findItem(R.id.actExport).setVisible(false);
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actEditForExport).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataKontak.size(); i++) {
                    ItemKontak ikontak = dataKontak.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataKontak.set(i, ikontak);
                }
                kontakAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Data Kontak tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actImporKontak).setVisible(true);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actEditForExport).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            menuTop.findItem(R.id.actImport).setVisible(true);
            menuTop.findItem(R.id.actExport).setVisible(false);
            listDefault();
        } else if (item.getItemId() == R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusKontak();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();

        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataKontak.size(); i++) {
                ItemKontak ikontak = dataKontak.get(i);
                ikontak.setCheckbox(true);
                dataKontak.set(i, ikontak);
            }
            kontakAdapter.notifyDataSetChanged();
        } else if (item.getItemId() == R.id.actImporKontak) {
            startActivityForResult(new Intent(getContext(), ImporKontakActivity.class), REQUEST_IMPORT);
        } else if (item.getItemId() == R.id.actImport) {
            if (type.equals("1")) {
                Toast.makeText(getContext(), "Fitur ini khusus untuk Akun Premium / Bisnis. Silahkan upgrade akun", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_IMPORT_CSV);
                } else {
                    Intent intent = new Intent();
                    //intent.setType("*/*");
                    intent.setType("text/*");

                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    startActivityForResult(Intent.createChooser(intent, "Select CSV"), REQUEST_IMPORT_CSV);
                }
            }
        } else if (item.getItemId() == R.id.actExport) {
            if (checkPermissionGallery()) {
                new AlertDialog.Builder(getContext())
                        .setMessage("Apakah anda yakin akan ekspor data berikut ke csv?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                exportCSV();
                            }
                        })
                        .setNegativeButton("Tidak", null)
                        .show();
            }

        } else if (item.getItemId() == R.id.actEditForExport) {
            if (dataKontak.size() > 0) {
                menuTop.findItem(R.id.actImporKontak).setVisible(false);
                menuTop.findItem(R.id.actImport).setVisible(false);
                menuTop.findItem(R.id.actExport).setVisible(true);
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(false);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actEditForExport).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataKontak.size(); i++) {
                    ItemKontak ikontak = dataKontak.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataKontak.set(i, ikontak);
                }
                kontakAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Data Kontak tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean checkPermissionGallery() {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHECK_PERMISSION);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void exportCSV() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dataKontak.size(); i++) {
            if (dataKontak.get(i).isCheckbox()) {
                jsonArray.put(Integer.parseInt(dataKontak.get(i).getId()));
            }
        }
        if (jsonArray.length() <= 0) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Tidak ada kontak yang dipilih untuk diekspor")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("contact_id", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_ALL_CONTACT)
                .buildUpon()
                .toString();
        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.e(TAG, "uri:" + uri);
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG, message);
                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        StringBuilder stringBuilder = new StringBuilder();

                        final String cnama_depan = "nama_depan";
                        final String cnama_belakang = "nama_belakang";
                        final String cemail = "email";
                        final String cphone = "phone";
                        final String csapaan = "sapaan";
                        final String ctelegram = "telegram";
                        final String caddress = "address";
                        final String cweb = "website";
                        final String cfb = "facebook";
                        final String cig = "instagram";
                        final String clinkedin = "linkedin";
                        final String ctokopedia = "tokopedia";
                        final String cbukalapak = "bukalapak";
                        final String cshopee = "shopee";
                        final String colshop_id = "olshop_id";
                        final String cgender = "gender";
                        final String cdate_of_birth = "date_of_birth";
                        final String cnote = "note";
                        final String cid_kota = "id_kota";
                        final String cid_wabot = "id_wabot";
                        final String cref_source = "ref_source";
                        final String ccucapan_ultah = "ucapan_ultah";
                        final String cnext_ultah = "next_ultah";
                        final String ccustomer_groups = "custmer_groups (json)";
                        final String chistori_kontak = "histori_kontak (json)";

                        stringBuilder.append(TextUtils.join(";", new String[]{cnama_depan, cnama_belakang, cemail, cphone, csapaan, ctelegram,
                                caddress, cweb, cfb, cig, clinkedin, ctokopedia, cbukalapak, cshopee, colshop_id, cgender, cdate_of_birth, cnote, cid_kota,
                                cid_wabot, cref_source, ccucapan_ultah, cnext_ultah, ccustomer_groups, chistori_kontak}));
                        stringBuilder.append("\n");

                        for (int i = 0; i < data.length(); i++) {
                            final String nama_depan = data.getJSONObject(i).getString("first_name");
                            final String nama_belakang = data.getJSONObject(i).getString("last_name");
                            final String email = data.getJSONObject(i).getString("email");
                            final String phone = data.getJSONObject(i).getString("phone");
                            final String sapaan = data.getJSONObject(i).getString("sapaan");
                            final String telegram = data.getJSONObject(i).getString("telegram");
                            final String address = data.getJSONObject(i).getString("address");
                            final String web = data.getJSONObject(i).getString("web");
                            final String fb = data.getJSONObject(i).getString("facebook");
                            final String ig = data.getJSONObject(i).getString("instagram");
                            final String linkedin = data.getJSONObject(i).getString("linkedin");
                            final String tokopedia = data.getJSONObject(i).getString("tokopedia");
                            final String bukalapak = data.getJSONObject(i).getString("bukalapak");
                            final String shopee = data.getJSONObject(i).getString("shopee");
                            final String olshop_id = data.getJSONObject(i).getString("olshop_id");
                            final String gender = data.getJSONObject(i).getString("gender");
                            final String date_of_birth = data.getJSONObject(i).getString("date_of_birth");
                            final String note = data.getJSONObject(i).getString("note");
                            final String id_kota = data.getJSONObject(i).getString("id_city");
                            final String id_wabot = data.getJSONObject(i).getString("id_wabot");
                            final String ref_source = data.getJSONObject(i).getString("ref_source");
                            final String ucapan_ultah = data.getJSONObject(i).getString("kirim_ultah");
                            final String next_ultah = data.getJSONObject(i).getString("next_ultah");
                            final JSONArray customer_groups = data.getJSONObject(i).getJSONArray("customer_groups");
                            final JSONArray histori_kontak = data.getJSONObject(i).getJSONArray("histori_kontak");

                            stringBuilder.append(TextUtils.join(";", new String[]{nama_depan, nama_belakang, email, phone, sapaan, telegram,
                                    address, web, fb, ig, linkedin, tokopedia, bukalapak, shopee, olshop_id, gender, date_of_birth, note, id_kota,
                                    id_wabot, ref_source, ucapan_ultah, next_ultah, customer_groups.toString(), histori_kontak.toString()}));
                            if (i < data.length() - 1) {
                                stringBuilder.append("\n");
                            }

                        }
                        if (stringBuilder.length() > 0) {
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                            String formattedDate = df.format(c);

                            String nama_file = "contact_" + formattedDate + ".csv";
                            createCSV(stringBuilder, nama_file);
                        } else {
                            Toast.makeText(getContext(), "Maaf, tidak ada kontak yang akan diekspor", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
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
                    errorResponse(getContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(getContext())
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
                        new AlertDialog.Builder(getContext())
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
                header.put("X-API-KEY", token);
                return header;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void createCSV(StringBuilder data, String nama_file) {
        //generate data

        try {
            //saving the file into device
            String dir = getDirWabot("export");
            String fileName = dir + "/" + nama_file;
            if (fileExist(getContext(), fileName) == false) {
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File myDir = new File(root + "/wabot/export/");
                myDir.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(new File(fileName)); //getActivity().openFileOutput(dir + "/data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actEditForExport).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            menuTop.findItem(R.id.actImporKontak).setVisible(true);
            menuTop.findItem(R.id.actImport).setVisible(true);
            menuTop.findItem(R.id.actExport).setVisible(false);
            new AlertDialog.Builder(getContext())
                    .setMessage("Export selesai. Path : " + fileName)
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show();
            //exporting
            //Context context = getContext();
            //File filelocation = new File(getDirWabot("export") , "data.csv");
//            Uri path = FileProvider.getUriForFile(context, "com.example.exportcsv.fileprovider", filelocation);
//            Intent fileIntent = new Intent(Intent.ACTION_SEND);
//            fileIntent.setType("text/csv");
//            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
//            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            fileIntent.putExtra(Intent.EXTRA_STREAM, fileName);
//            startActivity(Intent.createChooser(fileIntent, "Send mail"));
        } catch (Exception e) {
            e.printStackTrace();
            String stackTrace = Log.getStackTraceString(e);
            new AlertDialog.Builder(getContext())
                    .setMessage(stackTrace)
                    .setPositiveButton("OK", null)
                    .setCancelable(false)
                    .show();
        }


    }

    private void hapusKontak() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dataKontak.size(); i++) {
            if (dataKontak.get(i).isCheckbox()) {
                jsonArray.put(Integer.parseInt(dataKontak.get(i).getId()));
            }
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_HAPUS_CONTACT)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG, message);
                    if (status) {
                        for (int i = 0; i < dataKontak.size(); i++) {
                            if (dataKontak.get(i).isCheckbox()) {
                                dataKontak.remove(i);
                                i = i - 1;
                            }
                        }
                        kontakAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actEditForExport).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        menuTop.findItem(R.id.actImporKontak).setVisible(true);
                        menuTop.findItem(R.id.actImport).setVisible(true);
                        menuTop.findItem(R.id.actExport).setVisible(false);
                        listDefault();
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
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
                    errorResponse(getContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(getContext())
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
                        new AlertDialog.Builder(getContext())
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
                header.put("X-API-KEY", token);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD) {
            if (resultCode == RESULT_OK) {
                loadKontak();
            }
        } else if (requestCode == REQUEST_IMPORT) {
            if (resultCode == RESULT_OK) {
                loadKontak();
            }
        } else if (requestCode == REQUEST_IMPORT_CSV) {


            String tableName = "proinfo";
            final JSONArray dataImport = new JSONArray();
            try {
                if (resultCode == RESULT_OK) {
                    if (data == null) {
                        //no data present
                        return;
                    }
                    try {
                        Log.e(TAG, "getData:" + data.getData());
                        BufferedReader buffer = readCSV(data.getData());
                        String line = "";
                        int i = 0;
                        while ((line = buffer.readLine()) != null) {
                            if (i == 0) {
                                i++;
                                continue;
                            }
                            String[] str = line.split(";", 25);  // defining 3 columns with null or blank field //values acceptance
                            //Id, Company,Name,Price
                            String nama_depan = str[0];
                            String nama_belakang = str[1];
                            String email = str[2];
                            String phone = str[3];
                            String sapaan = str[4];
                            String telegram = str[5];
                            String address = str[6];
                            String web = str[7];
                            String fb = str[8];
                            String ig = str[9];
                            String linkedin = str[10];
                            String tokopedia = str[11];
                            String bukalapak = str[12];
                            String shopee = str[13];
                            String olshop_id = str[14];
                            String gender = str[15];
                            String date_of_birth = str[16];
                            String note = str[17];
                            String id_kota = str[18];
                            String id_wabot = str[19];
                            String ref_source = str[20];
                            String ucapan_ultah = str[21];
                            String next_ultah = str[22];
                            String customer_groups = str[23];
                            String histori_kontak = str[24];

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("k_nama_depan", nama_depan);
                            jsonObject.put("k_nama_belakang", nama_belakang);
                            jsonObject.put("k_email", email);
                            jsonObject.put("k_phone", phone);
                            jsonObject.put("k_sapaan", sapaan);
                            jsonObject.put("k_telegram", telegram);
                            jsonObject.put("k_address", address);
                            jsonObject.put("k_web", web);
                            jsonObject.put("k_fb", fb);
                            jsonObject.put("k_ig", ig);
                            jsonObject.put("k_linkedin", linkedin);
                            jsonObject.put("k_tokopedia", tokopedia);
                            jsonObject.put("k_bukalapak", bukalapak);
                            jsonObject.put("k_shopee", shopee);
                            jsonObject.put("k_olshop_id", olshop_id);
                            jsonObject.put("k_gender", gender);
                            jsonObject.put("k_date_of_birth", date_of_birth);
                            jsonObject.put("k_note", note);
                            jsonObject.put("id_kota", id_kota);
                            jsonObject.put("id_wabot", id_wabot);
                            jsonObject.put("k_ref_source", ref_source);
                            jsonObject.put("k_ucapan_ultah", ucapan_ultah);
                            jsonObject.put("k_next_ultah", next_ultah);
                            jsonObject.put("group", new JSONArray(customer_groups));
                            jsonObject.put("histori_kontak", new JSONArray((histori_kontak)));

                            dataImport.put(jsonObject);
                        }
                        new AlertDialog.Builder(getContext())
                                .setMessage("Apakah anda yakin akan import data ?")
                                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        importToDB(dataImport);
                                    }
                                })
                                .setNegativeButton("Tidak", null)
                                .show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(getContext())
                                .setMessage(e.getMessage())
                                .setPositiveButton("OK", null)
                                .show();
                        // db.endTransaction();
                    }
                } else {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Only CSV files allowed")
                            .setPositiveButton("OK", null)
                            .show();
                }
            } catch (Exception e) {
                Log.e(TAG, "File select error", e);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private BufferedReader readCSV(Uri uri) {
        ContentResolver contentResolver = getContext().getContentResolver();
        BufferedReader result = null;
        try {
            InputStream csvFile = contentResolver.openInputStream(uri);
            InputStreamReader isr = new InputStreamReader(csvFile);
            result = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }

    private void importToDB(JSONArray dataImport) {
        if (dataImport.length() <= 0) {
            Toast.makeText(getContext(), "Tidak ada data yang akan diimport", Toast.LENGTH_SHORT).show();
            return;
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("import", dataImport);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_IMPORT_CONTACT_CSV)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang import data...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.e(TAG, request_body.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    if (status) {
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        loadKontak();
                                    }
                                })
                                .setCancelable(false)
                                .show();
                    } else {
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
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
                    errorResponse(getContext(), error);
                } else {
                    if (response.statusCode == 403) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.data.toString());
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")) {
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / telah login di perangkat lain.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                session.clearData();
                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                getActivity().finish();
                                            }
                                        })
                                        .show();
                            } else {
                                new AlertDialog.Builder(getContext())
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
                        new AlertDialog.Builder(getContext())
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
                header.put("X-API-KEY", token);
                return header;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

}
