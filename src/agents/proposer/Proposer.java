
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import util.*;
import agents.learner.Learner;
import config.*;
import communication.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.UUID;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Console;

import javax.swing.Timer;

public class Proposer implements ActionListener
{	final String CLASS = "Proposer";

	public static final int MILLISECONDS = 1000;
	public static final int MAX_HEARTBEATS = 3;
	public static final int MAX_PROPOSERS = 10;

	//Configuration
	private static int WINDOW_SIZE = 1;
	private static int NUMBER_OF_ACCEPTORS = 1;
	private static int TIMEOUT_PHASE1AND2 = 2*MILLISECONDS;
	private static int HEARTBEAT_INTERVAL = 2*MILLISECONDS;
	private static int EXECUTE_PROCESS = 100;

	//-------------------------------------------------------------------------------------------------------
	// DATA-MEMBERS 			  (by convention all global class members are prefixed with an underscore "_")
	//-------------------------------------------------------------------------------------------------------

	//Proposer: AgentInfo Contains { uid, id, AGENT_TYPE } 
	AgentInfo _agentInfo = null;

	boolean _proposerList[] = null;

	//Communication
	MulticastChannel _proposers = null;
	MulticastSender _acceptors = null;

	//HeartBeat
	Timer _heatbeatTimer = null;
	int _heartbeatCount = 0;
	
	//ProcessTimer
	Timer _processTimer = null;

	//PendingList | when the client sends values to the proposer(leader) it is stored in this pending list
	BlockingQueue<Integer> _pending;

	//Learner
	Learner _learner = null;

	//States
	Empty _emptyState = null;
	P1_Pending _p1_pendingState = null;
	P1_Ready_without_value _p1_ready_without_valueState = null;
	P1_Ready_with_value _p1_ready_with_valueState = null;
	P2_Pending _p2_pendingState = null;
	Closed _closedState = null;
	Delivered _deliveredState = null;

	// Window | Instances
	Map<Integer,ProposerInstance> _currentWindow = null;
		Map<Integer,ProposerInstance> _windowOne = null;
		Map<Integer,ProposerInstance> _windowTwo = null;

	volatile int _countdownDisplayWindow=0;  //just using this as a counter to display the window every X (say 100) loops ... 
	volatile int _windowMaxIID = 0;	//The biggest instance ID in the current window -> when the _maxIID learner.getHighestDeliverIID

	volatile Iterator<Entry<Integer,ProposerInstance>> _iterator = null;
	volatile ProposerInstance _last = null;
	volatile ProposerInstance _current = null;

	//-------------------------------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public Proposer(Config config, final int id, boolean readFromConsole) {
		//Proposer
		_agentInfo = new AgentInfo(UUID.randomUUID(), id, AGENT_TYPE.PROPOSER);

		//configuration
		NUMBER_OF_ACCEPTORS = config.getNumberOfAcceptors();
		WINDOW_SIZE = config.getWindowSize();
		HEARTBEAT_INTERVAL = config.getProposerHeartbeatInterval()*MILLISECONDS;
		TIMEOUT_PHASE1AND2 = config.getTimeoutPhase1and2();
		EXECUTE_PROCESS = config.getProcessWindowTimer();

		 //Leader Election
		_proposerList = new boolean[MAX_PROPOSERS];
		clearProposerList();

		//Initialize States
		_emptyState = new Empty(this);
		_p1_pendingState = new P1_Pending(this);
		_p1_ready_without_valueState = new P1_Ready_without_value(this);
		_p1_ready_with_valueState = new P1_Ready_with_value(this);
		_p2_pendingState = new P2_Pending(this);
		_closedState = new Closed(this);
		_deliveredState = new Delivered(this);

		//pendingList
		_pending = new LinkedBlockingQueue<Integer>();
		
		// Window | Instances
		populateNextWindow();

		//position the _current pointer
		getNextInstance(false);

		//Initialize Communication
		_proposers = new MulticastChannel(_agentInfo.getUID(), id, config.getProposersIP(), config.getProposersPort() );
		_proposers.setNotifyMessageReceivedCallback(doOnProposerMessageReceived);
		_acceptors = new MulticastSender(id, config.getAcceptorsIp(), config.getAcceptorsPort() );

		_learner = new Learner(config, Instance.INCREMENT + id, false);

		//HeartBeat
		_heatbeatTimer = new Timer(HEARTBEAT_INTERVAL, this);

