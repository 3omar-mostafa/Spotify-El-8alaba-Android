package com.vnoders.spotify_el8alaba.Artist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.vnoders.spotify_el8alaba.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistProfileFragment extends Fragment {

    public ArtistProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist_profile, container, false);
    }
}
