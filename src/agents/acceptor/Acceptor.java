
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.acceptor;

import util.*;
import config.*;
import communication.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import javax.swing.Timer;

public class Acceptor implements ActionListener
{	final String CLASS = "Acceptor";
	final AGENT_TYPE AGENT = AGENT_TYPE.ACCEPTOR;

	public static final int MILLISECONDS = 1000;
	static int HEARTBEAT_INTERVAL = 3*MILLISECONDS; // {x}*milliseconds

	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS (by convention all global class members are prefixed with an underscore "_")
	//-------------------------------------------------------------------------------------------------------

	//Proposer: AgentInfo Contains { uid, id, AGENT_TYPE } 
	AgentInfo _agentInfo = null;

	Map<Integer,Instance> _instances = null;
	volatile Instance _highestInstanceAccepted = null;

	//Communication
	MulticastSender _proposers = null;
	MulticastReceiver _acceptors = null;
	MulticastSender _learners = null;

	//heartbeat
	Timer _heatbeatTimer = null;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public Acceptor(Config config, int id) {
		//Acceptor
		_agentInfo = new AgentInfo(UUID.randomUUID(), id, AGENT_TYPE.PROPOSER);

		_instances = new HashMap<Integer,Instance>();

		//Initialize Communication
		_proposers = new MulticastSender( id, config.getProposersIP(), config.getProposersPort() );
		_acceptors = new MulticastReceiver( _agentInfo.getUID(), id, config.getAcceptorsIp(), config.getAcceptorsPort() );
		_acceptors.setNotifyMessageReceivedCallback(doOnAcceptorMessageReceived);
		_learners = new MulticastSender( id, config.getLearnersIP(), config.getLearnersPort() );

		//heartbeat
		HEARTBEAT_INTERVAL = config.getProposerHeartbeatInterval()*MILLISECONDS;
		_heatbeatTimer = new Timer(HEARTBEAT_INTERVAL, this);

		//START
		_proposers.Start();
		_acceptors.Start();
		_learners.Start();
		_heatbeatTimer.start();
	}

	//-------------------------------------------------------------------------------------------------------
	//INNER OBJECTS | CLASSES
	//-------------------------------------------------------------------------------------------------------

	 final Runnable doOnAcceptorMessageReceived = new Runnable() {
	     public void run() 
	     { 	final String METHOD = "doOnProposerMessageReceived::run";

	     	Debug.out("\n\t EXECUTED | doOnAcceptorMessageReceived");

	    	 Message message = null;

	    	 try {
	    		 message = _acceptors.getQueue().take();
			} catch (InterruptedException e) {
				final String OFFENDING_CODE = "InterruptedException::message = _proposers.getMessageQueue().take()";
				Debug.Error( CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
				e.printStackTrace();
			}

	    	Debug.ToConsole(DEBUG_TYPE.RECEIVE, AGENT, "\t#"+CLASS+"::"+METHOD+"::_proposers.getMessageQueue().take()::Message" + 
	    				    "\n### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ###" + 
	    				    message.AsDisplayString() + 
			    			"### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ###\n");
	    	
	    	Debug.out("\n\t MESSAGE_TYPE: " + message.getMessageType());

	    	if( message.getMessageType() == MESSAGE_TYPE.PREPARE )
	    		OnPrepareMessageReceived(message);

		    if( message.getMessageType() == MESSAGE_TYPE.ACCEPT )
	    		OnAcceptMessageReceived(message);
		    
		    if( message.getMessageType() == MESSAGE_TYPE.RESEND )
		    	OnResendMessageReceived(message);

	     }//run
	 };

	//-------------------------------------------------------------------------------------------------------
	//EVENTS | CALLBACKS
	//-------------------------------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)  {
		if (e.getSource() == _heatbeatTimer) { OnHeartbeat(); }
  	}
	
	private void OnHeartbeat() {
		//The acceptor should periodically repeat the state of the highest instance for which some value was accepted.
		Debug.ToConsole2( DEBUG_TYPE.HEARTBEAT, AGENT_TYPE.ACCEPTOR, "Acceptor::OnHeartbeat!\n\t" + _highestInstanceAccepted );
		if (_highestInstanceAccepted != null )
			sendAccepted(_highestInstanceAccepted);
	}

	private void OnPrepareMessageReceived(Message message) {
		
		if(message == null || message.getInstance() == null) {
			Debug.ToConsole2( DEBUG_TYPE.ERROR, AGENT_TYPE.ACCEPTOR, "Acceptor::OnPrepareMessageReceived -> message/instance is null!" );
			//System.exit(1);
			return;
		}

		Instance msgInst = message.getInstance();
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "OnPrepareMessageReceived" + msgInst.AsDisplayString() );

