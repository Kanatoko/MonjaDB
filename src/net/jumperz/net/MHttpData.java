package net.jumperz.net;

import java.io.*;
import java.util.*;
import java.net.*;
import net.jumperz.util.*;
import net.jumperz.net.exception.*;
import net.jumperz.io.*;

public abstract class MHttpData
{
protected static final int DEFAULT_HEADER_BUFSIZE	= 1024 * 1;
protected static final int DEFAULT_BODY_BUFSIZE		= 1024 * 4;
private static final int MAX_HEADER_COUNT		= 200;
private static final int CONN_CLOSE			= 0;
private static final int CONN_KEEPALIVE			= 1;
private static final int CONN_UNKNOWN			= 2;
protected static final byte[] CRLF			= { (byte)0x0D, (byte)0x0A };
protected static boolean strictDelimiter 		= true;

protected List headerList;
protected BufferedInputStream bufferedInputStream;
protected int headerLength = 0;
protected int pos;
protected MBuffer bodyBuffer;
protected boolean hasBodyFlag = false;
protected int headerBufSize = DEFAULT_HEADER_BUFSIZE;
protected int bodyBufSize = DEFAULT_BODY_BUFSIZE;

public abstract byte[] getHeader();
protected abstract void recvBodyUntilDisconnected() throws IOException;
//-------------------------------------------------------------------------------------------
public MHttpData()
{
headerList = new ArrayList();
}
// --------------------------------------------------------------------------------
public int getSize()
{
int size = headerLength;
if( hasBody() )
	{
	size += getBodySize();
	}
return size;
}
// --------------------------------------------------------------------------------
public boolean isGZipped()
{
String contentEncoding = this.getHeaderValue( "Content-Encoding" );
if( contentEncoding == null )
	{
	return false;
	}

if( contentEncoding.toLowerCase().indexOf( "gzip" ) == -1 )
	{
	return false;
	}

if( this.hasBody() )
	{
	return true;
	}
else
	{
	return false;
	}
}
//-------------------------------------------------------------------------------
public int getHeaderLength()
{
return headerLength;
}
// --------------------------------------------------------------------------------
public final String getHeaderAsString()
{
String s = "";
try
	{
	s = new String( getHeader(), MCharset.CS_ISO_8859_1 );
	}
catch( UnsupportedEncodingException ignored )
	{
	}
return s;
}
//-------------------------------------------------------------------------------------------
public final void addHeaderValue( String name, String value )
{
if( name != null && value != null )
	{
	headerList.add( name + ": " + value );
	}
}
// --------------------------------------------------------------------------------
public final void addHeader( String header )
{
if( header != null )
	{
	headerList.add( header );
	}
}
//-------------------------------------------------------------------------------------------
public synchronized final void setHeaderValue( String name, String value )
{
int index = -1;

	// get index
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		index = i;
		break;
		}
	}

if( index > -1 )
	{
	removeHeaderValue( name );
	headerList.add( index, name + ": " + value );
	}
else
	{
	headerList.add( name + ": " + value );
	}


/*
removeHeaderValue( name );
headerList.add( name + ": " + value );
*/
}
// --------------------------------------------------------------------------------
public static void setStrictDelimiter( boolean b )
{
strictDelimiter = b;
}
// --------------------------------------------------------------------------------
protected MLineReader getLineReader()
{
if( strictDelimiter )
	{
	return new MCRLFLineReader();
	}
else
	{
	return new MAdvancedLineReader();
	}
}
//-------------------------------------------------------------------------------------------
public final synchronized String getHeaderValue( String name )
{
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		String value = header.substring( name.length() + 1 ); // value = " www.jumperz.net";
		int pos = 0;
		while( value.length() > pos && value.charAt( pos ) == ' ' )
			{
			pos++;
			}
		return value.substring( pos );
		}
	}
return null;
//		String value = MRegEx.getMatch( "[ ]*(.*)$", header.substring( name.length() + 1 ) );
}
// --------------------------------------------------------------------------------
public final List getHeaderValueList( String name )
{
List l = new ArrayList();
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		String value = MRegEx.getMatch( "[ ]*(.*)$", header.substring( name.length() + 1 ) );
		l.add( value );
		}
	}

