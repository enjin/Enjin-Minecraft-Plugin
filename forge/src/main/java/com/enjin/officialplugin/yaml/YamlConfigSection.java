package com.enjin.officialplugin.yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class was made to efficiently read and parse YAML configuration files
 * Due to how it was designed, this class does not allow for modification of values
 *
 * @author Devil Boy
 */
public class YamlConfigSection {

    List<String> lines;
    int indL;
    String indS;
    Map<String, Integer> paths;

    /**
     * This constructs an empty YamlConfigSection with no data
     */
    public YamlConfigSection() {
        this.lines = new ArrayList<String>();
        this.paths = new HashMap<String, Integer>();
    }

    /**
     * This is a private constructor for the creation of "lower" config sections
     *
     * @param lines The lines loaded from file
     * @param paths The paths to relevant data/values
     */
    private YamlConfigSection(List<String> lines, Map<String, Integer> paths) {
        this.lines = lines;
        this.paths = paths;
    }

    /**
     * Attempts to load the given file into memory
     *
     * @param file The file to load
     * @throws FileNotFoundException             Thrown if the file could not be found
     * @throws IOException                       Thrown if there was an issue while reading the file
     * @throws InvalidYamlConfigurationException Thrown if an error was detected in the formatting of the file
     */
    public void load(File file) throws FileNotFoundException, IOException, InvalidYamlConfigurationException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));

            String line = null;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (br != null) {
                br.close();
            }
        }

        checkIndent();
        getPaths();
    }

    /**
     * Detects the indentation size used in the file and then checks to make sure the rest of the file complies
     *
     * @throws InvalidYamlConfigurationException Thrown if the indentation in the file is inconsistent
     */
    public void checkIndent() throws InvalidYamlConfigurationException {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().equals("")) continue;

            if (indL == 0) { // Detect the indentation used
                if (line.startsWith(" ")) {
                    indL = YamlUtil.getLeadingSpaces(line);
                    indS = YamlUtil.emptyString(indL);
                }
            } else { // Make sure it's consistent
                if (YamlUtil.getLeadingSpaces(line) % indL != 0) {
                    throw new InvalidYamlConfigurationException("Inconsistent indentation on line " + i);
                }
            }
        }
    }

    /**
     * Loads all the paths along with their line numbers into memory
     *
     * @throws InvalidYamlConfigurationException Thrown if a formatting error was detected
     */
    public void getPaths() throws InvalidYamlConfigurationException {
        int depth = 0;
        LinkedList<String> path = new LinkedList<String>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().equals("")) continue;

            int currentDepth = YamlUtil.getLeadingSpaces(line) / indL;
            String node = line.split(":", 2)[0].trim();

            if (path.isEmpty()) {
                path.add(node);
            } else {
                if (currentDepth == depth) {
                    path.set(path.size() - 1, node);
                } else if (currentDepth > depth) {
                    path.add(node);
                } else if (currentDepth < depth) {
                    for (int j = 0; j < depth - currentDepth; j++) {
                        path.removeLast();
                    }

                    if (path.isEmpty()) {
                        throw new InvalidYamlConfigurationException("The indentation at line " + i + " is less than the starting indentation");
                    } else {
                        path.set(path.size() - 1, node);
                    }
                }
            }

            depth = currentDepth;
            paths.put(YamlUtil.listToPath(path), i);
        }
    }

    /**
     * Gets a "lower" configuration section
     *
     * @param path The path of the section
     * @return A configuration section (likely holding some of the values of this one)
     */
    public YamlConfigSection getConfigSection(String path) {
        HashMap<String, Integer> newPaths = new HashMap<String, Integer>();

        for (Map.Entry<String, Integer> pathEntry : paths.entrySet()) {
            if (pathEntry.getKey().startsWith(path + ".")) {
                newPaths.put(pathEntry.getKey().substring(path.length() + 1), pathEntry.getValue());
            }
        }

        return new YamlConfigSection(lines, newPaths);
    }

    /**
     * Gets a map containing paths and values in this configuration section
     *
     * @param deep Whether or not to include children of "lower" configuration sections
     * @return A map with paths and values from this section
     */
    public Map<String, Object> getValues(boolean deep) {
        HashMap<String, Object> map = new HashMap<String, Object>();

        for (Map.Entry<String, Integer> pathEntry : paths.entrySet()) {
            if (deep || !pathEntry.getKey().contains(".")) {
                map.put(pathEntry.getKey(), getString(pathEntry.getKey(), ""));
            }
        }

        return map;
    }

    /**
     * Gets a boolean value from the specified path
     *
     * @param path The path to the value
     * @param def  The default value to use if there is an error or the path is not found
     * @return The value at the path or the given default
     */
    public boolean getBoolean(String path, boolean def) {
        Integer lineNum = paths.get(path);
        if (lineNum == null) {
            return def;
        } else {
            try {
                return Boolean.parseBoolean(lines.get(lineNum).split(":", 2)[1].trim());
            } catch (Exception e) { // Can throw ArrayIndexOutOfBoundsException
                return def;
            }
        }
    }

    /**
     * Gets an integer value from the specified path
     *
     * @param path The path to the value
     * @param def  The default value to use if there is an error or the path is not found
     * @return The value at the path or the given default
     */
    public int getInt(String path, int def) {
        Integer lineNum = paths.get(path);
        if (lineNum == null) {
            return def;
        } else {
            try {
                return Integer.parseInt(lines.get(lineNum).split(":", 2)[1].trim());
            } catch (Exception e) { // Can throw ArrayIndexOutOfBoundsException or NumberFormatException
                return def;
            }
        }
    }

    /**
     * Gets a String value from the specified path
     *
     * @param path The path to the value
     * @param def  The default value to use if there is an error or the path is not found
     * @return The value at the path or a the given default
     */
    public String getString(String path, String def) {
        Integer lineNum = paths.get(path);
        if (lineNum == null) {
            return def;
        } else {
            try {
                return lines.get(lineNum).split(":", 2)[1].trim();
            } catch (Exception e) { // Can throw ArrayIndexOutOfBoundsException or NumberFormatException
                return def;
            }
        }
    }
}
