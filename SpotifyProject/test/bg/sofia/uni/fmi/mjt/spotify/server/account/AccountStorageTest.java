package bg.sofia.uni.fmi.mjt.spotify.server.account;

import bg.sofia.uni.fmi.mjt.spotify.server.exception.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccountStorageTest {

    AccountStorage accountStorage;
    @BeforeEach
    public void setUp() {
        Account gabiAccount = new Account("gabi@abv.bg", "12345678");
        Account stefanAccount = new Account("stefan@abv.bg", "1234");
        Map<String, Account> accounts = new HashMap<>();
        accounts.put(gabiAccount.getEmail(), gabiAccount);
        accounts.put(stefanAccount.getEmail(), stefanAccount);
        accountStorage = new AccountStorage(accounts);
    }

    @Test
    public void testRegisterValidEmail() throws AccountAlreadyExistsException {
        accountStorage.register("kali@abv.bg", "12345");
        assertEquals(Set.of("gabi@abv.bg", "stefan@abv.bg", "kali@abv.bg"), accountStorage.getUsernames(), "Unexpected exit after registration");
    }

    @Test
    public void testRegisterInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> accountStorage.register("kali", "12345"), "Unexpected exit after registration");
    }

    @Test
    public void testRegisterNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> accountStorage.register(null, "12345"), "Unexpected exit after registration");
    }

    @Test
    public void testRegisterNullPassword() {
        assertThrows(IllegalArgumentException.class, () -> accountStorage.register("kali@abv.bg", null), "Unexpected exit after registration");
    }

    @Test
    public void testRegisterWhenUserExists() {
        assertThrows(AccountAlreadyExistsException.class, () -> accountStorage.register("gabi@abv.bg", "12345"), "Unexpected exit after registration");
    }

    @Test
    public void testLogInWhenUserExists() throws AccountNotFoundException {
        assertTrue(accountStorage.login("gabi@abv.bg", "12345678"), "Unexpected exit after log in");
    }

    @Test
    public void testLogInWhenUserExistsAndPasswordNotCorrect() throws AccountNotFoundException {
        assertFalse(accountStorage.login("gabi@abv.bg", "2345678"), "Unexpected exit after log in");
    }

    @Test
    public void testLogInWhenUserNotExists() {
        assertThrows(AccountNotFoundException.class, () ->accountStorage.login("kali@abv.bg", "12345678"), "Unexpected exit after log in");
    }

    @Test
    public void testLogInWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () ->accountStorage.login(null, "12345678"), "Unexpected exit after log in");
    }

    @Test
    public void testLogInWithNullPassword() {
        assertThrows(IllegalArgumentException.class, () ->accountStorage.login("gabi@abv.bg", null), "Unexpected exit after log in");
    }

}