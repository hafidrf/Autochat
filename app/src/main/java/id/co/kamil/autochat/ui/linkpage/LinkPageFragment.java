package id.co.kamil.autochat.ui.linkpage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterLinkpage;
import id.co.kamil.autochat.adapter.ItemLinkpage;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.LIMIT_LINKPAGE_BASIC;
import static id.co.kamil.autochat.utils.API.LIMIT_LINKPAGE_PREMIUM;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_LINKPAGE;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_LINKPAGE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class LinkPageFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_ADD = 100;
    private static final String TAG = "LinkpageFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DBHelper dbHelper;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private EditText edtCari;
    private ListView listWaform;
    private Menu menuTop;
    private AdapterLinkpage waformAdapter;
    private boolean adapterInstance = false;
    private List<ItemLinkpage> dataWaform = new ArrayList<>();
    private String type;
    private int limit_pesan = 0;
    private SharPref sharePref;
    private TextView labelStorage;
    private ProgressBar progressStorage;
    private int count_number = 0;

    public LinkPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LinkPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LinkPageFragment newInstance(String param1, String param2) {
        LinkPageFragment fragment = new LinkPageFragment();
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
        View view = inflater.inflate(R.layout.fragment_link_page, container, false);
        dbHelper = new DBHelper(getContext());
        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        type = userDetail.get(KEY_CUST_GROUP);
        sharePref = new SharPref(getContext());

        if (type.equals("1")) {
            limit_pesan = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_LINKPAGE_BASIC));
            if (limit_pesan <= 0) {
                limit_pesan = LIMIT_LINKPAGE_BASIC;
            }
        } else if (type.equals("2")) {
            limit_pesan = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_WAFORM_PREMIUM));
            if (limit_pesan <= 0) {
                limit_pesan = LIMIT_LINKPAGE_PREMIUM;
            }
        }
        labelStorage = (TextView) view.findViewById(R.id.labelStorage);
        progressStorage = (ProgressBar) view.findViewById(R.id.progressStorage);

        if (type.equals("6")) { // basic
            progressStorage.setVisibility(View.GONE);
            labelStorage.setVisibility(View.GONE);
        } else {
            progressStorage.setVisibility(View.VISIBLE);
            labelStorage.setVisibility(View.VISIBLE);
        }
        pDialog = new ProgressDialog(getContext());
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        layMessage = (LinearLayout) view.findViewById(R.id.layMessage);
        lblMessage = (TextView) view.findViewById(R.id.lblMessage);
        btnCobaLagi = (Button) view.findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLinkpage();
            }
        });
        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listWaform = (ListView) view.findViewById(R.id.listLinkpage);
        listWaform.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                String[] arr = {"Edit", "Buka link", "Salin", "Bagikan"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(getContext(), FormLinkPageActivity.class);
                                intent.putExtra("id", dataWaform.get(i).getId());
                                intent.putExtra("tipe", "edit");
                                startActivityForResult(intent, REQUEST_ADD);
                                break;
                            case 1:
                                String url = dataWaform.get(i).getUrl();
                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                intent2.setData(Uri.parse(url));
                                startActivity(intent2);
                                break;
                            case 2:
                                setClipboard(getContext(), dataWaform.get(i).getUrl());
                                Toast.makeText(getContext(), "berhasil disalin", Toast.LENGTH_SHORT).show();
                                break;
                            case 3:
                                String konten = dataWaform.get(i).getUrl();
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, konten);
                                shareIntent.setType("text/plain");
                                startActivity(Intent.createChooser(shareIntent, "Bagikan lewat"));
                                break;
                        }
                    }
                });
                builder.create();
                builder.show();


            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    waformAdapter.filter(edtCari.getText().toString().trim());
                    listWaform.invalidate();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listWaform.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listWaform == null || listWaform.getChildCount() == 0) ?
                                0 : listWaform.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadLinkpage();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadLinkpage();
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    private void loadLinkpage() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_LINKPAGE)
                .buildUpon()
                .toString();
        showError(false, "", true);
        swipe_refresh.setRefreshing(true);
        dataWaform.clear();
        JSONObject parameters = new JSONObject();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameters, new Response.Listener<JSONObject>() {
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
                            final String created = data.getJSONObject(i).getString("created");
                            final String name = data.getJSONObject(i).getString("name");
                            final String url = data.getJSONObject(i).getString("url");
                            final String domain_code = data.getJSONObject(i).getString("domain_code");
                            final String domain = data.getJSONObject(i).getString("domain");
                            JSONArray field = new JSONArray();
                            if (data.getJSONObject(i).isNull("data") == false) {
                                field = data.getJSONObject(i).getJSONArray("data");
                            }
                            dataWaform.add(new ItemLinkpage(id, name, domain_code, domain, created, url, field, data.getJSONObject(i), false, false));
                        }
                    } else {
                        showError(true, message, false);
                    }
                    displayGrup();
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
                //header.put("Authorization","Bearer " + token);
                header.put("x-api-key", token);
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
            listWaform.setVisibility(View.GONE);
            lblMessage.setText(message);
        } else {
            layMessage.setVisibility(View.GONE);
            listWaform.setVisibility(View.VISIBLE);
        }
        if (visibleButton) {
            btnCobaLagi.setVisibility(View.VISIBLE);
        } else {
            btnCobaLagi.setVisibility(View.GONE);
        }
    }

    private void displayGrup() {
        waformAdapter = new AdapterLinkpage(dataWaform, getContext());
        listWaform.setAdapter(waformAdapter);
        adapterInstance = true;
        count_number = dataWaform.size();

        if (type.equals("1")) { // basic
            labelStorage.setText("Penyimpanan (Akun Basic) : " + count_number + " s.d " + limit_pesan + " (page)");

            if (count_number < limit_pesan) {
                progressStorage.setProgress((count_number * 100) / limit_pesan);
            } else {
                progressStorage.setProgress(100);
            }
        } else if (type.equals("2")) {
            labelStorage.setText("Penyimpanan (Akun Premium) : " + count_number + " s.d " + limit_pesan + " (page)");

            if (count_number < limit_pesan) {
                progressStorage.setProgress((count_number * 100) / limit_pesan);
            } else {
                progressStorage.setProgress(100);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actTambah) {
            if (!type.equals("6") && count_number >= limit_pesan) {
                Toast.makeText(getContext(), "Linkpage sudah limit, silahkan upgrade akun", Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(getContext(), FormLinkPageActivity.class);
                i.putExtra("tipe", "add");
                startActivityForResult(i, REQUEST_ADD);
            }
        } else if (item.getItemId() == R.id.actEdit) {
            if (dataWaform.size() > 0) {
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataWaform.size(); i++) {
                    ItemLinkpage ikontak = dataWaform.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataWaform.set(i, ikontak);
                }
                waformAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Data Template tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            listDefault();
        } else if (item.getItemId() == R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusTemplate();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();

        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataWaform.size(); i++) {
                ItemLinkpage ikontak = dataWaform.get(i);
                ikontak.setCheckbox(true);
                dataWaform.set(i, ikontak);
            }
            waformAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void listDefault() {
        for (int i = 0; i < dataWaform.size(); i++) {
            ItemLinkpage ikontak = dataWaform.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataWaform.set(i, ikontak);
        }
        waformAdapter.notifyDataSetChanged();
        if (dataWaform.size() == 0) {
            loadLinkpage();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance) {
            listDefault();
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menuTop = menu;
        inflater.inflate(R.menu.kontak, menuTop);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void hapusTemplate() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JSONArray idHapus = new JSONArray();
        for (int i = 0; i < dataWaform.size(); i++) {
            if (dataWaform.get(i).isCheckbox()) {
                idHapus.put(Integer.parseInt(dataWaform.get(i).getId()));
            }
        }
        final JSONObject param = new JSONObject();
        try {
            param.put("id", idHapus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_HAPUS_LINKPAGE)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        for (int i = 0; i < dataWaform.size(); i++) {
                            if (dataWaform.get(i).isCheckbox()) {
                                dataWaform.remove(i);
                                i = i - 1;
                            }
                        }
                        waformAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        listDefault();
                        loadLinkpage();
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
                //header.put("Authorization","Bearer " + token);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD) {
            if (resultCode == RESULT_OK) {
                loadLinkpage();
            }
        }
    }
}
