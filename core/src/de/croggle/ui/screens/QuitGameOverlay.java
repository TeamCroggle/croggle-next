package de.croggle.ui.screens;

import static de.croggle.data.LocalizationHelper._;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.croggle.Croggle;
import de.croggle.ui.ConfirmInterface;
import de.croggle.ui.actors.YesNoDialog;

public class QuitGameOverlay implements Screen {
	private ShapeRenderer shapes;
	private final Color shade;
	private Screen screenBelow;
	private Stage stage;
	private final Table table;
	private final OrthographicCamera camera;
	private final Croggle game;
	private InputMultiplexer inputMediator;

	public QuitGameOverlay(Croggle game) {
		this.game = game;
		this.screenBelow = null;
		shade = new Color(0, 0, 0, .5f);
		table = new Table();
		table.setFillParent(true);
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1024, 600);
	}

	public void setOverlayedScreen(Screen s) {
		this.screenBelow = s;
	}

	@Override
	public void render(float delta) {
		if (screenBelow != null) {
			screenBelow.render(delta);
		}

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapes.setProjectionMatrix(camera.combined);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(shade);
		shapes.rect(0, 0, camera.viewportWidth, camera.viewportHeight);
		shapes.end();
		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		// TODO this is probably superfluous
		stage.getViewport().setWorldSize(1024, 600);
		camera.update();
	}

	@Override
	public void show() {
		Viewport vp = new com.badlogic.gdx.utils.viewport.ScalingViewport(
				Scaling.stretchX, 1024, 600);
		vp.setCamera(camera);
		stage = new Stage(vp, game.batch);
		stage.addActor(table);
		shapes = new ShapeRenderer();
		inputMediator = new InputMultiplexer(stage, new BackButtonHandler());
		// make the screen as well as the stage an input processor
		Gdx.input.setInputProcessor(inputMediator);
		camera.update();

		Dialog quitDialog = new YesNoDialog(_("quit_game_prompt"),
				new ConfirmInterface() {

					@Override
					public void yes() {
						// do whatever needs to be disposed on exit, exit() only
						// closes the activity
						Gdx.app.exit();
					}

					@Override
					public void no() {
						hide();
						if (screenBelow != null) {
							game.setScreen(screenBelow);
						} else {
							throw new IllegalStateException(
									"Don't know where to return to if no screen is set as OverlayedScreen");
						}
					}
				});
		quitDialog.show(stage);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		stage.dispose();
		shapes.dispose();
	}

	private class BackButtonHandler extends InputAdapter {
		@Override
		public boolean keyUp(int keycode) {
			if (keycode == Keys.BACK) {
				if (screenBelow != null) {
					game.setScreen(screenBelow);
					return true;
				} else {
					throw new IllegalStateException(
							"Don't know where to return to if no screen is set as OverlayedScreen");
				}
			}
			return false;
		}
	}
}
