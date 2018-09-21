package com.enjin.emp.devtools;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ObjectArrays;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Application {

    public static final boolean IS_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    public static final File CWD = new File(".");
    public static final boolean autocrlf = !"\n".equals(System.getProperty("line.separator"));
    private static File msysDir;
    private static String mvn;

    public static void main(String... args) throws Exception {
        if (CWD.getAbsolutePath().contains("'") || CWD.getAbsolutePath().contains("#")) {
            System.err.println("Please do not run in a path with special characters!");
            return;
        }

        try {
            runProcess(CWD, "sh", "-c", "exit");
        } catch (Exception ex) {
            if (IS_WINDOWS) {
                String gitVersion = "PortableGit-2.15.0-" + (System.getProperty("os.arch").endsWith("64") ? "64" : "32") + "-bit";
                msysDir = new File(gitVersion, "PortableGit");

                if (!msysDir.isDirectory()) {
                    System.out.println("*** Could not find PortableGit installation, downloading. ***");

                    String gitName = gitVersion + ".7z.exe";
                    File gitInstall = new File(gitVersion, gitName);
                    gitInstall.getParentFile().mkdirs();

                    if (!gitInstall.exists()) {
                        download("https://static.spigotmc.org/git/" + gitName, gitInstall);
                    }

                    System.out.println("Extracting downloaded git install");

                    runProcess(gitInstall.getParentFile(), gitInstall.getAbsolutePath(), "-y", "-gm2", "-nr");

                    gitInstall.delete();
                }

                System.out.println("*** Using downloaded git " + msysDir + " ***");
                System.out.println("*** Please note that this is a beta feature, so if it does not work please also try a manual install of git from https://git-for-windows.github.io/ ***");
            } else {
                System.out.println("You must run this jar through bash (msysgit)");
                System.exit(1);
            }
        }

        runProcess(CWD, "git", "--version");

        // Validate Maven installation
        File maven;
        String m2Home = System.getenv("M2_HOME");
        if (m2Home == null || !(maven = new File(m2Home)).exists()) {
            maven = new File("apache-maven-3.5.0");

            if (!maven.exists()) {
                // Download Maven 3.5.0 from SpigotMC
                System.out.println("Maven does not exist, downloading. Please wait.");

                File mvnTemp = new File("mvn.zip");
                mvnTemp.deleteOnExit();

                download("https://static.spigotmc.org/maven/apache-maven-3.5.0-bin.zip", mvnTemp);
                unzip(mvnTemp, new File("."));
            }
        }

        mvn = maven.getAbsolutePath() + "/bin/mvn";

        // Download and install jars to local maven repository manually
        downloadAndInstallJar("https://dev.bukkit.org/projects/zpermissions/files/787619/download",
                "org.tyrannyofheaven.bukkit", "zPermissions", "1.3beta1");
        downloadAndInstallJar("https://dev.bukkit.org/projects/bpermissions/files/941243/download",
                "de.banaco", "bPermissions-Bukkit", "2.12.1");
        downloadAndInstallJar("https://dev.bukkit.org/projects/vanish/files/2597365/download",
                "org.kitteh", "VanishNoPacket", "3.20.1");
        downloadAndInstallJar("https://dev.bukkit.org/projects/tuxtwolib/files/2431867/download",
                "Tux2", "TuxTwoLib", "1.12-b8");
        downloadAndInstallJar("https://dev.bukkit.org/projects/permissionsex/files/909154/download",
                "ru.tehkode", "PermissionsEx", "1.23.4");
        downloadAndInstallJar("https://dev.bukkit.org/projects/permbukkit/files/911279/download",
                "com.platymuus", "bukkit-permissions", "2.5");
        downloadAndInstallJar("https://popicraft.net/jenkins/job/mcMMO/16/artifact/mcMMO/target/mcMMO.jar",
                "com.gmail.nossr50.mcMMO", "mcMMO", "1.5.10");
    }

    public static int runProcess(File workDir, String... command) throws Exception {
        if (msysDir != null) {
            if ("bash".equals(command[0])) {
                command[0] = "git-bash";
            }

            String[] shim = new String[] { "cmd.exe", "/C" };
            command = ObjectArrays.concat(shim, command, String.class);
        }

        return runProcess0(workDir, command);
    }

    private static int runProcess0(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));

        if (!pb.environment().containsKey("MAVEN_OPTS")) {
            pb.environment().put("MAVEN_OPTS", "-Xmx1024M");
        }

        if (msysDir != null) {
            String pathEnv = null;

            for (String key : pb.environment().keySet()) {
                if (key.equalsIgnoreCase("path")) {
                    pathEnv = key;
                }
            }

            if (pathEnv == null) {
                throw new IllegalStateException("Could not find path variable!");
            }

            String path = pb.environment().get(pathEnv);
            path += ";" + msysDir.getAbsolutePath();
            path += ";" + new File(msysDir, "bin").getAbsolutePath();
            pb.environment().put(pathEnv, path);
        }

        final Process ps = pb.start();

        new Thread(new StreamRedirector(ps.getInputStream(), System.out)).start();
        new Thread(new StreamRedirector(ps.getErrorStream(), System.err)).start();

        int status = ps.waitFor();

        if (status != 0) {
            throw new RuntimeException("Error running command, return status != 0: " + Arrays.toString(command));
        }

        return status;
    }

    public static File download(String url, File target) throws IOException {
        System.out.println("Starting download of " + url);

        byte[] bytes = Resources.toByteArray(new URL(url));

        System.out.println("Downloaded file: " + target + " with md5: " + Hashing.md5().hashBytes(bytes).toString());

        Files.write(bytes, target);

        return target;
    }

    public static void unzip(File zipFile, File targetFolder) throws IOException {
        unzip(zipFile, targetFolder, null);
    }

    public static void unzip(File zipFile, File targetFolder, Predicate<String> filter) throws IOException {
        targetFolder.mkdir();

        try (ZipFile zip = new ZipFile(zipFile)) {
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();

                if (filter != null) {
                    if (!filter.apply(entry.getName())) {
                        continue;
                    }
                }

                File outFile = new File(targetFolder, entry.getName());

                if (entry.isDirectory()) {
                    outFile.mkdirs();
                    continue;
                }

                if (outFile.getParentFile() != null) {
                    outFile.getParentFile().mkdirs();
                }

                try (InputStream is = zip.getInputStream(entry)) {
                    try (OutputStream os = new FileOutputStream(outFile)) {
                        ByteStreams.copy(is, os);
                    }
                }

                System.out.println("Extracted: " + outFile);
            }
        }
    }

    public static void install(File artifact, String groupId, String artifactId, String version, String packaging) throws Exception {
        runProcess(CWD,
                "sh", mvn, "install:install-file",
                "-Dfile=" + artifact,
                "-Dpackaging=" + packaging,
                "-DgroupId=" + groupId,
                "-DartifactId=" + artifactId,
                "-Dversion=" + version);
    }

    public static void downloadAndInstallJar(String url, String groupId, String artifactId, String version) throws Exception {
        String pluginsName = "./jars/";
        File pluginsDir = new File(pluginsName);

        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
        }

        String targetName = artifactId + "-" + version + ".jar";
        File targetFile = new File(pluginsDir, targetName);

        if (!targetFile.exists()) {
            download(url, targetFile);
        }

        install(targetFile, groupId, artifactId, version, "jar");
    }

    @RequiredArgsConstructor
    private static class StreamRedirector implements Runnable {

        private final InputStream in;
        private final PrintStream out;

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.println(line);
                }
            } catch (IOException ex) {
                throw Throwables.propagate(ex);
            }
        }
    }

}
