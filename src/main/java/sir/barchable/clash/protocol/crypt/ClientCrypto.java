package sir.barchable.clash.protocol.crypt;

import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.PublicKey;
import sir.barchable.util.ArrayUtils;

/**
 * To be used when talking to a Clash of Clans Server. Has logic for client crypto protocol
 */
public class ClientCrypto extends BaseCrypto {

    public ClientCrypto() {
//        setPeerPublicKey(new PublicKey("72f1a4a4c48e44da0c42310f800e96624e6dc6a641a9d41c3b5039d8dfadc27e").toBytes());
        setPeerPublicKey(new PublicKey("469b704e7f6009ba8fc72e9b5c864c8e9285a755c5190f03f5c74852f6d9f419").toBytes());
    }

    @Override
    public byte[] encrypt(byte[] b) {
        CoCNonce nonce = new CoCNonce(myKeyPair.getPublicKey(), getPeerPublicKey());
        byte[] enhancedMessage = ArrayUtils.join(new Hex().decode(sessionKey), encryptionNonce.getBytes(), b);
        byte[] encrypted = encrypt(enhancedMessage, nonce);
        return ArrayUtils.join(myKeyPair.getPublicKey().toBytes(), encrypted);
    }

    @Override
    public byte[] decrypt(byte[] b) {
        return b;
    }

    @Override
    public void setKey(byte[] key) {

    }
}
