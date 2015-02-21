
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.*;
import util.*;

public class P1_Pending implements ProposerState
{	final String CLASS = "P1_Pending";
	
	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.P1_PENDING; } 
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	Proposer _proposer;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public P1_Pending(Proposer proposer) {
		this._proposer = proposer;
	};
	
	public void Process() {
		Debug.out("\n              EXECUTED | P1_Pending->Process");
		
		if(D1()) 			//Check for Delivery
			return; 	

		//P(); 				//Called with a On-Message-Received event is fired

		if(TO1()) 
			return;

		if(R0()) {
			//current().S.NV(); //if R0 executed successfully we can try NV() <- P1_ready_without_value
			return;
		}

		if(R1()) {
			//current().S.E(); //if R1 executed successfully we can try E() <- P1_Ready_with_value
			return;
		}

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
	//state: empty
	
	public boolean D0(){
		Debug.out("          NOT-EXECUTED | P1_Pending->D0");
		return false;
	}
	public boolean S(){
		Debug.out("          NOT-EXECUTED | P1_Pending->S");
		
		return false;
	}

	//-------------------------------------------------------------------------------------------------------
	//STATE: P1_pending
	//-------------------------------------------------------------------------------------------------------
	
   /* D1:
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

	public boolean D1(){
		Debug.out("              EXECUTED | P1_Pending->D01");

		 if (_proposer.IsDelivered(current().I)) {
			_proposer.setDelivered();
		 	return true;
		}

		return false;
	}

	//-------------------------------------------------------------------------------------------------------
	
   /* P: 
	* 	The proposer RECEIVED a PROMISE Pa from some acceptor "a".
	* 
	* 	- The BALLOT NUMBER must match B (the one stored), 
	* 		otherwise the message is dropped.
	* 
	* 	- The record is updated to <-, -, -, {Pa U PSET}, -, -, -, ->
	* 		-> i.e. PSET.add(Pa.id)
	* 
	* 	V1 and VB are updated with the following rule:
	*		- if Pa contains a VALUE, 
	*  			and the corresponding VALUE BALLOT is higher than VB,
	*  		then 
	*  			- V1 is set to the VALUE in the PROMISE 
	*  			- and VB to its VALUE BALLOT.
	*  
	*	If the size of PSET is now equal to the majority of acceptors, 
	*  		- action:R0 
	*  		- or action:R1 
	*  	is triggered
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________
		PSET -> {Pa U PSET}

if (Pa.V != null) && (Pa.VB > VB)
		V1 = Pa.V
		VB = Pa.VB

 		<-,         -, -, {Pa U PSET},   v, vb,  -, - >
	P: 	<i,p1_pending, b, {Pa U PSET},  v1, vb, v2, cv>
	_____________________________________________________________ */

	public boolean P(Message msg) {
		Debug.out("              EXECUTED | P1_Pending->P");


		//check for matching instance and ballot numbers
		if ( msg.getInstance().I != current().I || msg.getInstance().B != current().B )
			return false;

		current().addPromise( msg.getAgentInfo().getID() );
		
		if ( msg.getInstance().V2 != null && msg.getInstance().VB > current().VB ) {
			current().V1 = msg.getInstance().V2;
			current().VB = msg.getInstance().VB;
		}
		
		Debug.ToConsole2(DEBUG_TYPE.STATE, AGENT_TYPE.LEADER , "\n\n\t INSTANCE UPDATED -> PROMISE ADDED!!\n"+current().AsDisplayString() );
		
		if ( current().countPromises() >= _proposer.acceptorMajority() ) {
			//call either R0 or R1
			if ( current().V1 == null )
				R0();
			else
				R1();
		}

		return true;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
   /* TO1: Timeout 1
	* 	The timeout for this instance is expired (a majority of promises where not received).
	* 
	* 	- The proposer increments the ballot,
	* 	- and sends the PREPARE requests,  
	* 	- clears the promises
	* 	- and sets the timeout
	* 
	*	Note: all other fields are not relevant
	* 		The update record consists of <-, -, B++, {0}, null, -, ->
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

			B -> b++
			PSET -> {}
			
// Prepare requests only consist of <i,b> there for we can reset V1 & VB 
// Only once we have a promise from acceptors do we issue values

			V1 -> null
			VB -> 0

 			<-,         -, b++, {},  null, 0,  -, - >
	TO1: 	<i,p1_pending, b++, {},  null, 0, v2, cv>
	_____________________________________________________________ */

	public boolean TO1() {
		Debug.out("              EXECUTED | P1_Pending->TO1");

		if ( !current().isTimeout() )
			return false;

		Debug.out("              EXECUTED | P1_Pending->TO1->TIMEOUT OCCURRED!!!");

		_proposer.incrementBallot();
		current().clearPromises();

		// Prepare requests only consist of <i,b> there for we can reset V1 & VB or can we?
		current().V1 = null;
		current().VB = 0;

		_proposer.setTimeout();	
		_proposer.sendPrepare();

		return true;
	}
	
	//-------------------------------------------------------------------------------------------------------

   /* R0:
	* 	A majority of PROMISES was received, none of which contained a VALUE.
	* 	- Update the state to: p1_ready_without_value
	* 		The record is updated to <-, p1_ready_without_value, -, {0}, null,0, -, ->
	* 
	* 	If V2 is non-null, 
	* 		this proposer previously assigned a CLIENT VALUE to this instance, 
	* 		- action A is executed. 
	* 
	* 	If v2 is null,
	* 		- the proposer assigns to it the next element from the PENDING LIST of CLIENT VALUES and sets CV = true.
	* 		- then action A is executed.
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

if (V1 == null)
	R0:
else
	R1:
	_____________________________________________________________
			S -> p1_ready_without_value			
			PSET -> {}											//NOTE: the PSET should be empty otherwise on Receiving another message another R0 is called ... 

(implied)	V1 -> null
(implied?)	VB = 0

if (V2 == null)
			V2 -> pendingList
			cv -> true
else
	action->A:

 			<-,p1_ready_without_value, -, {},  null, 0,   -, - >
	R0: 	<i,p1_ready_without_value, b, {},  null, vb, v2, cv>
	_____________________________________________________________ */

	public boolean R0() { final String METHOD = "R0";
		Debug.out("              EXECUTED | P1_Pending->R0");
		
		//Check for majority
		if ( current().countPromises() < _proposer.acceptorMajority() ) {
			Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().countPromises() < _proposer.acceptorMajority()", "Can't execute R0 until a majority has been reached.");
			return false; //Important! We do not want to execute the rest of R0
		}
		
		Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().countPromises() >= _proposer.acceptorMajority()", "MAJORITY REACHED! EXECUTING R0!!.");

		//Check for without value
		if ( current().V1 != null ) { //R1 should have executed
			Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().V1 != null", "R1 should have been called, instead!");
			return false; //Important! We do not want to execute the rest of R0
		}
		
		Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().V1 == null", "There is a no V1 value! GOING TO NEXT STATE: P1_READY_WITHOUT_VALUE");

		current().clearPromises();
		_proposer.setP1_Ready_Without_Value();

		//It is implied ...
		//current().V1 = null;
		//current().VB = 0;

		return true;
	}

	//-------------------------------------------------------------------------------------------------------
	
   /* R1: 
    * 	A majority of PROMISES was received, at least one of them contained a VALUE, V1. 
	* 	Different cases are possible:
	* 
	* 	- update the state to:  P1_Ready_With_Value
	* CASE: v2 is null 
	* 		- update the record to: <-, -, -, -, -, -, v1, false>
	* 			-> V2 = v1
	* 			-> CV = false
	* 		We assign no client value to this instance. 
	* 		Phase 2 is executed with the VALUE(among the PROMISES) that has the highest BALLOT VALUE.
	* 
	* CASE: v1 = v2
	* 		- Update the record to: <-, -, -, -, null, -, v2, cv)
	* 			-> V1 = null
	* 		The value found in phase 1 is the client value that we assigned to this instance 
	* 			(this can happen after a phase 2 timeout).
	* 
	* CASE: v1 != v2 & cv = true: 
	* 		- Push back v2 to the head of the pending list 
	* 		- then update the record: <-, -, -, -, -, -, v1, false>
	* 			-> V2 = v1
	* 			-> CV = false
	* 		While trying to send a CLIENT VALUE for this instance, we discovered another value and we must use it.
	* 		The CLIENT VALUE will be sent in another instance.
	* 
	* CASE: v1 != v2 & cv = false
	* 		- Discard the current v2, 
	* 		- then update the record to: <-, -, -, -, -, -, v1, false>
	* 			-> V2 = v1
	* 			-> CV = false
	* 		We had some non-CLIENT VALUE for this instance, but another value came along and replaced it
	* 
	* 	In all the above cases the result is the same: 
	* 		- the value found in phase 1 is used to execute phase 2
	* 		- (by)Triggering action E
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

v2 == null:
			V2 -> V1
			cv -> false
	
			V1 -> null
			VB -> 0

			<-,		     -, -, {},  null, 0,  v1, false >
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, false >
	_____________________________________________________________

v1 == v2: (was assigned after timeout)

			V1 -> null
			VB -> 0

			<-,		     -, -, {},  null, 0,   -, - 	>
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, cv(false) >
	_____________________________________________________________

(v1 != v2) && (cv == true)

			V2 -> V1
			cv -> false

			V1 -> null
			VB -> 0

			<-,		     		 -, -, {},  null, 0,  v1, false >
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, false >
	_____________________________________________________________

(v1 != v2) && (cv == false)

		V2 -> V1
		cv -> false

		V1 -> null
		VB -> 0

			<-,		     		 -, -, {},  null, 0,  v1, false >
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, false >
	_____________________________________________________________ */

	public boolean R1(){ final String METHOD = "R0";
		Debug.out("              EXECUTED | P1_Pending->R1");
		
		//Check for majority
		if ( current().countPromises() < _proposer.acceptorMajority() ) {
			Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().countPromises() < _proposer.acceptorMajority()", "Can't execute R1 untill a majority has been reached.");
			return false; //Important! We do not want to execute the rest of R1
		}

		Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().countPromises() >= _proposer.acceptorMajority()", "MAJORITY REACHED! EXECUTING R1!!.");

		//Check for value
		if ( current().V1 == null ) { // R0 should have executed
			Debug.Error(CLASS, METHOD, "current().V1 == null", "R0 should have been called, instead!");
			return false; //Important! We do not want to execute the rest of R1
		} 

		Debug.Message(DEBUG_TYPE.STATE, CLASS, METHOD, "current().V1 != null", "There is a V1 value! GOING TO NEXT STATE: P1_READY_WITH_VALUE");



		if (current().V2 == null) {	
			//There was no client value assigned to this instance. Phase 2 is executed with the value, among the promises, that has the highest value ballot
			current().V2 = current().V1;
			current().CV = false;
		} else 
		if (current().V1 == current().V2) { 
			//The value found in phase 1 is the client value that we assigned to this instance (this can happen after a phase 2 timeout)
			//nothing to do ...
		} else
		if ( (current().V1 != current().V2) && (current().CV == true) ) {
			//While trying to send a client value for this instance, we discovered another value and we must use it. The client value will be sent in another instance.
			_proposer.pushValueBackToPendingList(current().V2);
			current().V2 = current().V1;
			current().CV = false;
		} else
		if ( (current().V1 != current().V2) && (current().CV == false) ) {
			//We had some non-client value for this instance, but another value came along and replaced it.
			current().V2 = current().V1;
			current().CV = false;
		}

		current().V1 = null;
		current().VB = 0;
		current().clearPromises();
		_proposer.setP1_Ready_With_Value();

		return true;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_without_value
	public boolean NV(){
		Debug.out("          NOT-EXECUTED | P1_Pending->NV");
		
		return false;
	}
	public boolean D2(){
		Debug.out("          NOT-EXECUTED | P1_Pending->D2");
		
		return false;
	}
	public boolean A(){
		Debug.out("          NOT-EXECUTED | P1_Pending->A");
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_with_value
	public boolean D3(){
		Debug.out("          NOT-EXECUTED | P1_Pending->D3");
		
		return false;
	}
	public boolean E(){
		Debug.out("          NOT-EXECUTED | P1_Pending->E");
		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	//state: p2_pending
	public boolean TO2(){
		Debug.out("          NOT-EXECUTED | P1_Pending->TO2");
		
		return false;
	}
	public boolean D4(){
		Debug.out("          NOT-EXECUTED | P1_Pending->D4");
		
		return false;
	}
	public boolean C(){
		Debug.out("          NOT-EXECUTED | P1_Pending->C");
		
		return false;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: closed
	public boolean D5(){
		Debug.out("          NOT-EXECUTED | P1_Pending->D5");
		
		return false;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: delivered

	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------
	
	public String toString() {

		String status = "STATE: P1_PENDING";
		
		return status;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
}//P1_Pending
