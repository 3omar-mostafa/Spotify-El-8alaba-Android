package com.vnoders.spotify_el8alaba;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.vnoders.spotify_el8alaba.models.TrackPlayer.AlbumImage;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.CurrentlyPlayingTrackResponse;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetAlbum;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetArtist;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetPlaylist;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetPlaylistItem;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetPlaylistTrack;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetSeveralTracks;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.GetTrack;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.PostTrackId;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.CurrentlyPlayingTrack;
import com.vnoders.spotify_el8alaba.models.TrackPlayer.Track;
import com.vnoders.spotify_el8alaba.repositories.RetrofitClient;
import com.vnoders.spotify_el8alaba.repositories.TrackPlayerApi;
import com.vnoders.spotify_el8alaba.response.signup.CurrentlyPlaying;
import com.vnoders.spotify_el8alaba.ui.trackplayer.TrackViewModel;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author Ali Adel
 * Media playback service to play music from background even when activity is not visible
 */
public class MediaPlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {


    private static final String PLAYER_STREAMING_BASE_URL = RetrofitClient.BASE_URL + "streaming/";
    private static final String PLAYER_STREAMING_URL_MIDDLE = "?Authorization=Bearer ";

    //______________________________________________________________________________________________
    //--------------------------------------Variables-----------------------------------------------
    //______________________________________________________________________________________________

    // bind to give to activities to interact with this service
    private final IBinder mMediaPlaybackBinder = new MediaPlaybackBinder();
    // list of track ids received to play
    private List<Track> mTracksList;

    // index of current track active
    private int mCurrentTrackIndex;

    // instance of media player for audio playback
    private MediaPlayer mMediaPlayer;

    // know if playing right now
    private boolean mPlaying = false;

    // this is for event every .. ms
    private Handler mHandler = new Handler();
    // runnable for handler
    private Runnable mRunnable = new Runnable() {
        public void run() {
            //do something
            if (mMediaPlayer != null) {
                TrackViewModel.getInstance().updateTrackProgress(mMediaPlayer.getCurrentPosition());
            }

            mHandler.postDelayed(this, HANDLER_DELAY);
        }
    };

    // constant which dictates time of handler thread
    private static final int HANDLER_DELAY = 100;
    // know if this is the first init or not
    private boolean mFirstInit = false;
    private int mFirstProgress = 0;
    private boolean mRepeat = false;


    //______________________________________________________________________________________________
    //-------------------------------Service specific function--------------------------------------
    //______________________________________________________________________________________________

    /**
     * binder class that provides the interface to activities to act on
     */
    public class MediaPlaybackBinder extends Binder {
        public MediaPlaybackService getService() {
            return MediaPlaybackService.this;
        }
    }

