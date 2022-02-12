package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.account.Account;
import bg.sofia.uni.fmi.mjt.spotify.server.account.AccountStorage;
import bg.sofia.uni.fmi.mjt.spotify.server.exception.*;
import bg.sofia.uni.fmi.mjt.spotify.server.song.SongStorage;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Storage {
    private static final String NOT_LOGGED_IN = "You must be logged in to execute this command!";
    private static final int SONG_NAME = 0;
    private static final int ARTIST = 1;

    private final MemorySaver memoryManager;
    private AccountStorage accountStorage;
    private Map<String, Profile> profileStorage;
    private SongStorage songStorage;
    private Map<SelectionKey, String> activeUsers = new HashMap<>();
    private Map<String, List<String>> playedSongs = new HashMap<>();

    public Storage(String directory, String accountsFile, String songFile, String logFile) {
        memoryManager = new MemorySaver(directory, accountsFile, songFile, logFile);
    }

    public Storage(MemorySaver memoryManager, AccountStorage accountStorage, Map<String, Profile> profileStorage, SongStorage songStorage) {
        this.memoryManager = memoryManager;
        this.accountStorage = accountStorage;
        this.profileStorage = profileStorage;
        this.songStorage = songStorage;
    }

    public String disconnect(SelectionKey key) {
        Profile profile = getActiveProfileByKey(key);
        if (profile != null) {
            activeUsers.remove(key);
        }
        if (profile != null && playedSongs.get(profile.getUsername()) != null) {
            stopSong(key);
        }
        return "You have successfully disconnected!";
    }

    public String registerUser(SelectionKey key, String email, String password) {
        if (getActiveProfileByKey(key) != null) {
            return "You are logged in. You cannot register.";
        }
        try {
            accountStorage.register(email, password);
            addNewProfile(accountStorage.getAccountByUsername(email), new Profile(email));
        } catch (AccountAlreadyExistsException e) {
            return "A user with such an email already exists.";
        } catch (IllegalArgumentException e) {
            return "Wrong email format!";
        }
        return "You have successfully registered!";
    }

    public String login(SelectionKey key, String email, String password) {
        if (isUserLoggedIn(key, email)) {
            return "You are logged in. You cannot do it again.";
        }
        try {
            if (accountStorage.login(email, password)) {
                activeUsers.put(key,email);
            } else {
                return "Wrong password. Please, try again";
            }

        } catch (AccountNotFoundException e) {
            return "Such an account does not exist. You can register.";
        }
        return "You have successfully logged in";
    }

    public String searchSongByWords(SelectionKey key, String[] words) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }
        return songStorage.search(words);
    }

    public String topNSongsByListening(SelectionKey key, String number) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }

        int transformedNumber = 0;
        String answer;

        try {
            transformedNumber = Integer.parseInt(number);
            answer = songStorage.getTopNListenedSongsAtTheMoment(transformedNumber);
        } catch (NumberFormatException e) {
            return "You must enter a number!";
        } catch (IllegalArgumentException e) {
            return "The number must not be negative!";
        }
        return answer;
    }

    public String createPlaylist(SelectionKey key, String playlistName) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }

        Profile profile = getActiveProfileByKey(key);
        try {
            profile.addPlaylist(playlistName);
            memoryManager.saveProfile(profile);
        } catch (PlaylistAlreadyExistsException e) {
            return "A playlist with such a name already exists.";
        } catch (IllegalArgumentException e) {
            return "The name cannot be blank.";
        }
        return String.format("A playlist with a name %s created successfully!", playlistName);

    }

    public String addSongToPlaylist(SelectionKey key, String playlistName, String songName, String artist) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }

        Profile profile = getActiveProfileByKey(key);
        try {
            if (songStorage.isExists(songName, artist)) {
                profile.addSong(playlistName, songName, artist);
                memoryManager.saveProfile(profile);
            } else {
                return "A song with such a name does not exist.";
            }
        } catch (PlaylistNotFoundException e) {
            return "A playlist with such a name does not exist.";
        } catch (SongAlreadyExistsException e) {
            return String.format("A song is already added in playlist %s. Cannot be added a second time.", playlistName);
        }
        return String.format("You have successfully added new song in playlist %s!", playlistName);
    }

    public String showPlaylist(SelectionKey key, String playlistName) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }

        Profile profile = getActiveProfileByKey(key);
        String playlistContent;
        try {
            playlistContent = profile.showPlaylist(playlistName);
        } catch (PlaylistNotFoundException e) {
            return "A playlist with such a name does not exist.";
        }
        return playlistContent;
    }

    public String playSong(SelectionKey key, String songName, String artist) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }
        if (getActiveProfileByKey(key) != null &&
                playedSongs.get(getActiveProfileByKey(key).getUsername()) != null) {
            return "You are already listening to a song";
        }

        String formatOfData;
        try {
            formatOfData = songStorage.play(songName, artist);
            playedSongs.put(getActiveProfileByKey(key).getUsername(), List.of(songName, artist));
        } catch (SongNotFoundException e) {
            return "A song with such a name does not exist.";
        } catch (UnsupportedAudioFileException e) {
            storeLog(e, String.format("An error occurred while taking song format with name %s: " + e.getMessage() + System.lineSeparator(), songName), "");
            return "An error occurred with the song. Sorry for the inconvenience.";
        } catch (IOException e) {
            storeLog(e, String.format("An error occurred while taking song format with name %s: " + e.getMessage() + System.lineSeparator(), songName), "");
            return "An error occurred with the song. Sorry for the inconvenience.";
        }

        return formatOfData;
    }

    public String stopSong(SelectionKey key) {
        if (getActiveProfileByKey(key) == null) {
            return NOT_LOGGED_IN;
        }
        List<String> song = playedSongs.get(getActiveProfileByKey(key).getUsername());
        if (song == null) {
            return "You are not listening to a song";
        }
        try {
            songStorage.stop(song.get(SONG_NAME), song.get(ARTIST));
            playedSongs.remove(getActiveProfileByKey(key).getUsername());
        } catch (SongNotFoundException e) {
            storeLog(e, "A not existing song was played.", Arrays.toString(e.getStackTrace()));
            return String.format("A song with a name %s does not exist.", song.get(SONG_NAME));
        }

        return "You stopped the song successfully!";
    }

    public void addNewProfile(Account account, Profile profile) {
        profileStorage.put(profile.getUsername(), profile);
        memoryManager.saveAccount(account);
        memoryManager.saveProfile(profile);
    }



    public void restoreData() {
        accountStorage = new AccountStorage(memoryManager.restoreAccounts());
        profileStorage = memoryManager.restoreProfiles(accountStorage.getUsernames());
        songStorage = new SongStorage(memoryManager.restoreSongs());
    }

    public void storeLog(Exception exception, String message, String moreInfo) {
        System.out.println(message);
        memoryManager.saveLog(exception, message, moreInfo);
    }

    private Profile getActiveProfileByKey(SelectionKey key) {
        if (activeUsers.get(key) == null) {
            return null;
        }
        return profileStorage.get(activeUsers.get(key));
    }

    private boolean isUserLoggedIn(SelectionKey key, String username) {
        return getActiveProfileByKey(key) != null ||
                activeUsers.values().stream().filter(username::equals).count() != 0;
    }

}
