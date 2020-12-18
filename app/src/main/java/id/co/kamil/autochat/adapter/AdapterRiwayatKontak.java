package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import id.co.kamil.autochat.R;

public class AdapterRiwayatKontak extends ArrayAdapter<ItemRiwayatKontak> {

    private int _resource = R.layout.item_riwayat_kontak;

    public AdapterRiwayatKontak(@NonNull Context context, int resource, @NonNull List<ItemRiwayatKontak> objects) {
        super(context, resource, objects);
        this._resource = resource;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(this._resource, parent, false);
        TextView itemKomentar = (TextView) view.findViewById(R.id.itemKomentar);
        ImageButton buttonHapus = (ImageButton) view.findViewById(R.id.btnHapus);
        final ItemRiwayatKontak item = getItem(position);
        itemKomentar.setText(item.getKomentar());
        buttonHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView) parent).performItemClick(view, position, position);
            }
        });
        return view;
    }
}
