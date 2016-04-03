package sir.vertex.clash.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Console;

import sir.barchable.clash.ClashServices;
import sir.barchable.clash.Main;
import sir.barchable.clash.ResourceException;
import sir.barchable.clash.model.SessionState;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.model.json.Village.Building;
import sir.barchable.clash.protocol.*;
import sir.barchable.clash.proxy.MessageTapFilter;
import sir.barchable.clash.proxy.PduFilter;
import sir.barchable.clash.proxy.PduFilterChain;
import sir.barchable.clash.proxy.ProxySession;
import sir.barchable.clash.server.ServerSession;
import sir.barchable.clash.protocol.Message;
import sir.barchable.util.Hex;
import sir.vertex.clash.client.Settings.Account;
import sir.vertex.clash.client.command.Dispatcher;
import sir.multiply.clash.messagequeue.MessageQueue;
import sir.multiply.clash.client.AutoKeepAlive;
import sir.multiply.clash.api.API;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static sir.barchable.clash.model.ObjectType.OID_RADIX;
import static sir.barchable.clash.protocol.Pdu.Type.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ClientSession {
    private static final Logger log = LoggerFactory.getLogger(ServerSession.class);
    private AtomicBoolean running = new AtomicBoolean(true);
    private Connection serverConnection;
    private MessageFactory messageFactory;
    private Account account;
    private MessageCreator messageCreator; 

    private SessionState sessionState = new SessionState();
    private Dispatcher dispatcher;
    private MessageQueue messageQueue;

    /**
     * Get the session that your thread is participating in.
     *
     * @return your session, or null
     */
    public static ClientSession getSession() {
        return localSession.get();
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    /**
     * Thread local session.
     */
    private static final InheritableThreadLocal<ClientSession> localSession = new InheritableThreadLocal<>();

    private ClientSession(ClashServices services, Connection serverConnection, Main.ClientCommand command, Account account) throws IOException {
        this.messageFactory = services.getMessageFactory();
        this.serverConnection = serverConnection;
        this.account = account;
        messageCreator = new MessageCreator(messageFactory);
        dispatcher = new Dispatcher(serverConnection,messageFactory);
        messageQueue = new MessageQueue(serverConnection, messageFactory);
    }

    public static ClientSession newSession(ClashServices services, Connection serverConnection, Main.ClientCommand command, Account account) throws IOException {
        ClientSession session = new ClientSession(services, serverConnection, command,account);
        try {
            //
            // We run the uninterruptable IO in a separate thread to maintain an interruptable controlling thread from
            // which can stop processing by closing the input stream.
            //
            Thread t = new Thread(session::run, serverConnection.getName() + " server");
            t.start();
            t.join();
        } catch (InterruptedException e) {
            session.shutdown();
        } finally {
        }
        return session;
    }

    /**
     * Process the key exchange then loop to process PDUs from the client.
     */
    private void run() {
        try {
            Message loginMessage = messageCreator.loginMessage(account);

            serverConnection.getOut().write(messageFactory.toPdu(loginMessage));

            LoginKeyTap keyListener = new LoginKeyTap();    
            keyListener.setPrng(new Clash7Random((Integer) loginMessage.get("clientSeed")));
            PduFilter loginFilter = new MessageTapFilter(messageFactory, keyListener);

            log.debug("Key exchange");
            do {
                log.debug("Try next package");
                loginFilter.filter(serverConnection.getIn().read());
            } while (keyListener.getKey() == null);
            log.debug("Key received");

            byte[] key = keyListener.getKey();
            // Re-key the stream
            serverConnection.setKey(key);

            processRequests(serverConnection);
        } catch (PduException | IOException e) {
            log.error("Key exchange did not complete: " + e, e);
        }
    }

    private void processRequests(Connection connection) {
        new AutoKeepAlive(this.messageQueue);
        new API(this.messageQueue, this.messageFactory);

        try {
            while (running.get()) {
                try {
                    Pdu pdu = connection.getIn().read();
                    Message message = messageFactory.fromPdu(pdu);

                    log.debug("Receive {} ({}): {}", message.getTypeName(), Integer.toString(pdu.getId()), message.toString());

                    dispatcher.dispatch(message);
                    messageQueue.process(pdu, message);
                }
                catch (TypeException e) {
                    log.warn("Type Exception: {}", e.getMessage());
                }
                catch (PduException e) {
                    log.warn("Message definition is missing: {}", e);
                }
            }

            log.info("{} done", connection.getName());
        } catch (RuntimeException | IOException e) {
            log.info(
                "{} terminating: {}",
                connection.getName(),
                e
            );
        }
    }

    /**
     * A hint that processing should stop. Just sets a flag and waits for the processing threads to notice. If you
     * really want processing to stop in a hurry close the input streams.
     */
    public void shutdown() {
        running.set(false);
    }
}
