package com.vnoders.spotify_el8alaba.models.Search;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.vnoders.spotify_el8alaba.models.Image;
import com.vnoders.spotify_el8alaba.models.library.ArtistUserInfo;
import java.util.List;
import java.util.Objects;

public class SearchArtist {

    @SerializedName(value = "image", alternate = "images")
    @Expose
    private List<Image> images = null;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("userInfo")
    @Expose
    private ArtistUserInfo userInfo;

    @SerializedName("id")
    @Expose
    private String id;

    private boolean isSelected = false;

    public List<Image> getImages() {
        return (userInfo != null && userInfo.getImages() != null) ? userInfo.getImages() : images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return userInfo != null ? userInfo.getId() : id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void toggleSelection(){
        isSelected = !isSelected;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SearchArtist that = (SearchArtist) obj;
        return Objects.equals(name, that.name) &&
                Objects.equals(getId(), that.getId());
    }

}