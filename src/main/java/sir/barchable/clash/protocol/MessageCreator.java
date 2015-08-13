package sir.barchable.clash.protocol;

import static sir.barchable.clash.protocol.Pdu.Type.ChatToAllianceStream;
import static sir.barchable.clash.protocol.Pdu.Type.Login;
import static sir.barchable.clash.protocol.Pdu.Type.SearchAlliances;
import static sir.barchable.clash.protocol.Pdu.Type.VisitHome;

import java.util.ArrayList;
import java.util.List;

import sir.vertex.clash.client.Settings.Account;

public class MessageCreator {

	private MessageFactory messageFactory;

	public MessageCreator(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	public Message clanSearchRequest(String text) {
		return clanSearchRequest(text, 0, 0, 1, 50, 0, (byte) 0);
	}

	public Message clanSearchRequest(String text, Integer warFrequency,
			Integer clanLocation, Integer minMembers, Integer maxMembers,
			Integer trophyLimit, Byte onlyClanUserCanJoin) {
		Message searchClan = messageFactory.newMessage(SearchAlliances);
		searchClan.set("searchString", text);
		searchClan.set("warFrequency", warFrequency);
		searchClan.set("clanLocation", clanLocation);
		searchClan.set("minMembers", minMembers);
		searchClan.set("maxMembers", maxMembers);
		searchClan.set("trophyLimit", trophyLimit);
		searchClan.set("onlyClanUserCanJoin", onlyClanUserCanJoin);
		searchClan.set("field8", 0);
		searchClan.set("field8", 1);
		return searchClan;
	}

	public Message chatMessage(String text) {
		Message chatToAllianceStream = messageFactory
				.newMessage(ChatToAllianceStream);
			chatToAllianceStream.set("text",text);
			return chatToAllianceStream;
		
	}
	
	public List<Message> chatMessageLong(String text) {
		// Split text on multiple messages when bigger than 128 characters
		// TODO: Ellipsis at the end... for next chat message
		List<Message> messageList = new ArrayList<Message>();
		int len = text.length();
		Message chatToAllianceStream = messageFactory
				.newMessage(ChatToAllianceStream);
		for (int i = 0; i < len; i += 128) // Max 128 characters
		{
			chatToAllianceStream.set("text",
					text.substring(i, Math.min(len, i + 128)));
			messageList.add(chatToAllianceStream);

		}
		return messageList;
	}
	public Message loginMessage(Account account) {
		Message loginMessage = messageFactory.newMessage(Login);
		loginMessage.set("userId", account.userId);
		loginMessage.set("userToken", account.userToken);
		loginMessage.set("udid", account.udid);
		loginMessage.set("openUdid", account.openUdid);
		loginMessage.set("mac", account.mac);
		loginMessage.set("phoneModel", account.phoneModel);
		loginMessage.set("locale", account.locale);
		loginMessage.set("language", account.language);
		loginMessage.set("advertisingIdentifier", account.advertisingIdentifier);
		loginMessage.set("osVersion", account.osVersion);
		loginMessage.set("androidDeviceId", account.androidDeviceId);
		loginMessage.set("facebookAttributionId", account.facebookAttributionId);
		loginMessage.set("advertisingTrackingEnabled", account.advertisingTrackingEnabled);
		loginMessage.set("vendorUuid", account.vendorUuid);
		
		loginMessage.set("clientSeed", 14870668); // Not good, make random
		
		loginMessage.set("majorVersion", 7); // Not good, were store constants?
		loginMessage.set("minorVersion", 156); // Not good, were store constants?
		loginMessage.set("masterHash", "ae9b056807ac8bfa58a3e879b1f1601ff17d1df5"); // Not good, make dynamic
		return loginMessage;
	}
	
	public Message visitHomeMessage(Long homeId) {
		Message visitHome = messageFactory.newMessage(VisitHome);
		visitHome.set("homeId", homeId);
		return visitHome;
	}
}
