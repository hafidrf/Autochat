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

public class AdapterKontak extends BaseAdapter {
    private final String TAG = "AdapterKontak";
    public List<ItemKontak> listKontak;

    public Context context;
    public ArrayList<ItemKontak> arraylist;

    private static final int resource = R.layout.item_list_kontak;
    public class ViewHolder {
        TextView txtJudul;
        CheckBox chk1;

    }

    public AdapterKontak(List<ItemKontak> apps, Context context) {
        this.listKontak = apps;
        this.context = context;
        arraylist = new ArrayList<ItemKontak>();
        arraylist.addAll(listKontak);

    }


    @Override
    public int getCount() {
        return listKontak.size();
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
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemKontak item = listKontak.get(position);
        viewHolder.txtJudul.setText(item.getJudul());
        if (item.getId().equals("grupku")){
            viewHolder.chk1.setChecked(false);
        }else{
            viewHolder.chk1.setChecked(item.isCheckbox());
        }


        if (item.isChkvisible() && !item.getId().equals("grupku")){
            viewHolder.chk1.setVisibility(View.VISIBLE);
        }else{
            viewHolder.chk1.setVisibility(View.GONE);
        }
        viewHolder.chk1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i=0;i<arraylist.size();i++){
                    if (arraylist.get(i).getId().equals(item.getId())){
                        arraylist.get(i).setCheckbox(viewHolder.chk1.isChecked());
                        //listKontak.get(position).setCheckbox(viewHolder.chk1.isChecked());
                        break;
                    }
                }
                for (int i=0;i<listKontak.size();i++){
                    if (listKontak.get(i).getId().equals(item.getId())){
                        listKontak.get(i).setCheckbox(viewHolder.chk1.isChecked());
                        break;
                    }
                }
            }
        });
        return rowView;
    }

    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        listKontak.clear();
        if (charText.length() == 0) {
            listKontak.addAll(arraylist);

        } else {
            for (ItemKontak postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getJudul().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listKontak.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getNomorhp().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listKontak.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
