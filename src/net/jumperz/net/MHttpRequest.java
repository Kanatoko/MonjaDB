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
private int bodyType;

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

public static final int BODY_TYPE_UNKNOWN	= -1;
public static final int BODY_TYPE_URLENCODED	= 0;
public static final int BODY_TYPE_MULTIPART	= 1;
public static final int BODY_TYPE_PLAINTEXT	= 2;
public static final int BODY_TYPE_JSON		= 3;
public static final int BODY_TYPE_XML		= 4;
public static final int BODY_TYPE_DWR		= 5;

public static boolean strictRequestLine = true;
public static boolean useMultipartParameter2 = true;
public static boolean testMultipartParameter2 = false;

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
	String name = "";
	String value = "";
	int index = pairList[ i ].indexOf( '=' );
	if( index > -1 )
		{
		name  = pairList[ i ].substring( 0, index );
		value = "";
		try
			{
			//value = MStringUtil.urlDecode( pairList[ i ].substring( index + 1 ) );		
			//value = MStringUtil.fastUrlDecode( pairList[ i ].substring( index + 1 ) );		
			value = pairList[ i ].substring( index + 1 );
			}
		catch( IllegalArgumentException ignored )
			{
			}
		}
	else
		{
		name = pairList[ i ];
		if( name.equals( "" ) )
			{
			continue;
			}
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
			bodyType = BODY_TYPE_URLENCODED;
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
			bodyType = BODY_TYPE_MULTIPART;
			multipartParameterList = null;
			
			//parse later
			}
		else if(  contentType.toLowerCase().indexOf( "/json" ) > -1 
		       || contentType.toLowerCase().indexOf( "-json" ) > -1
			)
			{
			bodyType = BODY_TYPE_JSON;
			}
		else if( contentType.toLowerCase().indexOf( "/xml" ) > -1
		      || contentType.toLowerCase().indexOf( "-xml" ) > -1
		       )
			{
			bodyType = BODY_TYPE_XML;
			}
		else if( contentType.toLowerCase().indexOf( "text/plain" ) > -1 )
			{
			bodyType = BODY_TYPE_PLAINTEXT;
			}
		else
			{
			bodyType = BODY_TYPE_UNKNOWN;
			}
		}
	}
}
//--------------------------------------------------------------------------------
private static void p( Object o )
{
System.out.println( o );
}
//--------------------------------------------------------------------------------
private boolean checkLists( List l1, List l2 )
{
if( l1.size() != l2.size() )
	{
	return false;
	}

for( int i = 0; i < l1.size(); ++i )
	{
	MMultipartParameter param1 = ( MMultipartParameter )l1.get( i );
	
	MMultipartParameter param2 = ( MMultipartParameter )l2.get( i );
	if( param2.getValueSize() > 8192 )
		{
		//p( param2.getValue() );
		}
	
	if( !param1.getName().equals( param2.getName() ) )
		{
		return false;
		}
	if( param1.getValueSize() != param2.getValueSize() )
		{
		return false;
		}
	if( !param1.getHeaderList().equals( param2.getHeaderList() ) )
		{
		return false;
		}
	
	if( param1.hasFilename() )
		{
		if( !param2.hasFilename() )
			{
			return false;
			}
		p( param1.getFileName() );
		if( !param1.getFileName().equals( param2.getFileName() ) )
			{
			return false;
			}
		}
	}

return true;
}
//--------------------------------------------------------------------------------
private void onMBufferFound( MBuffer mbuffer )
{
if( internalMBufferList == null )
	{
	internalMBufferList = new ArrayList();
	}
internalMBufferList.add( mbuffer );
}
//--------------------------------------------------------------------------------
/*
 * Side effect: onMBufferFound
 */
