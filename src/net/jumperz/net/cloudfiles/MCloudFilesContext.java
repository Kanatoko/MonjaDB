package net.jumperz.net.cloudfiles;

import java.io.*;
import java.util.*;
import net.jumperz.net.*;
import net.jumperz.util.*;

public class MCloudFilesContext
implements MConstants
{
public static boolean debug = true;

private static MCloudFilesContext instance = new MCloudFilesContext();

private volatile int authState = CFILES_STATE_NO_AUTH;
private volatile int errorStatus = CFILES_ERROR_STATUS_DEFAULT;
private volatile int errorCount = 0;

private long lastAuth;

private volatile String authUser;
private volatile String authKey;
private volatile MRequestUri storageUrl;
private volatile String authToken;
private volatile String serverManagementUrl;
private volatile String storageToken;
private volatile MRequestUri cdnManagementUrl;
//private volatile MHttpResponse authResponse;
//--------------------------------------------------------------------------------
public boolean available()
{
if( errorStatus == CFILES_ERROR_STATUS_TOO_MANY_ERRORS )
	{
	return false;
	}
return true;
}
//--------------------------------------------------------------------------------
public MHttpResponse headObject( String containerName, String objectPath )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getObjectRequest( containerName, objectPath, storageUrl, authToken );
request.setMethod( "HEAD" );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
return response;
}
//--------------------------------------------------------------------------------
private void checkStatus()
throws IOException
{
if( !available() )
	{
	throw new MCloudFilesException( "CloudFiles is not available now." );
	}
if( authState == CFILES_STATE_NO_AUTH )
	{
	auth( false );
	}

long now = System.currentTimeMillis();
if( ( now - lastAuth ) > 1000 * 60 * 60 * 23 )
	{
	debug( "23 hours passed. getting new auth token." );
	auth( true );
	}
}
//--------------------------------------------------------------------------------
public MHttpResponse getObject( String containerName, String objectPath )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getObjectRequest( containerName, objectPath, storageUrl, authToken );
//debug( request );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
//debug( response.getHeaderAsString() );
return response;
}
//--------------------------------------------------------------------------------
public List getContainerList()
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getContainerListRequest( storageUrl, authToken );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );

if( response.getStatusCode() == 200 )
	{
	return Arrays.asList( response.getBodyAsString().split( "(\\r|\\n)+" ) );
	}
else
	{
	return new ArrayList();
	}
}
//--------------------------------------------------------------------------------
public List getList( String containerName )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getListRequest( containerName, storageUrl, authToken );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );

if( response.getStatusCode() == 200 )
	{
	return Arrays.asList( response.getBodyAsString().split( "(\\r|\\n)+" ) );
	}
else
	{
	return new ArrayList();
	}
}
//--------------------------------------------------------------------------------
public void deleteObject( String containerName, String objectPath )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getDeleteRequest( containerName, objectPath, storageUrl, authToken );
//debug( request );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
//debug( response );

int statusCode = response.getStatusCode();
if( statusCode == 204
 || statusCode == 404
  )
	{
	onSuccess();
	}
else
	{
	onError();
	warn( response );
	throw new MCloudFilesException( "delete failed." );
	}
}
//--------------------------------------------------------------------------------
public void uploadObject( String containerName, String objectPath, MHttpResponse objectResponse, Map metaData )
throws IOException
{
checkStatus();

objectResponse.chunkToNormal();

if( !objectResponse.hasBody() )
	{
	throw new MCloudFilesException( "Object has no body." );
	}

int contentLength = objectResponse.getBodySize();

String contentType = null;
if( objectResponse.headerExists( "Content-Type" ) )
	{
	contentType = objectResponse.getHeaderValue( "Content-Type" );	
	}

MHttpRequest request = MCloudFilesUtil.getUploadRequest( containerName, objectPath, contentLength, metaData, storageUrl, authToken, contentType ); 
MHttpResponse response = MCloudFilesUtil.sendRequestWithBodyStream( request, objectResponse.getBodyInputStream() );
//debug( response );

if( response.getStatusCode() == 201 || response.getStatusCode() == 202 )
	{
	onSuccess();
	debug( "object uploaded. " + containerName + ":" + objectPath );
	}
else
	{
	onError();
	warn( response );
	throw new MCloudFilesException( "upload failed." );
	}
}
//--------------------------------------------------------------------------------
public Map setContainerCdnEnabled( String containerName, int ttl )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getContainerCdnEnabledRequest( containerName, cdnManagementUrl, authToken, ttl );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
//debug( response );
if( response.getStatusCode() == 201 || response.getStatusCode() == 202 )
	{
	onSuccess();
	Map cdnData = new HashMap();
	cdnData.put( "X-CDN-URI", response.getHeaderValue( "X-CDN-URI" ) );
	cdnData.put( "X-CDN-SSL-URI", response.getHeaderValue( "X-CDN-SSL-URI" ) );
	cdnData.put( "X-CDN-STREAMING-URI", response.getHeaderValue( "X-CDN-STREAMING-URI" ) );
	return cdnData;
	}
