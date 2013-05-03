package net.jumperz.io.multicore;

import java.io.*;
import java.util.*;

public class MWriter
{
private OutputStream out;
private volatile int writeIndex;
private String charset;
//--------------------------------------------------------------------------------
public MWriter( OutputStream out, String charset )
throws IOException
{
this.out = new BufferedOutputStream( out );
this.charset = charset;
}
//--------------------------------------------------------------------------------
public synchronized void write( Map data )
throws IOException
{
while( true )
	{
	int index = ( ( Integer )data.get( "index" ) ).intValue();
	if( writeIndex == index )
		{
		List list = ( List )data.get( "data" );
		for( int i = 0; i < list.size(); ++i )
			{
			String line = ( String )list.get( i );
			if( line != null )
				{
				out.write( line.getBytes( charset ) );
				out.write( ( byte ) 0x0A );
				}
			}
		out.flush();
		++writeIndex;
		try
			{
			this.notifyAll();
			}
		catch( Exception e )
			{
			e.printStackTrace();
			}
		return;
		}
	else
		{
		try
			{
			this.wait();
			}
		catch( Exception e )
			{
			e.printStackTrace();
			break;
			}
		}
	}
}
//--------------------------------------------------------------------------------
}