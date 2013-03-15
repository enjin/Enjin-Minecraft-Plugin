package com.enjin.officialplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.server.MinecraftServer;

public class EnjinConfig {

	File INIFILE;
	Properties iniSettings = new Properties();
	boolean newconfigfile = false;
	
	public EnjinConfig(File datafolder) {
		INIFILE = new File(datafolder, "config.properties");
		loadIni();
	}

	public void loadIni() {
		if (INIFILE.exists()) {
			try {
				iniSettings.load(new FileInputStream(INIFILE));

			}catch (Exception e) {
				MinecraftServer.getServer().logInfo("[EnjinMinecraftPlugin] - properties file load failed, using defaults.");
			}		
		}else {
			createIni();
		}
	}
	
	public void createIni() {
		newconfigfile = true;
		try {
			INIFILE.getParentFile().mkdirs();
			INIFILE.createNewFile();
			iniSettings.load(new FileInputStream(INIFILE));
		} catch (Exception e) {
			MinecraftServer.getServer().logInfo("[EnjinMinecraftPlugin] - properties file creation failed, using defaults.");
		}

	}
	public boolean isNewConfigFile() {
		return newconfigfile;
	}

	public boolean getBoolean(String key) {
		String value = iniSettings.getProperty(key, "");
		if(value.equalsIgnoreCase("true")) {
			return true;
		}else {
			return false;
		}
	}

	public boolean getBoolean(String key, boolean other) {
		String value = iniSettings.getProperty(key, "");
		if(value.equalsIgnoreCase("true")) {
			return true;
		}else if(value.equalsIgnoreCase("false")){
			return false;
		}else {
			return other;
		}
	}
	
	public double getDouble(String key, double default1) {
		String value = iniSettings.getProperty(key, "");
		try{
			return Double.parseDouble(value.trim());
		}catch (Exception e) {
			return default1;
		}
	}
	
	public float getFloat(String key, float default1) {
		String value = iniSettings.getProperty(key, "");
		try{
			return Float.parseFloat(value.trim());
		}catch (Exception e) {
			return default1;
		}
	}
	
	public int getInt(String key, int default1) {
		String value = iniSettings.getProperty(key, "");
		try{
			return Integer.parseInt(value.trim());
		}catch (Exception e) {
			return default1;
		}
	}
	
	public long getLong(String key, long default1) {
		String value = iniSettings.getProperty(key, "");
		try{
			return Long.parseLong(value.trim());
		}catch (Exception e) {
			return default1;
		}
	}
	
	public String getString(String key, String default1) {
		return iniSettings.getProperty(key, default1);
	}
	
	public void set(String key, boolean value) {
		iniSettings.setProperty(key, Boolean.toString(value));
	}
	
	public void set(String key, String value) {
		iniSettings.setProperty(key, value);
	}
	
	public void set(String key, int value) {
		iniSettings.setProperty(key, Integer.toString(value));
	}
	
	public void save() {
		try {
			iniSettings.store(new FileOutputStream(INIFILE), "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void set(String key, double number) {
		iniSettings.setProperty(key, Double.toString(number));
	}

	public void set(String key, float number) {
		iniSettings.setProperty(key, Float.toString(number));
	}

	public String getString(String key) {
		return iniSettings.getProperty(key);
	}
}
