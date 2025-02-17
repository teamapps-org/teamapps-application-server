/*-
 * ========================LICENSE_START=================================
 * TeamApps Application Server
 * ---
 * Copyright (C) 2020 - 2025 TeamApps.org
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

import jakarta.servlet.Servlet;

import java.util.Collection;
import java.util.Collections;

public class ServletRegistration {

	private final Servlet servlet;
	private final Collection<String> mappings;
	private final boolean asyncSupported;

	public ServletRegistration(Servlet servlet, String mapping) {
		this(servlet, mapping, false);
	}

	public ServletRegistration(Servlet servlet, String mapping, boolean asyncSupported) {
		this(servlet, Collections.singleton(mapping), asyncSupported);
	}

	public ServletRegistration(Servlet servlet, Collection<String> mappings) {
		this(servlet, mappings, false);
	}

	public ServletRegistration(Servlet servlet, Collection<String> mappings, boolean asyncSupported) {
		this.servlet = servlet;
		this.mappings = mappings;
		this.asyncSupported = asyncSupported;
	}

	public Servlet getServlet() {
		return servlet;
	}

	public Collection<String> getMappings() {
		return mappings;
	}

	public boolean isAsyncSupported() {
		return asyncSupported;
	}
}
