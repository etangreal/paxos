/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.learner;

import util.*;
import communication.*;
import config.Config;
import agents.proposer.ProposerInstance;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.swing.Timer;

public class Learner implements ActionListener
{	final String CLASS = "Learner";

	public static final int MILLISECONDS = 1000;
	static int RESEND_TIMEOUT = 6100;
	static int HEARTBEAT_INTERVAL = 3*MILLISECONDS; // {x}*milliseconds
	static int NUMBER_OF_ACCEPTORS = 0;

	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS (by convention all global class members are prefixed with an underscore "_")
	//-------------------------------------------------------------------------------------------------------

	//Proposer: AgentInfo Contains { uid, id, AGENT_TYPE } 
	AgentInfo _agentInfo = null;

	private boolean _showDebugMessages = true;

	Map<Integer,ProposerInstance> _pending = null;
	Map<Integer,Instance> _delivered = null;

	volatile int _hdIID = 0; 						// hdIID = Highest Delivered Instance ID

	//HeartBeat
	Timer _heatbeatTimer = null;

	//communication
	MulticastReceiver _learners = null;
	MulticastSender _acceptors = null;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public Learner(Config config, int id, boolean verbose) {
		setShowDebugMessages(verbose);

		//Acceptor
		_agentInfo = new AgentInfo(UUID.randomUUID(), id, AGENT_TYPE.LEARNER);

		NUMBER_OF_ACCEPTORS = config.getNumberOfAcceptors();
		HEARTBEAT_INTERVAL = config.getLearnerHeartbeatInterval()*MILLISECONDS;
		RESEND_TIMEOUT = config.getResendTimeout();

		_pending = new HashMap<Integer,ProposerInstance>();
		_delivered = new HashMap<Integer,Instance>();	
		_hdIID = 0;

		_learners = new MulticastReceiver(_agentInfo.getUID(), id, config.getLearnersIP(), config.getLearnersPort() );
		_learners.setNotifyMessageReceivedCallback(doOnLearnerMessageReceived);
		_acceptors = new MulticastSender(id,config.getAcceptorsIp(),config.getAcceptorsPort());

		//HeartBeat
		_heatbeatTimer = new Timer(HEARTBEAT_INTERVAL, this);

		//START
		_heatbeatTimer.start();
		_acceptors.Start();
		_learners.Start();
	}

