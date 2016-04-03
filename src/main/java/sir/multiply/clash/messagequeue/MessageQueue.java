package sir.multiply.clash.messagequeue;

import sir.barchable.clash.protocol.*;
import sir.multiply.clash.messagequeue.MessageQueueItem;

import java.io.IOException;
import java.util.Queue;
import java.util.LinkedList;

public class MessageQueue {
	private Connection connection;
	private MessageCreator messageCreator;
	private MessageFactory messageFactory;
	private PduOutputStream sink;

	private Queue<MessageQueueItem> queue = new LinkedList<MessageQueueItem>();
	private MessageQueueItem currentItem = null;

	public MessageQueue(Connection connection, MessageFactory messageFactory) {
		this.connection = connection;
		this.messageFactory = messageFactory;
		this.messageCreator = new MessageCreator(messageFactory);
		this.sink = connection.getOut();
	}

	public MessageQueueItem addItem(MessageQueueItem item) {
		this.queue.add(item);

		if (this.currentItem == null) {
			this.next();
		}

		return item;
	}

	public Boolean next() {
		this.currentItem = this.queue.poll();

		if (this.currentItem == null) {
			return false;
		}

		this.currentItem.send(this.sink, this.messageFactory);

		return true;
	}

	public void process(Pdu pdu, Message message) {
		if (this.currentItem == null) {
			this.next();
			return;
		}

		Boolean isDone = this.currentItem.process(pdu, message);

		if (isDone) {
			this.next();
		}
	}
}
