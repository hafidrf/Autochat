package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.co.kamil.autochat.R;

public class AdapterShorten extends BaseAdapter {
    private final String TAG = "AdapterShorten";
    public List<ItemShorten> dataShorten;

    public Context context;
    ArrayList<ItemShorten> arraylist;

    private static final int resource = R.layout.item_list_shorten;

    public class ViewHolder {
        TextView txtDomain, txtKlik;
        CheckBox chk1;

    }

    public AdapterShorten(List<ItemShorten> apps, Context context) {
        this.dataShorten = apps;
        this.context = context;
        arraylist = new ArrayList<ItemShorten>();
        arraylist.addAll(dataShorten);

    }

    @Override
    public int getCount() {
        return dataShorten.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        View rowView = convertView;
        final ViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater)
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(resource, null);
            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.txtDomain = (TextView) rowView.findViewById(R.id.domain);
            viewHolder.txtKlik = (TextView) rowView.findViewById(R.id.klik);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemShorten item = dataShorten.get(position);
        viewHolder.txtDomain.setText(item.getDomain());
        viewHolder.txtKlik.setText(item.getTotalklik() + " klik");
        viewHolder.chk1.setChecked(item.isCheckbox());


        if (item.isChkvisible()) {
            viewHolder.chk1.setVisibility(View.VISIBLE);
        } else {
            viewHolder.chk1.setVisibility(View.GONE);
        }
        viewHolder.chk1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arraylist.get(position).setCheckbox(viewHolder.chk1.isChecked());
            }
        });
        return rowView;
    }

    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        dataShorten.clear();
        if (charText.length() == 0) {
            dataShorten.addAll(arraylist);

        } else {
            for (ItemShorten postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getDomain().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataShorten.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
