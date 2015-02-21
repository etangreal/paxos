/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import util.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestMulticastSender 
{
	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public TestMulticastSender()
	{
		Debug.out("Starting Server...\n");
		MulticastSender s = new MulticastSender(1, "239.0.0.1", 3000);
		Thread t = new Thread(s);
		t.start();

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

		while(true)
		{
			try {
				Message msg = new Message(MESSAGE_TYPE.UNDEFINED, AGENT_TYPE.UNDEFINED, null, null);

				Debug.out("Enter Message: ");
				msg.setTest( stdIn.readLine() ); 
				Debug.out("  ... OK");

			    if (msg.getTest() != null) {
	                Debug.out( "\nBroadcasting message: " + msg);
	                s.DispatchMessage( msg );
			    }
			} catch (IOException e) {
				Debug.out("MulticastSender::Error on writeLine::message::" + e.getMessage() );
				e.printStackTrace();
			}
		}
		
	}//public TestMulticastSender
	
	//-------------------------------------------------------------------------------------------------------

}//public class TestMulticastSender
