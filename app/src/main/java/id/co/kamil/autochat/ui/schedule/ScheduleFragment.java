package id.co.kamil.autochat.ui.schedule;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import id.co.kamil.autochat.adapter.AdapterPesan;
import id.co.kamil.autochat.adapter.AdapterSchedule;
import id.co.kamil.autochat.adapter.ItemPesan;
import id.co.kamil.autochat.adapter.ItemSchedule;
import id.co.kamil.autochat.utils.SessionManager;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_HAPUS_SCHEDULE;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_SCHEDULE;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;


public class ScheduleFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "ScheduleFragment";
    private static final int REQUEST_ADD = 100;

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

    private List<ItemSchedule> dataSchedule = new ArrayList<>();

    private AdapterSchedule adapterSchedule;
    private ListView listSchedule;
    private Menu menuTop;
    private boolean adapterInstance = false;
    private View view;

    public ScheduleFragment() {
        // Required empty public constructor
    }

    public static ScheduleFragment newInstance(String param1, String param2) {
        ScheduleFragment fragment = new ScheduleFragment();
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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_schedule, container, false);

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
                loadPesan();
            }
        });

        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listSchedule = (ListView) view.findViewById(R.id.listKontak);
        listSchedule.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(),FormScheduleActivity.class);
                intent.putExtra("is_new",false);
                if(dataSchedule.get(i).isGroup()){
                    intent.putExtra("tipe","grup");
                }else{
                    intent.putExtra("tipe","kontak");
                }
                intent.putExtra("id",dataSchedule.get(i).getId());
                startActivityForResult(intent,REQUEST_ADD);
            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    adapterSchedule.filter(edtCari.getText().toString().trim());
                    listSchedule.invalidate();
                }catch (NullPointerException e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listSchedule.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int topRowVerticalPosition =
                        (listSchedule == null || listSchedule.getChildCount() == 0) ?
                                0 : listSchedule.getChildAt(0).getTop();
                swipe_refresh.setEnabled(i == 0 && topRowVerticalPosition >= 0);
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

        return view;
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
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance){
            listDefault();
        }
    }
    private void listDefault(){
        for (int i = 0; i < dataSchedule.size(); i++){
            ItemSchedule ikontak = dataSchedule.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataSchedule.set(i,ikontak);
        }
        adapterSchedule.notifyDataSetChanged();
        if (dataSchedule.size()==0){
            loadPesan();
        }

    }
    private void loadPesan(){

        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final String uri = Uri.parse(URL_POST_LIST_SCHEDULE)
                .buildUpon()
                .toString();
        showError(false,"",true);
        swipe_refresh.setRefreshing(true);
        dataSchedule.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0 ;i<data.length();i++){

                            final String id = data.getJSONObject(i).getString("id");
                            final String next_schedule = data.getJSONObject(i).getString("next_schedule");
                            final String tipe = data.getJSONObject(i).getString("time_type");
                            final String group_id = data.getJSONObject(i).getString("group_id");
                            final String group_name = data.getJSONObject(i).getString("group_name");
                            final String contact_name = data.getJSONObject(i).getString("contact_name");
                            final String msg = data.getJSONObject(i).getString("message");
                            final String status_pesan = data.getJSONObject(i).getString("status");
                            String name;
                            boolean isGroup = true;
                            if (group_id.equals(null) || group_id == null || group_id.equals("null")){
                                name = contact_name;
                                isGroup = false;
                            }else{
                                name = group_name;
                                isGroup = true;
                            }
                            dataSchedule.add(new ItemSchedule(id,name,next_schedule,tipe,msg,status_pesan, isGroup,false,false));
                        }
                    }else{
                        Log.i(TAG,response.toString());
                        showError(true,message,false);
                    }
                    displayPesan();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(true,e.getMessage(),true);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(getContext(),error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
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
                            }else{
                                showError(true,msg,true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        showError(true,msg,true);
                    }
                }


            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("x-api-key",token);
                return header;
            }
        };
        RetryPolicy policy = new DefaultRetryPolicy(SOCKET_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);
        requestQueue.add(jsonObjectRequest);
    }

    private void showError(boolean show,String message, boolean visibleButton){
        if (show){
            layMessage.setVisibility(View.VISIBLE);
            listSchedule.setVisibility(View.GONE);
            lblMessage.setText(message);
        }else{
            layMessage.setVisibility(View.GONE);
            listSchedule.setVisibility(View.VISIBLE);
        }
        if (visibleButton){
            btnCobaLagi.setVisibility(View.VISIBLE);
        }else{
            btnCobaLagi.setVisibility(View.GONE);
        }
    }
    private void displayPesan() {
        adapterSchedule = new AdapterSchedule(dataSchedule,getContext());
        listSchedule.setAdapter(adapterSchedule);
        adapterInstance = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_ADD){
            if(resultCode==RESULT_OK){
                loadPesan();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.actTambah){
            String[] arr = {"Buat per Grup Kontak", "Buat per Kontak"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setItems(arr, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    switch (which){
                        case 0:
                            Intent intent = new Intent(getContext(), FormScheduleActivity.class);
                            intent.putExtra("tipe","grup");
                            intent.putExtra("is_new",true);
                            startActivityForResult(intent,REQUEST_ADD);
                            break;
                        case 1:
                            Intent intent2 = new Intent(getContext(), FormScheduleActivity.class);
                            intent2.putExtra("tipe","kontak");
                            intent2.putExtra("is_new",true);
                            startActivityForResult(intent2,REQUEST_ADD);
                            break;
                    }
                }
            });
            builder.create();
            builder.show();
        }else if (item.getItemId()==R.id.actEdit) {
            if (dataSchedule.size()>0){
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                for (int i = 0; i < dataSchedule.size(); i++){
                    ItemSchedule ikontak = dataSchedule.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataSchedule.set(i,ikontak);
                }
                adapterSchedule.notifyDataSetChanged();
            }else{
                Toast.makeText(getContext(), "Data antrian pesan tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        }else if (item.getItemId()==R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            listDefault();
        }else if (item.getItemId()==R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus data berikut?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapusPesan();
                        }
                    })
                    .setNegativeButton("Tidak",null)
                    .show();

        }else if (item.getItemId()==R.id.actSemua) {
            for (int i = 0; i < dataSchedule.size(); i++){
                ItemSchedule ikontak = dataSchedule.get(i);
                ikontak.setCheckbox(true);
                dataSchedule.set(i,ikontak);
            }
            adapterSchedule.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
    private void hapusPesan() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < dataSchedule.size(); i++){
            if (dataSchedule.get(i).isCheckbox() ){
                jsonArray.put(Integer.parseInt(dataSchedule.get(i).getId()));
            }
        }
        final JSONObject request_body = new JSONObject();
        try {
            request_body.put("id",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG,request_body.toString());
        final String uri = Uri.parse(URL_POST_HAPUS_SCHEDULE)
                .buildUpon()
                .toString();
        pDialog.setMessage("Sedang menghapus data...");
        pDialog.setCancelable(false);
        pDialog.show();

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, request_body , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");
                    Log.i(TAG,message);
                    if (status){
                        for (int i = 0; i < dataSchedule.size(); i++){
                            if (dataSchedule.get(i).isCheckbox()){
                                dataSchedule.remove(i);
                                i = i - 1;
                            }
                        }
                        adapterSchedule.notifyDataSetChanged();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(false);
                        listDefault();
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }else{
                        new AlertDialog.Builder(getContext())
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getContext())
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hidePdialog();
                Log.i(TAG,errorResponseString(error));
                NetworkResponse response = error.networkResponse;
                if (response == null){
                    errorResponse(getContext(),error);
                }else{
                    if (response.statusCode==403){
                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            final boolean status = jsonObject.getBoolean("status");
                            final String msg = jsonObject.getString("error");
                            if (msg.trim().toLowerCase().equals("invalid api key")){
                                new AlertDialog.Builder(getContext())
                                        .setMessage("Session telah habias / akun telah login di perangkat lain.")
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
                            }else{
                                new AlertDialog.Builder(getContext())
                                        .setMessage(msg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else{

                        final String msg = getResources().getString(errorResponse(error));
                        new AlertDialog.Builder(getContext())
                                .setMessage(msg)
                                .setCancelable(false)
                                .setPositiveButton("OK", null)
                                .show();
                    }
                }

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String,String> header = new HashMap<>();
                //header.put("Content-Type","application/json");
                header.put("x-api-key",token);
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
