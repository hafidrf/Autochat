package id.co.kamil.autochat.ui.pengaturan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import id.co.kamil.autochat.R;

import static id.co.kamil.autochat.utils.API.TEMPLATE_SHARE;
import static id.co.kamil.autochat.utils.API.URL_DOWNLINE;
import static id.co.kamil.autochat.utils.API.URL_LANDING_PAGE;
import static id.co.kamil.autochat.utils.Utils.setClipboard;

public class AffiliasiFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText edtKodeReferal;
    private ImageButton btnBagikanPlaystore;
    private Button btnDownline;
    private EditText edtLinkPlaystore;
    private EditText edtLinkWeb;
    private ImageButton btnBagikanWeb;
    private ImageButton btnCopyPlaystore,btnCopyWeb;
    private TextView txtInfo;

    public AffiliasiFragment() {
        // Required empty public constructor
    }

    public static AffiliasiFragment newInstance(String param1, String param2) {
        AffiliasiFragment fragment = new AffiliasiFragment();
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
        View view = inflater.inflate(R.layout.fragment_affiliasi, container, false);
        txtInfo = (TextView) view.findViewById(R.id.txtInfo);
        edtKodeReferal = (EditText) view.findViewById(R.id.edtKodeMarketing);
        edtLinkPlaystore = (EditText) view.findViewById(R.id.edtPlaystore);
        edtLinkWeb = (EditText) view.findViewById(R.id.edtLinkWeb);

        btnCopyPlaystore = (ImageButton) view.findViewById(R.id.btnCopyPlaystore);
        btnCopyWeb = (ImageButton) view.findViewById(R.id.btnCopyWeb);
        btnBagikanPlaystore = (ImageButton) view.findViewById(R.id.btnSharePlaystore);
        btnBagikanWeb = (ImageButton) view.findViewById(R.id.btnShareWeb);
        btnDownline = (Button) view.findViewById(R.id.btnDownline);
        btnCopyPlaystore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setClipboard(getContext(),edtLinkPlaystore.getText().toString());
                Toast.makeText(getContext(), "link berhasil disalin", Toast.LENGTH_SHORT).show();
            }
        });
        btnCopyWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setClipboard(getContext(),edtLinkWeb.getText().toString());
                Toast.makeText(getContext(), "link berhasil disalin", Toast.LENGTH_SHORT).show();
            }
        });
        btnBagikanPlaystore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String konten = TEMPLATE_SHARE;
                    konten = konten.replace("[linklanding]",URL_LANDING_PAGE);
                    konten = konten.replace("[linkweb]",edtLinkPlaystore.getText().toString());

                    String appId = getActivity().getPackageName();
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
                    String konten = TEMPLATE_SHARE;
                    konten = konten.replace("[linklanding]",URL_LANDING_PAGE);
                    konten = konten.replace("[linkweb]",edtLinkWeb.getText().toString());

                    String appId = getActivity().getPackageName();
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
        txtInfo.setText(Html.fromHtml("Dapatkan Penghasilan JUTAAN RUPIAH tiap Bulannya tanpa Batas dengan Menjadi Mitra Affiliate OLSHOP.ID dan WABOT. Info lebih lengkap klik <a href=\"https://olshop.id/affiliate\">DISINI</a>.\n"));
        loadDataReferal();
        return view;
    }

    private void loadDataReferal() {
        edtKodeReferal.setText("7cqBvzjj");
        String url_track_web = Uri.parse(URL_LANDING_PAGE)
                .buildUpon()
                .appendQueryParameter("tracking",edtKodeReferal.getText().toString())
                .toString();
        edtLinkWeb.setText(url_track_web);
        String url_track_playstore = Uri.parse("https://")
                .buildUpon()
                .appendQueryParameter("referrer",edtKodeReferal.getText().toString())
                .toString();
        edtLinkPlaystore.setText(url_track_playstore);
    }

}