return l;
}
//-------------------------------------------------------------------------------------------
public synchronized final void removeHeaderValue( String name )
{
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		headerList.remove( i );
		--count;
		--i;
		}
	}
}
// --------------------------------------------------------------------------------
public void parseConnectionAsProxy()
{
/*
HTTP/1.1 proxies MUST parse the Connection header field before a message is forwarded and,
 for each connection-token in this field, remove any header field(s) from the message
  with the same name as the connection-token.
*/

if( !headerExists( "Connection" ) )
	{
	return;
	}

int connType = CONN_UNKNOWN;
String s = getHeaderValue( "Connection" );
String[] array = s.split( "[\\s,]{1,}" );
for( int i = 0; i < array.length; ++i )
	{
	if( array[ i ].equalsIgnoreCase( "close" ) )
		{
		connType = CONN_CLOSE;
		}
	else if( array[ i ].equalsIgnoreCase( "Keep-Alive" ) )
		{
		connType = CONN_KEEPALIVE;
		}	
	else
		{
			//for example, "TE" header field is removed
		removeHeaderValue( array[ i ] );
		}
	}

if( connType == CONN_CLOSE )
	{
	setHeaderValue( "Connection", "close" );
	}
else if( connType == CONN_KEEPALIVE )
	{
	setHeaderValue( "Connection", "Keep-Alive" );
	}
else if( connType == CONN_UNKNOWN )
	{
	removeHeaderValue( "Connection" );
	}
}
//-------------------------------------------------------------------------------------------
public final boolean headerExists( String name )
{
int count = headerList.size();
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	if( header.toUpperCase().indexOf( name.toUpperCase() + ":" ) == 0 )
		{
		return true;
		}
	}
