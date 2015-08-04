package sir.vertex.clash.client.command;

import java.io.IOException;

import static sir.barchable.clash.protocol.Pdu.Type.AllianceList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.model.json.ClanSearchResults;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageCreator;
import sir.barchable.util.Json;

public class WarRecordCommand extends CommandWakeUp {

	private static final Logger log = LoggerFactory
			.getLogger(WarRecordCommand.class);

	private MessageCreator messageCreator;
	private String clanSearch;
	private Dispatcher dispatcher;

	public WarRecordCommand(MessageCreator messageCreator,
			Dispatcher dispatcher, String clanSearch) {
		this.messageCreator = messageCreator;
		this.clanSearch = clanSearch;
		this.dispatcher = dispatcher;
	}

	@Override
	public void execute() {
		dispatcher.send(messageCreator.clanSearchRequest(clanSearch));
		dispatcher.addWaitingCommand(this, AllianceList);
	}

	@Override
	public boolean wakeUp(Message message) {
		ClanSearchResults results;
		try {
			results = Json.convertValue(message.getFields(),
					ClanSearchResults.class);
			String warRecrod = "";
			if (results.searchEntries.length > 0) {
				warRecrod = "" + results.searchEntries[0].clanName + " :"
						+ results.searchEntries[0].warsWon + "-"
						+ results.searchEntries[0].warsLost + "-"
						+ results.searchEntries[0].warsTied;
			} else {
				warRecrod = "No results found";
			}
			dispatcher.send(messageCreator.chatMessage(warRecrod));
			return true;
		} catch (IOException e) {
			log.error("Error convert to ClanSearchResults class", e);
		}

		return false;
	}

}
