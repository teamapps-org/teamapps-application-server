package org.teamapps.application.server.ux.window;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.icons.Icon;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.toolbar.Toolbar;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.window.Window;
import org.teamapps.ux.session.SessionContext;

public class ApplicationWindow {

	private final Window window;
	private final ApplicationInstanceData applicationInstanceData;
	private Toolbar toolbar;
	private ToolbarButtonGroup currentButtonGroup;
	private ToolbarButton saveButton;
	private ToolbarButton cancelButton;

	public ApplicationWindow(Icon icon, String title, ApplicationInstanceData applicationInstanceData) {
		this.applicationInstanceData = applicationInstanceData;
		this.window = new Window(icon, title, 800, 600, null);
		toolbar = new Toolbar();
		window.setToolbar(toolbar);
		window.setMaximizable(true);
		window.setCloseable(true);
		window.setCloseOnEscape(true);
		currentButtonGroup = toolbar.addButtonGroup(new ToolbarButtonGroup());
	}

	public ToolbarButtonGroup addButtonGroup() {
		currentButtonGroup = new ToolbarButtonGroup();
		toolbar.addButtonGroup(currentButtonGroup);
		return currentButtonGroup;
	}

	public ToolbarButton addButton(Icon icon, String title) {
		ToolbarButton button = ToolbarButton.createTiny(icon, title);
		currentButtonGroup.addButton(button);
		return button;
	}

	public ToolbarButton addSaveButton() {
		return addSaveButton(applicationInstanceData.getLocalized(Dictionary.SAVE_CLOSE), applicationInstanceData.getLocalized(Dictionary.SAVE_AND_CLOSE_WINDOW));
	}

	public ToolbarButton addSaveButton(String title, String description) {
		saveButton = ToolbarButton.create(ApplicationIcons.FLOPPY_DISK, title, description);
		currentButtonGroup.addButton(saveButton);
		return saveButton;
	}

	public ToolbarButton addCancelButton() {
		return addCancelButton(applicationInstanceData.getLocalized(Dictionary.CANCEL_CLOSE), applicationInstanceData.getLocalized(Dictionary.CANCEL_AND_CLOSE_WINDOW));
	}

	public ToolbarButton addCancelButton(String title, String description) {
		cancelButton = ToolbarButton.create(ApplicationIcons.ERROR, title, description);
		currentButtonGroup.addButton(cancelButton);
		cancelButton.onClick.addListener(() -> window.close());
		return cancelButton;
	}

	public void setContent(Component content) {
		window.setContent(content);
	}

	public void setWindowRelativeSize(float relativeWidth, float relativeHeight) {
		int windowWidth = (int) Math.max(800, Math.min(1600, SessionContext.current().getClientInfo().getViewPortWidth() * relativeWidth));
		int windowHeight = (int) Math.max(600, Math.min(1200, SessionContext.current().getClientInfo().getScreenHeight() * relativeHeight));
		window.setSize(windowWidth, windowHeight);

	}

	public void setWindowPreferredSize(int width, int height, float minRelativeMargin) {
		int windowWidth = (int) Math.min(width, SessionContext.current().getClientInfo().getViewPortWidth() * (1 - minRelativeMargin));
		int windowHeight = (int) Math.min(height, SessionContext.current().getClientInfo().getScreenHeight() * (1 - minRelativeMargin));
		window.setSize(windowWidth, windowHeight);
	}

	public void setWindowSize(int width, int height) {
		window.setSize(width, height);
	}

	public void show() {
		window.show(300);
	}

	public void close() {
		window.close();
	}

	public ToolbarButtonGroup getCurrentButtonGroup() {
		return currentButtonGroup;
	}

	public ToolbarButton getSaveButton() {
		return saveButton;
	}

	public ToolbarButton getCancelButton() {
		return cancelButton;
	}
}
