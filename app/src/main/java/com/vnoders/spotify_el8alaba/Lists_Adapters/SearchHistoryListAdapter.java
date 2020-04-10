package com.vnoders.spotify_el8alaba.Lists_Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.vnoders.spotify_el8alaba.Lists_Items.SearchListItem;
import com.vnoders.spotify_el8alaba.R;
import com.vnoders.spotify_el8alaba.ui.search.SearchFragment;
import java.util.ArrayList;

public class SearchHistoryListAdapter extends
        RecyclerView.Adapter<SearchHistoryListAdapter.MyViewHolder> {

    private ArrayList<SearchListItem> mDataset;

    // Provide a suitable constructor (depends on the kind of dataset)
    public SearchHistoryListAdapter(ArrayList<SearchListItem> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public SearchHistoryListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_history_list_item, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.name.setText(mDataset.get(position).getName());
        holder.info.setText(mDataset.get(position).getInfo());
        if (!mDataset.get(position).getImageURL().equals("")) {
            Picasso.get().load(mDataset.get(position).getImageURL()).into(holder.image);
        }

        holder.removeIcon.setOnClickListener(v -> {
            mDataset.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDataset.size());
            if (mDataset.isEmpty()) {
                SearchFragment.removeSearchHistoryList();
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public View v;
        public TextView name;
        TextView info;
        public ImageView image;
        ImageView removeIcon;

        MyViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.search_history_item_name_text_view);
            info = v.findViewById(R.id.search_history_item_info_text_view);
            image = v.findViewById(R.id.search_history_item_image_view);
            removeIcon = v.findViewById(R.id.search_history_item_remove);
        }
    }
}

