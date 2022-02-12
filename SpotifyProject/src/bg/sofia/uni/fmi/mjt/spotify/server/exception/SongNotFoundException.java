package bg.sofia.uni.fmi.mjt.spotify.server.exception;

public class SongNotFoundException extends Exception{
    public SongNotFoundException() {
        super("Song not found");
    }
}
