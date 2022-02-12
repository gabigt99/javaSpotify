package bg.sofia.uni.fmi.mjt.spotify.server.exception;

public class AccountAlreadyExistsException extends Exception{
    public AccountAlreadyExistsException() {
        super("Account already exists");
    }
}
