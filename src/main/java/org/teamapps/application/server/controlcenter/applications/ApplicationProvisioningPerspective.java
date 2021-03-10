package org.teamapps.application.server.controlcenter.applications;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.organization.OrganizationUtils;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.ApplicationUiUtils;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.ux.LocalizationUiUtils;
import org.teamapps.application.server.ux.form.FormUtils;
import org.teamapps.application.tools.EntityListModelBuilder;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.model.controlcenter.ManagedApplicationPerspective;
import org.teamapps.model.controlcenter.OrganizationField;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.absolutelayout.Length;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.linkbutton.LinkButton;
import org.teamapps.ux.component.panel.Panel;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.template.BaseTemplate;

public class ApplicationProvisioningPerspective extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private final TwoWayBindableValue<ManagedApplication> selectedApplication = TwoWayBindableValue.create();


	public ApplicationProvisioningPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View applicationsView = View.createView(StandardLayout.CENTER, ApplicationIcons.INSTALL, getLocalized("applicationProvisioning.provisionedApplications"), null);
		View applicationDetailsView = View.createView(StandardLayout.RIGHT, ApplicationIcons.INSTALL, getLocalized("applicationProvisioning.provisionedApplication"), null);
		applicationDetailsView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		EntityModelBuilder<ManagedApplication> applicationModelBuilder = new EntityModelBuilder<>(ManagedApplication::filter, getApplicationInstanceData());
		Table<ManagedApplication> applicationsTable = applicationModelBuilder.createTemplateFieldTableList(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES, PropertyProviders.createManagedApplicationPropertyProvider(userSessionData), 60);
		applicationsTable.setStripedRows(false);
		applicationsView.setComponent(applicationsTable);
		applicationModelBuilder.onDataChanged.fire();
		applicationModelBuilder.attachSearchField(applicationsView);
		applicationModelBuilder.attachViewCountHandler(applicationsView, () -> getLocalized("applicationProvisioning.provisionedApplications"));
		applicationModelBuilder.onSelectedRecordChanged.addListener(selectedApplication::set);

		getPerspective().addView(applicationsView);
		getPerspective().addView(applicationDetailsView);

		ResponsiveForm applicationForm = new ResponsiveForm(100, 0, 0);
		ResponsiveFormLayout formLayout = applicationForm.addResponsiveFormLayout(400);

		ComboBox<Application> applicationComboBox = ApplicationUiUtils.createApplicationComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, userSessionData);
		ComboBox<OrganizationField> organizationFieldCombo = OrganizationUtils.createOrganizationFieldCombo(getApplicationInstanceData());
		ComboBox<Icon> iconComboBox = ApplicationIcons.createIconComboBox(BaseTemplate.LIST_ITEM_LARGE_ICON_SINGLE_LINE, true);
		iconComboBox.setShowClearButton(true);
		ComboBox<String> titleKeyCombo = LocalizationUiUtils.createLocalizationKeyCombo(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, getApplicationInstanceData(), getMainApplication());
		titleKeyCombo.setShowClearButton(true);
		LinkButton crateTitleKeyButton = new LinkButton(getLocalized("applications.createNewTitle"));
		ComboBox<String> descriptionKeyCombo = LocalizationUiUtils.createLocalizationKeyCombo(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, getApplicationInstanceData(), getMainApplication());
		descriptionKeyCombo.setShowClearButton(true);
		LinkButton createDescriptionKeyButton = new LinkButton(getLocalized("applications.createNewDescription"));

		EntityListModelBuilder<ManagedApplicationPerspective> perspectiveModelBuilder = new EntityListModelBuilder<>(getApplicationInstanceData());
		Table<ManagedApplicationPerspective> perspectivesList = perspectiveModelBuilder.createTemplateFieldTableList(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createManagedApplicationPerspectivePropertyProvider(userSessionData), 24);
		perspectivesList.setStripedRows(false);
		Panel perspectivesPanel = FormUtils.createFormPanel(ApplicationIcons.WINDOWS, getLocalized("applications.perspectives"));
		perspectivesPanel.setRightHeaderField(perspectiveModelBuilder.createSearchField());
		perspectivesPanel.setContent(perspectivesList);
		perspectivesPanel.setMinHeight(Length.ofPixels(150));

		formLayout.addLabelAndComponent(null, getLocalized("applications.mainApplication"), applicationComboBox);
		formLayout.addLabelAndComponent(null, getLocalized("applications.organizationField"), organizationFieldCombo);
		formLayout.addLabelAndComponent(null, getLocalized("applications.applicationIcon"), iconComboBox);
		formLayout.addLabelAndComponent(null, getLocalized("applications.appTitle"), titleKeyCombo);
		formLayout.addLabelAndComponent(null, null, crateTitleKeyButton);
		formLayout.addLabelAndComponent(null, getLocalized("applications.appDescription"), descriptionKeyCombo);
		formLayout.addLabelAndComponent(null, null, createDescriptionKeyButton);
		formLayout.addLabelAndComponent(null, getLocalized("applications.perspectives"), perspectivesPanel);



		selectedApplication.onChanged().addListener(app -> {
			applicationComboBox.setValue(app.getMainApplication());
			organizationFieldCombo.setValue(app.getOrganizationField());
			iconComboBox.setValue(app.getIcon() != null ? IconUtils.decodeIcon(app.getIcon()) : IconUtils.decodeIcon(app.getMainApplication().getIcon()));
			titleKeyCombo.setValue(app.getTitleKey() != null ? app.getTitleKey() : app.getMainApplication() != null ? app.getMainApplication().getTitleKey() : null);
			descriptionKeyCombo.setValue(app.getDescriptionKey() != null ? app.getDescriptionKey() : app.getMainApplication() != null ? app.getMainApplication().getDescriptionKey() : null);
			perspectiveModelBuilder.setEntities(app.getPerspectives());

		});
		applicationDetailsView.setComponent(applicationForm);
/*

		managedApplication
				.addReference("mainApplication", application, false, "installedAsMainApplication")
				.addReference("organizationField", organizationField, false)
				.addText("icon")
				.addText("titleKey")
				.addText("descriptionKey")
				.addBoolean("singleApplication")
				.addReference("perspectives", managedApplicationPerspective, true, "managedApplication", true)
				.addInteger("listingPosition")
				.addReference("applicationGroup", managedApplicationGroup, false, "applications")
				.addBoolean("hidden")
		;

		managedApplicationPerspective
				.addReference("managedApplication", managedApplication, false, "perspectives")
				.addReference("applicationPerspective", applicationPerspective, false, "managedPerspectives")
				.addText("iconOverride")
				.addText("titleKeyOverride")
				.addText("descriptionKeyOverride")
				.addInteger("listingPosition")
		;

		managedApplicationGroup
				.addText("icon")
				.addText("titleKey") //.addTranslatableText("title")
				.addReference("applications", managedApplication, true, "applicationGroup")
				.addInteger("listingPosition")
		;

 */
	}

}
