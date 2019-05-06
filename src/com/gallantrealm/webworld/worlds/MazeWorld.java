package com.gallantrealm.webworld.worlds;

import java.util.ArrayList;
import com.gallantrealm.myworld.FastMath;
import com.gallantrealm.myworld.android.AndroidClientModel;
import com.gallantrealm.myworld.android.renderer.AndroidRenderer;
import com.gallantrealm.myworld.client.model.ClientModelChangedEvent;
import com.gallantrealm.myworld.model.WWBehavior;
import com.gallantrealm.myworld.model.WWBox;
import com.gallantrealm.myworld.model.WWColor;
import com.gallantrealm.myworld.model.WWObject;
import com.gallantrealm.myworld.model.WWSimpleShape;
import com.gallantrealm.myworld.model.WWSphere;
import com.gallantrealm.myworld.model.WWUser;
import com.gallantrealm.myworld.model.WWVector;

// TODO: Add complexities on levels > 5
// TODO: Add points thing (level & time)
public class MazeWorld extends BaseEggWorld {

	WWSimpleShape avatar;

	public MazeWorld(String saveWorldFileName, String avatarName) {

		setName("Maze");

		setLevel(Math.max(1, AndroidClientModel.getClientModel().getScore(2)));

		// the ground
		WWSimpleShape ground = new WWBox(); // for the ground
		ground.setElasticity(-1);
		ground.setName("ground");
		ground.setColor(WWSimpleShape.SIDE_TOP, new WWColor(0x80C080)); // green, like grass
		ground.setSize(new WWVector(1000, 1000, 100));
		ground.setPosition(new WWVector(0, 0, -50));
		ground.setTextureURL(WWSimpleShape.SIDE_TOP, "grass");
		ground.setTextureScaleX(WWSimpleShape.SIDE_TOP, 0.01f);
		ground.setTextureScaleY(WWSimpleShape.SIDE_TOP, 0.01f);
		//ground.setImpactSound("grass");
		ground.setSlidingSound("movingGrass");
		addObject(ground);

		// user
		WWUser user = new WWUser();
		user.setName(avatarName);
		addUser(user);
		avatar = makeAvatar(avatarName);
		avatar.setPosition(new WWVector(3, -5, 1));
		avatar.setRotation(new WWVector(0, 0, 180));
		user.setAvatarId(avatar.getId());

		// the maze
		buildMaze(getLevel());

	}

	@Override
	public void displayed() {
		showBannerAds();
	}
	
	private int mazeWidth;
	private int mazeHeight;
	private boolean[][] beenThere;
	private boolean[][] horizontalWalls;
	private boolean[][] verticalWalls;
	ArrayList<WWBox> mazeWalls;
	WWBox startBanner;
	WWBox startLine;
	WWBox finishBanner;
	WWBox finishLine;

	/**
	 * The algorithm used to generate the maze is to randomly move forward, left, or right. Then recursively invoke to
	 * move again. No move is valid, though, if the trail has already been covered. The moving continues, then, till all
	 * avenues of movement are exhausted.
	 */
	private void buildMaze(int complexity) {

		mazeWidth = complexity * 2;
		mazeHeight = complexity * 2;

		beenThere = new boolean[mazeWidth][mazeHeight];
		horizontalWalls = new boolean[mazeWidth][mazeHeight + 1];
		verticalWalls = new boolean[mazeWidth + 1][mazeHeight];
		for (int x = 0; x < mazeWidth; x++) {
			for (int y = 0; y < mazeHeight + 1; y++) {
				horizontalWalls[x][y] = true;
			}
		}
		for (int x = 0; x < mazeWidth + 1; x++) {
			for (int y = 0; y < mazeHeight; y++) {
				verticalWalls[x][y] = true;
			}
		}

		mazeMove(mazeWidth - 1, mazeHeight - 1);
		horizontalWalls[0][0] = false;
		horizontalWalls[mazeWidth - 1][mazeHeight] = false;

		buildMazeWalls();
		buildStartAndFinish();
	}

	class Move {
		int x;
		int y;
		boolean movedUp, movedDown, movedLeft, movedRight;

		public Move(int x, int y, boolean movedUp, boolean movedDown, boolean movedLeft, boolean movedRight) {
			this.x = x;
			this.y = y;
			this.movedUp = movedUp;
			this.movedDown = movedDown;
			this.movedLeft = movedLeft;
			this.movedRight = movedRight;
		}
	};

	ArrayList<Move> moveStack = new ArrayList<Move>();

