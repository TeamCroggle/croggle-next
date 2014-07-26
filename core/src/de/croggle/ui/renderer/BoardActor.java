package de.croggle.ui.renderer;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

import de.croggle.data.AssetManager;
import de.croggle.data.persistence.Setting;
import de.croggle.data.persistence.SettingChangeListener;
import de.croggle.game.ColorController;
import de.croggle.game.board.Board;
import de.croggle.game.event.BoardEventListener;
import de.croggle.game.event.BoardEventMessenger;
import de.croggle.ui.renderer.layout.ActorLayout;
import de.croggle.ui.renderer.layout.ActorLayoutConfiguration;
import de.croggle.ui.renderer.layout.ActorLayoutStatistics;
import de.croggle.ui.renderer.objectactors.BoardObjectActor;
import de.croggle.ui.renderer.objectactors.ColoredBoardObjectActor;

/**
 * An actor used for representing a whole board, i.e. an alligator
 * constellation.
 */
public class BoardActor extends Group implements SettingChangeListener {

	private static boolean headless = false;

	/*
	 * the layout to be displayed
	 */
	private ActorLayout layout;

	/*
	 * the configuartion applied on the layout
	 */
	private final ActorLayoutConfiguration config;
	/*
	 * listener for board events. Responsible for updating the layout and
	 * applying animations
	 */
	private final BoardActorBoardChangeAnimator boardAnimator;
	/*
	 * provides functionality to zoom and pan this actor. Comes with standard
	 * gesture listener implementation
	 */
	private final BoardActorZoomAndPan zoomAndPan;

	/*
	 * provides functionality to add and manage user input listeners for the
	 * BoardObjectActors in the ActorLayout representing the board
	 */
	private BoardActorLayoutEditing layoutEditing;

	/*
	 * dedicated actor to display the game world in. Makes it easy to transform
	 * coordinates with parentToLocal and localToParent
	 */
	private final WorldPane world;

	/*
	 * the x position of the game world's origin relative to this BoardActor's
	 * origin and in this' coordinates/length.
	 */
	private float posX;
	/*
	 * the y position of the game world's origin relative to this BoardActor's
	 * origin and in this' coordinates/length.
	 */
	private float posY;

	private Texture background;
	private Color backgroundColor;

	/*
	 * whether this actor displays the board in color blind mode or not.
	 * Initially set to the value of isColorBlindEnabled of the
	 * ActorLayoutConfiguration given in the constructor
	 */
	private boolean colorBlind;

	private boolean zoomAndPanEnabled = false;

	private boolean layoutEditingEnabled = false;

	/**
	 * Creates a new BoardActor. The actor layout of the board's representation
	 * will be created using the given {@link ActorLayoutConfiguration}.
	 * 
	 * @param b
	 *            the board this {@link BoardActor} will represent
	 * @param config
	 *            an {@link ActorLayoutConfiguration} used for creating the
	 *            actor layout
	 */
	public BoardActor(Board b, ActorLayoutConfiguration config) {
		this.config = config;
		colorBlind = config.isColorBlindEnabled();

		// initialize world pane
		world = new WorldPane(this);
		super.addActor(world);

		// initialize layout by calling onBoardRebuilt. Needs world to add
		// actors to.
		boardAnimator = new BoardActorBoardChangeAnimator(this);
		boardAnimator.onBoardRebuilt(b);

		// initialize zoom and pan. Needs layout to derive limits from.
		zoomAndPan = new BoardActorZoomAndPan(this);
		setZoomAndPanEnabled(true);

		if (!headless) {
			// set transparent but existing background
			background = AssetManager.getInstance().getColorTexture(
					de.croggle.game.Color.uncolored());
			setBackgroundColor(new Color(1.f, 1.f, 1.f, 0.f));
		}

		initializePosition();
	}

	/**
	 * Creates a new BoardActor. This is the simpler version of constructing a
	 * BoardActor, using most of the default {@link ActorLayoutConfiguration}
	 * properties, only requiring the {@link ColorController} to be set
	 * correctly.
	 * 
	 * @param board
	 * @param controller
	 */
	public BoardActor(Board board, ColorController controller) {
		this(board, new ActorLayoutConfiguration()
				.setColorController(controller));
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (clipBegin()) {
			if (batch != null) {
				Color c = batch.getColor();
				Color bc = getBackgroundColor();
				batch.setColor(bc.r, bc.g, bc.b, bc.a * parentAlpha);
				batch.draw(background, getX(), getY(),
						getWidth() * getScaleX(), getHeight() * getScaleY());
				batch.setColor(c);
			}
			super.draw(batch, parentAlpha);
			clipEnd();
		}
	}

