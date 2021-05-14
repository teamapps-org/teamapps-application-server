/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2021 TeamApps.org
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package org.teamapps.application.server.controlcenter.users;

import org.apache.commons.io.IOUtils;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.application.perspective.PerspectiveMenuPanel;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.api.ui.FormMetaFields;
import org.teamapps.application.server.controlcenter.roles.UserRoleAssignmentPerspectiveBuilder;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.passwordhash.SecurePasswordHash;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.system.utils.ValueConverterUtils;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.server.ui.address.AddressForm;
import org.teamapps.application.ux.combo.ComboBoxUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.dialogue.FormDialogue;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.PasswordField;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.field.upload.PictureChooser;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.resource.ByteArrayResource;
import org.teamapps.ux.resource.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsersPerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<User> selectedUser = TwoWayBindableValue.create();
	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;


	public UsersPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		UsersPerspectiveBuilder usersPerspectiveBuilder = new UsersPerspectiveBuilder();
		PerspectiveMenuPanel menuPanel = PerspectiveMenuPanel.createMenuPanel(getApplicationInstanceData(),
				usersPerspectiveBuilder,
				new UserRoleAssignmentPerspectiveBuilder()
		);

		menuPanel.addInstantiatedPerspective(usersPerspectiveBuilder, this);
		setPerspectiveMenuPanel(menuPanel.getComponent());

		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.USERS_CROWD, getLocalized("users.users"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.USER, getLocalized("users.user"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<User> userModelBuilder = new EntityModelBuilder<>(() -> User.filter(), getApplicationInstanceData());
		userModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("users.users"));
		userModelBuilder.attachSearchField(masterView);
		userModelBuilder.onSelectedRecordChanged.addListener(selectedUser::set);
		Table<User> table = userModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(32);
		table.setStripedRows(false);
		userModelBuilder.updateModels();


		TemplateField<User> userTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createUserPropertyProvider(getApplicationInstanceData()));
		TemplateField<UserAccountStatus> accountStatusTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, createAccountStatusPropertyProvider());
		InstantDateTimeField lastLogin = new InstantDateTimeField();
		TagComboBox<Language> languagesTableField = Language.createTagComboBox(getApplicationInstanceData());
		TextField rolesTableField = new TextField();

		table.addColumn(new TableColumn<User>(User.FIELD_LAST_NAME, getLocalized("users.user"), userTableField).setDefaultWidth(250));
		table.addColumn(new TableColumn<User>(User.FIELD_USER_ACCOUNT_STATUS, getLocalized("users.accountStatus"), accountStatusTableField).setDefaultWidth(120));
		table.addColumn(new TableColumn<User>(User.FIELD_LAST_LOGIN, getLocalized("users.lastLogin"), lastLogin).setDefaultWidth(200));
		table.addColumn(new TableColumn<User>(User.FIELD_LANGUAGES, getLocalized("users.languages"), languagesTableField).setDefaultWidth(350));
		table.addColumn(new TableColumn<User>(User.FIELD_ROLE_ASSIGNMENTS, getLocalized("users.roles"), rolesTableField).setDefaultWidth(300));

		table.setPropertyExtractor((user, propertyName) -> switch (propertyName) {
			case User.FIELD_LAST_NAME -> user;
			case User.FIELD_USER_ACCOUNT_STATUS -> user.getUserAccountStatus();
			case User.FIELD_LAST_LOGIN -> user.getLastLogin();
			case User.FIELD_LANGUAGES -> getLanguages(user.getLanguages());
			case User.FIELD_ROLE_ASSIGNMENTS -> getRolesString(user.getRoleAssignments(), 5);
			default -> null;
		});
		masterView.setComponent(table);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton updatePasswordButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.KEYS, getLocalized(Dictionary.RESET_PASSWORD)));

		PictureChooser pictureChooser = new PictureChooser();
		pictureChooser.setImageDisplaySize(120, 120);
		pictureChooser.setTargetImageSize(240, 240);
		//pictureChooser.setBrowseButtonIcon(ApplicationIcons.UPLOAD);
		pictureChooser.setMaxFileSize(5_000_000);

		TextField firstNameField = new TextField();
		TextField lastNameField = new TextField();
		TagComboBox<Language> languagesField = Language.createTagComboBox(getApplicationInstanceData());
		languagesField.setWrappingMode(TagBoxWrappingMode.SINGLE_TAG_PER_LINE);
		languagesField.setShowClearButton(true);
		TextField emailField = new TextField();
		TextField mobileField = new TextField();
		TextField loginField = new TextField();
		PasswordField passwordField = new PasswordField();
		ComboBox<UserAccountStatus> accountStatusComboBox = createAccountStatusComboBox();
		TagComboBox<UserRoleAssignment> userRoleAssignmentTagCombo = createUserRoleAssignmentTagCombo();
		ComboBox<OrganizationUnit> orgUnitComboBox = OrganizationUtils.createOrganizationComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, OrganizationUnit.getAll(), getApplicationInstanceData());


		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("users.profilePicture"), pictureChooser);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.FIRST_NAME), firstNameField);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.LAST_NAME), lastNameField);
		formLayout.addLabelAndField(null, getLocalized("users.languages"), languagesField);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.E_MAIL), emailField);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.MOBILE_NUMBER), mobileField);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.USER_NAME), loginField);
		formLayout.addLabelAndField(null, getLocalized("users.accountStatus"), accountStatusComboBox);
		formLayout.addLabelAndField(null, getLocalized("users.roles"), userRoleAssignmentTagCombo);
		formLayout.addLabelAndField(null, getLocalized("users.organizationUnit"), orgUnitComboBox);

		AddressForm addressForm = new AddressForm(getApplicationInstanceData());
		addressForm.createAddressSection(formLayout);
		addressForm.addFields(formLayout);

		FormMetaFields formMetaFields = getApplicationInstanceData().getComponentFactory().createFormMetaFields();
		formMetaFields.addMetaFields(formLayout, false);
		selectedUser.onChanged().addListener(formMetaFields::updateEntity);

		detailView.setComponent(form);

		addButton.onClick.addListener(() -> selectedUser.set(User.create().setAddress(Address.create()).setUserAccountStatus(UserAccountStatus.ACTIVE)));

		updatePasswordButton.onClick.addListener(() -> {
			User user = selectedUser.get();
			if (user != null) {
				showUpdatePasswordDialogue(user);
			}
		});

		saveButton.onClick.addListener(() -> {
			User user = selectedUser.get();
			OrganizationUnit organizationUnit = orgUnitComboBox.getValue();
			if (organizationUnit != null && organizationUnit.getType().getAllowUserContainer()) {
				if (organizationUnit.getUserContainer() == null) {
					UserContainer.create().setOrganizationUnit(organizationUnit).save();
				}
			} else {
				UiUtils.showNotification(ApplicationIcons.ERROR, getLocalized("users.wrongOrMissingOrgUnit"));
				return;
			}
			if (!addressForm.validateAddress()) {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
				return;
			}
			if (organizationUnit != null && organizationUnit.getUserContainer() != null && user != null && firstNameField.getValue() != null && lastNameField.getValue() != null) {
				byte[] picture = readUserPicture(pictureChooser);
				user
						.setFirstName(firstNameField.getValue())
						.setLastName(lastNameField.getValue())
						.setLanguages(getCompressedLanguages(languagesField.getValue()))
						.setEmail(emailField.getValue())
						.setMobile(mobileField.getValue())
						.setLogin(loginField.getValue())
						.setUserAccountStatus(accountStatusComboBox.getValue())
						.setAddress(addressForm.getAddress())
						.setContainer(organizationUnit.getUserContainer())
				;
				if (picture != null && picture.length != user.getProfilePictureLength()) {
					user.setProfilePicture(picture);
				}
				user.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				userModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedUser.onChanged().addListener(user -> {
			pictureChooser.setValue(user.getProfilePicture() != null ? new ByteArrayResource(user.getProfilePicture(), "image.jpg") : null);
			firstNameField.setValue(user.getFirstName());
			lastNameField.setValue(user.getLastName());
			languagesField.setValue(getLanguages(user.getLanguages()));
			emailField.setValue(user.getEmail());
			mobileField.setValue(user.getMobile());
			loginField.setValue(user.getLogin());
			passwordField.setValue(user.getPassword());
			accountStatusComboBox.setValue(user.getUserAccountStatus());
			userRoleAssignmentTagCombo.setValue(user.getRoleAssignments());
			orgUnitComboBox.setValue(user.getContainer() != null ? user.getContainer().getOrganizationUnit() : null);

			addressForm.setAddress(user.getAddress());
		});

		selectedUser.set(User.create().setAddress(Address.create()).setUserAccountStatus(UserAccountStatus.ACTIVE));

	}

	private void showUpdatePasswordDialogue(User user) {
		FormDialogue formDialogue = FormDialogue.create(ApplicationIcons.KEYS, getLocalized(Dictionary.RESET_PASSWORD), getLocalized(Dictionary.RESET_PASSWORD));
		PasswordField passwordField = new PasswordField();
		formDialogue.addField(ApplicationIcons.KEYS, getLocalized(Dictionary.PASSWORD), passwordField);
		formDialogue.addOkCancelButtons(getLocalized(Dictionary.O_K), getLocalized(Dictionary.CANCEL));
		formDialogue.onOk.addListener(() -> {
			String value = passwordField.getValue();
			if (value.length() > 8) {
				user.setPassword(SecurePasswordHash.createDefault().createSecureHash(value)).save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				formDialogue.close();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});
		formDialogue.setCloseable(true);
		formDialogue.setCloseOnEscape(true);
		formDialogue.setMaximizable(true);
		formDialogue.show();
	}


	private byte[] readUserPicture(PictureChooser pictureChooser) {
		if (pictureChooser.getValue() != null) {
			Resource value = pictureChooser.getValue();
			try {
				return IOUtils.readFully(value.getInputStream(), (int) value.getLength());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public String getRolesString(List<UserRoleAssignment> assignments, int limit) {
		return assignments.stream()
				.map(assignment -> getLocalized(assignment.getRole().getTitle()))
				.limit(limit)
				.collect(Collectors.joining(", "));
	}

	public List<Language> getLanguages(String compressedValue) {
		return ValueConverterUtils.decompressToStringList(compressedValue)
				.stream()
				.map(Language::getLanguageByIsoCode)
				.collect(Collectors.toList());
	}

	public String getCompressedLanguages(List<Language> languages) {
		return ValueConverterUtils.compressStringList(
				languages.stream()
						.map(Language::getIsoCode)
						.collect(Collectors.toList())
		);
	}

	private ComboBox<UserAccountStatus> createAccountStatusComboBox() {
		return ComboBoxUtils.createRecordComboBox(Arrays.asList(UserAccountStatus.values()), createAccountStatusPropertyProvider(), BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
	}

	private TagComboBox<UserRoleAssignment> createUserRoleAssignmentTagCombo() {
		TagComboBox<UserRoleAssignment> tagComboBox = UiUtils.createTagComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, PropertyProviders.createUserRoleAssignmentPropertyProviderNoUserDisplay(userSessionData));
		tagComboBox.setEditingMode(FieldEditingMode.READONLY);
		return tagComboBox;
	}

	private PropertyProvider<UserAccountStatus> createAccountStatusPropertyProvider() {
		return (userAccountStatus, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, getAccountStatusIcon(userAccountStatus));
			map.put(BaseTemplate.PROPERTY_CAPTION, getLocalized("users.accountStatus." + userAccountStatus.name()));
			return map;
		};
	}

	private Icon getAccountStatusIcon(UserAccountStatus status) {
		return switch (status) {
			case ACTIVE -> ApplicationIcons.OK;
			case INACTIVE -> ApplicationIcons.ERROR;
			case SUPER_ADMIN -> ApplicationIcons.PILOT;
		};
	}
}

