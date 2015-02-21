/*
 * Author: Ernst Salzmann
 * Date: 19-05-2012
 *
 */

package communication;

public enum MESSAGE_TYPE {

	UNDEFINED,
	HEARTBEAT,
	
	PREPARE,
	PROMISE,

	ACCEPT,
	ACCEPTED,
	
	REJECT,
	
	RESEND,
	REQUEST_MAX_INSTANTS
}

