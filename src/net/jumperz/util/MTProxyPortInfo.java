package net.jumperz.util;

import java.util.*;

public class MTProxyPortInfo
{
private String ip;
private int proxyPort = 1024;
private Random random = new Random();
// --------------------------------------------------------------------------------
public MTProxyPortInfo( String s )
{
ip = s;
}
// --------------------------------------------------------------------------------
public void changePort( int clientRemotePort )
{
proxyPort = clientRemotePort + 20000 + random.nextInt( 10000 );
if( proxyPort > 65535 )
	{
	proxyPort -= 65535;
	if( proxyPort < 1024 )
		{
		proxyPort += 1024;
		}
	}
}
// --------------------------------------------------------------------------------
public String getIp()
{
return ip;
}
// --------------------------------------------------------------------------------
public int getProxyPort()
{
proxyPort++;
if( proxyPort == 65536 )
	{
	proxyPort = 1024;
	}
return proxyPort;
}
// --------------------------------------------------------------------------------
public String toString()
{
return ip + ":" + proxyPort;
}
// --------------------------------------------------------------------------------
}