		//ProcessTimer
		_processTimer = new Timer(EXECUTE_PROCESS, this);

		//START

		_acceptors.Start();
		_processTimer.start();
		run(readFromConsole);
	}

	private void run(boolean readFromConsole) {

		final String PROMPT =""; //= "Value: ";

		if (readFromConsole) {
			Console console = System.console();
			String value = 
					console.readLine(PROMPT);

			while(value.length() > 0) {
				try {
					_pending.add(Integer.parseInt(value));
				} catch(NumberFormatException e){
					Debug.Message(DEBUG_TYPE.ERROR, CLASS, "Constructor::run", "Integer.parseInt(value)", e.getMessage() + "\n"+ e.getStackTrace());
					break;
				}
				value = console.readLine(PROMPT);
			}
			//Debug.outAlways("Values added to pending. Now starting to propose...");
		}
		_heatbeatTimer.start();
	}//run

	//-------------------------------------------------------------------------------------------------------
	// EVENT | ROUTING
	//-------------------------------------------------------------------------------------------------------

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _heatbeatTimer) { OnHeartbeat(); }
		if (e.getSource() == _processTimer) { OnProcess(); }
  	}
	
	private void OnProcess() {		
		if ( _agentInfo.getAgentType() == AGENT_TYPE.LEADER )
			for(int i=0;i<WINDOW_SIZE;i++)
			{
				if(_learner.getHighestDeliveredIID() >= _windowMaxIID)
					populateNextWindow();

				_countdownDisplayWindow++;
				if (_countdownDisplayWindow >= 1000) {
					_countdownDisplayWindow=0;
					displayWindowInstances();
				}
	
				Process();
				getNextInstance(false);
			}
	}

	//-------------------------------------------------------------------------------------------------------
	// WINDOW | STATE
	//-------------------------------------------------------------------------------------------------------

	void Process() {
		current().S.Process();
	}

	synchronized ProposerInstance current() {
		return _current;
	}

	synchronized Map<Integer,ProposerInstance> window(Map<Integer,ProposerInstance> pointer) {
		if (pointer != null)
			_currentWindow = pointer;
		
		return _currentWindow;
	}

	private synchronized void populateNextWindow() {
		//Initialize
		if (_windowOne == null) _windowOne = new HashMap<Integer, ProposerInstance>();		
		if (_windowTwo == null) _windowTwo = new HashMap<Integer, ProposerInstance>();
		if (_currentWindow == null) _currentWindow = _windowOne;

		Map<Integer,ProposerInstance> pointer = _windowOne;
		if(_currentWindow == _windowOne) 
			pointer = _windowTwo;

		displayWindowInstances();	
		pointer.clear();

		int windowMinIID = _windowMaxIID+1;
		
		_windowMaxIID = _windowMaxIID + WINDOW_SIZE;
		if (_learner != null) //if the learner is initialized and supplying Highest Delivery IIDs we can use this to determine the window's MAX IID
			_windowMaxIID = _learner.getHighestDeliveredIID() + WINDOW_SIZE;

		for(Integer i=windowMinIID; i<=_windowMaxIID; i++) {
			ProposerInstance inst = new ProposerInstance(i,_emptyState);
			inst.I = i;
			pointer.put(i, inst);
		}

		window(pointer);
		getNextInstance(true);
		displayWindowInstances();
	}

	private synchronized void getNextInstance(boolean newWindow) { //moves _current to the next instance... 
		
		if (_iterator == null || !_iterator.hasNext() || newWindow)
			_iterator = window(null).entrySet().iterator();

		if (_current == _last) { //this is so that if we shift current "focus" to any other instance (say if there is an incoming message) then when we getNextInstance we continue from the last location ...
			_current = _iterator.next().getValue();
			_last = _current;
		}
		else
			_current = _last;

		Debug.ToConsole2( DEBUG_TYPE.MESSAGE, AGENT_TYPE.ACCEPTOR, _current.AsDisplayString() );
	}

	private synchronized void displayWindowInstances() {
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, "\tDICTIONARY OF INSTANCES\n");

		Iterator<Entry<Integer,ProposerInstance>> it =  window(null).entrySet().iterator();

		while(it.hasNext()){
			ProposerInstance inst = it.next().getValue();
			Debug.ToConsole( DEBUG_TYPE.PP_AA, AGENT_TYPE.ACCEPTOR, inst.toString() );
		}

		Debug.Line(true);
	}

	//-------------------------------------------------------------------------------------------------------
	// State Transitions

	public final PROPOSER_STATE STATE() {  return current().S.STATE();  }

	void setEmpty() { 
		Debug.out("              SETTING STATE | EMPTY");
		current().S = _emptyState;
	}
	
	void setP1_Pending() { 
		Debug.out("              SETTING STATE | P1_PENDING");
		current().S = _p1_pendingState; 
	}

	void setP1_Ready_Without_Value() {
		Debug.out("              SETTING STATE | P1_READY_WITHOUT_VALUE");
		current().S = _p1_ready_without_valueState; 
	}
	
	void setP1_Ready_With_Value() {
		Debug.out("              SETTING STATE | P1_READY_WITH_VALUE");
		current().S = _p1_ready_with_valueState; 
	}

	void setP2_Pending() { 
		Debug.out("              SETTING STATE | P2_PENDING");
		current().S = _p2_pendingState; 
	}
	
	void setClosed() { 
		Debug.out("              SETTING STATE | CLOSED");
		current().S = _closedState; 
	}

	void setDelivered() { 
		Debug.out("              SETTING STATE | DELIVERED IID:"+current().I);
		current().S = _deliveredState;
	}


	//-------------------------------------------------------------------------------------------------------
	// METHODS
	//-------------------------------------------------------------------------------------------------------

	public int acceptorMajority() {
		int majority = (int) Math.floor(NUMBER_OF_ACCEPTORS / 2) + 1;

		//Debug.ToConsole2(DEBUG_TYPE.STATE, AGENT_TYPE.LEADER, "\n\n\t MAJORITY: " + majority +"\n\n");

		return majority;
	}

	void setTimeout() {
		current().setTimeout(TIMEOUT_PHASE1AND2);
	}

	//SEND PREPARE TO ACCEPTORS
	void sendPrepare() {
		Message message = new Message(MESSAGE_TYPE.PREPARE,AGENT_TYPE.ACCEPTOR,_agentInfo, current().toNewInstance() );
		Debug.ToConsole2( DEBUG_TYPE.PP_AA, AGENT_TYPE.LEARNER, "\tEXECUTED | SEND PREPARE\n" + message.AsDisplayString() );
		_acceptors.DispatchMessage(message);
	}

	//SEND ACCEPT TO ACCEPTORS
	void sendAccept() {
		Message message = new Message(MESSAGE_TYPE.ACCEPT, AGENT_TYPE.ACCEPTOR, _agentInfo, current().toNewInstance() );
		Debug.ToConsole2(DEBUG_TYPE.PP_AA, AGENT_TYPE.LEADER, "\t EXECUTED | SENDING ACCEPT .... \n" + message.AsDisplayString() );
		_acceptors.DispatchMessage(message);
	}

	boolean IsClosed(int iid) { //the Instance is on the Learners pending List, ready for delivery (i.e: more than majority Accepts received) but it can't be delivered until all the previous instances are ...
		return _learner.IsClosed(iid);
	}

	boolean IsDelivered(Integer iid) {
		return (iid <= _learner.getHighestDeliveredIID());
	}

	private void OnPromiseReceived(Message message) {	
		final String METHOD = "OnPromiseReceived";

		Debug.ToConsole2(DEBUG_TYPE.RECEIVE, AGENT_TYPE.LEARNER, "\n\n\t 		EXECUTING... | OnPromiseReceived **************************************************\n" );

		UUID uid = message.getAgentInfo().getUID();
		
		if (_agentInfo.getUID() == uid) { // Technically this should never happen
			final String OFFENDING_CODE = "(_agentInfo.getID() == id)";
			final String ERROR = "Message SENT/RECEIVED from AGENT with the same ID. MESSAGE UID: " + uid + "\n\t AGENT UID: " + _agentInfo.getUID() + "\nQUTTING ...\n";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, ERROR);
			System.exit(1);
		}

		ProposerInstance pinst = window(null).get(message.getInstance().I);

		if(pinst != null) { 
			_current = pinst;		//we point current to the obtained instance... this is some fancy foot work - because everything inside the states refer to _current :( not good ... 
			_current.S.P(message);  //execute the function
			_current = _last;		//and switch current back to the last know position (last = current)
		}

		Debug.ToConsole2(DEBUG_TYPE.RECEIVE, AGENT_TYPE.LEARNER, "\n\n\t 		EXECUTED | OnPromiseReceived **************************************************\n" );
	}

	private void OnRejectReceived(Message message) {
		if(message == null || message.getInstance() == null) {
			Debug.ToConsole2( DEBUG_TYPE.ERROR, AGENT_TYPE.LEARNER, "Proposer::OnRejectReceived -> message / instance is null!" );
			//System.exit(1);
			return;
		}

		Instance msgInst = message.getInstance();
		Instance inst = window(null).get(msgInst.I);
		if (inst == null)
			return;

		inst.B = incrementSkip(inst.B, msgInst.B, _agentInfo.getID(), Instance.INCREMENT);
	}

	//-------------------------------------------------------------------------------------------------------
	/* BALLOTS
	 * 	- Two distinct proposers never generate the same ballot for the same instance.
	 *  - The operation increment must be defined for any ballot (we use b++ to denote the result of incrementing ballot b).
	 *  - The binary operations greater than and equal to must be defined for any two ballots.
	 *  
	 * In our implementation we use the following scheme:
	 * 	- fix the maximum number of proposer n as a power of 10, i.e., 100.
	 * 	- Each proposer is given a unique ID in the range 0 . . .n-1.
	 *  Proposer p’s first ballot number for each instance is created as:
	 *  	- an integer n+p (i.e. 103 if p is 3).
	 *  	  p -> proposer id = 3
	 *  
	 * Incrementing ballot b is straightforward:
	 * 	- b++ => (b+n)	(i.e., 103 -> 203 -> 303... )
	 * 
	 * When receiving a reject message containing the current ballot "to beat" b',
	 * 	- the proposer can specifically generate the next one (using INCREMENTSKIP Algorithm)
	 * 	  rather than just incrementing the current b
	 */

	void incrementBallot() {
		current().B = incrementBallot( current().B, ProposerInstance.INCREMENT, _agentInfo.getID() );
	}

	// b -> ballot
	// n -> the value by which the ballot is incremented for instance I; e.g: starts from 100
	// p -> proposer id
	public static int incrementBallot(int b, int n, int p) {
		if (b == 0) 
			return n + p;

		return  b + n;
	}

	/*	Algorithm INCREMENTSKIP(b,b')
		1: my_seq = b - p
		2: b'_owner = b' mod n
		3: b'_seq = b'- b'_owner
		4: use_seq = max(my_seq, b'_seq)
		5: use_seq = +1
		6: return (use_seq + p)
	 */

	// n -> the value by which the ballot is incremented for instance I; e.g: starts from 100
	// p -> proposer id
	// b -> current ballot
	// bP => b' -> ballot rejected, the one "to beat"
	public static int incrementSkip(int b, int bP, int p, int n) {
		int my_seq = b - p;
		int bP_owner = bP % n;
		int bP_seq = bP - bP_owner;
		int use_seq = Math.max(my_seq,bP_seq);
		
		return use_seq;
	}

	//-------------------------------------------------------------------------------------------------------
	// COMMUNICATION | RECEIVED MESSAGES
	//-------------------------------------------------------------------------------------------------------

	final Runnable doOnProposerMessageReceived = new Runnable() {
		public void run() 
    	{ 	final String METHOD = "doOnProposerMessageReceived::run";

	         Debug.ToConsole(DEBUG_TYPE.RECEIVE, "\n *** Proposer::OnProposerMessageReceived... *** \n");

	    	 Message message = null;
	    	 try {
	    		 message = _proposers.getMessageQueue().take();
			} catch (InterruptedException e) {
				final String OFFENDING_CODE = "InterruptedException::message = _proposers.getMessageQueue().take()";
				Debug.Error( CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
				e.printStackTrace();
			}

	    	Debug.ToConsole(DEBUG_TYPE.RECEIVE,"\t#OnProposerMessageReceived::queue->take()::Message" + 
	    		"\n### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ###" + 
	    		message.AsDisplayString() + 
	    		"\n### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ### ###\n");

	    	if ( message.getMessageType() == MESSAGE_TYPE.HEARTBEAT )
	    		OnHearbeatReceived(message);

	    	if ( message.getMessageType() == MESSAGE_TYPE.PROMISE )
	    		OnPromiseReceived(message);
	    	
	    	if ( message.getMessageType() == MESSAGE_TYPE.REJECT )
	    		OnRejectReceived(message);

    	}//run
	 };
	 
	//-------------------------------------------------------------------------------------------------------
	// CLIENT | PENDING VALUES 
	//-------------------------------------------------------------------------------------------------------

//	private static int pendingValue = 3333; //just temporarily for simulation purposes

	//simulate client value
	Integer getPendingListValue() {
//		if ( System.currentTimeMillis() % 2 == 0 ) //TODO: This is for Simulation Purposes... need to disable - was just for testing purposes ... 
//			_pending.add(++pendingValue);

		return _pending.poll();
	}

	void pushValueBackToPendingList(Integer V2) {
		_pending.add(V2);
	}

	//-------------------------------------------------------------------------------------------------------
	// HEARTBEAT | LEADER ELECTION
	//-------------------------------------------------------------------------------------------------------

	private void OnHeartbeat() {
		Debug.ToConsole(DEBUG_TYPE.HEARTBEAT, "\nProposer::OnHeartbeat");
		Message msg = new Message( MESSAGE_TYPE.HEARTBEAT, AGENT_TYPE.PROPOSER, _agentInfo, null);
		_proposers.DispatchMessage(msg);

		_heartbeatCount++;
		if (_heartbeatCount>=MAX_HEARTBEATS) {
			_heartbeatCount=0;
			int leaderId = getProposerLeader();
			updateLeader(leaderId);
			clearProposerList();
		}

		updateProposerList( _agentInfo.getID() );

		Debug.ToConsole(DEBUG_TYPE.HEARTBEAT, "\n\t @@@ proposers: " + getProposerListAsString() + " @@@ \n");
	}

	private void OnHearbeatReceived(Message message)
	{	final String METHOD = "OnHearbeatReceived";

		int id = message.getAgentInfo().getID();

		if (_agentInfo.getID() == id) {
			final String OFFENDING_CODE = "(_agentInfo.getID() == id)";
			final String ERROR = "To Proposers with the same ID detected: " + id + "\n\t QUITTING, UID: " + _agentInfo.getUID();
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, ERROR);
			System.exit(1);
		}

		if (id < 1 || id > MAX_PROPOSERS) {
			final String OFFENDING_CODE = "(id < 1 || id > MAX_PROPOSERS)";
			final String ERROR = "More than MAX_PROPSERS: " + MAX_PROPOSERS + " or invalid propser ID -> must be in range of: 1 - " + MAX_PROPOSERS;
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, ERROR);
			System.exit(1);
		}

		updateProposerList(id);
	}

	private int getProposerLeader()
	{
		int leaderId = _agentInfo.getID();

		for(int i=0;i<MAX_PROPOSERS;i++) 
			if (_proposerList[i]==true) {
				leaderId = i+1;
				break;
			}

		Debug.ToConsole(DEBUG_TYPE.LEADER, "\n\t @@@ PROPOSER LEADER ID -> " + leaderId +" @@@ \n");

		return leaderId;
	}
	
	private void updateProposerList(int id)
	{	final String METHOD = "updateProposerList";

		if (id < 1 || id > MAX_PROPOSERS)
		{
			final String OFFENDING_CODE = "(id < 1 || id > MAX_PROPOSERS)";
			final String ERROR = "id out of range: " + id + " -> must be in range of: 1 - " + MAX_PROPOSERS;
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, ERROR);
			System.exit(1);
		}

		_proposerList[id-1]=true;
	}

	private void clearProposerList()
	{
		for(int i=0;i<MAX_PROPOSERS;i++) 
			_proposerList[i]=false;
	}	

	private void updateLeader(int leaderId)
	{
		if ( _agentInfo.getID() == leaderId )
			_agentInfo.setAgentType(AGENT_TYPE.LEADER);
		else
			_agentInfo.setAgentType(AGENT_TYPE.PROPOSER);
	}
	
	private String getProposerListAsString()
	{
		String proposers = " ";
		for(int i=0;i<MAX_PROPOSERS;i++) 
			if (_proposerList[i]==true)
				proposers = proposers + (i+1) + " ";

		return "[" + proposers + "]";	 
	}

	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------

	public String toString() {
		String status = "Proposer::toString() | " + current().S;

		return status;
	}

	public String AsDisplayString() {
		return toString();
	}//displayState
	
	//-------------------------------------------------------------------------------------------------------	
	
}//Proposer
