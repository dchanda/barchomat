package sir.barchable.clash.protocol.crypt;

import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.keys.KeyPair;
import sir.barchable.util.ArrayUtils;

import java.util.Arrays;


/**
 * This class encapsulates the cryto protocol and logic when acting as a CoC Server.
 *
 */
public class ServerCrypto extends BaseCrypto {

    public ServerCrypto() {
        super("1891d401fadb51d25d3a9174d472a9f691a45b974285d47729c45c6538070d85");
    }

    @Override
    public byte[] encrypt(byte[] b) {
        if ( encryptionNonce == null ) {
            CoCNonce nonce = new CoCNonce(decryptionNonce, getPeerPublicKey(), myKeyPair.getPublicKey());
            encryptionNonce = new CoCNonce();
            KeyPair sharedKeyPair = new KeyPair();
            Box sharedKeyBox = new Box(sharedKeyPair.getPublicKey(), sharedKeyPair.getPrivateKey());
            byte[] secretBoxBytes = sharedKeyBox.getSharedKey();
            byte[] enhancedMessage = ArrayUtils.join(encryptionNonce.getBytes(), secretBoxBytes, b);
            byte[] encrypted = encrypt(enhancedMessage, nonce);
            this.sharedKey = sharedKeyBox;
            return encrypted;
        }

        return encrypt(b, null);
    }

    @Override
    public byte[] decrypt(byte[] b) {
        byte[] decryptedBytes;
        byte[] encryptedBytes = b;

        CoCNonce nonce = null;

        if (getPeerPublicKey() == null && b.length > 32) {
            setPeerPublicKey(Arrays.copyOf(b, 32));
            encryptedBytes = Arrays.copyOfRange(b, 32, b.length);
            nonce = new CoCNonce(getPeerPublicKey(), myKeyPair.getPublicKey());
        }

        if ( nonce != null ) {
            decryptedBytes = decrypt(encryptedBytes, nonce);
            //Future decryption NONCE is in the payload:
            if ( decryptionNonce  == null ) {
                decryptionNonce = new CoCNonce(Arrays.copyOfRange(decryptedBytes, 24, 48));
                decryptedBytes = Arrays.copyOfRange(decryptedBytes, 48, decryptedBytes.length);
            }
        } else {
            decryptedBytes = decrypt(encryptedBytes, null);
        }

        return decryptedBytes;
    }

    @Override
    public void setKey(byte[] key) {

    }

}
