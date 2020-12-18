package id.co.kamil.autochat.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;

import id.co.kamil.autochat.R;
import id.co.kamil.autochat.database.DBHelper;
import id.co.kamil.autochat.utils.SessionManager;

import static id.co.kamil.autochat.utils.SessionManager.KEY_CUST_ID;

public class LogActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private List<String[]> dataLog;
    private TextView labelLog;
    private SessionManager session;
    private HashMap<String, String> userDetail;
    private String user_id;
    private Button btnClear;
    private Button btnReload;
    private ProgressDialog pDialog;
    private EditText edtPage;
    private Button btnBack, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().setTitle("Log");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        labelLog = (TextView) findViewById(R.id.labelLog);

        dbHelper = new DBHelper(this);
        session = new SessionManager(this);
        userDetail = session.getUserDetails();
        user_id = userDetail.get(KEY_CUST_ID);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnReload = (Button) findViewById(R.id.btnRefresh);
        btnBack = (Button) findViewById(R.id.back);
        btnNext = (Button) findViewById(R.id.next);
        edtPage = (EditText) findViewById(R.id.page);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Integer.valueOf(edtPage.getText().toString()) > 1) {
                    edtPage.setText(String.valueOf(Integer.valueOf(edtPage.getText().toString()) - 1));
                    getLog(Integer.valueOf(edtPage.getText().toString()), 50);
                } else {
                    edtPage.setText("1");
                }
                btnNext.setEnabled(true);
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLog(Integer.parseInt(edtPage.getText().toString()) + 1, 50);
            }
        });
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLog(Integer.parseInt(edtPage.getText().toString()), 50);
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(LogActivity.this)
                        .setMessage("Apakah anda yakin akan clear history log?")
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.deleteAllLog(user_id);
                                edtPage.setText("1");
                                getLog(Integer.parseInt(edtPage.getText().toString()), 50);
                            }
                        })
                        .setNegativeButton("Tidak", null)
                        .show();

            }
        });
        pDialog = new ProgressDialog(this);

        getLog(Integer.parseInt(edtPage.getText().toString()), 50);


    }

    private void getLog(int page, int limit) {
        pDialog.setMessage("Loading...");
        pDialog.show();
        dataLog = dbHelper.getLog(user_id, page, limit);
        if (dataLog.size() > 0) {
            edtPage.setText(String.valueOf(page));
        } else {
            btnNext.setEnabled(false);
        }
        String tmp = "";
        tmp += "<p style=\"color:yellow\">DB Version : " + DBHelper.getDatabaseVersion() + "</p>\n\n\n";

        for (int i = 0; i < dataLog.size(); i++) {
            String created = dataLog.get(i)[1];
            String service = dataLog.get(i)[2];
            String log = dataLog.get(i)[3];
            String status = dataLog.get(i)[4];
            if (status.equals("normal")) {
                tmp += "<p style=\"color:yellow\">[" + created + "][" + service + "]<br>" + log + "</p>\n";
            } else if (status.equals("danger")) {
                tmp += "<p style=\"color:red\">[" + created + "][" + service + "]<br>" + log + "</p>\n";
            } else if (status.equals("success")) {
                tmp += "<p style=\"color:green\">[" + created + "][" + service + "]<br>" + log + "</p>\n";
            } else if (status.equals("warning")) {
                tmp += "<p style=\"color:red\">[" + created + "][" + service + "]<br>" + log + "</p>\n";
            }
        }
        pDialog.dismiss();
        setTextViewHTML(labelLog, tmp);
    }

    protected void setTextViewHTML(TextView text, String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
//        for(URLSpan span : urls) {
//            makeLinkClickable(strBuilder, span);
//        }
        text.setText(strBuilder);
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
