package id.co.kamil.autochat.ui.grup;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import id.co.kamil.autochat.adapter.AdapterGrup;
import id.co.kamil.autochat.adapter.ItemGrup;
import id.co.kamil.autochat.utils.SessionManager;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_GRUP;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_GRUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class GrupFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private static final int REQUEST_ADD = 100;
    private static final String TAG = "GrupFragment";
    private EditText edtCari;
    private ListView listGrup;
    private List<ItemGrup> dataGrup = new ArrayList<>();
    private AdapterGrup grupAdapter;
    private Menu menuTop;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private boolean adapterInstance = false;
    private SwipeRefreshLayout swipe_refresh;

    public GrupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GrupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GrupFragment newInstance(String param1, String param2) {
        GrupFragment fragment = new GrupFragment();
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
        View view = inflater.inflate(R.layout.fragment_grup, container, false);
        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(getContext());
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        layMessage = (LinearLayout) view.findViewById(R.id.layMessage);
        lblMessage = (TextView) view.findViewById(R.id.lblMessage);
        btnCobaLagi = (Button) view.findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadGrup();
            }
        });
        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listGrup = (ListView) view.findViewById(R.id.listGrup);
        listGrup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), ListGrupKontakActivity.class);
                intent.putExtra("id", dataGrup.get(i).getId());
                intent.putExtra("data", dataGrup.get(i).getJsonObject().toString());
                startActivityForResult(intent, REQUEST_ADD);

            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    grupAdapter.filter(edtCari.getText().toString().trim());
                    listGrup.invalidate();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listGrup.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listGrup == null || listGrup.getChildCount() == 0) ?
                                0 : listGrup.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadGrup();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadGrup();
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    private void loadGrup() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_GRUP)
                .buildUpon()
                .toString();
        showError(false, "", true);
        swipe_refresh.setRefreshing(true);
        dataGrup.clear();
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
                            final String name = data.getJSONObject(i).getString("name");
                            final String description = data.getJSONObject(i).getString("description");
                            dataGrup.add(new ItemGrup(id, name, description, data.getJSONObject(i)));
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
            listGrup.setVisibility(View.GONE);
            lblMessage.setText(message);
        } else {
            layMessage.setVisibility(View.GONE);
            listGrup.setVisibility(View.VISIBLE);
        }
        if (visibleButton) {
            btnCobaLagi.setVisibility(View.VISIBLE);
        } else {
            btnCobaLagi.setVisibility(View.GONE);
        }
    }

    private void displayGrup() {
        grupAdapter = new AdapterGrup(dataGrup, getContext());
        listGrup.setAdapter(grupAdapter);
        adapterInstance = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actTambah) {
            Intent i = new Intent(getContext(), FormGrupActivity.class);
            i.putExtra("tipe", "add");
            startActivityForResult(i, REQUEST_ADD);
        } else if (item.getItemId() == R.id.actEdit) {
            if (dataGrup.size() > 0) {
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataGrup.size(); i++) {
                    ItemGrup ikontak = dataGrup.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataGrup.set(i, ikontak);
                }
                grupAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Data Grup tidak tersedia", Toast.LENGTH_SHORT).show();
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
                            hapusGrup();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();

        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataGrup.size(); i++) {
                ItemGrup ikontak = dataGrup.get(i);
                ikontak.setCheckbox(true);
                dataGrup.set(i, ikontak);
            }
            grupAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void listDefault() {
        for (int i = 0; i < dataGrup.size(); i++) {
            ItemGrup ikontak = dataGrup.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataGrup.set(i, ikontak);
        }
        grupAdapter.notifyDataSetChanged();
        if (dataGrup.size() == 0) {
            loadGrup();
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

    private void hapusGrup() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JSONArray idHapus = new JSONArray();
        for (int i = 0; i < dataGrup.size(); i++) {
            if (dataGrup.get(i).isCheckbox()) {
                idHapus.put(Integer.parseInt(dataGrup.get(i).getId()));
            }
        }
        final JSONObject param = new JSONObject();
        try {
            param.put("id", idHapus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_HAPUS_GRUP)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG, "hapusKontak:" + param.toString());

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, param, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        for (int i = 0; i < dataGrup.size(); i++) {
                            if (dataGrup.get(i).isCheckbox()) {
                                dataGrup.remove(i);
                                i = i - 1;
                            }
                        }
                        grupAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        listDefault();
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
                loadGrup();
            }
        }
    }
}
