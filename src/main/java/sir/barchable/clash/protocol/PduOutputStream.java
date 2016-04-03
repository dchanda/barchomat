package sir.barchable.clash.protocol;

import sir.barchable.util.Cipher;
import sir.barchable.util.NoopCipher;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Write Clash PDUs.
 *
 * @author Sir Barchable
 *         Date: 6/04/15
 */
public class PduOutputStream implements Closeable {
    private OutputStream out;
    private Cipher cipher;

    /**
     * Creates a PDU output stream with a newly initialized stream cipher.
     * Call {@link #setKey(byte[])} after key exchange to reinitialize the stream cipher.
     *
     * @param out the stream to write to
     */
    public PduOutputStream(OutputStream out) {
        this(out, NoopCipher.NOOP_CIPHER);
    }

    public PduOutputStream(OutputStream out, Cipher cipher) {
        this.out = out;
        this.cipher = cipher;
    }

    public void write(Pdu pdu) throws IOException {
        byte[] encrypted = cipher.encrypt(pdu.getPayload());
        writeShort(pdu.getId());
        writeUInt3(encrypted.length);
        writeShort(pdu.getVersion());
        out.write( encrypted );
        out.flush();
    }

    private void writeUInt3(int v) throws IOException {
        out.write(v >>> 16);
        out.write(v >>> 8);
        out.write(v);
    }

    private void writeShort(int v) throws IOException {
        out.write(v >>> 8);
        out.write(v);
    }

    public void setKey(byte[] nonce) {
        cipher.setKey(nonce);
    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
