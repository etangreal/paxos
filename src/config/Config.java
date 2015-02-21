package config;
/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */


import util.*;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


public class Config 
{	final static String CLASS = "Config";

	//-------------------------------------------------------------------------------------------------------
	//DATA-MEMBERS
	//-------------------------------------------------------------------------------------------------------

	private String acceptors_ip = "239.0.0.1";
	private int acceptors_port = 5000;

	private String proposers_ip = "239.0.0.1";
	private int proposers_port = 6000;

	private String learners_ip = "239.0.0.1";
	private int learners_port = 7000;

	private String leaders_ip = "239.0.0.1";
	private int leaders_port = 8000;
	
	private int number_of_acceptors = 3;
	
	private int proposer_heartbeat_interval = 2;
	private int acceptor_heartbeat_interval = 3;
	private int learner_heartbeat_interval = 3;
	
	private int resend_timeout = 3100;
	private int timeout_phase1and2 = 1000;
	
	private int window_size = 100;
	private int process_window_timer=10;

	//-------------------------------------------------------------------------------------------------------
	//CONSTRUCTOR
	//-------------------------------------------------------------------------------------------------------

    public Config() 
    {

    }

	//-------------------------------------------------------------------------------------------------------
	//PROPERTIES
	//-------------------------------------------------------------------------------------------------------

	public String getAcceptorsIp() { 
		return acceptors_ip; 
	}
	public int getAcceptorsPort()  { 
		return acceptors_port; 
	}

	public String getProposersIP() { 
		return proposers_ip; 
	}
	public int getProposersPort() { 
		return proposers_port; 
	}

	public String getLearnersIP() { 
		return learners_ip; 
	}
	public int getLearnersPort() { 
		return learners_port; 
	}

	public String getLeadersIp() { 
		return leaders_ip; 
	};
	public int getLeadersPort() { 
		return leaders_port; 
	};
	
	public int getNumberOfAcceptors() { 
		return number_of_acceptors;
	}

	public int getProposerHeartbeatInterval() { 
		return proposer_heartbeat_interval;
	}
	
	public int getAcceptorHeartbeatInterval() { 
		return acceptor_heartbeat_interval;
	}
	
	public int getLearnerHeartbeatInterval() { 
		return learner_heartbeat_interval;
	}

	public int getResendTimeout() {
		return resend_timeout;
	}
	
	public int getTimeoutPhase1and2(){
		return timeout_phase1and2;
	}

	public int getWindowSize() {
		return window_size;
	}
	
	public int getProcessWindowTimer(){
		return process_window_timer;
	}

	//-------------------------------------------------------------------------------------------------------
	//METHODS
	//-------------------------------------------------------------------------------------------------------	

