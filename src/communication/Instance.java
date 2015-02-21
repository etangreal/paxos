
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import java.io.Serializable;

import util.*;

public class Instance implements Comparable<Instance>, Serializable
{
	static final long serialVersionUID = 3309219176816687417L;
	public static final int DEFAULT_TIMEOUT = 1000;
	public static final int INCREMENT = 100;					//INCREMENT => n -> is the base number used to increment the ballot

	public int I = 0;			//instance id (iid)	| A Unique Key -> indicates an instance of a consensus round 
	public int B = 0;			//Ballot			| Each proposal must have a larger Ballot(Proposal Number) than the previous Proposal
	public Integer V2 = null;	//Value 			| V2 (originally called V) - is defined as: a value given by Client OR null OR generated(reserved for future use)
	public Integer VB = 0;		//Value of Ballot 	| VB = B

	public long TS = 0;			//TimeStamp			| For flagging Timeouts, use the setTimeout(null), isTimeout(), clearTimeout() functions

	public Instance() {
		I = 0;
		B = 0;
		V2 = null;
		VB = 0;
		TS = 0;
	}

	public void setTimeout(Integer milliseconds) {
		if (milliseconds == null)
			milliseconds = DEFAULT_TIMEOUT;

		TS = System.currentTimeMillis() + milliseconds;
	}

	public boolean isTimeout() {

		if (TS == 0) return false;
		
		if (TS <= System.currentTimeMillis() ){
			Debug.ToConsole( DEBUG_TYPE.MESSAGE, AGENT_TYPE.UNDEFINED, "\n\n\nInstance::TIMEOUT\n"+AsDisplayString() );
			Debug.ToConsole2( DEBUG_TYPE.MESSAGE, AGENT_TYPE.UNDEFINED, "\nSystem::currentTimeMillis: " + System.currentTimeMillis() +"\n\t TIMEOUT= "+ (System.currentTimeMillis()-TS)  +"\n\n");
			return true;
		}

		return false;
		//return ( TS <= System.currentTimeMillis() );
	}

	public void clearTimeout() {
		TS = 0;
	}

	@Override
	public int compareTo(Instance inst) {
		return I - inst.I;
	}

	public boolean equals(Object obj) {
		//if the two objects are equal in reference, they are equal
		if(this == obj)
			return true;

		if (obj instanceof Instance) {
			Instance inst = (Instance) obj;
			return inst.I == this.I;
		}

		return false;
	}
	
	public Instance toNewInstance()
	{
		Instance instance = new Instance();
		instance.I = I;
		instance.B = B;
		instance.V2 = V2;
		instance.VB = VB;
		instance.TS = TS;
		
		return instance;
	}

	public String toString() {
		return
			"I="+I+";"+
			"B="+B+";"+
			"V2="+V2+";"+
			"VB="+VB+";"+
			"TS="+TS;
	}

	public String AsDisplayString() {
		return 
			"\n\n---------------------------- Instance ----------------------------"+
			"\n\t I: " + I +
			"\n\t B: " + B +
			"\n\t V2: " + V2 +
			"\n\t VB: " + VB +
			"\n\t TS: " + TS + 
			"\n";
	}

}//class