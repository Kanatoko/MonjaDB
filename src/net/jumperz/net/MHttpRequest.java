package net.jumperz.net;

import java.io.*;
import java.net.*;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import net.jumperz.io.*;
import net.jumperz.util.*;
import net.jumperz.net.exception.*;

public final class MHttpRequest
extends MHttpData
implements Cloneable
{
private int state;
private int methodType;
private String method;
private String uri;
private String version;
//private String requestLine;
private String addr = "";
private int port;
private boolean isMultipart = false;

	//parameters
private List uriParameterList;
private List bodyParameterList;
private List multipartParameterList;
private List param2List;

	//cookies
private List cookieList;

public static final int GET	=  0;
public static final int POST	=  1;
public static final int HEAD	=  2;
public static final int PUT	=  3;
public static final int DELETE	=  4;
public static final int TRACE	=  5;
public static final int CONNECT	=  6;
public static final int OPTIONS	=  7;

public static final int OTHER	= -1;

public static final int RECV_REQUEST_HEADER	= 0;
public static final int RECV_REQUEST_BODY	= 1;
public static final int REQUEST_COMPLETE	= 2;
public static final int SESSIONTYPE_QUERY	= 0;
public static final int SESSIONTYPE_PARAMETER	= 1;
public static final int SESSIONTYPE_COOKIE	= 2;

public static final int DEFAULT_METHOD_TYPE	= GET;
public static final String DEFAULT_METHOD	= "GET";
public static final String DEFAULT_URI		= "/";
public static final String DEFAULT_VERSION	= "HTTP/1.0";
//public static final String DEFAULT_REQUEST_LINE	= "GET / HTTP/1.0";

public static boolean strictRequestLine = true;

//--------------------------------------------------------------------------------
public MHttpRequest()
{
methodType	= DEFAULT_METHOD_TYPE;
method		= DEFAULT_METHOD;
uri		= DEFAULT_URI;
version		= DEFAULT_VERSION;
}
//--------------------------------------------------------------------------------
public MHttpRequest( String s )
throws IOException
{
BufferedInputStream i = new BufferedInputStream( new ByteArrayInputStream( s.getBytes( MCharset.CS_ISO_8859_1 ) ) );
init( i );
}
// --------------------------------------------------------------------------------
public MHttpRequest( byte[] buffer )
throws IOException
{
BufferedInputStream i = new BufferedInputStream( new ByteArrayInputStream( buffer ) );
init( i );
}
//--------------------------------------------------------------------------------
public MHttpRequest( BufferedInputStream in_bufferedInputStream )
throws IOException
{
init( in_bufferedInputStream );
}
//-------------------------------------------------------------------------------
public void init( BufferedInputStream in_bufferedInputStream )
throws IOException
{
state = MHttpRequest.RECV_REQUEST_HEADER;
bufferedInputStream = in_bufferedInputStream;
recvHeader();
setHasBodyFlag();

if( hasBodyFlag )
	{
	state = MHttpRequest.RECV_REQUEST_BODY;
	recvBody();
	}
parseParameters();
parseCookies();
state = MHttpRequest.REQUEST_COMPLETE;
}
// --------------------------------------------------------------------------------
public String getSessionId( int sessionType, String sessionIdName )
throws IOException
{
if( sessionType == SESSIONTYPE_QUERY )
	{
	boolean found = false;
	String sessionId = null;
	for( int i = 0; i < uriParameterList.size(); ++i )
		{
		MAbstractParameter parameter = ( MAbstractParameter )uriParameterList.get( i );
		if( parameter.getName().equals( sessionIdName ) )
			{
			if( found )
				{
				throw new IOException( "Two or more session id found." );		
				}
			else
				{
				found = true;
				sessionId = parameter.getValue();
				}
			}
		}
	return sessionId;
	}
else if( sessionType == SESSIONTYPE_PARAMETER )
	{
	MRequestUri ru = new MRequestUri( uri );
	String params = ru.getParams();
	if( params.indexOf( sessionIdName ) == 0 )
		{
		return params.substring( sessionIdName.length() + 1 );
		}
	}
else if( sessionType == SESSIONTYPE_COOKIE )
	{
	if( cookieExists( sessionIdName ) )
		{
		return getCookieValue( sessionIdName );		
		}
	}
return null;
}
// --------------------------------------------------------------------------------
public boolean cookieExists( String name )
{
if( cookieList != null )
	{
	for( int i = 0; i < cookieList.size(); ++i )
		{
		MParameter param = ( MParameter )cookieList.get( i );
		if( param.getName().equals( name ) )
			{
			return true;
			}
		}
	}
return false;
}
// --------------------------------------------------------------------------------
public void parseCookies()
{
cookieList = new ArrayList();

if( !headerExists( "Cookie" ) )
	{
	return;
	}

List l = getHeaderValueList( "Cookie" );
for( int k = 0; k < l.size(); ++k )
	{
	String cookie = l.get( k ) + "";
	String[] pairList = cookie.split( "[ ]*;[ ]*" );
	for( int i = 0; i < pairList.length; ++i )
		{
		String name = "";
		String value = "";
		int index = pairList[ i ].indexOf( '=' );
		if( index > 0 )
			{
			name  = pairList[ i ].substring( 0, index );
			value = pairList[ i ].substring( index + 1 );
			}
		else	// Cookie: FOO;BAR;
			{
			name = pairList[ i ];
			}
		MParameter param = new MParameter( name, value, MAbstractParameter.COOKIE );
		cookieList.add( param );
		}	
	}
}
// --------------------------------------------------------------------------------
public void applyCookie()
{
if( cookieList != null )
	{
	StringBuffer buf = new StringBuffer( 256 );
	for( int i = 0; i < cookieList.size(); ++i )
		{
		MAbstractParameter param = ( MAbstractParameter )cookieList.get( i );
		if( buf.length() > 0 )
			{
			buf.append( "; " );
			}
		buf.append( param.getName() );
		buf.append( "=" );
		buf.append( param.getValue() );
		}
	setHeaderValue( "Cookie", buf.toString() );
	}
}
// --------------------------------------------------------------------------------
private void parseParametersImpl2( String s, int type )
{
String[] pairList = s.split( "&" );
for( int i = 0; i < pairList.length; ++i )
	{
	int index = pairList[ i ].indexOf( '=' );
	if( index > 0 )
		{
		String name  = pairList[ i ].substring( 0, index );
		String value = "";
		try
			{
			//value = MStringUtil.urlDecode( pairList[ i ].substring( index + 1 ) );		
			//value = MStringUtil.fastUrlDecode( pairList[ i ].substring( index + 1 ) );		
			value = pairList[ i ].substring( index + 1 );		
			}
		catch( IllegalArgumentException ignored )
			{
			}
		MParameter parameter = new MParameter( name, value, type );
		if( type == MParameter.URI )
			{
			uriParameterList.add( parameter );		
			}
		else if( type == MParameter.BODY )
			{
			bodyParameterList.add( parameter );
			}
		}
	}
}
// --------------------------------------------------------------------------------
private void parseParam2()
{
	//param2
param2List = new ArrayList();
MRequestUri ruri = new MRequestUri( uri );
String params = ruri.getParams();
if( params.length() > 0 )
	{
	String[] array = params.split( ";" );
	for( int i = 0; i < array.length; ++i )
		{
		String eachParam2 = array[ i ]; // ex. jsessionid=12345
		if( eachParam2.indexOf( '=' ) > 0 )
			{
			String[] array2 = eachParam2.split( "=" );
			if( array2.length == 2 )
				{
				MAbstractParameter param = new MParameter( array2[ 0 ], array2[ 1 ], MAbstractParameter.PARAM );
				param2List.add( param );
				}
			}
		}
	}
}
// --------------------------------------------------------------------------------
public void parseParameters()
{
uriParameterList	= new ArrayList();
bodyParameterList	= new ArrayList();
multipartParameterList	= new ArrayList();

	//QueryString
parseParametersImpl2( new MRequestUri( uri ).getQuery(), MAbstractParameter.URI );

parseParam2();

	//Body
if( hasBody() )
	{
	if( headerExists( "Content-Type" ) )
		{
		String contentType = getHeaderValue( "Content-Type" );
		if( contentType.toLowerCase().indexOf( "application/x-www-form-urlencoded" ) > -1 )
			{
			try
				{
				String bodyStr = MStreamUtil.streamToString( getBodyInputStream() );
				parseParametersImpl2( bodyStr, MAbstractParameter.BODY );
				}
			catch( IOException e )
				{
				e.printStackTrace();
				}		
			}
		else if( contentType.indexOf( "multipart/form-data" ) > -1 )
			{
			String boundary = MRegEx.getMatch( "boundary=(.*)", contentType );
			try
				{
				parseParameters3( boundary );
				}
			catch( IOException e )
				{
				e.printStackTrace();
				}
			}
		}
	}
}
// --------------------------------------------------------------------------------
private void parseParameters3( String boundary )
throws IOException
{
StringBuffer buf = new StringBuffer( 8192 );
buf.append( "\r\n" );
buf.append( MStreamUtil.streamToString( getBodyInputStream() ) );
String bodyStr = buf.toString();
int index =  bodyStr.indexOf( "\r\n--" + boundary + "--" );
if( index == -1 )
	{
	throw new IOException( "Invalid format. Last boundary not found." );
	}

bodyStr = bodyStr.substring( 0, index );

List array = MStringUtil.split( bodyStr, "\r\n--" + boundary + "\r\n" );
for( int i = 0; i < array.size(); ++i )
	{
	String _s = ( String )array.get( i );
	if( _s.length() == 0 )
		{
		continue;
		}
	MMultipartParameter parameter = new MMultipartParameter( _s );
	multipartParameterList.add( parameter );
	}
isMultipart = true;
}
//-------------------------------------------------------------------------------
private void recvHeader()
throws IOException
{
bufferedInputStream.mark( Integer.MAX_VALUE );
bufferedInputStream.reset();

MLineReader reader = getLineReader();
reader.setInputStream( bufferedInputStream );

	//1st line of HTTP request header
String line = reader.readLine();

if( line == null || line.equals( "" ) )
	{
	throw new MHttpStreamClosedException( "Stream is closed." );
	}

if( strictRequestLine )
	{
	splitRequestLine( line );
	}
else
	{
	splitRequestLine2( line );
	}
headerLength += line.length() + reader.getLastDelimiterSize();

readHeaderFields( reader );
}
// --------------------------------------------------------------------------------
private void splitRequestLine2( String line )
throws IOException
{
int index1 = line.indexOf( ' ' );
int index2 = line.lastIndexOf( "HTTP/" );
setMethod( line.substring( 0, index1 ) );
version = line.substring( index2 );
uri = line.substring( index1 + 1, index2 - 1 );
}
//--------------------------------------------------------------------------------
private void splitRequestLine( String line )
throws IOException
{
	//block "GET / HTTP/1.0 "
if( line.charAt( line.length() -1 ) == ' ' )
	{
	throw new MHttpIOException( "Invalid request line:" + line );
	}

String[] requestLineArray = line.split( " " );
if( requestLineArray.length != 3 )
	{
	throw new MHttpIOException( "Invalid request line:" + line );
	}

	//check method
String method = requestLineArray[ 0 ];
if( !method.matches( "^[a-zA-Z]+$" ) )
	{
	throw new MHttpIOException( "Invalid request line:" + line );
	}

setMethod( method );
uri	= requestLineArray[ 1 ];
version	= requestLineArray[ 2 ];
}
//--------------------------------------------------------------------------------
public static void testHasValidBodyState()
throws Exception
{
MHttpRequest request = new MHttpRequest( "POST /index.php HTTP/1.1\r\n"
 + "Host: www.jumperz.net\r\n"
 + "User-Agent: Mozilla/5.0 (X11; U; Linux x86_64; ja; rv:1.9.0.13) Gecko/2009080315 Ubuntu/9.04 (jaunty) Firefox/3.0.13\r\n"
 + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n"
 + "Accept-Language: ja,en-us;q=0.7,en;q=0.3\r\n"
 + "Accept-Charset: Shift_JIS,utf-8;q=0.7,*;q=0.7\r\n"
 + "Keep-Alive: 300\r\n"
 + "Referer: http://www.jumperz.net/index.php?i=4\r\n"
 + "Cookie: JSESSIONID=FD4263B711FB3152E7C0193D627DBD5C\r\n"
 + "Content-Type: application/x-www-form-urlencoded\r\n"
 + "Content-Length: 31\r\n"
 + "Connection: keep-alive\r\n"
 + "\r\n" );

if( request.isInvalidPostState() != true ) { throw new Exception(); }

request.setBody("i=5&bazz=%82%A0&title=&message=" );

if( request.isInvalidPostState() != false ) { throw new Exception(); }

request = new MHttpRequest();
if( request.isInvalidPostState() != false ) { throw new Exception(); }
}
//--------------------------------------------------------------------------------
public static void main( String[] args )
throws Exception
{
testHasValidBodyState();
System.out.println( "OK." );
}
//--------------------------------------------------------------------------------
public boolean isInvalidPostState()
throws MHttpIOException
{
	// return true only when POST and has Content-Length header and invalid size body
if( getMethodType() == POST )
	{
	int contentLength = getContentLength();
	if( contentLength >= 0 )
		{
		if( getBodySize() != contentLength )
			{
			return true;
			}
		}
	}
return false;
}
//-------------------------------------------------------------------------------
public int getMethodType()
{
return methodType;
}
//-------------------------------------------------------------------------------
public byte[] getHeader()
{
ByteArrayOutputStream bufferStream = null;
try
	{
		// request line
	bufferStream = new ByteArrayOutputStream( headerBufSize );
	bufferStream.write( method.getBytes( MCharset.CS_ISO_8859_1 ) );
	bufferStream.write( (byte)0x20 );
	bufferStream.write( uri.getBytes( MCharset.CS_ISO_8859_1 ) );
	bufferStream.write( (byte)0x20 );
	bufferStream.write( version.getBytes( MCharset.CS_ISO_8859_1 ) );
	bufferStream.write( CRLF );
	
		// fields
	int count = headerList.size();
	for( int i = 0; i < count; ++i )
		{
		bufferStream.write( ( ( String )headerList.get( i ) ).getBytes( MCharset.CS_ISO_8859_1 ) );
		bufferStream.write( CRLF );
		}
	
		// blank line
	bufferStream.write( CRLF );
	}
catch( IOException e )
	{
	//MLogger.getInstance().Log( e.toString() );
	e.printStackTrace();
	}
return bufferStream.toByteArray();
}
//-------------------------------------------------------------------------------
public String getUri()
{
return uri;
}
//-------------------------------------------------------------------------------
public void setVersion( String in_version )
{
version = in_version;
}
//-------------------------------------------------------------------------------
public String getVersion()
{
return version;
}
//-------------------------------------------------------------------------------
public void setUri( String uri )
{
this.uri = uri;
uriParameterList = new ArrayList();
parseParametersImpl2( new MRequestUri( uri ).getQuery(), MAbstractParameter.URI );
parseParam2();
}
//-------------------------------------------------------------------------------
public void setMethod( String in_method )
{
method = in_method;

if( method.equals( "GET" ) )
	{
	methodType = GET;
	}
else if( method.equals( "POST" ) )
	{
	methodType = POST;
	}
else if( method.equals( "HEAD" ) )
	{
	methodType = HEAD;
	}
else if( method.equals( "PUT" ) )
	{
	methodType = PUT;
	}
else if( method.equals( "DELETE" ) )
	{
	methodType = DELETE;
	}
else if( method.equals( "TRACE" ) )
	{
	methodType = TRACE;
	}
else if( method.equals( "CONNECT" ) )
	{
	methodType = CONNECT;
	}
else if( method.equals( "OPTIONS" ) )
	{
	methodType = OPTIONS;
	}

else
	{
	methodType = OTHER;
	}
}
// --------------------------------------------------------------------------------
private void setHasBodyFlag()
throws MHttpIOException
{
if( headerExists( "Transfer-Encoding" ) )
	{
	hasBodyFlag = true;
	return;
	}
int contentLength = getContentLength();
hasBodyFlag = ( contentLength > 0 );
}
//-------------------------------------------------------------------------------
public String getRequestLine()
{
StringBuffer strBuf = new StringBuffer( 100 );
strBuf.append( method );
strBuf.append( " " );
strBuf.append( uri );
strBuf.append( " " );
strBuf.append( version );
return strBuf.toString();
}
// --------------------------------------------------------------------------------
public boolean isKeepAliveRequest( String fieldName )
{
boolean keepAlive;
if( version.equals( "HTTP/1.1" ) )
	{
	keepAlive = true;
	
	if( headerExists( fieldName )
	 && getHeaderValue( fieldName ).equalsIgnoreCase( "close" )
	  )
		{
		keepAlive = false;
		}
	}
else
	{
	keepAlive = false;

	if( headerExists( fieldName )
	 && getHeaderValue( fieldName ).equalsIgnoreCase( "Keep-Alive" )
	  )
		{
		keepAlive = true;
		}	
	}
return keepAlive;
}
//--------------------------------------------------------------------------------
public boolean isKeepAliveRequest()
{
return isKeepAliveRequest( "Connection" );
}
//-------------------------------------------------------------------------------
public int getState()
{
return state;
}
//-------------------------------------------------------------------------------
public String getAddr()
{
return addr;
}
//-------------------------------------------------------------------------------
public int getPort()
{
return port;
}
//-------------------------------------------------------------------------------
public void setPort( int in_port )
{
port = in_port;
}
//-------------------------------------------------------------------------------
public void setAddr( String in_addr )
{
addr = in_addr;
}
//--------------------------------------------------------------------------------
public String getMethod()
{
return method;
}
//-------------------------------------------------------------------------------
protected void recvBodyUntilDisconnected()
throws IOException
{
throw new MHttpException( 411, "Length Required" );
}
// --------------------------------------------------------------------------------
public List getCookieList()
{
if( cookieList != null )
	{
	return cookieList;
	}
else
	{
	return new ArrayList();
	}
}
// --------------------------------------------------------------------------------
/*
public boolean hasParameter( String name )
{
return parameterNameList.contains( name );
}*/
// --------------------------------------------------------------------------------
public String getCookieValue( String name )
{
if( cookieList != null )
	{
	for( int i = 0; i < cookieList.size(); ++i )
		{
		MParameter param = ( MParameter )cookieList.get( i );
		if( param.getName().equals( name ) )
			{
			return param.getValue();
			}
		}
	}
return "";
}
// --------------------------------------------------------------------------------
public List getParameterList( int type )
{
if( type == MAbstractParameter.URI ) 
	{
	return uriParameterList;
	}
else if( type == MAbstractParameter.BODY )
	{
	return bodyParameterList;
	}
else if( type == MAbstractParameter.MULTIPART )
	{
	return multipartParameterList;
	}
return new ArrayList();
}
// --------------------------------------------------------------------------------
public List getParameterList()
{
List l = new ArrayList();
l.addAll( uriParameterList );
l.addAll( bodyParameterList );
l.addAll( multipartParameterList );
return l;
}
/*
// --------------------------------------------------------------------------------
public List getParameterNameList()
{
return parameterNameList;
}*/
/*
// --------------------------------------------------------------------------------
public void setParameter( MAbstractParameter param, int index )
{
String name = param.getName();
if( parameterNameSet1.contains( name ) )
	{
	parameterMap1.put( name, param );
	}
else if( parameterNameSet2.contains( name ) )
	{
	List l = ( List )parameterMap2.get( name );
	for( int i = 0; i < l.size(); ++i )
		{
		if( i == index )
			{
				//replace old param
			Object oldParam = l.get( index );
			l.add( index, param );
			l.remove( oldParam );
			}
		}
	}
else
	{
	addParameter( param );
	}
}
// --------------------------------------------------------------------------------
public void setParameter( MAbstractParameter param )
{
setParameter( param, 0 );
}
*/
/*
// --------------------------------------------------------------------------------
public List getParameterList( String name )
{
if( parameterNameSet1.contains( name ) )
	{
	List l = new ArrayList();
	l.add( parameterMap1.get( name ) );
	return l;
	}
else if( parameterNameSet2.contains( name ) )
	{
	return ( List )parameterMap2.get( name );
	}
else
	{
	return new ArrayList();
	}
}
// --------------------------------------------------------------------------------
public MAbstractParameter getParameter( String name )
{
if( parameterNameSet1.contains( name ) )
	{
	return ( MAbstractParameter )parameterMap1.get( name );
	}
else if( parameterNameSet2.contains( name ) )
	{
	return ( MAbstractParameter )( ( List )parameterMap2.get( name ) ).get( 0 );
	}
else
	{
	return new MParameter( name, "", MAbstractParameter.URI );
	}
}
*/
//--------------------------------------------------------------------------------
public Object clone()
throws CloneNotSupportedException
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
	MHttpRequest request = new MHttpRequest( new BufferedInputStream( bufferIn ) );
	bufferOut.close();
	bufferIn.close();
	return request;
	}
