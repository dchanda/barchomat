package sir.vertex.clash.client;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import sir.barchable.clash.ClashProxy;
import sir.barchable.clash.ResourceException;
import sir.barchable.clash.model.Army;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.protocol.Protocol;
import sir.barchable.clash.protocol.Protocol.StructDefinition;
import sir.barchable.clash.protocol.Protocol.StructDefinition.FieldDefinition;
import sir.barchable.util.Json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Settings {
	private static final Logger log = LoggerFactory.getLogger(Settings.class);

	private List<Account> accounts;

	public Settings() {
		accounts = new ArrayList<Account>();
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void AddAccount(Account acc) {
		accounts.add(acc);
	}

	public Account GetAccount(String name) {
		for (Account account : accounts) {
			if (name.equals(account.name)) {
				return account;
			}
		}
		return null;
	}
	
	public Account GetAccount(Integer position) {
		if (position < accounts.size())
			return accounts.get(position);
		return null;
	}

	/**
	 * Reads file with account settings.
	 */

	public void read(File file) {
		try {
			log.debug("Reading settings from {}", file.getCanonicalPath());
			accounts = Arrays.asList(Json.read(file, Account[].class));

		} catch (IOException e) {
			throw new ResourceException("Could not read " + file.getName(), e);
		}
	}

	@JsonAutoDetect(fieldVisibility = ANY)
	@JsonInclude(NON_NULL)
	public static class Account {
		public String name;
		public Long userId;
		public String userToken;
		public String udid;
		public String openUdid;
		public String mac;
		public String phoneModel;
		public Integer locale;
		public String language;
		public String advertisingIdentifier;
		public String osVersion;
		public String androidDeviceId;
		public String facebookAttributionId;
		public Boolean advertisingTrackingEnabled;
		public String vendorUuid;

		@Override
		public String toString() {
			return "Account[" + "name='" + name + '\'' + ", userId=" + userId
					+ ']';
		}
	}
}
