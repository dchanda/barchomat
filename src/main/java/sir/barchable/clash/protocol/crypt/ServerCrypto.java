package sir.barchable.clash.protocol.crypt;

import com.sun.corba.se.spi.activation.Server;
import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.encoders.Encoder;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.PublicKey;

import java.util.Arrays;


/**
 * This class encapsulates the cryto protocol and logic when acting as a CoC Server.
 *
 */
public class ServerCrypto extends BaseCrypto {
    ClientCrypto clientCrypto;

    public ServerCrypto(ClientCrypto clientCrypto) {
        super("1891d401fadb51d25d3a9174d472a9f691a45b974285d47729c45c6538070d85");
        this.clientCrypto = clientCrypto;
    }

    @Override
    public byte[] encrypt(byte[] b) {
        return new byte[0];
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
            decryptedBytes = super.decrypt(encryptedBytes, nonce);
            //Future decryption NONCE is in the payload:
            if ( decryptionNonce  == null ) {
                decryptionNonce = new CoCNonce(Arrays.copyOfRange(decryptedBytes, 24, 48));
                this.clientCrypto.encryptionNonce = new CoCNonce(Arrays.copyOfRange(decryptedBytes, 24, 48));
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

    public static void main(String...args) {
        ServerCrypto crypto = new ServerCrypto(null);

        PublicKey pk = new PublicKey("469b704e7f6009ba8fc72e9b5c864c8e9285a755c5190f03f5c74852f6d9f419");

        byte[] nonce = new Hex().decode("80c7ff07cb5b3ecbcfbd788ef3920f21d07013ae344fd08e");

        Box box = new Box(pk, crypto.myKeyPair.getPrivateKey());

        System.out.println( new Hex().encode(box.encrypt(nonce, new Hex().decode("1234567890abcdef"))));
    }
}
