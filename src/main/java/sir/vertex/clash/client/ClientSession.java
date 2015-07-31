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
import sir.barchable.util.Hex;
import sir.vertex.clash.client.Settings.Account;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
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
    private PduFilterChain filterChain;
    
    private SessionState sessionState = new SessionState();
    
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

	 private ClientSession(ClashServices services, Connection serverConnection, Main.ClientCommand command, Account account,PduFilter... filters) throws IOException {
		 	this.messageFactory = services.getMessageFactory();
	        this.serverConnection = serverConnection;
	        this.account = account;
	        this.filterChain = new PduFilterChain(filters);
	    }

	 public static ClientSession newSession(ClashServices services, Connection serverConnection, Main.ClientCommand command, Account account,PduFilter... filters) throws IOException {
	        ClientSession session = new ClientSession(services, serverConnection, command,account,filters);
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
	        	Message loginMessage = messageFactory.newMessage(Login);

	        	loginMessage.set("userId", account.userId);
	        	loginMessage.set("userToken", account.userToken);
	        	loginMessage.set("udid", account.udid);
	        	loginMessage.set("openUdid", account.openUdid);
	        	loginMessage.set("mac", account.mac);
	        	loginMessage.set("phoneModel", account.phoneModel);
	        	loginMessage.set("locale", account.locale);
	        	loginMessage.set("language", account.language);
	        	loginMessage.set("advertisingIdentifier", account.advertisingIdentifier);
	        	loginMessage.set("osVersion", account.osVersion);
	        	loginMessage.set("androidDeviceId", account.androidDeviceId);
	        	loginMessage.set("facebookAttributionId", account.facebookAttributionId);
	        	loginMessage.set("advertisingTrackingEnabled", account.advertisingTrackingEnabled);
	        	loginMessage.set("vendorUuid", account.vendorUuid);
	        	loginMessage.set("clientSeed", 14870668); // Not good, make random
	        	
	        	loginMessage.set("majorVersion", 7); // Not good, were store constants?
	        	loginMessage.set("minorVersion", 156); // Not good, were store constants?
	        	loginMessage.set("masterHash", "77d150027859d9ad0d37fb30696c66cdbd2a0d9e"); // Not good, make dynamic
	        	
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
	    	Timer autoSendtimer = new Timer();
	    	autoSendtimer.schedule(new AutoSend(serverConnection.getOut(),messageFactory), 0, 5000);
	        try {
	            while (running.get()) {
	            	log.debug("Receive");
	                //
	                // Read a request PDU
	                //

	                Pdu pdu = connection.getIn().read();
	                
	                filterChain.filter(pdu);

	            }

	            log.info("{} done", connection.getName());
	        } catch (RuntimeException | IOException e) {
	            log.info(
	                "{} terminating: {}",
	                connection.getName(),
	                e
	            );
	        }
	        autoSendtimer.cancel();
	    }

	    /**
	     * A hint that processing should stop. Just sets a flag and waits for the processing threads to notice. If you
	     * really want processing to stop in a hurry close the input streams.
	     */
	    public void shutdown() {
	        running.set(false);
	    }
	 
}