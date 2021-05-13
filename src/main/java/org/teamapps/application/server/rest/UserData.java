package org.teamapps.application.server.rest;

public class UserData {

	private final String userId;
	private final String accessToken;
	private final String firstName;
	private final String lastName;

	public UserData(int userId, String accessToken, String firstName, String lastName) {
		this.userId = "user-" + userId;
		this.accessToken = accessToken;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public String getUserId() {
		return userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
}
