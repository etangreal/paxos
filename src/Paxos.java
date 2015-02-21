/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

import agents.acceptor.Acceptor;
import agents.proposer.Proposer;
import agents.learner.Learner;

import config.*;

public class Paxos
{	final static String CLASS = "Paxos";

	public static void main(final String[] args) {
		Config config = new Config();
		config.loadConfigFile();

		if (args.length >= 1) {

			if ( args[0].toString().toLowerCase().equals("leader")  ) {
				int id = 0;

				if (args.length >= 2) 
					id = Integer.parseInt( args[1].toString() );

				new Proposer(config, id, true /*readFromConsole*/);
			}

			if ( args[0].toString().toLowerCase().equals("proposer") ) {
				int id = 0;

				if (args.length >= 2) 
					id = Integer.parseInt( args[1].toString() );

				new Proposer(config, id, true);
			}

			if ( args[0].toString().toLowerCase().equals("acceptor") ) {
				int id = 0;

				if (args.length >= 2) 
					id = Integer.parseInt( args[1].toString() );

				new Acceptor(config, id);
			}
			
			if ( args[0].toString().toLowerCase().equals("learner") ) {
				int id = 0;

				if (args.length >= 2) 
					id = Integer.parseInt( args[1].toString() );

				new Learner(config, id, false /*VERBOSE*/);
			}

		}//if (args.length >= 1)

	}//main
}//class








