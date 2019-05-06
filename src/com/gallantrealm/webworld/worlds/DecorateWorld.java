package com.gallantrealm.webworld.worlds;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.client.model.InputResponseHandler;
import com.gallantrealm.myworld.client.model.SelectColorHandler;
import com.gallantrealm.myworld.client.model.SelectResponseHandler;
import com.gallantrealm.myworld.model.WWAction;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWEntity;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.webworld.pens.Medium;
import com.gallantrealm.webworld.pens.Thin;
import com.gallantrealm.webworld.pens.Wide;
import com.gallantrealm.webworld.shapes.Box;
import com.gallantrealm.webworld.shapes.FilledBox;
import com.gallantrealm.webworld.shapes.FilledOval;
import com.gallantrealm.webworld.shapes.Line;
import com.gallantrealm.webworld.shapes.Oval;
import com.gallantrealm.webworld.shapes.Pen;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;

/**
 * Decorate an egg
 * 
 * @author owingsbj
 * 
 */
public class DecorateWorld extends BaseEggWorld {

	static int PEN = 0;
	static int CIRCLE = 2;
	static int RECTANGLE = 3;
	static int LINE = 4;
	static int DIAMOND = 5;

	AndroidClientModel clientModel = AndroidClientModel.getClientModel();

	WWSphere egg;
	String bitmapName;
	Bitmap bitmap;
	Buffer dragbuffer;
	ArrayList<Buffer> undoQueue;
	Canvas canvas;
	Paint paint;
	int penThickness = 4;
	boolean filled;
	String avatarName;
	int avatarNum;

	public DecorateWorld(String saveWorldFileName, String avatarName) {
		setName("Decorate");
		setGravity(0);

		this.avatarName = avatarName;
		bitmapName = avatarName + "_skin";
		bitmap = clientModel.loadBitmap(bitmapName + ".png");
		clientModel.saveBitmap(bitmap, bitmapName + ".png"); // immediately save it to create it
		dragbuffer = IntBuffer.allocate(262144);  // assumes 512x512 
		canvas = new Canvas(bitmap);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(2);
		undoQueue = new ArrayList<Buffer>();
		avatarNum = Integer.parseInt(avatarName.substring(6)) + 12;

		// user
//		WWUser user = new WWUser();
//		user.setName(avatarName);
//		addUser(user);
		egg = (WWSphere) makeAvatar(avatarName);
//		user.setAvatarId(egg.getId());
		egg.setPhysical(false);
		egg.setCircleVertices(128);
		egg.freedomMoveX = false;
		egg.freedomMoveY = false;
		egg.freedomMoveZ = false;
		clientModel.setSelectedObject(egg);
		clientModel.setCameraObject(egg);

		egg.addBehavior(new PaintBehavior());

		worldActions = new WWAction[] { new UpAction(), new DownAction(), new LeftAction(), new RightAction(), new InAction(), new OutAction(), new SaveAction() };
		avatarActions = new WWAction[] { new ClearAction(), new ColorAction(), new ThicknessAction(), new ToolAction(), new UndoAction() };

		clientModel.setCameraPan(180);
	}

	@Override
	public boolean allowPicking() {
		return true;
	}

	@Override
	public boolean usesAccelerometer() {
		return false;
	}

	@Override
	public boolean usesController() {
		return false;
	}

	@Override
	public boolean allowCameraPositioning() {
		return false;
	}

	class SaveAction extends WWAction {

		@Override
		public String getName() {
			return "Save";
		}

		@Override
		public void start() {
			clientModel.inputAlert("Save Egg", "What should the egg's name be?", clientModel.getAvatarDisplayName(avatarNum, avatarName), new String[] { "Save", "Save and Quit", "Cancel" }, new InputResponseHandler() {

				public void handleInput(String value, int option) {
					if (option == 0) {
						clientModel.saveBitmap(bitmap, bitmapName + ".png");
						clientModel.setAvatarDisplayName(avatarNum, value);
					} else if (option == 1) {
						clientModel.saveBitmap(bitmap, bitmapName + ".png");
						System.out.println("naming avatar " + avatarNum + " as " + value);
						clientModel.setAvatarDisplayName(avatarNum, value);
						clientModel.disconnect();
					}
				}
			});
		}
	}