	public void Configure(Properties configProperties)
	{	final String METHOD = "Configure(Properties config)";

        //Print out the configuration parameters
        @SuppressWarnings("rawtypes")
		Enumeration en = configProperties.keys();

        while (en.hasMoreElements())
        {
            String key = (String) en.nextElement();

            try 
            {
	            if ( key.toLowerCase().equals("acceptors") )
	            {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	acceptors_ip = values[0];
	            	acceptors_port = Integer.parseInt( values[1] );
	            }

	            if ( key.toLowerCase().equals("proposers") )
	            {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	proposers_ip = values[0];
	            	proposers_port = Integer.parseInt( values[1] );
	            }
	            
	            if ( key.toLowerCase().equals("learners") )
	            {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	learners_ip = values[0];
	            	learners_port = Integer.parseInt( values[1] );
	            }
	            
	            if ( key.toLowerCase().equals("leaders") )
	            {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	learners_ip = values[0];
	            	leaders_port = Integer.parseInt( values[1] );
	            }
	            
	            if ( key.toLowerCase().equals("number_of_acceptors"))
	            {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	number_of_acceptors = Integer.parseInt( values[0] );
	            }

	            if ( key.toLowerCase().equals("proposer_heartbeat_interval")) {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	proposer_heartbeat_interval = Integer.parseInt( values[0] );
	            }
	            
	            if ( key.toLowerCase().equals("acceptor_heartbeat_interval")) {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	acceptor_heartbeat_interval = Integer.parseInt( values[0] );
	            }
	            
	            if ( key.toLowerCase().equals("learner_heartbeat_interval")) {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	learner_heartbeat_interval = Integer.parseInt( values[0] );
	            }
	            
	            if ( key.toLowerCase().equals("resend_timeout"))
	            {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	resend_timeout = Integer.parseInt( values[0] );
	            }
	            
	            if ( key.toLowerCase().equals("timeout_phase1and2")) {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	timeout_phase1and2 = Integer.parseInt( values[0] );
	            }
	            
	            if ( key.toLowerCase().equals("window_size")) {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	window_size = Integer.parseInt( values[0] );
	            }
	            
	            if ( key.toLowerCase().equals("process_window_timer")) {
	            	String[] values = configProperties.get(key).toString().split(" ");
	            	process_window_timer = Integer.parseInt( values[0] );
	            }

            }
            catch(Exception e)
            {
            	final String OFFENDING_CODE = " <acceptors,proposers,learners,leaders>_ip = values[0] | <acceptors,proposers,learners,leaders>_port = Integer.parseInt( values[1] ) ";
            	Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
                e.printStackTrace();
                return;
            }

        }//while (en.hasMoreElements())

        Debug.out("\n------------ Config Configured ------------\n");
        
        Debug.out("acceptors: " + acceptors_ip + " " + acceptors_port);
        Debug.out("proposers: " + proposers_ip + " " + proposers_port);
        Debug.out("learners: " + learners_ip + " " + learners_port);
        Debug.out("leaders: " + leaders_ip + " " + leaders_port);

        Debug.out("");
        
        Debug.out("Number of Acceptors: " + getNumberOfAcceptors() );
        Debug.out("Window Size: " + getWindowSize() );
        Debug.out("Process Window Timer: " + getProcessWindowTimer() );
        
        Debug.out("");
        
        Debug.out("Proposer HearBeat Interval: " + getProposerHeartbeatInterval() );
        Debug.out("Acceptor HeartBeat Interval: " + getAcceptorHeartbeatInterval() );
        Debug.out("Learner HeartBeat Interval: " + getLearnerHeartbeatInterval() );
        
        Debug.out("");
        
        Debug.out("Resend Timeout: " + getResendTimeout() );
        Debug.out("Timeout Phase 1 & 2: "+ getTimeoutPhase1and2());

        

	}//public static void Configure(Properties config)
	
	//-------------------------------------------------------------------------------------------------------
	
	public void loadConfigFile() { loadConfigFile("."); }

	public void loadConfigFile(String path) 
	{ 	final String METHOD = "loadConfigFile(String path)";
	
        //Load configuration file
        String filename = path + "\\config.txt";
    	Properties configProperties = new Properties();

        try {
            configProperties.load(new FileInputStream(filename));
        } catch (FileNotFoundException e) {
        	final String OFFENDING_CODE = "config.load(new FileInputStream(filename))";
        	Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
        	Debug.out( "Current Directory: " + System.getProperty("user.dir") );
        	Debug.out( "Config File path given: " + filename );
            e.printStackTrace();
            return;
        } catch (IOException e) {
        	final String OFFENDING_CODE = "config.load(new FileInputStream(filename))";
        	Debug.Error(CLASS, METHOD, OFFENDING_CODE, e.getMessage() );
            e.printStackTrace();
            return;
        }
 
        //Print out the configuration parameters
        @SuppressWarnings("rawtypes")
		Enumeration en = configProperties.keys();
        
        Debug.out("\n********** System configuration **********\n");
        while (en.hasMoreElements())
        {
            String key = (String) en.nextElement();
            Debug.out(key + " => " + configProperties.get(key)); 
        }
        
        Configure(configProperties);

    }//public Config loadConfigFile(String path)
	
	//-------------------------------------------------------------------------------------------------------

}//public class Config 
