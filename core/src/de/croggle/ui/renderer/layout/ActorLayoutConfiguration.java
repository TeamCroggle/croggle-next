package de.croggle.ui.renderer.layout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.math.Vector2;

import de.croggle.backends.BackendHelper;
import de.croggle.game.ColorController;
import de.croggle.ui.renderer.objectactors.AgedAlligatorActor;
import de.croggle.ui.renderer.objectactors.BoardObjectActor;
import de.croggle.ui.renderer.objectactors.ColoredAlligatorActor;
import de.croggle.ui.renderer.objectactors.EggActor;

/**
 * A class to accumulate all different properties to be taken into consideration
 * when layouting a tree of {@link BoardObjectActor}s.
 * 
 * WARNING: Layouter code will assume that the given widths and heights are
 * directed the same direction as the respective TreeGrowths are set. That
 * means, while rendering the drawn textures should extend in the direction the
 * TreeGrowths are set, especially in horizontal direction.
 */
public class ActorLayoutConfiguration {
	private static boolean aspectRatiosInitialized = false;
	private static float eggAspectRatio = 0;
	private static float agedAlligatorAspectRatio = 0;
	private static float coloredAlligatorAspectRatio = 0;

	private boolean colorBlindEnabled;
	private TreeGrowth horizontalGrowth;
	private TreeGrowth verticalGrowth;
	private TreeGrowth renderDirectionX;
	private TreeGrowth renderDirectionY;
	private ColorController colorController;
	private float verticalScaleFactor;
	private float uniformObjectWidth;
	private float uniformObjectHeight;
	private Vector2 treeOrigin;
	private float horizontalPadding;
	private float verticalPadding;

	private float eggWidth;
	private float eggHeight;
	private float agedAlligatorWidth;
	private float agedAlligatorHeight;
	private float coloredAlligatorWidth;
	private float coloredAlligatorHeight;

	/**
	 * 
	 */
	public ActorLayoutConfiguration() {
		treeOrigin = new Vector2(0, 0);
		colorController = null;
		verticalScaleFactor = .75f;
		colorBlindEnabled = false;

		horizontalGrowth = TreeGrowth.NEG_POS;
		verticalGrowth = TreeGrowth.POS_NEG;

		renderDirectionX = TreeGrowth.NEG_POS;
		renderDirectionY = TreeGrowth.NEG_POS;

		horizontalPadding = 0;
		verticalPadding = 0;

		initializeAspectRatios();

		eggWidth = 100;
		eggHeight = eggWidth / eggAspectRatio;

		agedAlligatorWidth = 150;
		agedAlligatorHeight = agedAlligatorWidth / agedAlligatorAspectRatio;

		coloredAlligatorWidth = agedAlligatorWidth;
		coloredAlligatorHeight = coloredAlligatorWidth
				/ coloredAlligatorAspectRatio;

		uniformObjectWidth = Math.max(eggWidth,
				Math.max(agedAlligatorWidth, coloredAlligatorWidth));
		uniformObjectHeight = Math.max(eggHeight,
				Math.max(agedAlligatorHeight, coloredAlligatorHeight));
	}

	/**
	 * 
	 * @param treeOrigin
	 *            the initial position from which to grow, not to be mixed up
	 *            with the root's coordinates
	 * @param horizontalGrowth
	 *            the direction the tree will grow in horizontally
	 * @param verticalGrowth
	 *            the direction the tree will grow in vertically
	 * @param renderDirectionX
	 *            the horizontal direction the layouted actors will be drawn
	 *            into (usually NEG_POS)
	 * @param renderDirectionY
	 *            the vertical direction the layouted actors will be drawn into
	 *            (usually NEG_POS)
	 * @param verticalScaleFactor
	 *            the relative size of a child as compared to its parent.
	 * @param horizontalPadding
	 *            the number of pixels padding between two horizontally
	 *            neighboring actors.
	 * 
	 * @param verticalPadding
	 *            the number of pixels padding between two vertically
	 *            neighboring actors.
	 * @param controller
	 *            the {@link ColorController} used to perform color lookups, so
	 *            the layout will be rendered correctly
	 * @param colorBlindEnabled
	 *            whether the actors created to be used in the respective
	 *            {@link ActorLayout}s will have color blind mode enabled
	 *            initially or not
	 * @param eggWidth
	 *            the actual width to be set on {@link EggActor}s in this layout
	 * @param eggHeight
	 *            the actual height to be set on {@link EggActor}s in this
	 *            layout
	 * @param agedAlligatorWidth
	 *            the actual width to be set on {@link AgedAlligatorActor}s in
	 *            this layout
	 * @param agedAlligatorHeight
	 *            the actual height to be set on {@link AgedAlligatorActor}s in
	 *            this layout
	 * @param coloredAlligatorWidth
	 *            the actual width to be set on {@link ColoredAlligatorActor}s
	 *            in this layout
	 * @param coloredAlligatorHeight
	 *            the actual height to be set on {@link ColoredAlligatorActor}s
	 *            in this layout
	 */
	public ActorLayoutConfiguration(Vector2 treeOrigin,
			TreeGrowth horizontalGrowth, TreeGrowth verticalGrowth,
			TreeGrowth renderDirectionX, TreeGrowth renderDirectionY,
			float verticalScaleFactor, float horizontalPadding,
			float verticalPadding, ColorController controller,
			boolean colorBlindEnabled, float eggWidth, float eggHeight,
			float agedAlligatorWidth, float agedAlligatorHeight,
			float coloredAlligatorWidth, float coloredAlligatorHeight) {
		this.treeOrigin = treeOrigin;
		colorController = controller;
		this.verticalScaleFactor = verticalScaleFactor;
		this.colorBlindEnabled = colorBlindEnabled;

		this.horizontalGrowth = horizontalGrowth;
		this.verticalGrowth = verticalGrowth;

		this.renderDirectionX = renderDirectionX;
		this.renderDirectionY = renderDirectionY;

		this.horizontalPadding = horizontalPadding;
		this.verticalPadding = verticalPadding;

		this.eggWidth = eggWidth;
		this.eggHeight = eggHeight;

		this.agedAlligatorWidth = agedAlligatorWidth;
		this.agedAlligatorHeight = agedAlligatorHeight;

		this.coloredAlligatorWidth = coloredAlligatorWidth;
		this.coloredAlligatorHeight = coloredAlligatorHeight;

		uniformObjectWidth = Math.max(
				Math.max(eggWidth, coloredAlligatorWidth), agedAlligatorWidth);
		uniformObjectHeight = Math.max(
				Math.max(eggHeight, coloredAlligatorHeight),
				agedAlligatorHeight);
	}

