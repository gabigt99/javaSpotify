package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.account.Account;
import bg.sofia.uni.fmi.mjt.spotify.server.song.Song;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class MemorySaver {
    private static final Gson GSON = new Gson();
    private static final Gson GSON_PRETTY_PRINT = new GsonBuilder().setPrettyPrinting().create();

    private final String directory;
    private final Path accountsStore;
    private final Path songsStore;
    private final Path logStore;

    public MemorySaver(String directory, String accountsFile, String songsFile, String logFile) {
        this.directory = directory;
        accountsStore = Path.of(directory, accountsFile);
        songsStore = Path.of(directory, songsFile);
        logStore = Path.of(directory, logFile);
    }

    public void saveLog(Exception exception, String message, String moreInfo) {
        String log = "\n" + message + exception.toString() + "\n" + moreInfo + "\n";
        try (OutputStream os = Files.newOutputStream(logStore, CREATE, APPEND)) {
            os.write(log.getBytes());
        } catch (IOException ex) {
            System.out.println("\n\nProblem with the logs file.\n");
        }
    }

    public void saveAccount(Account account) {
        try (OutputStream os = Files.newOutputStream(accountsStore, CREATE, APPEND)) {
            os.write((GSON.toJson(account) + System.lineSeparator()).getBytes());
        } catch (IOException ex) {
            saveLog(ex, "Problem with saving the account " + account.getEmail() + ".\n", "");
        }
    }

    public void saveProfile(Profile profile) {
        Path path = getProfilePath(profile.getUsername());
        try (OutputStream os = Files.newOutputStream(path, CREATE)) {
            String profileGSONString = GSON.toJson(profile);
            JsonElement element = JsonParser.parseString(profileGSONString);
            String prettyJsonString = GSON_PRETTY_PRINT.toJson(element);
            os.write(prettyJsonString.getBytes());
        } catch (IOException ex) {
            saveLog(ex, "Problem with saving the profile " + profile.getUsername() + ".\n", "");
        }
    }

    public Map<String, Account> restoreAccounts() {
        Map<String, Account> accounts = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(accountsStore))) {
            accounts =  reader.lines().map(currentLine -> GSON.fromJson(currentLine, Account.class))
                    .collect(Collectors.toMap(Account::getEmail, Function.identity()));
        } catch (FileNotFoundException | NoSuchFileException ex) {
            saveLog(ex, "Unable to find the accounts file.\n", "");
        } catch (IOException ex) {
            saveLog(ex, "Problem with the accounts file.\n", "");
        }
        return accounts;
    }

    public Map<String, Profile> restoreProfiles(Set<String> usernames) {
        Map<String, Profile> profiles = new HashMap<>();
        for (String currentUser : usernames) {
            try (BufferedReader reader = new BufferedReader(
                    Files.newBufferedReader(getProfilePath(currentUser)))) {
                Profile profile = GSON.fromJson(reader, Profile.class);
                profiles.put(currentUser, profile);
            } catch (FileNotFoundException | NoSuchFileException e) {
                saveLog(e, "Unable to find " + currentUser + "'s file.\n", "");
            } catch (IOException e) {
                saveLog(e, "Problem with " + currentUser + "'s file.\n", "");
            }
        }
        return profiles;
    }

    public Map<List<String>, Song> restoreSongs() {
        Map<List<String>, Song> songs = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(Files.newBufferedReader(songsStore))) {
            songs =  reader.lines().map(currentLine -> GSON.fromJson(currentLine, Song.class))
                    .collect(Collectors.toMap(song -> List.of(song.getName(), song.getArtist()), Function.identity()));
        } catch (FileNotFoundException | NoSuchFileException ex) {
            saveLog(ex, "Unable to find the songs file.\n", "");
        } catch (IOException ex) {
            saveLog(ex, "Problem with the songs file.\n", "");
        }
        return songs;
    }

    private Path getProfilePath(String username) {
        return Path.of(directory, "profiles", username + ".txt");
    }
}
