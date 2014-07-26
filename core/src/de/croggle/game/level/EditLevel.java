package de.croggle.game.level;

import java.util.HashMap;
import java.util.List;

import de.croggle.Croggle;
import de.croggle.game.Color;
import de.croggle.game.EditLevelGameController;
import de.croggle.game.board.Board;

public abstract class EditLevel extends Level {

	private final Color[] userColors;
	private final Color[] blockedColors;

	public EditLevel(int levelIndex, int packageIndex, Board initialBoard,
			Board goalBoard, List<String> animationPath, Color[] userColors,
			Color[] blockedColors, String hint, String description,
			int abortSimulationAfter, boolean showObjectBar) {
		super(levelIndex, packageIndex, initialBoard, goalBoard, animationPath,
				hint, description, abortSimulationAfter, showObjectBar);
		this.userColors = userColors;
		this.blockedColors = blockedColors;
	}

	/**
	 * Method to get the userColors of the level.
	 * 
	 * @return the user colors of this level
	 */
	public Color[] getUserColor() {
		return userColors;
	}

	/**
	 * Method to get the blocked colors of the level.
	 * 
	 * @return the blocked colors of this level
	 */
	public Color[] getBlockedColor() {
		return blockedColors;
	}

	@Override
	public EditLevelGameController createGameController(Croggle app) {
		return new EditLevelGameController(app, this);
	}

	@Override
	public boolean isLevelSolved(Board solution, int steps) {
		if ((getAbortSimulationAfter() > 0 && getAbortSimulationAfter() <= steps)
				|| solution.matchWithRecoloring(this.getGoalBoard(),
						new HashMap<Color, Color>())) {
			setSolvedTrue();
			return true;
		} else {
			return false;
		}
	}

}
