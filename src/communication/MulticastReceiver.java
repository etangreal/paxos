
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import util.*;

import java.util.UUID;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.SwingUtilities;

public class MulticastReceiver implements Runnable
{	final String CLASS = "MulticastReceiver";

	public static final int PACKET_SIZE = 65535;
	
	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	UUID _uid = null;
	int _id = 0;
	
	MulticastSocket _socket = null;
	String _ipaddress = "239.0.0.1";
	int _port = 3000;
	InetAddress _group = null;
	BlockingQueue<Message> _queue;
	Runnable _notifyMessageReceivedCallback = null;
	
	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public MulticastReceiver(UUID uid, int id, String ipaddress, int port)
	{	final String METHOD = "MulticastReceiver";

		this._id = id;
		this._uid = uid;
		
		this._port = port;
		this._ipaddress = ipaddress;
		_queue = new LinkedBlockingQueue<Message>();

		try {
			_group = InetAddress.getByName(_ipaddress);
			_socket = new MulticastSocket(_port);
			_socket.joinGroup(_group);
		}catch (UnknownHostException e) {
			final String OFFENDING_CODE = "UnknownHostException::_group = InetAddress.getByName(_ipaddress";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		} catch (IOException e) {
			final String OFFENDING_CODE = "IOException:: _socket = new MulticastSocket(_port) | _socket.joinGroup(_group) ";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------------------------------------

	public void Start()
	{
		Thread r = new Thread(this);
		r.start();
	}
	
	@Override
	public void run() 
	{
		Debug.ToConsole(DEBUG_TYPE.RECEIVE, "\n*** Multicast Receiver Started! [ ID: " + _id + " ] ***");

		while(true)
		{
			Message message = receiveMessage();
			EnqueueMessage(message);
		}//while(true)

	}//public void run

	//-------------------------------------------------------------------------------------------------------
	//PROPERTIES
	//-------------------------------------------------------------------------------------------------------

	public BlockingQueue<Message> getQueue()
	{
		return _queue;
	}
	
	public synchronized void setNotifyMessageReceivedCallback(Runnable notifyMessageReceivedCallback)
	{
		_notifyMessageReceivedCallback = notifyMessageReceivedCallback;
	}

	//-------------------------------------------------------------------------------------------------------
	//EVENTS | CALLBACKS
	//-------------------------------------------------------------------------------------------------------	

	//_notifyMessageReceivedCallback
	private synchronized void execNotifyMessageReceived()
	{	//final String METHOD = "DoNotifyMessageReceived";

		if(_notifyMessageReceivedCallback == null)
			return;

		//Debug.ToConsole(DEBUG_TYPE.RECEIVE, "--- " + CLASS + " :: " + METHOD + " :: DoNotifyMessageReceived executing... ---");
		SwingUtilities.invokeLater(_notifyMessageReceivedCallback);
		//Debug.ToConsole(DEBUG_TYPE.RECEIVE, "--- " + CLASS + " :: " + METHOD + " :: DoNotifyMessageReceived executed... ---");
	}
	
	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------	

	private Message receiveMessage()
	{	final String METHOD = "receiveMessage()";

		byte[] buffer = new byte[PACKET_SIZE];
	    DatagramPacket packet = new DatagramPacket( buffer, buffer.length );

		try {
			_socket.receive(packet);
		} catch (SocketException e) {
			final String OFFENDING_CODE = "SocketException::_socket.receive(packet)";
        	Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
            System.err.println(e);
        } catch (IOException e) { 
        	final String OFFENDING_CODE = "IOException::_socket.receive(packet)";
        	Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}

		ByteArrayInputStream baos = new ByteArrayInputStream(packet.getData());
	    ObjectInputStream oos = null;
	    Message message = null;

	    try {
	  		oos = new ObjectInputStream(baos);
	  		message = (Message) oos.readObject();
		} catch (IOException e) {
			final String OFFENDING_CODE = "oos = new ObjectInputStream(baos);";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			final String OFFENDING_CODE = "(Message) oos.readObject()";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}

		try {
			oos.close();
		} catch (IOException e) {
			final String OFFENDING_CODE = "oos.close()";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}

		return message;
	}

	private void EnqueueMessage(Message message)
	{	final String METHOD = "EnqueueMessage";

		if (message == null)
		{
			final String OFFENDING_CODE = "(message == null)";
			final String ERROR = "*** ERROR, ATTEMPTED TO ENQUEUE NULL MESSAGE ***";
			Debug.Error(CLASS,METHOD,OFFENDING_CODE,ERROR);
			return;
		}

		Debug.ToConsole(DEBUG_TYPE.ENQUEUE, "\n MESSAGE-AGENT'S UID: " + message.getAgentInfo().getUID() + "\n RECEIVER-AGENT'S UID: " + _uid + "\n");
		if ( message.getAgentInfo().getUID().equals(_uid) ) {
			//Drop the message ... because its was sent out by our owner ... 
			Debug.ToConsole(DEBUG_TYPE.ENQUEUE, "\n\t %%% DROPPING\n\t MESSAGE UID: " + message.getUID() + "\n\t FROM AGENT UID: " + _uid + "\n");
			return;
		}

        try {
        	//Debug.Message(DEBUG_TYPE.ENQUEUE, CLASS, METHOD, message.AsDisplayString(), "... message received -> queuing ...");
			_queue.put(message);
			execNotifyMessageReceived(); //notify Parent-Thread (the Event-Dispatch-Thread)
			Debug.Message(DEBUG_TYPE.ENQUEUE, CLASS, METHOD, message.AsDisplayString(), "... messaged received & queued ...");
		} catch (InterruptedException e) {
			final String OFFENDING_CODE = "InterruptedException::_queue.put(message)";
			Debug.Error( CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------------------------------------	

}//public class MulticastReceiver
