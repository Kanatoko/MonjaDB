package net.jumperz.io.multicore;

import java.io.*;
import java.util.*;

public class MReader
{
private BufferedReader reader;
private int batchSize = 10000;
private boolean closed = false;
private int index = 0;
//--------------------------------------------------------------------------------
public MReader( InputStream in )
throws IOException
{
init( in, "ISO-8859-1" );
}
//--------------------------------------------------------------------------------
public MReader( InputStream in, String charset )
throws IOException
{
init( in, charset );
}
//--------------------------------------------------------------------------------
public void init( InputStream in, String charset )
throws IOException
{
reader = new BufferedReader( new InputStreamReader( in, charset ) );
}
//--------------------------------------------------------------------------------
public void setBatchSize( int i )
{
batchSize = i;
}
//--------------------------------------------------------------------------------
public synchronized Map getLines()
throws IOException
{
if( closed )
	{
	return null;
	}

Map map = new HashMap();
List list = new ArrayList( batchSize );
String line = null;
for( int i = 0; i < batchSize; ++i )
	{
	line = reader.readLine();
	if( line == null )
		{
		closed = true;
		break;
		}
	list.add( line );
	}

map.put( "index", new Integer( index ) );
map.put( "data", list );
++index;
return map;
}
//--------------------------------------------------------------------------------
}