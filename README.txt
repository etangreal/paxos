
CONFIG FILE (config.txt)
There is a "config.txt" file that NOTE: must be in the directory from which you execute an agent
(there are some extra parameters one can set - but the defaults are fine as they are)

STARTING DIRECTORY
You can start the process from the PROJECT ROOT directory (the same one the "README.txt" is in)

STARTING AGENTS
acceptor 1
acceptor 2
learner 1
proposer 1

READING IN VALUES  
    -the proposer only starts after its has read the last value from the console ...
    -proposer expects an "empty readline" which indicates the last value before it starts

BUILDING
The project was done in eclipse. 
If you want to compile you can open eclipse, import the porject and run the Paxos (Compile) profile.

DEMO
A small demo
http://screencast.com/t/vAi0nXI1G



ADDITIONAL CONFI OPTIONS (in the config.txt file)
number_of_acceptors=3
window_size=3

proposer_heartbeat_interval=2
acceptor_heartbeat_interval=3
learner_heartbeat_interval=10

resend_timeout=6100
timeout_phase1and2=1000
process_window_timer=10