package net.jumperz.net;

import java.net.*;
import java.io.*;
import java.util.*;
import net.jumperz.net.exception.*;
import net.jumperz.io.*;
import net.jumperz.util.*;
import java.util.zip.*;

public final class MHttpResponse
extends MHttpData
implements Cloneable
{
private String version;
private int statusCode;
private String reasonPhrase;
private String cachedBody;

private static final String DEFAULT_VERSION		= "HTTP/1.0";
private static final int DEFAULT_STATUS_CODE		= 200;
private static final String DEFAULT_REASON_PHRASE	= "OK";
//--------------------------------------------------------------------------------
public MHttpResponse()
{
statusCode	= DEFAULT_STATUS_CODE;
version		= DEFAULT_VERSION;
reasonPhrase	= DEFAULT_REASON_PHRASE;
}
// --------------------------------------------------------------------------------
public MHttpResponse( byte[] buffer )
throws IOException
{
BufferedInputStream i = new BufferedInputStream( new ByteArrayInputStream( buffer ) );
hasBodyFlag = true;
init( i );
}
//-------------------------------------------------------------------------------------------
public MHttpResponse( BufferedInputStream in )
throws IOException
{
hasBodyFlag = true;
init( in );
}
//-------------------------------------------------------------------------------------------
public MHttpResponse( BufferedInputStream in, boolean isResponseOfHeadMethod )
throws IOException
{
init( in, isResponseOfHeadMethod );
}
//--------------------------------------------------------------------------------
public MHttpResponse( String s )
throws IOException
{
BufferedInputStream i = new BufferedInputStream( new ByteArrayInputStream( s.getBytes( MCharset.CS_ISO_8859_1 ) ) );
hasBodyFlag = true;
init( i );
}
// --------------------------------------------------------------------------------
public void init( BufferedInputStream in, boolean isResponseOfHeadMethod )
throws IOException
{
hasBodyFlag = !isResponseOfHeadMethod;
init( in );
}
//--------------------------------------------------------------------------------
public void init( BufferedInputStream in )
throws IOException
{
bufferedInputStream = in;
recvHeader();
if( hasBodyFlag )
	{
	recvBody();
	}
}
//-------------------------------------------------------------------------------------------
private void recvHeader()
throws IOException
{
bufferedInputStream.mark( Integer.MAX_VALUE );

MLineReader reader = getLineReader();
reader.setInputStream( bufferedInputStream );

	//1st line of HTTP response header
String line = reader.readLine();
if( line == null )
	{
	throw new MHttpStreamClosedException( "Stream is closed" );
	}

splitStatusLine( line );
headerLength += line.length() + reader.getLastDelimiterSize();

readHeaderFields( reader );
}
//--------------------------------------------------------------------------------
public void setStatusLine( String line )
throws IOException
{
splitStatusLine( line );
}
//--------------------------------------------------------------------------------
private void splitStatusLine( String line )
throws IOException
{
int spaceIndex = line.indexOf( ' ' );
if( spaceIndex <= 0 )
	{
	throw new MHttpIOException( "Invalid status line:" + line );
	}

version = line.substring( 0, spaceIndex );

if( line.length() < spaceIndex + 4 )
	{
	throw new MHttpIOException( "Invalid status line:" + line );	
	}

try
	{
	statusCode = Integer.parseInt( line.substring( spaceIndex + 1, spaceIndex + 4 ) );
	}
catch( NumberFormatException e )
	{
	throw new MHttpIOException( "Invalid status line:" + line );		
	}

if( statusCode < 200 
 || statusCode == 304
 || statusCode == 204
  )
	{
	hasBodyFlag = false;
	}

if( line.length() == spaceIndex + 4 )
	{
	reasonPhrase = "";
	}
else 
	{
	reasonPhrase = line.substring( spaceIndex + 5 );
	}

/*
String[] statusLineArray = line.split( " " );
if( statusLineArray.length < 3 )
	{
	throw new MHttpIOException( "Invalid status line:" + line );
	}

version = statusLineArray[ 0 ];

statusCode = Integer.parseInt( statusLineArray[ 1 ] );

if( statusCode < 200 
 || statusCode == 304
 || statusCode == 204
  )
	{
	hasBodyFlag = false;
	}

StringBuffer strBuf = new StringBuffer();
for( int i = 2; i < statusLineArray.length; ++i )
	{
	if( i != 2 )
		{
		strBuf.append( " " );
		}
	strBuf.append( statusLineArray[ i ] );
	}
reasonPhrase = strBuf.toString();
*/
}
//-------------------------------------------------------------------------------------------
public final int getStatusCode()
{
return statusCode;
}
//-------------------------------------------------------------------------------
public final byte[] getHeader()
{
ByteArrayOutputStream buf = null;
try
	{
		// request line
	buf = new ByteArrayOutputStream( headerBufSize );
	buf.write( version.getBytes( MCharset.CS_ISO_8859_1 ) );
	buf.write( (byte)0x20 );
	buf.write( Integer.toString( statusCode ).getBytes( MCharset.CS_ISO_8859_1 ) );
	buf.write( (byte)0x20 );
	buf.write( reasonPhrase.getBytes( MCharset.CS_ISO_8859_1 ) );
	buf.write( CRLF );
	
		// fields
	int count = headerList.size();
	for( int i = 0; i < count; ++i )
		{
		buf.write( ( ( String )headerList.get( i ) ).getBytes( MCharset.CS_ISO_8859_1 ) );
		buf.write( CRLF );
		}
	
		// blank line
	buf.write( CRLF );
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
return buf.toByteArray();
}
//-------------------------------------------------------------------------------------------
public String getStatusLine()
{
StringBuffer strBuf = new StringBuffer( 100 );
strBuf.append( version );
strBuf.append( " " );
strBuf.append( Integer.toString( statusCode ) );
strBuf.append( " " );
strBuf.append( reasonPhrase );
return strBuf.toString();
}
//--------------------------------------------------------------------------------
protected void recvBodyUntilDisconnected()
throws IOException
{
if( bodyBuffer == null )
	{
	bodyBuffer = new MBuffer();
	}

byte[] buffer = new byte[ bodyBufSize ];
int received;

while( true )
	{
	bufferedInputStream.mark( bodyBufSize );
	received = bufferedInputStream.read( buffer );
	if( received <= 0 )
		{
		break;
		}

	bufferedInputStream.reset();
	bufferedInputStream.skip( received );
	
	bodyBuffer.write( buffer, 0, received );
	}
bufferedInputStream.mark( Integer.MAX_VALUE );
}
//--------------------------------------------------------------------------------
public boolean isKeepAliveResponse()
{
return isKeepAliveResponse( "Connection" );
}
//--------------------------------------------------------------------------------
public boolean isKeepAliveResponse( String connHeaderName )
{
if( version.equals( "HTTP/1.0" ) )
	{
	if( headerExists( connHeaderName ) 
	 && getHeaderValue( connHeaderName ).equalsIgnoreCase( "Keep-Alive" )
	 && headerExists( "Content-Length" )
	 )
		{
		return true;
		}
	else
		{
		return false;
		}
	}
else if( version.equals( "HTTP/1.1" ) )
	{
	if( headerExists( connHeaderName ) )
		{
		String connHeaderValue = getHeaderValue( connHeaderName );
		if( connHeaderValue.equalsIgnoreCase( "close" ) )
			{
			return false;
			}
		else if ( connHeaderValue.equalsIgnoreCase( "Keep-Alive" ) )
			{
			return true;
			}
		else
			{
			return false;
			}
		}
	else if( headerExists( "Content-Length" ) )
		{
		return true;
		}
	else if( headerExists( "Transfer-Encoding" )
	      && getHeaderValue( "Transfer-Encoding" ).equalsIgnoreCase( "chunked" )
	       )
	      	{
	      	return true;
	      	}
	else
		{
			//no header fields
		if( hasBody() )
			{
			return false;		
			}
		else
			{
			return true;
			}
		}
	}
else
	{
	return false;
	}
}
// --------------------------------------------------------------------------------
public void gzip()
throws IOException
{
if( isGZipped() )
	{
	return;
	}
if( !hasBody() )
	{
	return;
	}

chunkToNormal();

MBuffer buf = new MBuffer();
InputStream in = getBodyInputStream();
int bodySize = MStreamUtil.connectStream( in, new GZIPOutputStream( buf ), true );
setBodyBuffer( buf );
setHeaderValue( "Content-Encoding", "gzip" );
setHeaderValue( "Content-Length", buf.getSize() + "" );
}
// --------------------------------------------------------------------------------
public void setCachedBody( String s )
{
cachedBody = s;
}
// --------------------------------------------------------------------------------
public String getCachedBody()
{
return cachedBody;
}
// --------------------------------------------------------------------------------
public void clearCachedBody()
{
cachedBody = null;
}
// --------------------------------------------------------------------------------
public boolean hasCachedBody()
{
return ( cachedBody != null );
}
// --------------------------------------------------------------------------------
public void gunzip()
throws IOException
{
if( !isGZipped() )
	{
	return;
	}

chunkToNormal();

MBuffer newBuffer = new MBuffer();
InputStream in = new GZIPInputStream( getBodyInputStream() );
int bodySize = MStreamUtil.connectStream( in, newBuffer, true );
setBodyBuffer( newBuffer );
removeHeaderValue( "Content-Encoding" );
setHeaderValue( "Content-Length", bodySize + "" );
}
//--------------------------------------------------------------------------------
public Object clone()
{
try
	{
	ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
	bufferOut.write( this.getHeader() );
	if( this.hasBody() )
		{
		MStreamUtil.connectStream( this.getBodyInputStream(), bufferOut );
		}
	InputStream bufferIn = new ByteArrayInputStream( bufferOut.toByteArray() );
	MHttpResponse response = new MHttpResponse( new BufferedInputStream( bufferIn ) );
	bufferOut.close();
	bufferIn.close();
	return response;
	}
catch( IOException e )
	{
	e.printStackTrace();
	return null;
	}
}
//-------------------------------------------------------------------------------------------
public String getReasonPhrase() {
	return reasonPhrase;
}

public String getVersion() {
	return version;
}

public void setReasonPhrase(String string) {
	reasonPhrase = string;
}

public void setStatusCode(int i) {
	statusCode = i;
}

public void setVersion(String string) {
	version = string;
}

}