	//-------------------------------------------------------------------------------------------------------
	//EVENTS | CALLBACKS
	//-------------------------------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e)  {
		if (e.getSource() == _heatbeatTimer) { OnHeartbeat(); }
	}
 
	private void OnHeartbeat() {
		show( Debug.ToConsole2(false, DEBUG_TYPE.HEARTBEAT, AGENT_TYPE.LEARNER, "\nLearner::OnHeartbeat hdIID: " + _hdIID) );

		int iid = _hdIID+1; //the next deliverable instance

		Iterator<Entry<Integer,ProposerInstance>> plist =  _pending.entrySet().iterator();

		while(plist.hasNext()) {
			ProposerInstance inst = plist.next().getValue();
			
			for(int i=iid; i<inst.I;i++) // if there are any holes in between (the last the hdIID and the next pending instance) OR (two pending instances)
				requestResend(i);
			
			iid = inst.I;
			
			if (inst.TS == 0) // if for some reason the instance TS was never set ... 
				inst.setTimeout(RESEND_TIMEOUT);
			
			if(inst.isTimeout()) { //if (the instance has been sitting in the pending list for some time)
				requestResend(inst.I);
				inst.setTimeout(RESEND_TIMEOUT);
			}	
		}
	}
	
	public void requestResend(int iid) {
		Message msg = new Message(MESSAGE_TYPE.RESEND, AGENT_TYPE.LEARNER, _agentInfo, new Instance());
		msg.getInstance().I = iid;
		
		show( Debug.ToConsole2(false, DEBUG_TYPE.PP_AA, AGENT_TYPE.LEARNER, "\tEXECUTED | SEND REQUEST RESEND\n" + msg.AsDisplayString()) );
		_acceptors.DispatchMessage(msg);
	}

	final Runnable doOnLearnerMessageReceived = new Runnable() {
		 public void run() { final String METHOD = "doOnLearnerMessageReceived";

			show("\n\t EXECUTED | doOnLearnerMessageReceived");

	    	Message message = null;

	    	try {
	    		message = _learners.getQueue().take();
			} catch (InterruptedException e) {
				final String OFFENDING_CODE = "InterruptedException::message = _learners.getMessageQueue().take()";
				Debug.Error( CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
				e.printStackTrace();
			}

	    	show( Debug.ToConsole2(false,DEBUG_TYPE.PP_AA, AGENT_TYPE.LEARNER, CLASS+"|"+METHOD+"|"+ message.AsDisplayString()) );

	    	if( message.getMessageType() == MESSAGE_TYPE.ACCEPTED )
	    		OnLearnMessageReceived(message);

		}//run
	};

	public void OnLearnMessageReceived(Message message) { 
		final String METHOD = "OnLearnMessageReceived";
		if (message == null || message.getInstance() == null) {
			Debug.Error( AGENT_TYPE.LEARNER, CLASS, METHOD, "message == null || message.getInstance() == null", "NULL VALUE EXCEPTION... EXITING." );
			return;
		}

		Instance msgInst = message.getInstance();
		show( Debug.ToConsole2(false, DEBUG_TYPE.PP_AA, AGENT_TYPE.LEARNER, "OnLearnMessageReceived" + msgInst.AsDisplayString()) );

		if (msgInst.I <= _hdIID) //we have already delivered the message ... ignore it
			return;

		ProposerInstance inst = _pending.get(msgInst.I); //check if it is in the pending list

		if(inst == null || (inst.B < msgInst.B) ) // if (its not in the pending list) OR (its a newer version with higher ballot) 
			inst = new ProposerInstance(msgInst); // create a new instance

		inst.addPromise(message.getAgentInfo().getID());	//in this case addPromise actually means => adding an ACCEPTED count from a specific acceptor
		inst.setTimeout(RESEND_TIMEOUT);

		_pending.put(inst.I, inst); // add/replace it to the pending list...

		tryDeliver(inst);

		displayInstances();
	}

	private void tryDeliver(ProposerInstance inst) {
		if (inst.countPromises() >= acceptorMajority() && inst.I==_hdIID+1) { // if (for this message a majority has been reached) && (its exactly the next message to be delivered)
			_pending.remove(inst.I);
			_delivered.put(inst.I,inst.toNewInstance());
			Debug.outAlways(inst.V2+" "+inst.I);
			_hdIID = inst.I;

			//try delivering the next sequence of instances
			ProposerInstance next = _pending.get(_hdIID+1); 
			while(next != null) {
				_pending.remove(next.I);
				_delivered.put(next.I,next.toNewInstance());
				Debug.outAlways(next.V2+" "+next.I);
				_hdIID = next.I;
				next = _pending.get(next.I+1);
			}
		}
	}

	//-------------------------------------------------------------------------------------------------------

	public int acceptorMajority() {
		int majority = (int) Math.floor(NUMBER_OF_ACCEPTORS / 2) + 1;
		return majority;
	}

	public boolean IsClosed(int iid) {
		ProposerInstance inst = _pending.get(iid);

		if(inst == null) 
			return false;

		return ( inst.countPromises() >= acceptorMajority() );
	}

	public int getHighestDeliveredIID() {
		return _hdIID;
	}

	//-------------------------------------------------------------------------------------------------------

	private void displayInstances() {
		show( Debug.ToConsole2(false, DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "\tDICTIONARY OF INSTANCES\n") );

		show("\n-----------------------PENDING-----------------------");

		Iterator<Entry<Integer,ProposerInstance>> plist =  _pending.entrySet().iterator();

		while(plist.hasNext())
			Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, plist.next().getValue().toString() );

		show("\n----------------------DELIVERED----------------------");

		Iterator<Entry<Integer,Instance>> dlist =  _delivered.entrySet().iterator();

		while(dlist.hasNext())
			Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, dlist.next().getValue().toString() );

		show( Debug.Line(false) );
	}
	
	public void setShowDebugMessages(boolean show) {
		_showDebugMessages = show;
	}

	private void show(String message) {
		if(_showDebugMessages && message != null)
			Debug.out(message);
	}

	//-------------------------------------------------------------------------------------------------------

}//class
