package com.search;

public class Engine {
	public int id;
	public String url;
	public boolean enabled;
	public String status;
	
	public Engine(int id, String url, Boolean enabled) {
		this.id = id;
		this.url = url;
		this.enabled = enabled;
		
		if (this.enabled) {
			this.status = "ENABLED";
		} else {
			this.status = "DISABLED";
		}
	}

	public int getId() {
		return this.id;
	}
	
	public String getUrl() {
		return this.url;
	}
	
	public boolean geEnabled() {
		return this.enabled;
	}
	
	public String getStatus() {
		return this.status;
	}
}
