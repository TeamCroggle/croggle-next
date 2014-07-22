package de.croggle.game;

import com.badlogic.gdx.Screen;

import de.croggle.Croggle;
import de.croggle.game.level.MultipleChoiceLevel;
import de.croggle.ui.screens.MultipleChoiceScreen;

public class MultipleChoiceGameController extends GameController {
	private final MultipleChoiceLevel level;
	private int selection;

	public MultipleChoiceGameController(Croggle app,
			MultipleChoiceLevel level) {
		super(app, level);
		this.level = level;
	}

	public void setSelection(int selection) {
		this.selection = selection;

	}

	@Override
	public Screen createPlacementScreen(Croggle app) {
		return new MultipleChoiceScreen(app, this);
	}

	@Override
	protected boolean isLevelSolved() {
		return super.isLevelSolved() && level.validateAnswer(selection);
	}
}
