package net.jumperz.net;

import java.io.*;
import java.util.*;
import java.net.*;
import net.jumperz.util.*;

public class MResolver
{
private static long MAX_WAIT_TIME = 1000; // 1 second

private Map database = new HashMap( 128 );
private MThreadPool threadPool;
//--------------------------------------------------------------------------------
public MResolver( MThreadPool t )
{
threadPool = t;
}
//--------------------------------------------------------------------------------
public String lookup( String ip )
throws IOException
{
return lookup( ip, MAX_WAIT_TIME );
}
//--------------------------------------------------------------------------------
public String lookup( String ip, long waitTime )
throws IOException
{
synchronized( database )
	{
	if( database.containsKey( ip ) )
		{
		return  ( String )database.get( ip );
		}	
	}

Object mutex = new Object();

while( true )
	{
	boolean result = threadPool.forceCommand( new MResolverLookupCommand( this, ip, mutex ) );
	if( result )
		{
		break;
		}
	MSystemUtil.sleep( waitTime );
	}

synchronized( mutex )
	{
	try
		{
		mutex.wait( waitTime );
		}
	catch( InterruptedException e )
		{
		e.printStackTrace();
		}
	}

String name = "";
synchronized( database )
	{
	if( database.containsKey( ip ) )
		{
		name = ( String )database.get( ip );		
		}
	}

return name;
}
//--------------------------------------------------------------------------------
public void addToDatabase( String ip, String name )
{
synchronized( database )
	{
	database.put( ip, name );
	}
}
//--------------------------------------------------------------------------------
}