return false;
}
//-------------------------------------------------------------------------------------------
protected final void readHeaderFields( MLineReader reader )
throws IOException
{
StringBuffer headerStrBuf = new StringBuffer( headerBufSize );
String line;

	// read all header fields
for( int i = 0;; ++i )
	{
	line = reader.readLine();
	if( line == null )
		{
		throw new IOException( "Invalid HTTP data" );
		}

	headerLength += line.length() + reader.getLastDelimiterSize();
	
	if( line.equals( "" ) )
		{
		bufferedInputStream.reset();
		bufferedInputStream.skip( headerLength );
		bufferedInputStream.mark( Integer.MAX_VALUE );
		break;
		}
	else
		{
		headerStrBuf.append( line );
		headerStrBuf.append( "\r\n" );
		}
	
		// for DoS attack 
	if( i > MHttpData.MAX_HEADER_COUNT )
		{
		throw new IOException( "Too many header fields." );
		}
	}

String headerStr = headerStrBuf.toString();
if( headerStr.equals( "" ) )
	{
		// no header fields
	return;
	}

String[] headerArray = headerStr.split( "\r\n" );

for( int i = 0; i < headerArray.length; ++i )
	{
	if( headerArray[ i ].indexOf( ":" ) == -1 )
		{
		//System.err.println( headerArray[ i ] );
		throw new IOException( "Invalid HTTP header ( multiple line header field is not allowed )" );
		}
	headerList.add( headerArray[ i ] );
	}

/* old code =)
headerStr = headerStr.replaceAll( "\r\n ", "\r " );
headerStr = headerStr.replaceAll( "\r\n\t", "\r\t" );
String[] headerArray = headerStr.split( "\r\n" );

for( int i = 0; i < headerArray.length; ++i )
	{
	String workStr	= headerArray[ i ].replaceAll( "\r\t" , "\r\n\t" );
	workStr		= workStr.replaceAll( "\r ", "\r\n " );
	if( workStr.indexOf( ":" ) == -1 )
		{
		System.err.println( workStr );
		throw new IOException( "Invalid HTTP data" );
		}
	headerList.add( workStr );
	}
*/
}
//-------------------------------------------------------------------------------------------
protected final void recvBody()
throws IOException
{
if( headerExists( "Transfer-Encoding" ) )
	{
	String transferEncoding = getHeaderValue( "Transfer-Encoding" );
	if( transferEncoding.equalsIgnoreCase( "chunked" ) )
		{
		recvChunkedBody();
		}
	else
		{
		throw new IOException( "Unsupported Transfer-Encoding :" + transferEncoding );
		}
	}
else if( headerExists( "Content-length" ) )
	{
	recvBodyByContentLength();
	}
else
	{
		// recv data until disconnected by the server
	recvBodyUntilDisconnected();
	}
bodyBuffer.close();
}
// --------------------------------------------------------------------------------
protected int getContentLength()
throws MHttpIOException
{
int contentLength = -1;
if( headerExists( "Content-Length" ) )
	{
	String value = getHeaderValue( "Content-Length" );
	value = value.trim(); // thanks anonymous!
	try
		{
		contentLength = Integer.parseInt( value );
		if( contentLength < 0 )
			{		
			throw new MHttpIOException( "Invalid Content-Length" );
			}
		}
	catch( NumberFormatException e )
		{
		throw new MHttpIOException( "Invalid Content-Length" );
		}
	}
return contentLength;
}
//-------------------------------------------------------------------------------------------
protected final void recvBodyByContentLength()
throws IOException
{
if( bodyBuffer == null )
	{
	bodyBuffer = new MBuffer();
	}
int received;
int remain = getContentLength();

int canRead	= bodyBufSize;
byte[] buffer	= new byte[ bodyBufSize ];

if( remain == 0 )
	{
	hasBodyFlag = false;
	return;
	}

while( remain > 0 )
	{
	if( remain < bodyBufSize )
		{
		canRead = remain;
		}

	bufferedInputStream.mark( canRead );

	received = bufferedInputStream.read( buffer, 0, canRead );
	if( received <= 0 )
		{
			// HEAD, etc
		break;
		}	
	remain -= received;

	bufferedInputStream.reset();
	bufferedInputStream.skip( received );

	bodyBuffer.write( buffer, 0, received );
	}
bufferedInputStream.mark( Integer.MAX_VALUE );
}
// --------------------------------------------------------------------------------
public int getBodySize()
{
if( !hasBody() || bodyBuffer == null )
	{
	return 0;
	}
return bodyBuffer.getSize();
}
// --------------------------------------------------------------------------------
public final void setContentLength()
throws IOException
{
setHeaderValue( "Content-Length", getBodyAsByte().length + "" );
}
// --------------------------------------------------------------------------------
public final void chunkToNormal()
throws IOException
{
if( !headerExists( "Transfer-Encoding" ) )
	{
	return;
	}
if( !getHeaderValue( "Transfer-Encoding" ).equals( "chunked" ) )
	{
	return;
	}
if( headerExists( "Content-Length" ) )
	{
	return;
	}
if( !hasBody() )
	{
	return;
	}

int contentLength = 0;
MBuffer dummyBuffer = new MBuffer();
MBuffer newBuffer = new MBuffer();
try
	{
	BufferedInputStream bis = new BufferedInputStream( getBodyInputStream() );
	
		// mark
	bis.mark( Integer.MAX_VALUE );
	
		// read chunk-size, chunk-extension (if any) and CRLF
	int chunkSize = readChunkSize( bis, dummyBuffer );
	
		// while (chunk-size > 0)
	while( chunkSize > 0 )
		{
			// read chunk-data
		int received;
		int remain	= chunkSize;
		int canRead	= bodyBufSize;
		byte[] buffer	= new byte[ bodyBufSize ];
	
		while( remain > 0 )
			{	
			if( remain < bodyBufSize )
				{
				canRead = remain;
				}
	
			received = bis.read( buffer, 0, canRead );
			if( received <= 0 )
				{
				throw new IOException( "Failed to read the chunked data" );
				}
			remain	-= received;
			
				// append chunk-data to entity-body
			newBuffer.write( buffer, received );
			contentLength += received;
			}
	
			// and CRLF
		readCRLF( bis, dummyBuffer );
	
			// reset, skip and mark
		bis.reset();
		bis.skip( chunkSize + 2 );
		bis.mark( Integer.MAX_VALUE );
		
			// read chunk-size and CRLF
		chunkSize = readChunkSize( bis, dummyBuffer );
		}
		
		// read entity-header
	String trailer = readTrailer( bis, dummyBuffer );
	
		// while (entity-header not empty)
	while( !trailer.equals( "" ) )
		{	
			// read entity-header
		trailer = readTrailer( bis, dummyBuffer );
		}
	}
finally
	{
	bodyBuffer.clear();
	dummyBuffer.clear();
	}
bodyBuffer = newBuffer;

removeHeaderValue( "Transfer-Encoding" );
setHeaderValue( "Content-Length", Integer.toString( contentLength ) );
}
//-------------------------------------------------------------------------------------------
protected final void recvChunkedBody()
throws IOException
{
	// http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.4.6

if( bodyBuffer == null )
	{
	bodyBuffer = new MBuffer();
	}
	
	// mark
bufferedInputStream.mark( Integer.MAX_VALUE );

	// read chunk-size, chunk-extension (if any) and CRLF
int chunkSize		= readChunkSize( bufferedInputStream, bodyBuffer );

	// while (chunk-size > 0)
while( chunkSize > 0 )
	{
		// read chunk-data
	int received;
	int remain	= chunkSize;
	int canRead	= bodyBufSize;
	byte[] buffer	= new byte[ bodyBufSize ];

	while( remain > 0 )
		{	
		if( remain < bodyBufSize )
			{
			canRead = remain;
			}

		received = bufferedInputStream.read( buffer, 0, canRead );
		if( received <= 0 )
			{
			throw new IOException( "Failed to read the chunked data" );
			}
		remain	-= received;
		
			// append chunk-data to entity-body
		bodyBuffer.write( buffer, 0, received );
		}

		// and CRLF
	readCRLF( bufferedInputStream, bodyBuffer );

		// reset, skip and mark
	bufferedInputStream.reset();
	bufferedInputStream.skip( chunkSize + 2 );
	bufferedInputStream.mark( Integer.MAX_VALUE );
	
		// read chunk-size and CRLF
	chunkSize		= readChunkSize( bufferedInputStream, bodyBuffer );
	}
	
	// read entity-header
String trailer = readTrailer( bufferedInputStream, bodyBuffer );

	// while (entity-header not empty)
while( !trailer.equals( "" ) )
	{	
		// read entity-header
	trailer = readTrailer( bufferedInputStream, bodyBuffer );
	}
}
//-------------------------------------------------------------------------------------------
/*
 * Caution: inputStream must reset, skipped and marked
 */
