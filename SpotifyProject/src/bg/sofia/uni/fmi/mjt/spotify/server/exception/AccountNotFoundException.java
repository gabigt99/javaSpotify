package bg.sofia.uni.fmi.mjt.spotify.server.exception;

public class AccountNotFoundException extends Exception {
    public AccountNotFoundException() {
        super("Account not found");
    }
}
