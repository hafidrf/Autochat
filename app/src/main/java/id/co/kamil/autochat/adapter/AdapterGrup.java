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

public class AdapterGrup extends BaseAdapter {
    private final String TAG = "AdapterGrup";
    public List<ItemGrup> dataGrup;

    public Context context;
    ArrayList<ItemGrup> arraylist;

    private static final int resource = R.layout.item_list_grup;

    public class ViewHolder {
        TextView txtJudul;
        TextView txtid;
        TextView txtmember;
        CheckBox chk1;

    }

    public void reloadArrayList() {
        arraylist = new ArrayList<ItemGrup>();
        arraylist.addAll(dataGrup);
    }

    public AdapterGrup(List<ItemGrup> apps, Context context) {
        this.dataGrup = apps;
        this.context = context;
        arraylist = new ArrayList<ItemGrup>();
        arraylist.addAll(dataGrup);

    }

    @Override
    public int getCount() {
        return dataGrup.size();
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
            viewHolder.txtJudul = (TextView) rowView.findViewById(R.id.judul);
            viewHolder.txtmember = (TextView) rowView.findViewById(R.id.member);
            viewHolder.txtid = (TextView) rowView.findViewById(R.id.id);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemGrup item = dataGrup.get(position);
        viewHolder.txtJudul.setText(item.getJudul());
        viewHolder.txtmember.setText(item.getMember());
        viewHolder.txtid.setText(item.getId());
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

        dataGrup.clear();
        if (charText.length() == 0) {
            dataGrup.addAll(arraylist);

        } else {
            for (ItemGrup postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getJudul().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataGrup.add(postDetail);
                    if (charText.length() != 0 && postDetail.getId().toLowerCase(Locale.getDefault()).contains(charText)) {
                        dataGrup.add(postDetail);
                        if (charText.length() != 0 && postDetail.getMember().toLowerCase(Locale.getDefault()).contains(charText)) {
                            dataGrup.add(postDetail);
                        } else if (charText.length() != 0 && postDetail.getDeskripsi().toLowerCase(Locale.getDefault()).contains(charText)) {
                            dataGrup.add(postDetail);
                        }
                    }
                }
                notifyDataSetChanged();
            }
        }
    }
}
