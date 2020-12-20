package id.co.kamil.autochat.ui.pesan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import com.google.android.material.snackbar.Snackbar;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.LoginActivity;
import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterPesan;
import id.co.kamil.autochat.adapter.ItemPesan;
import id.co.kamil.autochat.bulksender.WASendService;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static id.co.kamil.autochat.utils.API.LIMIT_PESAN;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_PESAN_ANTRIAN;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_SEMUA_PESAN_ANTRIAN;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_ANTRIAN_PESAN;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.SharPref.TRY_AGAIN_BULKSENDER;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;

public class AntrianPesanFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "AntrianPesanFragment";

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

    private List<ItemPesan> dataPesan = new ArrayList<>();

    private AdapterPesan pesanAdapter;
    private ListView listPesan;
    private Menu menuTop;
    private boolean adapterInstance = false;
    private View view;
    private TextView labelStorage;
    private ProgressBar progressStorage;
    private String type;
    private int count_number = 0;
    private SharPref sharePref;
    private int limit_pesan;
    private DBHelper dbHelper;
    private Button btnDeleteAll;
    private int current_page = 1;

    private boolean isLoading = false, isloadMore = true;
    private MyHandler mHandler;
    private View ftView;

    public AntrianPesanFragment() {
        // Required empty public constructor
    }

    public static AntrianPesanFragment newInstance(String param1, String param2) {
        AntrianPesanFragment fragment = new AntrianPesanFragment();
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
        view = inflater.inflate(R.layout.fragment_antrian_pesan, container, false);
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ftView = li.inflate(R.layout.footer_view_loading, null);

        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        dbHelper = new DBHelper(getContext());
        token = userDetail.get(KEY_TOKEN);
        type = userDetail.get(KEY_CUST_GROUP);
        sharePref = new SharPref(getContext());

        saveParseOutbox(session.getValue(KEY_CUST_ID));
        limit_pesan = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_PESAN));
        if (limit_pesan <= 0) {
            limit_pesan = LIMIT_PESAN;
        }
        mHandler = new MyHandler();

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
        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        layMessage = (LinearLayout) view.findViewById(R.id.layMessage);
        lblMessage = (TextView) view.findViewById(R.id.lblMessage);
        btnDeleteAll = (Button) view.findViewById(R.id.btnDeleteAll);
        btnCobaLagi = (Button) view.findViewById(R.id.btnCobaLagi);
        btnCobaLagi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPesan();
            }
        });

        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listPesan = (ListView) view.findViewById(R.id.listKontak);
        listPesan.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                        dataPesan.get(i).getError_again(),
                        Toast.LENGTH_SHORT);
                toast.show();
