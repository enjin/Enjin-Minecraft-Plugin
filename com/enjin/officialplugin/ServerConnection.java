package com.enjin.officialplugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.bukkit.Bukkit;

public class ServerConnection {
	private final Socket socket;
	public final InputStream in;
	public final OutputStream out;
	public final String ip;
	private final InputThread inthread;
	
	private class InputThread implements Runnable {
		@Override
		public void run() {
			try {
				int read;
				System.out.println("Enjin server connected: " + ip + ".");
				if(!PacketLoader.handleLogin(in)) {
					throw new Exception("Failed to authenticate server: " + ip);
				}
				while(((read = in.read()) > -1) && !ServerConnectionManager.stopping) {
					System.out.println("recieved packet: " + read);
					PacketLoader.readPacket(read, ServerConnection.this);
				}
				stop();
				System.out.println("stopping: " + ServerConnectionManager.stopping);
			} catch (Throwable t) {
				Bukkit.getLogger().warning("There was an error with the enjin server " + ip + ". " + t.getMessage());
				t.printStackTrace();
			}
		}
	}
	
	public ServerConnection(Socket socket) throws IOException {
		this.socket = socket;
		this.in = socket.getInputStream();
		this.out = socket.getOutputStream();
		this.ip = socket.getInetAddress().getHostAddress();
		inthread = new InputThread();
		(new Thread(inthread)).start();
	}
	
	public void stop() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Throwable t) {
			//socket probably closed;
		}
		EnjinMinecraftPlugin.conManager.removeConnection(this);
	}
}
