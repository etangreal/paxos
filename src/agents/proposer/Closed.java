
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.Message;

public class Closed implements ProposerState
{
	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.CLOSED; } 
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------
	
	Proposer _proposer;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public Closed(Proposer proposer) {
		this._proposer = proposer;
	};

	public void Process() {
		System.out.println("\n              EXECUTED | Closed->Process\n");

		if (D5()) //Check for Delivery
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
	//state: empty
	public boolean D0(){
		System.out.println("Closed->D0");
	return false;} 
	public boolean S(){
		System.out.println("Closed->S");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p1_pending
	public boolean TO1(){
		System.out.println("Closed->TO1");
	return false;} 
	public boolean P(Message msg) {
		System.out.println("Closed->P");
	return false;} 
	public boolean D1(){
		System.out.println("Closed->D01");
	return false;} 
	public boolean R0(){
		System.out.println("Closed->R0");
	return false;} 
	public boolean R1(){
		System.out.println("Closed->R1");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_without_value
	public boolean NV(){
		System.out.println("Closed->NV");
	return false;} 
	public boolean D2(){
		System.out.println("Closed->D2");
	return false;} 
	public boolean A(){
		System.out.println("Closed->A");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_with_value
	public boolean D3(){
		System.out.println("Closed->D3");
	return false;} 
	public boolean E(){
		System.out.println("Closed->E");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//state: p2_pending
	public boolean TO2(){
		System.out.println("Closed->TO2");
	return false;} 
	public boolean D4(){
		System.out.println("Closed->D4");
	return false;} 
	public boolean C(){
		System.out.println("Closed->C");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//STATE: closed
	//-------------------------------------------------------------------------------------------------------
	
   /* D5:
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
    * 
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

			S -> Delivered

			<-,Delivered, -,  -,     -, -,   -, -  >
	D: 		<i,Delivered, -, pset,  v1, vb,  v2, cv >
	_____________________________________________________________ */	
	
	public boolean D5() {
		System.out.println("Closed->D5");

		 if (_proposer.IsDelivered(current().I)) {
			_proposer.setDelivered();
		 	return true;
		}

		return false;
	} 
	
	
	//-------------------------------------------------------------------------------------------------------
	//state: delivered
	

	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------

	public String toString() {

		String status = "STATE: CLOSED";
		
		return status;
	}
	
	//-------------------------------------------------------------------------------------------------------

}//class P2_Closed
