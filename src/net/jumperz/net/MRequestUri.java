package net.jumperz.net;

import net.jumperz.util.*;

public class MRequestUri
{
private String path;
private String params;
private String query;
private String host;
private String user;
private String pass;
private int port = 80;
private boolean normalized = false;
private boolean relative = true;
private boolean isHttps = false;
//--------------------------------------------------------------------------------
public MRequestUri( String str )
{
if( str.indexOf( "http" ) == 0 )
	{
	if( str.indexOf( "https" ) == 0 )
		{
		isHttps = true;
		port = 443;
		}
	relative = false;
	String str2 = MRegEx.getMatch( "http[s]{0,1}://([^/]{1,})/", str );
	if( str2.indexOf( "@" ) > 0 )
		{
		String userPass = MRegEx.getMatch( "(^[^@]{1,})@", str2 );
		if( userPass.indexOf( ":" ) > 0 )
			{
			user = MRegEx.getMatch( "(^[^:]{0,}):", userPass );
			pass = MRegEx.getMatch( ":([^:]{0,})$", userPass );
			}
		str2 = str2.substring( str2.indexOf( "@" ) + 1 );
		}
	if( str2.indexOf( ":" ) > 0 )
		{
		host = MRegEx.getMatch( "^([^:]{0,}):", str2 );
		String portStr = MRegEx.getMatch( ":(.*)$", str2 );
		try
			{
			port = Integer.parseInt( portStr );
			}
		catch( NumberFormatException e )
			{
			port = 80;
			}
		}
	else
		{
		host = str2;
		}
	
	String str3 = MRegEx.getMatch( "(http[s]{0,1}://[^/]{1,})/", str );
	str = str.substring( str3.length() );
	}
int pos = str.indexOf( '?' );
if( pos == -1 )
	{
	query = "";
	}
else
	{
	query = str.substring( pos + 1 );
	str = str.substring( 0, pos );
	}

pos = str.indexOf( ';' );
if( pos == -1 )
	{
	params = "";
	path = str;
	}
else
	{
	params = str.substring( pos + 1 );
	path = str.substring( 0, pos );
	}
}
// --------------------------------------------------------------------------------
public String getHost()
{
return host;
}
// --------------------------------------------------------------------------------
public int getPort()
{
return port;
}
// --------------------------------------------------------------------------------
public void setRelative()
{
relative = true;
}
//--------------------------------------------------------------------------------
public void normalize()
{
if( normalized )
	{
	return;
	}

path = MStringUtil.normalize( path );
normalized = true;
}
// --------------------------------------------------------------------------------
public void addParameter( MAbstractParameter param )
{
if( query.equals( "" ) )
	{
	query = param.getName() + "=" + param.getValue();
	}
else
	{
	query += "&" + param.getName() + "=" + param.getValue();
	}
}
//--------------------------------------------------------------------------------
public String toString()
{
StringBuffer s = new StringBuffer( 256 );

if( !relative )
	{
	if( isHttps )
		{
		s.append( "https://" );
		}
	else
		{
		s.append( "http://" );
		}
	if( user != null && pass != null )
		{
		s.append( user );
		s.append( ":" );
		s.append( pass );
		s.append( "@" );
		}
	s.append( host );
	
	if( isHttps )
		{
		if( port != 443 )
			{
			s.append( ":" );
			s.append( port );
			}			
		}
	else
		{
		if( port != 80 )
			{
			s.append( ":" );
			s.append( port );
			}	
		}
	}

s.append( path );
if( !params.equals( "" ) )
	{
	s.append( ";" );
	s.append( params );
	}
if( !query.equals( "" ) )
	{
	s.append( "?" );
	s.append( query );
	}
return s.toString();
}
// --------------------------------------------------------------------------------
public boolean equals( Object o )
{
if( o.toString().equals( this.toString() ) )
	{
	return true;
	}
return false;
}
//--------------------------------------------------------------------------------
public String getParams() {
	return params;
}

public String getPath() {
	return path;
}

public String getQuery() {
	return query;
}

public void setParams(String string) {
	params = string;
}

public void setPath(String string) {
	path = string;
}

public void setQuery(String string) {
	query = string;
}

}