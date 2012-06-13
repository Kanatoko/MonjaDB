package net.jumperz.net.jaxer;

import net.jumperz.net.*;
import net.jumperz.util.*;
import net.jumperz.io.*;

import java.io.*;
import java.util.*;
import java.net.*;

public class MJaxerFilter
{
private Socket socket;
private MHttpRequest request;
private MHttpResponse response;
private MHttpResponse jaxerResponse = new MHttpResponse();
private OutputStream out;
private InputStream in;
private Map env = new HashMap();

private static final byte[] expectedBeginRequest = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x04, (byte)0x01 };
private static final byte[] EndRequest = new byte[]{ (byte)0x07, (byte)0x00, (byte)0x00 };
//--------------------------------------------------------------------------------
public MJaxerFilter( Socket s, MHttpRequest req, MHttpResponse res, Map e )
{
this.socket	= s;
this.request	= req;
this.response	= res;
initEnv( e );
}
//--------------------------------------------------------------------------------
public void close()
{
MSystemUtil.closeSocket( socket );
}
//--------------------------------------------------------------------------------
private void initEnv( Map e )
{
env.put( "HTTPS", "off" );
env.put( "JAXER_REQ_TYPE", "2" );
env.put( "HTTP_HOST", "localhost" );		// localhost:8081
env.put( "PATH", "/" );				// /usr/local/sbin:
env.put( "SERVER_SIGNATURE", "dummy" );
env.put( "SERVER_SOFTWARE", "dummy" );
env.put( "SERVER_NAME", "dummy" );
env.put( "SERVER_ADDR", "10.0.0.1" );
env.put( "SERVER_PORT", "1234" );
env.put( "REMOTE_ADDR", "10.0.0.1" );
env.put( "DOCUMENT_ROOT", "/" );		// /opt/AptanaJaxer/public
env.put( "SERVER_ADMIN", "dummy" );
env.put( "SCRIPT_FILENAME", "dummy" );		// /opt/AptanaJaxer/jaxer/aptana/tools/test/hoge.html
env.put( "REMOTE_PORT", "1234" );
env.put( "SERVER_PROTOCOL", "HTTP/1.1" );
env.put( "REQUEST_METHOD", "GET" );
env.put( "QUERY_STRING", "/" );
env.put( "REQUEST_URI", "/" );			// /aptana/tools/test/hoge.html?a=b
env.put( "SCRIPT_NAME", "/dummy" );		// /aptana/tools/test/hoge.html
env.put( "REMOTE_HOST", "dummy" );		// 127.0.0.1 client ip
env.put( "STATUS_CODE", "200" );

env.putAll( e );
}
//--------------------------------------------------------------------------------
public boolean isError()
throws IOException
{
int bufsize = 512;
String bodyString = null;
int bodySize = jaxerResponse.getBodySize();
if( bodySize > bufsize )
	{
	byte[] buffer = new byte[ bufsize ];
	InputStream in = null;
	try
		{
		in = jaxerResponse.getBodyInputStream();
		MStreamUtil.read( in, buffer );
		bodyString = new String( buffer, MCharset.CS_ISO_8859_1 );	
		}
	finally
		{
		MStreamUtil.closeStream( in );
		}
	}
else
	{
	bodyString = jaxerResponse.getBodyAsString();
	}
return bodyString.indexOf( "<title>Error Processing Your Request</title>" ) > -1;
}
//--------------------------------------------------------------------------------
private void impl()
throws IOException
{
out = socket.getOutputStream();
in = socket.getInputStream();

	//send BeginRequest
out.write( new byte[]{ (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x04, (byte)0x02 } );

	//recv BeginRequest
byte[] handlerBeginRequest = new byte[ 6 ];
int recv = in.read( handlerBeginRequest );
if( recv != handlerBeginRequest.length || !Arrays.equals( handlerBeginRequest, expectedBeginRequest ) )
	{
	throw new IOException( "Invalid Handler BeginRequest:" + recv );
	}

	//send RequestHeader Block
sendHttpHeader( request, 0x01 );

	//send ResponseHeader Block
sendHttpHeader( response, 0x02 );

	//send Environment Block
sendEnv();

	//send Response Body Block
sendResponseBody();

	//send End block
sendEndBlock();

	//recv Response Header Block
recvResponseHeader();

	//recv End block
recvResponseBody();

//System.out.println( MStringUtil.byteToHexString( handlerBeginRequest ) );
}
//--------------------------------------------------------------------------------
public MHttpResponse getJaxerResponse()
throws IOException
{
try
	{
	impl();
	return jaxerResponse;
	}
finally
	{
	MSystemUtil.closeSocket( socket );
	}
}
//--------------------------------------------------------------------------------
private void recvResponseBody()
throws IOException
{
MBuffer mbuf = new MBuffer();

while( true )
	{
	byte[] buffer = new byte[ 3 ];
	MStreamUtil.read( in, buffer );
	
	if( buffer[ 0 ] == (byte)0x04 )
		{
			//document
		int length = MStringUtil.byteArrayToUnsignedInt( new byte[]{ buffer[ 1 ], buffer[ 2 ] } );
		log( "document received:" + length );
		buffer = new byte[ length ];
		MStreamUtil.read( in, buffer );
		mbuf.write( buffer );
		}
	else if( buffer[ 0 ] == (byte)0x07 )
		{
			//end block
		log( MStringUtil.byteToHexString( buffer ) );
		break;
		}
	}

jaxerResponse.setBodyBuffer( mbuf );
jaxerResponse.setContentLength();
}
//--------------------------------------------------------------------------------
private void recvResponseHeader()
throws IOException
{
byte[] buffer = new byte[ 3 ];
MStreamUtil.read( in, buffer );

if( buffer[ 0 ] != ( byte ) 0x02 )
	{
	throw new IOException( "Recv Response Header Error : " + buffer[ 0 ] );	
	}

int headerSize = MStringUtil.byteArrayToUnsignedInt( new byte[]{ buffer[ 1 ], buffer[ 2 ] } );

buffer = new byte[ headerSize ];
MStreamUtil.read( in, buffer );

int headerCount = MStringUtil.byteArrayToUnsignedInt( new byte[]{ buffer[ 0 ], buffer[ 1 ] } );
log( "headerCount:" + headerCount );

ByteArrayInputStream bufIn = new ByteArrayInputStream( buffer );
bufIn.read();
bufIn.read();

for( int i = 0; i < headerCount; ++ i )
	{
	String name  = readHeader( bufIn );
	String value = readHeader( bufIn );
	
	if( name.equals( "status" ) )
		{
		jaxerResponse.setStatusLine( "HTTP/1.1 " + value );
		}
	else
		{
		jaxerResponse.setHeaderValue( name, value );
		}
	}
}
//--------------------------------------------------------------------------------
private String readHeader( InputStream bufIn  )
throws IOException
{
byte[] sizeBuf = new byte[ 2 ];
MStreamUtil.read( bufIn, sizeBuf );
int length = MStringUtil.byteArrayToUnsignedInt( sizeBuf );

byte[] buffer = new byte[ length ];
MStreamUtil.read( bufIn, buffer );
return new String( buffer, MCharset.CS_ISO_8859_1 );
}
//--------------------------------------------------------------------------------
private void sendEndBlock()
throws IOException
{
out.write( EndRequest );
}
//--------------------------------------------------------------------------------
private void sendResponseBody()
throws IOException
{
response.chunkToNormal();
String bodyStr = response.getBodyAsString();
while( true )
	{
	if( bodyStr.length() > 0xffff )
		{
		sendDocument( MStringUtil.getSubstring( bodyStr, 0xffff ) );
		bodyStr = bodyStr.substring( 0xffff );
		}
	else
		{
		sendDocument( bodyStr );
		break;
		}
	}
}
//--------------------------------------------------------------------------------
private void sendDocument( String doc )
throws IOException
{
out.write( ( byte ) 0x04 );//document
out.write( MSystemUtil.get16B( doc.length() ) );
out.write( doc.getBytes( MCharset.CS_ISO_8859_1 ) );
}
//--------------------------------------------------------------------------------
private void sendEnv()
throws IOException
{
ByteArrayOutputStream buffer = new ByteArrayOutputStream();
Iterator p = env.keySet().iterator();
int headerCount = 0;
while( p.hasNext() )
	{
	String envName = ( String )p.next();
	String envValue = ( String )env.get( envName );
	
		//check size
	if( envName.length() > 0xfff0
	 || envValue.length() > 0xfff0
	  )
		{
		continue;
		}
	 
	buffer.write( MSystemUtil.get16B( envName.length() ) );
	buffer.write( envName.getBytes( MCharset.CS_ISO_8859_1 ) );
	buffer.write( MSystemUtil.get16B( envValue.length() ) );
	buffer.write( envValue.getBytes( MCharset.CS_ISO_8859_1 ) );
	
	++ headerCount;
	}

if( buffer.size() > 0xfff0 )
	{
	throw new IOException( "ENV too long : " + buffer.size() );
	}

out.write( (byte) 0x03 ); //environ = 0x03
out.write( MSystemUtil.get16B( buffer.size() + 2 ) );
out.write( MSystemUtil.get16B( headerCount )  );
out.write( buffer.toByteArray() );
}
//--------------------------------------------------------------------------------
private void sendHttpHeader( MHttpData httpData, int type )
throws IOException
{
ByteArrayOutputStream buffer = new ByteArrayOutputStream();
List headerList = httpData.getHeaderList();
int headerCount = 0;
for( int headerIndex = 0; headerIndex < headerList.size(); ++headerIndex )
	{
	String header = ( String )headerList.get( headerIndex );
	String headerName = MRegEx.getMatch( "^([^:]{1,})", header );
	String headerValue = MRegEx.getMatch( "^[^:]+:\\s*(.*)$", header );
	
		//check size
	if( headerName.length() > 0xfff0
	 || headerValue.length() > 0xfff0
	  )
		{
		continue;
		}
	 
	buffer.write( MSystemUtil.get16B( headerName.length() ) );
	buffer.write( headerName.getBytes( MCharset.CS_ISO_8859_1 ) );
	buffer.write( MSystemUtil.get16B( headerValue.length() ) );
	buffer.write( headerValue.getBytes( MCharset.CS_ISO_8859_1 ) );
	
	++ headerCount;
	}

if( buffer.size() > 0xfff0 )
	{
	throw new IOException( "HTTP Header too long : " + buffer.size() );
	}

out.write( (byte)type );
out.write( MSystemUtil.get16B( buffer.size() + 2 ) );
out.write( MSystemUtil.get16B( headerCount )  );
out.write( buffer.toByteArray() );
}
//--------------------------------------------------------------------------------
private void log( Object o )
{
//System.out.println( o );
}
//--------------------------------------------------------------------------------
}