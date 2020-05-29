package com.vnoders.spotify_el8alaba.ui.library;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.vnoders.spotify_el8alaba.R;
import com.vnoders.spotify_el8alaba.models.library.LibraryPlaylistItem;
import java.util.List;


/**
 * This is the fragment which appears inside the library (Playlist Tab) that displays the list of
 * the user's playlists
 */
public class LibraryPlaylistFragment extends Fragment {

    private ProgressBar progressBar;
    private NestedScrollView playlistsContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        LibraryPlaylistViewModel playlistViewModel = new ViewModelProvider(requireActivity())
                .get(LibraryPlaylistViewModel.class);

        View root = inflater.inflate(R.layout.fragment_library_playlist, container, false);

        progressBar = root.findViewById(R.id.progress_bar);
        progressBar.setBackgroundColor(Color.BLACK);

        playlistsContainer = root.findViewById(R.id.library_playlist_container);
        View createPlaylist = root.findViewById(R.id.create_playlist);

        createPlaylist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext() , CreatePlaylistActivity.class);
                startActivity(intent);
            }
        });


        LibraryPlaylistAdapter playlistAdapter = new LibraryPlaylistAdapter(
                requireActivity().getSupportFragmentManager());

        RecyclerView recyclerView = root.findViewById(R.id.library_playlist_recycler_view);

        recyclerView.setAdapter(playlistAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        playlistViewModel.requestUserPlaylists();

        playlistViewModel.getUserPlaylists().observe(getViewLifecycleOwner(),
                new Observer<List<LibraryPlaylistItem>>() {
                    @Override
                    public void onChanged(List<LibraryPlaylistItem> libraryPlaylistItems) {
                        updateViewsVisibility();
                        playlistAdapter.submitList(libraryPlaylistItems);
                    }
                });
        return root;
    }

    private void updateViewsVisibility() {
        progressBar.setVisibility(View.GONE);
        playlistsContainer.setVisibility(View.VISIBLE);
    }
}