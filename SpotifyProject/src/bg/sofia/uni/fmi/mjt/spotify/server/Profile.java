package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.PlaylistNotFoundException;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.song.Playlist;

import java.util.HashMap;
import java.util.Map;

public class Profile {
    private String username;
    private Map<String, Playlist> playlists;

    public Profile(String username) {
        this.username = username;
        playlists = new HashMap<>();
    }

    public String getUsername() {
        return username;
    }

    public Map<String, Playlist> getPlaylist() {
        return playlists;
    }

    public void addPlaylist(String playlistName) throws PlaylistAlreadyExistsException {
        if (playlistName == null || playlistName.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (playlists.get(playlistName) != null) {
            throw new PlaylistAlreadyExistsException();
        }
        playlists.put(playlistName, new Playlist(playlistName));
    }

    public void addSong(String playlistName, String songName, String artist) throws PlaylistNotFoundException, SongAlreadyExistsException {
        if (playlistName == null || songName == null ||
                songName.isBlank() || artist == null || artist.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (playlists.get(playlistName) == null) {
            throw new PlaylistNotFoundException();
        }
        playlists.get(playlistName).add(songName, artist);
    }

    public String showPlaylist(String playlistName) throws PlaylistNotFoundException {
        if (playlistName == null) {
            throw new IllegalArgumentException();
        }
        if (playlists.get(playlistName) == null) {
            throw new PlaylistNotFoundException();
        }
        return playlists.get(playlistName).show();
    }
}
