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
