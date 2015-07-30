package sir.barchable.clash.model.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(NON_NULL)
public class AllianceData {
	public Long clanId;
	public String clanName;
	public Integer clanBadge;
	public Integer clanInviteType;
	public Integer memberCount;
	public Integer totalTrophies;
	public Integer requiredTrophies;
	public Integer clanWarsWon;
	public Integer clanWarsLost;
	public Integer clanWarsTied;
	public Integer clanLanguage;
	public Integer clanWarFrequency;
	public Integer clanLocation;
	public Integer clanPerksPoints;
	public Integer field15;
	public String clanDescription;
	public Integer field17;
	public Boolean clanInWar;
	public Long warID;
	public Members[] field20;
	
    @JsonAutoDetect(fieldVisibility = ANY)
    @JsonInclude(NON_NULL)
	public static class Members
	{
    	public Long id;
    	public String name;
    	public Integer role;
    	public Integer level;
    	public Integer leagueLevel;
    	public Integer trophies;
    	public Integer troopsDonated;
    	public Integer troopsReceived;
    	public Integer rank;
    	public Integer previousRank;
    	public Boolean newMember;
    	public Integer field12;
    	public Integer clanWarPreference;
    	public Boolean field14;
    	public Long id2;
	
	}
}