	class UpAction extends WWAction {

		@Override
		public String getName() {
			return "Up";
		}

		@Override
		public void start() {
			clientModel.setCameraTilt(FastMath.min(clientModel.getCameraTilt() + 10, 90));
		}
	}

	class DownAction extends WWAction {

		@Override
		public String getName() {
			return "Down";
		}

		@Override
		public void start() {
			clientModel.setCameraTilt(FastMath.max(clientModel.getCameraTilt() - 10, -90));
		}

	}

	class LeftAction extends WWAction {

		@Override
		public String getName() {
			return "Left";
		}

		@Override
		public void start() {
			egg.setRotation(egg.getRotation().add(0, 0, 15));
		}

	}

	class RightAction extends WWAction {

		@Override
		public String getName() {
			return "Right";
		}

		@Override
		public void start() {
			egg.setRotation(egg.getRotation().add(0, 0, -15));
		}

	}

	class InAction extends WWAction {

		@Override
		public String getName() {
			return "In";
		}

		@Override
		public void start() {
			float distance = Math.max(0.75f, clientModel.getCameraDistance() * 0.75f);
			clientModel.setCameraDistance(distance);
		}

	}

	class OutAction extends WWAction {

		@Override
		public String getName() {
			return "Out";
		}

		@Override
		public void start() {
			float distance = Math.min(1.25f * clientModel.getCameraDistance(), 2.5f);
			clientModel.setCameraDistance(distance);
		}

	}

	class UndoAction extends WWAction {

		@Override
		public String getName() {
			return "Undo";
		}

		@Override
		public void start() {
			if (undoQueue.size() > 0) {
				Buffer buffer = undoQueue.remove(undoQueue.size() - 1);
				buffer.position(0);
				bitmap.copyPixelsFromBuffer(buffer);
				needsRepaint = true;
			}
		}

	}

	int selectedColor;
	int drawMode; // 0=pen, 1=eraser

	class ColorAction extends WWAction {

		@Override
		public String getName() {
			return "Color";
		}

		@Override
		public void start() {
			clientModel.selectColor("Select color", selectedColor, new SelectColorHandler() {
				public void handleSelect(WWColor color) {
					selectedColor = 0xFF000000 | color.getRGB();
				}
			});
		}
	}

	class ClearAction extends WWAction {

		@Override
		public String getName() {
			return "Clear";
		}

		@Override
		public void start() {
			clientModel.selectColor("Choose clear color", 0xFFFFFF, new SelectColorHandler() {
				public void handleSelect(WWColor wcolor) {
					int color = wcolor.getRGB();
					saveUndo();
					paint.setColor(0xff000000 | color);
					paint.setStyle(Style.FILL);
					canvas.drawRect(new RectF(0, 0, 512, 512), paint);
					needsRepaint = true;
				}
			});
		}
	}

	class PenAction extends WWAction {

		@Override
		public String getName() {
			return "Pen";
		}

		@Override
		public void start() {
			drawMode = PEN;
		}

	}

	class OvalAction extends WWAction {

		@Override
		public String getName() {
			return "Oval";
		}

		@Override
		public void start() {
			drawMode = CIRCLE;
		}

	}

	class RectangleAction extends WWAction {

		@Override
		public String getName() {
			return "Box";
		}

		@Override
		public void start() {
			drawMode = RECTANGLE;
		}

	}

	class LineAction extends WWAction {

		@Override
		public String getName() {
			return "Line";
		}

		@Override
		public void start() {
			drawMode = LINE;
		}

	}

	class ThicknessAction extends WWAction {

		@Override
		public String getName() {
			return "Width";
		}

