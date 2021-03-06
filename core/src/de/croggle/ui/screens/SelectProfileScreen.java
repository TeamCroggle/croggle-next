package de.croggle.ui.screens;

import static de.croggle.data.LocalizationHelper._;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import de.croggle.Croggle;
import de.croggle.game.profile.Profile;
import de.croggle.game.profile.ProfileChangeListener;
import de.croggle.game.profile.ProfileController;
import de.croggle.ui.StyleHelper;
import de.croggle.ui.actors.NotificationDialog;

/**
 * Screen within which the player can change the name of his profile. For
 * reference see ``Pflichtenheft 10.5.12 / Abbildung 21''.
 */
public class SelectProfileScreen extends AbstractScreen implements
		ProfileChangeListener {

	private final ProfileController profileController;

	/**
	 * Creates the screen that is shown to the player while changing his
	 * profile.
	 * 
	 * @param game
	 *            the backreference to the central game
	 */
	public SelectProfileScreen(Croggle game) {
		super(game);
		profileController = game.getProfileController();
	}

	@Override
	protected void initializeWidgets() {
		super.initializeWidgets();
		fillTable();
	}

	private void fillTable() {
		StyleHelper helper = StyleHelper.getInstance();

		Table scrollTable = new Table();
		ImageButton newProfile = new ImageButton(
				helper.getImageButtonStyle("widgets/icon-plus"));

		newProfile.addListener(new NewProfileClickListener());

		scrollTable.defaults().minWidth(500).height(100).space(10);

		scrollTable.padTop(30);
		for (Profile profile : profileController.getAllProfiles()) {
			final String avatarPath = profile.getPicturePath();
			// needs to be a TextButton because the label is used later
			TextButton profileButton = new TextButton(profile.getName(),
					helper.getTextButtonStyle());
			Label label = profileButton.getLabel();
			profileButton.clearChildren();
			profileButton.add(new Image(helper.getDrawable(avatarPath)))
					.size(100).pad(10);
			profileButton.add(label).expandX();

			scrollTable.add(profileButton);
			scrollTable.row();
			profileButton.addListener(new ChangeProfileClickListener());
		}

		scrollTable.add(newProfile);

		scrollTable.padBottom(15);
		ScrollPane scrollPane = new ScrollPane(scrollTable);
		table.add(scrollPane).expand().fill();
	}

	@Override
	public void onProfileChange(Profile profile) {
		if (areWidgetsInitialized()) {
			table.clear();
			fillTable();
		}
	}

	private class ChangeProfileClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			TextButton target = (TextButton) event.getListenerActor();
			profileController.changeCurrentProfile(target.getText().toString());
			game.showMainMenuScreen();
		}
	}

	private class NewProfileClickListener extends ClickListener {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			// TODO make that six a constant somewhere
			if (profileController.getAllProfiles().size() == ProfileController.MAX_PROFILE_NUMBER) {
				Dialog infoDialog = new NotificationDialog(
						_("warning_max_profiles"));
				infoDialog.show(stage);
			} else {
				game.showProfileSetNameScreen();
			}
		}
	}

	@Override
	protected void showLogicalPredecessor() {
		if (profileController.getAllProfiles().size() != 0
				&& profileController.getCurrentProfile() != null) {
			game.showMainMenuScreen();
		}
	}
}
