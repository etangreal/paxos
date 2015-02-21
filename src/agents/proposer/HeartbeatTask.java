package agents.proposer;


import interfaces.Callable;
import java.util.TimerTask;

public class HeartbeatTask extends TimerTask 
{
	int _id = 0;
	Callable _callbackObj;
	
	public HeartbeatTask(int id, Callable callbackObj)
	{
		_id = id;
		_callbackObj = callbackObj;
	}
	
	public void run() {
	  System.out.println( "\nHeartbeatTask::run; ID: " + _id );

	  _callbackObj.callback( "OnHeartbeat", "hello world".split(" ") );
	}
}

/*
	Timer _heatbeat = new Timer();
	long delay = 2*1000;
	_heatbeat.schedule(new HeartbeatTask(1),0, delay);
*/