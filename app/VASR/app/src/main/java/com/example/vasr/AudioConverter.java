package com.example.vasr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class AudioConverter {
    static public void PCMToWAV(File input, File output, int channelCount, int sampleRate, int bitsPerSample) throws IOException {
        final int inputSize = (int) input.length();

        try (OutputStream encoded = new FileOutputStream(output)) {
            // WAVE RIFF header
            writeToOutput(encoded, "RIFF");
            writeToOutput(encoded, 36 + inputSize);
            writeToOutput(encoded, "WAVE");

            // SUB CHUNK 1 (FORMAT)
            writeToOutput(encoded, "fmt ");
            writeToOutput(encoded, 16);
            writeToOutput(encoded, (short) 1);
            writeToOutput(encoded, (short) channelCount);
            writeToOutput(encoded, sampleRate);
            writeToOutput(encoded, sampleRate * channelCount * bitsPerSample / 8);
            writeToOutput(encoded, (short) (channelCount * bitsPerSample / 8));
            writeToOutput(encoded, (short) bitsPerSample);

            // SUB CHUNK 2 (AUDIO DATA)
            writeToOutput(encoded, "data");
            writeToOutput(encoded, inputSize);
            copy(new FileInputStream(input), encoded);
        }
    }

    private static final int TRANSFER_BUFFER_SIZE = 10 * 1024;

    public static void writeToOutput(OutputStream output, String data) throws IOException {
        for (int i = 0; i < data.length(); i++)
            output.write(data.charAt(i));
    }

    public static void writeToOutput(OutputStream output, int data) throws IOException {
        output.write(data >> 0);
        output.write(data >> 8);
        output.write(data >> 16);
        output.write(data >> 24);
    }

    public static void writeToOutput(OutputStream output, short data) throws IOException {
        output.write(data >> 0);
        output.write(data >> 8);
    }

    public static long copy(InputStream source, OutputStream output) throws IOException {
        return copy(source, output, TRANSFER_BUFFER_SIZE);
    }

    public static long copy(InputStream source, OutputStream output, int bufferSize) throws IOException {
        long read = 0L;
        byte[] buffer = new byte[bufferSize];
        for (int n; (n = source.read(buffer)) != -1; read += n) {
            output.write(buffer, 0, n);
        }
        return read;
    }
}
