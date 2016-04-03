package sir.barchable.clash.protocol.crypt;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.crypto.Random;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PublicKey;
import sir.barchable.util.Blake2b;
import sir.barchable.util.Cipher;

import java.util.Arrays;

/**
 * Created by sankala on 3/31/16.
 */
public abstract class BaseCrypto implements Cipher {
    protected KeyPair myKeyPair;
    protected PublicKey peerPublicKey;

    protected String sessionKey;

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
        return sharedKey.decrypt( nonce.getBytes(), b);
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
        this.sessionKey = sessionKey;
    }

    public static class CoCNonce {

        byte[] nonce;

        public CoCNonce(byte[] nonce) {
            this.nonce = Arrays.copyOf(nonce, nonce.length);
        }

        public CoCNonce(PublicKey clientPubKey, PublicKey serverPubKey) {
            Blake2b.Digest digest = Blake2b.Digest.newInstance(24);
            if ( nonce != null )
                digest.update(nonce);
            digest.update(clientPubKey.toBytes());
            digest.update(serverPubKey.toBytes());
            nonce = digest.digest();
        }

        public CoCNonce() {
            nonce = new Random().randomBytes(NaCl.Sodium.NONCE_BYTES);
        }

        public byte[] getBytes() {
            return nonce;
        }

        public void increment() {}
    }


}

/*

def increment(self):
        self._nonce = (int.from_bytes(self._nonce, byteorder="little") + 2).to_bytes(Box.NONCE_SIZE, byteorder="little")

 */



/*

class CoCNonce:

    def __init__(self, nonce=None, clientkey=None, serverkey=None):
        if not clientkey:
            if nonce:
                self._nonce = nonce
            else:
                self._nonce = nacl.utils.random(Box.NONCE_SIZE)
        else:
            b2 = blake2b(digest_size=24)
            if nonce:
                b2.update(bytes(nonce))
            b2.update(bytes(clientkey))
            b2.update(bytes(serverkey))
            self._nonce = b2.digest()

    def __bytes__(self):
        return self._nonce

    def __len__(self):
        return len(self._nonce)

    def increment(self):
        self._nonce = (int.from_bytes(self._nonce, byteorder="little") + 2).to_bytes(Box.NONCE_SIZE, byteorder="little")




 */