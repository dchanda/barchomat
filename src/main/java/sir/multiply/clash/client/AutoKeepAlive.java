package sir.multiply.clash.client;

import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.Pdu;
import static sir.barchable.clash.protocol.Pdu.Type.KeepAlive;
import static sir.barchable.clash.protocol.Pdu.Type.ServerKeepAlive;
import sir.multiply.clash.messagequeue.*;

import java.util.Timer;
import java.util.TimerTask;

public class AutoKeepAlive {
	private MessageQueue messageQueue;
	private Integer timeout;

	public AutoKeepAlive(MessageQueue messageQueue, Integer timeout) {
		this.messageQueue = messageQueue;
		this.timeout = timeout;

		this.addToQueue();
	}

	public AutoKeepAlive(MessageQueue messageQueue) {
		this(messageQueue, 5000);
	}

	private void addToQueue() {
		MessageQueueSend send = new MessageQueueSend(KeepAlive);
		MessageQueueExpect expect = new MessageQueueExpect() {
			public Boolean process(Pdu pdu, Message message) {
				if (pdu.getType() != ServerKeepAlive) {
					return false;
				}

				return this.isComplete(true);
			}
		};
		MessageQueueCallback callback = new MessageQueueCallback() {
			public void run(MessageQueueItem item) {
				new Timer().schedule(new TimerTask() {
					public void run() {
						AutoKeepAlive.this.addToQueue();
					}
				}, AutoKeepAlive.this.timeout);
			}
		};
		MessageQueueItem item = new MessageQueueItem(send, expect, callback);

		this.messageQueue.addItem(item);
	}
}
