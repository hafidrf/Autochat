package id.co.kamil.autochat.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.adapter.AdapterPeringkat;
import id.co.kamil.autochat.adapter.ItemPeringkat;
import id.co.kamil.autochat.utils.ExpandableHeightListView;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.API.DESKRIPSI_INFO;
import static id.co.kamil.autochat.utils.API.SOCKET_TIMEOUT;
import static id.co.kamil.autochat.utils.API.TEMPLATE_SHARE;
import static id.co.kamil.autochat.utils.API.URL_DOWNLINE;
import static id.co.kamil.autochat.utils.API.URL_LANDING_PAGE;
import static id.co.kamil.autochat.utils.API.URL_POST_REFERAL;
import static id.co.kamil.autochat.utils.SessionManager.KEY_TOKEN;
import static id.co.kamil.autochat.utils.Utils.errorResponse;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class AffiliasiActivity extends AppCompatActivity implements ViewTreeObserver.OnScrollChangedListener {
    private EditText edtKodeReferal;
    private ImageButton btnBagikanPlaystore;
    private Button btnDownline;
    private EditText edtLinkPlaystore;
    private EditText edtLinkWeb;
    private ImageButton btnBagikanWeb;
    private ImageButton btnCopyPlaystore,btnCopyWeb;
    private TextView txtInfo;
    private ImageButton btnShareKode;
    private ImageButton btnCopyKode;
    private LinearLayout groupPlaystore;
    private LinearLayout groupWeb;
    private ProgressDialog pDialog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String token;
    private String template_share = "";
    private List<ItemPeringkat> dataPeringkat = new ArrayList<>();
    private ExpandableHeightListView listPeringkat;
    private SwipeRefreshLayout swipe_refresh;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affiliasi);
        getSupportActionBar().setTitle("Affiliasi");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        token = userDetail.get(KEY_TOKEN);

        pDialog = new ProgressDialog(this);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        groupPlaystore = (LinearLayout) findViewById(R.id.groupPlaystore);
        groupWeb = (LinearLayout) findViewById(R.id.groupWeb);

        txtInfo = (TextView) findViewById(R.id.txtInfo);
        edtKodeReferal = (EditText) findViewById(R.id.edtKodeMarketing);
        edtLinkPlaystore = (EditText) findViewById(R.id.edtPlaystore);
        edtLinkWeb = (EditText) findViewById(R.id.edtLinkWeb);

        listPeringkat =  (ExpandableHeightListView) findViewById(R.id.listPeringkat);
        btnShareKode =  (ImageButton) findViewById(R.id.btnShareKode);
        btnCopyKode = (ImageButton) findViewById(R.id.btnCopyKode);
        btnCopyPlaystore = (ImageButton) findViewById(R.id.btnCopyPlaystore);
        btnCopyWeb = (ImageButton) findViewById(R.id.btnCopyWeb);
        btnBagikanPlaystore = (ImageButton) findViewById(R.id.btnSharePlaystore);
        btnBagikanWeb = (ImageButton) findViewById(R.id.btnShareWeb);
        btnDownline = (Button) findViewById(R.id.btnDownline);
        btnCopyPlaystore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(AffiliasiActivity.this,edtLinkPlaystore.getText().toString());
                Toast.makeText(AffiliasiActivity.this, "link berhasil disalin", Toast.LENGTH_SHORT).show();
            }
        });
        btnShareKode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String konten = edtKodeReferal.getText().toString();
                    String appId = getPackageName();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = konten;
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Bagikan lewat"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });
        btnCopyKode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(AffiliasiActivity.this,edtKodeReferal.getText().toString());
                Toast.makeText(AffiliasiActivity.this, "link berhasil disalin", Toast.LENGTH_SHORT).show();
            }
        });
        btnCopyWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setClipboard(AffiliasiActivity.this,edtLinkWeb.getText().toString());
                Toast.makeText(AffiliasiActivity.this, "link berhasil disalin", Toast.LENGTH_SHORT).show();
            }
        });
        btnBagikanPlaystore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String konten = template_share;
                    konten = konten.replace("[linklanding]",edtLinkPlaystore.getText().toString());
                    konten = konten.replace("[linkweb]",edtLinkWeb.getText().toString());

                    String appId = getPackageName();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = konten;
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Bagikan lewat"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });
        btnBagikanWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String konten = template_share;
                    konten = konten.replace("[linklanding]",edtLinkPlaystore.getText().toString());
                    konten = konten.replace("[linkweb]",edtLinkWeb.getText().toString());

                    String appId = getPackageName();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    String sAux = konten;
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Bagikan lewat"));
                } catch(Exception e) {
                    //e.toString();
                }
            }
        });
        btnDownline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = URL_DOWNLINE;
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(url));
                startActivity(intent2);
            }
        });
        //setTextViewHTML(txtInfo,DESKRIPSI_INFO);
        swipe_refresh.post(new Runnable() {
            @Override
            public void run() {
                loadDataReferal();
            }
        });
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipe_refresh.setRefreshing(true);
                loadDataReferal();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDataReferal() {
        dataPeringkat.clear();
        template_share = TEMPLATE_SHARE;
        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        final HashMap<String,String> body = new HashMap<>();


        final String uri = Uri.parse(URL_POST_REFERAL)
                .buildUpon()
                .toString();
        swipe_refresh.setRefreshing(true);
//        pDialog.setMessage("Loading...");
//        pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                requestQueue.stop();
//            }
//        });
//        pDialog.show();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, uri, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                swipe_refresh.setRefreshing(false);
                //hidePdialog();
                try {
                    final boolean status = response.getBoolean("status");
                    final String message = response.getString("message");

                    if (status){

                        final String kode_referal = response.getString("kode_referal");
                        final String template = response.getString("template");
                        final String info = response.getString("info");
                        final JSONArray dataPeringkat = response.getJSONArray("dataPeringkat");

                        setTextViewHTML(txtInfo,info);

                        if (dataPeringkat.length()>0){
                            for(int i =0;i<dataPeringkat.length();i++){
                                final String itemPeringkat = dataPeringkat.getJSONObject(i).getString("peringkat");
                                final String itemNama = dataPeringkat.getJSONObject(i).getString("nama");
                                final String itemEmail = dataPeringkat.getJSONObject(i).getString("email");
                                final String itemDownline = dataPeringkat.getJSONObject(i).getString("downline");

                                AffiliasiActivity.this.dataPeringkat.add(new ItemPeringkat(itemPeringkat,itemEmail,itemNama,itemDownline));
                            }
                        }
                        template_share  = template;
                        edtKodeReferal.setText(kode_referal);
                        String url_track_web = Uri.parse(URL_LANDING_PAGE)
                                .buildUpon()
                                .appendQueryParameter("tracking",edtKodeReferal.getText().toString())
                                .toString();
                        edtLinkWeb.setText(url_track_web);
                        String url_track_playstore = Uri.parse("https://play.google.com/store/apps/details?id=id.co.kamil.autochat")
                                .buildUpon()
                                .appendQueryParameter("referrer",edtKodeReferal.getText().toString())
                                .toString();
                        edtLinkPlaystore.setText(url_track_playstore);
                    }else{
                        new AlertDialog.Builder(AffiliasiActivity.this)
                                .setMessage(message)
                                .setPositiveButton("OK",null)
                                .show();
                    }
                    displayPeringkat();
                } catch (JSONException e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(AffiliasiActivity.this)
                            .setMessage(e.getMessage())
                            .setPositiveButton("OK",null)
                            .show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipe_refresh.setRefreshing(false);
                //hidePdialog();
                final String msg = getResources().getString(errorResponse(error));
                new AlertDialog.Builder(AffiliasiActivity.this)
                        .setMessage(msg)
                        .setPositiveButton("OK",null)
                        .show();

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

    private void displayPeringkat() {
        final AdapterPeringkat adapterPeringkat = new AdapterPeringkat(this,R.layout.item_list_peringkat, dataPeringkat);
        listPeringkat.setAdapter(adapterPeringkat);
        listPeringkat.setExpanded(true);
    }

    private void hidePdialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span)
    {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        final ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                // Do something with span.getURL() to handle the link click...
                String url = span.getURL();
                Intent intent2 = new Intent(Intent.ACTION_VIEW);
                intent2.setData(Uri.parse(url));
                startActivity(intent2);
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    protected void setTextViewHTML(TextView text, String html)
    {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for(URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }
    @Override
    public void onStart() {
        super.onStart();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        scrollView.getViewTreeObserver().removeOnScrollChangedListener(this);
    }
    @Override
    public void onScrollChanged() {
        int scrollY = scrollView.getScrollY();
        if (scrollY==0){
            swipe_refresh.setEnabled(true);
        }else {
            if (!swipe_refresh.isRefreshing()){
                swipe_refresh.setEnabled(false);
            }
        }
    }
}
