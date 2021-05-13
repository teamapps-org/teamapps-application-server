package org.teamapps.application.server.system.session;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.ui.FormMetaFields;
import org.teamapps.application.api.ui.TranslationKeyField;
import org.teamapps.application.api.ui.UiComponentFactory;
import org.teamapps.application.server.system.bootstrap.BaseResourceLinkProvider;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ui.localize.LocalizationTranslationKeyField;
import org.teamapps.application.ux.UiUtils;
import org.teamapps.application.ux.localize.TranslatableField;
import org.teamapps.application.ux.org.OrganizationViewUtils;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.OrganizationUnitTypeView;
import org.teamapps.model.controlcenter.OrganizationUnitView;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.template.BaseTemplate;

import java.util.Set;

public class SessionUiComponentFactory implements UiComponentFactory {

	private final ApplicationInstanceData applicationInstanceData;
	private final BaseResourceLinkProvider baseResourceLinkProvider;
	private final Application application;

	public SessionUiComponentFactory(ApplicationInstanceData applicationInstanceData, BaseResourceLinkProvider baseResourceLinkProvider, Application application) {
		this.applicationInstanceData = applicationInstanceData;
		this.baseResourceLinkProvider = baseResourceLinkProvider;
		this.application = application;
	}

	@Override
	public ComboBox<OrganizationUnitView> createOrganizationUnitComboBox(Set<OrganizationUnitView> allowedUnits) {
		return OrganizationViewUtils.createOrganizationComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, allowedUnits, applicationInstanceData);
	}

	@Override
	public TagComboBox<OrganizationUnitTypeView> createOrganizationUnitTypeTagComboBox() {
		return OrganizationViewUtils.createOrganizationUnitTypeTagComboBox(150, applicationInstanceData);
	}

	@Override
	public TemplateField<OrganizationUnitView> createOrganizationUnitTemplateField() {
		return UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, OrganizationViewUtils.creatOrganizationUnitViewPropertyProvider(applicationInstanceData));
	}

	@Override
	public TemplateField<Integer> createUserTemplateField() {
		return UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE, PropertyProviders.createUserIdPropertyProvider(applicationInstanceData));
	}

	@Override
	public TranslatableField createTranslatableField() {
		return new TranslatableField(applicationInstanceData);
	}

	@Override
	public TranslationKeyField createTranslationKeyField(String linkButtonCaption) {
		return new LocalizationTranslationKeyField(linkButtonCaption, applicationInstanceData, () -> application);
	}

	@Override
	public FormMetaFields createFormMetaFields() {
		return null;
	}

	@Override
	public String createUserAvatarLink(int userId, boolean large) {
		return baseResourceLinkProvider.getUserProfilePictureLink(userId, large);
	}
}
