package net.jumperz.util;

import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import net.jumperz.net.*;
import net.jumperz.net.exception.*;
import java.util.zip.*;
import com.sun.image.codec.jpeg.*;

import java.awt.image.BufferedImage;

import javax.net.ssl.*;

public class MSystemUtil
{
//--------------------------------------------------------------------------------
public static byte[] get16B( int size )
{
byte[] buffer = new byte[ 2 ];
buffer[ 0 ] = (byte)(  size/256 );
buffer[ 1 ] = (byte)( size % 256 );
return buffer;
}
//--------------------------------------------------------------------------------
public static void signAWSRequest( MHttpRequest request, String accessKey, String secretAccessKey )
throws IOException
{
String hash = MStringUtil.getHMacSHAHash( secretAccessKey, getStringToSign( request ) );
String sig = Base64.encodeBytes( MStringUtil.hexStringToByteArray( hash ) );
request.setHeaderValue( "Authorization", "AWS " + accessKey + ":" + sig );
}
//--------------------------------------------------------------------------------
public static MHttpResponse generate503Response()
throws IOException
{
MHttpResponse response = new MHttpResponse( "HTTP/1.0 503 Service Unavailable\r\n\r\nHTTP 503 Service Unavailable" );
response.setHeaderValue( "Pragma", "no-cache" );
response.setHeaderValue( "Cache-Control", "private" );
response.setHeaderValue( "Expires", "Thu, 01 Jan 1970 00:00:00 GMT" );
response.setHeaderValue( "Content-Type", "text/plain" );
response.setContentLength();
return response;
}
//--------------------------------------------------------------------------------
public static String getStringToSign( MHttpRequest request )
throws IOException
{
StringBuffer buf = new StringBuffer( 1024 );
buf.append( request.getMethod() );
buf.append( "\n" );

	//Content-MD5
if( request.headerExists( "Content-MD5" ) )
	{
	buf.append( request.getHeaderValue( "Content-MD5" ) );
	}
else
	{
	buf.append( "" );
	}
buf.append( "\n" );

	//Content-Type
if( request.headerExists( "Content-Type" ) )
	{
	buf.append( request.getHeaderValue( "Content-Type" ) );
	}
else
	{
	buf.append( "" );
	}
buf.append( "\n" );

	//Date
if( request.headerExists( "Date" ) )
	{
	buf.append( request.getHeaderValue( "Date" ) );
	}
else
	{
	throw new IOException( "Date header not found." );
	}
buf.append( "\n" );

List headerFieldList = 	request.getHeaderFieldList();
List bufferList = new ArrayList();
for( int i = 0; i < headerFieldList.size(); ++i )
	{
	String fieldName = ( String )headerFieldList.get( i );
	if( fieldName.indexOf( "x-amz-" ) == 0 )
		{
		bufferList.add( fieldName + ":" + request.getHeaderValue( fieldName ) );
		}
	}
Collections.sort( bufferList );
for( int i = 0; i < bufferList.size(); ++i )
	{
	buf.append( bufferList.get( i ) );
	buf.append( "\n" );
	}

	//canonicalized resource
String host = request.getHeaderValue( "Host" );
if( host.indexOf( ".s3.amazonaws.com" ) == -1 )
	{
	throw new IOException( ".s3.amazonaws.com not found." );
	}
host = host.substring( 0, host.indexOf( ".s3.amazonaws.com" ) );
buf.append( "/" );
buf.append( host );

MRequestUri uri = new MRequestUri( request.getUri() );
buf.append( uri.getPath() );
//buf.append( request.getUri() ); //NG: Only path is required

return buf.toString();
}
// --------------------------------------------------------------------------------
public static void removeFile( String fileName )
{
File file = new File( fileName );
if( !file.exists() )
	{
	return;
	}

file.delete();
}
// --------------------------------------------------------------------------------
public static Socket connect( List hostList, int port )
throws IOException
{
List portList = new ArrayList( hostList.size() );
for( int i = 0; i < hostList.size(); ++i )
	{
	portList.add( port + "" );
	}
return connect( hostList, portList );
}
// --------------------------------------------------------------------------------
public static Socket connect( List hostList, List portList )
throws IOException
{
IOException ex = null;
for( int i = 0; i < hostList.size(); ++i )
	{
	try
		{
		Socket socket = connect( ( String )hostList.get( i ), MStringUtil.parseInt( portList.get( i ) ) );
		return socket;
		}
	catch( IOException e )
		{
		ex = e;
		}
	}
throw ex;
}
// --------------------------------------------------------------------------------
public static Socket connect( String host, int port )
throws IOException
{
return connect( host, port, 30 );
}
// --------------------------------------------------------------------------------
public static Socket connect( String host, int port, int connectTimeOut )
throws IOException
{
IOException ex = null;
Socket socket = null;
SocketAddress sockAddr = new InetSocketAddress( host, port );
for( int i = 0; i < 3; ++i )
	{
	try
		{
		socket = new Socket();
		socket.connect( sockAddr, connectTimeOut );
		return socket;
		}
	catch( IOException e )
		{
		ex = e;
		//System.err.println( socket );
		}
	}
throw ex;
}
// --------------------------------------------------------------------------------
public static boolean isJpeg( String fileName )
throws IOException
{
InputStream in = new FileInputStream( fileName );
try
	{
	byte[] buffer = new byte[ 1 ];
	in.read( buffer );
	return ( buffer[ 0 ] == ( byte )0xFF );
	}
finally
	{
	MStreamUtil.closeStream( in );
	}
}
// --------------------------------------------------------------------------------
/*
public static boolean isJpeg2( String fileName )
throws IOException
{
FileInputStream is = new FileInputStream( fileName );
try
	{
	JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder( is );
	BufferedImage image = decoder.decodeAsBufferedImage();
	}
catch( Exception e )
	{
	e.printStackTrace();
	return false;
	}
finally
	{
	MStreamUtil.closeStream( is );
	}
return true;
}
*/
// --------------------------------------------------------------------------------
public static String getCommandResult( String[] cmdArray )
throws IOException
{
Process process = Runtime.getRuntime().exec( cmdArray );
InputStream in = process.getInputStream();
String result = MStreamUtil.streamToString( in );

process.destroy();

return result;
}
//--------------------------------------------------------------------------------
public static String decodeParam( String value )
{
try
	{
	if( MUnicodeUrlDecoder.isUrlDecoded( value ) )
		{
		value = MUnicodeUrlDecoder.decode( value );
		}
	else
		{
		value = MStringUtil.fastUrlDecode( value, true );
		}
	}
catch( MIllegalEncodingException2 e )
	{
	}
catch( Exception e )
	{
	}
return value;
}
//--------------------------------------------------------------------------------
public static List getParametersFromRequest( MHttpRequest request )
{
List tmpList = new ArrayList();

	//loop for all parameters
List parameterList = request.getParameterList();
for( int i = 0; i < parameterList.size(); ++i )
	{
	String name = "";
	String value = "";
	MAbstractParameter parameter = ( MAbstractParameter )parameterList.get( i );
	if( parameter.getType() == MAbstractParameter.MULTIPART )
		{
		MMultipartParameter mparameter = ( MMultipartParameter )parameter;
		if( mparameter.hasFilename() )
			{
			continue; //should be ignored
			}
		else
			{
			name = parameter.getName();
			value = parameter.getValue();
			}
		}
	else if(  parameter.getType() == MAbstractParameter.URI 
	       || parameter.getType() == MAbstractParameter.BODY
	       )
		{
		name = decodeParam( parameter.getName() );
		value = decodeParam( parameter.getValue() );
		}
	
		//create new param
	tmpList.add( new MParameter( name, value, MAbstractParameter.UNKNOWN ) );
	}

return tmpList;
}
//--------------------------------------------------------------------------------
public static LinkedList toIpList( List hostOrIpList, String preferIp )
throws UnknownHostException
{
String[] hostOrIpArray = new String[ hostOrIpList.size() ];
for( int i = 0; i < hostOrIpArray.length; ++i )
	{
	hostOrIpArray[ i ] = ( String )hostOrIpList.get( i );
	}
return toIpList( hostOrIpArray, preferIp );
}
//--------------------------------------------------------------------------------
public static LinkedList toIpList( List hostOrIpList )
throws UnknownHostException
{
return toIpList( hostOrIpList, null );
}
//--------------------------------------------------------------------------------
public static LinkedList toIpList( String[] hostOrIpArray, String preferIp )
throws UnknownHostException
{
LinkedList list = toIpList( hostOrIpArray );
if( preferIp != null )
	{
	if( list.contains( preferIp ) )
		{
		list.remove( preferIp );
		list.addFirst( preferIp );
		}
	}
return list;
}
//--------------------------------------------------------------------------------
public static LinkedList toIpList( String[] hostOrIpArray )
throws UnknownHostException
{
LinkedList list = new LinkedList();
for( int i = 0; i < hostOrIpArray.length; ++i )
	{
	InetAddress[] addrArray = InetAddress.getAllByName( hostOrIpArray[ i ] );
	for( int k = 0; k < addrArray.length; ++k )
		{
		list.add( addrArray[ k ].getHostAddress() );
		}
	}
return list;
}
//--------------------------------------------------------------------------------
public static LinkedList toIpList( String hostNameString, String preferIp )
throws UnknownHostException
{
// hostNameStringは "www.example.jp" "1.2.3.4" "www.example.jp,www2.example.jp" "1.2.3.4,1.2.3.3" "www.example.jp,1.2.3.4"みたいなケースがありえる
return toIpList( hostNameString.split( ",\\s*" ), preferIp );
}
//--------------------------------------------------------------------------------
public static LinkedList toIpList( String hostNameString )
throws UnknownHostException
{
// hostNameStringは "www.example.jp" "1.2.3.4" "www.example.jp,www2.example.jp" "1.2.3.4,1.2.3.3" "www.example.jp,1.2.3.4"みたいなケースがありえる
return toIpList( hostNameString.split( ",\\s*" ) );
}
//--------------------------------------------------------------------------------
public static void shuffleWithPrefer( List list, String prefer )
{
Collections.shuffle( list );
if( prefer == null )
	{
	}
else
	{
	if( list.contains( prefer ) )
		{
		if( list.getClass().equals( LinkedList.class ) )
			{
			LinkedList ll = ( LinkedList )list;
			ll.remove( prefer );
			ll.addFirst( prefer );
			}
		else
			{
			List tmpList = new ArrayList();
			tmpList.add( prefer );
			list.remove( prefer );
			tmpList.addAll( list );
			list.clear();
			list.addAll( tmpList );
			}	
		}
	else
		{
		}
	}
}
// --------------------------------------------------------------------------------
public static List getCookieParameterFromResponse( MHttpResponse response )
{
List l = new ArrayList();
List setCookieList = response.getHeaderValueList( "Set-Cookie" );
for( int i = 0; i < setCookieList.size(); ++i )
	{
	String s = ( String )setCookieList.get( i );
	s = MRegEx.getMatch( "^[^=]{1,}=[^;]{1,}", s );
	String[] array = s.split( "=" );
	if( array.length == 2 )
		{
		String name = array[ 0 ];
		String value = array[ 1 ];
		MParameter param = new MParameter( name, value, MAbstractParameter.COOKIE );
		l.add( param );
		}
	}
return l;
}
// --------------------------------------------------------------------------------
public static String getCharsetFromHttpRequest( MHttpRequest request )
{
//MStringUtil.
return "";
}
// --------------------------------------------------------------------------------
public static String getCharsetFromHttpResponse( MHttpResponse response )
{
return getCharsetFromHttpResponse( response, MCharset.CS_ISO_8859_1 );
}
// --------------------------------------------------------------------------------
public static String getCharsetFromHttpResponse( MHttpResponse response, final String defaultCharset )
{
try
	{
	String pattern = "[^a-zA-Z0-9]+charset=([-_a-zA-Z0-9]{3,})";
	String headerStr = response.getHeaderAsString();
	String match = MRegEx.getMatchIgnoreCase( pattern, headerStr );
	if( !match.equals( "" ) )
		{
		return match;
		}
	
	if( !response.hasBody() )
		{
		return defaultCharset;
		}
	
	byte[] buf = new byte[ 4096 ];
	InputStream in = response.getBodyInputStream();
	int r = in.read( buf );
	if( r <= 0 )
		{
		return defaultCharset;
		}
	String bodyStr = new String( buf, 0, r, "ISO-8859-1" );
	match = MRegEx.getMatchIgnoreCase( pattern, bodyStr );
	if( !match.equals( "" ) )
		{
		return match;
		}
	
	bodyStr = bodyStr.toLowerCase();
	if( bodyStr.indexOf( "utf-8" ) > -1 )
		{
		return "utf-8";
		}
	else if( bodyStr.indexOf( "shift_jis" ) > -1 )
		{
		return "Shift_JIS";
		}
	else if( bodyStr.indexOf( "euc-jp" ) > -1 )
		{
		return "EUC-JP";
		}
	else if( bodyStr.indexOf( "iso-2022-jp" ) > -1 )
		{
		return "ISO-2022-JP";
		}
	}
catch( IOException ignored )
	{
	}
return defaultCharset;
}
// --------------------------------------------------------------------------------
public static MHttpRequest loadHttpRequestFromFile( String fileName, boolean allowGzip )
throws IOException
{
if( allowGzip )
	{
	BufferedInputStream bin = null;
	if( fileName.matches( ".*\\.gz$" ) )
		{
		bin = new BufferedInputStream( new GZIPInputStream( new FileInputStream( fileName ) ) );
		}
	else
		{
		bin = new BufferedInputStream( new FileInputStream( fileName ) );
		}
	try
		{
		return new MHttpRequest( bin );
		}
	finally
		{
		MStreamUtil.closeStream( bin );
		}	
	}
else
	{
	return loadHttpRequestFromFile( fileName );
	}
}
// --------------------------------------------------------------------------------
public static MHttpRequest loadHttpRequestFromFile( String fileName )
throws IOException
{
BufferedInputStream bin = new BufferedInputStream( new FileInputStream( fileName ) );
try
	{
	return new MHttpRequest( bin );
	}
finally
	{
	MStreamUtil.closeStream( bin );
	}
}
// --------------------------------------------------------------------------------
public static MHttpResponse loadHttpResponseFromFile( String fileName )
throws IOException
{
BufferedInputStream bin = new BufferedInputStream( new FileInputStream( fileName ) );
try
	{
	return new MHttpResponse( bin );
	}
finally
	{
	MStreamUtil.closeStream( bin );
	}
}
// --------------------------------------------------------------------------------
public static boolean isTextHttpResponse( MHttpResponse response )
{
if( !response.hasBody() )
	{
	return false;
	}

int bodySize = response.getBodySize();
if( bodySize == 0 )
	{
	return false;
	}

int maxCheckSize = 4096;
int checkSize = bodySize;
if( checkSize > maxCheckSize )
	{
	checkSize = maxCheckSize;
	}

byte[] buf = new byte[ checkSize ];
InputStream in = response.getBodyInputStream();

try
	{
	in.read( buf );
	}
catch( IOException e )
	{
	e.printStackTrace();
	return false;
	}

String[] typeList = new String[]{
	"javascript",
	"text",
	"css",
	};
String contentType = response.getHeaderValue( "Content-Type" );
if( contentType != null )
	{
	contentType = contentType.toLowerCase();
	for( int i = 0; i < typeList.length; ++i )
		{
		if( contentType.indexOf( typeList[ i ] ) > -1 )
			{
			return true;
			}
		}
	}
return false;
}
// --------------------------------------------------------------------------------
public static void saveHttpDataToFile( MHttpData data, String fileName )
throws IOException
{
FileOutputStream out = new FileOutputStream( fileName );
try
	{
	MStreamUtil.sendHttpDataToStream( data, out );
	}
finally
	{
	MStreamUtil.closeStream( out );
	}
}
// --------------------------------------------------------------------------------
public static Object get( Object o, Object key1, Object key2, Object key3 )
{
return ( ( Map )get( o, key1, key2 ) ).get( key3 );
}
// --------------------------------------------------------------------------------
public static Object get( Object o, Object key1, Object key2 )
{
return ( ( Map )( ( ( Map )o ).get( key1 ) ) ).get( key2 );
}
// --------------------------------------------------------------------------------
public static String getCommandResult( String command )
throws IOException
{
Process process = Runtime.getRuntime().exec( command );
InputStream in = process.getInputStream();
String result = MStreamUtil.streamToString( in );

process.destroy();

return result;
}
// --------------------------------------------------------------------------------
public static String getClassPath( Class clazz )
{
URL url;
String className = clazz.getName();
String classFileName = className.replaceAll( "\\.", "/" ) + ".class";
ClassLoader cl = clazz.getClassLoader();
if( cl == null )
	{
	url = ClassLoader.getSystemResource( classFileName );
	}
else
	{
	url = cl.getResource( classFileName );
	}

if( url.getProtocol().equals( "jar" ) )
	{
	String path = url.getPath();
	path = path.replaceFirst( "file:", "" );
	return path.substring( 0, path.indexOf( '!' ) );
	}
else
	{
	String path = url.getPath();
	return path.substring( 0, path.indexOf( classFileName ) );
	}
}
// --------------------------------------------------------------------------------
public static String getJumperzClassPath()
{
return getClassPath( MSystemUtil.class );
}
// --------------------------------------------------------------------------------
public static void shutdown( SSLSocket socket )
{
/*
Object object = socket;
String fieldName = "impl";
Field field = null;
Class clazz;

clazz = Class.forName( "javax.net.ssl.SSLSocket" );
field = clazz.getDeclaredField( fieldName );
field.setAccessible( true );
object = field.get( object );

fieldName = "fd";

clazz = Class.forName( "java.net.SocketImpl" );
field = clazz.getDeclaredField( fieldName );
field.setAccessible( true );
object = field.get( object );

clazz = Class.forName( "java.io.FileDescriptor" );
field = clazz.getDeclaredField( fieldName );
field.setAccessible( true );

fd = field.getInt( object );
*/
}
// --------------------------------------------------------------------------------
public static int getFD( ServerSocket socket )
{
int fd = -1;
try
	{
	Object object = socket;
	String fieldName = "impl";
	Field field = null;
	Class clazz;
	
	clazz = Class.forName( "java.net.ServerSocket" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	object = field.get( object );
	
	fieldName = "fd";
	
	clazz = Class.forName( "java.net.SocketImpl" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	object = field.get( object );
	
	clazz = Class.forName( "java.io.FileDescriptor" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	
	fd = field.getInt( object );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}

return fd;
}
//--------------------------------------------------------------------------------
public static void closeNativeSocket( Socket socket )
{
if( socket == null )
	{
	return;
	}
try
	{
	Object object = socket;
	String fieldName = "impl";
	Field field = null;
	Method method = null;
	Class clazz;
	
	clazz = Class.forName( "java.net.Socket" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	object = field.get( object );
	
	clazz = Class.forName( "java.net.PlainSocketImpl" );
	method = clazz.getDeclaredMethod( "socketClose0", new Class[] { Boolean.TYPE } );

	method.setAccessible( true );
	method.invoke( object, new Object[]{ new Boolean( false ) } );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}
}
// --------------------------------------------------------------------------------
public static int getFD( Socket socket )
{
int fd = -1;
try
	{
	Object object = socket;
	String fieldName = "impl";
	Field field = null;
	Class clazz;
	
	clazz = Class.forName( "java.net.Socket" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	object = field.get( object );
	
	fieldName = "fd";
	
	clazz = Class.forName( "java.net.SocketImpl" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	object = field.get( object );
	
	clazz = Class.forName( "java.io.FileDescriptor" );
	field = clazz.getDeclaredField( fieldName );
	field.setAccessible( true );
	
	fd = field.getInt( object );
	}
catch( Exception e )
	{
	e.printStackTrace();
	}

return fd;
}
// --------------------------------------------------------------------------------
public static void clearDir( String dirName )
throws IOException
{
clearDir( new File( dirName ) );
}
// --------------------------------------------------------------------------------
public static void clearDir( File dir )
throws IOException
{
File[] files = dir.listFiles();
for( int i = 0; i < files.length; ++i )
	{
	if( !files[ i ].delete() )
		{
		throw new IOException( "Could not delete file: " + files[ i ].getCanonicalPath() );
		}
	}
}
// --------------------------------------------------------------------------------
public static boolean waitForFile( File file )
throws IOException
{
for( int i = 0; i < 30; ++i )
	{
	if( file.exists() )
		{
		return true;
		}
	sleep( 1000 );
	}
return false;
}
// --------------------------------------------------------------------------------
public static boolean renameFile( String from, String to, boolean removeDest )
throws IOException
{
File fromFile = new File( from );
File toFile = new File( to );
if( toFile.exists() )
	{
	if( !toFile.delete() )
		{
		System.err.println( "delete failed:" + fromFile.getAbsolutePath() + " " + toFile.getAbsolutePath() );
		return false;
		}
	}
return fromFile.renameTo( toFile );
}
// --------------------------------------------------------------------------------
public static boolean renameFile( String from, String to )
throws IOException
{
return renameFile( from, to, true );
}
// --------------------------------------------------------------------------------
public static void copyFile( String from, String to )
throws IOException
{
FileInputStream in = new FileInputStream( from );
FileOutputStream out = new FileOutputStream( to );
try
	{
	MStreamUtil.connectStream( in, out );
	}
finally
	{
	in.close();
	out.close();
	}
}
//--------------------------------------------------------------------------------
public static String createDir( String dirName )
throws IOException
{
File dir = new File( dirName );
if( dir.exists() )
	{
	if( !dir.isDirectory() )
		{
		throw new IOException( "Couldn't make directory: " + dir.getCanonicalPath() );
		}
	}
else
	{
	if( !dir.mkdirs() )
		{
		sleep( 300 );
		if( !dir.isDirectory() )
			{
			throw new IOException( "Couldn't make directory: " + dir.getCanonicalPath() );		
			}
		}
	}
return dir.getCanonicalPath();
}
//--------------------------------------------------------------------------------
public static void sleep( long time )
{
try
	{
	Thread.sleep( time );
	}
catch( InterruptedException e )
	{
	e.printStackTrace();
	}
}
// --------------------------------------------------------------------------------
public static void loadProperties( Properties prop, InputStream in )
throws IOException
{
try
	{
	prop.load( in );
	}
finally
	{
	in.close();
	}
}
// --------------------------------------------------------------------------------
public static void deepCopy( Collection from, Collection to )
{
to.clear();

Iterator p = from.iterator();
while( p.hasNext() )
	{
	MCloneable o1 = ( MCloneable )p.next();
	Object o2 = o1.getClone();
	to.add( o2 );
	}
}
// --------------------------------------------------------------------------------
public static void closeSocket( ServerSocket s )
{
if( s == null )
	{
	return;
	}
try
	{
	if( !s.isClosed() )
		{	
		s.close();
		}
	}
catch( IOException e )
	{
	}
}
// --------------------------------------------------------------------------------
public static void closeSocket( Socket s )
{
if( s == null )
	{
	return;
	}
try
	{
	if( !s.isClosed() )
		{	
		s.close();
		}
	}
catch( IOException e )
	{
	}
}
// --------------------------------------------------------------------------------
public static String getIpFromLong( long l )
{
String s = Long.toHexString( l );
if( s.length() == 7 )
	{
	s = "0" + s;
	}
StringBuffer buf = new StringBuffer( 15 );
buf.append( Integer.parseInt( s.substring( 6, 8 ), 16 ) );
buf.append( "." );
buf.append( Integer.parseInt( s.substring( 4, 6 ), 16 ) );
buf.append( "." );
buf.append( Integer.parseInt( s.substring( 2, 4 ), 16 ) );
buf.append( "." );
buf.append( Integer.parseInt( s.substring( 0, 2 ), 16 ) );

return buf.toString();
}
//--------------------------------------------------------------------------------
}