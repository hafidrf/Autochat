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

public class AdapterSchedule extends BaseAdapter {
    private final String TAG = "AdapterPesan";
    public List<ItemSchedule> listData;

    public Context context;
    ArrayList<ItemSchedule> arraylist;

    private static final int resource = R.layout.item_list_schedule;
    public class ViewHolder {
        TextView txtJudul,txtTanggal,txtPesan,txtStatus,txtTipe,txtBy;
        CheckBox chk1;

    }

    public AdapterSchedule(List<ItemSchedule> apps, Context context) {
        this.listData = apps;
        this.context = context;
        arraylist = new ArrayList<ItemSchedule>();
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
            viewHolder.txtJudul = (TextView) rowView.findViewById(R.id.edtNama);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            viewHolder.txtPesan = (TextView) rowView.findViewById(R.id.edtPesan);
            viewHolder.txtTanggal = (TextView) rowView.findViewById(R.id.edtJadwalKirim);
            viewHolder.txtTipe = (TextView) rowView.findViewById(R.id.edtTipe);
            viewHolder.txtStatus = (TextView) rowView.findViewById(R.id.edtStatus);
            viewHolder.txtBy = (TextView) rowView.findViewById(R.id.edtBy);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemSchedule item = listData.get(position);
        String pesan = item.getPesan();
        if (pesan.length()>100){
            pesan = pesan.substring(0,100) + "...";
        }
        String by = "(Kontak)";
        if (item.isGroup()){
            by = "(Grup)";
        }
        viewHolder.txtJudul.setText(item.getNama());
        viewHolder.txtBy.setText(by);
        viewHolder.txtTipe.setText("("+item.getTipe()+")");
        viewHolder.txtTanggal.setText(formatIdDateFromString(item.getJadwalkirim()));
        viewHolder.txtPesan.setText(pesan);
        viewHolder.txtStatus.setText(item.getStatus());
        viewHolder.chk1.setChecked(item.isCheckbox());

        if (item.getStatus().equals("aktif") ){
            viewHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.md_green_600));
        }else {
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
            for (ItemSchedule postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getNama().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getTipe().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getPesan().toLowerCase(Locale.getDefault()).contains(charText)) {
                    listData.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getStatus().toLowerCase(Locale.getDefault()).contains(charText)) {
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
