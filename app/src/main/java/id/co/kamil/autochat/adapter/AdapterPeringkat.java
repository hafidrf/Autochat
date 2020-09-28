package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import id.co.kamil.autochat.R;

public class AdapterPeringkat extends ArrayAdapter<ItemPeringkat> {

    private final int _resource;

    public AdapterPeringkat(@NonNull Context context, int resource, @NonNull List<ItemPeringkat> objects) {
        super(context, resource, objects);
        this._resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = layoutInflater.inflate(R.layout.item_list_peringkat, parent, false);

        final TextView txtPeringkat = (TextView) view.findViewById(R.id.itemNo);
        final TextView txtNama = (TextView) view.findViewById(R.id.itemNama);
        final TextView txtEmail = (TextView) view.findViewById(R.id.itemEmail);
        final TextView txtDownline = (TextView) view.findViewById(R.id.itemDownline);

        final ItemPeringkat itemPeringkat = getItem(position);
        txtPeringkat.setText(itemPeringkat.getPeringkat());
        txtNama.setText(itemPeringkat.getNama());
        txtEmail.setText(itemPeringkat.getEmail());
        txtDownline.setText(itemPeringkat.getDownline());

        return view;
    }
}
