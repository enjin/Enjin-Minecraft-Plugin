package com.enjin.officialplugin.yaml;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents a node in YAML paths
 * It stores node values and keeps references to children nodes
 *
 * @author Devil Boy
 */
public class YamlWriterNode {

    Map<String, YamlWriterNode> children;
    String value;

    /**
     * Constructs a new YamlWriterNode with no value or children
     */
    public YamlWriterNode() {
        this.children = new LinkedHashMap<String, YamlWriterNode>();
        this.value = "";
    }

    /**
     * Sets the value at the given path (creating children nodes as necessary)
     *
     * @param path  The path to set the value at
     * @param value The value to set at the given path
     */
    public void set(String path, Object value) {
        if (path.trim().equals("")) {
            this.value = value.toString();
        } else {
            String[] pathNodes = path.split("\\.", 2);

            YamlWriterNode lowerNode = children.get(pathNodes[0]);
            if (lowerNode == null) {
                children.put(pathNodes[0], lowerNode = new YamlWriterNode());
            }

            lowerNode.set((pathNodes.length == 1) ? "" : pathNodes[1], value);
        }
    }

    /**
     * Loads up nodes and values from the given config section
     *
     * @param section The section to retrieve data from
     */
    public void load(YamlConfigSection section) {
        for (String paths : section.paths.keySet()) {
            set(paths, section.getString(paths, ""));
        }
    }
}