		Instance inst = _instances.get(msgInst.I);

/* Phase 1b: Promise
 * If the proposal's number N is higher than any previous proposal number received from any Proposer by the Acceptor, 
 * then the Acceptor must return a promise to ignore all future proposals having a number less than N. 
 * If the Acceptor accepted a proposal at some point in the past, 
 * it must include the previous proposal number and previous value in its response to the Proposer. 
 */
		if (inst != null) { 							//i.e: if already have an the instance stored...
			if (msgInst.B >= inst.B) { 					//the new proposed ballot is GREATER THAN OR EQUAL
				Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "BALLOT IS BIGGER OR EQUAL -> UPDATING");
				inst.B = msgInst.B;						//update the stored instance to match the proposed one
				sendPromise(inst);						//reply: with a promised
			} else {
/* Otherwise, send a reject message
 * the Acceptor can ignore the received proposal. 
 * It does not have to answer in this case for Paxos to work. However, for the sake of optimization, sending a denial (Nack) response would tell the 
 * Proposer that it can stop its attempt to create consensus with proposal N.
 */
				Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "BALLOT IS SMALLER -> REJECT");
				sendReject(inst);						//reject instance | sending a reject is not strictly necessary, however this is an optimization
			}
		} else {										//we haven't seen this instance before
			Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "NEW INSTANCE -> ADDING");
			_instances.put(msgInst.I,msgInst);			//add instance
			sendPromise(msgInst);						//reply: with a promised
		}

		displayInstances();
	}
	
	private void OnAcceptMessageReceived(Message message) {
		if(message == null || message.getInstance() == null) {
			Debug.ToConsole2( DEBUG_TYPE.ERROR, AGENT_TYPE.ACCEPTOR, "Acceptor::OnAcceptMessageReceived -> message/instance is null!" );
			//System.exit(1);
			return;
		}

		Instance msgInst = message.getInstance();
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "OnAcceptMessageReceived" + msgInst.AsDisplayString() );

		Instance inst = _instances.get(msgInst.I);

		if (inst != null) { 							//i.e: if already have an the instance stored...
			if (msgInst.B >= inst.B) { 					//and the ballot proposed for acceptance is GREATER THAN OR EQUAL to the current stored value
				Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "BALLOT IS BIGGER OR EQUAL -> UPDATING");
				inst.B = msgInst.B;						//update the stored instance's ballot, value and value ballot
				inst.V2 = msgInst.V2;						
				inst.VB = msgInst.B;
				sendAccepted(inst);						//Send an accept to the learners
			} else { 									//else
				Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "BALLOT IS SMALLER -> REJECT");
				sendReject(inst);						//send a reject message
			}
		} else {										//we haven't seen this instance before
			Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "NEW INSTANCE -> ADDING");
			_instances.put(msgInst.I,msgInst);			//add instance
			sendAccepted(msgInst);						//and reply with an accept to learners
		}
	}

	//-------------------------------------------------------------------------------------------------------
	// MESSAGING
	//-------------------------------------------------------------------------------------------------------	

	private void OnResendMessageReceived(Message msg) {

		if (msg == null || msg.getInstance() == null) {
			Debug.Error( AGENT_TYPE.ACCEPTOR, CLASS, "OnResendMessageReceived", "message == null || message.getInstance() == null", "NULL VALUE EXCEPTION... EXITING." );
			//System.exit(1);
			return;
		}

		Instance inst = _instances.get(msg.getInstance().I);
		if(inst == null) return;

		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "OnResendMessageReceived" + inst.AsDisplayString() );

		sendAccepted(inst);
	}

	//send promise to proposers
	private void sendPromise(Instance instance) { 
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "\n\n\t EXECUTING... | Sending Promise\n" + instance.AsDisplayString() );

		Message msg = new Message(MESSAGE_TYPE.PROMISE, AGENT_TYPE.ACCEPTOR, _agentInfo, instance);
		_proposers.DispatchMessage(msg);

		Debug.Line(true);
	}

	//send reject to proposers
	private void sendReject(Instance instance) {
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "\n\n\t EXECUTING... | Sending Reject\n" + instance.AsDisplayString() );

		Message msg = new Message(MESSAGE_TYPE.REJECT, AGENT_TYPE.ACCEPTOR, _agentInfo, instance);
		_proposers.DispatchMessage(msg);

		Debug.Line(true);
	}

	//send accept to learners
	private void sendAccepted(Instance instance) {
		if(instance == null) {
			Debug.ToConsole2( DEBUG_TYPE.ERROR, AGENT_TYPE.ACCEPTOR, "Acceptor::sendAccept -> instance is null!" );
			//System.exit(1);
			return;
		}

		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, instance.AsDisplayString() );

		if ( instance.V2 != null && (_highestInstanceAccepted == null || _highestInstanceAccepted.I < instance.I) )
			_highestInstanceAccepted = instance;	

		Message msg = new Message(MESSAGE_TYPE.ACCEPTED, AGENT_TYPE.ACCEPTOR, _agentInfo, instance);
		_learners.DispatchMessage(msg);

		Debug.Line(true);
	}

	//-------------------------------------------------------------------------------------------------------

	private void displayInstances()
	{
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "\tDICTIONARY OF INSTANCES\n");
		
		Iterator<Entry<Integer,Instance>> it =  _instances.entrySet().iterator();

		while(it.hasNext()){
			Instance inst = it.next().getValue();
			Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, inst.toString() );
		}
		
		Debug.Line(true);
	}
	
	//-------------------------------------------------------------------------------------------------------

}//class
