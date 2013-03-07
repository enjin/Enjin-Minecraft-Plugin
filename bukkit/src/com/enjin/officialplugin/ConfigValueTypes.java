package com.enjin.officialplugin;

public enum ConfigValueTypes {

	STRING(0),
	INT(1),
	DOUBLE(2),
	FLOAT(3),
	BOOLEAN(4),
	FORBIDDEN(5);

	public static ConfigValueTypes fromString(String text) {
		for (ConfigValueTypes m : ConfigValueTypes.values()) {
			if (text.equalsIgnoreCase(m.name())) {
				return m;
			}
		}
		return null;
	}

	public final byte id;
	ConfigValueTypes(int i) {
		id = (byte) i;
	}

}
