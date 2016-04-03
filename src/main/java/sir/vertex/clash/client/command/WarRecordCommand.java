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

	private int index = -1;

	public WarRecordCommand(MessageCreator messageCreator,
			Dispatcher dispatcher, String clanSearch) {
		this.messageCreator = messageCreator;
		this.clanSearch = clanSearch;
		this.dispatcher = dispatcher;
	}

	public WarRecordCommand(MessageCreator messageCreator,
			Dispatcher dispatcher, String clanSearch, int index) {
		this.messageCreator = messageCreator;
		this.clanSearch = clanSearch;
		this.dispatcher = dispatcher;
		this.index = index;
	}

	@Override
	public void execute() {
		log.debug(clanSearch);
		dispatcher.send(messageCreator.clanSearchRequest(clanSearch));
		dispatcher.addWaitingCommand(this, AllianceList);
	}

	@Override
	public boolean wakeUp(Message message) {
		ClanSearchResults results;
		try {
			results = Json.convertValue(message.getFields(),
					ClanSearchResults.class);
			String warRecord = "";
			if (results.searchEntries.length > 1) {
				if (index == -1) {
					warRecord = "Found multiple clans:";
					for (int i = 0; i <= 5; i++) {
						warRecord += " " + i + ":"
								+ results.searchEntries[i].clanName;
					}
					warRecord += "use !warRecord <clanname> <list number>";
				} else {
					warRecord = "" + results.searchEntries[index].clanName
							+ " :" + results.searchEntries[index].warsWon + "-"
							+ results.searchEntries[index].warsLost + "-"
							+ results.searchEntries[index].warsTied;
				}
			} else {
				warRecord = "No results found";
			}
			dispatcher.send(messageCreator.chatMessage(warRecord));
			return true;
		} catch (IOException e) {
			log.error("Error convert to ClanSearchResults class", e);
		}

		return false;
	}

}
