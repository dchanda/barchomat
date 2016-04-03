package sir.barchable.clash.proxy;

import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.protocol.Pdu;

/**
 * Created by sankala on 4/1/16.
 */
public class SessionKeyTap implements MessageTap {
    private String sessionKey = null;

    public String getSessionKey() {
        return sessionKey;
    }

    @Override
    public void onMessage(Message message) {
        if ( message.getType() == Pdu.Type.ServerHandshake ) {
            sessionKey = message.getString("sessionKey");
        }
    }

}
