package com.bree.proxy.model;

public class ProxyInfo {

	private int used;
	private String ip;
	private int port;
	private String username;
	private String password;
	
	public ProxyInfo(int used, String ip, int port, String username, String password) {
		super();
		this.used = used;
		this.ip = ip;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	public int getUsed() {
		return used;
	}
	public void setUsed(int used) {
		this.used = used;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
