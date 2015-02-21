
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package agents.proposer;

import communication.*;
import util.*;

import java.util.List;
import java.util.ArrayList;

public class ProposerInstance extends Instance
{
	static final long serialVersionUID = -8263443359510091482L;

	//State info: <i,S,b,pset,v1,vb,v2,false>
	//Initialized: <0,empty,0,{0},null,0,null,false>

	public ProposerState S = null;		//S: is a symbol representing the state
	public List<Integer> PSET = null; 	//pset: promise set; is a set of promises received
	public Integer V1;					//v1: is the value found after a successful phase 1
	//public Integer V2;					//v2: is the value to use for phase 2
	public boolean CV;					//cv: is a flag to indicate weather v2 is a value received from the client
	
	public ProposerInstance(Instance instance)
	{
		I = instance.I;
		S = null;								//S = empty;
		B = instance.B;
		PSET = new ArrayList<Integer>(); 		//EMPTY SET
		V1 = null;
		VB = instance.VB;
		V2 = instance.V2;
		CV = false;
		TS = 0;
	}

	public ProposerInstance(ProposerState state)
	{
		//Initialized to: <0,empty,0,{0},null,0,null,false>
		I = 0;
		S = state;								//S = empty;
		B = 0;
		PSET = new ArrayList<Integer>(); 		//EMPTY SET
		V1 = null;
		VB = 0;
		V2 = null;
		CV = false;
		TS = 0;
	}
	
	public ProposerInstance(int i, ProposerState state)
	{
		//Initialized to: <0,empty,0,{0},null,0,null,false>
		I = i;
		S = state;								//S = empty;
		B = 0;
		PSET = new ArrayList<Integer>(); 		//EMPTY SET
		V1 = null;
		VB = 0;
		V2 = null;
		CV = false;
		TS = 0;
	}

	public void clearPromises() {
		PSET.clear();
	}

	//id -> acceptor id
	public void addPromise(int acceptorID)  {
		
		//Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.LEADER, "\n\t ProposerInstance::addPromice::acceptorID:" + acceptorID + " | PSET: " + PSET + "\n\n");
		
		if ( PSET.contains(acceptorID) ) 
			return;
		
		PSET.add(acceptorID);
		
		Debug.ToConsole(DEBUG_TYPE.STATE, AGENT_TYPE.LEADER, "\n\t ProposerInstance::addPromice::acceptorID:" + acceptorID + " | PSET: " + PSET + "\n\n");
	}

	public int countPromises()
	{
		return PSET.size();
	}

	public String toString() {
		return
			I + ";" +
			S + ";" +
			B + ";" +
			"{" + PSET + "};" +
			V1 + ";" +
			VB + ";" +
			V2 + ";" +
			CV;
	}

	public String AsDisplayString() { 
		return 
			"\n\n---------------------------- Proposer Instance ----------------------------"+ 
			"\n\t S: " + S + 
			"\n\t PSET: "+ PSET + ";" + 
			"\n\t V1: " + V1 + 
			"\n\t V2: " + V1 + 
			"\n\t CV: " + CV + 
			super.AsDisplayString(); 
	}

}//class ProposerInstance