catch( IOException e )
	{
	e.printStackTrace();
	return null;
	}
}
// --------------------------------------------------------------------------------
public String refreshBody()
{
return null;
}
// --------------------------------------------------------------------------------
public String getParameterValue( String name )
{
MAbstractParameter parameter = getParameter( uriParameterList, name );
if( parameter != null )
	{
	return parameter.getValue();
	}

parameter = getParameter( bodyParameterList, name );
if( parameter != null )
	{
	return parameter.getValue();
	}

parameter = getParameter( multipartParameterList, name );
if( parameter != null )
	{
	return parameter.getValue();
	}
return "";
}
// --------------------------------------------------------------------------------
private MAbstractParameter getParameter( List parameterList, String name )
{
if( parameterList == null )
	{
	return null;
	}
for( int i = 0; i < parameterList.size(); ++i )
	{
	MAbstractParameter parameter = ( MAbstractParameter )parameterList.get( i );
	if( parameter.getName().equals( name ) )
		{
		return parameter;
		}
	}
return null;
}
// --------------------------------------------------------------------------------
public void setParameterList( List l, int type )
throws IOException
{
if( type == MAbstractParameter.URI ) 
	{
	uriParameterList = l;
	}
else if( type == MAbstractParameter.BODY )
	{
	bodyParameterList = l;
	}
else if( type == MAbstractParameter.MULTIPART )
	{
	multipartParameterList = l;
	}
}
// --------------------------------------------------------------------------------
public void setParam2List( List l )
{
param2List = l;
}
// --------------------------------------------------------------------------------
public void applyParameterList( int type )
throws IOException
{
if( type == MAbstractParameter.URI ) 
	{
	applyUriParameterList();
	}
else if( type == MAbstractParameter.BODY )
	{
	applyBodyParameterList();
	}
else if( type == MAbstractParameter.MULTIPART )
	{
	applyMultipartParameterList();
	}
else if( type == MAbstractParameter.PARAM )
	{
	applyParam2List();
	}
}
// --------------------------------------------------------------------------------
public void applyMultipartParameterList()
throws IOException
{
if( headerExists( "Content-Type" ) )
	{
	String contentType = getHeaderValue( "Content-Type" );
	if( contentType.indexOf( "multipart/form-data" ) > -1 )
		{
		chunkToNormal();
		StringBuffer buf = new StringBuffer( getBodySize() + 4096 );
		String boundary = "--" + MRegEx.getMatch( "boundary=(.*)", contentType );
		for( int i = 0; i < multipartParameterList.size(); ++i )
			{
			buf.append( boundary );
			buf.append( "\r\n" );
			buf.append( multipartParameterList.get( i ) );
			buf.append( "\r\n" );
			}
		buf.append( boundary );
		buf.append( "--" );
		buf.append( "\r\n" );
		setBody( buf.toString() );
		setContentLength();
		}
	}
}
// --------------------------------------------------------------------------------
public void applyBodyParameterList()
throws IOException
{
chunkToNormal();
setBody( parameterListToStr( bodyParameterList, "&" ) );
setContentLength();
}
//--------------------------------------------------------------------------------
private String parameterListToStr( List parameterList, String sep )
{
StringBuffer buf = new StringBuffer( 1024 );
for( int i = 0; i < parameterList.size(); ++i )
	{
	MAbstractParameter parameter = ( MAbstractParameter )parameterList.get( i );
	if( buf.length() != 0 )
		{
		buf.append( sep );
		}
	buf.append( parameter.getName() );
	buf.append( "=" );
	buf.append( parameter.getValue() );
	}
return buf.toString();
}
// --------------------------------------------------------------------------------
public void applyUriParameterList()
throws IOException
{
String queryStr = parameterListToStr( uriParameterList, "&" );
MRequestUri ru = new MRequestUri( uri );
ru.setQuery( queryStr );
setUri( ru.toString() );
}
// --------------------------------------------------------------------------------
public void applyParam2List()
{
String param2Str = parameterListToStr( param2List, ";" );
MRequestUri ru = new MRequestUri( uri );
ru.setParams( param2Str );
setUri( ru.toString() );
}
// --------------------------------------------------------------------------------
public List getBodyParameterList()
{
return bodyParameterList;
}
// --------------------------------------------------------------------------------
public List getMultipartParameterList()
{
return multipartParameterList;
}
// --------------------------------------------------------------------------------
public List getUriParameterList()
{
return uriParameterList;
}
// --------------------------------------------------------------------------------
public void setBodyParameterList( List bodyParameterList )
{
this.bodyParameterList = bodyParameterList;
}
// --------------------------------------------------------------------------------
public void setMultipartParameterList( List multipartParameterList )
{
this.multipartParameterList = multipartParameterList;
}
// --------------------------------------------------------------------------------
public void setUriParameterList( List uriParameterList )
{
this.uriParameterList = uriParameterList;
}
// --------------------------------------------------------------------------------
public boolean isMultipartRequest()
{
return isMultipart;
}
// --------------------------------------------------------------------------------
public List getParam2List()
{
return param2List;
}
// --------------------------------------------------------------------------------
}
