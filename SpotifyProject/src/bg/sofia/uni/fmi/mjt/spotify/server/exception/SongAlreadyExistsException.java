package bg.sofia.uni.fmi.mjt.spotify.server.exception;

public class SongAlreadyExistsException extends Exception {
    public SongAlreadyExistsException() {
        super("Song already exists");
    }
}
