package com.vnoders.spotify_el8alaba.models.library;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.vnoders.spotify_el8alaba.models.TrackImage;
import java.util.List;
import java.util.Objects;

/**
 * This class is used to model data parsed from json network response using {@link Gson} library
 */
public class Playlist {

    @SerializedName("tracks")
    private TracksPagingWrapper tracks;

    @SerializedName("collaborative")
    private boolean collaborative;

    @SerializedName("public")
    private boolean isPublic;

    @SerializedName("followers")
    private int followers;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("owner")
    private Owner owner;

    @SerializedName("images")
    private List<TrackImage> images = null;

    @SerializedName("type")
    private String type;

    @SerializedName("uri")
    private String uri;

    @SerializedName("href")
    private String href;

    @SerializedName("external_urls")
    private ExternalUrls externalUrls;

    @SerializedName("id")
    private String id;

    public TracksPagingWrapper getTracks() {
        return tracks;
    }

    public void setTracks(TracksPagingWrapper tracks) {
        this.tracks = tracks;
    }

    public boolean isCollaborative() {
        return collaborative;
    }

    public void setCollaborative(boolean collaborative) {
        this.collaborative = collaborative;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean _public) {
        this.isPublic = _public;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public List<TrackImage> getImages() {
        return images;
    }

    public void setImages(List<TrackImage> images) {
        this.images = images;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public ExternalUrls getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(ExternalUrls externalUrls) {
        this.externalUrls = externalUrls;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Playlist playlist = (Playlist) obj;
        return collaborative == playlist.collaborative &&
                isPublic == playlist.isPublic &&
                Objects.equals(tracks, playlist.tracks) &&
                Objects.equals(name, playlist.name) &&
                Objects.equals(description, playlist.description) &&
                Objects.equals(owner, playlist.owner) &&
                Objects.equals(images, playlist.images) &&
                Objects.equals(type, playlist.type) &&
                Objects.equals(uri, playlist.uri) &&
                Objects.equals(href, playlist.href) &&
                Objects.equals(externalUrls, playlist.externalUrls) &&
                Objects.equals(id, playlist.id);
    }

}
