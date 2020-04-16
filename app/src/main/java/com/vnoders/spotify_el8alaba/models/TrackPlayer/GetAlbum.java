package com.vnoders.spotify_el8alaba.models.TrackPlayer;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetAlbum {

    @SerializedName("name")
    private String mName;

    @SerializedName("tracks")
    private List<GetTrack> mTracks;

    @SerializedName("images")
    private List<AlbumImage> mImages;


    public String getName() {
        return this.mName;
    }

    public List<GetTrack> getTracks() {
        return this.mTracks;
    }

    public List<AlbumImage> getImages() {
        return this.mImages;
    }

}
