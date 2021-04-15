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
import org.teamapps.application.api.application.BaseApplicationBuilder;
import org.teamapps.application.api.application.perspective.PerspectiveBuilder;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.ApplicationInfoDataElement;
import org.teamapps.application.server.system.utils.KeyCompare;
import org.teamapps.application.server.system.utils.ValueCompare;
import org.teamapps.application.ux.IconUtils;
import org.teamapps.model.controlcenter.Application;
import org.teamapps.model.controlcenter.ApplicationPerspective;
import org.teamapps.universaldb.index.numeric.NumericFilter;
import org.teamapps.universaldb.pojo.Entity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PerspectiveDataInstallationPhase implements ApplicationInstallationPhase {


	@Override
	public void checkApplication(ApplicationInfo applicationInfo) {
		try {
			if (!applicationInfo.getErrors().isEmpty()) {
				return;
			}
			BaseApplicationBuilder baseApplicationBuilder = applicationInfo.getBaseApplicationBuilder();
			if (!(baseApplicationBuilder instanceof ApplicationBuilder)) {
				applicationInfo.setPerspectiveData(new ApplicationInfoDataElement());
				return;
			}
			ApplicationBuilder applicationBuilder = (ApplicationBuilder) baseApplicationBuilder;
			List<PerspectiveBuilder> perspectives = applicationBuilder.getPerspectiveBuilders();
			if (perspectives == null || perspectives.isEmpty()) {
				applicationInfo.addError("Missing perspectives");
				return;
			}
			for (PerspectiveBuilder builder : perspectives) {
				if (builder.getName() == null || builder.getTitleKey() == null) {
					applicationInfo.addError("Missing perspective meta data for perspective: " + builder.getName());
					return;
				}
			}
			ApplicationInfoDataElement dataInfo = new ApplicationInfoDataElement();
			Application application = applicationInfo.getApplication();
			List<ApplicationPerspective> applicationPerspectives = application == null ? Collections.emptyList() : ApplicationPerspective.filter()
					.application(NumericFilter.equalsFilter(application.getId()))
					.execute();

			dataInfo.setData(perspectives.stream().map(PerspectiveBuilder::getName).collect(Collectors.joining("\n")));
			KeyCompare<PerspectiveBuilder, ApplicationPerspective> keyCompare = new KeyCompare<>(perspectives, applicationPerspectives, PerspectiveBuilder::getName, ApplicationPerspective::getName);
			List<PerspectiveBuilder> newPerspectives = keyCompare.getNotInB();
			dataInfo.setDataAdded(newPerspectives.stream().map(PerspectiveBuilder::getName).collect(Collectors.toList()));
			List<ApplicationPerspective> removedPerspectives = keyCompare.getNotInA();
			dataInfo.setDataRemoved(removedPerspectives.stream().map(ApplicationPerspective::getName).collect(Collectors.toList()));
			applicationInfo.setPerspectiveData(dataInfo);
		} catch (Exception e) {
			e.printStackTrace();
			applicationInfo.addError("Error checking perspectives:" + e.getMessage());
		}
	}

	@Override
	public void installApplication(ApplicationInfo applicationInfo) {
		BaseApplicationBuilder baseApplicationBuilder = applicationInfo.getBaseApplicationBuilder();
		if (!(baseApplicationBuilder instanceof ApplicationBuilder)) {
			return;
		}
		ApplicationBuilder applicationBuilder = (ApplicationBuilder) baseApplicationBuilder;
		List<PerspectiveBuilder> perspectives = applicationBuilder.getPerspectiveBuilders();
		Application application = applicationInfo.getApplication();
		List<ApplicationPerspective> applicationPerspectives = ApplicationPerspective.filter()
				.application(NumericFilter.equalsFilter(application.getId()))
				.execute();

		KeyCompare<PerspectiveBuilder, ApplicationPerspective> keyCompare = new KeyCompare<>(perspectives, applicationPerspectives, PerspectiveBuilder::getName, ApplicationPerspective::getName);
		List<PerspectiveBuilder> newPerspectives = keyCompare.getNotInB();
		newPerspectives.forEach(perspective -> ApplicationPerspective.create()
				.setApplication(application)
				.setName(perspective.getName())
				.setIcon(IconUtils.encodeNoStyle(perspective.getIcon()))
				.setTitleKey(perspective.getTitleKey())
				.setDescriptionKey(perspective.getDescriptionKey())
				.setAutoProvision(perspective.autoProvisionPerspective())
				.save());

		List<ApplicationPerspective> removedPerspectives = keyCompare.getNotInA();
		removedPerspectives.forEach(Entity::delete);

		List<PerspectiveBuilder> existingPerspectives = keyCompare.getInB();
		for (PerspectiveBuilder perspective : existingPerspectives) {
			ApplicationPerspective applicationPerspective = keyCompare.getB(perspective);
			if (ValueCompare
					.create(perspective.getTitleKey(), applicationPerspective.getTitleKey())
					.check(perspective.getDescriptionKey(), applicationPerspective.getDescriptionKey())
					.check(IconUtils.encodeNoStyle(perspective.getIcon()), applicationPerspective.getIcon())
					.check(perspective.autoProvisionPerspective(), applicationPerspective.getAutoProvision())
					.isDifferent()
			) {
				applicationPerspective
						.setTitleKey(perspective.getTitleKey())
						.setDescriptionKey(perspective.getDescriptionKey())
						.setIcon(IconUtils.encodeNoStyle(perspective.getIcon()))
						.setAutoProvision(perspective.autoProvisionPerspective())
						.save();
			}

		}
	}

	@Override
	public void loadApplication(ApplicationInfo applicationInfo) {

	}
}
