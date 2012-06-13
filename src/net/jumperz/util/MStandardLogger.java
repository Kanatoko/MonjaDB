package net.jumperz.util;

import java.io.*;
import java.util.*;
import java.lang.*;

public final class MStandardLogger
implements MLogger
{
private final static int DEFAULT_BUFSIZE = 1024;
private static MStandardLogger instance = new MStandardLogger();
private List streamList;
private int bufsize = DEFAULT_BUFSIZE;

//-----------------------------------------------------------------------------
public MStandardLogger()
{
streamList = new ArrayList();
}
//-----------------------------------------------------------------------------
public static final MStandardLogger getInstance()
{
return instance;
}
//-----------------------------------------------------------------------------
public final synchronized void addStream( OutputStream stream )
{
streamList.add( stream );
}
// --------------------------------------------------------------------------------
public void setBufsize( int i )
{
bufsize = i;
}
// --------------------------------------------------------------------------------
public final void log( String prefix, String message )
{
try
	{
	StringBuffer strBuf = new StringBuffer( bufsize );
	strBuf.append( new Date() );
	strBuf.append( " " );
	strBuf.append( prefix );
	strBuf.append( ": " );
	strBuf.append( message );
	strBuf.append( "\n" );
	
	int count = streamList.size();
	for( int i = 0; i < count; ++i )
		{
		OutputStream stream = ( OutputStream )streamList.get( i );
		synchronized( stream )
			{
			stream.write( strBuf.toString().getBytes( MCharset.CS_ISO_8859_1 ) );
			}
		}
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
}
//-----------------------------------------------------------------------------
public final void log( String message )
{
log( "", message );
}
//-----------------------------------------------------------------------------
}
