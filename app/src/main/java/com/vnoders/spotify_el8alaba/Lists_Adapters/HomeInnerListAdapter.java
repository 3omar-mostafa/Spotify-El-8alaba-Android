package com.vnoders.spotify_el8alaba.Lists_Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.vnoders.spotify_el8alaba.Lists_Items.HomeInnerListItem;
import com.vnoders.spotify_el8alaba.R;
import com.vnoders.spotify_el8alaba.models.HomePlaylist;
import com.vnoders.spotify_el8alaba.models.TrackImage;
import com.vnoders.spotify_el8alaba.ui.library.PlaylistHomeFragment;
import java.util.ArrayList;
import java.util.List;

public class HomeInnerListAdapter extends RecyclerView.Adapter<HomeInnerListAdapter.MyViewHolder> {

    private static Fragment fragment;
    private static ArrayList<HomeInnerListItem> mockDataset;
    private static ArrayList<HomePlaylist> backDataset;

    //               The difference between these two constructors is that one uses mock data and the other
    //                  uses data retrieved from the server and the mock data one will be removed later on.

    /**
     * @param myDataset List Of Playlists in the home Category lists
     * @param fragment  The fragment where this list is in (Used to load another fragment)
     */
    // Provide a suitable constructor (depends on the kind of dataset)
    public HomeInnerListAdapter(ArrayList<HomeInnerListItem> myDataset, Fragment fragment) {
        backDataset = new ArrayList<>();
        mockDataset = myDataset;
        HomeInnerListAdapter.fragment = fragment;
    }

    HomeInnerListAdapter(Fragment fragment, ArrayList<HomePlaylist> backDataset) {
        mockDataset = new ArrayList<>();
        HomeInnerListAdapter.backDataset = backDataset;
        HomeInnerListAdapter.fragment = fragment;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HomeInnerListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
            int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_inner_list_item, parent, false);
        return new MyViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        //TODO: Replace the former with the latter code when backend is completed
        holder.title.setText(backDataset.get(position).getName());
        holder.subTitle.setText(backDataset.get(position).getDescription());

        String imageUrl;
        List<TrackImage> images = backDataset.get(position).getImages();
        if (!images.isEmpty()) {
            imageUrl = images.get(0).getUrl();
        } else {
            imageUrl = "https://getdrawings.com/free-icon-bw/black-music-icons-23.png";
        }
        Picasso.get().load(imageUrl).placeholder(R.drawable.spotify).into(holder.image);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return backDataset.size();
    }

    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // each data item is just a string in this case
        public View v;
        public TextView title;
        TextView subTitle;
        public ImageView image;

        MyViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.home_inner_list_item_title);
            subTitle = v.findViewById(R.id.home_inner_list_item_sub_title);
            image = v.findViewById(R.id.home_inner_list_item_image);

            v.setOnClickListener(v1 -> {

                Fragment targetFragment = PlaylistHomeFragment
                        .newInstance(backDataset.get(getAdapterPosition()).getId());
                fragment.getParentFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in,
                                R.anim.fade_out)
                        .replace(R.id.nav_host_fragment, targetFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

}
