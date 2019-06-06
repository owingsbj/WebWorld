package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWEntity;
import com.gallantrealm.myworld.model.WWObject;

public class EventBehavior extends WWBehavior {
	private static final long serialVersionUID = 1L;

	public Function onTouch;
	
	@Override
	public boolean touchEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onTouch != null) {
			World world = (World)object.world;
			Context cx = Context.enter();
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			onTouch.call(cx, world.scope, scriptableObject, new Object[] {scriptableToucher, side, x, y});
			Context.exit();
			return true;
		}
		return false;
	}

}
