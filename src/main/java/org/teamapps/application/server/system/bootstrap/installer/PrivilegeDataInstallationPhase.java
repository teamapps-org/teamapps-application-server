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
package org.teamapps.application.server.system.bootstrap.installer;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.privilege.Privilege;
import org.teamapps.application.api.privilege.PrivilegeGroup;
import org.teamapps.application.api.privilege.PrivilegeGroupType;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.ApplicationInfoDataElement;
import org.teamapps.application.server.system.privilege.ApplicationScopePrivilegeProvider;
import org.teamapps.application.server.ux.IconUtils;
import org.teamapps.application.server.system.utils.KeyCompare;
import org.teamapps.application.server.system.utils.ValueCompare;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationPrivilege;
import org.teamapps.model.controlcenter.ApplicationPrivilegeGroup;
import org.teamapps.model.controlcenter.ApplicationPrivilegeGroupType;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.pojo.Entity;

import java.util.ArrayList;
import java.util.List;

import static org.teamapps.model.controlcenter.ApplicationPrivilegeGroupType.*;

public class PrivilegeDataInstallationPhase implements ApplicationInstallationPhase {

	@Override
	public void checkApplication(ApplicationInfo applicationInfo) {
		try {
			if (!applicationInfo.getErrors().isEmpty()) {
				return;
			}
			ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
			List<PrivilegeGroup> privilegeGroups = applicationBuilder.getPrivilegeGroups();
			if (privilegeGroups == null) {
				applicationInfo.addError("Missing privileges");
				return;
			}

			ApplicationInfoDataElement dataInfo = new ApplicationInfoDataElement();
			Application application = applicationInfo.getApplication();
			List<String> privilegeGroupInfoData = getPrivilegeGroupInfoData(privilegeGroups);
			dataInfo.setData(String.join("\n", privilegeGroupInfoData));

			if (application == null) {
				dataInfo.setDataAdded(privilegeGroupInfoData);
			} else {
				List<ApplicationPrivilegeGroup> applicationPrivilegeGroups = getApplicationPrivilegeGroups(application);
				KeyCompare<PrivilegeGroup, ApplicationPrivilegeGroup> keyCompare = new KeyCompare<>(privilegeGroups, applicationPrivilegeGroups, PrivilegeGroup::getName, ApplicationPrivilegeGroup::getName);
				List<PrivilegeGroup> newPrivilegeGroups = keyCompare.getNotInB();
				dataInfo.setDataAdded(getPrivilegeGroupInfoData(newPrivilegeGroups));

				List<ApplicationPrivilegeGroup> removedPrivilegeGroups = keyCompare.getNotInA();
				dataInfo.setDataRemoved(getApplicationPrivilegeGroupInfoData(removedPrivilegeGroups));

				List<PrivilegeGroup> existingGroups = keyCompare.getInB();
				for (PrivilegeGroup privilegeGroup : existingGroups) {
					KeyCompare<Privilege, ApplicationPrivilege> privilegeCompare = new KeyCompare<>(privilegeGroup.getPrivileges(), keyCompare.getB(privilegeGroup).getPrivileges(), Privilege::getName, ApplicationPrivilege::getName);
					//privilege diff
				}
			}
			applicationInfo.setPrivilegeData(dataInfo);
		} catch (Exception e) {
			e.printStackTrace();
			applicationInfo.addError("Error checking privileges:" + e.getMessage());
		}
	}

	@Override
	public void installApplication(ApplicationInfo applicationInfo) {
		ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
		List<PrivilegeGroup> privilegeGroups = applicationBuilder.getPrivilegeGroups();
		Application application = applicationInfo.getApplication();
		List<ApplicationPrivilegeGroup> applicationPrivilegeGroups = getApplicationPrivilegeGroups(application);

		KeyCompare<PrivilegeGroup, ApplicationPrivilegeGroup> keyCompare = new KeyCompare<>(privilegeGroups, applicationPrivilegeGroups, PrivilegeGroup::getName, ApplicationPrivilegeGroup::getName);
		List<PrivilegeGroup> newPrivilegeGroups = keyCompare.getNotInB();
		newPrivilegeGroups.forEach(group -> createApplicationPrivilegeGroup(group, application));

		List<ApplicationPrivilegeGroup> removedPrivilegeGroups = keyCompare.getNotInA();
		removedPrivilegeGroups.forEach(Entity::delete);

		List<PrivilegeGroup> existingGroups = keyCompare.getInB();
		for (PrivilegeGroup group : existingGroups) {
			ApplicationPrivilegeGroup applicationPrivilegeGroup = keyCompare.getB(group);
			ValueCompare valueCompare = new ValueCompare()
					.check(IconUtils.encodeNoStyle(group.getIcon()), applicationPrivilegeGroup.getIcon())
					.check(group.getName(), applicationPrivilegeGroup.getName())
					.check(group.getTitleKey(), applicationPrivilegeGroup.getTitleKey())
					.check(group.getDescriptionKey(), applicationPrivilegeGroup.getDescriptionKey());

			if (valueCompare.isDifferent()) {
				applicationPrivilegeGroup
						.setIcon(IconUtils.encodeNoStyle(group.getIcon()))
						.setName(group.getName())
						.setTitleKey(group.getTitleKey())
						.setDescriptionKey(group.getDescriptionKey())
						.save();
			}
			KeyCompare<Privilege, ApplicationPrivilege> privilegeCompare = new KeyCompare<>(group.getPrivileges(), applicationPrivilegeGroup.getPrivileges(), Privilege::getName, ApplicationPrivilege::getName);
			List<Privilege> newPrivileges = privilegeCompare.getNotInB();
			newPrivileges.forEach(privilege -> createApplicationPrivilege(applicationPrivilegeGroup, privilege));

			List<ApplicationPrivilege> removedPrivileges = privilegeCompare.getNotInA();
			removedPrivileges.forEach(Entity::delete);

			List<Privilege> existingPrivileges = privilegeCompare.getInB();
			for (Privilege existingPrivilege : existingPrivileges) {
				ApplicationPrivilege applicationPrivilege = privilegeCompare.getB(existingPrivilege);
				if (new ValueCompare()
						.check(IconUtils.encodeNoStyle(existingPrivilege.getIcon()), applicationPrivilege.getIcon())
						.check(existingPrivilege.getTitleKey(), applicationPrivilege.getTitleKey())
						.isDifferent()) {
					applicationPrivilege
							.setIcon(IconUtils.encodeNoStyle(existingPrivilege.getIcon()))
							.setTitleKey(existingPrivilege.getTitleKey())
							.save();
				}
			}
		}
	}