	private static void initializeAspectRatios() {
		if (!aspectRatiosInitialized) {
			TextureAtlasData data = new TextureAtlasData(
					Gdx.files.internal(BackendHelper.getAssetDirPath()
							+ "textures/pack.atlas"),
					Gdx.files.internal(BackendHelper.getAssetDirPath()
							+ "textures"), false);
			boolean foundEgg = false;
			boolean foundAged = false;
			boolean foundColored = false;
			for (TextureAtlasData.Region region : data.getRegions()) {
				if (region.name.equals("agedalligator/alligator")) {
					foundAged = true;
					agedAlligatorAspectRatio = region.width
							/ (float) region.height;
				} else if (region.name.equals("coloredalligator/foreground")) {
					foundColored = true;
					coloredAlligatorAspectRatio = region.width
							/ (float) region.height;
				} else if (region.name.equals("egg/foreground")) {
					foundEgg = true;
					eggAspectRatio = region.width / (float) region.height;
				}
				if (foundEgg && foundColored && foundAged) {
					break;
				}
			}

			aspectRatiosInitialized = true;
		}
	}

	/**
	 * 
	 * @return The aspect ratio width/height of the egg foreground texture
	 */
	public static float getDefaultEggAspectRatio() {
		initializeAspectRatios();
		return eggAspectRatio;
	}

	/**
	 * 
	 * @return The aspect ratio width/height of the aged alligator texture
	 */
	public static float getDefaultAgedAlligatorAspectRatio() {
		initializeAspectRatios();
		return agedAlligatorAspectRatio;
	}

	/**
	 * 
	 * @return The aspect ratio width/height of the colored alligator foreground
	 *         texture
	 */
	public static float getDefaultColoredALligatorAspectRatio() {
		initializeAspectRatios();
		return coloredAlligatorAspectRatio;
	}

	/**
	 * 
	 * @return the horizontal growth direction of the layout tree
	 */
	public TreeGrowth getHorizontalGrowth() {
		return horizontalGrowth;
	}

	/**
	 * 
	 * @param horizontalGrowth
	 *            the horizontal growth direction of the layout tree to be set
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setHorizontalGrowth(
			TreeGrowth horizontalGrowth) {
		this.horizontalGrowth = horizontalGrowth;
		return this;
	}

	/**
	 * 
	 * @return the direction into which a tree will "grow" while being layouted
	 */
	public TreeGrowth getVerticalGrowth() {
		return verticalGrowth;
	}

	/**
	 * 
	 * @param verticalGrowth
	 *            the vertical growth direction of the layout tree to be set
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setVerticalGrowth(TreeGrowth verticalGrowth) {
		this.verticalGrowth = verticalGrowth;
		return this;
	}

	/**
	 * 
	 * @return the {@link ColorController} that is used by the
	 *         {@link BoardObjectActor}s for transfering game internal
	 *         {@link de.croggle.game.Color color names} into renderable
	 *         {@link com.badlogic.gdx.graphics.Color color}s
	 */
	public ColorController getColorController() {
		return colorController;
	}

	/**
	 * 
	 * @param colorController
	 *            a {@link ColorController} to be used for transfering game
	 *            internal {@link de.croggle.game.Color color names} into
	 *            renderable {@link com.badlogic.gdx.graphics.Color color}s
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setColorController(
			ColorController colorController) {
		this.colorController = colorController;
		return this;
	}

	/**
	 * 
	 * @return the relative edge size of a child {@link BoardObjectActor actor}
	 *         as compared to its parent
	 */
	public float getVerticalScaleFactor() {
		return verticalScaleFactor;
	}

