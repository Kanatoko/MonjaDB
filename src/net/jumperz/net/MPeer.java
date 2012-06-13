package net.jumperz.net;

import java.net.*;
import java.io.*;

public class MPeer
{
private int port;
private String host;
//--------------------------------------------------------------------------------
public MPeer( String in_host, int in_port )
{
host = in_host;
port = in_port;
}
//--------------------------------------------------------------------------------
public MPeer( Socket socket )
{
InetAddress inetAddress = socket.getInetAddress();
host = inetAddress.getHostAddress();
port = socket.getPort();
}
//--------------------------------------------------------------------------------
public String getHost() {

	return host;
}
public void setHost( String host ) {

	this.host = host;
}
public int getPort() {

	return port;
}
public void setPort( int port ) {

	this.port = port;
}
//--------------------------------------------------------------------------------
public boolean equals( Object o )
{
MPeer other = ( MPeer )o;

if( host == null
 || other.host == null
  )
	{
	return false;
	}

if( other.host.equals( host )
 && other.port == port )
	{
	return true;
	}
return false;
}
//--------------------------------------------------------------------------------
public String toString()
{
return "MPeer:" + host + ":" + port;
}
//--------------------------------------------------------------------------------
public Socket getSocket()
throws IOException, UnknownHostException
{
return new Socket( host, port );
}
//--------------------------------------------------------------------------------
}