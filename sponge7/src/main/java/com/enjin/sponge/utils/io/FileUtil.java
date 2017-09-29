package com.enjin.sponge.utils.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
    public static String readFile(File path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path.getAbsolutePath()));
        return new String(encoded, encoding);
    }
}
