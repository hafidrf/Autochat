package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import id.co.kamil.autochat.R;

public class AdapterLinkpageData extends ArrayAdapter<JSONObject> {
    private final String TAG = "AdapterWaformData";


    private static final int resource = R.layout.item_list_linkpage_data;

    public AdapterLinkpageData(@NonNull Context context, @NonNull List<JSONObject> objects) {
        super(context, resource, objects);
    }

    public class ViewHolder {
        TextView txtJudul, txtLink;
        ImageButton btnHapus, btnEdit;

    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View rowView = convertView;
        final ViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(resource, parent, false);
            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.txtJudul = (TextView) rowView.findViewById(R.id.judul);
            viewHolder.txtLink = (TextView) rowView.findViewById(R.id.link);
            viewHolder.btnHapus = (ImageButton) rowView.findViewById(R.id.btnHapus);
            viewHolder.btnEdit = (ImageButton) rowView.findViewById(R.id.btnEdit);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final JSONObject item = getItem(position);

        try {
            String link = item.getString("link");
            if (link.length() > 100) {
                link = link.substring(0, 100) + "...";
            }
            viewHolder.txtJudul.setText(item.getString("judul"));
            viewHolder.txtLink.setText(link);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        viewHolder.btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, 0);
            }
        });
        viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, 0);
            }
        });
        return rowView;
    }
}
