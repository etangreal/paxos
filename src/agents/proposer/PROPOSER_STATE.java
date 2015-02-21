package agents.proposer;

public enum PROPOSER_STATE 
{
	UNDEFINED,
	
	EMPTY,
	P1_PENDING,
	P1_READY_WITHOUT_VALUE,
	P1_READY_WITH_VALUE,
	P2_PENDING,
	CLOSED,
	DELIVERED,
	REMOVED,
}