		@Override
		public void start() {
			clientModel.selectAlert("Select drawing width", new Class[] { Thin.class, Medium.class, Wide.class }, null, new SelectResponseHandler() {

				public void handleSelect(Class selectedItem, int option) {
					if (option == 0) {
						if (selectedItem == Thin.class) {
							penThickness = 2;
						} else if (selectedItem == Medium.class) {
							penThickness = 4;
						} else if (selectedItem == Wide.class) {
							penThickness = 8;
						}
					}
				}

			});
		}

	}

	class ToolAction extends WWAction {

		@Override
		public String getName() {
			return "Tool";
		}

		@Override
		public void start() {
			clientModel.selectAlert("Select tool", new Class[] { Pen.class, Line.class, Oval.class, Box.class, FilledOval.class, FilledBox.class }, null, new SelectResponseHandler() {

				public void handleSelect(Class selectedItem, int option) {
					if (selectedItem == Pen.class) {
						drawMode = PEN;
					} else if (selectedItem == Line.class) {
						drawMode = LINE;
					} else if (selectedItem == Oval.class) {
						drawMode = CIRCLE;
						filled = false;
					} else if (selectedItem == Box.class) {
						drawMode = RECTANGLE;
						filled = false;
					} else if (selectedItem == FilledOval.class) {
						drawMode = CIRCLE;
						filled = true;
					} else if (selectedItem == FilledBox.class) {
						drawMode = RECTANGLE;
						filled = true;
					}
				}

			});
		}

	}

	class AccessoriesAction extends WWAction {

		@Override
		public String getName() {
			return "Hair";
		}

		@Override
		public void start() {
			clientModel.selectAlert("Select accessory", new Class[] {}, new String[] { "OK", "Cancel" }, new SelectResponseHandler() {

				public void handleSelect(Class selectedItem, int option) {
					// TODO Auto-generated method stub

				}

			});
		}

	}

	void saveUndo() {
		if (undoQueue.size() > 10) {
			undoQueue.remove(0);
		}
		IntBuffer buffer = IntBuffer.allocate(262144);
		bitmap.copyPixelsToBuffer(buffer);
		undoQueue.add(buffer);
	}

	boolean needsRepaint;

	class PaintBehavior extends WWBehavior {

		PaintBehavior() {
			setTimer(500);
		}

		@Override
		public boolean pressEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
			int i = (int) Math.min(Math.max(x * 511.0f, 0), 511);
			int j = (int) Math.min(Math.max((1 - y) * 511.0f, 0), 511);
			if (drawMode == PEN) {
				startPen(i, j);
			} else if (drawMode == CIRCLE) {
				startOval(i, j);
			} else if (drawMode == RECTANGLE) {
				startRect(i, j);
			} else if (drawMode == LINE) {
				startLine(i, j);
			}
			needsRepaint = true;
			return true;
		}

