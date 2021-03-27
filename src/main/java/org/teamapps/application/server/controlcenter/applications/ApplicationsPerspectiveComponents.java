package org.teamapps.application.server.controlcenter.applications;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.ApplicationLocalizationProvider;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.localization.LocalizationEntry;
import org.teamapps.application.api.localization.LocalizationEntrySet;
import org.teamapps.application.api.privilege.ApplicationRole;
import org.teamapps.application.api.privilege.Privilege;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.bootstrap.installer.ApplicationInstaller;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.window.WindowUtils;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationPrivilege;
import org.teamapps.model.controlcenter.ApplicationPrivilegeGroup;
import org.teamapps.ux.component.dialogue.FormDialogue;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.TagComboBox;
import org.teamapps.ux.component.field.upload.FileFieldDisplayType;
import org.teamapps.ux.component.field.upload.simple.FileItem;
import org.teamapps.ux.component.field.upload.simple.SimpleFileField;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.format.Spacing;
import org.teamapps.ux.component.progress.MultiProgressDisplay;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.window.Window;
import org.teamapps.ux.session.SessionContext;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationsPerspectiveComponents extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private final TwoWayBindableValue<Application> selectedApplication = TwoWayBindableValue.create();


	public ApplicationsPerspectiveComponents(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
	}

	public TwoWayBindableValue<Application> getSelectedApplication() {
		return selectedApplication;
	}


	public void createToolbarButtons(ToolbarButtonGroup buttonGroup) {
		ToolbarButton dataModelButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.DATA_CLOUD, getLocalized("applications.dataModel"), getLocalized("applications.showDataModel")));
		dataModelButton.onClick.addListener(() -> {
			Application application = selectedApplication.get();
			if (application == null) {
				return;
			}
			LoadedApplication loadedApplication = userSessionData.getRegistry().getLoadedApplication(application);
			String schemaDefinition = loadedApplication.getApplicationBuilder().getDatabaseModel() != null ? loadedApplication.getApplicationBuilder().getDatabaseModel().getSchema().createDefinition() : "";
			StringBuilder sb = new StringBuilder();
			for (String line : schemaDefinition.split("[\\r\\n]+")) {
				sb.append(line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;")).append("<br>");
			}

			showTextWindow(ApplicationIcons.DATA_CLOUD, getLocalized("applications.dataModel"), sb.toString());
		});

		buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.KEYS, getLocalized("applications.privileges"), getLocalized("applications.showPrivileges"))).onClick.addListener(() -> {
			Application application = selectedApplication.get();
			if (application == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(application);
			for (ApplicationPrivilegeGroup privilegeGroup : application.getPrivilegeGroups()) {
				sb.append("<b>").append(localizationProvider.getLocalized(privilegeGroup.getTitleKey())).append("</b>");
				if (privilegeGroup.getDescriptionKey() != null) {
					sb.append(" (").append(localizationProvider.getLocalized(privilegeGroup.getDescriptionKey())).append(")");
				}
				sb.append("<br>");
				if (!privilegeGroup.getPrivileges().isEmpty()) {
					sb.append("<ul>");
				}
				for (ApplicationPrivilege privilege : privilegeGroup.getPrivileges()) {
					sb.append("<li>").append(localizationProvider.getLocalized(privilege.getTitleKey())).append("</li>");
				}
				if (!privilegeGroup.getPrivileges().isEmpty()) {
					sb.append("</ul>");
				}
			}
			showTextWindow(ApplicationIcons.DATA_CLOUD, getLocalized("applications.privileges"), sb.toString());
		});

		ToolbarButton applicationRolesButton = buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.USERS_THREE, getLocalized("applications.applicationRoles"), getLocalized("applications.showApplicationRoles")));
		applicationRolesButton.onClick.addListener(() -> {
			Application application = selectedApplication.get();
			if (application == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			LoadedApplication loadedApplication = userSessionData.getRegistry().getLoadedApplication(application);
			ApplicationLocalizationProvider localizationProvider = userSessionData.getApplicationLocalizationProvider(application);
			if (loadedApplication.getApplicationBuilder().getApplicationRoles() != null) {
				for (ApplicationRole applicationRole : loadedApplication.getApplicationBuilder().getApplicationRoles()) {
					sb.append("<b>").append(getLocalized(applicationRole.getTitleKey())).append("</b>");
					if (applicationRole.getDescriptionKey() != null) {
						sb.append(" (").append(localizationProvider.getLocalized(applicationRole.getDescriptionKey())).append(")");
					}
					sb.append("<br>");
					for (PrivilegeGroup privilegeGroup : applicationRole.getPrivilegeGroups()) {
						sb.append(localizationProvider.getLocalized(privilegeGroup.getTitleKey()));
						if (privilegeGroup.getDescriptionKey() != null) {
							sb.append(" (").append(localizationProvider.getLocalized(privilegeGroup.getDescriptionKey())).append(")");
						}
						sb.append("<br>");
						if (!privilegeGroup.getPrivileges().isEmpty()) {
							sb.append("<ul>");
						}
						for (Privilege privilege : privilegeGroup.getPrivileges()) {
							sb.append("<li>").append(localizationProvider.getLocalized(privilege.getTitleKey())).append("</li>");
						}
						if (!privilegeGroup.getPrivileges().isEmpty()) {
							sb.append("</ul>");
						}
					}
				}
			}
			showTextWindow(ApplicationIcons.USERS_THREE, getLocalized("applications.applicationRoles"), sb.toString());
		});

		buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.DOCUMENT_TEXT, getLocalized("applications.applicationCaptions"), getLocalized("applications.showApplicationCaptions"))).onClick.addListener(() -> {
			Application application = selectedApplication.get();
			if (application == null) {
				return;
			}
			LoadedApplication loadedApplication = userSessionData.getRegistry().getLoadedApplication(application);
			StringBuilder sb = new StringBuilder();
			for (LocalizationEntrySet localizationEntrySet : loadedApplication.getApplicationBuilder().getLocalizationData().getLocalizationEntrySets()) {
				String language = localizationEntrySet.getLanguage();
				sb.append("<b>").append(language).append(":</b><br>");
				for (LocalizationEntry entry : localizationEntrySet.getEntries().stream().sorted(Comparator.comparing(LocalizationEntry::getKey)).collect(Collectors.toList())) {
					sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br>");
				}
			}
			showTextWindow(ApplicationIcons.DOCUMENT_TEXT, getLocalized("applications.applicationCaptions"), sb.toString());
		});

		applicationRolesButton.setVisible(false);
		dataModelButton.setVisible(false);

		selectedApplication.onChanged().addListener(() -> {
			Application application = selectedApplication.get();
			LoadedApplication loadedApplication = userSessionData.getRegistry().getLoadedApplication(application);
			applicationRolesButton.setVisible(false);
			dataModelButton.setVisible(false);
			if (loadedApplication != null) {
				ApplicationBuilder applicationBuilder = loadedApplication.getApplicationBuilder();
				applicationRolesButton.setVisible(applicationBuilder.getApplicationRoles() != null && !applicationBuilder.getApplicationRoles().isEmpty());
				dataModelButton.setVisible(applicationBuilder.getDatabaseModel() != null);
			}
		});


	}

	public void showTextWindow(Icon icon, String title, String content) {
		DisplayField displayField = new DisplayField(false, true);
		Window window = WindowUtils.createWindow(icon, title);
		WindowUtils.addCancelCloseButton(window, getApplicationInstanceData());
		displayField.setMargin(Spacing.px(10));
		displayField.setValue(content);
		window.setContent(displayField);
		window.show();
	}


	public void showInstallApplicationDialogue(Application fixedApplication) {
		FormDialogue dialogue = FormDialogue.create(ApplicationIcons.UPLOAD, getLocalized("applications.upload"), getLocalized("applications.uploadApplicationJar"));
		SimpleFileField fileField = new SimpleFileField();
		fileField.setMaxFiles(1);
		fileField.setDisplayType(FileFieldDisplayType.LIST);
		dialogue.addField(ApplicationIcons.JAR, getLocalized("applications.applicationJar"), fileField);
		dialogue.addOkCancelButtons(ApplicationIcons.CHECK, getLocalized(Dictionary.O_K), ApplicationIcons.ERROR, getLocalized(Dictionary.CANCEL));
		dialogue.setCloseable(true);
		dialogue.setAutoCloseOnOk(true);
		dialogue.setCloseOnEscape(true);
		dialogue.show();

		dialogue.onOk.addListener(() -> {
			if (!fileField.getValue().isEmpty()) {
				FileItem fileItem = fileField.getValue().get(0);
				File jarFile = fileItem.getFile();
				if (fileItem.getFileName().endsWith("jar")) {
					showInstaller(jarFile, fixedApplication);
				}
			}
			dialogue.close();
		});
	}

	public void showInstaller(File jarFile, Application fixedApplication) {
		ApplicationInstaller installer = userSessionData.getRegistry().createJarInstaller(jarFile);
		ApplicationInfo applicationInfo = installer.checkApplication();

		if (fixedApplication != null && !applicationInfo.getName().equals(fixedApplication.getName())) {
			FormDialogue dialogue = FormDialogue.create(ApplicationIcons.SIGN_WARNING, getLocalized("applications.wrongApplication"), getLocalized("applications.wrongApplication.error"));
			dialogue.addOkButton(ApplicationIcons.CHECK, getLocalized(Dictionary.O_K));
			dialogue.setCloseable(true);
			dialogue.setAutoCloseOnOk(true);
			dialogue.setCloseOnEscape(true);
			dialogue.show();
			return;
		}

		if (installer.isInstalled()) {
			FormDialogue dialogue = FormDialogue.create(ApplicationIcons.SIGN_WARNING, getLocalized("applications.applicationAlreadyInstalled"), getLocalized("applications.applicationAlreadyInstalled.error"));
			dialogue.addOkButton(ApplicationIcons.CHECK, getLocalized(Dictionary.O_K));
			dialogue.setCloseable(true);
			dialogue.setAutoCloseOnOk(true);
			dialogue.setCloseOnEscape(true);
			dialogue.show();
			return;
		}

		if (!applicationInfo.getErrors().isEmpty()) {
			FormDialogue dialogue = FormDialogue.create(ApplicationIcons.SIGN_WARNING, getLocalized("applications.cannotInstallApplication"), getLocalized("applications.cannotInstallApplication.error"));
			DisplayField displayField = new DisplayField(false, true);
			displayField.setValue(String.join("<br>", applicationInfo.getErrors()));
			dialogue.addField(null, getLocalized("applications.errors"), displayField);
			dialogue.addOkButton(ApplicationIcons.CHECK, getLocalized(Dictionary.O_K));
			dialogue.setCloseable(true);
			dialogue.setAutoCloseOnOk(true);
			dialogue.setCloseOnEscape(true);
			dialogue.show();
			return;
		}

		Window window = WindowUtils.createWindow(ApplicationIcons.UPLOAD, getLocalized("applications.installApplication"));
		ResponsiveForm form = new ResponsiveForm(100, 0, 0);
		ResponsiveFormLayout formLayout = form.addResponsiveFormLayout(400);
		ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();

		if (applicationInfo.getApplication() != null) {
			TemplateField<Application> applicationField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES, PropertyProviders.createApplicationPropertyProvider(userSessionData));
			applicationField.setValue(applicationInfo.getApplication());
			formLayout.addLabelAndComponent(null, getLocalized("applications.application"), applicationField);
		} else {
			Map<String, String> localizationMap = applicationBuilder.getLocalizationData().createLocalizationMapByLanguage().values().iterator().next();
			formLayout.addLabelAndComponent(null, getLocalized("applications.application"), UiUtils.createSingleValueTemplateField(applicationBuilder.getApplicationIcon(), applicationBuilder.getApplicationName()));
			formLayout.addLabelAndComponent(null, getLocalized("applications.appTitle"), UiUtils.createSingleValueTextField(applicationBuilder.getApplicationTitleKey() + " -> " + localizationMap.get(applicationBuilder.getApplicationTitleKey())));
			formLayout.addLabelAndComponent(null, getLocalized("applications.appDescription"), UiUtils.createSingleValueTextField(applicationBuilder.getApplicationDescriptionKey() + " -> " + localizationMap.get(applicationBuilder.getApplicationDescriptionKey())));
			formLayout.addLabelAndComponent(null, getLocalized("applications.releaseNotes"), UiUtils.createSingleValueTextField(applicationBuilder.getReleaseNotes()));
		}

		formLayout.addLabelAndComponent(null, getLocalized("applications.installationType"), UiUtils.createSingleValueTemplateField(applicationInfo.getApplication() != null ? ApplicationIcons.NAV_REFRESH : ApplicationIcons.INSTALL, applicationInfo.getApplication() != null ? getLocalized("applications.applicationUpdate") : getLocalized("applications.newApplication")));
		formLayout.addLabelAndComponent(null, getLocalized("applications.appVersion"), UiUtils.createSingleValueTextField(applicationBuilder.getApplicationVersion().getVersion()));

		if (!applicationInfo.getWarnings().isEmpty()) {
			TagComboBox<String> warningsField = UiUtils.createSingleValueTagComboBox(ApplicationIcons.SIGN_WARNING, applicationInfo.getWarnings());
			formLayout.addLabelAndComponent(null, getLocalized("applications.warnings"), warningsField);
		}

		formLayout.addSection(ApplicationIcons.INFORMATION, getLocalized("applications.applicationInfo")).setDrawHeaderLine(false);
		formLayout.addLabelAndComponent(null, getLocalized("applications.dataModelChanges"), UiUtils.createSingleValueTemplateField(ApplicationIcons.DATA_CLOUD, applicationInfo.getDataModelData().getChangeString()));
		formLayout.addLabelAndComponent(null, getLocalized("applications.localizationDataChanges"), UiUtils.createSingleValueTemplateField(ApplicationIcons.EARTH, applicationInfo.getLocalizationData().getChangeString()));
		formLayout.addLabelAndComponent(null, getLocalized("applications.privilegesDataChanges"), UiUtils.createSingleValueTemplateField(ApplicationIcons.KEYS, applicationInfo.getPrivilegeData().getChangeString()));
		formLayout.addLabelAndComponent(null, getLocalized("applications.perspectivesDataChanges"), UiUtils.createSingleValueTemplateField(ApplicationIcons.WINDOWS, applicationInfo.getPerspectiveData().getChangeString()));

		formLayout.addSection(ApplicationIcons.DATA_CLOUD, getLocalized("applications.dataModelChanges")).setDrawHeaderLine(false).setCollapsed(true);
		formLayout.addLabelAndComponent(null, UiUtils.createSingleValueDisplayField(applicationInfo.getDataModelData().getMultiLineChangeHtml(getLocalized("applications.addedData"), getLocalized("applications.removedData"))));

		formLayout.addSection(ApplicationIcons.EARTH, getLocalized("applications.localizationDataChanges")).setDrawHeaderLine(false).setCollapsed(true);
		formLayout.addLabelAndComponent(null, UiUtils.createSingleValueDisplayField(applicationInfo.getLocalizationData().getMultiLineChangeHtml(getLocalized("applications.addedData"), getLocalized("applications.removedData"))));

		formLayout.addSection(ApplicationIcons.KEYS, getLocalized("applications.privilegesDataChanges")).setDrawHeaderLine(false).setCollapsed(true);
		formLayout.addLabelAndComponent(null, UiUtils.createSingleValueDisplayField(applicationInfo.getPrivilegeData().getMultiLineChangeHtml(getLocalized("applications.addedData"), getLocalized("applications.removedData"))));

		formLayout.addSection(ApplicationIcons.WINDOWS, getLocalized("applications.perspectivesDataChanges")).setDrawHeaderLine(false).setCollapsed(true);
		formLayout.addLabelAndComponent(null, UiUtils.createSingleValueDisplayField(applicationInfo.getPerspectiveData().getMultiLineChangeHtml(getLocalized("applications.addedData"), getLocalized("applications.removedData"))));

		window.setContent(form);
		ToolbarButtonGroup buttonGroup = window.getToolbar().addButtonGroup(new ToolbarButtonGroup());
		buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.INSTALL, getLocalized("applications.installApplication"), getLocalized("applications.installApplication"))).onClick.addListener(() -> {
			SessionContext context = SessionContext.current();
			window.close();
			getApplicationInstanceData().getMultiProgressDisplay().addTask(ApplicationIcons.INSTALL, getLocalized("applications.installApplication"), progressMonitor -> {
				boolean result = userSessionData.getRegistry().installAndLoadApplication(installer);
				context.runWithContext(() -> {
					if (result) {
						SessionContext.current().showNotification(ApplicationIcons.OK, getLocalized("applications.installationSuccessful"));
					} else {
						SessionContext.current().showNotification(ApplicationIcons.ERROR, getLocalized("applications.installationNotSuccessful"));
					}
				});
			});
		});

		buttonGroup = window.getToolbar().addButtonGroup(new ToolbarButtonGroup());
		buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.DATA_CLOUD, getLocalized("applications.dataModel"), getLocalized("applications.showDataModel"))).onClick.addListener(() -> {
			String schemaDefinition = applicationBuilder.getDatabaseModel() != null ? applicationBuilder.getDatabaseModel().getSchema().createDefinition() : "";
			StringBuilder sb = new StringBuilder();
			for (String line : schemaDefinition.split("[\\r\\n]+")) {
				sb.append(line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;")).append("<br>");
			}
			showTextWindow(ApplicationIcons.DATA_CLOUD, getLocalized("applications.dataModel"), sb.toString());
		});

		buttonGroup.addButton(ToolbarButton.create(ApplicationIcons.DOCUMENT_TEXT, getLocalized("applications.applicationCaptions"), getLocalized("applications.showApplicationCaptions"))).onClick.addListener(() -> {
			StringBuilder sb = new StringBuilder();
			for (LocalizationEntrySet localizationEntrySet : applicationBuilder.getLocalizationData().getLocalizationEntrySets()) {
				String language = localizationEntrySet.getLanguage();
				sb.append("<b>").append(language).append(":</b><br>");
				for (LocalizationEntry entry : localizationEntrySet.getEntries().stream().sorted(Comparator.comparing(LocalizationEntry::getKey)).collect(Collectors.toList())) {
					sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("<br>");
				}
			}
			showTextWindow(ApplicationIcons.DOCUMENT_TEXT, getLocalized("applications.applicationCaptions"), sb.toString());
		});

		WindowUtils.addCancelCloseButton(window, getApplicationInstanceData());
		window.show();


	}
}
