package com.enjin.bukkit.util.io;

import java.io.*;

public class ReverseFileReader {
    private RandomAccessFile randomfile;
    private long position;

    public ReverseFileReader(String filename) throws Exception {
        this.randomfile = new RandomAccessFile(filename, "r");
        this.position = this.randomfile.length();
        this.randomfile.seek(this.position);
        //Move our pointer to the first valid position at the end of the file.
        String thisLine = this.randomfile.readLine();
        while (thisLine == null) {
            if (this.position < 0) {
                break;
            }

            this.position--;
            this.randomfile.seek(this.position);
            thisLine = this.randomfile.readLine();
            this.randomfile.seek(this.position);
        }
    }

    // Read one line from the current position towards the beginning
    public String readLine() throws Exception {
        int thisCode;
        char thisChar;
        String finalLine = "";

        // If our position is less than zero already, we are at the beginning
        // with nothing to return.
        if (this.position < 0) {
            return null;
        }

        while (true) {
            // we've reached the beginning of the file
            if (this.position < 0) {
                break;
            }
            // Seek to the current position
            this.randomfile.seek(this.position);

            // Read the data at this position
            thisCode = this.randomfile.readByte();
            thisChar = (char) thisCode;

            // If this is a line break or carrige return, stop looking
            if (thisCode == 13 || thisCode == 10) {
                // See if the previous character is also a line break character.
                // this accounts for crlf combinations
                this.randomfile.seek(this.position - 1);
                int nextCode = this.randomfile.readByte();
                if ((thisCode == 10 && nextCode == 13) || (thisCode == 13 && nextCode == 10)) {
                    // If we found another linebreak character, ignore it
                    this.position = this.position - 1;
                }
                // Move the pointer for the next readline
                this.position--;
                break;
            } else {
                // This is a valid character append to the string
                finalLine = thisChar + finalLine;
            }
            // Move to the next char
            this.position--;
        }
        // return the line
        return finalLine;
    }

    public void close() {
        try {
            randomfile.close();
        } catch (IOException ignored) {

        } catch (Exception ignored) {

        }
    }
}