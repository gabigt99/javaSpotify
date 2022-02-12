package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.spotify.server.command.CommandExecutor;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Server {
    private static final int BUFFER_SIZE = 8192;
    private static final int SONG_PATH = 0;
    private static final int SONG_FORMAT = 1;
    private static final String HOST = "localhost";
    private static final String directory = "repository";
    private static final String accountsFile = "accounts.txt";
    private static final String songsFile = "songs.txt";
    private static final String logFile = "logs.txt";

    private final Storage storage = new Storage(directory, accountsFile, songsFile, logFile);
    private final CommandExecutor commandExecutor = new CommandExecutor(storage);
    private Map<SelectionKey, Boolean> isStoppedSong = new HashMap<>();

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

            storage.restoreData();

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);
                            if (clientInput == null) {
                                continue;
                            }
                            System.out.println(clientInput);
                            if (isStoppedSong.get(key) != null && clientInput.equals("stop" + System.lineSeparator())) {
                                isStoppedSong.put(key, true);
                            }

                            String output = commandExecutor.execute(key, CommandCreator.newCommand(clientInput));
                            if (isCommandPlay(key, output)) {
                                output = "Start song" + System.lineSeparator();
                            }
                            writeClientOutput(clientChannel, output);

                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {
                    storage.storeLog(e, "Error occurred while processing client request.",
                            Arrays.toString(e.getStackTrace()));
                }
            }
        } catch (IOException e) {
            storage.storeLog(e, "Failed to start server.",
                    Arrays.toString(e.getStackTrace()));
        }
    }

    public boolean isCommandPlay(SelectionKey key, String output) {
        if (output.contains("+")) {
            playSong(key, output);
            return true;
        }
        return false;
    }

    public void playSong(SelectionKey key, String songInfo) {
        try {
            String[] song = songInfo.split("\\+");
            SocketChannel clientChannel = (SocketChannel) key.channel();
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(song[SONG_PATH]));
            writeClientOutput(clientChannel, getAllSongInfo(song[SONG_FORMAT], audioStream));
            byte[] bytesBuffer = new byte[audioStream.getFormat().getFrameSize()];

            isStoppedSong.put(key, false);
            while(!isStoppedSong.get(key) && audioStream.read(bytesBuffer) != -1) {
                writeAudioOutput(clientChannel, bytesBuffer);
            }
            audioStream.close();
            if (!isStoppedSong.get(key)) {
                storage.stopSong(key);
            }
            writeClientOutput(clientChannel, "Stop song");
        } catch (UnsupportedAudioFileException e) {
            storage.storeLog(e, "An error occurred while playing the song" + System.lineSeparator(),
                    Arrays.toString(e.getStackTrace()));
            storage.stopSong(key);
        } catch (IOException e) {
            storage.storeLog(e, "An error occurred while playing the song" + System.lineSeparator(),
                    Arrays.toString(e.getStackTrace()));
            storage.stopSong(key);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeAudioOutput(SocketChannel clientChannel, byte[] data) throws IOException {
        buffer.clear();
        buffer.put(data);
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private String getAllSongInfo(String songsFormat, AudioInputStream audioInputStream) {
        return songsFormat.replace(System.lineSeparator(),
                "," + audioInputStream.getFrameLength() + System.lineSeparator());
    }
}