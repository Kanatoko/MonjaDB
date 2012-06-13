package net.jumperz.net;

import java.net.*;
import net.jumperz.util.*;

public class MResolverLookupCommand
implements MCommand
{
private InetAddress inetAddress;
private String ip;
private Object mutex;
private MResolver resolver;
//--------------------------------------------------------------------------------
public MResolverLookupCommand( MResolver resolver, String ip, Object mutex )
throws UnknownHostException
{
this.resolver = resolver;
this.ip = ip;
this.mutex = mutex;
inetAddress = InetAddress.getByName( ip );
}
//--------------------------------------------------------------------------------
public void execute()
{
String name = inetAddress.getCanonicalHostName();
if( name.equals( ip ) )
	{
	name = "";
	}
resolver.addToDatabase( ip, name );
synchronized( mutex )
	{
	mutex.notify();
	}
}
//--------------------------------------------------------------------------------
public void breakCommand()
{
	// I can do nothing ;(
}
//--------------------------------------------------------------------------------
}