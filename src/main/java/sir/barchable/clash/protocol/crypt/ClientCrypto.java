package sir.barchable.clash.protocol.crypt;

import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.keys.PublicKey;
import sir.barchable.util.ArrayUtils;

import java.util.Arrays;

/**
 * To be used when talking to a Clash of Clans Server. Has logic for client crypto protocol
 */
public class ClientCrypto extends BaseCrypto {

    public ClientCrypto() {
        setPeerPublicKey(new PublicKey("469b704e7f6009ba8fc72e9b5c864c8e9285a755c5190f03f5c74852f6d9f419").toBytes());
    }

    @Override
    public byte[] encrypt(byte[] b) {
        CoCNonce nonce = new CoCNonce(myKeyPair.getPublicKey(), getPeerPublicKey());
        if ( encryptionNonce == null ) {
            encryptionNonce = new CoCNonce();
            byte[] enhancedMessage = ArrayUtils.join(sessionKey, encryptionNonce.getBytes(), b);
            byte[] encrypted = encrypt(enhancedMessage, nonce);
            return ArrayUtils.join(myKeyPair.getPublicKey().toBytes(), encrypted);
        }

        return encrypt(b, null);
    }

    @Override
    public byte[] decrypt(byte[] b) {
        if ( decryptionNonce == null ) {
            CoCNonce nonce = new CoCNonce(encryptionNonce, myKeyPair.getPublicKey(), getPeerPublicKey());
            byte[] decrypted = decrypt(b, nonce);
            decryptionNonce = new CoCNonce(Arrays.copyOfRange(decrypted, 0, 24));
            this.sharedKey = new Box( Arrays.copyOfRange(decrypted, 24,56) );
            return Arrays.copyOfRange(decrypted, 56, decrypted.length);
        } else {
            return decrypt(b, null);
        }
    }

    @Override
    public void setKey(byte[] key) {

    }
}
