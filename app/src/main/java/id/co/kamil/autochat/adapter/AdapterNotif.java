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

public class AdapterNotif extends BaseAdapter {
    private final String TAG = "ItemNotif";
    public List<ItemNotif> dataNotif;

    public Context context;
    ArrayList<ItemNotif> arraylist;

    private static final int resource = R.layout.item_list_notif;
    public class ViewHolder {
        TextView txtJudul,txtBody;
        CheckBox chk1;

    }
    public void reloadArrayList(){
        arraylist = new ArrayList<ItemNotif>();
        arraylist.addAll(dataNotif);
    }
    public AdapterNotif(List<ItemNotif> apps, Context context) {
        this.dataNotif = apps;
        this.context = context;
        arraylist = new ArrayList<ItemNotif>();
        arraylist.addAll(dataNotif);

    }

    @Override
    public int getCount() {
        return dataNotif.size();
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
            viewHolder.txtBody = (TextView) rowView.findViewById(R.id.body);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemNotif item = dataNotif.get(position);
        String body = item.getBody();
        if (body.length()>100){
            body = body.substring(0,100) + "...";
        }
        viewHolder.txtJudul.setText(item.getTitle());
        viewHolder.txtBody.setText(body);
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

        dataNotif.clear();
        if (charText.length() == 0) {
            dataNotif.addAll(arraylist);

        } else {
            for (ItemNotif postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getTitle().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataNotif.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getBody().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataNotif.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