//                Intent intent = new Intent(getContext(),FormPesanActivity.class);
//                intent.putExtra("tipe","edit");
//                intent.putExtra("id",dataPesan.get(i).getId());
//                startActivityForResult(intent,REQUEST_ADD);
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    pesanAdapter.filter(edtCari.getText().toString().trim());
                    listPesan.invalidate();
                } catch (NullPointerException e) {
                    Log.i(TAG, e.getMessage());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listPesan.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listPesan == null || listPesan.getChildCount() == 0) ?
                                0 : listPesan.getChildAt(0).getTop();
                swipe_refresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                if (absListView.getLastVisiblePosition() == totalItemCount - 1 && listPesan.getCount() >= 10 && isLoading == false && isloadMore) {
                    isLoading = true;
                    loadMore(current_page + 1);
                }
            }
        });
        setHasOptionsMenu(true);
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadPesan();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPesan();
            }
        });
        if (isAccessibilityEnabled() == false) {
            View v = (CoordinatorLayout) getActivity().findViewById(R.id.layMaster);
            showSnackBar(v);
        }
        btnDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    new AlertDialog.Builder(getContext())
                            .setMessage("Apakah anda yakin akan menghapus semua data diantrian pesan?")
                            .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteAll();
                                }
                            })
                            .setNegativeButton("Tidak", null)
                            .show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    private void saveParseOutbox(final String user_id) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Outbox");
        query.whereEqualTo("KeyCust",user_id);
        // Or use the the non-blocking method countInBackground method with a CountCallback
        query.countInBackground(new CountCallback() {
            public void done(int count, ParseException e) {
                if (e == null) {
                    if(count<=0){
                        ParseObject entity = new ParseObject("Outbox");
                        entity.put("KeyCust", user_id);
                        // Saves the new object.
                        // Notice that the SaveCallback is totally optional!
                        entity.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                // Here you can handle errors, if thrown. Otherwise, "e" should be null
                            }
                        });
                    }
                    Log.i(TAG,"count"+count);
                } else {
                    Log.i(TAG,e.getMessage());
                }
            }
        });
    }
    private void deleteAll() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        JSONObject bodyRequest = new JSONObject();
        try {
            bodyRequest.put("status", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String uri = Uri.parse(URL_POST_HAPUS_SEMUA_PESAN_ANTRIAN)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, bodyRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG, message);
                    if (status) {
                        loadPesan();
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
                header.put("x-api-key", token);
                return header;
            }
        };

        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }
    /*private void deleteParseOutboxMessage(final String idmessage) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("OutboxMessage");
        query.whereEqualTo("idmessage",idmessage);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject entity, ParseException e) {
                if (e == null) {

                    String keyCust=String.valueOf(entity.get("KeyCust"));
                    entity.deleteInBackground();
                    if(pesanAdapter!=null){
                        if(pesanAdapter.getCount()<=0){
                            ParseQuery<ParseObject> query = ParseQuery.getQuery("Outbox");
                            query.whereEqualTo("KeyCust",keyCust);
                            query.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    object.deleteInBackground();
                                }
                            });
                        }
                    }
                }
            }
        });
    }
    private void deleteParseAntrian() {
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Outbox");
        parseQuery.whereEqualTo("KeyCust", session.getValue(KEY_CUST_ID));
        final ParseQuery<ParseObject> parseQueryMessage = ParseQuery.getQuery("OutboxMessage");
        parseQueryMessage.whereEqualTo("KeyCust", session.getValue(KEY_CUST_ID));

        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e==null){
                    parseQueryMessage.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            ParseObject.deleteAllInBackground(objects, new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    loadPesan();
                                }
                            });
                        }
                    });
                }else{
                    object.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            parseQueryMessage.findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objects, ParseException e) {
                                    ParseObject.deleteAllInBackground(objects, new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            loadPesan();
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }
        });
    }*/

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    listPesan.addFooterView(ftView);
                    break;
                case 1:
                    pesanAdapter.addListItemToAdapter((ArrayList<ItemPesan>) msg.obj);
                    listPesan.removeFooterView(ftView);
                    isLoading = false;
                    break;
                case 3:
                    listPesan.removeFooterView(ftView);
                    isLoading = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void loadMore(final int page) {

        mHandler.sendEmptyMessage(0);
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("status", "pending");
            parameter.put("page", page);
            parameter.put("limit", 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_ANTRIAN_PESAN)
                .buildUpon()
                .toString();


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    count_number = response.getInt("count_number");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        List<ItemPesan> dataPesanTemp = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {

                            final String id = data.getJSONObject(i).getString("id");
                            final String created_at = data.getJSONObject(i).getString("created");
                            final String phone = data.getJSONObject(i).getString("to");
                            final String name = data.getJSONObject(i).getString("name");
                            final String msg = data.getJSONObject(i).getString("message");
                            final String status_pesan = data.getJSONObject(i).getString("status");

                            ItemPesan itemPesan = new ItemPesan(id, phone, name, created_at, msg, status_pesan);
                            dataPesanTemp.add(itemPesan);
                        }
                        current_page++;
                        Message msg = mHandler.obtainMessage(1, dataPesanTemp);
                        mHandler.sendMessage(msg);
                    } else {
                        mHandler.sendEmptyMessage(3);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(3);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mHandler.sendEmptyMessage(3);
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
        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menuTop = menu;
        inflater.inflate(R.menu.kontak, menuTop);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(false);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance) {
            listDefault();
        }
    }

    private void listDefault() {
        if (dataPesan.size() == 0) {
            loadPesan();
        }
        for (int i = 0; i < dataPesan.size(); i++) {
            ItemPesan ikontak = dataPesan.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataPesan.set(i, ikontak);
        }
        pesanAdapter.notifyDataSetChanged();
    }

    private void _loadPesan() {
        swipe_refresh.setRefreshing(true);
        dataPesan.clear();
        dbHelper = new DBHelper(getContext());
        List<String[]> antrianPesan = dbHelper.getAntrianPesan();
        for (String[] pesan : antrianPesan) {
            final String id = pesan[0];
            final String created_at = pesan[10];
            final String phone = pesan[1];
            final String name = pesan[11];
            final String msg = pesan[2];
            final String status_pesan = pesan[4];
            dataPesan.add(new ItemPesan(id, phone, name, created_at, msg, status_pesan));
        }
        displayPesan();
    }

    private void loadPesan() {
        current_page = 1;
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("status", "pending");
            parameter.put("page", current_page);
            parameter.put("limit", 10);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_ANTRIAN_PESAN)
                .buildUpon()
                .toString();
        showError(false, "", true);
        swipe_refresh.setRefreshing(true);
        dataPesan.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, parameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    count_number = response.getInt("count_number");

                    if (status) {
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {

                            final String id = data.getJSONObject(i).getString("id");
                            final String created_at = data.getJSONObject(i).getString("created");
                            final String phone = data.getJSONObject(i).getString("to");
                            final String name = data.getJSONObject(i).getString("name");
                            final String msg = data.getJSONObject(i).getString("message");
                            final String status_pesan = data.getJSONObject(i).getString("status");

                            ItemPesan itemPesan = new ItemPesan(id, phone, name, created_at, msg, status_pesan);
                            dataPesan.add(itemPesan);
                        }
                    } else {
                        Log.i(TAG, response.toString());
                        showError(true, message, false);
                    }
                    displayPesan();
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
                                new android.app.AlertDialog.Builder(getContext())
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
                header.put("x-api-key", token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    public void showSnackBar(View llShow) {
        // Create the Snackbar
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Snackbar snackbar = Snackbar.make(llShow, "", Snackbar.LENGTH_INDEFINITE);
        // Get the Snackbar's layout view
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        layout.setPadding(0, 0, 0, 0);
        // Hide the text
        TextView textView = (TextView) layout.findViewById(R.id.snackbar_text);
        textView.setVisibility(View.INVISIBLE);

        LayoutInflater mInflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate our custom view
        View snackView = getLayoutInflater().inflate(R.layout.my_snackbar, null);
        // Configure the view
        TextView textViewOne = (TextView) snackView.findViewById(R.id.txtOne);
        TextView textMessage = (TextView) snackView.findViewById(R.id.txtMessage);
        textMessage.setText(getString(R.string.warning_disable_accessibility));
        textViewOne.setText(getString(R.string.snack_enable_service));
        textViewOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(i);
            }
        });

        ImageView imgClose = (ImageView) snackView.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        // Add the view to the Snackbar's layout
        layout.addView(snackView, objLayoutParams);
        // Show the Snackbar
        snackbar.show();
    }

    private boolean isAccessibilityEnabled() {
        int enabled = 0;
        final String service = getActivity().getPackageName() + "/" + WASendService.class.getCanonicalName();

        try {
            enabled = Settings.Secure.getInt(getActivity().getApplicationContext().getContentResolver()
                    , Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        if (enabled == 1) {
            String settingValue = Settings.Secure.getString(
                    getActivity().getApplicationContext().getContentResolver()
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

    private void showError(boolean show, String message, boolean visibleButton) {
        if (show) {
            layMessage.setVisibility(View.VISIBLE);
            listPesan.setVisibility(View.GONE);
            lblMessage.setText(message);
        } else {
            layMessage.setVisibility(View.GONE);
            listPesan.setVisibility(View.VISIBLE);
        }
        if (visibleButton) {
            btnCobaLagi.setVisibility(View.VISIBLE);
        } else {
            btnCobaLagi.setVisibility(View.GONE);
        }
    }

    private void displayPesan() {
        pesanAdapter = new AdapterPesan(dataPesan, getContext());
        listPesan.setAdapter(pesanAdapter);
        adapterInstance = true;
        if (type.equals("1")) { // basic
            labelStorage.setText("Penyimpanan (Akun Basic) : " + count_number + " s.d " + limit_pesan + " (nomor)");

            if (count_number < limit_pesan) {
                progressStorage.setProgress((count_number * 100) / limit_pesan);
            } else {
                progressStorage.setProgress(100);
            }
        }
        if (dataPesan.size() > 0) {
            btnDeleteAll.setEnabled(true);
        } else {
            btnDeleteAll.setEnabled(false);
        }
        changeErrorAgain();
    }

    private void changeErrorAgain() {
        String prefTryagain = sharePref.getSessionStr(TRY_AGAIN_BULKSENDER);
        if (TextUtils.isEmpty(prefTryagain)) {
            prefTryagain = "5";
        }
        final int maxTryAgain = Integer.parseInt(prefTryagain);

        ParseLiveQueryClient parseLiveQueryClient = null;
        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://dash.wabot.id:1337"));
        } catch (URISyntaxException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("OutboxMessage");
        parseQuery.whereEqualTo("KeyCust", session.getValue(KEY_CUST_ID));

        SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(final ParseQuery<ParseObject> parseQuery, final ParseObject object) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            String id = object.get("idmessage")+"";
                            String error_again = object.get("error_again")+"";
                            for (int i = 0; i < dataPesan.size(); i++) {
                                if (dataPesan.get(i).getId().equals(id)) {
                                    ItemPesan itemPesan = dataPesan.get(i);
                                    itemPesan.setError_again(error_again);
                                    dataPesan.set(i, itemPesan);
                                }
                            }
                            for (ItemPesan itemPesan : dataPesan) {
                                if (!TextUtils.isEmpty(itemPesan.getError_again())) {
                                    if (Integer.parseInt(itemPesan.getError_again()) > maxTryAgain) {
                                        hapusPesan(itemPesan.getId());
                                    }
                                }
                            }
                            pesanAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
            @Override
            public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            String id = object.get("idmessage")+"";
                            String error_again = object.get("error_again")+"";
                            for (int i = 0; i < dataPesan.size(); i++) {
                                if (dataPesan.get(i).getId().equals(id)) {
                                    ItemPesan itemPesan = dataPesan.get(i);
                                    itemPesan.setError_again(error_again);
                                    dataPesan.set(i, itemPesan);
                                }
                            }
                            for (ItemPesan itemPesan : dataPesan) {
                                if (!TextUtils.isEmpty(itemPesan.getError_again())) {
                                    if (Integer.parseInt(itemPesan.getError_again()) > maxTryAgain) {
                                        hapusPesan(itemPesan.getId());
                                    }
                                }
                            }
                            pesanAdapter.notifyDataSetChanged();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actEdit) {
            if (dataPesan.size() > 0) {
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataPesan.size(); i++) {
                    ItemPesan ikontak = dataPesan.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataPesan.set(i, ikontak);
                }
                pesanAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Data antrian pesan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(false);
            listDefault();
        } else if (item.getItemId() == R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusPesan();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();

        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataPesan.size(); i++) {
                ItemPesan ikontak = dataPesan.get(i);
                ikontak.setCheckbox(true);
                dataPesan.set(i, ikontak);
            }
            pesanAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hapusPesan(final String id) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                final JSONArray jsonArray = new JSONArray();

                jsonArray.put(Integer.parseInt(id));
                Log.d("deletedari", "antrian pesan");
                final JSONObject request_body = new JSONObject();
                try {
                    request_body.put("id", jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, request_body.toString());
                final String uri = Uri.parse(URL_POST_HAPUS_PESAN_ANTRIAN)
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
                                Log.d("deletedari", "antrian pesan - berhasil delete");
                                loadPesan();
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                pesanAdapter.notifyDataSetChanged();
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
                        header.put("x-api-key", token);
                        return header;
                    }
                };

                RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jsonObjectRequest.setRetryPolicy(policy);
                requestQueue.add(jsonObjectRequest);
            }
        });
    }

    private void hapusPesan() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dataPesan.size(); i++) {
            if (dataPesan.get(i).isCheckbox()) {
                jsonArray.put(Integer.parseInt(dataPesan.get(i).getId()));
            }
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, request_body.toString());
        final String uri = Uri.parse(URL_POST_HAPUS_PESAN_ANTRIAN)
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
                        int i = 0;
                        do {
                            if (dataPesan.get(i).isCheckbox()) {
                                dataPesan.remove(i);
                            } else {
                                i++;
                            }
                        } while (i < dataPesan.size());

                        pesanAdapter.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(false);
                        listDefault();
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        loadPesan();
                    } else {
                        int i = 0;
                        do {
                            if (dataPesan.get(i).isCheckbox()) {
                                dataPesan.remove(i);
                            } else {
                                i++;
                            }
                        } while (i < dataPesan.size());
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

}
