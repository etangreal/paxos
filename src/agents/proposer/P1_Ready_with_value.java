
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.Message;
import util.*;

public class P1_Ready_with_value implements ProposerState
{
	
	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.P1_READY_WITH_VALUE; } 
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------
	
	Proposer _proposer;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public P1_Ready_with_value(Proposer proposer) {
		this._proposer = proposer;
	};

	public void Process() {
		Debug.out("\n              EXECUTED | P1_Ready_with_value->Process\n");

		if (D3()) 		//Check for Delivery
			return; 

		E();
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
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->D0");
	return false;} 
	public boolean S(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->S");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_pending
	public boolean TO1(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->TO1");
	return false;} 
	public boolean P(Message msg) {
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->P");
	return false;}
	public boolean D1(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->D01");
	return false;} 
	public boolean R0(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->R0");
	return false;} 
	public boolean R1(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->R1");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_without_value
	public boolean NV(){
		Debug.out("              EXECUTED | P1_Ready_with_value->NV");
	return false;} 
		//--progress
	public boolean D2(){
		Debug.out("              EXECUTED | P1_Ready_with_value->D2");
	return false;} 
	public boolean A(){
		Debug.out("              EXECUTED | P1_Ready_with_value->A");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//STATE: p1_ready_with_value
	//-------------------------------------------------------------------------------------------------------

   /* D3:
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

	public boolean D3(){
		Debug.out("              EXECUTED | P1_Ready_with_value->D3");

		 if (_proposer.IsDelivered(current().I)) {
			_proposer.setDelivered();
		 	return true;
		}

		return false;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
   /* E:
	* 	The proposer executes phase 2 with: 
	* 		- ballot b 
	* 		- and value v2
	* 
	* 	- The record is updated to <-, P2_ready, -, -, -, -, ->
	* 		-> S = P2_Pending
	* 
	* 	- A timeout is set
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

		S -> p2_pending

		<-,p2_pending, -,  -,     -, 0,   -, -  >
	E: 	<i,p2_pending, b, {},  null, 0,  v2, cv >
	_____________________________________________________________ */

	public boolean E() {
		Debug.out("              EXECUTED | P1_Ready_with_value->E");

		_proposer.sendAccept();
		current().setTimeout(null);
		_proposer.setP2_Pending();

		return true;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: p2_pending
	public boolean TO2(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->TO2");
	return false;} 
		//--progress
	public boolean D4(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->D4");
	return false;} 
	public boolean C(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->C");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: closed
	public boolean D5(){
		Debug.out("          NOT-EXECUTED | P1_Ready_with_value->D5");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: delivered

	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------

	public String toString() 
	{
		String status = "STATE: P1_READY_WITH_VALUE";
		
		return status;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
}//class P1_Ready_with_value
