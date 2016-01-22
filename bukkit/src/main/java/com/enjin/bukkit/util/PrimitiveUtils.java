package com.enjin.bukkit.util;

public class PrimitiveUtils {
    public static boolean getBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean) obj).booleanValue();
        } else if (obj instanceof String) {
            return (((String) obj).equalsIgnoreCase("true") || obj.equals("1"));
        } else if (obj instanceof Integer) {
            return (((Integer) obj) == 1);
        }
        return false;
    }

    public static int getInt(Object object) {
        int length = 0;
        if (object == null) {
            return length;
        }
        if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                length = ((Double) object).intValue();
            } else {
                length = ((Float) object).intValue();
            }
        } else if (object instanceof Integer) {
            length = ((Integer) object);
        } else {
            try {
                length = Integer.parseInt(object.toString());
            } catch (NumberFormatException ignored) {

            }
        }
        return length;
    }

    public static double getDouble(Object object) {
        double length = 0;
        if (object == null) {
            return length;
        }
        if (object instanceof String) {
            try {
                length = Double.parseDouble((String) object);
            } catch (NumberFormatException ignored) {

            }
        } else if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                length = ((Double) object);
            } else {
                length = ((Float) object);
            }
        } else if (object instanceof Integer) {
            length = ((Integer) object);
        }
        return length;
    }
}
