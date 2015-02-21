/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import java.util.UUID;
import java.io.Serializable;


public class Message implements Serializable
{
	static final long serialVersionUID = 7485961738069774385L;	

	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	private UUID _uid = UUID.randomUUID(); //message uuid

	private AgentInfo _agentInfo = null;

	private MESSAGE_TYPE _messageType = MESSAGE_TYPE.UNDEFINED;
	private AGENT_TYPE _toAgent = AGENT_TYPE.UNDEFINED;

	private Instance _instance = null;

	private String _test = "<undefined>";

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

//	public Message()
//	{
//		_agentInfo = new AgentInfo();
//		setMessage( MESSAGE_TYPE.UNDEFINED, AGENT_TYPE.UNDEFINED, new Instance() );
//	}

	public Message(MESSAGE_TYPE messageType, AGENT_TYPE toAgent, AgentInfo agentInfo, Instance instance) {

		_agentInfo = agentInfo;
		if (_agentInfo == null) _agentInfo = new AgentInfo();

		setMessage(messageType, toAgent, instance);
	}

	//-------------------------------------------------------------------------------------------------------
	//PROPERTIES
	//-------------------------------------------------------------------------------------------------------

	public UUID getUID() {
		return _uid;
	}

	public AgentInfo getAgentInfo() {
		return _agentInfo;
	}

	public void setMessage(MESSAGE_TYPE messageType, AGENT_TYPE toAgent, Instance instance)  {

		_instance = instance;
		if (_instance == null) _instance = new Instance();

		_messageType = messageType;
		_toAgent = toAgent;
	}

	//-------------------------------------------------------------------------

	public MESSAGE_TYPE getMessageType() {
		return _messageType;
	}

	public void setMessageType(MESSAGE_TYPE messageType) {
		_messageType = messageType;
	}

	public AGENT_TYPE getToAgent() {
		return _toAgent;
	}

	public Instance getInstance()
	{
		return _instance;
	}
	
	public void setInstance(Instance instance)
	{
		_instance = instance;
	}
	
	public String getTest() {
		return _test;
	}
	
	public void setTest(String test) {
		_test = test;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------
	
	//-------------------------------------------------------------------------------------------------------
	//OPERATORS
	//-------------------------------------------------------------------------------------------------------
	
	public String toString() {
		return
			_uid.toString() + ";" +
			_messageType.toString() + ";" + 
			_agentInfo.toString() + ";" +
			_instance.toString();
	}
	
	public String AsDisplayString() {
		return	
			_agentInfo.AsDisplayString() +
			"\n------------------- MESSAGE INFO -------------------" +
			"\n\t Message UUID: " + _uid.toString() + 
			"\n\t Message Type: " + _messageType.toString() +
			_instance.AsDisplayString();
	}
	
	//-------------------------------------------------------------------------------------------------------

}//class Message
