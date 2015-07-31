package sir.vertex.clash.client;

import java.io.IOException;
import java.util.TimerTask;

import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.PduOutputStream;
import static sir.barchable.clash.protocol.Pdu.Type.KeepAlive;

public class AutoSend extends TimerTask {
	private PduOutputStream sink;
	private MessageFactory messageFactory;

	public AutoSend(PduOutputStream sink, MessageFactory messageFactory) {
		this.sink = sink;
		this.messageFactory = messageFactory;
	}

	public void run() {
		try {
			sink.write(messageFactory.toPdu(messageFactory.newMessage(KeepAlive)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
