package sir.multiply.clash.messagequeue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.protocol.*;

import sir.multiply.clash.messagequeue.MessageQueueCallback;
import sir.multiply.clash.messagequeue.MessageQueueSend;
import sir.multiply.clash.messagequeue.MessageQueueExpect;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MessageQueueItem {
	private static final Logger log = LoggerFactory.getLogger(MessageQueueItem.class);

	public List<MessageQueueSend> sendList = new ArrayList<MessageQueueSend>();
	public List<MessageQueueExpect> expectList = new ArrayList<MessageQueueExpect>();
	private MessageQueueCallback callback = null;
	private Boolean isCallbackCalled = false;
	private Integer timeout = 5000;
	final private CountDownLatch latch = new CountDownLatch(1);

	public MessageQueueItem(ArrayList<MessageQueueSend> sendList, ArrayList<MessageQueueExpect> expectList, MessageQueueCallback callback) {
		this.sendList.addAll(sendList);
		this.expectList.addAll(expectList);
	}

	public MessageQueueItem(ArrayList<MessageQueueSend> sendList, ArrayList<MessageQueueExpect> expectList) {
		this(sendList, expectList, null);
	}

	public MessageQueueItem(MessageQueueSend send, MessageQueueExpect expect, MessageQueueCallback callback) {
		this.sendList.add(send);
		this.expectList.add(expect);
		this.callback = callback;
	}

	public MessageQueueItem(MessageQueueSend send, MessageQueueExpect expect) {
		this(send, expect, null);
	}

	public void send(PduOutputStream sink, MessageFactory messageFactory) {
		for (MessageQueueSend send : this.sendList) {
			try {
				Message message = send.getMessage(messageFactory);

				log.debug("MessageQueueSend: {}", message.getTypeName());

				sink.write(messageFactory.toPdu(message));
			} catch (IOException e) {
				log.error("MessageQueueSend failed", e);
			}
		}
	}

	public Boolean process(Pdu pdu, Message message) {
		Boolean status = true;

		for (MessageQueueExpect expect : this.expectList) {
			if (!expect.process(pdu, message)) {
				status = false;
			}
			else {
				log.debug("MessageQueueExpect: {}", message.getTypeName());
			}
		}

		if (status && !this.isCallbackCalled) {
			this.isCallbackCalled = true;

			if (this.callback != null) {
				this.callback.run(this);
			}

			this.latch.countDown();
		}

		return status;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getTimeout() {
		return this.timeout;
	}

	public void await() {
		try {
			this.latch.await();
		} catch (Exception e) {
			log.error("Could not await latch", e);
		}
	}
}
