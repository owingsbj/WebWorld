package com.gallantrealm.webworld.model;

import java.io.Serializable;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.InputResponseHandler;

public class GlobalFunctions implements Serializable {
	private static final long serialVersionUID = 1L;

	public static void alert(String message) {
		AndroidClientModel clientModel = AndroidClientModel.getClientModel();
		clientModel.alert(message, null);
	}

	public static boolean confirm(String message) {
		AndroidClientModel clientModel = AndroidClientModel.getClientModel();
		int rc = clientModel.alert(message, new String[] { "OK", "Cancel" });
		return rc == 0;
	}

	static String returnedValue = null;

	public static String prompt(String message, String defaultText) {
		AndroidClientModel clientModel = AndroidClientModel.getClientModel();
		returnedValue = null;
		final Thread thisThread = Thread.currentThread();
		clientModel.inputAlert("", message, defaultText, new String[] { "OK", "Cancel" }, new InputResponseHandler() {
			public void handleInput(String value, int option) {
				if (option == 0) {
					returnedValue = value;
				} else {
					returnedValue = null;
				}
				synchronized (thisThread) {
					thisThread.notify();
				}
			}
		});
		synchronized (thisThread) {
			try {
				thisThread.wait();
			} catch (InterruptedException e) {
			}
		}
		return returnedValue;
	}

}
