package bg.sofia.uni.fmi.mjt.spotify.client;

import javax.sound.sampled.AudioFormat;

public record SongFormat(AudioFormat.Encoding encoding, float sampleRate,
        int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, long frameLength) {

    private static final int SAMPLE_RATE = 1;
    private static final int SAMPLE_SIZE_IN_BITS =2;
    private static final int CHANNELS = 3;
    private static final int FRAME_SIZE = 4;
    private static final int FRAME_RATE = 5;
    private static final int BIG_ENDIAN = 6;
    private static final int FRAME_LENGTH = 7;
    public static SongFormat of(String line) {
        final String[] format = line.replace(System.lineSeparator(), "").split(",");
        return new SongFormat(AudioFormat.Encoding.PCM_SIGNED, Float.parseFloat(format[SAMPLE_RATE]),
                Integer.parseInt(format[SAMPLE_SIZE_IN_BITS]), Integer.parseInt(format[CHANNELS]),
                Integer.parseInt(format[FRAME_SIZE]), Float.parseFloat(format[FRAME_RATE]),
                Boolean.parseBoolean(format[BIG_ENDIAN]), Long.parseLong(format[FRAME_LENGTH]));
    }
}
