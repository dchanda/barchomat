package sir.vertex.clash.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.protocol.Clash7Random;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.proxy.MessageTap;

class LoginKeyTap implements MessageTap {
	private static final Logger log = LoggerFactory.getLogger(LoginKeyTap.class);

    private Clash7Random prng;

    public void setPrng(Clash7Random prng){
    	this.prng = prng;
    }
    private byte[] key;

    public byte[] getKey() {
        return key;
    }

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {

            // Server generates the nonce
            case Encryption:
                if (prng == null) {
                    throw new IllegalStateException("No login");
                }
                byte[] nonce = message.getBytes("serverRandom");
                // Generate the key
                key = prng.scramble(nonce);
                break;

            // Login failure (update?)
            case LoginFailed:
                log.info("Login failed: {}", message.get("failureReason"));
                break;

            default:
                log.warn("Pdu {} before key exchange", message.getType());
        }
    }
}
