package com.gallantrealm.webworld.model;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWEntity;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWVector;

public class EventBehavior extends WWBehavior {
	private static final long serialVersionUID = 1L;

	public Function onTouch;
	public Function onPress;
	public Function onDrag;
	public Function onRelease;
	public Function onCollide;
	public Function onSlide;
	public Function onStopSlide;
	public Function onTimer;
	
	@Override
	public boolean touchEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onTouch != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
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
	
	@Override
	public boolean pressEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onPress != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			onPress.call(cx, world.scope, scriptableObject, new Object[] {scriptableToucher, side, x, y});
			Context.exit();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean dragEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onDrag != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			onDrag.call(cx, world.scope, scriptableObject, new Object[] {scriptableToucher, side, x, y});
			Context.exit();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean releaseEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
		if (onRelease != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableToucher;
			if (toucher != null) {
				scriptableToucher = Context.toObject(toucher, world.scope);
			} else {
				scriptableToucher = null;
			}
			onRelease.call(cx, world.scope, scriptableObject, new Object[] {scriptableToucher, side, x, y});
			Context.exit();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onCollide != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableNearObject = Context.toObject(nearObject, world.scope);
			onCollide.call(cx, world.scope, scriptableObject, new Object[] {scriptableNearObject});
			Context.exit();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean slideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onSlide != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableNearObject = Context.toObject(nearObject, world.scope);
			onSlide.call(cx, world.scope, scriptableObject, new Object[] {scriptableNearObject});
			Context.exit();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean stopSlideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
		if (onStopSlide != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			Scriptable scriptableNearObject = Context.toObject(nearObject, world.scope);
			onStopSlide.call(cx, world.scope, scriptableObject, new Object[] {scriptableNearObject});
			Context.exit();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean timerEvent(WWObject object) {
		if (onTimer != null) {
			Context cx = Context.enter();
			World world = (World)object.world;
			Scriptable scriptableObject = Context.toObject(object, world.scope);
			onTimer.call(cx, world.scope, scriptableObject, new Object[] {});
			Context.exit();
			return true;
		}
		return false;
	}

}
