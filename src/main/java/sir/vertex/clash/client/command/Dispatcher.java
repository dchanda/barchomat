package sir.vertex.clash.client.command;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.model.json.ClanSearchResults;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageCreator;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.Pdu.Type;
import sir.barchable.util.Dates;
import sir.barchable.util.Json;
import static sir.barchable.clash.protocol.Pdu.Type.AllianceStreamEntry;
import static sir.barchable.clash.protocol.Pdu.Type.VisitHome;

public class Dispatcher {
	private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

	private HashMap<CommandWakeUp, Type> waitingCommandList = new HashMap<CommandWakeUp, Type>();
	private Connection connection;
	private MessageFactory messageFactory;
	private MessageCreator messageCreator;

	public Dispatcher(Connection connection, MessageFactory messageFactory) {
		this.connection = connection;
		this.messageFactory = messageFactory;
		messageCreator = new MessageCreator(messageFactory);
	}

	public void dispatch(Command command) {
		command.execute();
	}

	public void dispatch(Message message) {
		if (isWakeUpMessage(message.getType())) {
			wakeUpType(message);
		} else {
			if ((message.getType() == AllianceStreamEntry)
					&& (message.getInt("id") == 2) ) {
				String stringCommand = message.getString("text");
				if (stringCommand.charAt(0) == '!') {
					try {
						Command command = constructCommand(stringCommand.substring(1), message);
						dispatch(command);
					} catch (Exception e) {
						send(messageCreator.chatMessage("Command "+ stringCommand.substring(1) + " not found" ));
						log.error("Error", e);
					}
				}
			}
		}
	}

	public void send(Message message) {
		try {
			connection.getOut().write(messageFactory.toPdu(message));
		} catch (IOException e) {
			log.error("Error messageOut", e);
		}

	}

	private void wakeUpType(Message message) {
		for (Map.Entry<CommandWakeUp, Type> e  : waitingCommandList.entrySet()) {
			if (e.getValue() == (message.getType())) {
				if (e.getKey().wakeUp(message) || e.getKey().isMaxLife())
					removeWaitingCommand(e.getKey());
			}
		}
	}

	public void removeWaitingCommand(CommandWakeUp commandWakeUp) {
		waitingCommandList.remove(commandWakeUp);
	}

	public void addWaitingCommand(CommandWakeUp commandWakeUp, Type type) {
		waitingCommandList.put(commandWakeUp, type);
	}

	private Command constructCommand(String stringCommand, Message message)
			throws Exception {
		Command command = null;
		if (stringCommand.equals("getTime"))
			command = new GetTimeCommand(messageCreator, this,
					message.getString("userName"));
		if (stringCommand.equals("whenGems")) {
			command = new WhenGemsCommand(messageCreator, this,
					message.getLong("userId"));
		}
		if (stringCommand.equals("whenGems")) {
			command = new WhenGemsCommand(messageCreator, this,
					message.getLong("userId"));
		}
		if (stringCommand.contains("warRecord") && stringCommand.indexOf("warRecord") == 0) {
			command = new WarRecordCommand(messageCreator, this, stringCommand.substring(10));
		}
		if (command == null)
			throw new Exception("No command found");
		return command;
	}

	private boolean isWaitingCommand(Command command) {
		if (command instanceof CommandWakeUp)
			return true;
		return false;
	}

	private boolean isWakeUpMessage(Type type) {
		return waitingCommandList.containsValue(type);
	}
}
