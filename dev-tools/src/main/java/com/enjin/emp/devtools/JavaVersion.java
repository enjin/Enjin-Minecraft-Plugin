package com.enjin.emp.devtools;

import java.util.HashMap;
import java.util.Map;

public enum JavaVersion {

    UNKNOWN(-1),
    JAVA_5(49),
    JAVA_6(50),
    JAVA_7(51),
    JAVA_8(52),
    JAVA_9(53),
    JAVA_10(54),
    JAVA_11(55);

    private final int version;

    JavaVersion(int version) {
        this.version = version;
    }

    private static final Map<Integer, JavaVersion> byVersion = new HashMap<>();

    static {
        for (JavaVersion version : values()) {
            byVersion.put(version.version, version);
        }
    }

    public static JavaVersion getByVersion(int version) {
        return byVersion.containsKey(version) ? byVersion.get(version) : JavaVersion.UNKNOWN;
    }

    public static JavaVersion getCurrentVersion() {
        return getByVersion((int) Float.parseFloat(System.getProperty("java.class.version")));
    }

    public static String printVersions(int[] versions) {
        StringBuilder sb = new StringBuilder();

        sb.append('[');
        for (int version : versions) {
            JavaVersion found = getByVersion(version);
            sb.append(found == UNKNOWN ? version : found);
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(']');

        return sb.toString();
    }

}
