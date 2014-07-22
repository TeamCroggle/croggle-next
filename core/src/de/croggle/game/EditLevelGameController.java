package de.croggle.game;

import de.croggle.Croggle;
import de.croggle.data.persistence.LevelProgress;
import de.croggle.game.board.Board;
import de.croggle.game.level.EditLevel;
import de.croggle.util.convert.AlligatorToJson;
import de.croggle.util.convert.JsonToAlligator;

public class EditLevelGameController extends GameController {
	private final EditLevel level;

	public EditLevelGameController(Croggle app, EditLevel level) {
		super(app, level);
		this.level = level;
		setupColorController();
	}

	@Override
	protected void loadBoard(LevelProgress progress) {
		final String serializedBoard = progress.getCurrentBoard();
		if (serializedBoard == null) {
			return;
		}
		try {
			final Board previousBoard = JsonToAlligator
					.convertBoard(serializedBoard);
			setUserBoard(previousBoard);
		} catch (IllegalArgumentException e) {
			progress.setCurrentBoard("");
			app.getPersistenceManager().saveLevelProgress(
					app.getProfileController().getCurrentProfileName(),
					progress);
		}
	}

	@Override
	protected void convertBoard(final LevelProgress progress) {
		final Board boardCopy = getUserBoard().copy();
		Thread jsonConverter = new Thread() {
			@Override
			public void run() {
				synchronized (progress) {
					progress.setCurrentBoard(AlligatorToJson.convert(boardCopy));
				}
			}
		};
		jsonConverter.start();
	}

	@Override
	protected ColorController createColorController() {
		if (level == null) {
			// call from super constructor, ignore this
			return null;
		}
		final ColorController colorController = new ColorController();
		for (Color color : level.getUserColor()) {
			colorController.addUsableColor(color);
		}
		for (Color color : level.getBlockedColor()) {
			colorController.addBlockedColor(color);
		}
		return colorController;
	}

}
