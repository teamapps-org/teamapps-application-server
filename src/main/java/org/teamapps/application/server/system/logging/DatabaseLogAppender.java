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
package org.teamapps.application.server.system.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.teamapps.application.server.system.launcher.ApplicationLauncher;
import org.teamapps.model.controlcenter.*;

public class DatabaseLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	private static boolean started = false;

	public static void startLogger() {
		started = true;
	}

	@Override
	protected void append(ILoggingEvent event) {
		LogLevel logLevel = getLevel(event);
		if (!started || logLevel == null) return;

		ManagedApplication managedApplication = ApplicationLauncher.THREAD_LOCAL_APPLICATION.get();
		ManagedApplicationPerspective managedApplicationPerspective = ApplicationLauncher.THREAD_LOCAL_MANAGED_PERSPECTIVE.get();
		Application application = managedApplication != null ? managedApplication.getMainApplication() : null;
		ApplicationPerspective applicationPerspective = managedApplicationPerspective != null ? managedApplicationPerspective.getApplicationPerspective() : null;
		ApplicationVersion applicationVersion = application != null ? application.getInstalledVersion() : null;
		String threadName = event.getThreadName();
		String message = event.getFormattedMessage();
		String exceptionClass = null;
		String stackTrace = null;
		if (event.getThrowableProxy() != null) {
			exceptionClass = event.getThrowableProxy().getClassName();
			stackTrace = ThrowableProxyUtil.asString(event.getThrowableProxy());
		}

		SystemLog.create()
				.setLogLevel(logLevel)
				.setManagedApplication(managedApplication)
				.setManagedPerspective(managedApplicationPerspective)
				.setApplication(application)
				.setApplicationPerspective(applicationPerspective)
				.setApplicationVersion(applicationVersion)
				.setExceptionClass(exceptionClass)
				.setThread(threadName)
				.setMessage(message)
				.setDetails(stackTrace)
				.save();
	}

	private LogLevel getLevel(ILoggingEvent event) {
		Level level = event.getLevel();
		if (Level.INFO == level) {
			return LogLevel.INFO;
		} else if (Level.WARN == level) {
			return LogLevel.WARNING;
		} else if (Level.ERROR == level) {
			return LogLevel.ERROR;
		}
		return null;
	}


}
