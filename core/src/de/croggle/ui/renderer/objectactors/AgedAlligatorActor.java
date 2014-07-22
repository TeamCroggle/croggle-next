package de.croggle.ui.renderer.objectactors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.croggle.backends.BackendHelper;
import de.croggle.data.AssetManager;
import de.croggle.game.board.AgedAlligator;

/**
 * An actor used for representing an aged alligator.
 */
public class AgedAlligatorActor extends BoardObjectActor {

	private TextureRegion foreground;

	/**
	 * Creates a new actor.
	 * 
	 * @param alligator
	 *            the AgedAlligator represented by this actor
	 */
	AgedAlligatorActor(AgedAlligator alligator) {
		super(alligator);
		initialize();
	}

	protected void initialize() {
		AssetManager assetManager = AssetManager.getInstance();

		TextureAtlas tex;
		try {
			tex = assetManager.get(BackendHelper.getAssetDirPath()
					+ "textures/pack.atlas", TextureAtlas.class);
		} catch (GdxRuntimeException ex) {
			throw new IllegalStateException(
					"Could not access atlas containing necessary textures. Make sure it is loaded before instantiating BoardObjectActors.");
		}
		foreground = tex.findRegion("agedalligator/alligator");
		this.setWidth(foreground.getRegionWidth());
		this.setHeight(foreground.getRegionHeight());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void draw(Batch batch, float parentAlpha) {
		Color c = batch.getColor();
		Color n = getColor();
		batch.setColor(n.r, n.g, n.b, n.a * parentAlpha);
		batch.draw(foreground, getX(), getY(), getOriginX(), getOriginY(),
				getWidth(), getHeight(), getScaleX(), getScaleY(),
				getRotation());
		batch.flush();
		batch.setColor(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void act(float delta) {
		super.act(delta);
	}

	@Override
	public AgedAlligator getBoardObject() {
		return (AgedAlligator) super.getBoardObject();
	}
}
