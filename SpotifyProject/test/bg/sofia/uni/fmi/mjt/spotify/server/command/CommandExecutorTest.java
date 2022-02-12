package bg.sofia.uni.fmi.mjt.spotify.server.command;

import bg.sofia.uni.fmi.mjt.spotify.server.MemorySaver;
import bg.sofia.uni.fmi.mjt.spotify.server.Profile;
import bg.sofia.uni.fmi.mjt.spotify.server.Storage;
import bg.sofia.uni.fmi.mjt.spotify.server.account.AccountStorage;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.*;
import bg.sofia.uni.fmi.mjt.spotify.server.song.SongStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CommandExecutorTest {

    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"" + System.lineSeparator();
    private static final String NOT_LOGGED_IN = "You must be logged in to execute this command!" + System.lineSeparator();

    private static final String REGISTER = "register";
    private static final String LOGIN = "login";
    private static final String SEARCH = "search";
    private static final String TOP = "top";
    private static final String CREATE_PLAYLIST = "create-playlist";
    private static final String ADD_SONG_TO = "add-song-to";
    private static final String SHOW_PLAYLIST = "show-playlist";
    private static final String PLAY = "play";
    private static final String STOP = "stop";
    private static final String DISCONNECT = "disconnect";

    private SelectionKey key;
    private Storage storage;
    private CommandExecutor cmdExecutor;
    private MemorySaver memoryManager;
    private AccountStorage accountStorage;
    private Map<String, Profile> profileStorage;
    private SongStorage songStorage;

    @BeforeEach
    public void setUp() {

        key = mock(SelectionKey.class);
        memoryManager = mock(MemorySaver.class);
        accountStorage = mock(AccountStorage.class);
        Profile gabi = mock(Profile.class);
        profileStorage = new HashMap<>();
        profileStorage.put("gabi@abv.bg", gabi);
        songStorage = mock(SongStorage.class);
        storage = new Storage(memoryManager, accountStorage, profileStorage, songStorage);
        cmdExecutor = new CommandExecutor(storage);
    }

    @Test
    public void testRegisterWithMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2,
                REGISTER + " <email> <password>");
        String actual = cmdExecutor.execute(key, new Command(REGISTER, new String[]{"gabi@abv.bg", "123", "456"}));

        assertEquals(expected, actual, "Unexpected output for 'register'");
    }

    @Test
    public void testRegisterSuccessfully() {
        String expected = "You have successfully registered!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(REGISTER, new String[]{"stefan@abv.bg", "123"}));

        assertEquals(expected, actual, "Unexpected output for 'register'");
    }

    @Test
    public void testRegisterWhenAccountExists() throws AccountAlreadyExistsException {
        doThrow(AccountAlreadyExistsException.class)
                .when(accountStorage).register("gabi@abv.bg", "12345678");

        String expected = "A user with such an email already exists." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(REGISTER, new String[]{"gabi@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'register'");
    }

    @Test
    public void testRegisterWhenAccountIsLoggedIn() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = "You are logged in. You cannot register." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(REGISTER, new String[]{"gabi@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'register'");
    }

    @Test
    public void testRegisterWhenItIsNotEmail() throws AccountAlreadyExistsException {
        doThrow(IllegalArgumentException.class).when(accountStorage).register("gabi", "12345678");

        String expected = "Wrong email format!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(REGISTER, new String[]{"gabi", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'register'");
    }

    @Test
    public void testLoginWithLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2,
                LOGIN + " <email> <password>");
        String actual = cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg"}));

        assertEquals(expected, actual, "Unexpected output for 'login'");
    }

    @Test
    public void testLoginWhenAccountExists() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);

        String expected = String.format("You have successfully logged in" + System.lineSeparator());
        String actual = cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'login'");
    }

    @Test
    public void testLoginWhenAccountExistsWrongPassword() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(false);

        String expected = String.format("Wrong password. Please, try again" + System.lineSeparator());
        String actual = cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'login'");
    }

    @Test
    public void testLoginWhenAccountNotExists() throws AccountNotFoundException {
        when(accountStorage.login("stefan@abv.bg", "12345678")).thenThrow(AccountNotFoundException.class);

        String expected = String.format("Such an account does not exist. You can register." + System.lineSeparator());
        String actual = cmdExecutor.execute(key, new Command(LOGIN, new String[]{"stefan@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'login'");
    }

    @Test
    public void testLoginWhenAccountIsAlreadyLoggedIn() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = String.format("You are logged in. You cannot do it again." + System.lineSeparator());
        String actual = cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'login'");
    }

    @Test
    public void testLoginWhenAccountIsAlreadyLoggedInWithAnotherKey() throws AccountNotFoundException {
        SelectionKey secondKey = mock(SelectionKey.class);
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = String.format("You are logged in. You cannot do it again." + System.lineSeparator());
        String actual = cmdExecutor.execute(secondKey, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        assertEquals(expected, actual, "Unexpected output for 'login'");
    }

    @Test
    public void testSearchWithLessArguments() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = String.format("Invalid count of arguments: search expects more than zero arguments. Example: \"%s\"" + System.lineSeparator(),
                SEARCH + " <words>");
        String actual = cmdExecutor.execute(key, new Command(SEARCH, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'search'");
    }

    @Test
    public void testSearchWhenSongsExist() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        when(songStorage.search("me", "Shakira")).thenReturn("Girl like me - Shakira" + System.lineSeparator());

        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = String.format("Girl like me - Shakira" + System.lineSeparator() + System.lineSeparator());
        String actual = cmdExecutor.execute(key, new Command(SEARCH, new String[]{"me", "Shakira"}));

        assertEquals(expected, actual, "Unexpected output for 'search'");
    }

    @Test
    public void testSearchWhenNotLoggedIn() {
        String expected = String.format(NOT_LOGGED_IN);
        String actual = cmdExecutor.execute(key, new Command(SEARCH, new String[]{"me", "Shakira"}));

        assertEquals(expected, actual, "Unexpected output for 'search'");
    }

    @Test
    public void testTopNWithMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, TOP, 1,
                TOP + " <number>");
        String actual = cmdExecutor.execute(key, new Command(TOP, new String[]{"23", "43"}));

        assertEquals(expected, actual, "Unexpected output for 'top'");
    }

    @Test
    public void testTopN() throws AccountNotFoundException {
        when(songStorage.getTopNListenedSongsAtTheMoment(1)).thenReturn("Girl like me - Shakira" + System.lineSeparator());
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = "Girl like me - Shakira" + System.lineSeparator() + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(TOP, new String[]{"1"}));

        assertEquals(expected, actual, "Unexpected output for 'top'");
    }

    @Test
    public void testTopNWhenNotLoggedIn() {
        String expected = NOT_LOGGED_IN;
        String actual = cmdExecutor.execute(key, new Command(TOP, new String[]{"1"}));

        assertEquals(expected, actual, "Unexpected output for 'top'");
    }

    @Test
    public void testTopNWhenNIsNotNumber() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = "You must enter a number!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(TOP, new String[]{"number"}));

        assertEquals(expected, actual, "Unexpected output for 'top'");
    }

    @Test
    public void testTopNWhenNIsNegativeNumber() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.getTopNListenedSongsAtTheMoment(-1)).thenThrow(IllegalArgumentException.class);

        String expected = "The number must not be negative!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(TOP, new String[]{"-1"}));

        assertEquals(expected, actual, "Unexpected output for 'top'");
    }

    @Test
    public void testCreatePlaylistWithLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CREATE_PLAYLIST, 1,
                CREATE_PLAYLIST + " <name_of_the_playlist>");
        String actual = cmdExecutor.execute(key, new Command(CREATE_PLAYLIST, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'create-playlist'");
    }

    @Test
    public void testCreatePlaylistWhenNotLoggedIn() {
        String expected = NOT_LOGGED_IN;
        String actual = cmdExecutor.execute(key, new Command(CREATE_PLAYLIST, new String[]{"Hey"}));

        assertEquals(expected, actual, "Unexpected output for 'create-playlist'");
    }

    @Test
    public void testCreatePlaylistSuccessfully() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = "A playlist with a name Hello created successfully!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(CREATE_PLAYLIST, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'create-playlist'");
    }

    @Test
    public void testCreatePlaylistWhenExists() throws AccountNotFoundException, PlaylistAlreadyExistsException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        doThrow(PlaylistAlreadyExistsException.class).when(profileStorage.get("gabi@abv.bg")).addPlaylist("Hello");

        String expected = "A playlist with such a name already exists." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(CREATE_PLAYLIST, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'create-playlist'");
    }

    @Test
    public void testCreatePlaylistWithWrongName() throws AccountNotFoundException, PlaylistAlreadyExistsException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        doThrow(IllegalArgumentException.class).when(profileStorage.get("gabi@abv.bg")).addPlaylist("   ");

        String expected = "The name cannot be blank." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(CREATE_PLAYLIST, new String[]{"   "}));

        assertEquals(expected, actual, "Unexpected output for 'create-playlist'");
    }

    @Test
    public void testAddSongToWithLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, ADD_SONG_TO, 3,
                ADD_SONG_TO + " <name_of_the_playlist> <song_name> <artist>");
        String actual = cmdExecutor.execute(key, new Command(ADD_SONG_TO, new String[]{"Hello", "Say my name"}));

        assertEquals(expected, actual, "Unexpected output for 'add-song-to'");
    }

    @Test
    public void testAddSongToWhenNotLoggedIn() {
        String expected = NOT_LOGGED_IN;
        String actual = cmdExecutor.execute(key, new Command(ADD_SONG_TO, new String[]{"Hey", "World", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'add-song-to'");
    }

    @Test
    public void testAddSongToSuccessfully() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.isExists("World", "Adel")).thenReturn(true);

        String expected = "You have successfully added new song in playlist Hello!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(ADD_SONG_TO, new String[]{"Hello", "World", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'add-song-to'");
    }

    @Test
    public void testAddSongToWhenSongNotExists() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.isExists("World", "Adel")).thenReturn(false);

        String expected = "A song with such a name does not exist." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(ADD_SONG_TO, new String[]{"Hello", "World", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'add-song-to'");
    }

    @Test
    public void testAddSongToWhenPlaylistNotExists() throws AccountNotFoundException, SongAlreadyExistsException, PlaylistNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.isExists("World", "Adel")).thenReturn(true);
        doThrow(PlaylistNotFoundException.class).when(profileStorage.get("gabi@abv.bg")).addSong("Hello", "World", "Adel");

        String expected = "A playlist with such a name does not exist." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(ADD_SONG_TO, new String[]{"Hello", "World", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'add-song-to'");
    }

    @Test
    public void testAddSongToWhenSongAlreadyExistsInPlaylist() throws AccountNotFoundException, SongAlreadyExistsException, PlaylistNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.isExists("World", "Adel")).thenReturn(true);
        doThrow(SongAlreadyExistsException.class).when(profileStorage.get("gabi@abv.bg")).addSong("Hello", "World", "Adel");

        String expected = "A song is already added in playlist Hello. Cannot be added a second time." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(ADD_SONG_TO, new String[]{"Hello", "World", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'add-song-to'");
    }

    @Test
    public void testShowPlaylistWithMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SHOW_PLAYLIST, 1,
                SHOW_PLAYLIST + " <name_of_the_playlist>");
        String actual = cmdExecutor.execute(key, new Command(SHOW_PLAYLIST, new String[]{"Hello", "Say my name"}));

        assertEquals(expected, actual, "Unexpected output for 'show-playlist'");
    }

    @Test
    public void testShowPlaylistWhenNotLoggedIn() {
        String expected = NOT_LOGGED_IN;
        String actual = cmdExecutor.execute(key, new Command(SHOW_PLAYLIST, new String[]{"Hey"}));

        assertEquals(expected, actual, "Unexpected output for 'show-playlist'");
    }

    @Test
    public void testShowPlaylistSuccessfully() throws AccountNotFoundException, PlaylistNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(profileStorage.get("gabi@abv.bg").showPlaylist("Hello")).thenReturn("Say my name - Adel" + System.lineSeparator());

        String expected = "Say my name - Adel" + System.lineSeparator() + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(SHOW_PLAYLIST, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'show-playlist'");
    }

    @Test
    public void testShowPlaylistWhenNotExists() throws AccountNotFoundException, PlaylistNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(profileStorage.get("gabi@abv.bg").showPlaylist("Hello")).thenThrow(PlaylistNotFoundException.class);

        String expected = "A playlist with such a name does not exist." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(SHOW_PLAYLIST, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'show-playlist'");
    }

    @Test
    public void testPlayWithLessArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, PLAY, 2,
                PLAY + " <song_name> <artist>");
        String actual = cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'play'");
    }

    @Test
    public void testPlayWhenNotLoggedIn() {
        String expected = NOT_LOGGED_IN;
        String actual = cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'play'");
    }

    @Test
    public void testPlaySuccessfully() throws AccountNotFoundException, UnsupportedAudioFileException, IOException, SongNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.play("Hello", "Adel")).thenReturn("format,PCM_SIGNED,16,8000,1,4,16,true:songRepository\\audio.txt");

        String expected = "format,PCM_SIGNED,16,8000,1,4,16,true:songRepository\\audio.txt" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'play'");
    }

    @Test
    public void testPlayWhenSongNotExist() throws AccountNotFoundException, UnsupportedAudioFileException, IOException, SongNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.play("Hello", "Adel")).thenThrow(SongNotFoundException.class);

        String expected = "A song with such a name does not exist." + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'play'");
    }

    @Test
    public void testPlaySongForASecondTime() throws AccountNotFoundException, UnsupportedAudioFileException, IOException, SongNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.play("Hello", "Adel")).thenReturn("format,PCM_SIGNED,16,8000,1,4,16,true:songRepository\\audio.txt");
        cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        String expected = "You are already listening to a song" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        assertEquals(expected, actual, "Unexpected output for 'play'");
    }

    @Test
    public void testStopWithMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, STOP, 0,
                STOP);
        String actual = cmdExecutor.execute(key, new Command(STOP, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'stop'");
    }

    @Test
    public void testStopWhenNotLoggedIn() {
        String expected = NOT_LOGGED_IN;
        String actual = cmdExecutor.execute(key, new Command(STOP, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'stop'");
    }

    @Test
    public void testStopSuccessfully() throws AccountNotFoundException, UnsupportedAudioFileException, IOException, SongNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.play("Hello", "Adel")).thenReturn("format,PCM_SIGNED,16,8000,1,4,16,true:songRepository\\audio.txt");
        cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        String expected = "You stopped the song successfully!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(STOP, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'stop'");
    }

    @Test
    public void testStopNotPlayingSong() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = "You are not listening to a song" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(STOP, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'stop'");
    }

    @Test
    public void testDisconnectWithMoreArguments() {
        String expected = String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DISCONNECT, 0,
                DISCONNECT);
        String actual = cmdExecutor.execute(key, new Command(DISCONNECT, new String[]{"Hello"}));

        assertEquals(expected, actual, "Unexpected output for 'disconnect'");
    }

    @Test
    public void testDisconnectWhenNotLoggedIn() {
        String expected = "You have successfully disconnected!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(DISCONNECT, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'disconnect'");
    }

    @Test
    public void testDisconnectWhenLoggedIn() throws AccountNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));

        String expected = "You have successfully disconnected!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(DISCONNECT, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'disconnect'");
    }

    @Test
    public void testDisconnectWhenNotLoggedInAndPlayingSong() throws AccountNotFoundException, UnsupportedAudioFileException, IOException, SongNotFoundException {
        when(accountStorage.login("gabi@abv.bg", "12345678")).thenReturn(true);
        cmdExecutor.execute(key, new Command(LOGIN, new String[]{"gabi@abv.bg", "12345678"}));
        when(songStorage.play("Hello", "Adel")).thenReturn("format,PCM_SIGNED,16,8000,1,4,16,true:songRepository\\audio.txt");
        cmdExecutor.execute(key, new Command(PLAY, new String[]{"Hello", "Adel"}));

        String expected = "You have successfully disconnected!" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command(DISCONNECT, new String[]{}));

        assertEquals(expected, actual, "Unexpected output for 'stop'");
    }

    @Test
    public void testUnknownCommand() {
        String expected = "Unknown command" + System.lineSeparator();
        String actual = cmdExecutor.execute(key, new Command("unknown", new String[]{"command"}));

        assertEquals(expected, actual, "Unexpected output for unknown command");
    }
}