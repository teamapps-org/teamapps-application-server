package org.teamapps.application.server.system.launcher;

import org.teamapps.application.api.application.ApplicationPerspective;
import org.teamapps.application.api.localization.Dictionary;
import org.teamapps.application.api.theme.ApplicationIcons;
import org.teamapps.application.server.system.bootstrap.LoadedApplication;
import org.teamapps.application.server.system.bootstrap.SystemRegistry;
import org.teamapps.application.server.system.login.LoginHandler;
import org.teamapps.application.server.system.session.ManagedApplicationSessionData;
import org.teamapps.application.server.system.session.PerspectiveSessionData;
import org.teamapps.application.server.system.session.UserSessionData;
import org.teamapps.common.format.Color;
import org.teamapps.icon.standard.StandardIcon;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ManagedApplication;
import org.teamapps.model.controlcenter.ManagedApplicationGroup;
import org.teamapps.model.controlcenter.ManagedApplicationPerspective;
import org.teamapps.ux.application.ResponsiveApplication;
import org.teamapps.ux.application.layout.StandardLayout;
import org.teamapps.ux.application.perspective.Perspective;
import org.teamapps.ux.application.view.View;
import org.teamapps.ux.component.Component;
import org.teamapps.ux.component.animation.PageTransition;
import org.teamapps.ux.component.field.TextField;
import org.teamapps.ux.component.flexcontainer.VerticalLayout;
import org.teamapps.ux.component.itemview.SimpleItem;
import org.teamapps.ux.component.itemview.SimpleItemGroup;
import org.teamapps.ux.component.itemview.SimpleItemView;
import org.teamapps.ux.component.mobile.MobileLayout;
import org.teamapps.ux.component.panel.Panel;
import org.teamapps.ux.component.rootpanel.RootPanel;
import org.teamapps.ux.component.tabpanel.Tab;
import org.teamapps.ux.component.tabpanel.TabPanel;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.toolbar.Toolbar;
import org.teamapps.ux.component.toolbar.ToolbarButton;
import org.teamapps.ux.component.toolbar.ToolbarButtonGroup;
import org.teamapps.ux.component.tree.Tree;
import org.teamapps.ux.model.ListTreeModel;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationLauncher {

	private final UserSessionData userSessionData;
	private final SystemRegistry registry;
	private final boolean mobileView;
	private final RootPanel rootPanel;
	private List<ApplicationGroupData> sortedApplicationGroups;
	private Component applicationLauncher;
	private Set<ApplicationData> openedApplications = new HashSet<>();
	private Map<ApplicationData, Tab> tabByApplicationData = new HashMap<>();
	private Map<ApplicationData, Component> mobilAppByApplicationData = new HashMap<>();
	private TabPanel applicationsTabPanel;

	public ApplicationLauncher(UserSessionData userSessionData) {
		this.userSessionData = userSessionData;
		this.rootPanel = userSessionData.getRootPanel();
		this.registry = userSessionData.getRegistry();
		this.mobileView = userSessionData.getContext().getClientInfo().isMobileDevice();
		initApplicationData();
		createApplicationLauncher();
		createMainView();
	}

	private void initApplicationData() {
		List<ApplicationGroupData> applicationGroups = new ArrayList<>();
		for (ManagedApplicationGroup applicationGroup : ManagedApplicationGroup.getAll()) {
			ApplicationGroupData applicationGroupData = new ApplicationGroupData(applicationGroup, userSessionData);
			applicationGroups.add(applicationGroupData);
			for (ManagedApplication managedApplication : applicationGroup.getApplications()) {
				Application application = managedApplication.getMainApplication();
				LoadedApplication loadedApplication = registry.getLoadedApplication(application);
				if (loadedApplication.getApplicationBuilder().isApplicationAccessible(userSessionData.getApplicationPrivilegeProvider(managedApplication))) {
					ManagedApplicationSessionData applicationSessionData = userSessionData.createManageApplicationSessionData(managedApplication, new MobileApplicationNavigation());
					ApplicationData applicationData = new ApplicationData(managedApplication, loadedApplication, applicationSessionData);
					applicationGroupData.addApplicationData(applicationData);
				}
			}
		}
		sortedApplicationGroups = ApplicationGroupData.getSortedGroups(applicationGroups.stream().filter(group -> !group.getSortedApplications().isEmpty()).collect(Collectors.toList()));
	}

	private void createApplicationLauncher() {
		SimpleItemView<ApplicationData> itemView = new SimpleItemView<>();
		for (ApplicationGroupData applicationGroup : sortedApplicationGroups) {
			SimpleItemGroup<ApplicationData> itemGroup = new SimpleItemGroup<>(applicationGroup.getIcon(), applicationGroup.getTitle(), BaseTemplate.LIST_ITEM_EXTRA_VERY_LARGE_ICON_TWO_LINES);
			itemView.addGroup(itemGroup);
			itemGroup.setButtonWidth(220);
			for (ApplicationData applicationData : applicationGroup.getSortedApplications()) {
				SimpleItem<ApplicationData> item = itemGroup.addItem(applicationData.getIcon(), applicationData.getTitle(), applicationData.getDescription());
				item.onClick.addListener(() -> openApplication(applicationData));
			}
		}
		if (mobileView) {
			//add logout button
			SimpleItemGroup<ApplicationData> itemGroup = new SimpleItemGroup<>(ApplicationIcons.DELETE, getLocalized(Dictionary.LOGOUT), BaseTemplate.LIST_ITEM_EXTRA_VERY_LARGE_ICON_TWO_LINES);
			itemView.addGroup(itemGroup);
			itemGroup.setButtonWidth(220);
			SimpleItem<ApplicationData> item = itemGroup.addItem(ApplicationIcons.DELETE, getLocalized(Dictionary.LOGOUT), getLocalized(Dictionary.LOGOUT));
			item.onClick.addListener(this::logout);

		}
		applicationLauncher = createLauncherView(itemView, mobileView);
	}

	private void logout() {
		LoginHandler loginHandler = new LoginHandler(registry);
		loginHandler.createLoginView(userSessionData.getContext(), userSessionData.getRootPanel());
		userSessionData.invalidate();
	}

	private void createMainView() {
		userSessionData.getContext().showDefaultBackground(500);
		if (mobileView) {
			rootPanel.setContent(applicationLauncher);
		} else {
			applicationsTabPanel = new TabPanel();
			Tab applicationsTab = new Tab(ApplicationIcons.HOME, getLocalized(Dictionary.APPLICATIONS), applicationLauncher);
			applicationsTabPanel.addTab(applicationsTab, true);

			Tab logoutTab = new Tab(ApplicationIcons.ERROR, getLocalized(Dictionary.LOGOUT), null);
			logoutTab.setLazyLoading(true);
			logoutTab.setRightSide(true);
			applicationsTabPanel.addTab(logoutTab, false);
			logoutTab.onSelected.addListener(this::logout);
			rootPanel.setContent(applicationsTabPanel);
		}
	}

	private void openApplication(ApplicationData applicationData) {
		if (openedApplications.contains(applicationData)) {
			if (mobileView) {
				Component component = mobilAppByApplicationData.get(applicationData);
				rootPanel.setContent(component);
				applicationData.getApplicationSessionData().getMobileNavigation().onShowStartViewRequest().fire();
			} else {
				Tab tab = tabByApplicationData.get(applicationData);
				tab.select();
			}
		} else {
			if (mobileView) {
				Component application = createMobileApplication(applicationData);
				rootPanel.setContent(application);
				openedApplications.add(applicationData);
				mobilAppByApplicationData.put(applicationData, application);
			} else {
				Component application = createApplication(applicationData);
				Tab tab = new Tab(applicationData.getIcon(), applicationData.getTitle(), application);
				tab.setCloseable(true);
				openedApplications.add(applicationData);
				tabByApplicationData.put(applicationData, tab);
				tab.onClosed.addListener(() -> {
					tabByApplicationData.remove(applicationData);
					openedApplications.remove(applicationData);
				});
				applicationsTabPanel.addTab(tab, true);
			}
		}
	}

	private Component createMobileApplication(ApplicationData applicationData) {
		ManagedApplicationSessionData applicationSessionData = applicationData.getApplicationSessionData();
		MobileApplicationNavigation mobileNavigation = applicationSessionData.getMobileNavigation();
		mobileNavigation.setApplicationLauncher(applicationLauncher);
		ResponsiveApplication responsiveApplication = applicationSessionData.getResponsiveApplication();
		if (applicationSessionData.isUnmanagedApplication()) {
			applicationSessionData.getMainApplication().getApplicationBuilder().build(responsiveApplication, applicationSessionData.getUnmanagedApplicationData());
			return responsiveApplication.getUi();
		}
		List<PerspectiveSessionData> sortedPerspectives = new ArrayList<>();
		List<ManagedApplicationPerspective> managedApplicationPerspectives = applicationData.getManagedApplication().getPerspectives().stream()
				.sorted(Comparator.comparingInt(ManagedApplicationPerspective::getListingPosition))
				.collect(Collectors.toList());
		for (ManagedApplicationPerspective managedApplicationPerspective : managedApplicationPerspectives) {
			PerspectiveSessionData perspectiveSessionData = applicationSessionData.createPerspectiveSessionData(managedApplicationPerspective);
			if (perspectiveSessionData.getPerspectiveBuilder().isPerspectiveAccessible(perspectiveSessionData)) {
				sortedPerspectives.add(perspectiveSessionData);
			}
		}

		View applicationMenu = View.createView(StandardLayout.LEFT, StandardIcon.RADIO_BUTTON_GROUP, getLocalized(Dictionary.APPLICATION_MENU), null);
		applicationMenu.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.84f));
		responsiveApplication.addApplicationView(applicationMenu);
		MobileLayout mobileLayout = new MobileLayout();
		applicationMenu.setComponent(mobileLayout);
		mobileNavigation.setApplicationMenu(applicationMenu);

		Tree<PerspectiveSessionData> tree = createApplicationMenuTree(sortedPerspectives);
		mobileLayout.setContent(tree);

		Map<PerspectiveSessionData, ApplicationPerspective> applicationPerspectiveByPerspectiveBuilder = new HashMap<>();
		tree.onNodeSelected.addListener(perspectiveSessionData -> {
			showPerspective(perspectiveSessionData, mobileLayout, applicationMenu, applicationPerspectiveByPerspectiveBuilder);
		});

		mobileNavigation.onBackOperation.addListener(() -> {
			mobileLayout.setContent(tree, PageTransition.MOVE_TO_RIGHT_VS_MOVE_FROM_LEFT, 500);
		});

		showPerspective(sortedPerspectives.get(0), mobileLayout, applicationMenu, applicationPerspectiveByPerspectiveBuilder);

		return responsiveApplication.getUi();
	}

	private void showPerspective(PerspectiveSessionData perspectiveSessionData, MobileLayout mobileLayout, View applicationMenu, Map<PerspectiveSessionData, ApplicationPerspective> applicationPerspectiveByPerspectiveBuilder) {
		ResponsiveApplication responsiveApplication = perspectiveSessionData.getManagedApplicationSessionData().getResponsiveApplication();
		ApplicationPerspective applicationPerspective = applicationPerspectiveByPerspectiveBuilder.get(perspectiveSessionData);
		if (applicationPerspective == null) {
			applicationPerspective = perspectiveSessionData.getPerspectiveBuilder().build(perspectiveSessionData, null);
			applicationPerspectiveByPerspectiveBuilder.put(perspectiveSessionData, applicationPerspective);
			responsiveApplication.addPerspective(applicationPerspective.getPerspective());
		}
		Perspective perspective = applicationPerspective.getPerspective();
		if (perspective.getFocusedView() == null && applicationPerspective.getPerspectiveMenuPanel() != null) {
			perspective.setFocusedView(applicationMenu);
		}
		responsiveApplication.showPerspective(perspective);
		if (applicationPerspective.getPerspectiveMenuPanel() != null) {
			if (applicationMenu.equals(perspective.getFocusedView())) {
				mobileLayout.setContent(applicationPerspective.getPerspectiveMenuPanel(), PageTransition.MOVE_TO_LEFT_VS_MOVE_FROM_RIGHT, 500);
			} else {
				mobileLayout.setContent(applicationPerspective.getPerspectiveMenuPanel());
			}
			perspectiveSessionData.getManagedApplicationSessionData().getMobileNavigation().setBackOperationAvailable(true);
		}
	}

	private Component createApplication(ApplicationData applicationData) {
		ManagedApplicationSessionData applicationSessionData = applicationData.getApplicationSessionData();
		ResponsiveApplication responsiveApplication = applicationSessionData.getResponsiveApplication();
		if (applicationSessionData.isUnmanagedApplication()) {
			applicationSessionData.getMainApplication().getApplicationBuilder().build(responsiveApplication, applicationSessionData.getUnmanagedApplicationData());
			return responsiveApplication.getUi();
		}
		List<PerspectiveSessionData> sortedPerspectives = new ArrayList<>();
		List<ManagedApplicationPerspective> managedApplicationPerspectives = applicationData.getManagedApplication().getPerspectives().stream()
				.sorted(Comparator.comparingInt(ManagedApplicationPerspective::getListingPosition))
				.collect(Collectors.toList());
		for (ManagedApplicationPerspective managedApplicationPerspective : managedApplicationPerspectives) {
			if (managedApplicationPerspective.getApplicationPerspective() != null) {
				PerspectiveSessionData perspectiveSessionData = applicationSessionData.createPerspectiveSessionData(managedApplicationPerspective);
				if (perspectiveSessionData.getPerspectiveBuilder().isPerspectiveAccessible(perspectiveSessionData)) {
					sortedPerspectives.add(perspectiveSessionData);
				}
			}
		}

		View applicationMenu = View.createView(StandardLayout.LEFT, StandardIcon.RADIO_BUTTON_GROUP, getLocalized(Dictionary.APPLICATION_MENU), null);
		responsiveApplication.addApplicationView(applicationMenu);
		applicationMenu.getPanel().setBodyBackgroundColor(Color.WHITE.withAlpha(0.84f));
		VerticalLayout verticalLayout = new VerticalLayout();
		applicationMenu.setComponent(verticalLayout);

		Toolbar toolbar = new Toolbar();
		ToolbarButtonGroup buttonGroup = toolbar.addButtonGroup(new ToolbarButtonGroup());
		buttonGroup.setShowGroupSeparator(false);
		ToolbarButton backButton = ToolbarButton.createSmall(StandardIcon.NAV_LEFT, getLocalized(Dictionary.BACK));
		backButton.setVisible(false);
		buttonGroup.addButton(backButton);
		verticalLayout.addComponent(toolbar);
		MobileLayout mobileLayout = new MobileLayout();
		verticalLayout.addComponentFillRemaining(mobileLayout);


		Tree<PerspectiveSessionData> tree = createApplicationMenuTree(sortedPerspectives);
		mobileLayout.setContent(tree);

		Map<PerspectiveSessionData, ApplicationPerspective> applicationPerspectiveByPerspectiveBuilder = new HashMap<>();
		tree.onNodeSelected.addListener(perspectiveSessionData -> {
			showPerspective(perspectiveSessionData, backButton, mobileLayout, applicationPerspectiveByPerspectiveBuilder);
		});

		if (!sortedPerspectives.isEmpty()) {
			showPerspective(sortedPerspectives.get(0), backButton, mobileLayout, applicationPerspectiveByPerspectiveBuilder);
		}

		backButton.onClick.addListener(() -> {
			backButton.setVisible(false);
			mobileLayout.setContent(tree, PageTransition.MOVE_TO_RIGHT_VS_MOVE_FROM_LEFT, 500);
		});
		return responsiveApplication.getUi();
	}

	private Tree<PerspectiveSessionData> createApplicationMenuTree(List<PerspectiveSessionData> sortedPerspectives) {
		ListTreeModel<PerspectiveSessionData> treeModel = new ListTreeModel<>(sortedPerspectives);
		Tree<PerspectiveSessionData> tree = new Tree<>(treeModel);
		tree.setShowExpanders(false);
		tree.setEntryTemplate(BaseTemplate.LIST_ITEM_VERY_LARGE_ICON_TWO_LINES);
		tree.setPropertyExtractor((perspectiveSessionData, propertyName) -> {
			switch (propertyName) {
				case BaseTemplate.PROPERTY_BADGE:
					return null; //todo
				case BaseTemplate.PROPERTY_ICON:
					return perspectiveSessionData.getIcon();
				case BaseTemplate.PROPERTY_CAPTION:
					return perspectiveSessionData.getTitle();
				case BaseTemplate.PROPERTY_DESCRIPTION:
					return perspectiveSessionData.getDescription();
				default:
					return null;
			}
		});
		return tree;
	}

	private void showPerspective(PerspectiveSessionData perspectiveSessionData, ToolbarButton backButton, MobileLayout mobileLayout, Map<PerspectiveSessionData, ApplicationPerspective> applicationPerspectiveByPerspectiveBuilder) {
		ResponsiveApplication responsiveApplication = perspectiveSessionData.getManagedApplicationSessionData().getResponsiveApplication();
		ApplicationPerspective applicationPerspective = applicationPerspectiveByPerspectiveBuilder.get(perspectiveSessionData);
		if (applicationPerspective == null) {
			applicationPerspective = perspectiveSessionData.getPerspectiveBuilder().build(perspectiveSessionData, null);
			applicationPerspectiveByPerspectiveBuilder.put(perspectiveSessionData, applicationPerspective);
			responsiveApplication.addPerspective(applicationPerspective.getPerspective());
		}
		responsiveApplication.showPerspective(applicationPerspective.getPerspective());
		if (applicationPerspective.getPerspectiveMenuPanel() != null) {
			backButton.setVisible(true);
			mobileLayout.setContent(applicationPerspective.getPerspectiveMenuPanel(), PageTransition.MOVE_TO_LEFT_VS_MOVE_FROM_RIGHT, 500);
		}
	}


	private Component createLauncherView(SimpleItemView<ApplicationData> applicationLauncher, boolean mobileView) {
		Panel panel = new Panel(ApplicationIcons.HOME, getLocalized(Dictionary.APPLICATIONS));
		TextField applicationsSearchField = new TextField();
		applicationsSearchField.setShowClearButton(true);
		applicationsSearchField.setEmptyText(getLocalized(Dictionary.SEARCH___));
		applicationsSearchField.onTextInput().addListener(applicationLauncher::setFilter);
		panel.setRightHeaderField(applicationsSearchField);
		panel.setContent(applicationLauncher);
		panel.setBodyBackgroundColor(Color.WHITE.withAlpha(0.7f));
		if (mobileView) {
			return panel;
		}
		Panel framePanel = new Panel();
		framePanel.setHideTitleBar(true);
		framePanel.setPadding(5);
		framePanel.setContent(panel);
		framePanel.setBodyBackgroundColor(Color.WHITE.withAlpha(0.001f));
		return framePanel;
	}

	public String getLocalized(String key, Object... objects) {
		return userSessionData.getDictionary().getLocalized(key, objects);
	}
}
