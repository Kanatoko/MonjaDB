package net.jumperz.sql;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

import net.jumperz.util.*;

public class MConnServer
{
private String dbmsUser;
private String dbmsPass;
private List urlList;
private String lastFileName;
private boolean loaded = false;
private String prefix;
private int normalUrlCount;
// --------------------------------------------------------------------------------
public MConnServer()
{
prefix = "";
}
// --------------------------------------------------------------------------------
public MConnServer( String s )
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
	throw new IOException( "ConnServer is not yet loaded" );
	}
}
// --------------------------------------------------------------------------------
public synchronized void load( String fileName )
throws IOException
{
lastFileName = fileName;
urlList = new ArrayList();

MProperties prop = new MProperties();
prop.load( new FileInputStream( fileName ) );

dbmsUser = prop.getProperty( prefix + "dbmsUser", "root" );
dbmsPass = prop.getProperty( prefix + "dbmsPass", "" );

String[] array = prop.getProperty( prefix + "dbmsUrl", "jdbc:postgresql:" + prefix.replaceAll( "\\.", "" ) ).split( "," );
for( int i = 0; i < array.length; ++i )
	{
	urlList.add( array[ i ] );
	}
normalUrlCount = array.length;

	//load class
String jdbcDriverClassName = MSqlUtil.getJdbcDriverClassName( ( String  )urlList.get( 0 ) );
try
	{
	Class.forName( jdbcDriverClassName ).newInstance();
	}
catch( Exception e )
	{
	throw new IOException( e.getMessage() + urlList.toString() );
	}

loaded = true;
}
// --------------------------------------------------------------------------------
public Connection getConnection()
throws SQLException
{
List tmpList = new ArrayList();
synchronized( this )
	{
	tmpList.addAll( urlList );
	}

Connection conn = null;
SQLException e = null;
for( int i = 0; i < tmpList.size(); ++i )
	{
	String dbmsUri = ( String )tmpList.get( i );
	try
		{
		conn = MSqlUtil.getConnection( dbmsUri, dbmsUser, dbmsPass );
		return conn;
		}
	catch( SQLException ex )
		{
		e = ex;
		tmpList.remove( dbmsUri );
		--i;
		synchronized( this )
			{
			urlList.clear();
			urlList.addAll( tmpList );
			}
		}
	}

throw e;
}
// --------------------------------------------------------------------------------
public String toString()
{
StringBuffer buf = new StringBuffer();
buf.append( "MConnServer:" );
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