	private final void mazeMove(int x, int y) {
		beenThere[x][y] = true;
		moveStack.add(new Move(x, y, false, false, false, false));

		while (!moveStack.isEmpty()) {
			Move move = moveStack.remove(moveStack.size() - 1);
			x = move.x;
			y = move.y;
			boolean movedUp = move.movedUp;
			boolean movedDown = move.movedDown;
			boolean movedLeft = move.movedLeft;
			boolean movedRight = move.movedRight;
			while (!(movedUp && movedDown && movedLeft && movedRight)) {
				int choice = (int) (FastMath.random(0, 3.9f));
				if (choice <= 0 && !movedUp) {
					movedUp = true;
					if (y + 1 < mazeHeight && !beenThere[x][y + 1]) {
						horizontalWalls[x][y + 1] = false;
						moveStack.add(new Move(x, y, movedUp, movedDown, movedLeft, movedRight));
						y = y + 1;
						beenThere[x][y] = true;
						movedUp = false;
						movedDown = true;
						movedLeft = false;
						movedRight = false;
					}
				} else if (choice <= 1 && !movedDown) {
					movedDown = true;
					if (y > 0 && !beenThere[x][y - 1]) {
						horizontalWalls[x][y] = false;
						moveStack.add(new Move(x, y, movedUp, movedDown, movedLeft, movedRight));
						y = y - 1;
						beenThere[x][y] = true;
						movedUp = true;
						movedDown = false;
						movedLeft = false;
						movedRight = false;
					}
				} else if (choice <= 2 && !movedLeft) {
					movedLeft = true;
					if (x > 0 && !beenThere[x - 1][y]) {
						verticalWalls[x][y] = false;
						moveStack.add(new Move(x, y, movedUp, movedDown, movedLeft, movedRight));
						x = x - 1;
						beenThere[x][y] = true;
						movedUp = false;
						movedDown = false;
						movedLeft = false;
						movedRight = true;
					}
				} else if (!movedRight) { // right
					movedRight = true;
					if (x + 1 < mazeWidth && !beenThere[x + 1][y]) {
						verticalWalls[x + 1][y] = false;
						moveStack.add(new Move(x, y, movedUp, movedDown, movedLeft, movedRight));
						x = x + 1;
						beenThere[x][y] = true;
						movedUp = false;
						movedDown = false;
						movedLeft = true;
						movedRight = false;
					}
				}
			}
		}
	}

	private void buildMazeWalls() {
		mazeWalls = new ArrayList<WWBox>();
		for (int y = 0; y < mazeHeight; y++) {
			for (int x = 0; x < mazeWidth; x++) {
				if (horizontalWalls[x][y]) {
					WWBox mazeWall = new WWBox();
					mazeWall.setMonolithic(true);
					mazeWall.setFixed(true);
					mazeWall.setSize(new WWVector(5, 1, 2.5f));
					mazeWall.setPosition(new WWVector(x * 5 + 0.5f + 2.5f, y * 5, 1.25f));
					mazeWall.setTextureURL(WWObject.SIDE_ALL, "brick");
					mazeWall.setTextureScaleX(WWObject.SIDE_ALL, 0.5f);
					mazeWall.setImpactSound("wood");
					addObject(mazeWall);
					mazeWalls.add(mazeWall);
				}
			}
			for (int x = 0; x < mazeWidth + 1; x++) {
				if (verticalWalls[x][y]) {
					WWBox mazeWall = new WWBox();
					mazeWall.setMonolithic(true);
					mazeWall.setSize(new WWVector(1, 5, 2.5f));
					mazeWall.setPosition(new WWVector(x * 5 + 0.5f, y * 5 + 2.5f, 1.25f));
					mazeWall.setTextureURL(WWObject.SIDE_ALL, "brick");
					mazeWall.setTextureScaleX(WWObject.SIDE_ALL, 0.5f);
					mazeWall.setImpactSound("wood");
					addObject(mazeWall);
					mazeWalls.add(mazeWall);
				}
			}
		}
		for (int x = 0; x < mazeWidth; x++) {
			if (horizontalWalls[x][mazeHeight]) {
				WWBox mazeWall = new WWBox();
				mazeWall.setMonolithic(true);
				mazeWall.setSize(new WWVector(5, 1, 2.5f));
				mazeWall.setPosition(new WWVector(x * 5 + 2.5f, mazeHeight * 5 + 0.5f, 1.25f));
				mazeWall.setTextureURL(WWObject.SIDE_ALL, "brick");
				mazeWall.setTextureScaleX(WWObject.SIDE_ALL, 0.5f);
				mazeWall.setImpactSound("wood");
				addObject(mazeWall);
				mazeWalls.add(mazeWall);
			}
		}
//		WWBox mazeRoof = new WWBox();
//		mazeRoof.setSize(new WWVector(mazeWidth * 5, mazeHeight * 5, 1));
//		mazeRoof.setPosition(new WWVector(mazeWidth * 5 / 2, mazeHeight * 5 / 2, 4));
//		mazeRoof.setTextureURL(WWObject.SIDE_ALL, "brick");
//		addObject(mazeRoof);
//		mazeWalls.add(mazeRoof);
	}

