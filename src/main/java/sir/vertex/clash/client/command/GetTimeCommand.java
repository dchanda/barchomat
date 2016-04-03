package sir.vertex.clash.client.command;

import java.text.SimpleDateFormat;
import java.util.Date;

import sir.barchable.clash.model.Logic;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.MessageCreator;
import sir.barchable.clash.protocol.MessageFactory;

public class GetTimeCommand implements Command {

	private MessageCreator messageCreator;
	private Dispatcher dispatcher;
	private String userName;

	public GetTimeCommand(MessageCreator messageCreator, Dispatcher dispatcher, String userName) {
		this.messageCreator = messageCreator;
		this.dispatcher = dispatcher;
		this.userName = userName;
	}

	@Override
	public void execute() {
		dispatcher.send(messageCreator.chatMessage("To "
				+ userName
				+ " "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date())));
	}
}
