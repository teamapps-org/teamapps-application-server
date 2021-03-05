package org.teamapps.application.server.system.bootstrap.installer;

import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.application.ApplicationPerspectiveBuilder;
import org.teamapps.application.api.application.PerspectiveBuilder;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.application.server.system.bootstrap.ApplicationInfoDataElement;
import org.teamapps.application.server.system.utils.IconUtils;
import org.teamapps.application.server.system.utils.KeyCompare;
import org.teamapps.application.server.system.utils.ValueCompare;
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
			ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
			if (!(applicationBuilder instanceof ApplicationPerspectiveBuilder)) {
				return;
			}
			ApplicationPerspectiveBuilder applicationPerspectiveBuilder = (ApplicationPerspectiveBuilder) applicationBuilder;
			List<PerspectiveBuilder> perspectives = applicationPerspectiveBuilder.getPerspectiveBuilders();
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

		} catch (Exception e) {
			e.printStackTrace();
			applicationInfo.addError("Error checking perspectives:" + e.getMessage());
		}
	}

	@Override
	public void installApplication(ApplicationInfo applicationInfo) {
		ApplicationBuilder applicationBuilder = applicationInfo.getApplicationBuilder();
		if (!(applicationBuilder instanceof ApplicationPerspectiveBuilder)) {
			return;
		}
		ApplicationPerspectiveBuilder applicationPerspectiveBuilder = (ApplicationPerspectiveBuilder) applicationBuilder;
		List<PerspectiveBuilder> perspectives = applicationPerspectiveBuilder.getPerspectiveBuilders();
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
					.isDifferent()
			) {
				applicationPerspective
						.setTitleKey(perspective.getTitleKey())
						.setDescriptionKey(perspective.getDescriptionKey())
						.setIcon(IconUtils.encodeNoStyle(perspective.getIcon()))
						.save();
			}

		}
	}

	@Override
	public void loadApplication(ApplicationInfo applicationInfo) {

	}
}
