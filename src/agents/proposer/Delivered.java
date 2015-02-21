
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.Message;
import util.*;

public class Delivered implements ProposerState
{
	public final PROPOSER_STATE STATE() { return PROPOSER_STATE.DELIVERED; } 

	//-------------------------------------------------------------------------------------------------------
	// DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	Proposer _proposer;

	//-------------------------------------------------------------------------------------------------------
	// CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public Delivered(Proposer proposer) {
		this._proposer = proposer;
	};

	public void Process() {
		Debug.out("\n              EXECUTED | Delivered->Process\n");

		Debug.out("Instance IID: " + current().I + " has been delivered."); 		
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
		Debug.out("Delivered->D0");
	return false;} 
	public boolean S(){
		Debug.out("Delivered->S");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p1_pending
	public boolean TO1(){
		Debug.out("Delivered->TO1");
	return false;} 
	public boolean P(Message msg) {
		Debug.out("Delivered->P");
	return false;} 
	public boolean D1(){
		Debug.out("Delivered->D01");
	return false;} 
	public boolean R0(){
		Debug.out("Delivered->R0");
	return false;} 
	public boolean R1(){
		Debug.out("Delivered->R1");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_without_value
	public boolean NV(){
		Debug.out("Delivered->NV");
	return false;} 
	public boolean D2(){
		Debug.out("Delivered->D2");
	return false;} 
	public boolean A(){
		Debug.out("Delivered->A");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p1_ready_with_value
	public boolean D3(){
		Debug.out("Delivered->D3");
	return false;} 
	public boolean E(){
		Debug.out("Delivered->E");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: p2_pending
	public boolean TO2(){
		Debug.out("Delivered->TO2");
	return false;} 
	public boolean D4(){
		Debug.out("Delivered->D4");
	return false;} 
	public boolean C(){
		Debug.out("Delivered->C");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: closed
	public boolean D5(){
		Debug.out("Delivered->D5");
	return false;} 

	//-------------------------------------------------------------------------------------------------------
	//state: delivered
	//-------------------------------------------------------------------------------------------------------

	//TODO: Instance is finalized and can be removed from the window?

	//-------------------------------------------------------------------------------------------------------
	// SUNDRY
	//-------------------------------------------------------------------------------------------------------

	public String toString() {

		String status = "STATE: DELIVERED";

		return status;
	}

	//-------------------------------------------------------------------------------------------------------

}//Class Delivered
