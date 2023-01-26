package com.gallantrealm.webworld.model;

import java.io.Serializable;
import com.gallantrealm.myworld.android.AndroidClientModel;

public class Console implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static void log(String message) {
		AndroidClientModel.getClientModel().log(message);
	}
	
	public static void debug(String message) {
		if (AndroidClientModel.getClientModel().isShowDebugLogging()) {
			log("DEBUG: "+message);
		}
	}
	
	public static void info(String message) {
		log("INFO: "+message);
	}
	
	public static void warn(String message) {
		log("WARNING: "+message);
	}
	
	public static void error(String message) {
		log("ERROR: "+message);
	}
	
	public static void assertt(boolean condition, String message) {
		if (condition) {
			if (AndroidClientModel.getClientModel().isShowDebugLogging()) {
				log("ASSERT: "+message);
			}
		}
	}

}
