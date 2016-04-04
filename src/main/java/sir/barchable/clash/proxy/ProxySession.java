package sir.barchable.clash.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.barchable.clash.model.SessionState;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.MessageFactory;
import sir.barchable.clash.protocol.crypt.ClientCrypto;
import sir.barchable.clash.protocol.crypt.ServerCrypto;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Used to push data through the pipes until EOF.
 *
 * @author Sir Barchable
 *         Date: 15/04/15
 */
public class ProxySession {
    private static final Logger log = LoggerFactory.getLogger(ProxySession.class);

    private AtomicBoolean running = new AtomicBoolean(true);

    private MessageFactory messageFactory;
    private PduFilterChain filterChain;
    private Connection clientConnection;
    private Connection serverConnection;

    private SessionState sessionState = new SessionState();

    /**
     * When the pipe threads finish they wait here.
     */
    private CountDownLatch latch = new CountDownLatch(2);

    private ProxySession(MessageFactory messageFactory, Connection clientConnection, Connection serverConnection, PduFilter... filters) {
        this.messageFactory = messageFactory;
        this.clientConnection = clientConnection;
        this.serverConnection = serverConnection;
        if ( filters != null ) {
            if ( filters.length==1 && filters[0] instanceof PduFilterChain )
                this.filterChain = (PduFilterChain) filters[0];
            else
                this.filterChain = new PduFilterChain(filters);
        }
    }

    /**
     * Get the session that your thread is participating in.
     *
     * @return your session, or null
     */
    public static ProxySession getSession() {
        return localSession.get();
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    /**
     * Thread local session.
     */
    private static final InheritableThreadLocal<ProxySession> localSession = new InheritableThreadLocal<>();

    /**
     * Proxy a connection from a client to a clash server. This will block until processing completes, or until the
     * calling thread is interrupted.
     * <p>
     * Normal completion is usually the result of an EOF on one of the input streams.
     */
    public static ProxySession newSession(MessageFactory messageFactory, Connection clientConnection, Connection serverConnection, PduFilter... filters) throws IOException {
        ProxySession session = new ProxySession(messageFactory, clientConnection, serverConnection, filters);
        localSession.set(session);
        try {
            session.start();
            session.await();
        } catch (InterruptedException e) {
            session.shutdown();
        } finally {
            localSession.set(null);
        }
        return session;
    }

    private void start() throws IOException {
        // A pipe for messages from client -> server
        Pipe clientPipe = new Pipe(clientConnection.getName(), clientConnection.getIn(), serverConnection.getOut());
        // A pipe for messages from server -> client
        Pipe serverPipe = new Pipe(serverConnection.getName(), serverConnection.getIn(), clientConnection.getOut());

        SessionKeyTap sessionKeyListener = new SessionKeyTap();
        PduFilter loginFilter = filterChain.addAfter(new MessageTapFilter(messageFactory, sessionKeyListener));

        //Get the session Key.
        do {
            clientPipe.filterThrough(loginFilter);
            serverPipe.filterThrough(loginFilter);
        } while (sessionKeyListener.getSessionKey() == null);

        //Start cryptography for streams
        ClientCrypto clientCrypto = new ClientCrypto();
        ServerCrypto serverCrypto = new ServerCrypto();
        clientCrypto.setSessionKey( sessionKeyListener.getSessionKey() );
        clientConnection.setCipher( serverCrypto );
        serverConnection.setCipher( clientCrypto );

        // Proxy messages from client -> server
        runPipe(clientPipe);

        // Proxy messages from server -> client
        runPipe(serverPipe);
    }

    /**
     * This is used by {@link #newSession} to wait for the pipe threads to complete.
     */
    void await() throws InterruptedException {
        latch.await();
    }

    private void runPipe(Pipe pipe) {
        Thread t = new Thread(() -> {
            try {
                while (running.get()) {
                    pipe.filterThrough(filterChain);
                }
            } catch (EOFException e) {
                log.debug("{} at EOF", pipe.getName());
            } catch (IOException e) {
                log.debug("{} IOException", pipe.getName());
            } catch (RuntimeException e) {
                // It broke unexpectedly
                log.debug("{} closed with exception", pipe.getName(), e);
            }
            latch.countDown();
        }, "Pipe thread for " + pipe.getName());
        t.setDaemon(true);
        t.start();
    }

    /**
     * A hint that processing should stop. Just sets a flag and waits for the processing threads to notice. If you
     * really want processing to stop in a hurry close the input streams.
     */
    public void shutdown() {
        running.set(false);
    }
}
