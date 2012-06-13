package net.jumperz.net;

import java.net.*;
import net.jumperz.util.*;
import java.io.*;

public final class MSingleAcceptor
implements MCommand, MSubject1
{
private int port;
private Socket socket;
private ServerSocket serverSocket;
private int state;
private MSubject1 subject = new MSubject1Impl();

public static final int CONNECTED	= 0;
public static final int ERROR		= 1;

private static final int BACKLOG	= 1;
//--------------------------------------------------------------------------------------
public MSingleAcceptor( int in_port )
{
port = in_port;
}
//--------------------------------------------------------------------------------------
public final void execute()
{
try
	{
	serverSocket = new ServerSocket( port, BACKLOG );
	socket = serverSocket.accept();
	state = CONNECTED;
	}
catch( IOException e )
	{
	e.printStackTrace();
	state = ERROR;
	}
notify1();
}
//--------------------------------------------------------------------------------------
public final void breakCommand()
{
try
	{
	if( serverSocket != null )
		{
		serverSocket.close();
		}
	}
catch( IOException e )
	{
	}
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