    /**
     * called when startService is called in main activity
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // get the list of tracks to play
        getCurrentlyPlaying();

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called when an activity wants to connect with me
     *
     * @param intent send by activity (can carry info in bundle)
     * @return returns binder to provide interface with me
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMediaPlaybackBinder;
    }

    /**
     * use this call back to release media player
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyMediaPlayer();
        mHandler = null;
        mRunnable = null;
    }

    //______________________________________________________________________________________________
    //------------------------------Track Manipulating Functions------------------------------------
    //______________________________________________________________________________________________

    public void playTrack(String trackId, boolean repeat) {

        if (TextUtils.isEmpty(trackId)) {
            return;
        }

        mRepeat = repeat;

        getTrack(trackId);
    }

    public void playList(List<String> trackIds, boolean shuffle, boolean repeat) {
        if (trackIds == null || trackIds.isEmpty()) {
            return;
        }

        mRepeat = repeat;

        getSeveralTracks(trackIds, shuffle);
    }

    public void playAlbum(String albumId, boolean shuffle, boolean repeat) {
        if (TextUtils.isEmpty(albumId)) {
            return;
        }

        mRepeat = repeat;

        getAlbum(albumId, shuffle);
    }

    public void playPlaylist(String playlistId, boolean shuffle, boolean repeat) {
        if (TextUtils.isEmpty(playlistId)) {
            return;
        }

        mRepeat = repeat;

        getPlaylist(playlistId, shuffle);
    }

    public void shuffle() {

        if (mTracksList == null || mTracksList.size() < 1){
            return;
        }

        Track currentTrack = mTracksList.get(mCurrentTrackIndex);
        mTracksList.remove(mCurrentTrackIndex);

        Collections.shuffle(mTracksList);

        mTracksList.add(mCurrentTrackIndex, currentTrack);
    }

    public void repeatAllToggle() {

        if (mTracksList == null || mTracksList.size() < 1) {
            return;
        }

        if (mRepeat) {

            mRepeat = false;

            if (mCurrentTrackIndex < 1)
                mTracksList.get(mCurrentTrackIndex).setHasPrev(false);
            else
                mTracksList.get(mCurrentTrackIndex).setHasPrev(true);

            if (mCurrentTrackIndex >= mTracksList.size() - 1)
                mTracksList.get(mCurrentTrackIndex).setHasNext(false);
            else
                mTracksList.get(mCurrentTrackIndex).setHasNext(true);

        } else {

            mRepeat = true;

            if (mTracksList.size() > 1) {
                mTracksList.get(mCurrentTrackIndex).setHasPrev(true);
                mTracksList.get(mCurrentTrackIndex).setHasNext(true);
            } else {
                mTracksList.get(mCurrentTrackIndex).setHasNext(false);
                mTracksList.get(mCurrentTrackIndex).setHasNext(false);
            }
        }

        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));
    }

    //______________________________________________________________________________________________
    //------------------------------MediaPlayer Public Functions------------------------------------
    //______________________________________________________________________________________________

    /**
     * called to start playing song
     */
    public void start() {
        // if no instance then init media player and let on prepared listener play the song
        if (mMediaPlayer == null && mTracksList.get(mCurrentTrackIndex) != null && !TextUtils.isEmpty(mTracksList.get(mCurrentTrackIndex).getId())) {
            initMediaPlayer(mTracksList.get(mCurrentTrackIndex).getId());
            return;
        }

        //Toast.makeText(getApplicationContext(), "Start enter", Toast.LENGTH_SHORT).show();

        // if media player is already active and there is network connection then play song
        // and start handler to update user about progress
        if (!mPlaying && isNetworkAvailable()) {
            setIsPlaying(true);
            mMediaPlayer.start();
            startHandler();
        }

    }

    /**
     * called to pause song
     */
    public void pause() {
        //Toast.makeText(getApplicationContext(), "Pause enter", Toast.LENGTH_SHORT).show();

        // if song is already playing then pause it and stop the handler from updating
        if (mPlaying) {
            setIsPlaying(false);
            mMediaPlayer.pause();
            stopHandler();
        }
    }

