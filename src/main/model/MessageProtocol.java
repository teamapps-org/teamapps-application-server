/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2022 TeamApps.org
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


import org.teamapps.message.protocol.message.MessageDefinition;
import org.teamapps.message.protocol.message.MessageModelCollection;
import org.teamapps.message.protocol.model.ModelCollection;
import org.teamapps.message.protocol.model.ModelCollectionProvider;

public class MessageProtocol implements ModelCollectionProvider {
	@Override
	public ModelCollection getModelCollection() {
		MessageModelCollection protocol = new MessageModelCollection("ApplicationServerProtocol", "org.teamapps.protocol.system", 1);

		MessageDefinition logEntry = protocol.createModel("systemLogEntry", "#t.sle");
		logEntry.addInteger("logId", 1);
		logEntry.addLong("nodeId", 2);
		logEntry.addInteger("userId", 3);
		logEntry.addLong("timestamp", 4);
		logEntry.addInteger("logLevel", 5);
		logEntry.addInteger("managedApplicationId", 6);
		logEntry.addInteger("managedApplicationPerspectiveId", 7);
		logEntry.addString("applicationVersion", 8);
		logEntry.addString("exceptionClass", 9);
		logEntry.addString("threadName", 10);
		logEntry.addString("message", 12);
		logEntry.addString("stackTrace", 13);
		logEntry.addString("marker", 14);

		MessageDefinition loginData = protocol.createModel("loginData", "#t.l");
		loginData.addInteger("loginId", 1);
		loginData.addInteger("userId", 2);
		loginData.addString("ip", 3);
		loginData.addString("userAgent", 4);
		loginData.addBoolean("mobileDevice", 5);
		loginData.addInteger("screenWidth", 6);
		loginData.addInteger("screenHeight", 7);
		loginData.addInteger("loginTimestamp", 8);
		loginData.addInteger("logoutTimestamp", 9);
		loginData.addInteger("openApplicationsCount", 10);
		loginData.addInteger("openPerspectivesCount", 11);
		loginData.addInteger("activityCount", 12);
		loginData.addLong("sentBytesToClient", 13);
		loginData.addLong("receivedBytesFromClient", 14);


		return protocol;
	}
}
