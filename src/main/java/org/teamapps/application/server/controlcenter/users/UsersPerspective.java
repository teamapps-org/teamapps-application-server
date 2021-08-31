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
import org.teamapps.application.api.application.perspective.AbstractPerspectiveBuilder;
import org.teamapps.application.api.application.perspective.PerspectiveMenuPanel;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.Language;
import org.teamapps.application.api.privilege.Privilege;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.controlcenter.Privileges;
import org.teamapps.application.server.controlcenter.roles.UserRoleAssignmentPerspectiveBuilder;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.passwordhash.SecurePasswordHash;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.system.utils.ValueConverterUtils;
import org.teamapps.application.server.ui.address.AddressForm;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.combo.ComboBoxUtils;
import org.teamapps.application.ux.form.FormController;
import org.teamapps.application.ux.view.MasterDetailController;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.pojo.Entity;
import org.teamapps.universaldb.pojo.Query;
import org.teamapps.ux.component.dialogue.FormDialogue;
import org.teamapps.ux.component.field.*;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagBoxWrappingMode;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.field.upload.PictureChooser;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.format.Spacing;
import org.teamapps.ux.component.itemview.SimpleItemView;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.resource.ByteArrayResource;
import org.teamapps.ux.resource.Resource;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UsersPerspective extends AbstractManagedApplicationPerspective {

	private final UserSessionData userSessionData;


	public UsersPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
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
		SimpleItemView<AbstractPerspectiveBuilder> buttonMenu = menuPanel.getButtonMenu();
		setPerspectiveMenuPanel(menuPanel.getComponent(), buttonMenu);

		List<OrganizationUnitView> allowedUnits = getAllowedUnits(Privileges.USERS_PERSPECTIVE, Privilege.READ);
		Set<Integer> unitIdSet = allowedUnits.stream().map(Entity::getId).collect(Collectors.toSet());

		Supplier<Query<User>> querySupplier = () -> User.filter().customFilter(user -> user.getOrganizationUnit() == null || unitIdSet.contains(user.getOrganizationUnit().getId()));
		MasterDetailController<User> masterDetailController = new MasterDetailController<>(ApplicationIcons.USERS_CROWD, getLocalized("users.users"), getApplicationInstanceData(), querySupplier, Privileges.USERS_PERSPECTIVE);
		EntityModelBuilder<User> entityModelBuilder = masterDetailController.getEntityModelBuilder();
		FormController<User> formController = masterDetailController.getFormController();
		ResponsiveForm<User> form = masterDetailController.getResponsiveForm();

		Table<User> table = entityModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(32);
		table.setStripedRows(false);
		entityModelBuilder.updateModels();

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


		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);


		ToolbarButtonGroup buttonGroup = new ToolbarButtonGroup();
		ToolbarButton updatePasswordButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.KEYS, getLocalized(Dictionary.RESET_PASSWORD)));
		formController.addToolbarButtonGroup(buttonGroup);

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
		AbstractField<OrganizationUnitView> organizationUnitViewField = formController.getOrganizationUnitViewField();

		userRoleAssignmentTagCombo.setMargin(new Spacing(0, 5, 0, -4));

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
		formLayout.addLabelAndField(null, getLocalized("users.organizationUnit"), organizationUnitViewField);

		AddressForm addressForm = new AddressForm(getApplicationInstanceData());
		addressForm.createAddressSection(formLayout);
		addressForm.addFields(formLayout);

		formController.addNotBlank(firstNameField);
		formController.addNotBlank(lastNameField);
		formController.addEmailOrEmpty(emailField);
		formController.addPhoneOrEmptyNumber(mobileField);
		formController.addMinCharactersOrEmpty(loginField, 2);
		formController.addMinCharactersOrEmpty(passwordField, 9);
		formController.addNotEmptyList(languagesField);
		formController.addValidator(organizationUnitViewField, unit -> unit != null && OrganizationUtils.convert(unit).getType().isAllowUsers() ? null : getLocalized("users.wrongOrMissingOrgUnit"));

		masterDetailController.createViews(getPerspective(), table, formLayout);

		updatePasswordButton.onClick.addListener(() -> {
			User user = entityModelBuilder.getSelectedRecord();
			if (user != null) {
				showUpdatePasswordDialogue(user);
			}
		});

		formController.setSaveEntityHandler(user -> {
			if (!addressForm.validateAddress() || !addressForm.getAddress().equals(user.getAddress())) {
				return false;
			}
			OrganizationUnit organizationUnit = OrganizationUtils.convert(organizationUnitViewField.getValue());
			byte[] picture = readUserPicture(pictureChooser);
			addressForm.getAddress().save();
			user
					.setFirstName(firstNameField.getValue())
					.setLastName(lastNameField.getValue())
					.setLanguages(getCompressedLanguages(languagesField.getValue()))
					.setEmail(emailField.getValue())
					.setMobile(mobileField.getValue())
					.setLogin(loginField.getValue())
					.setUserAccountStatus(accountStatusComboBox.getValue())
					.setOrganizationUnit(organizationUnit)
			;
			if (picture != null && picture.length != user.getProfilePictureLength()) {
				user.setProfilePicture(picture);
			}
			return true;
		});

		entityModelBuilder.getOnSelectionEvent().addListener(user -> {
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
			addressForm.setAddress(user.getAddress());
			formController.clearMessages();
		});

		Supplier<User> createNewEntitySupplier = () -> User.create().setAddress(Address.create()).setUserAccountStatus(UserAccountStatus.ACTIVE);
		formController.setCreateNewEntitySupplier(createNewEntitySupplier);
		entityModelBuilder.setSelectedRecord(createNewEntitySupplier.get());
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
				.filter(assignment -> assignment.getRole() != null)
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
		TagComboBox<UserRoleAssignment> tagComboBox = UiUtils.createTagComboBox(BaseTemplate.LIST_ITEM_MEDIUM_ICON_TWO_LINES, PropertyProviders.createUserRoleAssignmentPropertyProviderNoUserDisplay(userSessionData));
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

