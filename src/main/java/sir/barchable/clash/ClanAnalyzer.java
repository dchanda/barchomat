package sir.barchable.clash;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import sir.barchable.clash.model.Logic;
import sir.barchable.clash.model.json.AllianceData;
import sir.barchable.clash.model.json.Village;
import sir.barchable.clash.protocol.Message;
import sir.barchable.clash.proxy.MessageTap;



public class ClanAnalyzer implements MessageTap {

	private static final Logger log = LoggerFactory.getLogger(ClanAnalyzer.class);

    private Logic logic;
    
	public ClanAnalyzer(Logic logic) {
		 this.logic = logic;
	}

	@Override
	public void onMessage(Message message) {
		String data = message.toString();

        switch (message.getType()) {
            case AllianceData:
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                	AllianceData clanData =  objectMapper.readValue(data, AllianceData.class);
                	analyzeClanInfo(clanData);
                } catch (RuntimeException | IOException e) {
                    log.warn("Could not read village", e);
                }
                break;
        }
		
	}
	
	private void analyzeClanInfo(AllianceData clanData) {

        log.info("=================================================");
        log.info("Clan name: {}", clanData.clanName);
        log.info("Wars Won: {}", clanData.clanWarsWon);
        log.info("Wars Lost: {}", clanData.clanWarsTied);
        log.info("Wars Tied: {}", clanData.clanWarsLost);
        log.info("Clan Desc: {}", clanData.clanDescription);        
        log.info("-------------------------------------------------");
        int optInCount = 0;
        int troopsRecieved = 0;
        int troopsDonated = 0;
        for (AllianceData.Members member : clanData.field20 ) {
        	String info = "";
        	switch(member.clanWarPreference)
        	{
        	case 0:
        		info = "N";
        		break;
        	case 1:
        		info = "Y";
        		optInCount++;
        		break;
        	default:
        		info = "" + member.clanWarPreference;
        	}
        	troopsRecieved += member.troopsReceived;
        	troopsDonated += member.troopsDonated;
        	info = info + " | " + member.name + " | " + member.trophies + " | " + member.troopsDonated + " | " + member.troopsReceived;
        	log.info("{}", info);
        }
        log.info("-------------------------------------------------");
        log.info("Total OptIns: {}", optInCount);   
        log.info("Total Troops Donated: {}", troopsDonated);   
        log.info("Total Troops Requested: {}", troopsRecieved);   
        log.info("=================================================");
	}

	
}
