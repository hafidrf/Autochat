package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.co.kamil.autochat.R;

public class AdapterTemplateShareDetail extends BaseAdapter {
    private final String TAG = "AdapterTemplateShare";
    public List<ItemTemplateShareDetail> listData;

    public Context context;
    ArrayList<ItemTemplateShareDetail> arraylist;

    private static final int resource = R.layout.item_list_template_share_detail;
    public class ViewHolder {
        TextView txtEmail;
        CheckBox chk1;

    }

    public AdapterTemplateShareDetail(List<ItemTemplateShareDetail> apps, Context context) {
        this.listData = apps;
        this.context = context;
        arraylist = new ArrayList<ItemTemplateShareDetail>();
        arraylist.addAll(listData);

    }

    @Override
    public int getCount() {
        return listData.size();
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
            viewHolder.txtEmail = (TextView) rowView.findViewById(R.id.labelEmail);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemTemplateShareDetail item = listData.get(position);
        viewHolder.txtEmail.setText(item.getEmail());
        viewHolder.chk1.setChecked(item.isCheckbox());

        if (item.isChkvisible()){
            viewHolder.chk1.setVisibility(View.VISIBLE);
        }else{
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

        listData.clear();
        if (charText.length() == 0) {
            listData.addAll(arraylist);

        } else {
            for (ItemTemplateShareDetail postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getEmail().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
