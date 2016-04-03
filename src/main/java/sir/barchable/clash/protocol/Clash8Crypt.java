package sir.barchable.clash.protocol;

import org.abstractj.kalium.NaCl;
import org.abstractj.kalium.crypto.Box;
import org.abstractj.kalium.encoders.Hex;
import org.abstractj.kalium.keys.KeyPair;
import org.abstractj.kalium.keys.PrivateKey;
import sir.barchable.util.Cipher;


/**
 * Created by sankala on 3/29/16.
 */
public class Clash8Crypt implements Cipher {
    byte[] key;

    public Clash8Crypt() {
        NaCl.init();
        KeyPair keyPair = new KeyPair("1891d401fadb51d25d3a9174d472a9f691a45b974285d47729c45c6538070d85", new Hex());
        System.out.println(keyPair.getPublicKey().toString());
    }

    @Override
    public byte[] encrypt(byte[] b) {
        if ( key == null )
            return b;
        else
            return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] b) {
        return b.clone();
    }

    @Override
    public void setKey(byte[] key) {

    }

    public static void main(String...args) {
        new Clash8Crypt();
    }
}
