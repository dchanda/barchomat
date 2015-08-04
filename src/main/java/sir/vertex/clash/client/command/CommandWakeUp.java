package sir.vertex.clash.client.command;

import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.Pdu.Type;

public abstract class CommandWakeUp implements Command {
	public abstract boolean wakeUp(Message message);
	private int TTL=0;
	
	public void tick(){
		TTL++;
	}
	
	public boolean isMaxLife(){
		if (TTL > 10000)
			return true;
		else
			return false;
	}
}
