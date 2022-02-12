package bg.sofia.uni.fmi.mjt.spotify.server.song;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.SongNotFoundException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.*;

public class SongStorage {
    private static final String COMMA = ",";
    private Map<List<String>, Song> songs;

    public SongStorage(Map<List<String>, Song> songs) {
        this.songs = songs;
    }

    public String search(String... words) {
        if (words == null || Arrays.stream(words)
                .filter(Objects::isNull)
                .count() != 0) {
            throw new IllegalArgumentException();
        }
        return songs.values().stream()
                .filter(song -> song.ifConstrainsAllWords(words))
                .map(song -> song.getName() + " - " + song.getArtist() + System.lineSeparator())
                .reduce(String::concat)
                .orElse("There is no song that contains these words in its name and artist");
    }

    public String getTopNListenedSongsAtTheMoment(int n) {
        if (n < 0) {
            throw new IllegalArgumentException();
        }
        return songs.values().stream()
                .filter(song -> song.getListenedAtTheMoment() > 0)
                .sorted(Comparator.comparing(Song::getListenedAtTheMoment).reversed())
                .limit(n)
                .map(song -> song.getName() + " - " + song.getArtist() + System.lineSeparator())
                .reduce(String::concat)
                .orElse("No songs are currently being listened to.");
    }

    public String play(String songName, String artist) throws SongNotFoundException, UnsupportedAudioFileException, IOException {
        if (songName == null || artist == null) {
            throw new IllegalArgumentException();
        }

        List key = List.of(songName, artist);
        if (songs.get(key) == null) {
            throw new SongNotFoundException();
        }
        Song song = songs.get(key);
        song.listen();
        return song.getSongPath() + "+" + audioFormatToString(song.getFormat());
    }

    public void stop(String songName, String artist) throws SongNotFoundException {
        if (songName == null || artist == null) {
            throw new IllegalArgumentException();
        }

        List key = List.of(songName, artist);
        if (songs.get(key) == null) {
            throw new SongNotFoundException();
        }
        songs.get(key).stopListen();
    }

    private String audioFormatToString(AudioFormat audioFormat) {
        String format = "format,";
        format += audioFormat.getSampleRate() + COMMA;
        format += audioFormat.getSampleSizeInBits() + COMMA;
        format += audioFormat.getChannels() + COMMA;
        format += audioFormat.getFrameSize() + COMMA;
        format += audioFormat.getFrameRate() + COMMA;
        format += audioFormat.isBigEndian();
        return format;
    }

    public boolean isExists(String songName, String artist) {
        if (songName == null || artist == null) {
            throw new IllegalArgumentException();
        }
        return songs.get(new String[]{songName, artist}) != null;
    }
}
