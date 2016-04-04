package sir.barchable.clash.protocol.crypt;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.crypto.Random;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PublicKey;
import sir.barchable.util.Blake2b;
import sir.barchable.util.Cipher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by sankala on 3/31/16.
 */
public abstract class BaseCrypto implements Cipher {
    protected KeyPair myKeyPair;
    protected PublicKey peerPublicKey;

    protected byte[] sessionKey;

    protected Box sharedKey;

    protected CoCNonce encryptionNonce;
    protected CoCNonce decryptionNonce;

    public BaseCrypto() {
        myKeyPair = new KeyPair();
    }

    public BaseCrypto(String privateKey) {
        myKeyPair = new KeyPair(privateKey, new Hex());
    }


    protected byte[] decrypt(byte[] b, CoCNonce nonce) {
        if (nonce == null) {
            decryptionNonce.increment();
            nonce = decryptionNonce;
        }
        return sharedKey.decrypt(nonce.getBytes(), b);
    }

    protected byte[] encrypt(byte[] b, CoCNonce nonce) {
        if ( nonce == null ) {
            encryptionNonce.increment();
            nonce = encryptionNonce;
        }
        return sharedKey.encrypt(nonce.getBytes(), b);
    }

    protected void setPeerPublicKey(byte[] peerPublicKey) {
        this.peerPublicKey = new PublicKey(peerPublicKey);
        sharedKey = new Box(peerPublicKey, myKeyPair.getPrivateKey().toBytes() );
    }

    protected PublicKey getPeerPublicKey() {
        return peerPublicKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = new Hex().decode(sessionKey);
    }


    public static class CoCNonce {

        byte[] nonce;

        public CoCNonce(byte[] nonce) {
            this.nonce = Arrays.copyOf(nonce, nonce.length);
        }

        public CoCNonce(CoCNonce nonce, PublicKey clientPubKey, PublicKey serverPubKey) {
            this(nonce.getBytes(), clientPubKey, serverPubKey);
        }

        public CoCNonce(byte[] nonce, PublicKey clientPubKey, PublicKey serverPubKey) {
            this.nonce = nonce;
            Blake2b.Digest digest = Blake2b.Digest.newInstance(24);
            if ( this.nonce != null )
                digest.update(this.nonce);
            if (clientPubKey != null) digest.update(clientPubKey.toBytes());
            if (serverPubKey != null) digest.update(serverPubKey.toBytes());
            this.nonce = digest.digest();
        }


        public CoCNonce(PublicKey clientPubKey, PublicKey serverPubKey) {
            this((byte[]) null, clientPubKey, serverPubKey);
        }

        public CoCNonce() {
            nonce = new Random().randomBytes(NaCl.Sodium.NONCE_BYTES);
        }

        public byte[] getBytes() {
            return nonce;
        }

        public synchronized void increment() {
            ByteBuffer buffer = ByteBuffer.wrap(nonce).order(ByteOrder.LITTLE_ENDIAN);
            int newInt = buffer.getInt()+2;
            buffer.position(0);
            buffer.putInt(newInt);
            nonce = buffer.array();
        }
    }
}
