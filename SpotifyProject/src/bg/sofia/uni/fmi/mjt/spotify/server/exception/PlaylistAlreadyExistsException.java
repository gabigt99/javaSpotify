package bg.sofia.uni.fmi.mjt.spotify.server.exception;

public class PlaylistAlreadyExistsException extends Exception {
    public PlaylistAlreadyExistsException() {
        super("Playlist already exists");
    }
}