	private void initializePosition() {
		final float offsetLeft = 0;
		final float offsetTop = -(getHeight() / 2 - 40);

		// have the tree displayed horizontally centered and with its top
		// offsetTop pixels below (negative = above) the screen mid
		ActorLayoutStatistics stats = layout.getLayoutStatistics();
		Vector2 orig = config.getTreeOrigin();
		float treeMidX = orig.x + stats.getWidthMap().get(layout.getBoard())
				/ 2;
		float treeTop = orig.y;

		zoomAndPan.centerOntoWorldPoint(treeMidX + offsetLeft, treeTop
				+ offsetTop);
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color c) {
		backgroundColor = c;
	}

	/**
	 * Moves (pans) the "world of alligators" (the plane on which the actors are
	 * presented) by the given distances, whereby the lengths are corresponding
	 * to the {@link BoardActor}'s and its parents' scalings.
	 * 
	 * @param deltaX
	 *            the distance to move the "world" to the right, in the same
	 *            unit as you would specify the actor's width in
	 * @param deltaY
	 *            the distance to move the "world" to the bottom, in the same
	 *            unit as you would specify the actor's height in
	 */
	public void panActorDistance(float deltaX, float deltaY) {
		if (zoomAndPan != null) {
			zoomAndPan.panActorCoords(deltaX, deltaY);
		}
	}

	public boolean zoomIn(float percent, float pointX, float pointY) {
		if (zoomAndPan != null) {
			return zoomAndPan.zoomIn(percent, pointX, pointY);
		}
		return false;
	}

	public boolean zoomIn(float percent) {
		return zoomIn(percent, getWidth() / 2, getHeight() / 2);
	}

	public boolean zoomOut(float percent, float pointX, float pointY) {
		if (zoomAndPan != null) {
			return zoomAndPan.zoomOut(percent, pointX, pointY);
		}
		return false;
	}

	public boolean zoomOut(float percent) {
		return zoomOut(percent, getWidth() / 2, getHeight() / 2);
	}

	@Override
	protected void sizeChanged() {
		world.syncBounds();
		if (zoomAndPan != null) {
			zoomAndPan.validate();
		}
		initializePosition();
	}

	public Vector2 boardActorToWorldCoordinates(Vector2 coords) {
		return world.parentToLocalCoordinates(coords);
	}

	public Vector2 worldToBoardActorCoordinates(Vector2 coords) {
		return world.localToParentCoordinates(coords);
	}

	/**
	 * 
	 * @param coords
	 * @param scale
	 *            the scale to be assumed the worldPane has. Useful if you want
	 *            to compare the position of a BoardActor coordinate with
	 *            different scales applied (e.g., before and after zoom)
	 * @return
	 */
	public Vector2 boardActorToWorldCoordinates(Vector2 coords, float scale) {
		return world.parentToLocalCoordinates(coords, scale);
	}

	/**
	 * 
	 * @param coords
	 * @param scale
	 *            the scale to be assumed the worldPane has. Useful if you want
	 *            to compare the position of a WorldPane coordinate with
	 *            different scales applied (e.g., before and after zoom)
	 * @return
	 */
	public Vector2 worldToBoardActorCoordinates(Vector2 coords, float scale) {
		return world.localToParentCoordinates(coords, scale);
	}

	/**
	 * 
	 * @return a {@link BoardEventListener} to provide a means for this
	 *         {@link BoardActor} to be updated if the represented {@link Board}
	 *         changes.
	 */
	public BoardEventListener getBoardEventListener() {
		return boardAnimator;
	}

	public void setColorBlindEnabled(boolean enabled) {
		if (enabled == colorBlind) {
			return;
		} else {
			config.setColorBlindEnabled(enabled);
			colorBlind = enabled;
			for (Actor actor : world.getChildren()) {
				if (actor instanceof ColoredBoardObjectActor) {
					((ColoredBoardObjectActor) actor)
							.setColorBlindEnabled(enabled);
				}
			}
		}
	}

	public void setBackground(Texture bg) {
		background = bg;
	}

	public boolean getColorBlindEnabled() {
		return colorBlind;
	}

