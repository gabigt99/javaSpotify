package bg.sofia.uni.fmi.mjt.spotify.server.account;

import java.util.Objects;

public class Account {
    private final String email;
    private final String password;

    public Account(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean isPasswordCorrect(String password) {
        return this.password.equals(password);
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account user = (Account) o;
        return Objects.equals(email, user.email) && Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return email + " " + password + "\n";
    }
}
