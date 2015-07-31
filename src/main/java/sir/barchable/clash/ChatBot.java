package sir.barchable.clash;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.proxy.MessageTap;
import sir.barchable.util.Dates;
import sir.barchable.util.Json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static sir.barchable.clash.protocol.Pdu.Type.*;

public class ChatBot implements MessageTap {
	private static final Logger log = LoggerFactory.getLogger(ChatBot.class);
	private Logic logic;
	private Connection connection;
	private MessageFactory messageFactory;

	public ChatBot(Logic logic, Connection connection,
			MessageFactory messageFactory) {
		this.logic = logic;
		this.connection = connection;
		this.messageFactory = messageFactory;
	}

	@Override
	public void onMessage(Message message) {
		switch (message.getType()) {
		case AllianceStreamEntry:
			if (message.getInt("id") == 2) {
				String text = message.getString("text");
				if (text.equals("getTime")) {
					sendChatMessage(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date()));
				}
				if (text.equals("whenGems")) {
					Message visitHome = messageFactory.newMessage(VisitHome);

					visitHome.set("homeId", message.getLong("homeId"));
					try {
						connection.getOut().write(
								messageFactory.toPdu(visitHome));
					} catch (IOException e) {
						log.error("Error sending {}", e);
						e.printStackTrace();
					}
				}
			}
			break;
		case VisitedHomeData:
			String homeVillage = (String) message.get("homeVillage");
			try {
				Village village = Json.valueOf(homeVillage, Village.class);
				sendChatMessage("Edelstenen box van "
						+ message.getMessage("user").getString("userName")
						+ " op: zo"
						+ Dates.formatIntervalToDayString(village.respawnVars.time_to_gembox_drop));
			} catch (RuntimeException | IOException e) {
				log.warn("Could not read village", e);
			}
			break;
		default:
			break;

		}
	}

	public void sendChatMessage(String text) {
		// Split text on multiple messages when bigger than 128 characters
		// TODO: Ellipsis at the end... for next chat message
		int len = text.length();
		Message chatToAllianceStream =messageFactory
				.newMessage(ChatToAllianceStream);
		try {
			for (int i = 0; i < len; i += 128) // Max 128 characters
			{
				chatToAllianceStream.set("text",
						text.substring(i, Math.min(len, i + 128)));
				connection.getOut().write(
						messageFactory.toPdu(chatToAllianceStream));
				Thread.sleep(500);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
