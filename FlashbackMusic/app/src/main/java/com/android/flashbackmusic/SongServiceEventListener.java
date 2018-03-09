package com.android.flashbackmusic;

/**
 * Created by K on 3/8/2018.
 */

public interface SongServiceEventListener {
    void onSongLoaded(Song loadedSong);
    void onSongCompleted(Song completedSong, Song nextSong);
    void onSongPaused(Song currentSong);
    void onSongSkipped(Song skippedSong, Song nextSong);
    void onSongResumed(Song currentSong);
    void onVibeModeToggled(boolean vibeModeOn);
}
