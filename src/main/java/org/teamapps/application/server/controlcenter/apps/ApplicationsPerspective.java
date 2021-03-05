package org.teamapps.application.server.controlcenter.apps;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.tools.EntityListModelBuilder;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationVersion;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.databinding.MutableValue;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.infiniteitemview.InfiniteItemView2;
import org.teamapps.ux.component.template.BaseTemplate;

public class ApplicationsPerspective extends AbstractManagedApplicationPerspective {

	public ApplicationsPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
	}

	@Override
	public void createPerspectiveUI() {
		PerspectiveSessionData perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		UserSessionData userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		View appsView = View.createView(StandardLayout.CENTER, ApplicationIcons.WINDOW, getLocalized(Dictionary.APPLICATIONS), null);
		View appVersionView = View.createView(StandardLayout.CENTER_BOTTOM, ApplicationIcons.WINDOW, getLocalized(Dictionary.APPLICATIONS), null);

		EntityModelBuilder<Application> applicationModelBuilder = new EntityModelBuilder<>(Application::filter, getApplicationInstanceData());
		InfiniteItemView2<Application> appItemView = applicationModelBuilder.createItemView2(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES, 0, 60);
		appItemView.setItemPropertyProvider(PropertyProviders.createApplicationPropertyProvider(userSessionData));
		appsView.setComponent(appItemView);
		applicationModelBuilder.onDataChanged.fire();


		EntityListModelBuilder<ApplicationVersion> applicationVersionModelBuilder = new EntityListModelBuilder<>(getApplicationInstanceData());
		applicationVersionModelBuilder.setEntityStringFunction(ApplicationVersion::getVersion);
		InfiniteItemView2<ApplicationVersion> versionsItemView = applicationVersionModelBuilder.createItemView2(BaseTemplate.LIST_ITEM_LARGE_ICON_TWO_LINES, 0, 36);
		versionsItemView.setItemPropertyProvider(PropertyProviders.createApplicationVersionPropertyProvider(userSessionData));
		appVersionView.setComponent(versionsItemView);

		applicationModelBuilder.onSelectedRecordChanged.addListener(app -> applicationVersionModelBuilder.setEntities(app.getVersions()));

		getPerspective().addView(appsView);
		getPerspective().addView(appVersionView);

	}
}
