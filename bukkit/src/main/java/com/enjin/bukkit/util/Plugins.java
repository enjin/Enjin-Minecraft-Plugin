package com.enjin.bukkit.util;

import org.bukkit.Bukkit;

public class Plugins {
	public static boolean isEnabled(String plugin) {
		return Bukkit.getPluginManager().isPluginEnabled(plugin);
	}
}
