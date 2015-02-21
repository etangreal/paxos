package util;



public class Utils {

	//public static final int MILLISECONDS = 1000;
	
	public static void pause(int milliseconds) {	
		Debug.ToConsole( DEBUG_TYPE.SUNDRY, "\nUtil.Pause for: " +milliseconds+" senconds.");
		
		long future = System.currentTimeMillis() + milliseconds;
		while (System.currentTimeMillis() < future ){}
	}
	
}//class Utils
