package org.teamapps.application.server.system.sms;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.teamapps.application.server.system.config.TwilioConfig;

public class SmsMessage {

	public static String sendSMS(String phoneNumber, String message, TwilioConfig config) {
		Twilio.init(config.getAccountSid(), config.getAuthToken());
		Message msg = Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(config.getSenderPhoneNumber()), message).create();
		//todo log to db
		return msg.getStatus().toString();
	}

}
