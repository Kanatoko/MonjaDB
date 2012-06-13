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

private volatile int status = CFILES_STATUS_NO_AUTH;
private volatile int errorStatus = CFILES_ERROR_STATUS_DEFAULT;
private volatile int errorCount = 0;

private volatile String authUser;
private volatile String authKey;
private volatile MRequestUri storageUrl;
private volatile String authToken;
private volatile String serverManagementUrl;
private volatile String storageToken;
private volatile MRequestUri cdnManagementUrl;
private volatile MHttpResponse authResponse;
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
private void checkStatus()
throws IOException
{
if( !available() )
	{
	throw new MCloudFilesException( "CloudFiles is not available now." );
	}
if( status == CFILES_STATUS_NO_AUTH )
	{
	auth();
	}
}
//--------------------------------------------------------------------------------
public void uploadObject( String containerName, String objectPath, MHttpResponse objectResponse, Map metaData )
throws IOException
{
checkStatus();

if( !objectResponse.hasBody() )
	{
	throw new MCloudFilesException( "Object has no body." );
	}

int contentLength = objectResponse.getBodySize();

MHttpRequest request = MCloudFilesUtil.getUploadRequest( containerName, objectPath, contentLength, metaData, storageUrl, authToken ); 
MHttpResponse response = MCloudFilesUtil.sendRequestWithBodyStream( request, objectResponse.getBodyInputStream() );
debug( response );

if( response.getStatusCode() == 201 || response.getStatusCode() == 202 )
	{
	onSuccess();
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
checkStatus();

MHttpRequest request = MCloudFilesUtil.getContainerCdnEnabledRequest( containerName, cdnManagementUrl, authToken, CFILES_DEFAULT_TTL );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
debug( response );
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
public void createContainer( String containerName, Map metaData )
throws IOException
{
checkStatus();

MHttpRequest request = MCloudFilesUtil.getCreateContainerRequest( containerName, metaData, storageUrl, authToken );
MHttpResponse response = MCloudFilesUtil.sendRequest( request );
debug( response );
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
private synchronized void auth()
throws IOException
{
MHttpRequest request = MCloudFilesUtil.getAuthRequest( authUser, authKey );
authResponse = MCloudFilesUtil.sendRequest( request );

if( authResponse.getStatusCode() == 204 )
	{
	onSuccess();
	storageUrl		= new MRequestUri( authResponse.getHeaderValue( "X-Storage-Url" ) );
	authToken		= authResponse.getHeaderValue( "X-Auth-Token" );
	serverManagementUrl	= authResponse.getHeaderValue( "X-Server-Management-Url" );
	storageToken		= authResponse.getHeaderValue( "X-Storage-Token" );
	cdnManagementUrl	= new MRequestUri ( authResponse.getHeaderValue( "X-CDN-Management-Url" ) );
	status = CFILES_STATUS_AUTH_OK;
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
return status;
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