	@Override
	public void onSettingChange(Setting setting) {
		if (setting.isColorblindEnabled() != colorBlind) {
			setColorBlindEnabled(setting.isColorblindEnabled());
		}
	}

	/**
	 * Removes all listeners added by the user but will maintain actor managed
	 * listeners like e.g. zoomAndPan
	 */
	@Override
	public void clearListeners() {
		super.clearListeners();
		if (zoomAndPanEnabled) {
			super.addListener(zoomAndPan);
		}
	}

	public boolean isZoomAndPanEnabled() {
		return zoomAndPanEnabled;
	}

	public void setZoomAndPanEnabled(boolean zoomAndPanEnabled) {
		if (zoomAndPanEnabled != this.zoomAndPanEnabled) {
			this.zoomAndPanEnabled = zoomAndPanEnabled;
			if (zoomAndPanEnabled) {
				// zoomAndPan = new BoardActorZoomAndPan(this);
				super.addListener(zoomAndPan);
			} else {
				super.removeListener(zoomAndPan);
			}
		}
	}

	/**
	 * Returns whether or not the editing functionality is enabled for this
	 * {@link BoardActor} or not.
	 * 
	 * @return true if editing is possible for the user, false otherwise
	 */
	public boolean isUserLayoutInteractionEnabled() {
		return layoutEditingEnabled;
	}

	/**
	 * Enables editing functionality of the BoardActor (Color editing and drag
	 * and drop moving)
	 * 
	 * @param m
	 *            messenger to notify other listeners on {@link Board} changes
	 *            made by the user
	 * @param objectBar
	 *            whether to create an object bar or not
	 */
	public void enableLayoutEditing(BoardEventMessenger m, boolean objectBar) {
		if (layoutEditingEnabled == false) {
			// initialize user interaction listeners on layout
			layoutEditing = new BoardActorLayoutEditing(this, m, objectBar);
			layoutEditing.registerLayoutListeners();
			layoutEditingEnabled = true;
		}
	}

	/**
	 * 
	 * 
	 * @return ObjectBar to be used with this BoardActor, or null if layout
	 *         editing i s disabled or no ObjectBar was created at
	 *         enableLayoutEditing
	 */
	public ObjectBar getObjectBar() {
		if (layoutEditing != null) {
			return layoutEditing.getObjectBar();
		} else {
			return null;
		}
	}

	/**
	 * Disables all editing functionality on the actor. This means that it will
	 * also be impossible to {@link #getObjectBar() get the object bar}.
	 */
	public void disableLayoutEditing() {
		layoutEditing.unregisterLayoutListeners();
		layoutEditing = null;
		layoutEditingEnabled = false;
	}

	/**
	 * TODO Sets a listener to be called back when a set of animations has
	 * finished running.
	 * 
	 * @param listener
	 *            The listener to be called back
	 */
	public void setAnimationsFinishedListener(
			AnimationsFinishedListener listener) {
		boardAnimator.setAnimationsFinishedLitener(listener);
	}

	/**
	 * Sets the speed factor of the {@link BoardActor}'s animation system. A
	 * speed value of 2 means double the speed, which causes the animations to
	 * take half the time
	 * 
	 * @param speed
	 *            The speed factor
	 */
	public void setAnimationSpeed(float speed) {
		boardAnimator.setAnimationSpeed(speed);
	}

	/**
	 * Retrieves the width of the board that is represented by this actor from
	 * the BoardActor's {@link ActorLayout}'s
	 * {@link ActorLayout#getLayoutStatistics() statistics} and multiplies it by
	 * the current zoom level of the actor.
	 * 
	 * @return the width that would be needed to show the whole alligator
	 *         constellation at once
	 */
	public float getPreferredWidth() {
		return layout.getLayoutStatistics().getWidthMap()
				.get(layout.getBoard())
				* getZoom();
	}

	/**
	 * Retrieves the height of the board that is represented by this actor from
	 * the BoardActor's {@link ActorLayout}'s
	 * {@link ActorLayout#getLayoutStatistics() statistics} and multiplies it by
	 * the current zoom level of the actor.
	 * 
	 * @return the height that would be needed to show the whole alligator
	 *         constellation at once
	 */
	public float getPreferredHeight() {
		return layout.getLayoutStatistics().getHeightMap()
				.get(layout.getBoard())
				* getZoom();
	}

