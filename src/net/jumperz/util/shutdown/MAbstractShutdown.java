package net.jumperz.util.shutdown;

import java.util.*;
import java.io.*;
import net.jumperz.util.*;

public abstract class MAbstractShutdown
{
protected static String serverHost;
protected static int serverPort;
protected static String clientHost;
protected static int clientPort;
protected static String password;

private static final String DEFAULT_SHUTDOWN_SERVER_HOST	= "127.0.0.1";
private static final String DEFAULT_SHUTDOWN_SERVER_PORT	= "9120";
private static final String DEFAULT_SHUTDOWN_CLIENT_HOST	= "127.0.0.1";
private static final String DEFAULT_SHUTDOWN_CLIENT_PORT	= "920";
private static final String DEFAULT_SHUTDOWN_PASSWORD		= "changeMe";
//--------------------------------------------------------------------------------
public static void load( String configFileName )
throws IOException
{
Properties shutdownProperties = new Properties();
MSystemUtil.loadProperties( shutdownProperties, new FileInputStream( configFileName ) );

serverHost = shutdownProperties.getProperty( "serverHost", DEFAULT_SHUTDOWN_SERVER_HOST );
serverPort = Integer.parseInt( shutdownProperties.getProperty( "serverPort", DEFAULT_SHUTDOWN_SERVER_PORT ) );
clientHost = shutdownProperties.getProperty( "clientHost", DEFAULT_SHUTDOWN_CLIENT_HOST );
clientPort = Integer.parseInt( shutdownProperties.getProperty( "clientPort", DEFAULT_SHUTDOWN_CLIENT_PORT ) );
password   = shutdownProperties.getProperty( "password" , DEFAULT_SHUTDOWN_PASSWORD );
}
//--------------------------------------------------------------------------------
public static String getClientHost() {
	return clientHost;
}

public static int getClientPort() {
	return clientPort;
}

public static String getPassword() {
	return password;
}

public static String getServerHost() {
	return serverHost;
}

public static int getServerPort() {
	return serverPort;
}

}