package bg.sofia.uni.fmi.mjt.spotify.server.command;

import bg.sofia.uni.fmi.mjt.spotify.server.Storage;

import java.nio.channels.SelectionKey;

public class CommandExecutor {
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"" + System.lineSeparator();

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

    Storage storage;

    public CommandExecutor(Storage storage) {
        this.storage = storage;
    }

    public String execute(SelectionKey key, Command cmd) {
        return switch (cmd.command()) {
            case REGISTER -> register(key, cmd.arguments());
            case LOGIN -> login(key, cmd.arguments());
            case SEARCH -> search(key, cmd.arguments());
            case TOP -> top(key, cmd.arguments());
            case CREATE_PLAYLIST -> createPlaylist(key, cmd.arguments());
            case ADD_SONG_TO -> addSongTo(key, cmd.arguments());
            case SHOW_PLAYLIST -> showPlaylist(key, cmd.arguments());
            case PLAY -> play(key, cmd.arguments());
            case STOP -> stop(key, cmd.arguments());
            case DISCONNECT -> disconnect(key, cmd.arguments());
            default -> "Unknown command" + System.lineSeparator();
        };
    }

    private String register(SelectionKey key, String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2,
                    REGISTER + " <email> <password>");
        }
        String email = args[0];
        String password = args[1];

        return storage.registerUser(key, email, password) + System.lineSeparator();
    }

    private String login(SelectionKey key, String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, LOGIN, 2,
                    LOGIN + " <email> <password>");
        }
        String email = args[0];
        String password = args[1];

        return storage.login(key, email, password) + System.lineSeparator();
    }

    private String search(SelectionKey key, String[] args) {
        if (args.length < 1) {
            return String.format("Invalid count of arguments: search expects more than zero arguments. Example: \"%s\"" + System.lineSeparator(),
                    SEARCH + " <words>");
        }
        return storage.searchSongByWords(key, args) +System.lineSeparator();
    }

    private String top(SelectionKey key, String[] args) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, TOP, 1,
                    TOP + " <number>");
        }
        return storage.topNSongsByListening(key, args[0]) + System.lineSeparator();
    }

    private String createPlaylist(SelectionKey key, String[] args) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, CREATE_PLAYLIST, 1,
                    CREATE_PLAYLIST + " <name_of_the_playlist>");
        }
        String playlistName = args[0];

        return storage.createPlaylist(key, playlistName) + System.lineSeparator();
    }

    private String addSongTo(SelectionKey key, String[] args) {
        if (args.length != 3) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, ADD_SONG_TO, 3,
                    ADD_SONG_TO + " <name_of_the_playlist> <song_name> <artist>");
        }
        String playlistName = args[0];
        String songName = args[1];
        String artist = args[2];

        return storage.addSongToPlaylist(key, playlistName, songName, artist) + System.lineSeparator();
    }

    private String showPlaylist(SelectionKey key, String[] args) {
        if (args.length != 1) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, SHOW_PLAYLIST, 1,
                    SHOW_PLAYLIST + " <name_of_the_playlist>");
        }
        String playlistName = args[0];

        return storage.showPlaylist(key, playlistName) + System.lineSeparator();
    }

    private String play(SelectionKey key, String[] args) {
        if (args.length != 2) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, PLAY, 2,
                    PLAY + " <song_name> <artist>");
        }
        String songName = args[0];
        String artist = args[1];

        return storage.playSong(key, songName, artist) + System.lineSeparator();
    }

    private String stop(SelectionKey key, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, STOP, 0,
                    STOP);
        }

        return storage.stopSong(key) + System.lineSeparator();
    }

    private String disconnect(SelectionKey key, String[] args) {
        if (args.length != 0) {
            return String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, DISCONNECT, 0,
                    DISCONNECT);
        }

        return storage.disconnect(key) + System.lineSeparator();
    }
}
