package sir.vertex.clash.client.command;

import java.util.HashMap;

import sir.vertex.clash.client.command.GetTimeCommand;
public class CommandList {
	// Class not using!
	HashMap<String, Class<?>> hash;
	
	public CommandList() {
		hash = new HashMap<String, Class<?>>();
		hash.put("getTime",  GetTimeCommand.class);
		hash.put("whenGems",  WhenGemsCommand.class);
		hash.put("warRecord", WarRecordCommand.class);
	}
		
	public Class<?> getCommand(String command){
		return hash.get(command);
	}
	public boolean isCommand(String command){
		return hash.containsKey(command);
	}
}