private final String readTrailer( BufferedInputStream inputStream, OutputStream bodyBuffer )
throws IOException
{
MLineReader reader = getLineReader();
reader.setInputStream( inputStream );

String trailer = reader.readLine();
if( trailer == null )
	{
	return "";
	}

byte[] buffer = trailer.getBytes( MCharset.CS_ISO_8859_1 ); 
bodyBuffer.write( buffer );
bodyBuffer.write( CRLF );

	// reset and skip
inputStream.reset();
inputStream.skip( trailer.length() + 2 );
inputStream.mark( Integer.MAX_VALUE );

return trailer;
}
//-------------------------------------------------------------------------------------------
/*
 * Caution: inputStream must reset, skipped and marked
 */
private final int readChunkSize( BufferedInputStream inputStream, OutputStream bodyBuffer )
throws IOException
{	
MLineReader reader = getLineReader();
reader.setInputStream( inputStream );

String line		= reader.readLine();
if( line == null )
	{
	return 0;
	}

byte[] buffer = line.getBytes( MCharset.CS_ISO_8859_1 );
bodyBuffer.write( buffer );
bodyBuffer.write( CRLF );
String chunkSizeStr	= MRegEx.getMatch( "^[0-9A-Fa-f]{1,}", line );

	// reset, skip and mark
inputStream.reset();
inputStream.skip( line.length() + 2 );
inputStream.mark( Integer.MAX_VALUE );

return Integer.parseInt( chunkSizeStr, 16 );
}
//-------------------------------------------------------------------------------------------
private final void readCRLF( InputStream inputStream, OutputStream bodyBuffer )
throws IOException
{
int CR = inputStream.read();
if( CR != 0x0D )
	{
	throw new IOException( "Invalid CR" );
	}
int LF = inputStream.read();
if( LF != 0x0A )
	{
	throw new IOException( "Invalid LF" );
	}
bodyBuffer.write( CRLF );
}
//-------------------------------------------------------------------------------------------
public final InputStream getBodyInputStream()
{
if( bodyBuffer == null )
	{
	bodyBuffer = new MBuffer();
	}
return bodyBuffer.getInputStream();
}
// --------------------------------------------------------------------------------
public final byte[] getBodyAsByte()
throws IOException
{
return MStreamUtil.streamToBytes( getBodyInputStream() );
}
// --------------------------------------------------------------------------------
public final String getBodyAsString()
throws IOException
{
return MStreamUtil.streamToString( getBodyInputStream() ); 
}
//-------------------------------------------------------------------------------------------
public final synchronized List getHeaderFieldList()
{
int count = headerList.size();
ArrayList headerFieldList = new ArrayList( count );
for( int i = 0; i < count; ++i )
	{
	String header = ( String )headerList.get( i );
	headerFieldList.add( MRegEx.getMatch( "([^:]+):", header ) );
	}
return headerFieldList;
}
//-------------------------------------------------------------------------------------------
public final List getHeaderList()
{
return new ArrayList( headerList );
}
//--------------------------------------------------------------------------------
public final void setHeaderList( List list )
{
headerList = list;
}
//-------------------------------------------------------------------------------
public final boolean hasBody()
{
return hasBodyFlag;
}
//-------------------------------------------------------------------------------------------
public final void clear()
{
if( bodyBuffer != null )
	{
	bodyBuffer.clear();
	}
}
// --------------------------------------------------------------------------------
public final void setBodyBuffer( MBuffer newBuffer )
throws IOException
{
clear();

bodyBuffer = newBuffer;
hasBodyFlag = true;
}
//--------------------------------------------------------------------------------
public final void setBody( byte[] data )
throws IOException
{
clear();

bodyBuffer = new MBuffer();
bodyBuffer.write( data );
hasBodyFlag = true;
}
// --------------------------------------------------------------------------------
public final void setBody( String str, String charset )
throws IOException
{
setBody( str.getBytes( charset ) );
}
//--------------------------------------------------------------------------------
public final void setBody( String str )
throws IOException
{
setBody( str, MCharset.CS_ISO_8859_1 );
}
// --------------------------------------------------------------------------------
public byte[] toByteArray()
{
ByteArrayOutputStream baos = toByteArrayOutputStream();
return baos.toByteArray();
}
// --------------------------------------------------------------------------------
private ByteArrayOutputStream toByteArrayOutputStream()
{
ByteArrayOutputStream baos = new ByteArrayOutputStream( 512 );
try
	{
	baos.write( getHeader() );
	if( hasBody() )
		{
		MStreamUtil.connectStream( getBodyInputStream(), baos );
		}
	}
catch( IOException e )
	{
	e.printStackTrace();
	}
return baos;
}
//--------------------------------------------------------------------------------
public String toString( String enc )
{
ByteArrayOutputStream baos = toByteArrayOutputStream();
String s = null;
try
	{
	s = baos.toString( enc );
	}
catch( UnsupportedEncodingException e )
	{
	e.printStackTrace();
	}
return s;
}
// --------------------------------------------------------------------------------
public String toString()
{
return toString( MCharset.CS_ISO_8859_1 );
}
//--------------------------------------------------------------------------------
public int getBodyBufSize()
{
return bodyBufSize;
}
//--------------------------------------------------------------------------------
public void setBodyBufSize( int bodyBufSize )
{
this.bodyBufSize = bodyBufSize;
}
//--------------------------------------------------------------------------------
public int getHeaderBufSize()
{
return headerBufSize;
}
//--------------------------------------------------------------------------------
public void setHeaderBufSize( int headerBufSize )
{
this.headerBufSize = headerBufSize;
}
//--------------------------------------------------------------------------------
}