private List parseMultipartParameters2( String boundary )
throws IOException
{
final List _list = new ArrayList();
final List _mbufferList = parseMultipartParameter( bodyBuffer.getInputStream(), boundary, 1024 * 4 );
for( int i = 0; i < _mbufferList.size(); ++i )
	{
	MBuffer mbuffer = ( MBuffer )_mbufferList.get( i );
	onMBufferFound( mbuffer );
	
	_list.add( new MMultipartParameter2( mbuffer ) );
	}
return _list;
}
//--------------------------------------------------------------------------------
public static List parseMultipartParameter( InputStream in, String boundary, int bufSize )
throws IOException
{
/*
Content-Length: ...

--abc
Content-Disposition: form-data; name="a"

b
--abc
Content-Disposition: form-data; name="c"

d
--abc
Content-Disposition: form-data; name="foobar"

sstattack
--abc--

--abcを探し、その次の2バイトに注目する
\r\nの場合はその次のバイトからデータが開始される
--の場合はパース終了
--abcXXが探すべき文字列

dmzSize = boundaryLength + 4 -1 = boundaryLength + 3

*/
if( boundary.length() > 1024 )
	{
	throw new IOException( "Invalid boudary. Too long." + boundary.substring( 0, 100 ) );
	}

List mbufferList = new ArrayList();
final String target = "\r\n--" + boundary + "XX"; //ダミーのXXも含む
final int dmzSize = target.length() - 1;
final int targetSize = target.length();
byte[] targetBuf = target.getBytes(); //boundaryはまぁUS-ASCIIだろうから、デフォルトの文字コードでOKとする
byte[] buf = new byte[ bufSize ];
byte[] dmzBuf = new byte[ dmzSize ];
Arrays.fill( dmzBuf, 0, dmzBuf.length -1 , ( byte )0x00 );
dmzBuf[ dmzBuf.length - 2 ] = ( byte )0x0D;
dmzBuf[ dmzBuf.length - 1 ] = ( byte )0x0A;
final int readMaxSize = buf.length - dmzSize;
int searchIndex = 0;
MBuffer mbuffer = null;
boolean lastBoundaryFound = false;
int mbufferP = dmzSize - 2;

parse:
while( true )
	{
		//前回のDMZデータをコピー
	for( int i = 0; i < dmzSize; ++i )
		{
		buf[ i ] = dmzBuf[ i ];
		}
	
		//新しいデータをストリームから読み込む
	int read;
	while( true )
		{
		read = in.read( buf, dmzSize, readMaxSize );
		//read = in.read( buf, dmzSize, 1 );
		if( read != 0 )
			{
			break;
			}
		}
	
	if( read == -1 )
		{
		//p( "stream end" );
		//TODO: Invalid?
		break;
		}
	
		//次回比較用にDMZデータを取得
	for( int i = 0; i < dmzSize; ++i )
		{
		dmzBuf[ i ] = buf[ read + i ];
		}
	
		//buf上にtargetBufと一致する箇所があるか検索開始
	for( int bufIndex = mbufferP; bufIndex < read; ++bufIndex )
		{
		boolean notFound = false;
		for( int targetIndex = 0; targetIndex < targetSize - 2; ++targetIndex ) //XXは探さないので -2 に注意
			{
			if( buf[ bufIndex + targetIndex ] != targetBuf[ targetIndex ] )
				{
				notFound = true;
				break;
				}
			}
		
		if( !notFound ) // found
			{
				//--abc\r\n か --abc-- かを見分ける
			if( buf[ bufIndex + targetSize - 2 ] == ( byte )0x2d
			 && buf[ bufIndex + targetSize - 1 ] == ( byte )0x2d
			  )
				{
					//--abc-- found
				//p( "lastBoundaryFound" );
				lastBoundaryFound = true;
				}
			else if( buf[ bufIndex + targetSize - 2 ] == ( byte )0x0d
			      && buf[ bufIndex + targetSize - 1 ] == ( byte )0x0a 
				)
				{
					//--abc\r\n found
				}
			else
				{
				throw new IOException( "Invalid boundary found:" + new String( buf, bufIndex, targetSize ) );
				}
			
				//見つかった場所までのデータをmbufferに書き込む
			if( mbuffer != null )
				{
				mbuffer.write( buf, mbufferP, bufIndex - mbufferP );
				mbuffer.close();
				}
			
			if( lastBoundaryFound )
				{
				break parse;
				}
			
			mbuffer = new MBuffer();
			mbufferList.add( mbuffer );
			mbufferP = bufIndex + targetSize;
			bufIndex += targetSize;
			}
		}
	
		//buf上の残りのデータをmbufferに書き込んでおく
	if( mbuffer != null )
		{
		int dataSize = read - mbufferP;
		if( dataSize > 0 )
			{
			mbuffer.write( buf, mbufferP, dataSize );
			mbufferP += dataSize;
			}
		}
	mbufferP -= read;
	}

return mbufferList;
}
// --------------------------------------------------------------------------------
/*
 * Almost no side effect ( getBodyInputStream() may create new bodyBuffer instance )
 */
