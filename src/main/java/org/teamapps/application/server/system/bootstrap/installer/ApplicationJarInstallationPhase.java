package org.teamapps.application.server.system.bootstrap.installer;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import org.teamapps.application.api.application.ApplicationBuilder;
import org.teamapps.application.api.application.ApplicationPerspectiveBuilder;
import org.teamapps.application.server.system.bootstrap.ApplicationInfo;
import org.teamapps.universaldb.index.file.FileUtil;

import java.net.URL;
import java.net.URLClassLoader;

public class ApplicationJarInstallationPhase implements ApplicationInstallationPhase {

	@Override
	public void checkApplication(ApplicationInfo applicationInfo) {
		try {
			if (applicationInfo.getApplicationJar() == null) {
				return;
			}
			boolean unmanagedApplication = false;
			ApplicationBuilder applicationBuilder = null;
			String fileHash = FileUtil.createFileHash(applicationInfo.getApplicationJar());
			URLClassLoader classLoader = new URLClassLoader(new URL[]{applicationInfo.getApplicationJar().toURI().toURL()});
			ClassInfoList classInfos = new ClassGraph()
					.overrideClassLoaders(classLoader)
					.enableAllInfo()
					.scan()
					.getClassesImplementing(ApplicationPerspectiveBuilder.class.getName())
					.getStandardClasses();

			if (classInfos.size() == 0) {
				classInfos = new ClassGraph()
						.overrideClassLoaders(classLoader)
						.enableAllInfo()
						.scan()
						.getClassesImplementing(ApplicationBuilder.class.getName())
						.getStandardClasses();
				unmanagedApplication = true;
			}

			if (classInfos.isEmpty()) {
				applicationInfo.addError("Could not find application in jar file");
				return;
			}
			if (classInfos.size() > 1) {
				applicationInfo.addError("Too many application classes in jar file");
				return;
			}

			Class<?> builder = classInfos.get(0).loadClass();
			if (unmanagedApplication) {
				applicationBuilder = (ApplicationPerspectiveBuilder) builder.getDeclaredConstructor().newInstance();
			} else {
				applicationBuilder = (ApplicationBuilder) builder.getDeclaredConstructor().newInstance();
			}
			applicationInfo.setApplicationBuilder(applicationBuilder);
			applicationInfo.setUnmanagedPerspectives(unmanagedApplication);
			applicationInfo.setBinaryHash(fileHash);
			applicationInfo.setApplicationClassLoader(classLoader);
		} catch (Exception e) {
			e.printStackTrace();
			applicationInfo.addError("Error checking jar file:" + e.getMessage());
		}
	}

	@Override
	public void installApplication(ApplicationInfo applicationInfo) {

	}

	@Override
	public void loadApplication(ApplicationInfo applicationInfo) {

	}
}
