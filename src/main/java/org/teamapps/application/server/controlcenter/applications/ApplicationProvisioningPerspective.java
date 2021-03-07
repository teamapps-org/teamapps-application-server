package org.teamapps.application.server.controlcenter.applications;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
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

		getPerspective().addView(applicationsView);
		getPerspective().addView(applicationDetailsView);

		ResponsiveForm applicationForm = new ResponsiveForm(100, 0, 0);
		ResponsiveFormLayout formLayout = applicationForm.addResponsiveFormLayout(400);

	}

}