	/**
	 * 
	 * @param verticaleScaleFactor
	 *            a factor to scale scale descendant {@link BoardObjectActor
	 *            actors} relative to their direct parents
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setVerticalScaleFactor(
			float verticaleScaleFactor) {
		verticalScaleFactor = verticaleScaleFactor;
		return this;
	}

	/**
	 * 
	 * @return the width to be assumed every node in the layout tree has
	 */
	public float getUniformObjectWidth() {
		return uniformObjectWidth;
	}

	/**
	 * This method is not part of the api as the uniform size is calculated
	 * using all actor specific bounds
	 * 
	 * @param originalObjectWidth
	 *            new value for the width to be assumed every node in the layout
	 *            tree has
	 */
	/*
	 * public void setUniformObjectWidth(float originalObjectWidth) {
	 * this.uniformObjectWidth = originalObjectWidth; }
	 */

	/**
	 * 
	 * @return the height to be assumed every node in the layout tree has
	 */
	public float getUniformObjectHeight() {
		return uniformObjectHeight;
	}

	/**
	 * This method is not part of the api as the uniform size is calculated
	 * using all actor specific bounds
	 * 
	 * @param originalObjectHeight
	 */
	/*
	 * public ActorLayoutConfiguration setUniformObjectHeight(float
	 * originalObjectHeight) { this.uniformObjectHeight = originalObjectHeight;
	 * return this; }
	 */

	/**
	 * 
	 * @return
	 */
	public Vector2 getTreeOrigin() {
		return treeOrigin;
	}

	/**
	 * 
	 * @param treeOrigin
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setTreeOrigin(Vector2 treeOrigin) {
		this.treeOrigin = treeOrigin;
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getHorizontalPadding() {
		return horizontalPadding;
	}

	/**
	 * 
	 * @param horizontalPadding
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setHorizontalPadding(float horizontalPadding) {
		this.horizontalPadding = horizontalPadding;
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getVerticalPadding() {
		return verticalPadding;
	}

	/**
	 * 
	 * @param verticalPadding
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setVerticalPadding(float verticalPadding) {
		this.verticalPadding = verticalPadding;
		return this;
	}

	private void newWidth(float width) {
		uniformObjectWidth = Math.max(width, uniformObjectWidth);
	}

	private void newHeight(float height) {
		uniformObjectHeight = Math.max(height, uniformObjectHeight);
	}

	/**
	 * 
	 * @return
	 */
	public float getEggWidth() {
		return eggWidth;
	}

	/**
	 * 
	 * @param eggWidth
	 */
	public ActorLayoutConfiguration setEggWidth(float eggWidth) {
		this.eggWidth = eggWidth;
		newWidth(eggWidth);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getEggHeight() {
		return eggHeight;
	}

	/**
	 * 
	 * @param eggHeight
	 */
	public ActorLayoutConfiguration setEggHeight(float eggHeight) {
		this.eggHeight = eggHeight;
		newHeight(eggHeight);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getAgedAlligatorWidth() {
		return agedAlligatorWidth;
	}

	/**
	 * 
	 * @param agedAlligatorWidth
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setAgedAlligatorWidth(
			float agedAlligatorWidth) {
		this.agedAlligatorWidth = agedAlligatorWidth;
		newWidth(agedAlligatorWidth);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getAgedAlligatorHeight() {
		return agedAlligatorHeight;
	}

	/**
	 * 
	 * @param agedAlligatorHeight
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setAgedAlligatorHeight(
			float agedAlligatorHeight) {
		this.agedAlligatorHeight = agedAlligatorHeight;
		newHeight(agedAlligatorHeight);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getColoredAlligatorWidth() {
		return coloredAlligatorWidth;
	}

	/**
	 * 
	 * @param coloredAlligatorWidth
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setColoredAlligatorWidth(
			float coloredAlligatorWidth) {
		this.coloredAlligatorWidth = coloredAlligatorWidth;
		newWidth(coloredAlligatorWidth);
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public float getColoredAlligatorHeight() {
		return coloredAlligatorHeight;
	}

	/**
	 * 
	 * @param coloredAlligatorHeight
	 * @return this {@link ActorLayoutConfiguration} object, to allow for
	 *         chaining setter calls
	 */
	public ActorLayoutConfiguration setColoredAlligatorHeight(
			float coloredAlligatorHeight) {
		this.coloredAlligatorHeight = coloredAlligatorHeight;
		newHeight(coloredAlligatorHeight);
		return this;
	}

	public TreeGrowth getRenderDirectionX() {
		return renderDirectionX;
	}

	public void setRenderDirectionX(TreeGrowth renderDirectionX) {
		this.renderDirectionX = renderDirectionX;
	}

	public TreeGrowth getRenderDirectionY() {
		return renderDirectionY;
	}

	public void setRenderDirectionY(TreeGrowth renderDirectionY) {
		this.renderDirectionY = renderDirectionY;
	}

	public boolean isColorBlindEnabled() {
		return colorBlindEnabled;
	}

	public void setColorBlindEnabled(boolean colorBlindEnabled) {
		this.colorBlindEnabled = colorBlindEnabled;
	}
}
