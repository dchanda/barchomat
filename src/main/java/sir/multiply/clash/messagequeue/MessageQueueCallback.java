package sir.multiply.clash.messagequeue;

import sir.multiply.clash.messagequeue.MessageQueueItem;

public interface MessageQueueCallback {
	public void run(MessageQueueItem item);
}
