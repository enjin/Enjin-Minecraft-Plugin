package com.enjin.officialplugin.yaml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

/**
 * This class is used to write out a tree-structured database to a YAML-formatted file
 *
 * @author Devil Boy
 */
public class YamlConfigWriter extends YamlWriterNode {

    String indent;

    /**
     * Constructs a new YamlConfigWriter with no data
     */
    public YamlConfigWriter() {
        super();

        this.value = null;
        this.indent = "  ";
    }

    /**
     * Saves this node tree to file (will overwrite if the file exists)
     *
     * @param file The file to write to
     * @throws IOException Thrown if there is an issue while writing to the file
     */
    public void save(File file) throws IOException {
        if (file == null) throw new IllegalArgumentException();

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));

            for (Map.Entry<String, YamlWriterNode> child : children.entrySet()) {
                writeValues(out, child.getValue(), child.getKey(), 0);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes a node's and its children's values to file
     *
     * @param writer   The Writer to use
     * @param node     The node whose data to write
     * @param nodeName The name of the node whose data is being written
     * @param depth    The depth of indentation
     * @throws IOException Thrown if there is an error in the write
     */
    private void writeValues(Writer writer, YamlWriterNode node, String nodeName, int depth) throws IOException {
        writer.write(getIndent(depth) + nodeName + ": " + node.value + "\r\n");
        for (Map.Entry<String, YamlWriterNode> child : node.children.entrySet()) {
            writeValues(writer, child.getValue(), child.getKey(), depth + 1);
        }
    }

    /**
     * Generates a String for proper-length indentation
     *
     * @param depth The size of the indent to create
     * @return A String consisting of space characters
     */
    private String getIndent(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append(indent);
        }
        return sb.toString();
    }
}
