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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.*;

public class ApiServlet extends HttpServlet {

	private static final ApiServlet INSTANCE = new ApiServlet();

	private Map<String, ApiHandler> handlerMap = new HashMap<>();

	private ApiServlet() {
	}

	public static ApiServlet getInstance() {
		return INSTANCE;
	}

	public void addHandler(String app, ApiHandler handler) {
		handlerMap.put(app, handler);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp, false);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		handleRequest(req, resp, true);
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp, boolean post) throws ServletException, IOException {
		String pathInfo = req.getPathInfo();
		if (pathInfo == null || pathInfo.length() < 3) {
			return;
		}
		String[] parts = pathInfo.split("/");
		String appName = parts[1];
		List<String> apiPath = new ArrayList<>();
		if (parts.length > 2) {
			apiPath.addAll(Arrays.asList(parts).subList(2, parts.length));
		}
		Map<String, String> parameterMap = new HashMap<>();
		req.getParameterNames().asIterator().forEachRemaining(name -> {
			parameterMap.put(name, req.getParameter(name));
		});

		byte[] bodyData = null;
		if (req.getHeader("transfer-encoding") != null || req.getHeader("content-length") != null) {
			bodyData = IOUtils.toByteArray(req.getInputStream());
		}
		String contentType = req.getHeader("content-type");

		ApiHandler apiHandler = handlerMap.get(appName);
		if (apiHandler != null) {
			apiHandler.handleApiRequest(apiPath, parameterMap, req, resp, post, contentType, bodyData);
		}
	}
}
