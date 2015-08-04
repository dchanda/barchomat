package sir.vertex.clash.client.command;


import static sir.barchable.clash.protocol.Pdu.Type.VisitedHomeData;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageCreator;
import sir.barchable.util.Dates;
import sir.barchable.util.Json;

public class WhenGemsCommand extends CommandWakeUp {
	private static final Logger log = LoggerFactory
			.getLogger(WhenGemsCommand.class);

	private MessageCreator messageCreator;
	private Dispatcher dispatcher;

	private Long userId;

	public WhenGemsCommand(MessageCreator messageCreator, Dispatcher dispatcher, Long userId) {
		this.messageCreator = messageCreator;
		this.dispatcher = dispatcher;
		this.userId = userId;
	}

	@Override
	public void execute() {
		dispatcher.addWaitingCommand(this, VisitedHomeData);
		dispatcher.send(messageCreator.visitHomeMessage(userId));
	}

	@Override
	public boolean wakeUp(Message message) {
		if (message.getMessage("user").getLong("userId").equals(userId)) {
			try {
				String homeVillage = (String) message.get("homeVillage");
				Village village = Json.valueOf(homeVillage, Village.class);

				dispatcher
						.send(messageCreator.chatMessage("GemBox of user "
								+ message.getMessage("user").getString(
										"userName")
								+ " on: "
								+ Dates.formatIntervalToDayString(village.respawnVars.time_to_gembox_drop)));
				return true;

			} catch (IOException e) {
				log.error("Error convert to village class", e);
			}
		}
		return false;
	}
}
