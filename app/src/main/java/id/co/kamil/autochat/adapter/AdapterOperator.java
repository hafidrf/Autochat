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

public class AdapterOperator extends BaseAdapter {
    private final String TAG = "AdapterGrup";
    public List<ItemOperator> dataOperator;

    public Context context;
    ArrayList<ItemOperator> arraylist;

    private static final int resource = R.layout.item_list_operator;
    public class ViewHolder {
        TextView txtNama,txtEmail;
        CheckBox chk1;

    }
    public void reloadArrayList(){
        arraylist = new ArrayList<ItemOperator>();
        arraylist.addAll(dataOperator);
    }
    public AdapterOperator(List<ItemOperator> apps, Context context) {
        this.dataOperator = apps;
        this.context = context;
        arraylist = new ArrayList<ItemOperator>();
        arraylist.addAll(dataOperator);

    }

    @Override
    public int getCount() {
        return dataOperator.size();
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
            viewHolder.txtNama = (TextView) rowView.findViewById(R.id.itemNama);
            viewHolder.txtEmail = (TextView) rowView.findViewById(R.id.itemEmail);
            viewHolder.chk1 = (CheckBox) rowView.findViewById(R.id.checkbox);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ItemOperator item = dataOperator.get(position);
        viewHolder.txtNama.setText(item.getNama());
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

        dataOperator.clear();
        if (charText.length() == 0) {
            dataOperator.addAll(arraylist);

        } else {
            for (ItemOperator postDetail : arraylist) {
                if (charText.length() != 0 && postDetail.getNama().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataOperator.add(postDetail);
                }
                else if (charText.length() != 0 && postDetail.getEmail().toLowerCase(Locale.getDefault()).contains(charText)) {
                    dataOperator.add(postDetail);
                }
            }
        }
        notifyDataSetChanged();
    }
}
