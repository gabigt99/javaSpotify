package bg.sofia.uni.fmi.mjt.spotify.server.account;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.AccountNotFoundException;

import java.util.Map;
import java.util.Set;

public class AccountStorage {
    private Map<String, Account> accounts;

    public AccountStorage(Map<String, Account> accounts) {
        this.accounts = accounts;
    }

    public void register(String email, String password) throws AccountAlreadyExistsException {
        if(email == null || !isValidEmailFormat(email) || password == null) {
            throw new IllegalArgumentException();
        }
        if(accounts.get(email) != null) {
            throw new AccountAlreadyExistsException();
        }
        accounts.put(email, new Account(email, password));
    }

    public boolean login(String email, String password) throws AccountNotFoundException {
        if(email == null || password == null) {
            throw new IllegalArgumentException();
        }
        if (accounts.get(email) == null) {
            throw new AccountNotFoundException();
        }
        return accounts.get(email).isPasswordCorrect(password);
    }

    private boolean isValidEmailFormat(String email) {
        String[] haveAt = email.split("@");
        if (haveAt.length == 2) {
            String[] haveDot = haveAt[1].split("\\.");
            return haveDot.length == 2 && !haveAt[0].isBlank() &&
                    !haveDot[0].isBlank() && !haveDot[1].isBlank();
        }
        return false;
    }

    public Set<String> getUsernames() {
        return Set.copyOf(accounts.keySet());
    }

    public Account getAccountByUsername(String username) {
        return accounts.get(username);
    }
}
