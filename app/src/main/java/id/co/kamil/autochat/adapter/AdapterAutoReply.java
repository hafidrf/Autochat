package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.co.kamil.autochat.R;

import static id.co.kamil.autochat.utils.Utils.formatIdDateFromString;

public class AdapterAutoReply extends BaseAdapter {
    private final String TAG = "AdapterPesan";
    public List<ItemAutoReply> listData;

    public Context context;
    ArrayList<ItemAutoReply> arraylist;

    private static final int resource = R.layout.item_list_auto_reply;

    public class ViewHolder {
        TextView txtReply, txtCreated, txtKeyword, txtStatus;
        CheckBox chk1;

    }

    public AdapterAutoReply(List<ItemAutoReply> apps, Context context) {
        this.listData = apps;
        this.context = context;
        arraylist = new ArrayList<ItemAutoReply>();
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
            viewHolder.txtReply = (TextView) rowView.findViewById(R.id.reply);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            viewHolder.txtCreated = (TextView) rowView.findViewById(R.id.created_at);
            viewHolder.txtStatus = (TextView) rowView.findViewById(R.id.status);
            viewHolder.txtKeyword = (TextView) rowView.findViewById(R.id.keyword);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemAutoReply item = listData.get(position);
        String pesan = item.getReply();
        if (pesan.length() > 100) {
            pesan = pesan.substring(0, 100) + "...";
        }
        String keyword = "";
        try {
            JSONArray jsonArray = new JSONArray(item.getKeyword());
            for (int i = 0; i < jsonArray.length(); i++) {
                keyword += jsonArray.getString(i);
                if (i < jsonArray.length() - 1) {
                    keyword += ", ";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        viewHolder.txtReply.setText(pesan);
        viewHolder.txtCreated.setText(formatIdDateFromString(item.getCreated_at()));
        viewHolder.txtKeyword.setText(keyword);
        viewHolder.txtStatus.setText(item.getStatus());
        viewHolder.chk1.setChecked(item.isCheckbox());

        if (item.getStatus().equals("aktif")) {
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.md_green_600));
        } else {
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.md_red_600));
        }
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
            for (ItemAutoReply postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getKeyword().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                } else if (charText.length() != 0 && postDetail.getReply().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
