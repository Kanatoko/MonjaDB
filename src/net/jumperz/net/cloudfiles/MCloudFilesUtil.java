package net.jumperz.net.cloudfiles;

import net.jumperz.net.*;
import java.io.*;
import java.net.*;
import net.jumperz.security.*;
import net.jumperz.util.*;
import java.util.*;

public class MCloudFilesUtil
{
//--------------------------------------------------------------------------------
public static MHttpRequest getUploadRequest( String containerName, String objectPath, int contentLength, Map metaData, MRequestUri storageUrl, String authToken )
throws IOException
{
MHttpRequest request = new MHttpRequest( "PUT " + storageUrl.getPath() + "/" + containerName + objectPath + " HTTP/1.1\r\n\r\n" );
request.setHeaderValue( "Host", storageUrl.getHost() );
request.setHeaderValue( "Connection", "close" );
request.setHeaderValue( "X-Auth-Token", authToken );
request.setHeaderValue( "Content-Length", contentLength + "" );

Set keySet = metaData.keySet();
Iterator p = keySet.iterator();
while( p.hasNext() )
	{
	String key = ( String )p.next();
	request.setHeaderValue( "X-Object-Meta-" + key, metaData.get( key ) + "" );
	}
return request;
}
//--------------------------------------------------------------------------------
public static MHttpRequest getContainerCdnEnabledRequest( String containerName, MRequestUri  cdnManagementUrl, String authToken, int ttl )
throws IOException
{
MHttpRequest request = new MHttpRequest( "PUT " + cdnManagementUrl.getPath() + "/" + containerName + " HTTP/1.1\r\n\r\n" );
request.setHeaderValue( "Host", cdnManagementUrl.getHost() );
request.setHeaderValue( "Connection", "close" );
request.setHeaderValue( "X-Auth-Token", authToken );
request.setHeaderValue( "X-Log-Retention", "True" );
request.setHeaderValue( "X-TTL", ttl + "" );

return request;
}
//--------------------------------------------------------------------------------
public static MHttpRequest getCreateContainerRequest( String containerName, Map metaData, MRequestUri storageUrl, String authToken )
throws IOException
{
MHttpRequest request = new MHttpRequest( "PUT " + storageUrl.getPath() + "/" + containerName + " HTTP/1.1\r\n\r\n" );
request.setHeaderValue( "Host", storageUrl.getHost() );
request.setHeaderValue( "Connection", "close" );
request.setHeaderValue( "X-Auth-Token", authToken );

Set keySet = metaData.keySet();
Iterator p = keySet.iterator();
while( p.hasNext() )
	{
	String key = ( String )p.next();
	request.setHeaderValue( "X-Container-Meta-" + key, metaData.get( key ) + "" );
	}

return request;
}
//--------------------------------------------------------------------------------
public static MHttpRequest getAuthRequest( String authUser, String authKey )
throws IOException
{
MHttpRequest request = new MHttpRequest( "GET /v1.0 HTTP/1.1\r\nHost: auth.api.rackspacecloud.com\r\n\r\n" );
request.setHeaderValue( "X-Auth-User", authUser );
request.setHeaderValue( "X-Auth-Key", authKey );
request.setHeaderValue( "Connection", "close" );
return request;
}
//--------------------------------------------------------------------------------
public static MHttpResponse sendRequestWithBodyStream( MHttpRequest request, InputStream bodyInputStream )
throws IOException
{
String host = request.getHeaderValue( "Host" );
if( host == null || host.equals( "" ) )
	{
	throw new IOException( "Host header required." );
	}

Socket socket = MSecurityUtil.sslConnect( host, 443, 8000 );
try
	{
	OutputStream out = socket.getOutputStream();

	out.write( request.getHeader() );
	MStreamUtil.connectStream( bodyInputStream, out );
	
	BufferedInputStream in = new BufferedInputStream( socket.getInputStream() );
	MHttpResponse response = new MHttpResponse( in );
	return response;
	}
finally
	{
	MSystemUtil.closeSocket( socket );
	}
}
//--------------------------------------------------------------------------------
public static MHttpResponse sendRequest( MHttpRequest request )
throws IOException
{
String host = request.getHeaderValue( "Host" );
if( host == null || host.equals( "" ) )
	{
	throw new IOException( "Host header required." );
	}

Socket socket = MSecurityUtil.sslConnect( host, 443, 8000 );
try
	{
	return getHttpResponse( socket, request );
	}
finally
	{
	MSystemUtil.closeSocket( socket );
	}
}
//--------------------------------------------------------------------------------
private static MHttpResponse getHttpResponse( Socket socket, MHttpRequest request )
throws IOException
{
OutputStream out = socket.getOutputStream();

out.write( request.getHeader() );
if( request.hasBody() )
	{
	MStreamUtil.connectStream( request.getBodyInputStream(), out );
	}

BufferedInputStream in = new BufferedInputStream( socket.getInputStream() );
MHttpResponse response = new MHttpResponse( in );
return response;
}
//--------------------------------------------------------------------------------
}