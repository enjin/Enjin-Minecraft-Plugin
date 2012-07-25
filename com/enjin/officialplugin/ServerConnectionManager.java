package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;

public class ServerConnectionManager implements Runnable{
	final static int port = 6115;
	static boolean stopping = false;
	private ServerSocket servSocket;
	private final List<ServerConnection> connections = new LinkedList<ServerConnection>();
	
	@Override
	public void run() {
		try {
			servSocket = new ServerSocket(port, 50, EnjinMinecraftPlugin.localip);
			Bukkit.getLogger().info("Starting enjin listen server at " + ((EnjinMinecraftPlugin.localip == null) ? "localhost" : EnjinMinecraftPlugin.localip.getHostAddress()) + ":6115.");
			
			while(!stopping) {
				Socket socket = servSocket.accept();
				if(socket == null) {
					break;
				}
				ServerConnection con = new ServerConnection(socket);
				System.out.println("An Enjin server connected: " + socket.getInetAddress().getHostAddress());
				connections.add(con);
			}
		} catch (BindException e){
			Bukkit.getLogger().severe("Error, exception: JVM_Bind, Could not create a server socket on port " + port + ".");
			Bukkit.getLogger().severe("Is there already a server running on port " + port + "?");
		} catch (SocketException e) {
			return; //shutdown
		} catch (IOException e) {
			e.printStackTrace();
		}
		stop();
	}
	
	public void stop() {
		stopping = true;
		for(ServerConnection con : connections) {
			con.stop();
			connections.remove(con);
		}
		try {
			if(servSocket != null)
				servSocket.close();
		} catch (IOException e) {}
		Bukkit.getLogger().info("Stopping enjin listen server.");
	}
}
