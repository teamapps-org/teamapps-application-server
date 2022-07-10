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
		logEntry.addLongProperty("nodeId", 1);
		logEntry.addIntProperty("userId", 2);
		logEntry.addLongProperty("timestamp", 3);
		logEntry.addIntProperty("logLevel", 4);
		logEntry.addIntProperty("managedApplicationId", 5);
		logEntry.addIntProperty("managedApplicationPerspectiveId", 6);
		logEntry.addStringProperty("applicationVersion", 7);
		logEntry.addStringProperty("exceptionClass", 8);
		logEntry.addStringProperty("threadName", 9);
		logEntry.addStringProperty("message", 10);
		logEntry.addStringProperty("stackTrace", 11);
		return modelCollection;
	}
}
