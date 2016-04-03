package sir.vertex.clash.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.protocol.Pdu;
import sir.barchable.clash.protocol.PduOutputStream;
import sir.barchable.clash.proxy.PduFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sir Barchable Date: 15/04/15
 */
public class PduChain {
	private static final Logger log = LoggerFactory.getLogger(PduChain.class);

	private List<Pdu> pduChain;

	public PduChain() {
		this.pduChain = new ArrayList<Pdu>();
	}

	public PduChain(Pdu... pduChain) {
		this.pduChain = new ArrayList<Pdu>(Arrays.asList(pduChain));
	}

	public List<Pdu> getPduChain() {
		return	pduChain;
	}
	
	public List<Pdu> add(Pdu pdu) {
		this.pduChain.add(pdu);
		return this.pduChain;
	}
	
	public List<Pdu> addBefore(Pdu... pduChain) {
		this.pduChain.addAll(0, Arrays.asList(pduChain));
		return this.pduChain;
	}

	public List<Pdu> addAfter(Pdu... pduChain) {
		this.pduChain.addAll(Arrays.asList(pduChain));
		return this.pduChain;
	}

	public List<Pdu> add(PduChain pduChain) {
		this.pduChain.addAll(pduChain.getPduChain());
		return this.pduChain;
    }
	
	public void write(PduOutputStream sink) throws IOException {
		for (Pdu pdu : pduChain) {
			sink.write(pdu);
		}
	}
}
