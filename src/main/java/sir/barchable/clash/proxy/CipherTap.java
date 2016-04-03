package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.protocol.Clash7Random;
import sir.barchable.clash.protocol.Message;

/**
 * Created by sankala on 4/1/16.
 */
public class CipherTap implements MessageTap {
    private static final Logger log = LoggerFactory.getLogger(CipherTap.class);

    @Override
    public void onMessage(Message message) {
        switch (message.getType()) {

            case Login:
                break;
        }
    }
}
