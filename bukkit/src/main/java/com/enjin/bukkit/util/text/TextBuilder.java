package com.enjin.bukkit.util.text;

public class TextBuilder {

    public static final String NEWLINE = "\n";

    private final StringBuilder builder;
    private int tabWidth = 4;

    public TextBuilder(StringBuilder builder) {
        this.builder = builder;
    }

    public TextBuilder() {
        this(new StringBuilder());
    }

    public TextBuilder append(String str) {
        builder.append(str);
        return this;
    }

    public TextBuilder append(char c) {
        builder.append(c);
        return this;
    }

    public TextBuilder append(boolean b) {
        builder.append(b);
        return this;
    }

    public TextBuilder append(int i) {
        builder.append(i);
        return this;
    }

    public TextBuilder newLine() {
        return append(NEWLINE);
    }

    public TextBuilder indent(int count, boolean useTabWidth) {
        if (useTabWidth)
            count *= tabWidth;

        return repeat(' ', count);
    }

    public TextBuilder indent(int count) {
        return indent(count, false);
    }

    public TextBuilder repeat(char c, int count) {
        for (int i = 0; i < count; i++)
            append(c);

        return this;
    }

    public TextBuilder withTabWidth(int tabWidth) {
        this.tabWidth = tabWidth;
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
