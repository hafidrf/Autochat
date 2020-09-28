package id.co.kamil.autochat.ui.shorten;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterShorten;
import id.co.kamil.autochat.adapter.ItemShorten;
import id.co.kamil.autochat.utils.SessionManager;
import id.co.kamil.autochat.utils.SharPref;

import static android.app.Activity.RESULT_OK;
import static id.co.kamil.autochat.utils.API.LIMIT_SHORTEN;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.URL_POST_DELETE_SHORTEN;
import static id.co.kamil.autochat.utils.API.URL_POST_LIST_SHORTEN;
import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_GROUP;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.convertDpToPixel;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.errorResponseString;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class WAShortenFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_ADD = 100;
    private static final String TAG = "ShortenFragment";
    private static final int REQUEST_QRCODE = 101;
    private static final int PERMISSION_REQUEST_CAMERA = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText edtCari;
    private ListView listShorten;
    private List<ItemShorten> dataShorten = new ArrayList<>();
    private Menu menuTop;
    private AdapterShorten shortenAdapter;

    public final static int QRcodeWidth = 500;
    private LinearLayout layMessage;
    private TextView lblMessage;
    private Button btnCobaLagi;
    private SwipeRefreshLayout swipe_refresh;

    private boolean adapterInstance = false;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private ProgressDialog pDialog;
    private SharPref sharePref;
    private int limit_shorten;
    private TextView labelStorage;
    private ProgressBar progressStorage;
    private String type;
    private int count_number = 0;

    public WAShortenFragment() {
        // Required empty public constructor
    }

    public static WAShortenFragment newInstance(String param1, String param2) {
        WAShortenFragment fragment = new WAShortenFragment();
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
        View view = inflater.inflate(R.layout.fragment_washorten, container, false);
        session = new SessionManager(getContext());
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);
        sharePref = new SharPref(getContext());
        type = userDetail.get(KEY_CUST_GROUP);
        limit_shorten = Integer.parseInt(sharePref.getSessionStr(SharPref.KEY_LIMIT_SHORTEN));
        if (limit_shorten <= 0) {
            limit_shorten = LIMIT_SHORTEN;
        }
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
                loadShorten();
            }
        });
        pDialog = new ProgressDialog(getContext());

        swipe_refresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        edtCari = (EditText) view.findViewById(R.id.edtCari);
        listShorten = (ListView) view.findViewById(R.id.listShorten);
        listShorten.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        edtCari.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    shortenAdapter.filter(edtCari.getText().toString().trim());
                    listShorten.invalidate();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        listShorten.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                String[] arr = {"Tampilkan QRCode", "Buka link", "Edit", "Salin"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setItems(arr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case 0:
                                final LayoutInflater inflater = getLayoutInflater();
                                final View dialogLayout = inflater.inflate(R.layout.item_qrcode, null);
                                final String loc_qrcode = "https://autochat.id/assets/qrcode/" + dataShorten.get(i).getSubdomaincode() + ".png";
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) convertDpToPixel(200, getContext()));
                                ImageView img = (ImageView) dialogLayout.findViewById(R.id.imageView);
                                //img.setLayoutParams(layoutParams);

                                //img.setScaleType(ImageView.ScaleType.FIT_XY);
                                Picasso.with(getContext()).load(loc_qrcode).placeholder(R.drawable.ic_image).error(R.drawable.ic_image).into(img);

