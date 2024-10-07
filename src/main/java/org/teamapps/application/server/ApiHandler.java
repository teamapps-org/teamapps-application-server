package org.teamapps.application.server;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ApiHandler {

	void handleApiRequest(List<String> apiPath, Map<String, String> parameterMap, HttpServletRequest request, HttpServletResponse response, boolean postRequest, String contentType, byte[] bodyData) throws ServletException, IOException;

}
