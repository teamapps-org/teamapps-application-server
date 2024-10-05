package org.teamapps.application.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

public class ApiServlet extends HttpServlet {

	private static final ApiServlet INSTANCE = new ApiServlet();

	private Map<String, ApiHandler> handlerMap = new HashMap<>();

	public static ApiServlet getInstance() {
		return INSTANCE;
	}

	private ApiServlet() {
	}

	public void addHandler(String app, ApiHandler handler) {
		handlerMap.put( app, handler);
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
		ApiHandler apiHandler = handlerMap.get(appName);
		if (apiHandler != null) {
			apiHandler.handleApiRequest(apiPath, parameterMap, req, resp, post);
		}
	}
}
