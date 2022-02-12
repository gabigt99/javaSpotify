package bg.sofia.uni.fmi.mjt.spotify.client;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6600;

    private boolean isPlaying;


    public static void main(String[] args) {
        Client chatClient = new Client();
        chatClient.start();
    }

    public void start() {
        try (Socket socketChannel = new Socket(SERVER_HOST, SERVER_PORT);
             InputStream serverInputStream = socketChannel.getInputStream();
             BufferedReader serverInputReader = new BufferedReader(new InputStreamReader(serverInputStream));
             PrintWriter serverOutputWriter = new PrintWriter(socketChannel.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server.");

            new Thread(() -> sendMessages(serverOutputWriter, scanner)).start();
            receiveMessages(serverInputStream, serverInputReader);
        } catch (IOException e) {
            System.out.println("Unable to connect to the server. Please try again later.");
        } catch (LineUnavailableException e) {
            System.out.println("There was an error playing the song.");
        }
    }

    private void receiveMessages(InputStream serverInputStream, BufferedReader reader) throws IOException, LineUnavailableException {
        while (true) {
            String message = reader.readLine();

            if (message.split(",")[0].equals("format")) {
                isPlaying = true;
                playSong(serverInputStream, SongFormat.of(message));
                getRidOfTheStayedGibberish(reader);
            }

            System.out.println(message);

            if (message.equals("You have successfully disconnected!")) {
                break;
            }

        }
    }

    private void sendMessages(PrintWriter writer, Scanner scanner) {
        System.out.print("Please, write a command: ");
        while (true) {
            String message = scanner.nextLine();
            writer.println(message);

            if (message.equals("stop")) {
                isPlaying = false;
            }

            if (message.equalsIgnoreCase("disconnect")) {
                break;
            }
        }
    }

    private void playSong(InputStream serverInputStream, SongFormat songFormat) throws LineUnavailableException, IOException {
        AudioFormat format = new AudioFormat(songFormat.encoding(), songFormat.sampleRate(), songFormat.sampleSizeInBits(),
                songFormat.channels(), songFormat.frameSize(), songFormat.frameRate(), songFormat.bigEndian());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
        dataLine.open();
        dataLine.start();
        byte[] bytesBuffer = new byte[songFormat.frameSize()];
        long size = songFormat.frameLength();
        long countFrames = 0;
        int bytesRead = -1;

        while (isPlaying && countFrames < size) {
            bytesRead = serverInputStream.read(bytesBuffer);
            dataLine.write(bytesBuffer, 0, bytesRead);
            System.out.println(bytesRead);
            countFrames++;
        }
        dataLine.flush();
        dataLine.drain();
        dataLine.close();
    }

    private void getRidOfTheStayedGibberish(BufferedReader reader) throws IOException {
        String message = reader.readLine();
        while (!message.contains("Stop song")) {
            message = reader.readLine();
        }
    }

}