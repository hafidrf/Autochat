package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import id.co.kamil.autochat.R;

import static id.co.kamil.autochat.utils.Utils.formatIdDateFromString;

public class AdapterTemplatePromosi extends BaseAdapter {
    private final String TAG = "AdapterTemplatePromosi";
    public List<ItemTemplatePromosi> listData;

    public Context context;
    ArrayList<ItemTemplatePromosi> arraylist;

    private static final int resource = R.layout.item_list_template_promosi;

    public class ViewHolder {
        TextView txtName, txtContent, txtCreated, txtTags, txtStatusImage;
        ImageView imgShare;
        CheckBox chk1;

    }

    public AdapterTemplatePromosi(List<ItemTemplatePromosi> apps, Context context) {
        this.listData = apps;
        this.context = context;
        arraylist = new ArrayList<ItemTemplatePromosi>();
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
            viewHolder.imgShare = (ImageView) rowView.findViewById(R.id.imgShare);
            viewHolder.txtName = (TextView) rowView.findViewById(R.id.labelName);
            viewHolder.txtContent = (TextView) rowView.findViewById(R.id.content);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            viewHolder.txtCreated = (TextView) rowView.findViewById(R.id.created_at);
            viewHolder.txtTags = (TextView) rowView.findViewById(R.id.tags);
            viewHolder.txtStatusImage = (TextView) rowView.findViewById(R.id.status_image);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemTemplatePromosi item = listData.get(position);
        String content = item.getContent();
        if (content.length() > 100) {
            content = content.substring(0, 100) + "...";
        }
        String tags = "";
        try {
            JSONArray jsonArray = new JSONArray(item.getTags());
            for (int i = 0; i < jsonArray.length(); i++) {
                tags += jsonArray.getString(i);
                if (i < jsonArray.length() - 1) {
                    tags += ", ";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (item.isOwner()) {
            viewHolder.imgShare.setVisibility(View.GONE);
        } else {
            viewHolder.imgShare.setVisibility(View.VISIBLE);
        }
        if (item.getStatus_image().equals("tersedia")) {
            viewHolder.txtStatusImage.setTextColor(Color.GREEN);
        } else if (item.getStatus_image().equals("sedang didownload")) {
            viewHolder.txtStatusImage.setTextColor(Color.BLUE);
        } else {
            viewHolder.txtStatusImage.setTextColor(Color.RED);
        }
        viewHolder.txtStatusImage.setText(item.getStatus_image());
        viewHolder.txtName.setText(item.getName());
        viewHolder.txtContent.setText(content);
        viewHolder.txtCreated.setText(formatIdDateFromString(item.getCreated_at()));
        viewHolder.txtTags.setText(tags);
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
            for (ItemTemplatePromosi postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getTags().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                } else if (charText.length() != 0 && postDetail.getContent().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                } else if (charText.length() != 0 && postDetail.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
