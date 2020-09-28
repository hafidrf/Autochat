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

import java.util.List;

import id.co.kamil.autochat.R;

import static id.co.kamil.autochat.utils.Utils.formatIdDateFromString;

public class AdapterFollowupData extends ArrayAdapter<ItemFollowupData> {
    private final String TAG = "AdapterFollowupData";


    private static final int resource = R.layout.item_list_followup_data;

    public AdapterFollowupData(@NonNull Context context, @NonNull List<ItemFollowupData> objects) {
        super(context, resource, objects);
    }

    public class ViewHolder {
        TextView txtJudul, txtSchedule;
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
            viewHolder.txtSchedule = (TextView) rowView.findViewById(R.id.schedule);
            viewHolder.btnHapus = (ImageButton) rowView.findViewById(R.id.btnHapus);
            viewHolder.btnEdit = (ImageButton) rowView.findViewById(R.id.btnEdit);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemFollowupData item = getItem(position);
        String pesan = item.getMessage();
        if (pesan.length() > 100) {
            pesan = pesan.substring(0, 100);
        }
        viewHolder.txtJudul.setText(pesan);
        if (Integer.parseInt(item.getInterval()) > 0 && position > 0) {
            viewHolder.txtSchedule.setText(item.getInterval() + " hari");
        } else {
            viewHolder.txtSchedule.setText(formatIdDateFromString(item.getSchedule()));
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
