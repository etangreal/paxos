/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import java.io.Serializable;
import java.util.UUID;

public class AgentInfo implements Serializable
{
	static final long serialVersionUID = 6088581115000207361L;
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	UUID _uid = null;

	int _id = 0; 			//this is the proposer ID - each proposer should have a unique id 
							//unique is a question? in which sense? 
							//	such that no two proposers that are currently running have the same id
							//	or such that no two proposers ever have the same id again? i.e. a uuid (universally unique id)
							//The question is partially answered by the fact that we would like to use the proposer ID as part of the ballot ...
							//For this reason it should be a simple id - i.e: 1, 2, 3, 4, 5, 6

	AGENT_TYPE _agentType = AGENT_TYPE.UNDEFINED;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public AgentInfo()
	{
		_uid = UUID.randomUUID();
		_id = 0;
		_agentType = AGENT_TYPE.UNDEFINED;
	}

	public AgentInfo(UUID uid, int id, AGENT_TYPE fromAgent)
	{
		setInfo(uid,id,fromAgent);
	}

	//-------------------------------------------------------------------------------------------------------
	//PROPERTIES
	//-------------------------------------------------------------------------------------------------------

	public UUID getUID() {
		return _uid;
	}

	public void setUID(UUID uid) {
		_uid = uid;
	}

	public int getID() {
		return _id;
	}

	public void setID(int id) {
		_id = id;
	}

	public AGENT_TYPE getAgentType() {
		return _agentType;
	}

	public void setAgentType(AGENT_TYPE agentType) {
		_agentType = agentType;
	}

	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------

	public void setInfo(UUID uid, int id, AGENT_TYPE agentType)
	{
		_uid = uid;
		_id = id;
		_agentType = agentType;
	}

	public void Copy(AgentInfo agentInfo)
	{
		_uid = agentInfo.getUID();
		_id = agentInfo.getID();
		_agentType = agentInfo.getAgentType();
	}

	public String toString()
	{
		return _uid.toString() + ";" + _id + ";" + _agentType.toString() + ";";
	}

	public String AsDisplayString()
	{
		return  "\n"+
				"\n------------------- AGENT INFO -------------------\n" +
				"\t Agent UUID: " + _uid + "\n" +
				"\t Agent ID: " + _id + "\n" +
				"\t Agent Type: " + _agentType.toString() + "\n"; 
	}

	//-------------------------------------------------------------------------------------------------------

}//public class AgentInfo 
