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
	
	static String[] selections;
	static String[] imageFileNames;
	
	static class Selection1 {
		public static String getStaticName() {
			return selections[0];
		}
		public static String getStaticImageFileName() {
			return imageFileNames[0];
		}
	}
	
	static class Selection2 {
		public static String getStaticName() {
			return selections[1];
		}
		public static String getStaticImageFileName() {
			return imageFileNames[1];
		}
	}
	
	static class Selection3 {
		public static String getStaticName() {
			return selections[2];
		}
		public static String getStaticImageFileName() {
			return imageFileNames[2];
		}
	}
	
	static class Selection4 {
		public static String getStaticName() {
			return selections[3];
		}
		public static String getStaticImageFileName() {
			return imageFileNames[3];
		}
	}
	
	static class Selection5 {
		public static String getStaticName() {
			return selections[4];
		}
		public static String getStaticImageFileName() {
			return imageFileNames[4];
		}
	}
	
	static class Selection6 {
		public static String getStaticName() {
			return selections[5];
		}
		public static String getStaticImageFileName() {
			return imageFileNames[5];
		}
	}
	
	public static String select(String message, NativeArray jsSelections, NativeArray jsImageFileNames) {
		AndroidClientModel clientModel = AndroidClientModel.getClientModel();
		selections = new String[jsSelections.size()];
		for (int i = 0; i < selections.length; i++) {
			selections[i] = String.valueOf(jsSelections.get(i));
		}
		imageFileNames = new String[jsImageFileNames.size()];
		for (int i = 0; i < imageFileNames.length; i++) {
			imageFileNames[i] = Texture.worldPrefixUrl(String.valueOf(jsImageFileNames.get(i)));
		}
		Class[] availableItems = new Class[selections.length];
		for (int i = 0; i < selections.length; i++) {
			if (i == 0) {
				availableItems[i] = Selection1.class;
			} else if (i == 1) {
				availableItems[i] = Selection2.class;
			} else if (i == 2) {
				availableItems[i] = Selection3.class;
			} else if (i == 3) {
				availableItems[i] = Selection4.class;
			} else if (i == 4) {
				availableItems[i] = Selection5.class;
			} else if (i == 5) {
				availableItems[i] = Selection6.class;
			}
		}
		returnedValue = null;
		final Thread thisThread = Thread.currentThread();
		clientModel.selectAlert(message, availableItems, new String[] { "OK", "Cancel" }, new SelectResponseHandler() {
			public void handleSelect(Class selectedItem, int option) {
				if (option == 0) {
					try {
						returnedValue = (String)selectedItem.getMethod("getStaticName").invoke(null);
					} catch (Exception e) {
						returnedValue = null;;
					}
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
