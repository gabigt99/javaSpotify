package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.PlaylistAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.PlaylistNotFoundException;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.SongAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.song.Playlist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileTest {

    private Profile profile;

    @BeforeEach
    public void setUp() {
        profile = new Profile("Gabi");
    }

    @Test
    public void testAddPlaylist() throws PlaylistAlreadyExistsException {
        profile.addPlaylist("rock");
        assertEquals(Map.of("rock", new Playlist("rock")), profile.getPlaylist(), "Unexpected exit after add playlist");
    }

    @Test
    public void testAddPlaylistWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> profile.addPlaylist(null), "Unexpected exit after add playlist");
    }

    @Test
    public void testAddPlaylistWithBlankName() {
        assertThrows(IllegalArgumentException.class, () -> profile.addPlaylist(""), "Unexpected exit after add playlist");
    }

    @Test
    public void testAddPlaylistWhenAlreadyExists() throws PlaylistAlreadyExistsException {
        profile.addPlaylist("rock");
        assertThrows(PlaylistAlreadyExistsException.class, () -> profile.addPlaylist("rock"), "Unexpected exit after add playlist");
    }

    @Test
    public void testAddSong() throws SongAlreadyExistsException, PlaylistNotFoundException, PlaylistAlreadyExistsException {
        profile.addPlaylist("rock");
        profile.addSong("rock", "Hello", "World");
        Playlist playlist = new Playlist("rock");
        playlist.add("Hello", "World");
        assertEquals(Map.of("rock", playlist), profile.getPlaylist(), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongInNotExistingPlaylist() {
        assertThrows(PlaylistNotFoundException.class, () -> profile.addSong("rock", "Hello", "World"), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongForSecondTimeInSamePlaylist() throws SongAlreadyExistsException, PlaylistNotFoundException, PlaylistAlreadyExistsException {
        profile.addPlaylist("rock");
        profile.addSong("rock", "Hello", "World");
        assertThrows(SongAlreadyExistsException.class, () -> profile.addSong("rock", "Hello", "World"), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongWhenPlaylistNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> profile.addSong(null, "Hello", "World"), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongWhenSongNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> profile.addSong("rock", null, "World"), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongWhenArtistIsNull() {
        assertThrows(IllegalArgumentException.class, () -> profile.addSong("rock", "Hello", null), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongWhenSongNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> profile.addSong("rock", "   ", "World"), "Unexpected exit after add song");
    }

    @Test
    public void testAddSongWhenArtistIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> profile.addSong("rock", "Hello", ""), "Unexpected exit after add song");
    }

    @Test
    public void testShowPlaylist() throws PlaylistAlreadyExistsException, SongAlreadyExistsException, PlaylistNotFoundException {
        profile.addPlaylist("rock");
        profile.addSong("rock", "Hello", "World");
        profile.addSong("rock", "Bye", "World");
        assertEquals("Hello - World" + System.lineSeparator() +
                "Bye - World" + System.lineSeparator(),profile.showPlaylist("rock"), "Unexpected exit after show playlist");
    }

    @Test
    public void testShowPlaylistWithoutSongs() throws PlaylistAlreadyExistsException, PlaylistNotFoundException {
        profile.addPlaylist("rock");
        assertEquals("There are no songs.",profile.showPlaylist("rock"), "Unexpected exit after show playlist");
    }

    @Test
    public void testShowPlaylistWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> profile.showPlaylist(null), "Unexpected exit after show playlist");
    }

    @Test
    public void testShowPlaylistWhenNotExists() {
        assertThrows(PlaylistNotFoundException.class, () -> profile.showPlaylist("rock"), "Unexpected exit after show playlist");
    }
}