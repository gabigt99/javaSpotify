package bg.sofia.uni.fmi.mjt.spotify.server.song;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class Song {
    private String name;
    private String artist;
    private String fileName;
    private int listenedAtTheMoment;

    public Song(String name, String artist, String fileName) {
        this.name = name;
        this.artist = artist;
        this.fileName = fileName;
        listenedAtTheMoment = 0;
    }

    public boolean ifConstrainsAllWords(String...words) {
        String nameLowerCase = getName().toLowerCase();
        String contractorLowerCase = getArtist().toLowerCase();
        for (String word : words) {
            if(!nameLowerCase.contains(word.toLowerCase()) &&
                    !contractorLowerCase.contains(word.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public void listen() {
        listenedAtTheMoment++;
    }

    public void stopListen() {
        listenedAtTheMoment--;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public AudioFormat getFormat () throws UnsupportedAudioFileException, IOException {
        return AudioSystem.getAudioInputStream(new File(getSongPath())).getFormat();
    }

    public int getListenedAtTheMoment() {
        return listenedAtTheMoment;
    }

    public String getSongPath() {
        return "songRepository" + File.separator + fileName;
    }
}