private List parseMultipartParameters( String boundary )
throws IOException
{
List _list = new ArrayList();
StringBuffer _buf = new StringBuffer( 8192 );
_buf.append( "\r\n" );
_buf.append( MStreamUtil.streamToString( getBodyInputStream() ) );
String _bodyStr = _buf.toString();
int index =  _bodyStr.indexOf( "\r\n--" + boundary + "--" );
if( index == -1 )
	{
	throw new IOException( "Invalid format. Last boundary not found." );
	}

_bodyStr = _bodyStr.substring( 0, index );

List array = MStringUtil.split( _bodyStr, "\r\n--" + boundary + "\r\n" );
for( int i = 0; i < array.size(); ++i )
	{
	String _s = ( String )array.get( i );
	if( _s.length() == 0 )
		{
		continue;
		}
	_list.add( new MMultipartParameter( _s ) );
	}
return _list;
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
headerLengthForStream += line.length() + reader.getLastDelimiterSize();

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
byte[] buf = null;
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
	
	buf = bufferStream.toByteArray();
	}
catch( Exception e )
	{
	//MLogger.getInstance().Log( e.toString() );
	e.printStackTrace();
	buf = new byte[]{};
	}
finally
	{
	MStreamUtil.closeStream( bufferStream );
	}
return buf;
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
	return MSystemUtil.avoidNullList( uriParameterList );
	}
else if( type == MAbstractParameter.BODY )
	{
	return MSystemUtil.avoidNullList( bodyParameterList );
	}
else if( type == MAbstractParameter.MULTIPART )
	{
	return MSystemUtil.avoidNullList( getMultipartParameterList() );
	}
return new ArrayList();
}
// --------------------------------------------------------------------------------
public List getParameterList()
{
List l = new ArrayList();
MSystemUtil.addAll( l, uriParameterList );
MSystemUtil.addAll( l, bodyParameterList );
MSystemUtil.addAll( l, getMultipartParameterList() );
return l;
}
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

parameter = getParameter( getMultipartParameterList(), name );
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
	//is lazy?
if( isMultipartRequest() )
	{
	if( multipartParameterList == null )
		{
		String contentType = getHeaderValue( "Content-Type" );
		if( contentType != null )
			{
			String boundary = MRegEx.getMatch( "boundary=(.*)", contentType );
			try
				{
				multipartParameterList = new ArrayList();
				if( testMultipartParameter2 )
					{
					List l1 = parseMultipartParameters( boundary );
					List l2 = parseMultipartParameters2( boundary );
					p( "--MHttpRequest-check  " + checkLists( l1, l2 ) );
					
					multipartParameterList.addAll( l2 );
					}
				else if( useMultipartParameter2 )
					{					
					multipartParameterList.addAll( parseMultipartParameters2( boundary ) );
					}
				else
					{
					multipartParameterList.addAll( parseMultipartParameters( boundary ) );
					}
				}
			catch( IOException e )
				{
				e.printStackTrace();
				}
			}
		}
	}

if( multipartParameterList == null )
	{
	multipartParameterList = new ArrayList();
	}
return multipartParameterList;
}
// --------------------------------------------------------------------------------
public List getUriParameterList()
{
return MSystemUtil.avoidNullList( uriParameterList );
}
// --------------------------------------------------------------------------------
/*public void setBodyParameterList( List bodyParameterList )
{
this.bodyParameterList = bodyParameterList;
}*/
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
return hasBody() && getBodyType() == BODY_TYPE_MULTIPART;
}
// --------------------------------------------------------------------------------
public List getParam2List()
{
return param2List;
}
//--------------------------------------------------------------------------------
public int getBodyType()
{
return bodyType;
}
// --------------------------------------------------------------------------------
}
