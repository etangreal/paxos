
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;
import util.Debug;
import communication.Message;

public class P1_Ready_without_value implements ProposerState
{ 	final String CLASS = "P1_Pending";

	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.P1_READY_WITHOUT_VALUE; } 
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	Proposer _proposer;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public P1_Ready_without_value(Proposer proposer) {
		this._proposer = proposer;
	};

	public void Process() {
		Debug.out("\n              EXECUTED | P1_Ready_without_value->Process\n");

		if (D2()) 	//Check for Delivery
			return; 

		if (NV()) 					//Check if there is a V2, if there is none: try assigning. 
			if(A())					//If NV() was successful we can execute A()
				current().S.E();	//If A() was successful we can try E() <- P1_Ready_with_value
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
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->D0");
	return false;} 
	public boolean S(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->S");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p1_pending
	public boolean TO1(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->TO1");
	return false;} 
	public boolean P(Message P){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->P");
	return false;} 
		//--progress
	public boolean D1(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->D01");
	return false;} 
	public boolean R0(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->R0");
	return false;} 
	public boolean R1(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->R1");
	return false;} 
	
	//-------------------------------------------------------------------------------------------------------
	//STATE: P1_Ready_Without_Value
	//-------------------------------------------------------------------------------------------------------

   /* D2:
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
	
	public boolean D2(){
		Debug.out("              EXECUTED | P1_Ready_without_value->D2");

		 if (_proposer.IsDelivered(current().I)) {
			_proposer.setDelivered();
		 	return true;
		}

		return false;
	} 

	//-------------------------------------------------------------------------------------------------------

   /* NV:
    * 	Phase 1 is completed, 
    * 		v2 is null and 
    * 		the pending list happens to be empty
	* 	This instance is not used until a value is submitted by some client
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

(implied)	V2 -> null

 			<-,			-, -,  -,     -,  -, null, - >
	NV: 	<i,p1_ready_without_value, b, {},  null, vb, null, cv>
	_____________________________________________________________ */

	public boolean NV() {
		Debug.out("              EXECUTED | P1_Ready_without_value->NV current().V2 == " + current().V2);

		if ( current().V2 != null )	{			 //Check if value is assigned
			return true;
		}
		Integer V = _proposer.getPendingListValue(); //if client value is available

		if (V != null) { 						 //assign the pending value
			current().V2 = V;
			current().CV = true;
		}
		
		Debug.out("NV() UPDATED | current().V2 = " + current().V2);

		return ( current().V2 != null );
	}

	//-------------------------------------------------------------------------------------------------------
	
   /* A:
    * 	Some client value v2 was assigned to this instance, 
    * 		the record consists of: <I, p1_ready_without_value, b, {0}, null, 0, v2, cv>
    * 	Phase 2 can start with action E 
	_____________________________________________________________
	STATE TRANSITION
	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	_____________________________________________________________

(implied)	V2 -> v2
(implied?)	cv -> true/false

(assign)	V2 -> v2
(assign)	cv -> true

 		<-,p1_ready_with_value, -,  -,     -,  -,   v2, true 	  	>
	A: 	<i,p1_ready_with_value, b, {},  null, vb,   v2, cv(true/false) 	>
	_____________________________________________________________ */

	public boolean A() { final String METHOD = "A";
		Debug.out("              EXECUTED | P1_Ready_without_value->A");

		// Check for V2 Value, NV() executes before A() which tries to acquire a V2 value
		if ( current().V2 == null ) {
			Debug.Error(CLASS, METHOD, "current().V2 == null", "No further action was taken");
			return false; //Important! We do not want to execute the rest of A
		}

		//some client value v2 was assigned to this instance, the record consists, we can go to P1_Ready_with_Value and execute E()
		_proposer.setP1_Ready_With_Value();

		return true;
	}

	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_with_value
	public boolean D3(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->D3");
	return false;} 
	public boolean E(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->E");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p2_pending
	public boolean TO2(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->TO2");
	return false;}
	public boolean D4(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->D4");
	return false;} 
	public boolean C(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->C");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: closed
	public boolean D5(){
		Debug.out("          NOT-EXECUTED | P1_Ready_without_value->D5");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: delivered

	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------

	public String toString() {

		String status = "STATE: P1_READY_WITHOUT_VALUE";
		
		return status;
	}
	
	//-------------------------------------------------------------------------------------------------------
	
	
}//class P1_Ready_without_value
