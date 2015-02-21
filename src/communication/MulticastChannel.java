/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

//import util.*;

import java.net.MulticastSocket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class MulticastChannel
{
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	UUID _uid = null;
	int _id = 0;	
	
	MulticastSocket _socket = null;
	String _ipaddress = "239.0.0.1";
	int _port = 3000;
	MulticastSender _sender;
	MulticastReceiver _receiver;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------
	
	public MulticastChannel(UUID uid, int id, String ipaddress, int port)
	{
		this._id = id;
		this._uid = uid;
		
		this._port = port;
		this._ipaddress = ipaddress;
		
		_receiver = new MulticastReceiver(uid, id, ipaddress, port);
		_sender = new MulticastSender(id, ipaddress, port);

		Thread s = new Thread(_sender);
		s.start();

		Thread r = new Thread(_receiver);
		r.start();
	}
	
	//-------------------------------------------------------------------------------------------------------
	//PROPERTIES
	//-------------------------------------------------------------------------------------------------------
	
	public BlockingQueue<Message> getMessageQueue()
	{
		return _receiver.getQueue();
	}
	
	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------	

	public void DispatchMessage(Message message)
	{
		_sender.DispatchMessage(message);
	}
	
	public void setNotifyMessageReceivedCallback(Runnable notifyMessageReceivedCallback)
	{
		_receiver.setNotifyMessageReceivedCallback(notifyMessageReceivedCallback);
	}
	
	//-------------------------------------------------------------------------------------------------------	

}//public class MulticastChannel