	@Override
	public Actor hit(float x, float y, boolean touchable) {
		// TODO questionable if necessary. Maybe a good point to throw out
		// superfluous code
		if (touchable && getTouchable() == Touchable.disabled) {
			return null;
		}
		Vector2 point = new Vector2();
		Array<Actor> children = super.getChildren();
		Actor hit;
		// look first for hits NOT being world
		for (int i = children.size - 1; i >= 0; i--) {
			Actor child = children.get(i);
			if (!child.isVisible()) {
				continue;
			}
			child.parentToLocalCoordinates(point.set(x, y));
			hit = child.hit(point.x, point.y, touchable);
			if (hit != null) {
				if (hit == world) {
					continue;
				}
				return hit;
			}
		}

		// consider elements in world as hit targets
		world.parentToLocalCoordinates(point.set(x, y));
		hit = world.hit(point.x, point.y, touchable);
		if (hit != world) {
			return hit;
		}

		// if the actor was hit, but none of the children, return this
		if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
			return this;
		}

		return null;
	}

	void layoutSizeChanged() {
		if (zoomAndPan != null) {
			zoomAndPan.validate();
		}
	}

	/*
	 * Package internal api
	 */

	float getZoom() {
		return world.getScaleX();
	}

	ActorLayout getLayout() {
		return layout;
	}

	void setLayout(ActorLayout layout) {
		this.layout = layout;
	}

	/**
	 * Unsafe zoom method. Used by {@link BoardActorZoomAndPan}. Use
	 * {@link BoardActorZoomAndPan} methods for better safety.
	 * 
	 * @param zoom
	 */
	void setZoom(float zoom) {
		world.setScale(zoom);
	}

	void clearWorld() {
		world.clearChildren();
	}

	void addToWorld(Actor actor) {
		world.addActor(actor);
	}

	boolean removeFromWorld(Actor actor) {
		return world.removeActor(actor);
	}

	void clearActor() {
		super.clearChildren();
		super.addActor(world);
	}

	void addToActor(Actor actor) {
		super.addActor(actor);
	}

	boolean removeFromActor(Actor actor) {
		return super.removeActor(actor);
	}

	void updateListeners() {
		if (layoutEditingEnabled) {
			layoutEditing.unregisterLayoutListeners();
			layoutEditing.registerLayoutListeners();
		}
	}

	void fixLayout() {
		List<BoardObjectActor> added = layout.fix();
		for (BoardObjectActor actor : added) {
			world.addActor(actor);
		}
		layoutSizeChanged();
	}

	void fixLayoutAnimated() {
		if (boardAnimator != null) {
			boardAnimator.fixLayout();
		} else {
			fixLayout();
		}
	}

	ActorLayoutConfiguration getLayoutConfiguration() {
		return config;
	}

	float getWorldX() {
		return posX;
	}

	void setWorldX(float x) {
		posX = x;
	}

	float getWorldY() {
		return posY;
	}

	void setWorldY(float y) {
		posY = y;
	}

	void addLayoutActor(BoardObjectActor a) {
		layout.addActor(a);
		world.addActor(a);
	}

	void removeLayoutActor(BoardObjectActor a) {
		layout.removeActor(a);
		world.removeActor(a);
	}

	// stuff inherited from Group that should not be used as originally intended
	// still performing their tasks as there are some back and forth
	// dependencies in actors
	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void addActor(Actor actor) {
		super.addActor(actor);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void addActorAfter(Actor a, Actor b) {
		super.addActorAfter(a, b);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void addActorAt(int index, Actor actor) {
		super.addActorAt(index, actor);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void addActorBefore(Actor a, Actor b) {
		super.addActorBefore(a, b);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void clearChildren() {

	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public boolean removeActor(Actor a) {
		return super.removeActor(a);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public boolean swapActor(Actor a, Actor b) {
		return super.swapActor(a, b);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public boolean swapActor(int x, int y) {
		return super.swapActor(x, y);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	@Override
	public SnapshotArray<Actor> getChildren() {
		return super.getChildren();
	}

	/**
	 * If set to true, future instantiations of {@link BoardActor} will assume
	 * to run in a headless environment, causing e.g. that no background is
	 * initially set (would need {@link Pixmap}s), preventing teinting using
	 * {@link #setColor(Color)} effectless.
	 * 
	 * @param headless
	 */
	public static void setHeadlessInstantiation(boolean headless) {
		BoardActor.headless = headless;
	}
}
