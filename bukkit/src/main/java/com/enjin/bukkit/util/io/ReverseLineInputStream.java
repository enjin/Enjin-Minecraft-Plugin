package com.enjin.bukkit.util.io;

import java.io.*;

public class ReverseLineInputStream extends InputStream {

    private RandomAccessFile in;

    private long currentLineStart = -1;
    private long currentLineEnd = -1;
    private long currentPos = -1;
    private long lastPosInFile = -1;

    public ReverseLineInputStream(File file) throws FileNotFoundException {
        in = new RandomAccessFile(file, "r");
        currentLineStart = file.length();
        currentLineEnd = file.length();
        lastPosInFile = file.length() - 1;
        currentPos = currentLineEnd;
    }

    public void findPreviousLine() throws IOException {
        currentLineEnd = currentLineStart;

        // There are no more lines, since we are at the beginning of the file and no lines.
        if (currentLineEnd == 0) {
            currentLineEnd = -1;
            currentLineStart = -1;
            currentPos = -1;
            return;
        }

        long filePointer = currentLineStart - 1;

        while (true) {
            filePointer--;

            // we are at start of file so this is the first line in the file.
            if (filePointer < 0) {
                break;
            }

            in.seek(filePointer);
            int readByte = in.readByte();

            // We ignore last LF in file. search back to find the previous LF.
            if (readByte == 0xA && filePointer != lastPosInFile) {
                break;
            }
        }

        currentLineStart = filePointer + 1;
        currentPos = currentLineStart;
    }

    @Override
    public int read() throws IOException {
        if (currentPos < currentLineEnd) {
            in.seek(currentPos++);
            int readByte = in.readByte();
            return readByte;
        } else if (currentPos < 0) {
            return -1;
        } else {
            findPreviousLine();
            return read();
        }
    }

}
