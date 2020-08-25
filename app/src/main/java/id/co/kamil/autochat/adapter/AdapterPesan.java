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

import static id.co.kamil.autochat.utils.Utils.formatIdDateFromString;

public class AdapterPesan extends BaseAdapter {
    private final String TAG = "AdapterPesan";
    public List<ItemPesan> listData;

    public Context context;
    ArrayList<ItemPesan> arraylist;

    private static final int resource = R.layout.item_list_pesan;
    public class ViewHolder {
        TextView txtJudul,txtTanggal,txtPesan,txtStatus,txtNomor,txtError;
        CheckBox chk1;

    }

    public AdapterPesan(List<ItemPesan> apps, Context context) {
        this.listData = apps;
        this.context = context;
        arraylist = new ArrayList<ItemPesan>();
        arraylist.addAll(listData);

    }
    public void addListItemToAdapter(List<ItemPesan> list){
        this.listData.addAll(list);
        this.arraylist.addAll(list);
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
            viewHolder.txtJudul = (TextView) rowView.findViewById(R.id.judul);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            viewHolder.txtPesan = (TextView) rowView.findViewById(R.id.pesan);
            viewHolder.txtTanggal = (TextView) rowView.findViewById(R.id.tanggal);
            viewHolder.txtNomor = (TextView) rowView.findViewById(R.id.nomor);
            viewHolder.txtStatus = (TextView) rowView.findViewById(R.id.status);
            viewHolder.txtError = (TextView) rowView.findViewById(R.id.error);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemPesan item = listData.get(position);
        String pesan = item.getPesan();
        if (pesan.length()>100){
            pesan = pesan.substring(0,100) + "...";
        }
        viewHolder.txtJudul.setText(item.getNama());
        viewHolder.txtNomor.setText(item.getNomor());
        viewHolder.txtTanggal.setText(formatIdDateFromString(item.getTglpesan()));
        viewHolder.txtPesan.setText(pesan);
        viewHolder.txtStatus.setText(item.getStatus());
        viewHolder.chk1.setChecked(item.isCheckbox());
        if (item.getError_again() == null){
            viewHolder.txtError.setVisibility(View.GONE);
        }else{
            viewHolder.txtError.setText(item.getError_again() + "x percobaan");
            viewHolder.txtError.setVisibility(View.VISIBLE);
        }

        if (item.getStatus().equals("terkirim") || item.getStatus().equals("active") || item.getStatus().equals("success")){
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.md_green_600));
        }else if (item.getStatus().equals("pending")) {
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.md_orange_600));
        }else{
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.md_red_600));
        }
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
            for (ItemPesan postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getNama().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getNomor().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getPesan().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getTglpesan().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getJadwalkirim().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
