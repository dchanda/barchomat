package sir.multiply.clash.messagequeue;

import sir.barchable.clash.protocol.*;

public class MessageQueueSend {
	private Pdu.Type type;
	private Message message;

	public MessageQueueSend(Pdu.Type type) {
		this.setType(type);
	}

	public MessageQueueSend(Message message) {
		this.setMessage(message);
	}

	public void setMessage(Message message) {
		this.message = message;
		this.setType(message.getType());
	}

	public Message getMessage(MessageFactory messageFactory) {
		if (this.message == null) {
			this.message = messageFactory.newMessage(this.getType());
		}

		return this.message;
	}

	public Message getMessage() {
		return this.message;
	}

	public void setType(Pdu.Type type) {
		this.type = type;
	}

	public Pdu.Type getType() {
		return this.type;
	}
}
