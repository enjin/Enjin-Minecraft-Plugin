package com.enjin.bukkit.util.text;

public class TextBuilder {

    public static final String NEWLINE = "\n";

    private final StringBuilder builder;
    private int tabWidth = 4;
    private int indentLevel = 0;
    private int borderWidth = 40;
    private char borderChar = '=';
    private LineState lineState = LineState.NEWLINE;

    public TextBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public TextBuilder() {
        this(new StringBuilder());
    }

    public TextBuilder append(String str) {
        applyIndentLevel();
        builder.append(str);
        return this;
    }

    public TextBuilder append(char c) {
        applyIndentLevel();
        builder.append(c);
        return this;
    }

    public TextBuilder append(boolean b) {
        applyIndentLevel();
        builder.append(b);
        return this;
    }

    public TextBuilder append(int i) {
        applyIndentLevel();
        builder.append(i);
        return this;
    }

    public TextBuilder append(long l) {
        applyIndentLevel();
        builder.append(l);
        return this;
    }

    public TextBuilder append(float f) {
        applyIndentLevel();
        builder.append(f);
        return this;
    }

    public TextBuilder append(double d) {
        applyIndentLevel();
        builder.append(d);
        return this;
    }

    public TextBuilder newLine() {
        builder.append(NEWLINE);
        lineState = LineState.NEWLINE;
        return this;
    }

    public TextBuilder indent(int count, boolean useTabWidth) {
        if (count == 0)
            return this;
        if (useTabWidth)
            count *= tabWidth;
        return repeat(' ', count);
    }

    public TextBuilder indent(int count) {
        return indent(count, false);
    }

    public TextBuilder incrementIndentLevel() {
        indentLevel++;
        return this;
    }

    public TextBuilder decrementIndentLevel() {
        indentLevel = Math.max(0, indentLevel - 1);
        return this;
    }

    public TextBuilder resetIndentLevel() {
        indentLevel = 0;
        return this;
    }

    public TextBuilder repeat(char c, int count) {
        lineState = LineState.EDITING;
        for (int i = 0; i < count; i++)
            append(c);
        return this;
    }

    public TextBuilder header(BorderOptions options, String... lines) {
        if (options.top)
            border();
        for (String line : lines)
            indent(calculateHeaderIndent(line)).append(line).newLine();
        if (options.bottom)
            border();
        return this;
    }

    public TextBuilder header(String... lines) {
        return header(BorderOptions.BOTH, lines);
    }

    public TextBuilder border() {
        repeat(borderChar, borderWidth).newLine();
        return this;
    }

    public TextBuilder setTabWidth(int tabWidth) {
        this.tabWidth = Math.max(1, tabWidth);
        return this;
    }

    public TextBuilder setBorderChar(char borderChar) {
        this.borderChar = borderChar;
        return this;
    }

    public TextBuilder setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        return this;
    }

    private int calculateHeaderIndent(String line) {
        if (line.length() > borderWidth)
            return line.length();
        return (borderWidth - line.length()) / 2;
    }

    private void applyIndentLevel() {
        if (lineState != LineState.NEWLINE)
            return;
        indent(indentLevel, true);
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public enum LineState {
        NEWLINE,
        EDITING
    }

    public enum BorderOptions {
        TOP(true, false),
        BOTTOM(false, true),
        BOTH(true, true);

        protected boolean top;
        protected boolean bottom;

        BorderOptions(boolean top, boolean bottom) {
            this.top = top;
            this.bottom = bottom;
        }
    }
}
