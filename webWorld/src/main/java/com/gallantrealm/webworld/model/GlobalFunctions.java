package com.gallantrealm.webworld.model;

import java.io.Serializable;
import org.mozilla.javascript.NativeArray;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.client.model.InputResponseHandler;
import com.gallantrealm.myworld.client.model.SelectResponseHandler;

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
	static Integer returnedIndex = null;

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
	
	static Selection[] selections;
	
	static class Selection {
		String name;
		String imageFileName;
		public Selection(String name, String imageFileName) {
			this.name = name;
			this.imageFileName = imageFileName;
		}
		public String getName() {
			return name;
		}
		public String getImageFileName() {
			return imageFileName;
		}
	}
	
	public static Integer select(String message, NativeArray jsSelections, NativeArray jsImageFileNames) {
		AndroidClientModel clientModel = AndroidClientModel.getClientModel();
		selections = new Selection[jsSelections.size()];
		for (int i = 0; i < selections.length; i++) {
			selections[i] = new Selection(String.valueOf(jsSelections.get(i)), Texture.worldPrefixUrl(String.valueOf(jsImageFileNames.get(i))));
		}
		returnedIndex = null;
		final Thread thisThread = Thread.currentThread();
		clientModel.selectAlert(message, selections, null, new SelectResponseHandler() {
			public void handleSelect(int selectedItemPosition, int option) {
				if (option == 0) {
					try {
						returnedIndex = (Integer)selectedItemPosition;
					} catch (Exception e) {
						returnedIndex = null;;
					}
				} else {
					returnedIndex = null;
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
		return returnedIndex;
	}

}
