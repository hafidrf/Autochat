package id.co.kamil.autochat.ui.notification;

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
import id.co.kamil.autochat.adapter.AdapterNotif;
import id.co.kamil.autochat.adapter.ItemNotif;
import id.co.kamil.autochat.utils.SessionManager;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_DELETE_NOTIF_FIREBASE;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_NOTIF_FIREBASE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class NotifFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_ADD = 100;
    private static final String TAG = "NotifFragment";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SwipeRefreshLayout swipe_refresh;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private EditText edtCari;
    private ListView listNotif;
    private Menu menuTop;
    private boolean adapterInstance = false;
    private AdapterNotif notifAdapter;
    private List<ItemNotif> dataNotif = new ArrayList<>();

    public NotifFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotifFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotifFragment newInstance(String param1, String param2) {
        NotifFragment fragment = new NotifFragment();
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
        View view = inflater.inflate(R.layout.fragment_notif, container, false);
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
                loadTemplate();
            }
        });
        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listNotif = (ListView) view.findViewById(R.id.listNotif);
        listNotif.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(), FormTemplateNotifActivity.class);
                intent.putExtra("id", dataNotif.get(i).getId());
                intent.putExtra("tipe", "edit");
                intent.putExtra("data", dataNotif.get(i).getJson().toString());
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
                    notifAdapter.filter(edtCari.getText().toString().trim());
                    listNotif.invalidate();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        listNotif.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listNotif == null || listNotif.getChildCount() == 0) ?
                                0 : listNotif.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
            }
        });
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadTemplate();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadTemplate();
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    private void loadTemplate() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_NOTIF_FIREBASE)
                .buildUpon()
                .toString();
        showError(false, "", true);
        swipe_refresh.setRefreshing(true);
        dataNotif.clear();
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
                            final String title = data.getJSONObject(i).getString("title");
                            final String body = data.getJSONObject(i).getString("body");
                            final String url = data.getJSONObject(i).getString("url");
                            dataNotif.add(new ItemNotif(id, title, body, url, data.getJSONObject(i), false, false));
                        }
                    } else {
                        showError(true, message, false);
                    }
                    displayNotif();
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
            listNotif.setVisibility(View.GONE);
            lblMessage.setText(message);
        } else {
            layMessage.setVisibility(View.GONE);
            listNotif.setVisibility(View.VISIBLE);
        }
        if (visibleButton) {
            btnCobaLagi.setVisibility(View.VISIBLE);
        } else {
            btnCobaLagi.setVisibility(View.GONE);
        }
    }

    private void displayNotif() {
        notifAdapter = new AdapterNotif(dataNotif, getContext());
        listNotif.setAdapter(notifAdapter);
        adapterInstance = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actTambah) {
            Intent i = new Intent(getContext(), FormTemplateNotifActivity.class);
            i.putExtra("tipe", "add");
            startActivityForResult(i, REQUEST_ADD);
        } else if (item.getItemId() == R.id.actEdit) {
            if (dataNotif.size() > 0) {
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                menuTop.findItem(R.id.actSend).setVisible(false);
                menuTop.findItem(R.id.actSendTemplate).setVisible(false);
                for (int i = 0; i < dataNotif.size(); i++) {
                    ItemNotif ikontak = dataNotif.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataNotif.set(i, ikontak);
                }
                notifAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Data Template tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            menuTop.findItem(R.id.actSend).setVisible(true);
            menuTop.findItem(R.id.actSendTemplate).setVisible(true);
            listDefault();
        } else if (item.getItemId() == R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusNotif();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();

        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataNotif.size(); i++) {
                ItemNotif ikontak = dataNotif.get(i);
                ikontak.setCheckbox(true);
                dataNotif.set(i, ikontak);
            }
            notifAdapter.notifyDataSetChanged();
        } else if (item.getItemId() == R.id.actSend) {
            startActivity(new Intent(getContext(), KirimNotifActivity.class));
        } else if (item.getItemId() == R.id.actSendTemplate) {
            startActivity(new Intent(getContext(), KirimNotifTemplateActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void listDefault() {
        for (int i = 0; i < dataNotif.size(); i++) {
            ItemNotif ikontak = dataNotif.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataNotif.set(i, ikontak);
        }
        notifAdapter.notifyDataSetChanged();
        if (dataNotif.size() == 0) {
            loadTemplate();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        menu.findItem(R.id.actSend).setVisible(true);
        menu.findItem(R.id.actSendTemplate).setVisible(true);
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

    private void hapusNotif() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JSONArray idHapus = new JSONArray();
        for (int i = 0; i < dataNotif.size(); i++) {
            if (dataNotif.get(i).isCheckbox()) {
                idHapus.put(Integer.parseInt(dataNotif.get(i).getId()));
            }
        }
        final JSONObject param = new JSONObject();
        try {
            param.put("id", idHapus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_DELETE_NOTIF_FIREBASE)
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
                        for (int i = 0; i < dataNotif.size(); i++) {
                            if (dataNotif.get(i).isCheckbox()) {
                                dataNotif.remove(i);
                                i = i - 1;
                            }
                        }
                        notifAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        menuTop.findItem(R.id.actSend).setVisible(true);
                        menuTop.findItem(R.id.actSendTemplate).setVisible(true);
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
                loadTemplate();
            }
        }
    }

}
