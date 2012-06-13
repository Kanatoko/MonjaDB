package net.jumperz.net;

import java.io.*;
import java.util.*;
import net.jumperz.util.*;

public class MMail
{
protected InputStream inputStream;
protected ArrayList toAddrList;
protected ArrayList ccAddrList;
protected String fromAddr;
protected boolean isMultipart;
protected ArrayList mailList;
protected String boundary;
protected StringBuffer body;
protected ArrayList headerList;

protected static final int TYPE_TO = 0;
protected static final int TYPE_CC = 1;

protected static final int BUFSIZE = 4096;
protected static String CRLF	= "\r\n";

//----------------------------------------------------------------------------------------------
public MMail()
{
init();
}
//----------------------------------------------------------------------------------------------
public MMail( InputStream in_inputStream )
throws IOException
{
inputStream = in_inputStream;
init();
read();
}
//----------------------------------------------------------------------------------------------
public MMail( String data )
throws IOException
{
inputStream = new ByteArrayInputStream( data.getBytes( "ISO-8859-1" ) );
init();
read();
}
//----------------------------------------------------------------------------------------------
protected void init()
{
headerList	= new ArrayList();
body		= new StringBuffer( BUFSIZE );
}
//----------------------------------------------------------------------------------------------
protected void read()
throws IOException
{
BufferedReader reader		= new BufferedReader( new InputStreamReader( inputStream, MCharset.CS_ISO_8859_1 ) );
StringBuffer headerStrBuf	= new StringBuffer( BUFSIZE );

String line;
boolean hasBody = true;

	// read header
while( true )
	{
	line = reader.readLine();
	if( line == null )
		{
		//throw new IOException( "body not found" );
		
			// body does not exist
		hasBody = false;
		break;
		}
	else if( line.equals( "" ) )
		{
		break;
		}
	else
		{
		headerStrBuf.append( line );
		headerStrBuf.append( CRLF );
		}
	}

analyzeHeader( headerStrBuf.toString() );

if( !hasBody )
	{
	return;
	}

	// read body
while( true )
	{
	line = reader.readLine();
	if( line == null )
		{
		break;
		}
	else
		{
		body.append( line );
		body.append( CRLF );
		}
	}

analyzeBody();
}
//----------------------------------------------------------------------------------------------
protected void analyzeBody()
throws IOException
{
if( isMultipart )
	{
	mailList	= new ArrayList();
	String bodyStr	= body.toString();
	String boundary	= MRegEx.getMatch( "boundary=\"(.+)\"", getHeaderValue( "Content-Type" ) );
	
	bodyStr = "\r\n" + bodyStr;

	String[] bodyArray = bodyStr.split( "\r\n--" + boundary );
	
		// delete the head and tail
	for( int i = 1; i < bodyArray.length - 1; ++i )
		{
		MMail mail = new MMail( bodyArray[ i ].substring( 2 ) );
		mailList.add( mail );
		}
	}
}
//----------------------------------------------------------------------------------------------
protected void analyzeHeader( String headerStr )
{
headerStr = headerStr.replaceAll( "\r\n ", "\r " );
String[] headerArray = headerStr.split( "\r\n" );
for( int i = 0; i < headerArray.length; ++i )
	{
	headerList.add( headerArray[ i ].replaceAll( "\r ", "\r\n " ) );
	}

	// isMultipart?
String contentType = getHeaderValue( "Content-Type" );

if( contentType != null	
 && contentType.toUpperCase().indexOf( "MULTIPART/MIXED" ) > -1
  )
	{
	isMultipart = true;
	}
else
	{
	isMultipart = false;
	}
}
//----------------------------------------------------------------------------------------------
public boolean isMultipart()
{
return isMultipart;
}	
//-------------------------------------------------------------------------------------------
public final String getHeaderValue( String name )
{
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		String value = MRegEx.getMatch( ".{" + ( name.length() + 1 ) + "}(.+)", header );
		return value.trim();
		}
	}
return null;
}
//----------------------------------------------------------------------------------------------
public String getBody()
{
return body.toString();
}
//----------------------------------------------------------------------------------------------
public String getHeader()
{
StringBuffer strBuf = new StringBuffer();

	// fields
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	strBuf.append( ( ( String )headerList.get( i ) ) );
	strBuf.append( CRLF );
	}

	// blank line
strBuf.append( CRLF );

return strBuf.toString();
}	
//-------------------------------------------------------------------------------------------
public final void setHeaderValue( String name, String value )
{
boolean found = false;
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
			// if name is found, replace
		headerList.set( i, name + ": " + value );
		found = true;
		break;
		}
	}
if( !found )
	{
	headerList.add( name + ": " + value );
	}
}
//----------------------------------------------------------------------------------------------
public ArrayList getMailList()
{
return mailList;
}
//----------------------------------------------------------------------------------------------
public String toString()
{
return getHeader() + getBody();
}
//----------------------------------------------------------------------------------------------
public void setBody( String in_body )
{
body = new StringBuffer( BUFSIZE );
body.append( in_body );
}
//----------------------------------------------------------------------------------------------
}