    /**
     * called to skip to next song
     *
     * @return true if succeeds to skip : there is a next song
     * false if fails to skip : last song in list
     */
    public boolean skipToNext() {

        // if there are no tracks then return false
        if (mTracksList == null || mTracksList.isEmpty())
            return false;

        // if last song in list return false
        ++mCurrentTrackIndex;
        if (mCurrentTrackIndex > mTracksList.size() - 1) {
            if (mRepeat) {
                mCurrentTrackIndex = 0;
            } else {
                mCurrentTrackIndex = mTracksList.size() - 1;
                mMediaPlayer.seekTo(mMediaPlayer.getDuration());
                mMediaPlayer.pause();
                setIsPlaying(false);
                return false;
            }
        }

        // there is next song so load it and update current track being played
        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));

        // reload media player instance to set data source to new song
        destroyMediaPlayer();
        initMediaPlayer(mTracksList.get(mCurrentTrackIndex).getId());

        return true;
    }

    /**
     * called to skip to previous song
     */
    public void skipToPrev() {

        // if no track in list then do nothing
        if (mTracksList == null || mTracksList.isEmpty())
            return;

        // if first song in list then just seek to beginning of song
        --mCurrentTrackIndex;
        if (mCurrentTrackIndex < 0) {
            if (mRepeat) {
                mCurrentTrackIndex = mTracksList.size() - 1;
            } else {
                mCurrentTrackIndex = 0;
            }
        }

        // there is previous song so load it and change global song to it
        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));

        // reload media player instance to set data source to new song
        destroyMediaPlayer();
        initMediaPlayer(mTracksList.get(mCurrentTrackIndex).getId());
    }

    /**
     * seek to specified location
     *
     * @param loc location to seek to
     */
    public void seek(int loc) {

        // if no player then don't seek
        if (mMediaPlayer == null) {
            return;
        }

        // get actual seeking location cus we got only from 0-100 to scale it to actual song
        int seekTo = (loc * mMediaPlayer.getDuration()) / 100;

        mMediaPlayer.seekTo(seekTo);
    }


    //______________________________________________________________________________________________
    //-----------------------------MediaPlayer private Functions------------------------------------
    //______________________________________________________________________________________________

    /**
     * init media player
     */
    private void initMediaPlayer(String trackId) {
        // if there is instance of media player then it has already been initialized so return
        if (mMediaPlayer != null)
            return;

        // if no track to play return nothing to do
        if (TextUtils.isEmpty(trackId)) {
            return;
        }

        // know if song has next or previous and set data accordingly
        if (mCurrentTrackIndex < 1)
            mTracksList.get(mCurrentTrackIndex).setHasPrev(false);
        else
            mTracksList.get(mCurrentTrackIndex).setHasPrev(true);

        if (mCurrentTrackIndex >= mTracksList.size() - 1)
            mTracksList.get(mCurrentTrackIndex).setHasNext(false);
        else
            mTracksList.get(mCurrentTrackIndex).setHasNext(true);

        if (mRepeat) {
            if (mTracksList.size() > 1) {
                mTracksList.get(mCurrentTrackIndex).setHasPrev(true);
                mTracksList.get(mCurrentTrackIndex).setHasNext(true);
            } else {
                mTracksList.get(mCurrentTrackIndex).setHasNext(false);
                mTracksList.get(mCurrentTrackIndex).setHasNext(false);
            }
            TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));
        }

        postCurrentlyPlaying(trackId);


        // create new instance of media player and set it to playing music
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build());


        // set the data source and prepare the data and setting the listener to this class
        try {
            SharedPreferences prefs =getSharedPreferences(getResources().getString(R.string.access_token_preference),MODE_PRIVATE);
            String access_token = prefs.getString("token", null);
            String songUrl = PLAYER_STREAMING_BASE_URL + trackId + PLAYER_STREAMING_URL_MIDDLE + access_token;
            mMediaPlayer.setDataSource(songUrl);
            mMediaPlayer.prepareAsync();
            setIsPlaying(false);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * setting if song is playing or not and updating current playing track accordingly
     *
     * @param playing caller determines the state of playing
     */
    private void setIsPlaying(boolean playing) {
        mPlaying = playing;
        mTracksList.get(mCurrentTrackIndex).setIsPlaying(mPlaying);
        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));
    }

    /**
     * utility function to destroy media player
     * and stop handler thread
     */
    private void destroyMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            stopHandler();
        }
    }


    //______________________________________________________________________________________________
    //-------------------------------Getting tracks-------------------------------------------------
    //______________________________________________________________________________________________


    private void postCurrentlyPlaying(String trackId) {
        if (TextUtils.isEmpty(trackId)) {
            return;
        }

        PostTrackId trackIdObj = new PostTrackId(trackId);

        Call<Void> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).postTrack(trackIdObj);

        request.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private void getCurrentlyPlaying() {
        RetrofitClient retrofitClient = RetrofitClient.getInstance();

        SharedPreferences prefs =getSharedPreferences(getResources().getString(R.string.access_token_preference),MODE_PRIVATE);
        String access_token = prefs.getString("token", null);
        retrofitClient.setToken(access_token);

        Call<CurrentlyPlayingTrackResponse> request = retrofitClient.getAPI(TrackPlayerApi.class).getCurrentlyPlaying();

        request.enqueue(new Callback<CurrentlyPlayingTrackResponse>() {
            @Override
            public void onResponse(Call<CurrentlyPlayingTrackResponse> call, Response<CurrentlyPlayingTrackResponse> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    TrackViewModel.getInstance().updateCurrentTrack(null);
                    return;
                }
                CurrentlyPlayingTrack track = response.body().getCurrentTrackWrapper().getCurrentTrack();
                String artistName = track.getArtists().get(0).getUserInfo().getName();
                Track currentTrack = new Track(track.getId(), track.getName(), track.getDuration(), artistName, Track.TYPE_ARTIST, artistName, track.getAlbum().getImages().get(0).getUrl());

                TrackViewModel.getInstance().updateCurrentTrack(currentTrack);

                mFirstInit = true;
                mFirstProgress = response.body().getCurrentTrackWrapper().getTrackProgress();
                mTracksList = new ArrayList<>();
                mTracksList.add(currentTrack);
                mCurrentTrackIndex = 0;
                destroyMediaPlayer();
                initMediaPlayer(currentTrack.getId());
            }

            @Override
            public void onFailure(Call<CurrentlyPlayingTrackResponse> call, Throwable t) {
                TrackViewModel.getInstance().updateCurrentTrack(null);
            }
        });
    }

    private void getTrack(String trackId) {
        Call<GetTrack> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).getTrack(trackId);

        request.enqueue(new Callback<GetTrack>() {
            @Override
            public void onResponse(Call<GetTrack> call, Response<GetTrack> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                GetTrack track = response.body();

                Track currentTrack = new Track(track.getId(), track.getName(), track.getDuration(), "", Track.TYPE_ARTIST, "", "");

                TrackViewModel.getInstance().updateCurrentTrack(currentTrack);

                mTracksList = new ArrayList<>();
                mTracksList.add(currentTrack);
                mCurrentTrackIndex = 0;
                getArtistInfo(track.getArtists().get(mCurrentTrackIndex), currentTrack);
                getAlbumImage(track.getAlbumId(), currentTrack);
                destroyMediaPlayer();
                initMediaPlayer(currentTrack.getId());
            }

            @Override
            public void onFailure(Call<GetTrack> call, Throwable t) {
            }
        });
    }

    private void getSeveralTracks(List<String> tracksIds, boolean shuffle) {

        String tracksIdsReadyString = tracksIds.get(0);

        for (int i = 1; i < tracksIds.size(); ++i) {
            tracksIdsReadyString += "," + tracksIds.get(i);
        }

        Call<GetSeveralTracks> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).getSeveralTracks(tracksIdsReadyString);

        request.enqueue(new Callback<GetSeveralTracks>() {
            @Override
            public void onResponse(Call<GetSeveralTracks> call, Response<GetSeveralTracks> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                List<GetTrack> tracks = response.body().getTracks();
                mTracksList = new ArrayList<>();

                for (int i = 0; i < tracks.size(); ++i) {
                    GetTrack currentLoop = tracks.get(i);
                    Track addTrack = new Track(currentLoop.getId(), currentLoop.getName(), currentLoop.getDuration(), "", Track.TYPE_ARTIST, "", "");
                    mTracksList.add(addTrack);
                    getArtistInfo(currentLoop.getArtists().get(0), addTrack);
                    getAlbumImage(currentLoop.getAlbumId(), addTrack);
                }

                if (shuffle) {
                    Collections.shuffle(mTracksList);
                }

                mCurrentTrackIndex = 0;

                destroyMediaPlayer();
                initMediaPlayer(mTracksList.get(mCurrentTrackIndex).getId());
            }

            @Override
            public void onFailure(Call<GetSeveralTracks> call, Throwable t) {

            }
        });
    }

    private void getAlbum(String albumId, boolean shuffle) {
        Call<GetAlbum> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).getAlbum(albumId);

        request.enqueue(new Callback<GetAlbum>() {
            @Override
            public void onResponse(Call<GetAlbum> call, Response<GetAlbum> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                GetAlbum album = response.body();

                AlbumImage image = album.getImages().get(0);

                List<GetTrack> tracks = album.getTracks();
                String albumName = album.getName();

                mTracksList = new ArrayList<>();

                for (int i = 0; i < tracks.size(); ++i) {
                    GetTrack track = tracks.get(i);
                    Track addTrack = new Track(track.getId(), track.getName(), track.getDuration(), "", Track.TYPE_ALBUM, albumName, image.getUrl());
                    mTracksList.add(addTrack);

                    getArtistInfo(track.getArtists().get(0), addTrack);
                }

                if (shuffle) {
                    Collections.shuffle(mTracksList);
                }

                mCurrentTrackIndex = 0;

                destroyMediaPlayer();
                initMediaPlayer(mTracksList.get(mCurrentTrackIndex).getId());
            }

            @Override
            public void onFailure(Call<GetAlbum> call, Throwable t) {

            }
        });
    }

    private void getPlaylist(String playlistId, boolean shuffle) {
        Call<GetPlaylist> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).getPlaylist(playlistId);

        request.enqueue(new Callback<GetPlaylist>() {
            @Override
            public void onResponse(Call<GetPlaylist> call, Response<GetPlaylist> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                List<GetPlaylistItem> items = response.body().getTracks().getItems();

                mTracksList = new ArrayList<>();

                for (int i = 0; i < items.size(); ++i) {
                    GetPlaylistTrack track = items.get(i).getTrack();

                    Track addTrack = new Track(track.getId(), track.getName(), track.getDuration(), "", Track.TYPE_PLAYLIST, response.body().getName(), "");
                    mTracksList.add(addTrack);

                    getArtistInfo(track.getArtists().get(0).getId(), addTrack);
                    getAlbumImage(track.getAlbum().getId(), addTrack);
                }

                if (shuffle) {
                    Collections.shuffle(mTracksList);
                }

                mCurrentTrackIndex = 0;

                destroyMediaPlayer();
                initMediaPlayer(mTracksList.get(mCurrentTrackIndex).getId());
            }

            @Override
            public void onFailure(Call<GetPlaylist> call, Throwable t) {
            }
        });
    }

    private void getArtistInfo(String artistId, Track track) {

        if (TextUtils.isEmpty(artistId) || track == null) {
            return;
        }

        Call<GetArtist> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).getArtist(artistId);

        request.enqueue(new Callback<GetArtist>() {
            @Override
            public void onResponse(Call<GetArtist> call, Response<GetArtist> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                String name = response.body().getName();

                if (!TextUtils.isEmpty(name)) {
                    track.setArtistName(name);
                    if (track.getType().equals(Track.TYPE_ARTIST)) {
                        track.setTypeName(name);
                    }
                    if (track == mTracksList.get(mCurrentTrackIndex)) {
                        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));
                    }
                }
            }

            @Override
            public void onFailure(Call<GetArtist> call, Throwable t) {

            }
        });
    }

    private void getAlbumImage(String albumId, Track track) {
        if (TextUtils.isEmpty(albumId) || track == null) {
            return;
        }

        Call<GetAlbum> request = RetrofitClient.getInstance().getAPI(TrackPlayerApi.class).getAlbum(albumId);

        request.enqueue(new Callback<GetAlbum>() {
            @Override
            public void onResponse(Call<GetAlbum> call, Response<GetAlbum> response) {
                if ((!response.isSuccessful()) || (response.code() != 200)) {
                    return;
                }

                if (response.body() == null) {
                    return;
                }

                String imageUrl = response.body().getImages().get(0).getUrl();

                if (!TextUtils.isEmpty(imageUrl)) {
                    track.setImage(imageUrl);
                    if (track == mTracksList.get(mCurrentTrackIndex)) {
                        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));
                    }
                }
            }

            @Override
            public void onFailure(Call<GetAlbum> call, Throwable t) {

            }
        });
    }

    // _____________________________________________________________________________________________
    // --------------------------------Media player callbacks---------------------------------------
    // _____________________________________________________________________________________________

    /**
     * called when media player finished getting song from url
     */
    @Override
    public void onPrepared(MediaPlayer mp) {

        // update live data info
        mTracksList.get(mCurrentTrackIndex).setDuration(mMediaPlayer.getDuration());
        TrackViewModel.getInstance().updateCurrentTrack(mTracksList.get(mCurrentTrackIndex));

        // if first init then don't start song
        if (mFirstInit) {
            mFirstInit = false;
            mMediaPlayer.seekTo(mFirstProgress);
            mFirstProgress = 0;
            mMediaPlayer.pause();
            setIsPlaying(false);
            return;
        }

        // if not playing and there is network connection then play song and update global
        // live data variable
        // start the handler
        if (!mPlaying && isNetworkAvailable()) {
            setIsPlaying(true);
            mMediaPlayer.start();
            startHandler();
        }

    }

    /**
     * when song finishes playing this is called and skip to next song if available
     * if not then stop playing
     *
     * @param mp media player object playing
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!skipToNext()) {
            setIsPlaying(false);
            stopHandler();
        }

        //Toast.makeText(getApplicationContext(), "Completion enter", Toast.LENGTH_SHORT).show();
    }


    //______________________________________________________________________________________________
    //------------------------------------Utility Functions-----------------------------------------
    //______________________________________________________________________________________________

    /**
     * start the handler to watch progress and report it
     */
    private void startHandler() {
        mHandler.postDelayed(mRunnable, HANDLER_DELAY);
    }

    /**
     * stop handler to not leak any resources in the end
     */
    private void stopHandler() {
        mHandler.removeCallbacks(mRunnable); //stop handler when activity not visible
    }

    /**
     * check if there is network connectivity or not
     *
     * @return true : network connection available
     * false : no network connection
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
