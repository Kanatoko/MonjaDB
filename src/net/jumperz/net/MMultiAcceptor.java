package net.jumperz.net;

import java.net.*;
import net.jumperz.util.*;
import java.io.*;

public final class MMultiAcceptor
implements MCommand, MSubject1
{
private int port;
private Socket socket;
private ServerSocket serverSocket;
private volatile int state;
private String host;
private boolean terminated = false;
private MSubject1 subject = new MSubject1Impl();
private Exception exception;

public static final int ACCEPTED	= 0;
public static final int ERROR		= 1;
public static final int LISTENING	= 2;
public static final int BACKLOG		= 65535;
//--------------------------------------------------------------------------------------
public MMultiAcceptor( ServerSocket in_serverSocket )
{
serverSocket = in_serverSocket;
}
//--------------------------------------------------------------------------------------
public MMultiAcceptor( int in_port )
{
port = in_port;
}
//--------------------------------------------------------------------------------------
public MMultiAcceptor( String host, int port )
{
this.host = host;
this.port = port;
}
//--------------------------------------------------------------------------------------
public final void execute()
{
try
	{
	serverSocket = getServerSocket();
	state = LISTENING;
	}
catch( IOException e )
	{
	System.err.println( "host:" + host + " port:" + port );
	exception = e;
	e.printStackTrace();
	state = ERROR;
	}
notify1();

while( !terminated )
	{
	try
		{
		while( true )
			{
			socket = serverSocket.accept();
			state = ACCEPTED;
			notify1();
			}
		}
	catch( IOException e )
		{
		if( !terminated )
			{
			e.printStackTrace();		
			}
		
		/*
		if( e.getMessage().equals( "Connection reset by peer" ) )
			{
				// it's ok
			}
		else
			{
			state = ERROR;
			sendNotify();				
			}
		*/
		}
	}
}
// --------------------------------------------------------------------------------
public void shutdown()
{
terminated = true;
MSystemUtil.closeSocket( serverSocket );
}
// --------------------------------------------------------------------------------
public Exception getException()
{
return exception;
}
//--------------------------------------------------------------------------------------
public final void breakCommand()
{
shutdown();
}
//--------------------------------------------------------------------------------------
public final int getState()
{
return state;
}
//--------------------------------------------------------------------------------------
public final Socket getSocket()
{
return socket;
}
//--------------------------------------------------------------------------------------
private ServerSocket getServerSocket()
throws IOException
{
if( serverSocket != null )
	{
	return serverSocket;
	}
else if( host == null )
	{
	return new ServerSocket( port );
	}
else
	{
	return new ServerSocket( port, BACKLOG, InetAddress.getByName( host ) );
	}
}
//--------------------------------------------------------------------------------------
public String toString()
{
if( serverSocket != null )
	{
	return MMultiAcceptor.class.toString() + "/" + serverSocket;
	}
else
	{
	return MMultiAcceptor.class.toString() + "/" + host + ":" + port;		
	}
}
//--------------------------------------------------------------------------------
public String getHost()
{
return host;
}
//--------------------------------------------------------------------------------
public int getPort()
{
return port;
}
//--------------------------------------------------------------------------------------
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