//                                final Bitmap bitmap;
//                                try {
//                                    bitmap = TextToImageEncode(dataShorten.get(i).getDomain());
//                                    img.setImageBitmap(bitmap);
//                                } catch (WriterException e) {
//                                    e.printStackTrace();
//                                }

                                new AlertDialog.Builder(getContext())
                                        .setView(dialogLayout)
                                        .setNeutralButton("Download", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i2) {
                                                String url = "https://autochat.id/download/qrcode/" + dataShorten.get(i).getId();
                                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                                intent2.setData(Uri.parse(url));
                                                startActivity(intent2);
                                            }
                                        })
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;
                            case 1:
                                String url = dataShorten.get(i).getDomain();
                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                intent2.setData(Uri.parse(url));
                                startActivity(intent2);
                                break;
                            case 2:
                                Intent intent = new Intent(getContext(), FormShortenLinkActivity.class);
                                intent.putExtra("tipe", "edit");
                                intent.putExtra("id", dataShorten.get(i).getId());
                                startActivityForResult(intent, REQUEST_ADD);
                                break;
                            case 3:
                                setClipboard(getContext(), dataShorten.get(i).getDomain());
                                Toast.makeText(getContext(), "berhasil disalin", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                builder.create();
                builder.show();

            }
        });
        setHasOptionsMenu(true);

        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                swipe_refresh.setRefreshing(true);
                loadShorten();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadShorten();
            }
        });

        return view;
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.md_black_1000) : getResources().getColor(R.color.md_white_1000);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    private void showError(boolean show, String message, boolean visibleButton) {
        if (show) {
            layMessage.setVisibility(View.VISIBLE);
            listShorten.setVisibility(View.GONE);
            lblMessage.setText(message);
        } else {
            layMessage.setVisibility(View.GONE);
            listShorten.setVisibility(View.VISIBLE);
        }
        if (visibleButton) {
            btnCobaLagi.setVisibility(View.VISIBLE);
        } else {
            btnCobaLagi.setVisibility(View.GONE);
        }
    }

    private void hapus() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        final JSONObject requestBody = new JSONObject();
        try {
            final JSONArray jsonArray = new JSONArray();

            for (int i = 0; i < dataShorten.size(); i++) {
                if (dataShorten.get(i).isCheckbox()) {
                    jsonArray.put(Integer.parseInt(dataShorten.get(i).getId()));
                }
            }
            requestBody.put("id", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String uri = Uri.parse(URL_POST_DELETE_SHORTEN)
                .buildUpon()
                .toString();


        pDialog.setMessage("Loading...");
        pDialog.setCancelable(false);
        pDialog.show();
        Log.i(TAG, requestBody.toString());
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        menuTop.findItem(R.id.actBatal).setVisible(false);
                        menuTop.findItem(R.id.actHapus).setVisible(false);
                        menuTop.findItem(R.id.actSemua).setVisible(false);
                        menuTop.findItem(R.id.actEdit).setVisible(true);
                        menuTop.findItem(R.id.actTambah).setVisible(true);
                        listDefault();
                        loadShorten();
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
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(getContext())
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                        .show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
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

    private void loadShorten() {
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        final String uri = Uri.parse(URL_POST_LIST_SHORTEN)
                .buildUpon()
                .toString();
        showError(false, "", true);
        swipe_refresh.setRefreshing(true);
        dataShorten.clear();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);

                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status) {
                        count_number = response.getInt("count_number");
                        final JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            final String id = data.getJSONObject(i).getString("s_id");
                            final String domain = data.getJSONObject(i).getString("s_domain");
                            final String subdomain = data.getJSONObject(i).getString("s_subdomain");
                            final String subdomaincode = data.getJSONObject(i).getString("s_subdomain_code");
                            final String totalklik = data.getJSONObject(i).getString("s_klik");
                            dataShorten.add(new ItemShorten(id, domain + subdomain, subdomaincode, totalklik));
                        }
                    } else {
                        showError(true, message, false);
                    }
                    displayShorten();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showError(true, e.getMessage(), true);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                final String msg = getResources().getString(errorResponse(error));
                showError(true, msg, true);
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

    private void displayShorten() {
        shortenAdapter = new AdapterShorten(dataShorten, getContext());
        listShorten.setAdapter(shortenAdapter);
        adapterInstance = true;
        if (type.equals("1")) { // basic
            labelStorage.setText("Penyimpanan (Akun Basic) : " + count_number + " s.d " + limit_shorten + " (custom domain)");

            if (count_number < limit_shorten) {
                progressStorage.setProgress((count_number * 100) / limit_shorten);
            } else {
                progressStorage.setProgress(100);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menuTop = menu;
        inflater.inflate(R.menu.shorten, menuTop);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.actEdit).setVisible(true);
        menu.findItem(R.id.actTambah).setVisible(true);
        menu.findItem(R.id.actScan).setVisible(true);
        menu.findItem(R.id.actBatal).setVisible(false);
        menu.findItem(R.id.actHapus).setVisible(false);
        menu.findItem(R.id.actSemua).setVisible(false);
        if (adapterInstance) {
            listDefault();
        }
    }

    private void listDefault() {
        for (int i = 0; i < dataShorten.size(); i++) {
            ItemShorten ikontak = dataShorten.get(i);
            ikontak.setCheckbox(false);
            ikontak.setChkvisible(false);
            dataShorten.set(i, ikontak);
        }
        shortenAdapter.notifyDataSetChanged();
        if (dataShorten.size() == 0) {
            loadShorten();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actTambah) {
            Intent i = new Intent(getContext(), FormShortenLinkActivity.class);
            i.putExtra("tipe", "add");
            startActivityForResult(i, REQUEST_ADD);
        } else if (item.getItemId() == R.id.actEdit) {
            if (dataShorten.size() > 0) {
                menuTop.findItem(R.id.actBatal).setVisible(true);
                menuTop.findItem(R.id.actHapus).setVisible(true);
                menuTop.findItem(R.id.actSemua).setVisible(true);
                menuTop.findItem(R.id.actEdit).setVisible(false);
                menuTop.findItem(R.id.actTambah).setVisible(false);
                menuTop.findItem(R.id.actScan).setVisible(false);
                for (int i = 0; i < dataShorten.size(); i++) {
                    ItemShorten ikontak = dataShorten.get(i);
                    ikontak.setChkvisible(!ikontak.isChkvisible());
                    dataShorten.set(i, ikontak);
                }
                if (adapterInstance) {
                    shortenAdapter.notifyDataSetChanged();
                }
            } else {
                Toast.makeText(getContext(), "Data WA Shortenlink tidak tersedia", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == R.id.actBatal) {
            menuTop.findItem(R.id.actBatal).setVisible(false);
            menuTop.findItem(R.id.actHapus).setVisible(false);
            menuTop.findItem(R.id.actSemua).setVisible(false);
            menuTop.findItem(R.id.actEdit).setVisible(true);
            menuTop.findItem(R.id.actTambah).setVisible(true);
            menuTop.findItem(R.id.actScan).setVisible(true);
            listDefault();
        } else if (item.getItemId() == R.id.actHapus) {
            new AlertDialog.Builder(getContext())
                    .setMessage("Apakah anda yakin akan menghapus shortenlink berikut ? ")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            hapus();
                        }
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        } else if (item.getItemId() == R.id.actSemua) {
            for (int i = 0; i < dataShorten.size(); i++) {
                ItemShorten ikontak = dataShorten.get(i);
                ikontak.setCheckbox(true);
                dataShorten.set(i, ikontak);
            }
            if (adapterInstance) {
                shortenAdapter.notifyDataSetChanged();
            }
        } else if (item.getItemId() == R.id.actScan) {
            requestCameraPermission();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestCameraPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Toast.makeText(getContext(), "Camera access is required to display the camera preview.", Toast.LENGTH_SHORT).show();
            /*Snackbar.make(mLayout, "",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();*/
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            startActivityForResult(new Intent(getContext(), ScanQrActivity.class), REQUEST_QRCODE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD) {
            if (resultCode == RESULT_OK) {
                loadShorten();
            }
        } else if (requestCode == REQUEST_QRCODE) {
            if (resultCode == RESULT_OK) {
                final String qrcode = data.getStringExtra("result");
                new AlertDialog.Builder(getContext())
                        .setMessage(qrcode)
                        .setPositiveButton("Buka Link", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String url = qrcode;
                                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                intent2.setData(Uri.parse(url));
                                startActivity(intent2);
                            }
                        })
                        .setCancelable(false)
                        .setNegativeButton("Batal", null)
                        .show();
            }
        }
    }
}
