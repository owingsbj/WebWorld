package com.gallantrealm.webworld.model;

import java.io.Serializable;

public class Console implements Serializable {
	private static final long serialVersionUID = 1L;

	public Console() {
	}
	
	public void log(String message) {
		
	}
	
	public void trace(String message) {
		log(message);
	}
	
	public void info(String message) {
		log(message);
	}
	
	public void warn(String message) {
		log(message);
	}
	
	public void error(String message) {
		log(message);
	}
	
	public void assertt(boolean condition, String message) {
		if (condition) {
			log(message);
		}
	}

}
