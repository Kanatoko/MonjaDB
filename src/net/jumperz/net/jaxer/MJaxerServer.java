package net.jumperz.net.jaxer;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import net.jumperz.net.*;
import net.jumperz.util.*;

public class MJaxerServer
{
private List urlList;
private String lastFileName;
private boolean loaded = false;
private String prefix;
private int normalUrlCount;

public static final int JAXER_DEFAULT_PORT = 4327;
// --------------------------------------------------------------------------------
public MJaxerServer()
{
prefix = "";
}
// --------------------------------------------------------------------------------
public MJaxerServer( String s )
{
if( s.indexOf( '.' ) == -1 )
	{
	prefix = "." + s;
	}
else
	{
	prefix = s;
	}
}
// --------------------------------------------------------------------------------
public synchronized void reload()
throws IOException
{
if( loaded )
	{
	load( lastFileName );
	}
else
	{
	throw new IOException( "JaxerServer is not yet loaded" );
	}
}
// --------------------------------------------------------------------------------
public synchronized void load( String fileName )
throws IOException
{
/*
test.jaxer=192.168.3.99
test.jaxer=192.168.3.99:4327
test.jaxer=192.168.3.99,192.168.3.98
test.jaxer=192.168.3.99:4327,192.168.3.98:4327
*/

lastFileName = fileName;
MProperties prop = new MProperties();
prop.load( new FileInputStream( fileName ) );

loadFromStr( prop.getProperty( prefix + "jaxer" ) );
}
//--------------------------------------------------------------------------------
public synchronized void loadFromString( String configStr )
{
loadFromStr( configStr );
}
//--------------------------------------------------------------------------------
public synchronized void loadFromStr( String configStr )
{
urlList = new ArrayList();
String[] array = configStr.split( "," );
for( int i = 0; i < array.length; ++i )
	{
	MPeer peer = null;
	String jaxerServer = array[ i ];
	if( jaxerServer.indexOf( ':' ) > -1 ) //with port
		{
		String[] array2 = jaxerServer.split( ":" );
		peer = new MPeer( array2[ 0 ], MStringUtil.parseInt( array2[ 1 ] ) );
		}
	else
		{
		peer = new MPeer( jaxerServer, JAXER_DEFAULT_PORT );
		}
	urlList.add( peer );
	}
normalUrlCount = array.length;

loaded = true;
}
// --------------------------------------------------------------------------------
public synchronized Socket getConnection()
throws IOException
{
Socket socket = null;
IOException e = null;
for( int i = 0; i < urlList.size(); ++i )
	{
	MPeer jaxerServer = ( MPeer )urlList.get( i );
	try
		{
		socket = MSystemUtil.connect( jaxerServer.getHost(), jaxerServer.getPort() );
		return socket;
		}
	catch( IOException ex )
		{
		e = ex;
		urlList.remove( jaxerServer );
		--i;
		}
	}
throw e;
}
// --------------------------------------------------------------------------------
public String toString()
{
StringBuffer buf = new StringBuffer();
buf.append( "MJaxerServer:" );
buf.append( urlList.toString() );
buf.append( ":" );
buf.append( urlList.size() );
buf.append( "/" );
buf.append( normalUrlCount );
return buf.toString();
}
// --------------------------------------------------------------------------------
public List getUrlList()
{
return urlList;
}
// --------------------------------------------------------------------------------
}