		@Override
		public boolean dragEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
			int i = (int) Math.min(Math.max(x * 511.0f, 0), 511);
			int j = (int) Math.min(Math.max((1 - y) * 511.0f, 0), 511);
			if (drawMode == PEN) {
				midPen(i, j);
			} else if (drawMode == CIRCLE) {
				midOval(i, j);
			} else if (drawMode == RECTANGLE) {
				midRect(i, j);
			} else if (drawMode == LINE) {
				midLine(i, j);
			}
			needsRepaint = true;
			return true;
		}

		@Override
		public boolean releaseEvent(WWObject object, WWEntity toucher, int side, float x, float y) {
			int i = (int) Math.min(Math.max(x * 511.0f, 0), 511);
			int j = (int) Math.min(Math.max((1 - y) * 511.0f, 0), 511);
			if (drawMode == PEN) {
				endPen(i, j);
			} else if (drawMode == CIRCLE) {
				endOval(i, j);
			} else if (drawMode == RECTANGLE) {
				endRect(i, j);
			} else if (drawMode == LINE) {
				endLine(i, j);
			}
			needsRepaint = true;
			return true;
		}

		@Override
		public boolean timerEvent() {
			if (needsRepaint) {
				((AndroidRenderer) getRendering().getRenderer()).regenTexture(bitmap, bitmapName);
				needsRepaint = false;
			}
			setTimer(100);
			return true;
		}

	}

	int lastX;
	int lastY;

	void startPen(int i, int j) {
		saveUndo();
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		canvas.drawPoint(i, j, paint);
		lastX = i;
		lastY = j;
	}

	void midPen(int i, int j) {
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		if (Math.abs(lastX - i) + Math.abs(lastY - j) < 250) {
			canvas.drawLine(lastX, lastY, i, j, paint);
		}
		lastX = i;
		lastY = j;
	}

	void endPen(int i, int j) {
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		if (Math.abs(lastX - i) + Math.abs(lastY - j) < 250) {
			canvas.drawLine(lastX, lastY, i, j, paint);
		}
		lastX = i;
		lastY = j;
	}

	void startLine(int i, int j) {
		saveUndo();
		lastX = i;
		lastY = j;
		dragbuffer.clear();
		bitmap.copyPixelsToBuffer(dragbuffer);
	}

	void midLine(int i, int j) {
		dragbuffer.position(0);
		bitmap.copyPixelsFromBuffer(dragbuffer);
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Style.STROKE);
		canvas.drawLine(lastX, lastY, i, j, paint);
	}

	void endLine(int i, int j) {
		dragbuffer.position(0);
		bitmap.copyPixelsFromBuffer(dragbuffer);
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		paint.setStyle(Style.STROKE);
		canvas.drawLine(lastX, lastY, i, j, paint);
	}

	void startOval(int i, int j) {
		saveUndo();
		lastX = i;
		lastY = j;
		dragbuffer.clear();
		bitmap.copyPixelsToBuffer(dragbuffer);
	}

	void midOval(int i, int j) {
		dragbuffer.position(0);
		bitmap.copyPixelsFromBuffer(dragbuffer);
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		if (filled) {
			paint.setStyle(Style.FILL_AND_STROKE);
		} else {
			paint.setStyle(Style.STROKE);
		}
		RectF oval = new RectF(lastX, lastY, i, j);
		canvas.drawOval(oval, paint);
	}

	void endOval(int i, int j) {
		dragbuffer.position(0);
		bitmap.copyPixelsFromBuffer(dragbuffer);
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		if (filled) {
			paint.setStyle(Style.FILL_AND_STROKE);
		} else {
			paint.setStyle(Style.STROKE);
		}
		RectF oval = new RectF(lastX, lastY, i, j);
		canvas.drawOval(oval, paint);
	}

	void startRect(int i, int j) {
		saveUndo();
		lastX = i;
		lastY = j;
		dragbuffer.clear();
		bitmap.copyPixelsToBuffer(dragbuffer);
	}

	void midRect(int i, int j) {
		dragbuffer.position(0);
		bitmap.copyPixelsFromBuffer(dragbuffer);
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		if (filled) {
			paint.setStyle(Style.FILL_AND_STROKE);
		} else {
			paint.setStyle(Style.STROKE);
		}
		RectF oval = new RectF(lastX, lastY, i, j);
		canvas.drawRect(oval, paint);
	}

	void endRect(int i, int j) {
		dragbuffer.position(0);
		bitmap.copyPixelsFromBuffer(dragbuffer);
		paint.setColor(0xff000000 | selectedColor);
		paint.setStrokeWidth(penThickness);
		paint.setStrokeCap(Cap.ROUND);
		if (filled) {
			paint.setStyle(Style.FILL_AND_STROKE);
		} else {
			paint.setStyle(Style.STROKE);
		}
		RectF oval = new RectF(lastX, lastY, i, j);
		canvas.drawRect(oval, paint);
	}
	
	public boolean supportsOpenGLES20() {
		return false;
	}
}
