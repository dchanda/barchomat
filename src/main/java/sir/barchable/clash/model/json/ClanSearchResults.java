package sir.barchable.clash.model.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonAutoDetect(fieldVisibility = ANY)
@JsonInclude(NON_NULL)
public class ClanSearchResults {
	public String searchAlliance;
	public SearchEntry[] searchEntries;
	
    @JsonAutoDetect(fieldVisibility = ANY)
    @JsonInclude(NON_NULL)
	public static class SearchEntry
	{
    	public Long clanId;
    	public String clanName;
    	public Integer clanBadge;
    	public Integer inviteType;
    	public Integer currentMembers;
    	public Integer clanTrophies;
    	public Integer requiredTrophies;
    	public Integer warsWon;
    	public Integer warsLost;
    	public Integer warsTied;
    	public Integer clanLanguage;
    	public Integer clanWarFrequency;
    	public Integer clanLocation;
    	public Integer clanPerksPoints;
    	public Integer clanLevel;	
	}
}
