package org.teamapps.application.server.controlcenter.systemlog;

import org.teamapps.application.api.application.ApplicationInstanceData;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.application.AbstractManagedApplicationPerspective;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.application.server.system.template.PropertyProviders;
import org.teamapps.application.server.ux.ApplicationUiUtils;
import org.teamapps.application.server.ux.UiUtils;
import org.teamapps.application.server.ux.combo.ComboBoxUtils;
import org.teamapps.application.tools.EntityModelBuilder;
import org.teamapps.common.format.Color;
import org.teamapps.data.extract.PropertyProvider;
import org.teamapps.databinding.MutableValue;
import org.teamapps.databinding.TwoWayBindableValue;
import org.teamapps.icons.Icon;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.field.DisplayField;
import org.teamapps.ux.component.field.FieldEditingMode;
import org.teamapps.ux.component.field.MultiLineTextField;
import org.teamapps.ux.component.field.TemplateField;
import org.teamapps.ux.component.field.combobox.ComboBox;
import org.teamapps.ux.component.field.datetime.InstantDateTimeField;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.form.ResponsiveForm;
import org.teamapps.ux.component.form.ResponsiveFormLayout;
import org.teamapps.ux.component.format.Spacing;
import org.teamapps.ux.component.table.Table;
import org.teamapps.ux.component.table.TableColumn;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.timegraph.TimeGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SystemLogPerspective extends AbstractManagedApplicationPerspective {

	private final PerspectiveSessionData perspectiveSessionData;
	private final UserSessionData userSessionData;
	private final TwoWayBindableValue<SystemLog> selectedLog = TwoWayBindableValue.create();

	public SystemLogPerspective(ApplicationInstanceData applicationInstanceData, MutableValue<String> perspectiveInfoBadgeValue) {
		super(applicationInstanceData, perspectiveInfoBadgeValue);
		perspectiveSessionData = (PerspectiveSessionData) getApplicationInstanceData();
		userSessionData = perspectiveSessionData.getManagedApplicationSessionData().getUserSessionData();
		createUi();
	}

	private void createUi() {
		View masterView = getPerspective().addView(View.createView(StandardLayout.CENTER, ApplicationIcons.CONSOLE, getLocalized("systemLog.systemLogs"), null));
		View timeLineView = getPerspective().addView(View.createView(StandardLayout.TOP, ApplicationIcons.FORM, getLocalized(Dictionary.TIMELINE), null));
		View detailView = getPerspective().addView(View.createView(StandardLayout.RIGHT, ApplicationIcons.CONSOLE, getLocalized("systemLog.logMessage"), null));
		masterView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));
		detailView.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.9f));

		ComboBox<LogLevel> logLeveComboBox = createLogLeveComboBox();
		ComboBox<Application> applicationComboBox = ApplicationUiUtils.createApplicationComboBox(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, userSessionData);
		ComboBox<User> userComboBox = createUserComboBox();
		ComboBox<String> exceptionClassComboBox = createExceptionClassComboBox();

		logLeveComboBox.setShowClearButton(true);
		applicationComboBox.setShowClearButton(true);
		userComboBox.setShowClearButton(true);
		exceptionClassComboBox.setShowClearButton(true);

		ResponsiveForm selectionForm = new ResponsiveForm(50, 75, 200);
		selectionForm.setMargin(Spacing.px(0));
		ResponsiveFormLayout formLayout = selectionForm.addResponsiveFormLayout(500);
		formLayout.addSection().setCollapsible(false).setPadding(new Spacing(0, 5)).setMargin(new Spacing(4, 2, 4, 2));

		formLayout.addLabelAndField(null, getLocalized("systemLog.logLevel"), logLeveComboBox);
		formLayout.addLabelAndField(null, getLocalized("systemLog.user"), userComboBox, false);
		formLayout.addLabelAndField(null, getLocalized("applications.application"), applicationComboBox);
		formLayout.addLabelAndField(null, getLocalized("systemLog.exceptionClass"), exceptionClassComboBox, false);

		EntityModelBuilder<SystemLog> logModelBuilder = new EntityModelBuilder<>(() -> isAppFilter() ? SystemLog.filter().application(NumericFilter.equalsFilter(getMainApplication().getId())) : SystemLog.filter(), getApplicationInstanceData());
		logModelBuilder.attachSearchField(masterView);
		logModelBuilder.attachViewCountHandler(masterView, () -> getLocalized("systemLog.systemLogs"));
		logModelBuilder.onSelectedRecordChanged.addListener(selectedLog::set);
		TimeGraph timeGraph = logModelBuilder.createTimeGraph(SystemLog::getMetaCreationDateAsEpochMilli, SystemLog.FIELD_META_CREATION_DATE);
		logModelBuilder.updateModels();

		Table<SystemLog> table = logModelBuilder.createTable();
		table.setDisplayAsList(true);
		table.setStripedRows(false);
		table.setRowHeight(28);

		TemplateField<ManagedApplication> managedApplicationField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createManagedApplicationPropertyProvider(userSessionData));
		TemplateField<ManagedApplicationPerspective> perspectiveField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createManagedApplicationPerspectivePropertyProvider(userSessionData));
		TemplateField<Application> applicationField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createApplicationPropertyProvider(userSessionData));
		TemplateField<SystemLog> messageField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, createSystemLogPropertyProvider());
		InstantDateTimeField timeField = new InstantDateTimeField();


		table.addColumn(new TableColumn<>(SystemLog.FIELD_MANAGED_APPLICATION, getLocalized("applicationProvisioning.provisionedApplication"), managedApplicationField));
		table.addColumn(new TableColumn<>(SystemLog.FIELD_MANAGED_PERSPECTIVE, getLocalized("applications.perspective"), perspectiveField));
		//table.addColumn(new TableColumn<>(SystemLog.FIELD_APPLICATION, getLocalized("applications.application"), applicationField));
		table.addColumn(new TableColumn<SystemLog>(SystemLog.FIELD_MESSAGE, getLocalized("systemLog.logMessage"), messageField).setDefaultWidth(230));
		table.addColumn(new TableColumn<>(SystemLog.FIELD_META_CREATION_DATE, getLocalized(Dictionary.DATE), timeField));

		table.setPropertyExtractor((systemLog, propertyName) -> switch (propertyName) {
			case SystemLog.FIELD_MANAGED_APPLICATION -> systemLog.getManagedApplication();
			case SystemLog.FIELD_MANAGED_PERSPECTIVE -> systemLog.getManagedPerspective();
			case SystemLog.FIELD_APPLICATION -> systemLog.getApplication();
			case SystemLog.FIELD_MESSAGE -> systemLog;
			case SystemLog.FIELD_META_CREATION_DATE -> systemLog.getMetaCreationDate();
			default -> null;
		});

		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.addComponent(selectionForm);
		verticalLayout.addComponentFillRemaining(table);

		VerticalLayout detailsVerticalLayout = new VerticalLayout();
		ResponsiveForm form = new ResponsiveForm(120, 120, 0);
		detailsVerticalLayout.addComponent(form);
		formLayout = form.addResponsiveFormLayout(450);


		TemplateField<Application> applicationFormField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createApplicationPropertyProvider(userSessionData));
		TemplateField<ManagedApplication> managedApplicationFormField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createManagedApplicationPropertyProvider(userSessionData));
		TemplateField<ManagedApplicationPerspective> perspectiveFormField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createManagedApplicationPerspectivePropertyProvider(userSessionData));
		TemplateField<LogLevel> logLevelFormField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, createLogLevelPropertyProvider());
		TemplateField<User> userFormField = UiUtils.createTemplateField(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE, PropertyProviders.createUserPropertyProvider());
		DisplayField exceptionClassFormField = new DisplayField();
		DisplayField messageFormField = new DisplayField();
		MultiLineTextField detailsFormField = new MultiLineTextField();
		InstantDateTimeField timeFormField = new InstantDateTimeField();
		timeFormField.setEditingMode(FieldEditingMode.READONLY);

		formLayout.addSection().setCollapsible(false).setDrawHeaderLine(false);
		formLayout.addLabelAndField(null, getLocalized("applicationProvisioning.provisionedApplication"), managedApplicationFormField);
		formLayout.addLabelAndField(null, getLocalized("applications.application"), applicationFormField);
		formLayout.addLabelAndField(null, getLocalized("applications.perspective"), perspectiveFormField);
		formLayout.addLabelAndField(null, getLocalized("systemLog.logLevel"), logLevelFormField);
		formLayout.addLabelAndField(null, getLocalized("systemLog.exceptionClass"), exceptionClassFormField);
		formLayout.addLabelAndField(null, getLocalized("systemLog.user"), userFormField);
		formLayout.addLabelAndField(null, getLocalized(Dictionary.DATE), timeFormField);
		formLayout.addLabelAndField(null, getLocalized("systemLog.logMessage"), messageFormField);

		detailsVerticalLayout.addComponentFillRemaining(detailsFormField);


		timeLineView.setComponent(timeGraph);
		masterView.setComponent(verticalLayout);
		detailView.setComponent(detailsVerticalLayout);

		Runnable onFilterChange = () -> {
			Predicate<SystemLog> filter = createFilter(logLeveComboBox.getValue(), applicationComboBox.getValue(), userComboBox.getValue(), exceptionClassComboBox.getValue());
			logModelBuilder.setCustomFilter(filter);
		};

		logLeveComboBox.onValueChanged.addListener(value -> onFilterChange.run());
		applicationComboBox.onValueChanged.addListener(value -> onFilterChange.run());
		userComboBox.onValueChanged.addListener(value -> onFilterChange.run());
		exceptionClassComboBox.onValueChanged.addListener(value -> onFilterChange.run());

		selectedLog.onChanged().addListener(log -> {
			managedApplicationFormField.setValue(log.getManagedApplication());
			applicationFormField.setValue(log.getApplication());
			perspectiveFormField.setValue(log.getManagedPerspective());
			logLevelFormField.setValue(log.getLogLevel());
			userFormField.setValue(log.getMetaCreatedBy() > 0 ? User.getById(log.getMetaCreatedBy()) : null);
			exceptionClassFormField.setValue(log.getExceptionClass());
			messageFormField.setValue(log.getMessage());
			detailsFormField.setValue(log.getDetails());
			timeFormField.setValue(log.getMetaCreationDate());
		});

	}

	private Predicate<SystemLog> createFilter(LogLevel logLevel, Application application, User user, String exceptionClass) {
		if (logLevel == null && application == null && user == null && exceptionClass == null) return null;
		return systemLog -> {
			if (logLevel != null && !logLevel.equals(systemLog.getLogLevel())) {
				return false;
			}
			if (application != null && !application.equals(systemLog.getApplication())) {
				return false;
			}
			if (user != null && systemLog.getMetaCreatedBy() != user.getId()) {
				return false;
			}
			if (exceptionClass != null && !exceptionClass.equals(systemLog.getExceptionClass())) {
				return false;
			}
			return true;
		};
	}

	private ComboBox<LogLevel> createLogLeveComboBox() {
		ComboBox<LogLevel> comboBox = ComboBoxUtils.createRecordComboBox(Arrays.asList(LogLevel.values()), createLogLevelPropertyProvider(), BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setDropDownTemplate(BaseTemplate.LIST_ITEM_MEDIUM_ICON_SINGLE_LINE);
		return comboBox;
	}

	private ComboBox<User> createUserComboBox() {
		ComboBox<User> comboBox = new ComboBox<>(BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
		comboBox.setPropertyProvider(PropertyProviders.createUserPropertyProvider());
		comboBox.setRecordToStringFunction(user -> user.getFirstName() + " " + user.getLastName());
		comboBox.setModel(query -> query == null || query.isBlank() ?
				User.getAll().stream().limit(50).collect(Collectors.toList()) :
				User.filter().parseFullTextFilter(query).execute().stream().limit(50).collect(Collectors.toList()));
		return comboBox;
	}

	private ComboBox<String> createExceptionClassComboBox() {
		Set<String> exceptionClasses = new HashSet<>();
		SystemLog.getAll().forEach(log -> {
			if (log.getExceptionClass() != null) {
				exceptionClasses.add(log.getExceptionClass());
			}
		});
		return ComboBoxUtils.createRecordComboBox(new ArrayList<>(exceptionClasses), PropertyProviders.createStringPropertyProvider(ApplicationIcons.BUG), BaseTemplate.LIST_ITEM_SMALL_ICON_SINGLE_LINE);
	}


	private PropertyProvider<LogLevel> createLogLevelPropertyProvider() {
		return (logLevel, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, getLoglevelIcon(logLevel));
			map.put(BaseTemplate.PROPERTY_CAPTION, logLevel.name());
			return map;
		};
	}

	private PropertyProvider<SystemLog> createSystemLogPropertyProvider() {
		return (log, propertyNames) -> {
			Map<String, Object> map = new HashMap<>();
			map.put(BaseTemplate.PROPERTY_ICON, getLoglevelIcon(log.getLogLevel()));
			map.put(BaseTemplate.PROPERTY_CAPTION, log.getMessage());
			return map;
		};
	}

	private Icon getLoglevelIcon(LogLevel level) {
		return switch (level) {
			case INFO -> ApplicationIcons.INFORMATION;
			case WARNING -> ApplicationIcons.SIGN_WARNING;
			case ERROR -> ApplicationIcons.DELETE;
		};
	}

}

