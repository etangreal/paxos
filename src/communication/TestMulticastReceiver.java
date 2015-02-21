/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import util.*;

import java.util.UUID;

public class TestMulticastReceiver 
{
	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public TestMulticastReceiver()
	{
		Debug.out("Starting Client...\n");
		MulticastReceiver r = new MulticastReceiver(UUID.randomUUID(), 1, "239.0.0.1", 3000);		
		Thread t = new Thread(r);
		t.start();

		while(true)
		{
			try {
				Debug.out("\n--------------------------------------------------------------------------------");
				Debug.out("Reading from queue...");
				
				Message msg = r.getQueue().take();
				
				Debug.out( "\nTestMulticastReceiver::Received Message: "+msg.getTest() );
			} catch (InterruptedException e) {
				Debug.out( "TestMulticastReceiver::TestMulticastReceiver()::queue.take()::\n\t" + e.getMessage() );
				e.printStackTrace();
			}
		}

	}//public TestMulticastReceiver

	//-------------------------------------------------------------------------------------------------------
	
}//public class TestMulticastReceiver
