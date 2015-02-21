
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.Message;
import util.*;

public class P2_Pending implements ProposerState
{
	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.P2_PENDING; } 
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------
	
	Proposer _proposer;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public P2_Pending(Proposer proposer) {
		this._proposer = proposer;
	};
	
	public void Process() {
		Debug.out("\n              EXECUTED | P2_Pending->Process\n");

		if (D4()) 		//Check for Delivery
			return;

		if (TO2()) 
			return;

		C();	
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
		Debug.out("          NOT-EXECUTED | P2_Pending->D0");
	return false;} 
	public boolean S(){
		Debug.out("          NOT-EXECUTED | P2_Pending->S");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_pending
	public boolean TO1(){
		Debug.out("          NOT-EXECUTED | P2_Pending->TO1");
	return false;} 
	public boolean P(Message msg) {
		Debug.out("          NOT-EXECUTED | P2_Pending->P");
	return false;} 
		//--progress
	public boolean D1(){
		Debug.out("          NOT-EXECUTED | P2_Pending->D01");
	return false;} 
	public boolean R0(){
		Debug.out("          NOT-EXECUTED | P2_Pending->R0");
	return false;} 
	public boolean R1(){
		Debug.out("          NOT-EXECUTED | P2_Pending->R1");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_without_value
	public boolean NV(){
		Debug.out("          NOT-EXECUTED | P2_Pending->NV");
	return false;} 
	public boolean D2(){
		Debug.out("          NOT-EXECUTED | P2_Pending->D2");
	return false;} 
	public boolean A(){
		Debug.out("          NOT-EXECUTED | P2_Pending->A");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_with_value
	public boolean D3(){
		Debug.out("          NOT-EXECUTED | P2_Pending->D3");
	return false;} 
	public boolean E(){
		Debug.out("          NOT-EXECUTED | P2_Pending->E");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//STATE: P2_pending
	//-------------------------------------------------------------------------------------------------------

   /* D4:
    * 	at any time during the life of the instance, something else may happen:
    * 		the learner may kick-in and deliver some value v' for it.
    * 		Maybe that value got accepted "slowly" making the proposer timeout,
    * 		maybe another proposer is sending values.
    * 	Independently of the current state, the event is handled with the following rule:
    * 
    * CASE: v' = v2 & cv = true
    * 		Our clien'ts value was delivered, 
    * 			inform it if the case.
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
	
	public boolean D4() {
		Debug.out("              EXECUTED | P2_Pending->D4");

		 if (_proposer.IsDelivered(current().I)) {
			_proposer.setDelivered();
		 	return true;
		}

		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
   /* TO2:
	* 	The timer for phase 2 expired. 
	* 		It is necessary to restart from phase 1.
	* 	- The record is updated to: <-,p1_pending, b++, {},  null, 0,  v2, cv >
	* 		-> S = P1_Pending
	* 		-> b++
	* 		-> PSET = {}
	* 		-> v1 = null
	* 		-> vb = 0
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

			b => b++
			PSET -> {}
			v1 -> null;
			vb -> 0;
			S -> p1_pending

			<-,p1_pending, b++, {},  null, 0,   -, -  >
	T02: 	<i,p1_pending, b++, {},  null, 0,  v2, cv >
	_____________________________________________________________ */

	public boolean TO2() {
		Debug.out("              EXECUTED | P2_Pending->TO2");

		if ( !current().isTimeout() )
			return false;

		current().setTimeout(null);
		_proposer.incrementBallot();
		current().clearPromises();
		current().V1 = null;
		current().VB = 0;

		_proposer.setP1_Pending();
		_proposer.sendPrepare();

		return true;
	} 
	
   /* C:
    * 	This action is triggered if the proposer realizes that instance i is closed 
    * 		(i.e. by querying the learning) 
    * 		with any value.
    * 	- The record is updated to: <-, Closed, -, -, -, -, ->
    * 		-> S = Closed
    * 	
    * 	This record can be ignored until it is delivered (D5)
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

			S -> Closed

			<-,Closed, -,  -,     -, -,   -, -  >
	C: 		<i,Closed, -, {},  null, 0,  v2, cv >
	_____________________________________________________________ */
	
	public boolean C() {
		Debug.out("              EXECUTED | P2_Pending->C");

		if ( _proposer.IsClosed(current().I) ) return true;


		//this action is triggered if the proposer realizes that instance i is closed (i.e., by querying its learner) with any value.

		//TODO: query learner ... 
		//_proposer.setClosed();

		return true;
	} 

	//-------------------------------------------------------------------------------------------------------
	//state: closed
	public boolean D5(){
		Debug.out("          NOT-EXECUTED | P2_Pending->D5");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: delivered
	
	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------

	public String toString() {

		String status = "STATE: P2_PENDING";
		
		return status;
	}

	//-------------------------------------------------------------------------------------------------------

}//P2_Pending
