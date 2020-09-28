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

public class AdapterTemplateShare extends BaseAdapter {
    private final String TAG = "AdapterTemplateShare";
    public List<ItemTemplateShare> listData;

    public Context context;
    ArrayList<ItemTemplateShare> arraylist;

    private static final int resource = R.layout.item_list_template_share;

    public class ViewHolder {
        TextView txtTemplate, txtTotalShare;
        CheckBox chk1;

    }

    public AdapterTemplateShare(List<ItemTemplateShare> apps, Context context) {
        this.listData = apps;
        this.context = context;
        arraylist = new ArrayList<ItemTemplateShare>();
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
            viewHolder.txtTemplate = (TextView) rowView.findViewById(R.id.labelTemplate);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            viewHolder.txtTotalShare = (TextView) rowView.findViewById(R.id.labelTotalShare);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemTemplateShare item = listData.get(position);
        String content = item.getTemplate();

        viewHolder.txtTotalShare.setText(item.getTotalshare());
        viewHolder.txtTemplate.setText(item.getTemplate());
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

        listData.clear();
        if (charText.length() == 0) {
            listData.addAll(arraylist);

        } else {
            for (ItemTemplateShare postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getTemplate().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
