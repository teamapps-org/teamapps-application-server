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

import org.teamapps.protocol.schema.MessageModelCollection;
import org.teamapps.protocol.schema.ModelCollection;
import org.teamapps.protocol.schema.ModelCollectionProvider;
import org.teamapps.protocol.schema.ObjectPropertyDefinition;

public class Protocol implements ModelCollectionProvider {
	@Override
	public ModelCollection getModelCollection() {
		MessageModelCollection modelCollection = new MessageModelCollection("ApplicationServerProtocol", "org.teamapps.protocol.system", 1);

		ObjectPropertyDefinition logEntry = modelCollection.createModel("systemLogEntry", "#t.sle");
		logEntry.addIntProperty("logId", 1);
		logEntry.addLongProperty("nodeId", 2);
		logEntry.addIntProperty("userId", 3);
		logEntry.addLongProperty("timestamp", 4);
		logEntry.addIntProperty("logLevel", 5);
		logEntry.addIntProperty("managedApplicationId", 6);
		logEntry.addIntProperty("managedApplicationPerspectiveId", 7);
		logEntry.addStringProperty("applicationVersion", 8);
		logEntry.addStringProperty("exceptionClass", 9);
		logEntry.addStringProperty("threadName", 10);
		logEntry.addStringProperty("title", 11);
		logEntry.addStringProperty("message", 12);
		logEntry.addStringProperty("stackTrace", 13);
		return modelCollection;
	}
}
