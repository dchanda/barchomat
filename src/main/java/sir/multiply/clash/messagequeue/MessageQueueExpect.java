package sir.multiply.clash.messagequeue;

import sir.barchable.clash.protocol.*;

public class MessageQueueExpect {
	private Boolean complete = false;

	public Boolean isComplete() {
		return this.complete;
	}

	public Boolean isComplete(Boolean complete) {
		return this.complete = complete;
	}

	public Boolean process(Pdu pdu, Message message) {
		return this.isComplete(true);
	}
}
