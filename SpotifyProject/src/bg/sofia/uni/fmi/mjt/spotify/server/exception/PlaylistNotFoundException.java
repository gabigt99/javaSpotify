package bg.sofia.uni.fmi.mjt.spotify.server.exception;

public class PlaylistNotFoundException extends Exception {
    public PlaylistNotFoundException() {
        super("Playlist not found");
    }
}
