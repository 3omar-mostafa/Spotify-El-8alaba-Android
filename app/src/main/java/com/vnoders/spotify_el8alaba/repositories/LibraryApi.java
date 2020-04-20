package com.vnoders.spotify_el8alaba.repositories;

import com.vnoders.spotify_el8alaba.models.TrackImage;
import com.vnoders.spotify_el8alaba.models.library.Playlist;
import com.vnoders.spotify_el8alaba.models.library.TracksPagingWrapper;
import com.vnoders.spotify_el8alaba.models.library.UserLibraryPlaylist;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * This interface contains all backend's API endpoints related to the user's library. This interface
 * is implemented by {@link RetrofitClient#getAPI}
 */
public interface LibraryApi {

    /**
     * @return List of playlists (wrapped in {@link UserLibraryPlaylist}) of the current user
     * wrapped in a {@link Call} object
     */
    @GET("me/playlists")
    Call<UserLibraryPlaylist> getUserPlaylists();


    /**
     * @param playlistId The id of the required playlist
     *
     * @return The playlist requested wrapped in a {@link Call} object
     */
    @GET("playlists/{playlist_id}")
    Call<Playlist> getPlaylist(@Path("playlist_id") String playlistId);


    /**
     * @param playlistId The id of the playlist that contains required tracks
     *
     * @return List of tracks (wrapped in {@link TracksPagingWrapper}) in the requested playlist
     * wrapped in a {@link Call} object
     */
    @GET("playlists/{playlist_id}/tracks")
    Call<TracksPagingWrapper> getPlaylistTracks(@Path("playlist_id") String playlistId);


    /**
     * @param playlistId The id of the playlist to get its images
     *
     * @return List of cover images (different sizes) of the requested playlist
     */
    @GET("playlists/{playlist_id}/images")
    Call<List<TrackImage>> getPlaylistCoverImages(@Path("playlist_id") String playlistId);


    @GET("playlists/{playlist_id}/followers/contains")
    Call<List<Boolean>> doesUsersFollowPlaylist(@Path("playlist_id") String playlistId,
            @Query("ids") List<String> userIds);


    @GET("playlists/{playlist_id}/followers/contains")
    default Call<List<Boolean>> doesCurrentUserFollowPlaylist(
            @Path("playlist_id") String playlistId) {

        //TODO: update the value to get the user id from shared preferences
        List<String> currentUserId = new ArrayList<>(
                Collections.singletonList("5e8f3a6bac9eb42ab5ae0514"));
        return doesUsersFollowPlaylist(playlistId, currentUserId);
    }


    @PUT("playlists/{playlist_id}/followers")
    Call<Void> followPlaylist(@Path("playlist_id") String playlistId);


    @DELETE("playlists/{playlist_id}/followers")
    Call<Void> unfollowPlaylist(@Path("playlist_id") String playlistId);


}