	private void destroyMaze() {
		for (int i = 0; i < mazeWalls.size(); i++) {
			removeObject(mazeWalls.get(i).getId());
		}
		AndroidRenderer.clearRenderings();
	}

	private void buildStartAndFinish() {
		if (startBanner == null) {
			startBanner = new WWBox();
			startBanner.setSize(new WWVector(5, 0.5f, 1));
			startBanner.setPosition(3f, 0.5f, 3);
			startBanner.setTextureURL(WWBox.SIDE_SIDE1, "start");
			addObject(startBanner);
			startLine = new WWBox();
			startLine.setSize(new WWVector(5, 1, 5));
			startLine.setPosition(3f, 0.5f, 0);
			startLine.setTransparency(WWBox.SIDE_ALL, 1.0f);
			startLine.setSolid(false);
			startLine.addBehavior(new StartLineBehavior());
			addObject(startLine);

			WWSphere startBarrier = new WWSphere();
			startBarrier.setTransparency(SIDE_ALL, 1f);
			startBarrier.setPenetratable(true);
			startBarrier.setHollow(0.8f);
			startBarrier.setCutoutEnd(0.5f);
			startBarrier.setSize(15f, 6f, 15f);
			startBarrier.setPosition(3, 0, 0);
			startBarrier.setRotation(0, 0, -90);
			addObject(startBarrier);

			finishBanner = new WWBox();
			finishBanner.setSize(new WWVector(5, 0.5f, 1));
			finishBanner.setPosition(5 * mazeWidth - 2.5f, 5 * mazeHeight + 0.5f, 3);
			finishBanner.setTextureURL(WWBox.SIDE_SIDE1, "finish");
			addObject(finishBanner);
			finishLine = new WWBox();
			finishLine.setSize(new WWVector(5, 1, 5));
			finishLine.setPosition(5 * mazeWidth - 2.5f, 5 * mazeHeight + 0.5f, 0);
			finishLine.setTransparency(WWBox.SIDE_ALL, 1.0f);
			finishLine.setSolid(false);
			finishLine.addBehavior(new FinishLineBehavior());
			addObject(finishLine);
		} else {
			// just adjust positions
			finishBanner.setPosition(5 * mazeWidth - 2.5f, 5 * mazeHeight + 0.5f, 3);
			finishLine.setPosition(5 * mazeWidth - 2.5f, 5 * mazeHeight + 0.5f, 0);
		}
	}

	private class StartLineBehavior extends WWBehavior {
		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			playSong("maze_music", 0.25f);
			return true;
		}
	}

	private class FinishLineBehavior extends WWBehavior {

		@Override
		public boolean collideEvent(WWObject object, WWObject nearObject, WWVector proximity) {
			stopPlayingSong();
			AndroidClientModel clientModel = AndroidClientModel.getClientModel();
			playSound("winningSound", 0.125f);
			if (clientModel.isPlayMusic()) {
				playSound("winningSound2", 0.1f);
			}
			AndroidClientModel.getClientModel().setScore(2, getLevel());
			int rc = clientModel.alert(null, "Congratulations, you made it through level " + getLevel() + "!", new String[] { "Next Level", "Quit" }, "I made it through level " + getLevel() + " of the EggWorld maze!");
			destroyMaze();
			if (rc == 0) {
				setLevel(getLevel() + 1);
				clientModel.fireClientModelChanged(ClientModelChangedEvent.EVENT_TYPE_WWMODEL_UPDATED);
			} else {
				clientModel.disconnect();
				return true;
			}
			buildMaze(getLevel());

			avatar.setThrust(new WWVector(0, 0, 0));
			avatar.setVelocity(new WWVector(0, 0, 0));
			avatar.setPosition(new WWVector(3, -5, 1));
			avatar.setRotation(new WWVector(0, 0, 180));
			return true;
		}

	}

//	@Override
//	public float[] getMoveXTurn() {
//		return new float[] { -180, -120, -60, -30, -15, 0, 15, 30, 60, 120, 180 };
//	}

}
