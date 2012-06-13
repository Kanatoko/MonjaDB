package net.jumperz.util.shutdown;

import net.jumperz.app.*;
import net.jumperz.net.*;
import net.jumperz.util.*;
import java.io.*;
import java.net.*;


public class MShutdownerObserver
extends MAbstractShutdown
implements MObserver1
{
private MMultiAcceptor acceptor;
private MApplication application;

//-------------------------------------------------------------------------------
public void update()
{
int state = acceptor.getState();
if( state == MMultiAcceptor.ACCEPTED )
	{
	try
		{	
		Socket socket = acceptor.getSocket();
				
		if( socket.getInetAddress().equals( InetAddress.getByName( clientHost ) )
		 && socket.getPort() == clientPort
		  )
			{
			checkPassword( socket );
			}
		else
			{
			socket.close();
			}
		}
	catch( IOException e )
		{
		e.printStackTrace();
		}
	}
else if( state == MMultiAcceptor.ERROR )
	{
	application.shutdown();
	}
}
//-------------------------------------------------------------------------------
private void checkPassword( Socket socket )
throws IOException
{
BufferedReader reader = new BufferedReader( new InputStreamReader( socket.getInputStream(), MCharset.CS_ISO_8859_1 ) );

String line = reader.readLine();

socket.close();

if( line != null )
	{
	if( line.equals( password ) )
		{
		application.log( "SYS", "shutdown password confirmed." );
		application.shutdown();
		}
	else
		{
		application.log( "SYS", "incorrect password : " + line );
		}
	}
}
//-------------------------------------------------------------------------------
public void setAcceptor( MMultiAcceptor acceptor )
{
this.acceptor = acceptor;
}
//--------------------------------------------------------------------------------
public void setApplication( MApplication application )
{
this.application = application;
}
//--------------------------------------------------------------------------------
}