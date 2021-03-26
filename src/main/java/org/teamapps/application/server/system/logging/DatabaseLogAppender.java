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
