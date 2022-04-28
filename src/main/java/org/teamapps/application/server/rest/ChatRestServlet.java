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
package org.teamapps.application.server.rest;


import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teamapps.application.api.password.SecurePasswordHash;
import org.teamapps.model.controlcenter.*;
import org.teamapps.universaldb.index.enumeration.EnumFilterType;
import org.teamapps.universaldb.index.text.TextFilter;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatRestServlet extends HttpServlet {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private Gson gson = new Gson();

	public ChatRestServlet() {
	}

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || !pathInfo.contains("/")) {
			setError(response);
			return;
		}
		String id = request.getSession().getId();
		String userAgent = request.getHeader("User-Agent");
		String acceptLanguage = request.getHeader("Accept-Language");
		String remoteAddr = request.getRemoteAddr();

		String[] parts = pathInfo.split("/");
		if (parts.length < 3) {
			setError(response);
			return;
		}
		String method = parts[1];

		if (method.equals("login")) {
			handleLogin(parts[2], parts[3], userAgent, response);
			return;
		}
		String token = parts[2];
		User user = getUser(token);
		if (user == null) {
			setError(response);
			return;
		}
		switch (method) {
			case "channels":
				handleChannelsRequest(user, response);
				break;
			case "messages":
				String channelId = parts[3];
				String lastMessageId = parts[4];
				handleMessagesRequest(user, channelId, lastMessageId, response);
				break;
			case "avatar":
				String avatarId = parts[3];
				handleAvatarRequest(user, avatarId, response);
				break;
		}

		/*
			get:
				/login/username/pwd
				/channels/token/
				/messages/token/channel/lastMessageId
				/avatar/token/id

			post:
				/message/token
				/media/token/id
		 */


	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null || !pathInfo.contains("/")) {
			setError(response);
			return;
		}
		String[] parts = pathInfo.split("/");
		String method = parts[1];
		String token = parts[2];
		User user = getUser(token);
		if (user == null) {
			setError(response);
			return;
		}

		switch (method) {
			case "message":
				NewMessage newMessage = this.gson.fromJson(request.getReader(), NewMessage.class);
				handleNewMessage(newMessage, user, response);
				break;
			case "media":
				break;
		}

		//String requestData = request.getReader().lines().collect(Collectors.joining());







