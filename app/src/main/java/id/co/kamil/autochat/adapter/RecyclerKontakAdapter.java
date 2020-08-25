package id.co.kamil.autochat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.co.kamil.autochat.R;

public class RecyclerKontakAdapter extends RecyclerView.Adapter<RecyclerKontakAdapter.ViewHolder> {
    private List<ItemRecyclerKontak> list;
    private Context context;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemRecyclerKontak item);
    }
    public RecyclerKontakAdapter(List<ItemRecyclerKontak> list, Context context, OnItemClickListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerKontakAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recycler_kontak, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerKontakAdapter.ViewHolder viewHolder, int i) {
        viewHolder.imageView.setImageResource(list.get(i).getIcon());
        viewHolder.bind(list.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.itemIcon);
        }
        public void bind(final ItemRecyclerKontak item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
