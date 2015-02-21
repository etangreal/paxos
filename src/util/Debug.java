
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package util;

import communication.AGENT_TYPE;

public final class Debug 
{
	public static final boolean VERBOSE = false;

    // Suppress default constructor for non-instantiability
    private Debug() {
        throw new AssertionError();
    }

    @SuppressWarnings("unused")
	private static boolean included(DEBUG_TYPE debugType) {
    	return ( VERBOSE &&
    		debugType == DEBUG_TYPE.UNDEFINED
//    		|| debugType == DEBUG_TYPE.SUNDRY

    		|| debugType == DEBUG_TYPE.ERROR
//    	    || debugType == DEBUG_TYPE.MESSAGE    		
//
//    		|| debugType == DEBUG_TYPE.LEADER
//			|| debugType == DEBUG_TYPE.HEARTBEAT
//
//			|| debugType == DEBUG_TYPE.RECEIVE
//			|| debugType == DEBUG_TYPE.SEND
//
//			|| debugType == DEBUG_TYPE.ENQUEUE
//			|| debugType == DEBUG_TYPE.DEQUEUE

			|| debugType == DEBUG_TYPE.STATE
			|| debugType == DEBUG_TYPE.PP_AA
    	);
    }

    @SuppressWarnings("unused")
	private static boolean included(AGENT_TYPE agentType) {
    	return ( VERBOSE &&
    		agentType == AGENT_TYPE.UNDEFINED

    		|| agentType == AGENT_TYPE.LEADER
    	   	|| agentType == AGENT_TYPE.PROPOSER
    		|| agentType == AGENT_TYPE.ACCEPTOR

			|| agentType == AGENT_TYPE.LEARNER
			|| agentType == AGENT_TYPE.CLIENT
		);
    }

	public static void Error(AGENT_TYPE agentType, String CLASS, String METHOD, String OFFENDING_CODE, String ERROR) {
		String out =
		"\n-------------------- DEBUG ERROR --------------------\n\n"+
		CLASS+"::"+METHOD+"::"+OFFENDING_CODE+"\n\t"+ERROR+"\n";
		
		if ( included(DEBUG_TYPE.ERROR) ) 
			System.out.println(out);
	}

	public static void Error(String CLASS, String METHOD, String OFFENDING_CODE, String ERROR) {
		String out =		
		"\n-------------------- DEBUG ERROR --------------------\n\n" +
		CLASS+"::"+METHOD+"::"+OFFENDING_CODE+"\n\t"+ERROR+"\n";
		
		if ( included(DEBUG_TYPE.ERROR) )
			System.out.println(out);
	}
	
	@SuppressWarnings("unused")
	public static void Message(DEBUG_TYPE debugType, String CLASS, String METHOD, String CODE, String MESSAGE) {
		String out =
		"\n-------------------- DEBUG MESSAGE --------------------\n" +
		CLASS+"::"+METHOD+"::"+CODE+"\n\t"+MESSAGE+"\n";
		
		if ( VERBOSE && included(debugType) )
			System.out.println(out);
	}
	
	@SuppressWarnings("unused")
	public static void ToConsole(DEBUG_TYPE debugType, String message) {
		if ( VERBOSE && included(debugType) )
			System.out.println(message);
	}

	@SuppressWarnings("unused")
	public static void ToConsole(DEBUG_TYPE debugType, AGENT_TYPE agentType, String message) {
		if ( VERBOSE && included(debugType) && included(agentType) )
			System.out.println("\n\t"+ message +"\n");
	}
	
	//http://stackoverflow.com/questions/421280/in-java-how-do-i-find-the-caller-of-a-method-using-stacktrace-or-reflection
	//StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace()
	//getClassName(), getFileName(), getLineNumber() and getMethodName()	
	
	public static String ToConsole2(DEBUG_TYPE debugType, AGENT_TYPE agentType, String message) {
		return ToConsole2(true,debugType,agentType,message);
	}
	
	@SuppressWarnings("unused")
	public static String ToConsole2(boolean show, DEBUG_TYPE debugType, AGENT_TYPE agentType, String message)
	{
		String out = 
			"\n____________________________________________________________________________________________________\n" +
			"\n\t"+ message +"\n";
		
		if ( VERBOSE && included(debugType) && included(agentType) ) {
			if(show) System.out.println(out);
			return out;
		}

		return null;
	}
	
	public static void out(String message) {
		if (VERBOSE)
			System.out.println(message);
	}
	
	public static void outAlways(String message) {
		System.out.println(message);
	}
	
	@SuppressWarnings("unused")
	public static String Line(boolean show) {
		
		String out = "\n____________________________________________________________________________________________________\n";
		
		if(VERBOSE && show)
			System.out.println(out);
		
		if(!VERBOSE)
			out = "";
		
		return out;
	}

}//public final class Debug
