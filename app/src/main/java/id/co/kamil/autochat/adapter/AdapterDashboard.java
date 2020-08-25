package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import id.co.kamil.autochat.R;

public class AdapterDashboard extends ArrayAdapter<ItemDashboard> {
    private int _resource;
    public AdapterDashboard(@NonNull Context context, int resource, @NonNull List<ItemDashboard> objects) {
        super(context, resource, objects);
        this._resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(this._resource,parent,false);
        TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
        TextView txtValue = (TextView) view.findViewById(R.id.txtValue);
        LinearLayout background = (LinearLayout) view.findViewById(R.id.background);

        final ItemDashboard itemDashboard = getItem(position);
        txtTitle.setText(itemDashboard.getTitle());
        txtValue.setText(itemDashboard.getVal());
        if (itemDashboard.getColor().equals("blue")){
            background.setBackground(getContext().getDrawable(R.drawable.rectangle_blue));
        }else if (itemDashboard.getColor().equals("orange")) {
            background.setBackground(getContext().getDrawable(R.drawable.rectangle_orange));
        }else if (itemDashboard.getColor().equals("green")) {
            background.setBackground(getContext().getDrawable(R.drawable.rectangle_green));
        }else if (itemDashboard.getColor().equals("tosca")) {
            background.setBackground(getContext().getDrawable(R.drawable.rectangle_tosca));
        }
        return view;
    }
}
