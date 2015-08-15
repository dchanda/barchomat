package sir.multiply.clash.messagequeue;

import sir.barchable.clash.protocol.*;

public class MessageQueueSend {
	private Pdu.Type type;

	public MessageQueueSend(Pdu.Type type) {
		this.setType(type);
	}

	public Message getMessage(MessageFactory messageFactory) {
		Message message = messageFactory.newMessage(this.getType());

		return message;
	}

	public void setType(Pdu.Type type) {
		this.type = type;
	}

	public Pdu.Type getType() {
		return this.type;
	}
}
