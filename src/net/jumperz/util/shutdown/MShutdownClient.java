package net.jumperz.util.shutdown;

import java.net.*;
import java.io.*;
import net.jumperz.util.*;

public class MShutdownClient
extends MAbstractShutdown
{
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws IOException
{
if( args.length != 1 )
	{
	System.err.println( "Usage: java net.jumperz.util.shutdown.MShutdownClient CONFIG_FILE_NAME" );
	return;
	}
else
	{
	MAbstractShutdown.load( args[ 0 ] );
	MShutdownClient shutdownClient = new MShutdownClient();
	shutdownClient.start();
	}
}
//--------------------------------------------------------------------------------
public void start()
{
try
	{
	InetAddress clientInetAddr = InetAddress.getByName( clientHost );
	Socket socket = new Socket( serverHost, serverPort, clientInetAddr, clientPort );
	socket.getOutputStream().write( ( password + "\n" ).getBytes( MCharset.CS_ISO_8859_1 ) );
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
}
//--------------------------------------------------------------------------------
}