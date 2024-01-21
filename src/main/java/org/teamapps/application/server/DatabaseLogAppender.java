/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2024 TeamApps.org
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
package org.teamapps.application.server;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Marker;
import org.teamapps.protocol.system.SystemLogEntry;
import org.teamapps.universaldb.UniversalDB;
import org.teamapps.universaldb.message.MessageStore;

import java.io.IOException;
import java.util.List;


public class DatabaseLogAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

	public static final ThreadLocal<Integer> THREAD_LOCAL_MANAGED_APPLICATION = new ThreadLocal<>();
	public static final ThreadLocal<String> THREAD_LOCAL_APPLICATION_VERSION = new ThreadLocal<>();
	public static final ThreadLocal<Integer> THREAD_LOCAL_MANAGED_PERSPECTIVE = new ThreadLocal<>();


	private static boolean started = false;
	private static MessageStore<SystemLogEntry> messageStore;

	public static void startLogger(MessageStore<SystemLogEntry> store) {
		started = true;
		messageStore = store;
	}

	private int getManagedApplicationId() {
		Integer applicationId = THREAD_LOCAL_MANAGED_APPLICATION.get();
		return applicationId != null ? applicationId : 0;
	}

	private int getManagedPerspectiveId() {
		Integer perspectiveId = THREAD_LOCAL_MANAGED_PERSPECTIVE.get();
		return perspectiveId != null ? perspectiveId : 0;
	}

	@Override
	protected void append(ILoggingEvent event) {
		int logLevel = getLogLevel(event);
		if (!started || logLevel == 0) return;

		String marker = getMarker(event);
		if (marker != null && UniversalDB.SKIP_DB_LOGGING.getName().equals(marker)) {
			return;
		}
		int userId = UniversalDB.getUserId();
		int managedApplicationId = getManagedApplicationId();
		int managedPerspectiveId = getManagedPerspectiveId();
		String applicationVersion = THREAD_LOCAL_APPLICATION_VERSION.get();
		String threadName = event.getThreadName();
		String message = event.getFormattedMessage();
		String exceptionClass = null;
		String stackTrace = null;
		if (event.getThrowableProxy() != null) {
			exceptionClass = event.getThrowableProxy().getClassName();
			stackTrace = ThrowableProxyUtil.asString(event.getThrowableProxy());
		}
		SystemLogEntry logEntry = new SystemLogEntry()
				.setLogLevel(logLevel)
				.setTimestamp(System.currentTimeMillis())
				.setUserId(userId)
				.setManagedApplicationId(managedApplicationId)
				.setManagedApplicationPerspectiveId(managedPerspectiveId)
				.setApplicationVersion(applicationVersion)
				.setThreadName(threadName)
				.setMessage(message);
		if (marker != null) {
			logEntry.setMarker(marker);
		}
		if (stackTrace != null) {
			logEntry
					.setExceptionClass(exceptionClass)
					.setStackTrace(stackTrace);
		}
		try {
			messageStore.save(logEntry);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private String getMarker(ILoggingEvent event) {
		List<Marker> markerList = event.getMarkerList();
		if (markerList != null && !markerList.isEmpty()) {
			return markerList.get(0).getName();
		} else {
			return null;
		}
	}

	public static int getLogLevel(org.slf4j.event.Level level) {
		return switch (level) {
			case ERROR -> 4;
			case WARN -> 3;
			case INFO -> 2;
			case DEBUG -> 1;
			case TRACE -> 0;
		};
	}

	public static int getLogLevel(ILoggingEvent event) {
		Level level = event.getLevel();
		if (Level.DEBUG == level) {
			return 1;
		} else if (Level.INFO == level) {
			return 2;
		} else if (Level.WARN == level) {
			return 3;
		} else if (Level.ERROR == level) {
			return 4;
		}
		return 0;
	}



}
