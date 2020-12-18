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

public class AdapterWaformData extends ArrayAdapter<ItemWaformData> {
    private final String TAG = "AdapterWaformData";


    private static final int resource = R.layout.item_list_waform_data;

    public AdapterWaformData(@NonNull Context context, @NonNull List<ItemWaformData> objects) {
        super(context, resource, objects);
    }

    public class ViewHolder {
        TextView txtJudul, txtType;
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
            viewHolder.txtType = (TextView) rowView.findViewById(R.id.type);
            viewHolder.btnHapus = (ImageButton) rowView.findViewById(R.id.btnHapus);
            viewHolder.btnEdit = (ImageButton) rowView.findViewById(R.id.btnEdit);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemWaformData item = getItem(position);

        viewHolder.txtJudul.setText(item.getLabel());
        viewHolder.txtType.setText(item.getType());

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
