package bg.sofia.uni.fmi.mjt.spotify.server.song;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.SongAlreadyExistsException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Playlist {
    private final String name;
    private final Map<String, String> songs;

    public Playlist(String name) {
        this.name = name;
        songs = new HashMap<>();
    }

    public void add(String songName, String artist) throws SongAlreadyExistsException {
        if (songName == null || songName.isBlank() || artist == null || artist.isBlank()) {
            throw new IllegalArgumentException();
        }
        if (songs.get(songName) != null) {
            throw new SongAlreadyExistsException();
        }
        songs.put(songName, artist);
    }

    public String show() {
        return songs.entrySet().stream()
                .map(song -> song.getKey() + " - " + song.getValue() + System.lineSeparator())
                .reduce(String::concat)
                .orElse("There are no songs.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist playlist = (Playlist) o;
        return Objects.equals(name, playlist.name) && Objects.equals(songs, playlist.songs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, songs);
    }
}