//		try {
//			for (Part file : parts) {
//				String uuidString = UUID.randomUUID().toString();
//				File tempFile = new File(uploadDirectory, uuidString);
//
//				try (InputStream in = file.getInputStream();
//					 OutputStream out = new FileOutputStream(tempFile)) {
//					IOUtils.copy(in, out);
//				}
//
//				uuids.add(uuidString);
//
//				//uploadListener.accept(tempFile, uuidString);
//			}
//		} catch (Exception e) {
//			LOGGER.warn("Error while uploading files" + e);
//			response.setStatus(500);
//			return;
//		}
//
//		response.setStatus(200);
//		response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
//		response.setContentType("application/json");
//		response.getWriter().println("[" + uuids.stream()
//				.map(uuid -> "\"" + uuid + "\"")
//				.collect(Collectors.joining(",")) + "]");
	}

	private void handleNewMessage(NewMessage newMessage, User user, HttpServletResponse response) {
		String channelId = newMessage.getChannelId();
		ChatChannel channel = ChatChannel.getById(parseId(channelId));
		if (!channel.isStored()) {
			setError(response);
			return;
		}

		ChatMessage.create()
				.setMessage(newMessage.getMessage())
				.setChatChannel(channel)
				.setAuthor(user)
				.save();

		response.setStatus(HttpServletResponse.SC_OK);

	}

	private void handleAvatarRequest(User user, String avatarId, HttpServletResponse response) throws IOException {
		if (avatarId.startsWith("user-")) {
			int id = parseId(avatarId);
			User requestedUser = User.getById(id);
			byte[] profilePicture = requestedUser.getProfilePicture();
			if (profilePicture != null) {
				response.setHeader("Content-Disposition", String.format("%s;filename=\"%2$s\"; filename*=UTF-8''%2$s", "attachment", "avatar.jpg"));
				response.setHeader("Content-Length", String.valueOf(profilePicture.length));
				ServletOutputStream output = response.getOutputStream();
				output.write(profilePicture);
				output.close();
			}
			ChatMessage.create().setChatChannel(ChatChannel.getById(3)).setAuthor(user).setMessage("Avatar request for:" + requestedUser.getFirstName() + " " + requestedUser.getLastName() + " with avatar:" + (profilePicture != null ? "" + profilePicture.length : "0")).save();
		}

	}

	private void handleMessagesRequest(User user, String channelId, String lastMessageId, HttpServletResponse response) throws IOException {
		ChatChannel channel = ChatChannel.getById(parseId(channelId));
		if (!channel.isStored()) {
			setError(response);
			return;
		}
		int messageId = parseId(lastMessageId);
		List<Message> messages = new ArrayList<>();
		channel.getChatMessages().stream().filter(message -> message.getId() > messageId).forEach(message -> {
			messages.add(new Message("message-" + message.getId(), createAuthor(message.getAuthor()), message.getMessage(), message.getMetaCreationDateAsEpochMilli()));
		});
		sendJson(messages, response);
		ChatMessage.create().setChatChannel(ChatChannel.getById(3)).setAuthor(user).setMessage("Message request for channel: " + channel.getTitle() + ", last message id:" + lastMessageId + " returned messages:" + messages.size()).save();
	}

	private Author createAuthor(User user) {
		return new Author("user-" + user.getId(), user.getFirstName() + " " + user.getLastName(), user.getProfilePictureLength() > 0 ? user.getMetaModificationDateAsEpochSecond() : 0);
	}

	private int parseId(String id) {
		String value = id.substring(id.indexOf('-') + 1);
		return Integer.parseInt(value);
	}

	private void handleChannelsRequest(User user, HttpServletResponse response) throws IOException {
		List<Channel> channels = new ArrayList<>();
		for (ChatChannel channel : ChatChannel.getAll()) {
			channels.add(new Channel("channel-" + channel.getId(), channel.getTitle(), System.currentTimeMillis(), (int) (Math.random() * 10)));
		}
		sendJson(channels, response);
		ChatMessage.create().setChatChannel(ChatChannel.getById(3)).setAuthor(user).setMessage("Channel request").save();
	}

	private void handleLogin(String userName, String password, String userAgent, HttpServletResponse response) throws IOException {
		User user = authenticate(userName, password);
		if (user == null) {
			setError(response);
			return;
		}
		String token = "TOK" + UUID.randomUUID().toString().replace("-", "");
		UserAccessToken.create().setUser(user).setSecureToken(token).setUserAgentOnCreation(userAgent).setValid(true).save();
		sendJson(new UserData(user.getId(), token, user.getFirstName(), user.getLastName()), response);
		ChatMessage.create().setChatChannel(ChatChannel.getById(3)).setAuthor(user).setMessage("User login").save();
	}

	private void setError(HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPut(req, resp);
	}

	private void sendJson(Object obj, HttpServletResponse response) throws IOException {
		String json = this.gson.toJson(obj);
		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.print(json);
		out.flush();
	}

	@Override
	protected long getLastModified(HttpServletRequest req) {
		return super.getLastModified(req);
	}

	private User getUser(String accessToken) {
		if (accessToken == null || !accessToken.startsWith("TOK")) {
			return null;
		}
		UserAccessToken userAccessToken = UserAccessToken.filter().secureToken(TextFilter.textEqualsFilter(accessToken)).executeExpectSingleton();
		if (userAccessToken != null && userAccessToken.isValid()) {
			return userAccessToken.getUser();
		} else {
			return null;
		}
	}

	private String createToken(String username, String password, String userAgent) {
		User user = authenticate(userAgent, password);
		if (user == null) {
			return null;
		}
		String token = "TOK" + UUID.randomUUID().toString().replace("-", "");
		UserAccessToken.create().setUser(user).setSecureToken(token).setUserAgentOnCreation(userAgent).setValid(true).save();
		return token;
	}

	private User authenticate(String login, String password) {
		if (login == null || login.isBlank() || password == null || password.isBlank()) {
			return null;
		} else {
			User user = User.filter()
					.login(TextFilter.textEqualsIgnoreCaseFilter(login))
					.userAccountStatus(EnumFilterType.NOT_EQUALS, UserAccountStatus.INACTIVE)
					.executeExpectSingleton();
			if (user != null) {
				String hash = user.getPassword();
				if (SecurePasswordHash.createDefault().verifyPassword(password, hash)) {
					return user;
				}
			}
		}
		return null;
	}
}

