package sir.multiply.clash.messagequeue;

import sir.barchable.clash.protocol.*;

public interface MessageQueueExpect {
	public Boolean isComplete();
	public Boolean process(Pdu pdu, Message message);
}
