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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import org.teamapps.application.api.application.ApplicationBuilder;
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
					.getClassesImplementing(ApplicationBuilder.class.getName())
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
				applicationBuilder = (ApplicationBuilder) builder.getDeclaredConstructor().newInstance();
			} else {
				applicationBuilder = (ApplicationBuilder) builder.getDeclaredConstructor().newInstance();
			}
			applicationInfo.setBaseApplicationBuilder(applicationBuilder);
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
