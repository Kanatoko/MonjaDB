package net.jumperz.net;

import java.net.*;
import net.jumperz.util.*;
import java.io.*;

public class MConnector
implements MCommand, MSubject1
{
protected String Host;
protected int port;
private Socket socket;
private int state;
private int error_code;
private MSubject1 subject = new MSubject1Impl();

public static final int CONNECTING	= 0;
public static final int CONNECTED	= 1;
public static final int ERROR		= 2;

public static final int UNKNOWNHOST	= 1;
public static final int IOERROR		= 2;

//----------------------------------------------------------------------------------------
public MConnector( String in_Host, int in_port )
{
Host		= in_Host;
port		= in_port;
error_code	= 0;
state		= CONNECTING;
}
//----------------------------------------------------------------------------------------
public final void execute()
{
try
	{
	notify1();
	socket = connect();
	state = CONNECTED;
	}
catch( UnknownHostException e )
	{
	state = ERROR;
	error_code = UNKNOWNHOST;
	}
catch( IOException e )
	{
	state = ERROR;
	error_code = IOERROR;
	}
notify1();
}
// --------------------------------------------------------------------------------
protected Socket connect()
throws IOException
{
Socket _socket = null;
IOException e = null;
for( int i = 0; i < 3; ++i )
	{
	try
		{
		_socket = new Socket( Host, port );
		return _socket;	
		}
	catch( IOException _e )
		{
		e = _e;
		}
	}
throw e;
}
//----------------------------------------------------------------------------------------
public final void breakCommand()
{
try
	{
	if( socket != null )
		{
		socket.close();
		}
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
}
//----------------------------------------------------------------------------------------
public final int getState()
{
return state;
}
//----------------------------------------------------------------------------------------
public final int getErrorCode()
{
return error_code;
}
//----------------------------------------------------------------------------------------
public final Socket getSocket()
{
return socket;
}
//----------------------------------------------------------------------------------------
public void notify1()
{
subject.notify1();
}
//----------------------------------------------------------------
public void register1( MObserver1 observer )
{
subject.register1( observer );
}
//----------------------------------------------------------------
public void removeObservers1()
{
subject.removeObservers1();
}
//----------------------------------------------------------------
public void removeObserver1( MObserver1 observer )
{
subject.removeObserver1( observer );
}
//----------------------------------------------------------------
}
