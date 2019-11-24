package com.enjin.bukkit.util.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    public static String readFile(File path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path.getAbsolutePath()));
        return new String(encoded, encoding);
    }

    public static void write(File dest, String contents) throws IOException {
        Files.createDirectories(dest.getParentFile().toPath());
        Files.write(dest.toPath(), contents.getBytes());
    }

}