	@Override
	public void loadApplication(ApplicationInfo applicationInfo) {
		applicationInfo.getLoadedApplication().setAppPrivilegeProvider(new ApplicationScopePrivilegeProvider(applicationInfo.getApplicationBuilder()));
	}

	private List<ApplicationPrivilegeGroup> getApplicationPrivilegeGroups(Application application) {
		return ApplicationPrivilegeGroup.filter()
				.application(NumericFilter.equalsFilter(application.getId()))
				.execute();
	}

	private void createApplicationPrivilegeGroup(PrivilegeGroup group, Application application) {
		ApplicationPrivilegeGroup applicationPrivilegeGroup = ApplicationPrivilegeGroup.create()
				.setApplication(application)
				.setIcon(IconUtils.encodeNoStyle(group.getIcon()))
				.setName(group.getName())
				.setTitleKey(group.getTitleKey())
				.setDescriptionKey(group.getDescriptionKey())
				.setApplicationPrivilegeGroupType(getType(group.getType()))
				.save();
		for (Privilege privilege : group.getPrivileges()) {
			createApplicationPrivilege(applicationPrivilegeGroup, privilege);
		}
	}

	private void createApplicationPrivilege(ApplicationPrivilegeGroup applicationPrivilegeGroup, Privilege privilege) {
		ApplicationPrivilege.create()
				.setPrivilegeGroup(applicationPrivilegeGroup)
				.setIcon(IconUtils.encodeNoStyle(privilege.getIcon()))
				.setName(privilege.getName())
				.setTitleKey(privilege.getTitleKey())
				.save();
	}

	private ApplicationPrivilegeGroupType getType(PrivilegeGroupType groupType) {
		switch (groupType) {
			case SIMPLE_PRIVILEGE:
				return SIMPLE_PRIVILEGE;
			case SIMPLE_ORGANIZATIONAL_PRIVILEGE:
				return SIMPLE_ORGANIZATIONAL_PRIVILEGE;
			case SIMPLE_CUSTOM_OBJECT_PRIVILEGE:
				return SIMPLE_CUSTOM_OBJECT_PRIVILEGE;
			case STANDARD_PRIVILEGE_GROUP:
				return STANDARD_PRIVILEGE_GROUP;
			case ORGANIZATIONAL_PRIVILEGE_GROUP:
				return ORGANIZATIONAL_PRIVILEGE_GROUP;
			case CUSTOM_OBJECT_PRIVILEGE_GROUP:
				return CUSTOM_OBJECT_PRIVILEGE_GROUP;
			default:
				return null;
		}
	}

	private List<String> getPrivilegeGroupInfoData(List<PrivilegeGroup> privilegeGroups) {
		List<String> infoData = new ArrayList<>();
		for (PrivilegeGroup privilegeGroup : privilegeGroups) {
			infoData.add(privilegeGroup.getName() + ": " + privilegeGroup.getType().name());
			for (Privilege privilege : privilegeGroup.getPrivileges()) {
				infoData.add(" -> " + privilege.getName());
			}
		}
		return infoData;
	}

	private List<String> getApplicationPrivilegeGroupInfoData(List<ApplicationPrivilegeGroup> privilegeGroups) {
		List<String> infoData = new ArrayList<>();
		for (ApplicationPrivilegeGroup privilegeGroup : privilegeGroups) {
			infoData.add(privilegeGroup.getName() + ": " + privilegeGroup.getApplicationPrivilegeGroupType().name());
			for (ApplicationPrivilege privilege : privilegeGroup.getPrivileges()) {
				infoData.add(" -> " + privilege.getName());
			}
		}
		return infoData;
	}
}