else
	{
	onError();
	warn( response );
	throw new MCloudFilesException( "container cdn enabled failed." );
	}
}
//--------------------------------------------------------------------------------
public Map setContainerCdnEnabled( String containerName )
throws IOException
{
return setContainerCdnEnabled( containerName, CFILES_DEFAULT_TTL );
}
//--------------------------------------------------------------------------------
public void createContainer( String containerName, Map metaData )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getCreateContainerRequest( containerName, metaData, storageUrl, authToken );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
//debug( response );
if( response.getStatusCode() == 201 || response.getStatusCode() == 202 )
	{
	onSuccess();
	}
else
	{
	onError();
	warn( response );
	throw new MCloudFilesException( "create container failed." );
	}
}
//--------------------------------------------------------------------------------
public void createContainer( String containerName )
throws IOException
{
createContainer( containerName, new HashMap() );
}
//--------------------------------------------------------------------------------
private void onError()
{
	//try auth again
try
	{
	auth( true );
	}
catch( Exception e )
	{
	info( e );
	}

++ errorCount;
if( errorCount > CFILES_MAX_ALLOWED_ERROR )
	{
	errorStatus = CFILES_ERROR_STATUS_TOO_MANY_ERRORS;
	}
}
//--------------------------------------------------------------------------------
private void onSuccess()
{
errorCount = 0;
errorStatus = CFILES_ERROR_STATUS_OK;
}
//--------------------------------------------------------------------------------
private synchronized void auth( boolean force )
throws IOException
{
//debug( "auth:" + authState );
if( authState == CFILES_STATE_AUTH_OK
 && force == false
  )
	{
	//debug( "auth passed" );
	return;
	}

MHttpRequest request = MCloudFilesUtil.getAuthRequest( authUser, authKey );
MHttpResponse authResponse = MCloudFilesUtil.sendRequest( request );
//debug( authResponse );

if( authResponse.getStatusCode() == 204 )
	{
	onSuccess();
	storageUrl		= new MRequestUri( authResponse.getHeaderValue( "X-Storage-Url" ) );
	authToken		= authResponse.getHeaderValue( "X-Auth-Token" );
	serverManagementUrl	= authResponse.getHeaderValue( "X-Server-Management-Url" );
	storageToken		= authResponse.getHeaderValue( "X-Storage-Token" );
	cdnManagementUrl	= new MRequestUri ( authResponse.getHeaderValue( "X-CDN-Management-Url" ) );
	authState = CFILES_STATE_AUTH_OK;
	lastAuth		= System.currentTimeMillis();
	}
else
	{
	onError();
	warn( authResponse );
	throw new MCloudFilesException( "CloudFiles auth failed." );
	}
}
//--------------------------------------------------------------------------------
public MRequestUri getStorageUrl()
{
return storageUrl;
}
//--------------------------------------------------------------------------------
public void setStorageUrl( MRequestUri storageUrl )
{
this.storageUrl = storageUrl;
}
//--------------------------------------------------------------------------------
public String getAuthToken()
{
return authToken;
}
//--------------------------------------------------------------------------------
public void setAuthToken( String authToken )
{
this.authToken = authToken;
}
//--------------------------------------------------------------------------------
public String getServerManagementUrl()
{
return serverManagementUrl;
}
//--------------------------------------------------------------------------------
public void setServerManagementUrl( String serverManagementUrl )
{
this.serverManagementUrl = serverManagementUrl;
}
//--------------------------------------------------------------------------------
public String getStoragetoken()
{
return storageToken;
}
//--------------------------------------------------------------------------------
public void setStoragetoken( String storagetoken )
{
storageToken = storagetoken;
}
//--------------------------------------------------------------------------------
public MRequestUri getCdnManagementUrl()
{
return cdnManagementUrl;
}
//--------------------------------------------------------------------------------
public void setCdnManagementUrl( MRequestUri cdnManagementUrl )
{
this.cdnManagementUrl = cdnManagementUrl;
}
//--------------------------------------------------------------------------------
public String getAuthUser()
{
return authUser;
}
//--------------------------------------------------------------------------------
public String getAuthKey()
{
return authKey;
}
//--------------------------------------------------------------------------------
public static MCloudFilesContext getInstance()
{
return instance;
}
//--------------------------------------------------------------------------------
private MCloudFilesContext()
{
//singleton
//MLogServer.getInstance().addIgnoredClassName( "MCloudFilesContext" );
}
//--------------------------------------------------------------------------------
public void setAuthUser( String s )
{
authUser = s;
}
//--------------------------------------------------------------------------------
public void setAuthKey( String s )
{
authKey = s;
}
//--------------------------------------------------------------------------------
public int getStatus()
{
return authState;
}

// --------------------------------------------------------------------------------
public void log( int logLevel, Object message )
{
MLogServer.getInstance().log( "CFC", logLevel, "", message );
}
// --------------------------------------------------------------------------------
public void info( Object message )
{
log( MLogServer.log_info, message );
}
// --------------------------------------------------------------------------------
public void warn( Object message )
{
log( MLogServer.log_warn, message );
}
// --------------------------------------------------------------------------------
public void debug( Object message )
{
if( debug )
	{
	log( MLogServer.log_debug, message );
	}
}

//--------------------------------------------------------------------------------
}
