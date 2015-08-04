package sir.barchable.clash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sir.barchable.clash.ClashServices;
import sir.barchable.clash.Main;
import sir.barchable.clash.model.SessionState;
import sir.barchable.clash.protocol.Connection;
import sir.barchable.clash.protocol.Pdu;
import sir.barchable.clash.proxy.MessageLogger;
import sir.barchable.clash.proxy.MessageTapFilter;
import sir.barchable.clash.proxy.PduFilter;
import sir.barchable.clash.proxy.PduFilterChain;
import sir.barchable.clash.proxy.ProxySession;
import sir.barchable.clash.ClashProxy;
import sir.vertex.clash.client.ClientSession;
import sir.vertex.clash.client.Settings;
import sir.barchable.util.Dns;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sir Vertex Date: 1/07/15
 */
public class ClashClient {
	public static final int CLASH_PORT = 9339;

	private static final Logger log = LoggerFactory.getLogger(ClashProxy.class);

	/**
	 * Common services.
	 */
	private final ClashServices services;
	
	private SessionState sessionState = new SessionState();
	
	private final Main.ClientCommand command;
	/**
	 * Source for the address of the real server.
	 */
	private Dns dns;

	private Settings settings = new Settings();
	
    public SessionState getSessionState() {
        return sessionState;
    }
    
	public ClashClient(ClashServices services, Main.ClientCommand command) throws IOException {
		settings.read(new File(services.getWorkingDir(), "Account.json"));
		this.services = services;
		this.command = command;
		//
		// Look up the server using an external DNS because the internal one is
		// probably being used to redirect
		// the client to this client. The default, 8.8.8.8, is one of Google's
		// public DNS servers.
		//
		this.dns = new Dns(command.getNameServer());
		
	}

	public void run() throws IOException {
		try {
			InetAddress serverAddress = dns.getAddress("gamea.clashofclans.com");

			try (Socket clientSocket = new Socket(serverAddress, CLASH_PORT);
					Connection serverConnection = new Connection(clientSocket)) {
				log.info("Connecting to {}:{}", serverAddress, CLASH_PORT);
				
				ClientSession session = ClientSession.newSession(services,
						serverConnection, command, settings.GetAccount(0));
			}
			log.info("Client {} disconnected");
		} catch (IOException e) {
			log.info("Could not connect {}", e.toString());
		} finally {

		}
	}
}
