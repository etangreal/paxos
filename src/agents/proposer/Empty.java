
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import util.*;
import communication.Message;
import communication.AGENT_TYPE;

public class Empty implements ProposerState 
{	
	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.EMPTY; }
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	Proposer _proposer;
	


	//-------------------------------------------------------------------------------------------------------
	// CONSTRUCTOR | MAIN
	//-------------------------------------------------------------------------------------------------------
	
	public Empty(Proposer proposer) {
		this._proposer = proposer;
	};


	public void Process() {
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"\n              EXECUTED | Empty->Process");

		if (D0()) //Check for Delivery
			return;
		
		if (S()) 
			return;
	}
	
	//-------------------------------------------------------------------------------------------------------
	// PROPERTY
	//-------------------------------------------------------------------------------------------------------
	
	public ProposerInstance current() { 
		if (_proposer == null) 
			return null;
		
		return _proposer.current();
	}

	//-------------------------------------------------------------------------------------------------------
	// STATE: empty
	//-------------------------------------------------------------------------------------------------------


   /* D0:
    * 	At any time during the life of the instance, something else may happen:
    * 		the learner may kick-in and deliver some value v' for it. Why? How?
    * 			Maybe that value got accepted "slowly" making the proposer timeout,
    * 			or maybe another proposer is sending values.
    * 	
    * 	Independently of the current state, 
    * 		the event is handled with the following rule:
    * 
    * CASE: v' = v2 & cv = true
    * 		Our client's value was delivered, 
    * 			- inform it if this is the case.
    * 
    * CASE: v' != v2 & cv = true
    * 		push back v2 to the head of the pending list,
    * 		since we could not deliver it in this instance
    * 
    * CASE: any other case requires no action.
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

			S -> Delivered

			<-,Delivered, -,  -,     -, -,   -, -  >
	D: 		<i,Delivered, -, pset,  v1, vb,  v2, cv >
	_____________________________________________________________ */

	public boolean D0() {
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"              EXECUTED | Empty->D0");

		 if (_proposer.IsDelivered(current().I)) {
			_proposer.setDelivered();
		 	return true;
		}

		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------

	/* S:
	* 	The proposer executes phase 1 for the first time in this instance(numbered i).
	* 
	* 	It generates its first ballot.
	* 		For example if the proposer ID is 2, the first ballot "b" is 102.
	* 		- The ballot is updated (B = b)
	* 	
	* 	- Send out Prepare <i, b, v> to all acceptors
	* 
	* 	Before|After? sending the PREPARE request to acceptors, 
	* 		- The STATE is updated to -> P1_PENDING state.
	* 
	* 	- Sets a timeout for this instance
	_____________________________________________________________
	STATE TRANSITION - RECORD UPDATE DOCUMETATION
	_____________________________________________________________

	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	init:	<0,empty,0,{},null,0,null,false>
	_____________________________________________________________
		I -> i
		S -> p1_pending
		B -> b

		<i,p1_pending,b, -,  -, -, -, ->
	S: 	<i,p1_pending,b,{},null,0,null,false>
	_____________________________________________________________ */

	public boolean S() {
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"EXECUTED | Empty->S");

		_proposer.incrementBallot();
		_proposer.setTimeout();
		_proposer.setP1_Pending();
		_proposer.sendPrepare();

		return true;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: p1_pending
	public boolean TO1(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->TO1");
		return false;
	}
	public boolean P(Message msg){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->P");
		return false;
	}
	public boolean D1(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->D01");
		return false;
	}
	public boolean R0(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->R0");
		return false;
	}
	public boolean R1(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->R1");
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_without_value
	public boolean NV(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->NV");
		return false;
	}
	public boolean D2(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->D2");
		return false;
	}
	public boolean A(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->A");
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_with_value
	public boolean D3(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->D3");
		return false;
	}
	public boolean E(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->E");
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//state: p2_pending
	public boolean TO2(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->TO2");
		return false;
	}
	public boolean D4(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->D4");
		return false;
	}
	public boolean C(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->C");
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//state: closed
	public boolean D5(){
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.PROPOSER,"          NOT-EXECUTED | Empty->D5");
		return false;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: delivered
	
	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------

	public String toString() 
	{
		String status = "STATE: EMPTY";

		return status;
	}

	//-------------------------------------------------------------------------------------------------------

}//Empty
