
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.Message;

public interface ProposerState 
{	
	public PROPOSER_STATE STATE();// { return PROPOSER_STATE.UNDEFINED; }

	//-------------------------------------------------------------------------------------------------------
	
	public void Process();
	
	//-------------------------------------------------------------------------------------------------------	
	
	/* STATE TRANSITION - RECORD UPDATE DOCUMETATION
	_____________________________________________________________

	RECORD:	<I,S,B,PSET,V1,VB,V2,CV>
	init:	<0,empty,0,{},null,0,null,false>

	_____________________________________________________________
		I -> i
		S -> p1_pending
		B -> b

		<i,p1_pending,b, -,  -, -, -, ->
	S: 	<i,p1_pending,b,{},null,0,null,false>

	_____________________________________________________________
		B -> b++
		PSET -> {}

// Prepare requests only consist of <i,b> there for we can reset V1 & VB 
// Only once we have a promise from acceptors do we issue values

		V1 -> null
		VB -> 0

 			<-,         -, b++, {},  null, 0,  -, - >
	TO1: 	<i,p1_pending, b++, {},  null, 0, v2, cv>
	_____________________________________________________________
		PSET -> {Pa U PSET}

if (Pa.V != null) && (Pa.VB > VB)
	V1 = Pa.V
	VB = Pa.VB

 		<-,         -, -, {Pa U PSET},  v, vb,  -, - >
	P: 	<i,p1_pending, b, {Pa U PSET},  v1, vb, v2, cv>
	_____________________________________________________________

if (V1 == null)
	R0:
else
	R1:
	_____________________________________________________________
		S -> p1_ready_without_value
	?	PSET -> {}
(implied)	V1 -> null
(implied?)	VB = 0


 		<-,p1_ready_without_value, -, {},  null, 0,   -, - >
	R0: 	<i,p1_ready_without_value, b, {},  null, vb, v2, cv>
	_____________________________________________________________

if (V2 == null) && (PendingList is empty)
	NV:
(N.B: basically NV: is a waiting step ... nothing is done)
	_____________________________________________________________

(implied)	V2 -> null

 		<-,			-, -,  -,     -,  -, null, - >
	NV: 	<i,p1_ready_without_value, b, {},  null, vb, null, cv>
	_____________________________________________________________

(implied)	V2 -> v2
(implied?)	cv -> true/false

(assign)	V2 -> v2
(assign)	cv -> true

 		<-,p1_ready_with_value, -,  -,     -,  -,   v2, true 	  	>
	A: 	<i,p1_ready_with_value, b, {},  null, vb,   v2, cv(true/false) 	>
	_____________________________________________________________

v2 = null:
		V2 -> V1
		cv -> false

		V1 -> null
		VB -> 0

		<-,		     -, -, {},  null, 0,  v1, false >
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, false >
	_____________________________________________________________

v1 = v2: (was assigned after timeuot)

		V1 -> null
		VB -> 0

		<-,		     -, -, {},  null, 0,   -, - 	>
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, cv(false) >
	_____________________________________________________________

(v1 != v2) && (cv = true)

		V2 -> V1
		cv -> false

		V1 -> null
		VB -> 0

		<-,		     -, -, {},  null, 0,  v1, false >
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, false >
	_____________________________________________________________

(v1 != v2) && (cv = false)

		V2 -> V1
		cv -> false

		V1 -> null
		VB -> 0

		<-,		     -, -, {},  null, 0,  v1, false >
	R1: 	<i,p1_ready_with_value, b, {},  null, 0,  v1, false >
	_____________________________________________________________

		S -> p2_pending

		<-,p2_pending, -,  -,     -, 0,   -, -  >
	E: 	<i,p2_pending, b, {},  null, 0,  v2, cv >
	_____________________________________________________________


		S -> p1_pending
		b => b++

		<-,p1_pending, b++,  -,     -, 0,   -, -  >
	T02: 	<i,p1_pending, b++, {},  null, 0,  v2, cv >
	_____________________________________________________________ */

	//state: empty
		//--progress
	public boolean D0();
	public boolean S();

	//state: p1_pending
	public boolean TO1();
	public boolean P(Message msg);
		//--progress
	public boolean D1();
	public boolean R0();
	public boolean R1();
	
	//state: p1_ready_without_value
	public boolean NV();
		//--progress
	public boolean D2();
	public boolean A();
	
	//state: p1_ready_with_value
		//--progress
	public boolean D3();
	public boolean E();
	
	//state: p2_pending
	public boolean TO2();
		//--progress
	public boolean D4();
	public boolean C();
	
	//state: closed
		//--progress
	public boolean D5();
	
	//state: delivered
	
	//-------------------------------------------------------------------------------------------------------
	
	public String toString();
	
	//-------------------------------------------------------------------------------------------------------
	
}//State
