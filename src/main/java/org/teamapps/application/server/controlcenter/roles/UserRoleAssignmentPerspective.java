package org.teamapps.application.server.controlcenter.roles;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.combo.ComboBoxUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.model.controlcenter.OrganizationUnit;
import org.teamapps.model.controlcenter.Role;
import org.teamapps.model.controlcenter.User;
import org.teamapps.model.controlcenter.UserRoleAssignment;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;

import java.time.Instant;
import java.util.stream.Collectors;

public class UserRoleAssignmentPerspective extends AbstractManagedApplicationPerspective {

	private final TwoWayBindableValue<UserRoleAssignment> selectedUserRoleAssignment = TwoWayBindableValue.create();


	public UserRoleAssignmentPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.USERS_THREE_RELATION, getLocalized("userRoleAssignment.userRoleAssignments"), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.USERS_THREE_RELATION, getLocalized("userRoleAssignment.userRoleAssignment"), null));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<UserRoleAssignment> roleModelBuilder = new EntityModelBuilder<>(() -> isAppFilter() ? UserRoleAssignment.filter().filterRole(Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId()))) : UserRoleAssignment.filter(), getApplicationInstanceData());
		roleModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("userRoleAssignment.userRoleAssignments"));
		roleModelBuilder.attachSearchField(masterView);
		roleModelBuilder.onSelectedRecordChanged.addListener(selectedUserRoleAssignment::set);
		Table<UserRoleAssignment> table = roleModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setRowHeight(28);
		table.setStripedRows(false);
		roleModelBuilder.updateModels();

		TemplateField<User> userTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createUserPropertyProvider());
		TemplateField<Role> roleTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()));
		TemplateField<OrganizationUnit> orgUnitTableField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.creatOrganizationUnitPropertyProvider(getApplicationInstanceData()));

		table.addColumn(new TableColumn<UserRoleAssignment>(UserRoleAssignment.FIELD_USER, getLocalized("userRoleAssignment.user"), userTableField).setDefaultWidth(200));
		table.addColumn(new TableColumn<UserRoleAssignment>(UserRoleAssignment.FIELD_ROLE, getLocalized("userRoleAssignment.role"), roleTableField).setDefaultWidth(200));
		table.addColumn(new TableColumn<UserRoleAssignment>(UserRoleAssignment.FIELD_ORGANIZATION_UNIT, getLocalized("userRoleAssignment.orgUnit"), orgUnitTableField).setDefaultWidth(200));

		table.setPropertyExtractor((userRoleAssignment, propertyName) -> switch (propertyName) {
			case UserRoleAssignment.FIELD_USER -> userRoleAssignment.getUser();
			case UserRoleAssignment.FIELD_ROLE -> userRoleAssignment.getRole();
			case UserRoleAssignment.FIELD_ORGANIZATION_UNIT -> userRoleAssignment.getOrganizationUnit();
			default -> null;
		});
		masterView.setComponent(table);

		ToolbarButtonGroup buttonGroup = detailView.addWorkspaceButtonGroup(new ToolbarButtonGroup());
		ToolbarButton addButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.ADD, getLocalized(Dictionary.ADD), getLocalized(Dictionary.ADD_RECORD)));

		buttonGroup = detailView.addLocalButtonGroup(new ToolbarButtonGroup());
		ToolbarButton saveButton = buttonGroup.addButton(ToolbarButton.createSmall(ApplicationIcons.FLOPPY_DISK, getLocalized(Dictionary.SAVE_CHANGES)));


		ComboBox<User> userCombobox = ComboBoxUtils.createComboBox(query -> query == null || query.isBlank() ?
						User.getAll().stream().limit(50).collect(Collectors.toList()) :
						User.filter().parseFullTextFilter(query).execute().stream().limit(50).collect(Collectors.toList()),
				PropertyProviders.createUserPropertyProvider(), BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES);
		ComboBox<Role> roleComboBox = ComboBoxUtils.createRecordComboBox(
				() -> isAppFilter() ? Role.filter().organizationField(NumericFilter.equalsFilter(getOrganizationField().getId())).execute() : Role.getAll(),
				PropertyProviders.createRolePropertyProvider(getApplicationInstanceData()),
				BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES
		);

		ComboBox<OrganizationUnit> organizationComboBox = OrganizationUtils.createOrganizationComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, OrganizationUnit.getAll(), getApplicationInstanceData());


		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(450);
		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.user"), userCombobox);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.role"), roleComboBox);
		formLayout.addLabelAndField(null, getLocalized("userRoleAssignment.orgUnit"), organizationComboBox);

		detailView.setComponent(form);

		addButton.onClick.addListener(() -> selectedUserRoleAssignment.set(UserRoleAssignment.create()));

		saveButton.onClick.addListener(() -> {
			UserRoleAssignment userRoleAssignment = selectedUserRoleAssignment.get();
			if (userRoleAssignment != null && userCombobox.getValue() != null && roleComboBox.getValue() != null && organizationComboBox.getValue() != null) {
				userRoleAssignment
						.setUser(userCombobox.getValue())
						.setRole(roleComboBox.getValue())
						.setOrganizationUnit(organizationComboBox.getValue())
						.setLastVerified(Instant.now())
						.save();
				UiUtils.showSaveNotification(true, getApplicationInstanceData());
				roleModelBuilder.updateModels();
			} else {
				UiUtils.showSaveNotification(false, getApplicationInstanceData());
			}
		});

		selectedUserRoleAssignment.onChanged().addListener(userRoleAssignment -> {
			userCombobox.setValue(userRoleAssignment.getUser());
			roleComboBox.setValue(userRoleAssignment.getRole());
			organizationComboBox.setValue(userRoleAssignment.getOrganizationUnit());
		});
		selectedUserRoleAssignment.set(UserRoleAssignment.create());
	}

}

