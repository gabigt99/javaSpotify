package bg.sofia.uni.fmi.mjt.spotify.server.song;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.SongNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SongStorageTest {

    SongStorage songStorage;

    @BeforeEach
    public void setUp() {
        Song helloSong = new Song("Hello", "No one", "audio.wav");
        Song everSong = new Song("Ever", "No one", "audio.wav");
        Song neverSong = new Song("Never", "No one", "audio.wav");
        Map<List<String>, Song> songs = new HashMap<>();
        songs.put(List.of(helloSong.getName(), helloSong.getArtist()), helloSong);
        songs.put(List.of(everSong.getName(), everSong.getArtist()), everSong);
        songs.put(List.of(neverSong.getName(), neverSong.getArtist()), neverSong);
        songStorage = new SongStorage(songs);
    }

    @Test
    public void testPlaySong() throws UnsupportedAudioFileException, IOException, SongNotFoundException {
        assertEquals("songRepository\\audio.wav+format,8000.0,16,2,4,8000.0,false", songStorage.play("Hello", "No one"), "Unexpected exit after play music");
    }

    @Test
    public void testPlaySongWithNullName() {
        assertThrows(IllegalArgumentException.class, () ->songStorage.play(null, "No one"), "Unexpected exit after play music");
    }

    @Test
    public void testPlaySongWithNullArtist() {
        assertThrows(IllegalArgumentException.class, () ->songStorage.play("Hello", null), "Unexpected exit after play music");
    }

    @Test
    public void testPlayNotExistsSong() {
        assertThrows(SongNotFoundException.class, () ->songStorage.play("Bye", "No one"), "Unexpected exit after play music");
    }

    @Test
    public void testStopSong() throws UnsupportedAudioFileException, IOException, SongNotFoundException {
        songStorage.play("Hello", "No one");
        songStorage.stop("Hello", "No one");
        assertEquals("No songs are currently being listened to.", songStorage.getTopNListenedSongsAtTheMoment(1), "Unexpected exit after stop music");
    }

    @Test
    public void testStopSongWithNullName() {
        assertThrows(IllegalArgumentException.class, () ->songStorage.stop(null, "No one"), "Unexpected exit after play music");
    }

    @Test
    public void testStopSongWithNullArtist() {
        assertThrows(IllegalArgumentException.class, () ->songStorage.stop("Hello", null), "Unexpected exit after play music");
    }

    @Test
    public void testStopNotExistsSong() {
        assertThrows(SongNotFoundException.class, () ->songStorage.stop("Bye", "No one"), "Unexpected exit after play music");
    }

    @Test
    public void testTopNSongsWhenSongsAreNotAllPlayed() throws UnsupportedAudioFileException, IOException, SongNotFoundException {
        songStorage.play("Hello", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Never", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Never", "No one");
        assertEquals("Hello - No one" + System.lineSeparator() + "Never - No one" + System.lineSeparator(), songStorage.getTopNListenedSongsAtTheMoment(3), "Unexpected exit after get top N songs");
    }

    @Test
    public void testTopNSongsWhenPlayedSongsAreMoreThanN() throws UnsupportedAudioFileException, IOException, SongNotFoundException {
        songStorage.play("Hello", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Never", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Never", "No one");
        assertEquals("Hello - No one" + System.lineSeparator(), songStorage.getTopNListenedSongsAtTheMoment(1), "Unexpected exit after get top N songs");
    }

    @Test
    public void testTopNSongsWhenPlayedSongsAreLessThanN() throws UnsupportedAudioFileException, IOException, SongNotFoundException {
        songStorage.play("Hello", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Never", "No one");
        songStorage.play("Hello", "No one");
        songStorage.play("Never", "No one");
        assertEquals("Hello - No one" + System.lineSeparator() + "Never - No one" + System.lineSeparator(), songStorage.getTopNListenedSongsAtTheMoment(10), "Unexpected exit after get top N songs");
    }

    @Test
    public void testTopNSongsWhenNotPlayedSongs() {
        assertEquals("No songs are currently being listened to.", songStorage.getTopNListenedSongsAtTheMoment(10), "Unexpected exit after get top N songs");
    }

    @Test
    public void testTopNSongsWhenNIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> songStorage.getTopNListenedSongsAtTheMoment(-10), "Unexpected exit after get top N songs");
    }

    @Test
    public void testSearchSong() {
        assertEquals("Hello - No one" + System.lineSeparator() +
                "Ever - No one" + System.lineSeparator() +
                "Never - No one" + System.lineSeparator(), songStorage.search("No", "one"), "Unexpected exit after search song");
        assertEquals("Hello - No one" + System.lineSeparator(), songStorage.search("No", "one", "hello"), "Unexpected exit after search song");
    }

    @Test
    public void testSearchWithNullWords() {
        assertThrows(IllegalArgumentException.class, () -> songStorage.search(null), "Unexpected exit after search song");
    }

    @Test
    public void testSearchWithSomeWordsAreNull() {
        assertThrows(IllegalArgumentException.class, () -> songStorage.search("No", null, "one"), "Unexpected exit after search song");
    }

    @Test
    public void testSearchWhenNotAllWordsMatches() {
        assertEquals("There is no song that contains these words in its name and artist", songStorage.search("No", "one", "Try"), "Unexpected exit after search song");
    }
}