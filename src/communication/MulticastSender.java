/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

import util.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MulticastSender implements Runnable
{	final String CLASS = "MulticastSender";

	public static final int PACKET_SIZE = 65535;

	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	MulticastSocket _socket = null;
	int _id = 0;
	String _ipaddress = "239.0.0.1";
	int _port = 3000;
	InetAddress _group = null;
	BlockingQueue<Message> _queue;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

	public MulticastSender(int id, String ipaddress, int port)
	{	final String METHOD = "MulticastSender(String id, String ipaddress, int port)";
	
		this._id = id;
		this._ipaddress = ipaddress;
		this._port = port;
		_queue = new LinkedBlockingQueue<Message>();

		try {
			_group = InetAddress.getByName(_ipaddress);
			_socket = new MulticastSocket(_port);
			_socket.joinGroup(_group);
		}catch (UnknownHostException e) {
			final String OFFENDING_CODE = "_group = InetAddress.getByName(_ipaddress)";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			final String OFFENDING_CODE = " _socket = new MulticastSocket(_port) | _socket.joinGroup(_group) ";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage());
			e.printStackTrace();
		}

	}//public MulticastSender()

	//-------------------------------------------------------------------------------------------------------

	public void Start() {
		Thread s = new Thread(this);
		s.start();
	}
	
	@Override
	public void run() 
	{
		Debug.out("\n*** MulticastServer Started! [ ID: " + _id + " ] ***");

		processQueue();

	}//public void run

	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------

	private void processQueue()
	{	
		while(true)
		{
			Message message;
			try {
				message = _queue.take();
				
	            //Debug.ToConsole(DEBUG_TYPE.ENQUEUE, "\nMulticastSender::processQueue::Sending Message: " + message.AsDisplayString() + "\n\t ... sending message ...");
				SendMessage(message);
				Debug.ToConsole(DEBUG_TYPE.ENQUEUE,"\nMulticastSender::processQueue::Message Sent: " + message.AsDisplayString() + "\n\t ... message sent ...");
			} catch (InterruptedException e) {
				Debug.out( "MulticastSender::processQueue()::_queue.take()::\n\t" + e.getMessage() );
				e.printStackTrace();
			}
		}//while(true)
	}

	//-------------------------------------------------------------------------------------------------------

	public void DispatchMessage(Message message)
	{
		try {
			_queue.put(message);
		} catch (InterruptedException e) {
			Debug.out( "EnqueueMessage::run()::InterruptedException::" + e.getMessage() );
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------------------------------------

	private boolean SendMessage(Message message)
	{ 	final String METHOD = "SendMessage";

		byte[] buffer = null;
  		ByteArrayOutputStream baos = new ByteArrayOutputStream();
  		ObjectOutputStream oos = null;
		
		try {
	  		oos = new ObjectOutputStream(baos);
			oos.writeObject(message);
			oos.flush();
		} catch (IOException e) {
			final String OFFENDING_CODE = "IOException::oos = new ObjectOutputStream(baos) | oos.flush()";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}

		buffer = baos.toByteArray();
		DatagramPacket packet = new DatagramPacket( buffer, buffer.length, _group, _port );

		try {
			_socket.send(packet);
		} catch (IOException e) {
			final String OFFENDING_CODE = "IOException::socket.send(packet)";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
			//TODO: requeue message?
			return false;
		}

		try {
			oos.close();
		} catch (IOException e) {
			final String OFFENDING_CODE = "IOException::oos.close()";
			Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
			e.printStackTrace();
		}

		return true;

	}//SendMessage
	
	//-------------------------------------------------------------------------------------------------------
	
}//public class MulticastSender

